/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.array.operation.COPY;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure1D;

/**
 * A sparse, primitive double based, LU decomposition with support for Forrest-Tomlin updates (column
 * replacement). It is designed specifically to be used with the {@link LinearSolver} but also implements the
 * standard {@link MatrixDecomposition} interfaces.
 * <p>
 * There are two ways to factorise:
 * <ul>
 * <li>{@link #decompose(Access2D.Collectable)} pivots on the columns in their natural order, choosing each
 * pivot row by threshold partial pivoting. [L] is unit lower triangular and [U] upper triangular, as
 * specified by {@link LU}. Only exact zeros are dropped.
 * <li>{@link #factor(R064CSC, int[])} is intended for simplex bases. Column and row singletons are pivoted
 * first, and what remains with Markowitz pivot selection and threshold partial pivoting. That permutes the
 * columns too, so {@link #getU()} is a column permutation of an upper triangular matrix. Values smaller than
 * 1e-14 are dropped.
 * </ul>
 * Rectangular and rank deficient matrices can be factorised: rows and columns without nonzero pivot
 * candidates are paired up, last, with zero pivots. The sparse solves
 * ({@link #ftranColumn(R064CSC, int, DensityTrackingArray)} and
 * {@link #btranUnit(int, DensityTrackingArray)}) and the updates ({@link #updateColumn(int, R064CSC, int)})
 * require a square matrix. An update reuses the partial results of the preceding sparse solves.
 */
public final class SparseLU extends AbstractDecomposition<Double, R064Store> implements LU<Double> {

    /**
     * Depth-first search through the dependency graph of a triangular solve (Gilbert-Peierls): finds the
     * nodes that a sparse right hand side reaches, before any arithmetic is done. Nodes are marked visited by
     * stamping them. The solves that are not hyper-sparse use the same marks to keep track of which positions
     * are already listed in an index.
     */
    static final class DepthFirstSearch {

        /**
         * The nodes being explored, with the cursors and limits of their adjacency lists.
         */
        private final int[] myStackCursor;
        private final int[] myStackLimit;
        private final int[] myStackNode;
        private int myStamp = 0;
        /**
         * Per node, the stamp it was last visited with. A node is visited when this equals the current stamp.
         */
        private final int[] myVisited;
        /**
         * The reached nodes, in postorder, as found by the most recent {@link #reach}. Processing them in
         * reverse order respects the dependencies.
         */
        final int[] postorder;

        DepthFirstSearch(final int nbNodes) {

            super();

            postorder = new int[nbNodes];

            myStackCursor = new int[nbNodes];
            myStackLimit = new int[nbNodes];
            myStackNode = new int[nbNodes];
            myVisited = new int[nbNodes];
        }

        /**
         * A new stamp, making every node unvisited.
         */
        private void newStamp() {
            if (myStamp == Integer.MAX_VALUE) {
                Arrays.fill(myVisited, 0);
                myStamp = 0;
            }
            myStamp++;
        }

        /**
         * Put a node on the stack, at the given height, with the cursor and limit of its adjacency list.
         */
        private void push(final int top, final int node, final int[] nodeToList, final int[] listStart, final int[] listEnd) {
            myStackNode[top] = node;
            int list = nodeToList != null ? nodeToList[node] : node;
            if (list >= 0) {
                myStackCursor[top] = listStart[list];
                myStackLimit[top] = listEnd != null ? listEnd[list] : listStart[list + 1];
            } else {
                myStackCursor[top] = 0;
                myStackLimit[top] = 0;
            }
        }

        /**
         * Search, from the seed nodes, through a graph given by adjacency lists. Edges with a zero weight
         * (deleted entries) are skipped. The reached nodes are stored in {@link #postorder}, and are marked
         * visited with a new stamp.
         *
         * @param seeds      The seed nodes
         * @param nbSeeds    The number of seed nodes
         * @param nodeToList Maps a node to its adjacency list, -1 if it has none (null means identity)
         * @param listStart  The start of each adjacency list
         * @param listEnd    The end of each adjacency list (null means the start of the next list)
         * @param adjacency  The adjacency lists' target nodes
         * @param weights    The edge weights (null means no zero weights)
         * @return The number of reached nodes
         */
        int reach(final int[] seeds, final int nbSeeds, final int[] nodeToList, final int[] listStart, final int[] listEnd, final int[] adjacency,
                final double[] weights) {

            this.newStamp();

            int nbReached = 0;

            for (int k = 0; k < nbSeeds; k++) {

                if (!this.visit(seeds[k])) {
                    continue;
                }

                int top = 0;
                this.push(top, seeds[k], nodeToList, listStart, listEnd);

                while (top >= 0) {

                    int cursor = myStackCursor[top];
                    int limit = myStackLimit[top];
                    int child = -1;

                    while (cursor < limit) {
                        int candidate = adjacency[cursor];
                        boolean edge = weights == null || weights[cursor] != ZERO;
                        cursor++;
                        if (edge && this.visit(candidate)) {
                            child = candidate;
                            break;
                        }
                    }

                    if (child >= 0) {
                        myStackCursor[top] = cursor;
                        this.push(++top, child, nodeToList, listStart, listEnd);
                    } else {
                        postorder[nbReached++] = myStackNode[top--];
                    }
                }
            }

            return nbReached;
        }

        /**
         * Mark a node visited, with the current stamp.
         *
         * @return true if it was not visited already
         */
        boolean visit(final int node) {
            if (myVisited[node] == myStamp) {
                return false;
            }
            myVisited[node] = myStamp;
            return true;
        }

        /**
         * A new stamp, with these nodes (and only these) visited.
         */
        void visitOnly(final int[] nodes, final int nbNodes) {
            this.newStamp();
            for (int k = 0; k < nbNodes; k++) {
                myVisited[nodes[k]] = myStamp;
            }
        }

    }

    /**
     * A sequence of eta vectors (elementary transformations), each with a pivot row and a list of (row,
     * value) entries, stored compactly. [L] is stored, and applied, as column etas: eta t subtracts multiples
     * of its pivot row's value from the rows in its list. The Forrest-Tomlin R-etas are row etas: eta e
     * subtracts a combination of the rows in its list from its pivot row. For [L] there is also a row-wise
     * copy, used by the sparse solves with [L] transposed.
     */
    static final class EtaSequence {

        /**
         * The number of (completed) etas.
         */
        int count = 0;
        /**
         * The eta with this pivot row, or -1. Built by {@link #buildRowWise(int)}.
         */
        int[] etaOfRow = null;
        /**
         * The rows of the entries.
         */
        int[] index;
        /**
         * The pivot row of each eta.
         */
        int[] pivotRow;
        /**
         * Row-wise copy, built by {@link #buildRowWise(int)}: for each row, the pivot rows of the etas it has
         * an entry in, and the entries' values. Row i's entries are {@code rowStart[i]} to
         * {@code rowStart[i + 1]}.
         */
        int[] rowStart = null;
        int[] rowTarget = null;
        double[] rowValue = null;
        /**
         * The number of entries, including those of an eta not yet completed.
         */
        int size = 0;
        /**
         * Eta t's entries are {@code start[t]} to {@code start[t + 1]}.
         */
        int[] start;
        /**
         * The values of the entries.
         */
        double[] value;

        EtaSequence(final int nbEtas, final int nbEntries) {

            super();

            pivotRow = new int[Math.max(1, nbEtas)];
            start = new int[pivotRow.length + 1];
            index = new int[Math.max(16, nbEntries)];
            value = new double[index.length];
        }

        /**
         * Add an entry to the eta being built.
         */
        void add(final int row, final double entry) {
            if (size >= index.length) {
                index = COPY.grow(index, size + 1);
                value = COPY.grow(value, size + 1);
            }
            index[size] = row;
            value[size] = entry;
            size++;
        }

        /**
         * Build {@link #etaOfRow} and the row-wise copy. The entries are placed using {@link #rowStart} as
         * cursors, which shifts it one row, and it is then shifted back.
         */
        void buildRowWise(final int nbRows) {

            if (etaOfRow == null || etaOfRow.length != nbRows) {
                etaOfRow = new int[nbRows];
                rowStart = new int[nbRows + 1];
                rowTarget = new int[Math.max(16, size)];
                rowValue = new double[rowTarget.length];
            } else {
                rowTarget = COPY.grow(rowTarget, size);
                rowValue = COPY.grow(rowValue, size);
            }

            Arrays.fill(etaOfRow, -1);
            Arrays.fill(rowStart, 0);
            for (int t = 0; t < count; t++) {
                etaOfRow[pivotRow[t]] = t;
                for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                    rowStart[index[p] + 1]++;
                }
            }
            for (int i = 0; i < nbRows; i++) {
                rowStart[i + 1] += rowStart[i];
            }

            for (int t = 0; t < count; t++) {
                for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                    int q = rowStart[index[p]]++;
                    rowTarget[q] = pivotRow[t];
                    rowValue[q] = value[p];
                }
            }
            for (int i = nbRows; i > 0; i--) {
                rowStart[i] = rowStart[i - 1];
            }
            rowStart[0] = 0;
        }

        /**
         * Complete the eta being built, with the entries added since the previous one was completed.
         */
        void close(final int row) {
            pivotRow = COPY.grow(pivotRow, count + 1);
            start = COPY.grow(start, count + 2);
            pivotRow[count] = row;
            start[++count] = size;
        }

        void reset() {
            count = 0;
            size = 0;
            start[0] = 0;
        }

        /**
         * Apply the etas, in order, as column etas, maintaining the index of the argument. Hyper-sparse: a
         * depth-first search finds the rows that will be nonzero, and only the etas of those rows are
         * applied. Otherwise all etas, listing new nonzeros as they appear. When done, the listed positions
         * are marked visited with the current stamp of the search.
         */
        void solveColumnEtas(final DensityTrackingArray arg, final DepthFirstSearch search, final boolean hyper) {

            int[] indices = arg.indices();
            int nbListed = arg.countNonzeros();

            if (hyper) {

                int nbReached = search.reach(indices, nbListed, etaOfRow, start, null, index, null);

                for (int k = nbReached - 1; k >= 0; k--) {
                    int row = search.postorder[k];
                    int t = etaOfRow[row];
                    if (t >= 0) {
                        double pivot = arg.values[row];
                        if (pivot != ZERO) {
                            for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                                arg.values[index[p]] -= value[p] * pivot;
                            }
                        }
                    }
                }

                System.arraycopy(search.postorder, 0, indices, 0, nbReached);
                nbListed = nbReached;

            } else {

                search.visitOnly(indices, nbListed);

                for (int t = 0; t < count; t++) {
                    double pivot = arg.values[pivotRow[t]];
                    if (pivot != ZERO) {
                        for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                            int i = index[p];
                            arg.values[i] -= value[p] * pivot;
                            if (search.visit(i)) {
                                indices[nbListed++] = i;
                            }
                        }
                    }
                }
            }

            arg.setNonzeroCount(nbListed);
        }

        /**
         * Apply the etas, in order, as column etas.
         */
        void solveColumnEtas(final double[] arg) {
            for (int t = 0; t < count; t++) {
                double pivot = arg[pivotRow[t]];
                if (pivot != ZERO) {
                    for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                        arg[index[p]] -= value[p] * pivot;
                    }
                }
            }
        }

        /**
         * Apply the transposed column etas, maintaining the index of the argument, in scatter form using the
         * row-wise copy. Hyper-sparse: a depth-first search finds the rows that will be nonzero, and only
         * those are processed. Otherwise all rows, in reverse pivot order, listing new nonzeros as they
         * appear (the listed positions must be marked visited with the current stamp of the search).
         *
         * @param order The rows in pivot order
         */
        void solveColumnEtasTransposed(final DensityTrackingArray arg, final DepthFirstSearch search, final boolean hyper, final int[] order) {

            int[] indices = arg.indices();
            int nbListed = arg.countNonzeros();

            if (hyper) {

                int nbReached = search.reach(indices, nbListed, null, rowStart, null, rowTarget, null);

                for (int k = nbReached - 1; k >= 0; k--) {
                    int i = search.postorder[k];
                    double pivot = arg.values[i];
                    if (pivot != ZERO) {
                        for (int p = rowStart[i], limit = rowStart[i + 1]; p < limit; p++) {
                            arg.values[rowTarget[p]] -= rowValue[p] * pivot;
                        }
                    }
                }

                System.arraycopy(search.postorder, 0, indices, 0, nbReached);
                nbListed = nbReached;

            } else {

                for (int k = order.length - 1; k >= 0; k--) {
                    int i = order[k];
                    double pivot = arg.values[i];
                    if (pivot != ZERO) {
                        for (int p = rowStart[i], limit = rowStart[i + 1]; p < limit; p++) {
                            int target = rowTarget[p];
                            arg.values[target] -= rowValue[p] * pivot;
                            if (search.visit(target)) {
                                indices[nbListed++] = target;
                            }
                        }
                    }
                }
            }

            arg.setNonzeroCount(nbListed);
        }

        /**
         * Apply the transposed column etas, in reverse order.
         */
        void solveColumnEtasTransposed(final double[] arg) {
            for (int t = count - 1; t >= 0; t--) {
                int row = pivotRow[t];
                double sum = arg[row];
                for (int p = start[t], limit = start[t + 1]; p < limit; p++) {
                    sum -= value[p] * arg[index[p]];
                }
                arg[row] = sum;
            }
        }

        /**
         * Apply the etas, in order, as row etas, maintaining the index of the argument. The listed positions
         * must be marked visited with the current stamp of the search.
         */
        void solveRowEtas(final DensityTrackingArray arg, final DepthFirstSearch search) {

            int[] indices = arg.indices();
            int nbListed = arg.countNonzeros();

            for (int e = 0; e < count; e++) {
                int row = pivotRow[e];
                double sum = arg.values[row];
                for (int p = start[e], limit = start[e + 1]; p < limit; p++) {
                    sum -= value[p] * arg.values[index[p]];
                }
                arg.values[row] = sum;
                if (sum != ZERO && search.visit(row)) {
                    indices[nbListed++] = row;
                }
            }

            arg.setNonzeroCount(nbListed);
        }

        /**
         * Apply the etas, in order, as row etas.
         */
        void solveRowEtas(final double[] arg) {
            for (int e = 0; e < count; e++) {
                int row = pivotRow[e];
                double sum = arg[row];
                for (int p = start[e], limit = start[e + 1]; p < limit; p++) {
                    sum -= value[p] * arg[index[p]];
                }
                arg[row] = sum;
            }
        }

        /**
         * Apply the transposed row etas, in reverse order, maintaining the index of the argument. When done,
         * the listed positions are marked visited with the current stamp of the search.
         */
        void solveRowEtasTransposed(final DensityTrackingArray arg, final DepthFirstSearch search) {

            int[] indices = arg.indices();
            int nbListed = arg.countNonzeros();

            search.visitOnly(indices, nbListed);

            for (int e = count - 1; e >= 0; e--) {
                double pivot = arg.values[pivotRow[e]];
                if (pivot != ZERO) {
                    for (int p = start[e], limit = start[e + 1]; p < limit; p++) {
                        int i = index[p];
                        arg.values[i] -= value[p] * pivot;
                        if (search.visit(i)) {
                            indices[nbListed++] = i;
                        }
                    }
                }
            }

            arg.setNonzeroCount(nbListed);
        }

        /**
         * Apply the transposed row etas, in reverse order.
         */
        void solveRowEtasTransposed(final double[] arg) {
            for (int e = count - 1; e >= 0; e--) {
                double pivot = arg[pivotRow[e]];
                if (pivot != ZERO) {
                    for (int p = start[e], limit = start[e + 1]; p < limit; p++) {
                        arg[index[p]] -= value[p] * pivot;
                    }
                }
            }
        }

    }

    /**
     * Computes a fresh factorisation of a selection of columns of a CSC matrix: the pivot sequence, [L] (as
     * column etas) and [U]. With Markowitz pivoting, column singletons (such as slack columns) and row
     * singletons are pivoted first, without arithmetic and without fill, and what remains (the active
     * submatrix) is factorised by right-looking Gaussian elimination with Markowitz pivot selection and
     * threshold partial pivoting. Otherwise the columns are pivoted in their natural order, with threshold
     * partial pivoting. Rows and columns without nonzero pivot candidates are paired up, last, with zero
     * pivots.
     * <p>
     * A row or column is active (not yet pivoted) as long as it has no slot in the {@link PivotSequence}.
     * <p>
     * All fields are scratch space, meaningful only during {@link #factorise}. They are kept to be reused by
     * the next factorisation.
     */
    static final class Factoriser {

        /**
         * Threshold partial pivoting: a pivot must be at least this fraction of the largest magnitude in its
         * column.
         */
        private static final double PIVOT_THRESHOLD = 0.1;
        /**
         * The Markowitz pivot search stops after this many candidate rows/columns have been examined (once a
         * valid pivot has been found).
         */
        private static final int SEARCH_LIMIT = 8;

        private final int myColDim;
        /**
         * The number of (active) nonzeros in each column, and the columns by count.
         */
        private final BucketQueue myColumns;
        private double myDropTolerance = ZERO;
        /**
         * The rows, and their multipliers, eliminated by the current pivot.
         */
        private final double[] myEliminationMultiplier;
        private final int[] myEliminationRow;
        /**
         * The active submatrix: its columns (the number of entries, their rows and values) and its rows (the
         * number of entries and their column indices).
         */
        private final int[] myKernelColumnLength;
        private final int[][] myKernelColumnRows;
        private final double[][] myKernelColumnValues;
        private final int[][] myKernelRowColumns;
        private final int[] myKernelRowLength;
        /**
         * Per row, scratch that is all zero between uses.
         */
        private final int[] myMark;
        /**
         * The pattern of the factorised matrix, row-wise: the column indices of row i's nonzeros are
         * {@code myPatternPosition[myPatternStart[i]]} to {@code myPatternPosition[myPatternStart[i + 1]]}.
         */
        private int[] myPatternPosition = new int[16];
        private final int[] myPatternStart;
        /**
         * The pivot chosen by {@link #selectPivot()}: its row, column index and value.
         */
        private int myPivotColumn = -1;
        private int myPivotRow = -1;
        private double myPivotValue = ZERO;
        /**
         * Singleton columns, or rows, waiting to be pivoted.
         */
        private final int[] myQueue;
        private final int myRowDim;
        /**
         * The number of (active) nonzeros in each row, and the rows by count.
         */
        private final BucketQueue myRows;
        /**
         * The [U] entries (slot, column index, value), collected as they are found, and converted to
         * {@link UpperFactor} at the end.
         */
        private int myTripletCount = 0;
        private int[] myTripletPosition = new int[16];
        private int[] myTripletSlot = new int[16];
        private double[] myTripletValue = new double[16];

        Factoriser(final int nbRows, final int nbCols) {

            super();

            myRowDim = nbRows;
            myColDim = nbCols;

            myColumns = new BucketQueue(nbCols, nbRows);
            myRows = new BucketQueue(nbRows, nbCols);
            myQueue = new int[Math.max(nbRows, nbCols)];
            myMark = new int[nbRows];
            myPatternStart = new int[nbRows + 1];

            myKernelColumnRows = new int[nbCols][];
            myKernelColumnValues = new double[nbCols][];
            myKernelColumnLength = new int[nbCols];
            myKernelRowColumns = new int[nbRows][];
            myKernelRowLength = new int[nbRows];

            myEliminationRow = new int[nbRows];
            myEliminationMultiplier = new double[nbRows];
        }

        private void addU(final int slot, final int position, final double value) {
            if (myTripletCount >= myTripletSlot.length) {
                myTripletSlot = COPY.grow(myTripletSlot, myTripletCount + 1);
                myTripletPosition = COPY.grow(myTripletPosition, myTripletCount + 1);
                myTripletValue = COPY.grow(myTripletValue, myTripletCount + 1);
            }
            myTripletSlot[myTripletCount] = slot;
            myTripletPosition[myTripletCount] = position;
            myTripletValue[myTripletCount] = value;
            myTripletCount++;
        }

        /**
         * Remove the pivot row entry from active column j (recording it in [U]) and update the column with
         * the current eliminations, creating fill-in as needed.
         */
        private void eliminate(final int slot, final int pivotRow, final int j, final int nbEliminations) {

            int[] rows = myKernelColumnRows[j];
            double[] values = myKernelColumnValues[j];
            int length = myKernelColumnLength[j];

            double u = ZERO;
            for (int t = 0; t < length; t++) {
                if (rows[t] == pivotRow) {
                    u = values[t];
                    length--;
                    rows[t] = rows[length];
                    values[t] = values[length];
                    break;
                }
            }
            this.addU(slot, j, u);

            if (nbEliminations > 0 && u != ZERO) {

                for (int t = 0; t < length; t++) {
                    myMark[rows[t]] = t + 1;
                }

                for (int b = 0; b < nbEliminations; b++) {
                    int i = myEliminationRow[b];
                    double delta = -myEliminationMultiplier[b] * u;
                    int t = myMark[i];
                    if (t > 0) {
                        values[t - 1] += delta;
                    } else if (Math.abs(delta) > myDropTolerance) {
                        if (length >= rows.length) {
                            rows = myKernelColumnRows[j] = COPY.grow(rows, length + 1);
                            values = myKernelColumnValues[j] = COPY.grow(values, length + 1);
                        }
                        rows[length] = i;
                        values[length] = delta;
                        length++;
                        int rowLength = myKernelRowLength[i];
                        if (rowLength >= myKernelRowColumns[i].length) {
                            myKernelRowColumns[i] = COPY.grow(myKernelRowColumns[i], rowLength + 1);
                        }
                        myKernelRowColumns[i][rowLength] = j;
                        myKernelRowLength[i] = rowLength + 1;
                    }
                }

                for (int t = 0; t < length; t++) {
                    myMark[rows[t]] = 0;
                }
            }

            myKernelColumnLength[j] = length;
            myColumns.remove(j);
            myColumns.insert(j, length);
        }

        /**
         * The columns in their natural order, each pivot row chosen by threshold partial pivoting preferring
         * sparse rows. Columns without nonzero candidates are skipped (they get zero pivots at the end),
         * which keeps [U] in row echelon form.
         *
         * @return The number of pivots
         */
        private int factoriseInOrder(final PivotSequence sequence, final EtaSequence lower) {

            int rank = Math.min(myRowDim, myColDim);
            int k = 0;

            for (int c = 0; c < myColDim && k < rank; c++) {

                int[] rows = myKernelColumnRows[c];
                double[] values = myKernelColumnValues[c];
                int length = myKernelColumnLength[c];

                double largest = ZERO;
                for (int t = 0; t < length; t++) {
                    largest = Math.max(largest, Math.abs(values[t]));
                }
                double limit = PIVOT_THRESHOLD * largest;

                int bestRow = -1;
                double pivot = ZERO;
                if (largest > ZERO) {
                    for (int t = 0; t < length; t++) {
                        double magnitude = Math.abs(values[t]);
                        if (magnitude >= limit) {
                            int row = rows[t];
                            if (bestRow < 0 || myKernelRowLength[row] < myKernelRowLength[bestRow]
                                    || myKernelRowLength[row] == myKernelRowLength[bestRow] && magnitude > Math.abs(pivot)) {
                                bestRow = row;
                                pivot = values[t];
                            }
                        }
                    }
                    this.pivotKernel(k++, bestRow, c, pivot, sequence, lower);
                }
            }

            return k;
        }

        /**
         * Markowitz elimination, with threshold partial pivoting, of the rows and columns that remain active
         * after the triangular pass. Stops when there are no nonzero pivot candidates left.
         *
         * @return The number of pivots
         */
        private int factoriseKernel(final int first, final PivotSequence sequence, final EtaSequence lower) {

            int rank = Math.min(myRowDim, myColDim);

            for (int k = first; k < rank; k++) {

                if (!this.selectPivot()) {
                    return k;
                }

                this.pivotKernel(k, myPivotRow, myPivotColumn, myPivotValue, sequence, lower);
            }

            return rank;
        }

        /**
         * Pivot on column singletons (which include all slack columns) and then on row singletons, without
         * any arithmetic and without fill.
         *
         * @return The number of pivots
         */
        private int factoriseTriangular(final R064CSC matrix, final int[] columns, final PivotSequence sequence, final EtaSequence lower) {

            int k = 0;

            int head = 0;
            int tail = 0;
            for (int j = 0; j < myColDim; j++) {
                if (myColumns.count[j] == 1) {
                    myQueue[tail++] = j;
                }
            }
            while (head < tail) {
                int j = myQueue[head++];
                if (sequence.slotOfPosition[j] >= 0 || myColumns.count[j] != 1) {
                    continue;
                }
                int column = columns[j];
                int r = -1;
                double pivot = ZERO;
                for (int p = matrix.pointers[column], limit = matrix.pointers[column + 1]; p < limit; p++) {
                    if (matrix.values[p] != ZERO && sequence.slotOfRow[matrix.indices[p]] < 0) {
                        r = matrix.indices[p];
                        pivot = matrix.values[p];
                        break;
                    }
                }
                sequence.set(k, r, j, pivot);
                for (int q = myPatternStart[r], limit = myPatternStart[r + 1]; q < limit; q++) {
                    int j2 = myPatternPosition[q];
                    if (sequence.slotOfPosition[j2] < 0) {
                        this.addU(k, j2, matrix.doubleValue(r, columns[j2]));
                        if (--myColumns.count[j2] == 1) {
                            myQueue[tail++] = j2;
                        }
                    }
                }
                k++;
            }

            head = 0;
            tail = 0;
            for (int i = 0; i < myRowDim; i++) {
                if (sequence.slotOfRow[i] < 0) {
                    int count = 0;
                    for (int q = myPatternStart[i], limit = myPatternStart[i + 1]; q < limit; q++) {
                        if (sequence.slotOfPosition[myPatternPosition[q]] < 0) {
                            count++;
                        }
                    }
                    myRows.count[i] = count;
                    if (count == 1) {
                        myQueue[tail++] = i;
                    }
                }
            }
            while (head < tail) {
                int i = myQueue[head++];
                if (sequence.slotOfRow[i] >= 0 || myRows.count[i] != 1) {
                    continue;
                }
                int j = -1;
                for (int q = myPatternStart[i], limit = myPatternStart[i + 1]; q < limit; q++) {
                    if (sequence.slotOfPosition[myPatternPosition[q]] < 0) {
                        j = myPatternPosition[q];
                        break;
                    }
                }
                int column = columns[j];
                double pivot = matrix.doubleValue(i, column);
                sequence.set(k, i, j, pivot);
                int begin = lower.size;
                for (int p = matrix.pointers[column], limit = matrix.pointers[column + 1]; p < limit; p++) {
                    int i2 = matrix.indices[p];
                    if (matrix.values[p] != ZERO && sequence.slotOfRow[i2] < 0) {
                        lower.add(i2, matrix.values[p] / pivot);
                        if (--myRows.count[i2] == 1) {
                            myQueue[tail++] = i2;
                        }
                    }
                }
                if (lower.size > begin) {
                    lower.close(i);
                }
                k++;
            }

            return k;
        }

        /**
         * Load the active submatrix: the active columns (active rows only) and the patterns of the active
         * rows.
         */
        private void loadKernel(final R064CSC matrix, final int[] columns, final PivotSequence sequence) {

            myColumns.clear();
            myRows.clear();

            for (int j = 0; j < myColDim; j++) {
                if (sequence.slotOfPosition[j] < 0) {
                    int column = columns[j];
                    int length = matrix.pointers[column + 1] - matrix.pointers[column];
                    if (myKernelColumnRows[j] == null || myKernelColumnRows[j].length < length + 4) {
                        myKernelColumnRows[j] = new int[length + 8];
                        myKernelColumnValues[j] = new double[length + 8];
                    }
                    int count = 0;
                    for (int p = matrix.pointers[column], limit = matrix.pointers[column + 1]; p < limit; p++) {
                        if (matrix.values[p] != ZERO && sequence.slotOfRow[matrix.indices[p]] < 0) {
                            myKernelColumnRows[j][count] = matrix.indices[p];
                            myKernelColumnValues[j][count] = matrix.values[p];
                            count++;
                        }
                    }
                    myKernelColumnLength[j] = count;
                    myColumns.insert(j, count);
                }
            }

            for (int i = 0; i < myRowDim; i++) {
                if (sequence.slotOfRow[i] < 0) {
                    int count = 0;
                    for (int q = myPatternStart[i], limit = myPatternStart[i + 1]; q < limit; q++) {
                        if (sequence.slotOfPosition[myPatternPosition[q]] < 0) {
                            count++;
                        }
                    }
                    if (myKernelRowColumns[i] == null || myKernelRowColumns[i].length < count + 4) {
                        myKernelRowColumns[i] = new int[count + 8];
                    }
                    count = 0;
                    for (int q = myPatternStart[i], limit = myPatternStart[i + 1]; q < limit; q++) {
                        if (sequence.slotOfPosition[myPatternPosition[q]] < 0) {
                            myKernelRowColumns[i][count++] = myPatternPosition[q];
                        }
                    }
                    myKernelRowLength[i] = count;
                    myRows.insert(i, count);
                }
            }
        }

        /**
         * Pivot on an entry of the active submatrix: record the [L] eta and the [U] row, and update the
         * remaining active submatrix.
         */
        private void pivotKernel(final int slot, final int row, final int column, final double pivot, final PivotSequence sequence, final EtaSequence lower) {

            sequence.set(slot, row, column, pivot);

            int[] columnRows = myKernelColumnRows[column];
            double[] columnValues = myKernelColumnValues[column];
            int columnLength = myKernelColumnLength[column];

            int nbEliminations = 0;
            for (int t = 0; t < columnLength; t++) {
                int i = columnRows[t];
                if (i != row) {
                    double multiplier = columnValues[t] / pivot;
                    myEliminationRow[nbEliminations] = i;
                    myEliminationMultiplier[nbEliminations] = multiplier;
                    nbEliminations++;
                    lower.add(i, multiplier);
                }
            }
            if (nbEliminations > 0) {
                lower.close(row);
            }

            myColumns.remove(column);
            myRows.remove(row);

            int[] pivotRowColumns = myKernelRowColumns[row];
            for (int s = 0, rowLength = myKernelRowLength[row]; s < rowLength; s++) {
                int j = pivotRowColumns[s];
                if (j != column) {
                    this.eliminate(slot, row, j, nbEliminations);
                }
            }

            for (int t = 0; t < columnLength; t++) {
                int i = columnRows[t];
                if (i != row) {
                    int[] rowColumns = myKernelRowColumns[i];
                    int rowLength = myKernelRowLength[i];
                    for (int s = 0; s < rowLength; s++) {
                        if (rowColumns[s] == column) {
                            rowColumns[s] = rowColumns[--rowLength];
                            break;
                        }
                    }
                    myKernelRowLength[i] = rowLength;
                    myRows.remove(i);
                    myRows.insert(i, rowLength);
                }
            }
        }

        /**
         * Markowitz search: examine columns and rows in order of increasing count, among the nonzero entries
         * that pass the threshold test pick the one minimising (row count - 1) * (column count - 1).
         *
         * @return true if a pivot was selected, it is then in {@link #myPivotRow} and {@link #myPivotColumn}
         */
        private boolean selectPivot() {

            myPivotRow = -1;
            myPivotColumn = -1;
            myPivotValue = ZERO;
            long bestMerit = Long.MAX_VALUE;
            int searched = 0;

            for (int count = 1, maxCount = Math.max(myRowDim, myColDim); count <= maxCount; count++) {

                if (count <= myRowDim) {
                    for (int j = myColumns.first(count); j >= 0; j = myColumns.next(j)) {
                        int[] rows = myKernelColumnRows[j];
                        double[] values = myKernelColumnValues[j];
                        int length = myKernelColumnLength[j];
                        double largest = ZERO;
                        for (int t = 0; t < length; t++) {
                            largest = Math.max(largest, Math.abs(values[t]));
                        }
                        double limit = PIVOT_THRESHOLD * largest;
                        for (int t = 0; t < length; t++) {
                            double magnitude = Math.abs(values[t]);
                            if (magnitude >= limit && magnitude > ZERO) {
                                long merit = (long) (myKernelRowLength[rows[t]] - 1) * (count - 1);
                                if (merit < bestMerit || merit == bestMerit && magnitude > Math.abs(myPivotValue)) {
                                    bestMerit = merit;
                                    myPivotRow = rows[t];
                                    myPivotColumn = j;
                                    myPivotValue = values[t];
                                }
                            }
                        }
                        if (myPivotRow >= 0 && (++searched >= SEARCH_LIMIT || bestMerit <= (long) (count - 1) * (count - 1))) {
                            return true;
                        }
                    }
                }

                if (count <= myColDim) {
                    for (int i = myRows.first(count); i >= 0; i = myRows.next(i)) {
                        int[] columns = myKernelRowColumns[i];
                        for (int s = 0, rowLength = myKernelRowLength[i]; s < rowLength; s++) {
                            int j = columns[s];
                            int[] rows = myKernelColumnRows[j];
                            double[] values = myKernelColumnValues[j];
                            int length = myKernelColumnLength[j];
                            double largest = ZERO;
                            double value = ZERO;
                            for (int t = 0; t < length; t++) {
                                largest = Math.max(largest, Math.abs(values[t]));
                                if (rows[t] == i) {
                                    value = values[t];
                                }
                            }
                            double magnitude = Math.abs(value);
                            if (magnitude >= PIVOT_THRESHOLD * largest && magnitude > ZERO) {
                                long merit = (long) (count - 1) * (length - 1);
                                if (merit < bestMerit || merit == bestMerit && magnitude > Math.abs(myPivotValue)) {
                                    bestMerit = merit;
                                    myPivotRow = i;
                                    myPivotColumn = j;
                                    myPivotValue = value;
                                }
                            }
                        }
                        if (myPivotRow >= 0 && (++searched >= SEARCH_LIMIT || bestMerit <= (long) (count - 1) * (count - 1))) {
                            return true;
                        }
                    }
                }

                if (myPivotRow >= 0 && bestMerit <= (long) count * count) {
                    return true;
                }
            }

            return myPivotRow >= 0;
        }

        /**
         * Factorise the matrix formed by selecting columns from a CSC matrix.
         *
         * @param markowitz     true: triangular pass and then Markowitz pivot selection (rows and columns),
         *                      false: the columns in order and threshold partial pivoting
         * @param dropTolerance Fill-in with a magnitude not larger than this is not stored
         * @param sequence      Reset and then filled with the pivots (not yet ordered)
         * @param lower         Reset and then filled with [L]
         * @param upper         Built with [U]
         * @return The number of entries in [U] (excluding the diagonal)
         */
        int factorise(final R064CSC matrix, final int[] columns, final boolean markowitz, final double dropTolerance, final PivotSequence sequence,
                final EtaSequence lower, final UpperFactor upper) {

            int rank = Math.min(myRowDim, myColDim);

            myDropTolerance = dropTolerance;

            sequence.reset();
            lower.reset();
            myTripletCount = 0;

            Arrays.fill(myRows.count, 0);
            int nonzeros = 0;
            for (int j = 0; j < myColDim; j++) {
                int column = columns[j];
                int count = 0;
                for (int p = matrix.pointers[column], limit = matrix.pointers[column + 1]; p < limit; p++) {
                    if (matrix.values[p] != ZERO) {
                        count++;
                        myRows.count[matrix.indices[p]]++;
                    }
                }
                myColumns.count[j] = count;
                nonzeros += count;
            }
            myPatternStart[0] = 0;
            for (int i = 0; i < myRowDim; i++) {
                myPatternStart[i + 1] = myPatternStart[i] + myRows.count[i];
            }
            myPatternPosition = COPY.grow(myPatternPosition, nonzeros);
            System.arraycopy(myPatternStart, 0, myMark, 0, myRowDim);
            for (int j = 0; j < myColDim; j++) {
                int column = columns[j];
                for (int p = matrix.pointers[column], limit = matrix.pointers[column + 1]; p < limit; p++) {
                    if (matrix.values[p] != ZERO) {
                        myPatternPosition[myMark[matrix.indices[p]]++] = j;
                    }
                }
            }
            Arrays.fill(myMark, 0);

            int k = markowitz ? this.factoriseTriangular(matrix, columns, sequence, lower) : 0;

            if (k < rank) {

                this.loadKernel(matrix, columns, sequence);

                if (markowitz) {
                    k = this.factoriseKernel(k, sequence, lower);
                } else {
                    k = this.factoriseInOrder(sequence, lower);
                }

                for (int i = 0, j = 0; k < rank; k++) {
                    while (sequence.slotOfRow[i] >= 0) {
                        i++;
                    }
                    while (sequence.slotOfPosition[j] >= 0) {
                        j++;
                    }
                    sequence.set(k, i, j, ZERO);
                }
            }

            upper.build(rank, myTripletCount, myTripletSlot, myTripletPosition, myTripletValue, sequence);

            return myTripletCount;
        }

    }

    /**
     * [L] in pivot order: [A] = [P][L][U]
     */
    final class FactorL extends AbstractDecomposition.PrimitiveFactor {

        @Override
        public void btran(final double[] arg) {
            double[] rowIndexed = SparseLU.this.toRowOrder(arg);
            myL.solveColumnEtasTransposed(rowIndexed);
            SparseLU.this.toPivotOrder(rowIndexed, arg);
        }

        @Override
        public void ftran(final double[] arg) {
            double[] rowIndexed = SparseLU.this.toRowOrder(arg);
            myL.solveColumnEtas(rowIndexed);
            SparseLU.this.toPivotOrder(rowIndexed, arg);
        }

        @Override
        public MatrixStore<Double> get() {
            return SparseLU.this.getL();
        }

        @Override
        public int getColDim() {
            return myRowDim;
        }

        @Override
        public int getRowDim() {
            return myRowDim;
        }

    }

    /**
     * The Forrest-Tomlin R-etas (the identity until there has been an update), in pivot order.
     */
    final class FactorR implements InvertibleFactor<Double> {

        @Override
        public void btran(final double[] arg) {
            double[] rowIndexed = SparseLU.this.toRowOrder(arg);
            myR.solveRowEtasTransposed(rowIndexed);
            SparseLU.this.toPivotOrder(rowIndexed, arg);
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {
            InvertibleFactor.doPrimitive(arg, this);
        }

        @Override
        public void ftran(final double[] arg) {
            double[] rowIndexed = SparseLU.this.toRowOrder(arg);
            myR.solveRowEtas(rowIndexed);
            SparseLU.this.toPivotOrder(rowIndexed, arg);
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {
            InvertibleFactor.doPrimitive(this, arg);
        }

        @Override
        public int getColDim() {
            return myRowDim;
        }

        @Override
        public int getRowDim() {
            return myRowDim;
        }

    }

    /**
     * [U], rows in pivot order and columns in the original order: [A] = [P][L][U]
     */
    final class FactorU extends AbstractDecomposition.PrimitiveFactor {

        @Override
        public void btran(final double[] arg) {
            int[] index = mySequence.getReversePivotOrder();
            double[] rowIndexed = new double[myRowDim];
            myU.solveTransposed(Arrays.copyOf(arg, myColDim), rowIndexed, mySequence);
            for (int s = 0; s < mySequence.count; s++) {
                int row = mySequence.row[s];
                if (row >= 0) {
                    arg[index[row]] = rowIndexed[row];
                }
            }
        }

        @Override
        public void ftran(final double[] arg) {
            myU.solve(SparseLU.this.toRowOrder(arg), arg, mySequence);
        }

        @Override
        public MatrixStore<Double> get() {
            return SparseLU.this.getU();
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myRowDim;
        }

    }

    /**
     * The pivots, in elimination order, as a sequence of slots. Each slot records the row and the column
     * index ("position") of a pivot, and its value (the diagonal of [U]). The factorisation fills slots 0 to
     * r-1. Each update voids the slot of the replaced column and appends a new one. The row and column
     * permutations are implicit in this sequence; they are never applied to the solve vectors.
     */
    static final class PivotSequence {

        /**
         * The column permutation of the last factorisation: the column indices in the order they were
         * pivoted, followed by any that were not.
         */
        private final Pivot myColumns;
        /**
         * The row permutation of the last factorisation: the rows in the order they were pivoted, followed by
         * any rows that were not.
         */
        private final Pivot myRows;
        /**
         * The number of slots, voided ones included.
         */
        int count = 0;
        /**
         * The pivot value of each slot.
         */
        double[] diagonal;
        /**
         * The column index of each slot's pivot.
         */
        int[] position;
        /**
         * The row of each slot's pivot, -1 for a voided slot.
         */
        int[] row;
        /**
         * The slot of each column index, -1 if it has none.
         */
        final int[] slotOfPosition;
        /**
         * The slot of each row, -1 if it has none.
         */
        final int[] slotOfRow;

        PivotSequence(final int nbRows, final int nbCols, final int capacity) {

            super();

            diagonal = new double[capacity];
            position = new int[capacity];
            row = new int[capacity];

            slotOfPosition = new int[nbCols];
            slotOfRow = new int[nbRows];

            myColumns = new Pivot();
            myColumns.reset(nbCols);
            myRows = new Pivot();
            myRows.reset(nbRows);
        }

        /**
         * The rows in the order they were pivoted by the last factorisation, followed by any rows that were
         * not. See {@link SparseLU#getPivotOrder()}.
         */
        int[] getPivotOrder() {
            return myRows.getOrder();
        }

        /**
         * The inverse of {@link #getPivotOrder()}: the position of each row in the pivot order.
         */
        int[] getReversePivotOrder() {
            return myRows.reverseOrder();
        }

        /**
         * The row permutation, for {@link AbstractDecomposition.FactorPivot}.
         */
        Pivot getRowPivot() {
            return myRows;
        }

        boolean isPivoted() {
            return myRows.isModified();
        }

        /**
         * After a factorisation: the first {@code rank} slots are the pivots, in order. Builds the row and
         * column permutations by exchanges, so that they track their signs.
         */
        void order(final int rank) {

            count = rank;

            myRows.reset(slotOfRow.length);
            myColumns.reset(slotOfPosition.length);
            for (int k = 0; k < rank; k++) {
                myRows.change(k, myRows.locationOf(row[k]));
                myColumns.change(k, myColumns.locationOf(position[k]));
            }
        }

        /**
         * Void a slot and append a new one, for the same row and column index, with a new pivot value.
         *
         * @return The new slot
         */
        int replace(final int slot, final double pivot) {

            int pivotRow = row[slot];
            int pivotPosition = position[slot];

            row[slot] = -1;

            int newSlot = count++;
            diagonal = COPY.grow(diagonal, count);
            position = COPY.grow(position, count);
            row = COPY.grow(row, count);
            this.set(newSlot, pivotRow, pivotPosition, pivot);

            return newSlot;
        }

        void reset() {
            count = 0;
            Arrays.fill(slotOfRow, -1);
            Arrays.fill(slotOfPosition, -1);
        }

        void set(final int slot, final int pivotRow, final int pivotPosition, final double pivot) {
            row[slot] = pivotRow;
            position[slot] = pivotPosition;
            diagonal[slot] = pivot;
            slotOfRow[pivotRow] = slot;
            slotOfPosition[pivotPosition] = slot;
        }

        /**
         * The sign of the row permutation times the sign of the column permutation, of the last
         * factorisation. Remains valid after updates.
         */
        int signum() {
            return myRows.signum() * myColumns.signum();
        }

    }

    /**
     * Measures of the current factorisation: its size and pivot range (for callers deciding when to
     * refactorise), and the running average densities of the sparse solves' results (for deciding how to
     * solve).
     */
    static final class Statistics {

        /**
         * Running average density of the results of {@link SparseLU#btranUnit(int, DensityTrackingArray)}.
         */
        double btranDensity = ZERO;
        /**
         * The number of nonzeros in [L] and [U], including the diagonal, at the last factorisation.
         */
        int factorNonzeros = 0;
        /**
         * Running average density of the results of
         * {@link SparseLU#ftranColumn(R064CSC, int, DensityTrackingArray)}.
         */
        double ftranDensity = ZERO;
        /**
         * The pivot magnitude range since the last factorisation, including the pivots from updates. A
         * non-finite pivot (from non-finite input) counts as zero.
         */
        double maxPivot = ZERO;
        double minPivot = Double.MAX_VALUE;
        /**
         * The number of nonzeros added by updates (R-etas and the replacement columns of [U]) since the last
         * factorisation.
         */
        int updateNonzeros = 0;

        void factorised(final int nonzeros, final double largestPivot, final double smallestPivot) {
            factorNonzeros = nonzeros;
            updateNonzeros = 0;
            maxPivot = largestPivot;
            minPivot = smallestPivot;
        }

        void recordBtran(final double density) {
            btranDensity = 0.95 * btranDensity + 0.05 * density;
        }

        void recordFtran(final double density) {
            ftranDensity = 0.95 * ftranDensity + 0.05 * density;
        }

        void updated(final int nonzeros, final double pivot) {
            updateNonzeros += nonzeros;
            maxPivot = Math.max(maxPivot, pivot);
            minPivot = Math.min(minPivot, pivot);
        }

    }

    /**
     * [U] without its diagonal (the diagonal is in the {@link PivotSequence}), stored both column-wise (for
     * FTRAN) and row-wise (for BTRAN), by slot. Column s holds the rows (of earlier pivots) and values of the
     * off-diagonal entries. Row s holds the column indices (of later pivots) and values. Columns added by
     * updates are appended at the end of the column arrays. A row that needs to grow is moved to the end of
     * the row arrays, with room to grow. Deleted entries are zeroed, not removed.
     */
    static final class UpperFactor {

        /**
         * Column-wise: column s's entries are {@code columnStart[s]} to {@code columnEnd[s]}, with their rows
         * in {@link #columnRow} and values in {@link #columnValue}. {@link #columnSize} is the number of
         * entries (used or deleted).
         */
        int[] columnEnd;
        int[] columnRow;
        int columnSize = 0;
        int[] columnStart;
        double[] columnValue;
        /**
         * Row-wise: row s's entries are {@code rowStart[s]} to {@code rowEnd[s]}, with their column indices
         * in {@link #rowPosition} and values in {@link #rowValue}. Row s has room for {@code rowCapacity[s]}
         * entries before it has to be moved. {@link #rowSize} is the size of the used part of the row arrays
         * (entries and spare capacity).
         */
        int[] rowCapacity;
        int[] rowEnd;
        int[] rowPosition;
        int rowSize = 0;
        int[] rowStart;
        double[] rowValue;

        UpperFactor(final int nbSlots, final int nbEntries) {

            super();

            columnStart = new int[nbSlots];
            columnEnd = new int[nbSlots];
            columnRow = new int[nbEntries];
            columnValue = new double[nbEntries];

            rowStart = new int[nbSlots];
            rowEnd = new int[nbSlots];
            rowCapacity = new int[nbSlots];
            rowPosition = new int[nbEntries];
            rowValue = new double[nbEntries];
        }

        /**
         * Append an entry to a slot's row. A row without spare capacity is moved to the end of the row arrays
         * (dropping its zeroed entries) with room to grow.
         */
        private void appendToRow(final int slot, final int position, final double value) {

            int length = rowEnd[slot] - rowStart[slot];

            if (length >= rowCapacity[slot]) {

                int capacity = 2 * length + 4;
                rowPosition = COPY.grow(rowPosition, rowSize + capacity);
                rowValue = COPY.grow(rowValue, rowSize + capacity);

                int from = rowStart[slot];
                int to = rowSize;
                int kept = 0;
                for (int p = from, limit = from + length; p < limit; p++) {
                    if (rowValue[p] != ZERO) {
                        rowPosition[to + kept] = rowPosition[p];
                        rowValue[to + kept] = rowValue[p];
                        kept++;
                    }
                }

                rowStart[slot] = to;
                rowCapacity[slot] = capacity;
                rowSize += capacity;
                length = kept;
            }

            int p = rowStart[slot] + length;
            rowPosition[p] = position;
            rowValue[p] = value;
            rowEnd[slot] = p + 1;
        }

        private void ensureCapacity(final int nbSlots) {
            columnStart = COPY.grow(columnStart, nbSlots);
            columnEnd = COPY.grow(columnEnd, nbSlots);
            rowStart = COPY.grow(rowStart, nbSlots);
            rowEnd = COPY.grow(rowEnd, nbSlots);
            rowCapacity = COPY.grow(rowCapacity, nbSlots);
        }

        /**
         * Add the column for a new slot (from an update), and its entries to the rows.
         *
         * @param slot          The new slot
         * @param column        The new column (above the diagonal), indexed by row
         * @param dropTolerance Entries with a magnitude not larger than this are not stored
         * @return The number of entries added
         */
        int append(final int slot, final DensityTrackingArray column, final double dropTolerance, final PivotSequence sequence) {

            int pivotRow = sequence.row[slot];
            int position = sequence.position[slot];

            this.ensureCapacity(slot + 1);
            rowStart[slot] = rowSize;
            rowEnd[slot] = rowSize;
            rowCapacity[slot] = 0;

            int[] indices = column.indices();
            int nbIndices = column.countNonzeros();

            columnRow = COPY.grow(columnRow, columnSize + nbIndices);
            columnValue = COPY.grow(columnValue, columnSize + nbIndices);
            columnStart[slot] = columnSize;
            for (int k = 0; k < nbIndices; k++) {
                int i = indices[k];
                double value = column.values[i];
                if (i != pivotRow && Math.abs(value) > dropTolerance) {
                    columnRow[columnSize] = i;
                    columnValue[columnSize] = value;
                    columnSize++;
                    this.appendToRow(sequence.slotOfRow[i], position, value);
                }
            }
            columnEnd[slot] = columnSize;

            return columnEnd[slot] - columnStart[slot];
        }

        /**
         * Build both forms from the entries collected by a factorisation.
         *
         * @param rank      The number of slots
         * @param nbEntries The number of entries
         * @param slots     The slot (row of [U]) of each entry
         * @param positions The column index of each entry
         * @param values    The value of each entry
         */
        void build(final int rank, final int nbEntries, final int[] slots, final int[] positions, final double[] values, final PivotSequence sequence) {

            columnRow = COPY.grow(columnRow, nbEntries);
            columnValue = COPY.grow(columnValue, nbEntries);
            rowPosition = COPY.grow(rowPosition, nbEntries);
            rowValue = COPY.grow(rowValue, nbEntries);

            Arrays.fill(columnStart, 0, rank, 0);
            Arrays.fill(rowCapacity, 0, rank, 0);
            for (int t = 0; t < nbEntries; t++) {
                int columnSlot = sequence.slotOfPosition[positions[t]];
                if (columnSlot >= 0) {
                    columnStart[columnSlot]++;
                }
                rowCapacity[slots[t]]++;
            }

            int nbColumnEntries = 0;
            int nbRowEntries = 0;
            for (int s = 0; s < rank; s++) {
                int columnCount = columnStart[s];
                columnStart[s] = nbColumnEntries;
                columnEnd[s] = nbColumnEntries;
                nbColumnEntries += columnCount;
                rowStart[s] = nbRowEntries;
                rowEnd[s] = nbRowEntries;
                nbRowEntries += rowCapacity[s];
            }

            for (int t = 0; t < nbEntries; t++) {
                int rowSlot = slots[t];
                int position = positions[t];
                double value = values[t];
                int columnSlot = sequence.slotOfPosition[position];
                if (columnSlot >= 0) {
                    int pc = columnEnd[columnSlot]++;
                    columnRow[pc] = sequence.row[rowSlot];
                    columnValue[pc] = value;
                }
                int pr = rowEnd[rowSlot]++;
                rowPosition[pr] = position;
                rowValue[pr] = value;
            }

            columnSize = nbColumnEntries;
            rowSize = nbRowEntries;
        }

        /**
         * Delete a slot's row from the columns, and its column from the rows (before the slot is replaced by
         * an update).
         */
        void delete(final int slot, final PivotSequence sequence) {

            int pivotRow = sequence.row[slot];
            int position = sequence.position[slot];

            for (int p = rowStart[slot], limit = rowEnd[slot]; p < limit; p++) {
                if (rowValue[p] != ZERO) {
                    int other = sequence.slotOfPosition[rowPosition[p]];
                    for (int q = columnStart[other], end = columnEnd[other]; q < end; q++) {
                        if (columnRow[q] == pivotRow) {
                            columnValue[q] = ZERO;
                        }
                    }
                }
            }

            for (int q = columnStart[slot], end = columnEnd[slot]; q < end; q++) {
                if (columnValue[q] != ZERO) {
                    int other = sequence.slotOfRow[columnRow[q]];
                    for (int p = rowStart[other], limit = rowEnd[other]; p < limit; p++) {
                        if (rowPosition[p] == position) {
                            rowValue[p] = ZERO;
                        }
                    }
                }
            }
        }

        /**
         * Solve with [U] (scatter form using the column-wise [U]), maintaining the index of the result.
         * Hyper-sparse: a depth-first search finds the pivots that will be reached, and only those are
         * processed. Otherwise backwards through the whole pivot sequence.
         *
         * @param rhs    The right hand side, indexed by row (reset when done)
         * @param result The solution, indexed by column (assumed reset)
         */
        void solve(final DensityTrackingArray rhs, final DensityTrackingArray result, final PivotSequence sequence, final DepthFirstSearch search,
                final boolean hyper) {

            int[] indices = result.indices();
            int count = 0;

            if (hyper) {

                int nbReached = search.reach(rhs.indices(), rhs.countNonzeros(), sequence.slotOfRow, columnStart, columnEnd, columnRow, columnValue);

                for (int k = nbReached - 1; k >= 0; k--) {
                    int row = search.postorder[k];
                    double value = rhs.values[row];
                    if (value != ZERO) {
                        rhs.values[row] = ZERO;
                        int s = sequence.slotOfRow[row];
                        value /= sequence.diagonal[s];
                        for (int p = columnStart[s], limit = columnEnd[s]; p < limit; p++) {
                            rhs.values[columnRow[p]] -= columnValue[p] * value;
                        }
                        int position = sequence.position[s];
                        result.values[position] = value;
                        indices[count++] = position;
                    }
                }

            } else {

                for (int s = sequence.count - 1; s >= 0; s--) {
                    int row = sequence.row[s];
                    if (row >= 0) {
                        double value = rhs.values[row];
                        if (value != ZERO) {
                            rhs.values[row] = ZERO;
                            value /= sequence.diagonal[s];
                            for (int p = columnStart[s], limit = columnEnd[s]; p < limit; p++) {
                                rhs.values[columnRow[p]] -= columnValue[p] * value;
                            }
                            int position = sequence.position[s];
                            result.values[position] = value;
                            indices[count++] = position;
                        }
                    }
                }
            }

            rhs.reset();
            result.setNonzeroCount(count);
        }

        /**
         * Solve with [U], backwards through the pivot sequence (scatter form using the column-wise [U]).
         *
         * @param rowIndexed    The right hand side, indexed by row (overwritten)
         * @param columnIndexed The solution, indexed by column
         */
        void solve(final double[] rowIndexed, final double[] columnIndexed, final PivotSequence sequence) {
            for (int s = sequence.count - 1; s >= 0; s--) {
                int row = sequence.row[s];
                if (row >= 0) {
                    double value = rowIndexed[row];
                    if (value != ZERO) {
                        value /= sequence.diagonal[s];
                        for (int p = columnStart[s], limit = columnEnd[s]; p < limit; p++) {
                            rowIndexed[columnRow[p]] -= columnValue[p] * value;
                        }
                    }
                    columnIndexed[sequence.position[s]] = value;
                }
            }
        }

        /**
         * Solve with [U] transposed (scatter form using the row-wise [U]), maintaining the index of the
         * result. Hyper-sparse: a depth-first search finds the pivots that will be reached, and only those
         * are processed. Otherwise forwards through the whole pivot sequence.
         *
         * @param rhs    The right hand side, indexed by column (reset when done)
         * @param result The solution, indexed by row (assumed reset)
         */
        void solveTransposed(final DensityTrackingArray rhs, final DensityTrackingArray result, final PivotSequence sequence, final DepthFirstSearch search,
                final boolean hyper) {

            int[] indices = result.indices();
            int count = 0;

            if (hyper) {

                int nbReached = search.reach(rhs.indices(), rhs.countNonzeros(), sequence.slotOfPosition, rowStart, rowEnd, rowPosition, rowValue);

                for (int k = nbReached - 1; k >= 0; k--) {
                    int position = search.postorder[k];
                    double value = rhs.values[position];
                    if (value != ZERO) {
                        rhs.values[position] = ZERO;
                        int s = sequence.slotOfPosition[position];
                        value /= sequence.diagonal[s];
                        for (int p = rowStart[s], limit = rowEnd[s]; p < limit; p++) {
                            rhs.values[rowPosition[p]] -= rowValue[p] * value;
                        }
                        int row = sequence.row[s];
                        result.values[row] = value;
                        indices[count++] = row;
                    }
                }

            } else {

                for (int s = 0; s < sequence.count; s++) {
                    int row = sequence.row[s];
                    if (row >= 0) {
                        int position = sequence.position[s];
                        double value = rhs.values[position];
                        if (value != ZERO) {
                            rhs.values[position] = ZERO;
                            value /= sequence.diagonal[s];
                            for (int p = rowStart[s], limit = rowEnd[s]; p < limit; p++) {
                                rhs.values[rowPosition[p]] -= rowValue[p] * value;
                            }
                            result.values[row] = value;
                            indices[count++] = row;
                        }
                    }
                }
            }

            rhs.reset();
            result.setNonzeroCount(count);
        }

        /**
         * Solve with [U] transposed, forwards through the pivot sequence (scatter form using the row-wise
         * [U]).
         *
         * @param columnIndexed The right hand side, indexed by column (overwritten)
         * @param rowIndexed    The solution, indexed by row
         */
        void solveTransposed(final double[] columnIndexed, final double[] rowIndexed, final PivotSequence sequence) {
            for (int s = 0; s < sequence.count; s++) {
                int row = sequence.row[s];
                if (row >= 0) {
                    double value = columnIndexed[sequence.position[s]];
                    if (value != ZERO) {
                        value /= sequence.diagonal[s];
                        for (int p = rowStart[s], limit = rowEnd[s]; p < limit; p++) {
                            columnIndexed[rowPosition[p]] -= rowValue[p] * value;
                        }
                    }
                    rowIndexed[row] = value;
                }
            }
        }

    }

    /**
     * Work arrays for the solves, and the partial solve results retained for the next update.
     */
    static final class Work {

        /**
         * Right hand side of the sparse solves, indexed by column. All zero between uses.
         */
        final DensityTrackingArray columnIndexed;
        /**
         * Work array for the dense solves, {@link SparseLU#ftran(double[])} and
         * {@link SparseLU#btran(double[])}.
         */
        final double[] dense;
        /**
         * Right hand side of the sparse solves, indexed by row. All zero between uses.
         */
        final DensityTrackingArray rowIndexed;
        /**
         * The spike: a column after [L] and the R-etas, but before [U]. Row indexed. Retained by
         * {@link SparseLU#ftranColumn(R064CSC, int, DensityTrackingArray)} for the next update.
         */
        final DensityTrackingArray spike;
        /**
         * The column {@link #spike} was computed for, or -1.
         */
        int spikeColumn = -1;
        /**
         * The [U] part of the BTRAN of a unit vector. Row indexed. Retained by
         * {@link SparseLU#btranUnit(int, DensityTrackingArray)} for the next update.
         */
        final DensityTrackingArray unitRow;
        /**
         * The column index {@link #unitRow} was computed for, or -1.
         */
        int unitRowColumn = -1;

        Work(final int nbRows, final int nbCols) {

            super();

            columnIndexed = new DensityTrackingArray(nbCols);
            dense = new double[Math.max(nbRows, nbCols)];
            rowIndexed = new DensityTrackingArray(nbRows);
            spike = new DensityTrackingArray(nbRows);
            unitRow = new DensityTrackingArray(nbRows);
        }

        /**
         * Invalidate the retained partial results. They belong to the factors as they were.
         */
        void forget() {
            spikeColumn = -1;
            unitRowColumn = -1;
        }

        /**
         * {@link #forget()}, and zero the right hand side arrays.
         */
        void reset() {
            this.forget();
            Arrays.fill(columnIndexed.values, ZERO);
            Arrays.fill(rowIndexed.values, ZERO);
        }

    }

    /**
     * A solve is done hyper-sparse (depth-first reach, then only the reached pivots) when the right hand side
     * is sparser than {@link #HYPER_CANCEL} and the expected result density (running average) is below the
     * threshold for that part of the solve. Otherwise it loops over all pivots, skipping zeros. When the
     * expected density is not below {@link #HYPER_BTRAN_L}, {@link #btranUnit(int, DensityTrackingArray)}
     * instead does a plain dense solve.
     */
    private static final double HYPER_BTRAN_L = 0.10;
    private static final double HYPER_BTRAN_U = 0.15;
    private static final double HYPER_CANCEL = 0.05;
    private static final double HYPER_FTRAN_L = 0.50;
    private static final double HYPER_FTRAN_U = 0.03;
    /**
     * With {@link #factor(R064CSC, int[])} values with a smaller magnitude are not stored.
     */
    private static final double TINY = 1E-14;

    /**
     * Whether a (part of a) solve should be hyper-sparse: the right hand side is sparse enough, and so is the
     * expected result (the running average density of earlier results).
     */
    private static boolean isHyper(final int nbNonzeros, final int dim, final double expectedDensity, final double threshold) {
        return (double) nbNonzeros / dim < HYPER_CANCEL && expectedDensity < threshold;
    }

    private int myColDim = 0;
    /**
     * Fill-in, spike and R-eta entries with a magnitude not larger than this are not stored.
     */
    private double myDropTolerance = ZERO;
    /**
     * Computes the factorisations, reusing its scratch space.
     */
    private Factoriser myFactoriser;
    /**
     * [L], as column etas in pivot order.
     */
    private EtaSequence myL;
    /**
     * The Forrest-Tomlin R-etas, as row etas in update order.
     */
    private EtaSequence myR;
    private int myRowDim = 0;
    /**
     * Finds the pattern of a hyper-sparse solve's result before any arithmetic is done.
     */
    private DepthFirstSearch mySearch;
    /**
     * The pivots, in elimination order.
     */
    private PivotSequence mySequence;
    private Statistics myStatistics = new Statistics();
    /**
     * [U], without its diagonal (that is in {@link #mySequence}).
     */
    private UpperFactor myU;
    private Work myWork;

    public SparseLU() {
        super(R064Store.FACTORY);
    }

    @Override
    public void btran(final double[] arg) {
        System.arraycopy(arg, 0, myWork.dense, 0, myColDim);
        myU.solveTransposed(myWork.dense, arg, mySequence);
        this.btranLowerDense(arg);
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(arg, this);
    }

    /**
     * Solves [A]<sup>T</sup>[y] = [e<sub>j</sub>], the unit vector for column index j, exploiting sparsity.
     * The result is the row of the inverse corresponding to that column. The [U] part of the solve is
     * retained, so that a following update replacing that same column does not need to repeat it.
     *
     * @param index  The column index j
     * @param result Overwritten with the solution [y], without values that would not be stored (see
     *               {@link #factor(R064CSC, int[])})
     */
    public void btranUnit(final int index, final DensityTrackingArray result) {

        result.reset();

        if (myStatistics.btranDensity < HYPER_BTRAN_L) {

            myWork.columnIndexed.set(index, ONE);

            this.btranUpper(myWork.columnIndexed, result);

            result.supplyTo(myWork.unitRow);

            this.btranLower(result);

        } else {

            myWork.columnIndexed.values[index] = ONE;
            myU.solveTransposed(myWork.columnIndexed.values, result.values, mySequence);
            Arrays.fill(myWork.columnIndexed.values, ZERO);

            result.reindex();
            result.supplyTo(myWork.unitRow);

            this.btranLowerDense(result.values);
            result.reindex();
        }

        myWork.unitRowColumn = index;
        result.tighten(myDropTolerance);

        myStatistics.recordBtran(result.density());
    }

    @Override
    public Double calculateDeterminant(final Access2D<?> matrix) {
        this.decompose(this.wrap(matrix));
        return this.getDeterminant();
    }

    /**
     * The number of nonzeros added by updates (R-etas and the replacement columns of [U]) since the last
     * factorisation.
     */
    public int countEtaNonzeros() {
        return myStatistics.updateNonzeros;
    }

    /**
     * The number of nonzeros in [L] and [U], including the diagonal, at the last factorisation.
     */
    public int countFactorNonzeros() {
        return myStatistics.factorNonzeros;
    }

    @Override
    public int countSignificant(final double threshold) {
        int significant = 0;
        for (int s = 0; s < mySequence.count; s++) {
            if (mySequence.row[s] >= 0 && Math.abs(mySequence.diagonal[s]) > threshold) {
                significant++;
            }
        }
        return significant;
    }

    @Override
    public boolean decompose(final Access2D.Collectable<Double, ? super TransformableRegion<Double>> matrix) {

        R064CSC csc = R064CSC.of(matrix);

        int[] columns = Structure1D.newIncreasingRange(0, csc.getColDim());

        return this.factorise(csc, columns, false);
    }

    /**
     * Factorises the matrix formed by selecting columns from a CSC matrix, typically a simplex basis. The
     * column indices of the factorised matrix (as used by {@link #updateColumn(int, R064CSC, int)},
     * {@link #btranUnit(int, DensityTrackingArray)}...) are the positions in the selection.
     *
     * @param matrix  The full matrix in CSC format
     * @param columns The indices of the columns to factorise
     */
    public boolean factor(final R064CSC matrix, final int[] columns) {
        return this.factorise(matrix, columns, true);
    }

    @Override
    public void ftran(final double[] arg) {
        System.arraycopy(arg, 0, myWork.dense, 0, myRowDim);
        this.ftranLowerDense(myWork.dense);
        myU.solve(myWork.dense, arg, mySequence);
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {
        InvertibleFactor.doPrimitive(this, arg);
    }

    /**
     * Solves [A][x] = [b] where [b] is a column of a sparse matrix, exploiting sparsity. The partial result
     * (after [L] and the R-etas, before [U]) is retained, so that a following
     * {@link #updateColumn(int, R064CSC, int)} with the same column does not need to repeat it.
     *
     * @param matrix The matrix containing [b]
     * @param column The column index of [b] in that matrix
     * @param result Overwritten with the solution [x], without values that would not be stored (see
     *               {@link #factor(R064CSC, int[])})
     */
    public void ftranColumn(final R064CSC matrix, final int column, final DensityTrackingArray result) {

        matrix.supplyTo(column, myWork.rowIndexed);

        result.reset();

        this.ftranLower(myWork.rowIndexed);

        myWork.rowIndexed.supplyTo(myWork.spike);

        this.ftranUpper(myWork.rowIndexed, result);

        myWork.spikeColumn = column;
        result.tighten(myDropTolerance);

        myStatistics.recordFtran(result.density());
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    /**
     * The product of the pivots times the signs of the row and column permutations (see
     * {@link PivotSequence#signum()}).
     */
    @Override
    public Double getDeterminant() {

        double retVal = mySequence.signum();

        for (int s = 0; s < mySequence.count; s++) {
            if (mySequence.row[s] >= 0) {
                retVal *= mySequence.diagonal[s];
            }
        }

        return Double.valueOf(retVal);
    }

    /**
     * Largest pivot magnitude at the last factorisation, before any updates.
     *
     * @deprecated v58 No longer used internally, and will be removed.
     */
    @Deprecated
    public double getFactorMaxPivotMagnitude() {
        double retVal = ZERO;
        for (int s = 0, rank = Math.min(myRowDim, myColDim); s < rank; s++) {
            double pivot = mySequence.diagonal[s];
            retVal = Math.max(retVal, Double.isFinite(pivot) ? Math.abs(pivot) : ZERO);
        }
        return retVal;
    }

    /**
     * Smallest pivot magnitude at the last factorisation, before any updates.
     *
     * @deprecated v58 No longer used internally, and will be removed. Use {@link #getMinPivotMagnitude()} to
     *             include the pivots from updates.
     */
    @Deprecated
    public double getFactorMinPivotMagnitude() {
        double retVal = Double.MAX_VALUE;
        for (int s = 0, rank = Math.min(myRowDim, myColDim); s < rank; s++) {
            double pivot = mySequence.diagonal[s];
            retVal = Math.min(retVal, Double.isFinite(pivot) ? Math.abs(pivot) : ZERO);
        }
        return retVal;
    }

    /**
     * [A] = [P][L][R]<sup>-1</sup>[U] where [R] is the identity until there has been an update.
     */
    @Override
    public List<InvertibleFactor<Double>> getFactors() {

        List<InvertibleFactor<Double>> retVal = new ArrayList<>(4);

        retVal.add(this.getFactorP());
        retVal.add(this.getFactorL());
        retVal.add(new FactorR());
        retVal.add(this.getFactorU());

        return retVal;
    }

    @Override
    public MatrixStore<Double> getInverse(final PhysicalStore<Double> preallocated) {
        return this.getSolution(this.makeIdentity(myRowDim), preallocated);
    }

    /**
     * Unit lower triangular, with the rows in pivot order. Not updated by
     * {@link #updateColumn(int, Access1D.Collectable)}.
     */
    @Override
    public MatrixStore<Double> getL() {

        int rank = Math.min(myRowDim, myColDim);
        int[] index = mySequence.getReversePivotOrder();

        SparseStore<Double> retVal = SparseStore.R064.make(myRowDim, rank);

        for (int k = 0; k < rank; k++) {
            retVal.set(k, k, ONE);
        }
        for (int t = 0; t < myL.count; t++) {
            int column = index[myL.pivotRow[t]];
            for (int p = myL.start[t], limit = myL.start[t + 1]; p < limit; p++) {
                retVal.set(index[myL.index[p]], column, myL.value[p]);
            }
        }

        return retVal;
    }

    /**
     * Largest pivot magnitude since the last factorisation, including the pivots from updates.
     *
     * @deprecated v58 No longer used internally, and will be removed.
     */
    @Deprecated
    public double getMaxPivotMagnitude() {
        return myStatistics.maxPivot;
    }

    /**
     * Smallest pivot magnitude since the last factorisation, including the pivots from updates.
     */
    public double getMinPivotMagnitude() {
        return myStatistics.minPivot;
    }

    @Override
    public int[] getPivotOrder() {
        return mySequence.getPivotOrder().clone();
    }

    @Override
    public double getRankThreshold() {

        double largest = MACHINE_SMALLEST;
        for (int s = 0; s < mySequence.count; s++) {
            if (mySequence.row[s] >= 0) {
                largest = Math.max(largest, Math.abs(mySequence.diagonal[s]));
            }
        }

        return largest * this.getDimensionalEpsilon();
    }

    @Override
    public int[] getReversePivotOrder() {
        return mySequence.getReversePivotOrder().clone();
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    @Override
    public MatrixStore<Double> getSolution(final Collectable<Double, ? super PhysicalStore<Double>> rhs, final PhysicalStore<Double> preallocated) {

        rhs.supplyTo(preallocated);

        AbstractDecomposition.ftranColumns(this, preallocated);

        return preallocated;
    }

    /**
     * Upper triangular (after {@link #decompose(Access2D.Collectable)}) with the rows in pivot order. After
     * {@link #factor(R064CSC, int[])} the columns are permuted. Only valid until the first update: after
     * {@link #updateColumn(int, Access1D.Collectable)} the factorisation is [P][L][R]<sup>-1</sup>[U], with
     * [R] and the updated [U] available as {@link #getFactors()}.
     */
    @Override
    public MatrixStore<Double> getU() {

        int rank = Math.min(myRowDim, myColDim);
        int[] index = mySequence.getReversePivotOrder();

        SparseStore<Double> retVal = SparseStore.R064.make(rank, myColDim);

        for (int s = 0; s < mySequence.count; s++) {
            int row = mySequence.row[s];
            if (row >= 0) {
                int k = index[row];
                retVal.set(k, mySequence.position[s], mySequence.diagonal[s]);
                for (int p = myU.rowStart[s], limit = myU.rowEnd[s]; p < limit; p++) {
                    if (myU.rowValue[p] != ZERO) {
                        retVal.set(k, myU.rowPosition[p], myU.rowValue[p]);
                    }
                }
            }
        }

        return retVal;
    }

    @Override
    public MatrixStore<Double> invert(final Access2D<?> original, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(original));

        if (this.isSolvable()) {
            return this.getInverse(preallocated);
        } else {
            throw RecoverableCondition.newMatrixNotInvertible();
        }
    }

    @Override
    public boolean isPivoted() {
        return mySequence.isPivoted();
    }

    @Override
    public boolean isSolvable() {
        return super.isSolvable();
    }

    @Override
    public PhysicalStore<Double> preallocate(final int nbEquations, final int nbVariables, final int nbSolutions) {
        return this.makeZero(nbEquations, nbSolutions);
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        this.decompose(this.wrap(body));

        if (this.isSolvable()) {
            return this.getSolution(this.wrap(rhs), preallocated);
        } else {
            throw RecoverableCondition.newEquationSystemNotSolvable();
        }
    }

    /**
     * Forrest-Tomlin update. Fails, leaving the decomposition unchanged, if the new pivot is not significant
     * compared to the largest pivot since the last factorisation. As specified by
     * {@link MatrixDecomposition.Updatable}, it only returns true if the updated decomposition is solvable.
     * An update can not repair a rank deficient factorisation (the zero pivots remain), so then it returns
     * false even if the column was replaced.
     */
    @Override
    public boolean updateColumn(final int columnIndex, final Access1D.Collectable<Double, ? super TransformableRegion<Double>> newColumn) {

        newColumn.supplyTo(R064Store.wrap(myWork.rowIndexed.values));
        myWork.rowIndexed.reindex();

        this.ftranLower(myWork.rowIndexed);
        myWork.rowIndexed.supplyTo(myWork.spike);
        myWork.rowIndexed.reset();

        return this.update(columnIndex) && this.isSolvable();
    }

    /**
     * Forrest-Tomlin update, the new column taken from a CSC matrix. Reuses the partial result of a preceding
     * {@link #ftranColumn(R064CSC, int, DensityTrackingArray)} of that same column (the matrix is assumed to
     * be the same), and of a preceding {@link #btranUnit(int, DensityTrackingArray)} for the replaced column
     * index. Fails, leaving the decomposition unchanged, if the new pivot is not significant compared to the
     * largest pivot since the last factorisation. Unlike {@link #updateColumn(int, Access1D.Collectable)} it
     * does not check whether the result is solvable.
     *
     * @param columnIndex  The column index being replaced
     * @param matrix       The matrix containing the new column
     * @param sourceColumn The column index, in that matrix, of the new column
     * @return true if the update succeeded
     */
    public boolean updateColumn(final int columnIndex, final R064CSC matrix, final int sourceColumn) {

        if (myWork.spikeColumn != sourceColumn) {
            matrix.supplyTo(sourceColumn, myWork.rowIndexed);
            this.ftranLower(myWork.rowIndexed);
            myWork.rowIndexed.supplyTo(myWork.spike);
            myWork.rowIndexed.reset();
        }

        return this.update(columnIndex);
    }

    /**
     * (Re)allocate the dimension dependent arrays, if the dimensions changed.
     */
    private void allocate(final int nbRows, final int nbCols) {

        if (mySequence != null && nbRows == myRowDim && nbCols == myColDim) {
            return;
        }

        myRowDim = nbRows;
        myColDim = nbCols;

        int rank = Math.min(nbRows, nbCols);
        int maxDim = Math.max(nbRows, nbCols);
        int initial = Math.max(16, 2 * maxDim);
        int slots = rank + 64;

        mySequence = new PivotSequence(nbRows, nbCols, slots);
        myL = new EtaSequence(rank, initial);
        myR = new EtaSequence(16, 16);

        myU = new UpperFactor(slots, initial);

        myWork = new Work(nbRows, nbCols);
        mySearch = new DepthFirstSearch(maxDim);

        myFactoriser = new Factoriser(nbRows, nbCols);

        myStatistics = new Statistics();
    }

    /**
     * Apply the transposed R-etas (in reverse order) and then solve with [L] transposed. The index of the
     * argument is maintained.
     */
    private void btranLower(final DensityTrackingArray arg) {
        myR.solveRowEtasTransposed(arg, mySearch);
        boolean hyper = SparseLU.isHyper(arg.countNonzeros(), myRowDim, myStatistics.btranDensity, HYPER_BTRAN_L);
        myL.solveColumnEtasTransposed(arg, mySearch, hyper, mySequence.getPivotOrder());
    }

    /**
     * Apply the transposed R-etas (in reverse order) and then solve with [L] transposed.
     */
    private void btranLowerDense(final double[] arg) {
        myR.solveRowEtasTransposed(arg);
        myL.solveColumnEtasTransposed(arg);
    }

    /**
     * Solve with [U] transposed.
     *
     * @param rhs    The right hand side, indexed by column (reset when done)
     * @param result The solution, indexed by row (assumed reset)
     */
    private void btranUpper(final DensityTrackingArray rhs, final DensityTrackingArray result) {
        boolean hyper = SparseLU.isHyper(rhs.countNonzeros(), myColDim, myStatistics.btranDensity, HYPER_BTRAN_U);
        myU.solveTransposed(rhs, result, mySequence, mySearch, hyper);
    }

    /**
     * @param markowitz true: triangular pass and then Markowitz pivot selection (rows and columns), false:
     *                  the columns in order and threshold partial pivoting
     */
    private boolean factorise(final R064CSC matrix, final int[] columns, final boolean markowitz) {

        this.reset();

        this.allocate(matrix.getRowDim(), columns.length);

        myDropTolerance = markowitz ? TINY : ZERO;
        myWork.reset();
        myR.reset();

        int nbEntries = myFactoriser.factorise(matrix, columns, markowitz, myDropTolerance, mySequence, myL, myU);

        int rank = Math.min(myRowDim, myColDim);

        mySequence.order(rank);
        myL.buildRowWise(myRowDim);

        double largest = ZERO;
        double smallest = Double.MAX_VALUE;
        for (int s = 0; s < rank; s++) {
            double pivot = mySequence.diagonal[s];
            double magnitude = Double.isFinite(pivot) ? Math.abs(pivot) : ZERO;
            largest = Math.max(largest, magnitude);
            smallest = Math.min(smallest, magnitude);
        }
        myStatistics.factorised(myL.size + nbEntries + rank, largest, smallest);

        return this.computed(true);
    }

    /**
     * Solve with [L] and then apply the R-etas (in update order). The index of the argument is maintained.
     */
    private void ftranLower(final DensityTrackingArray arg) {
        boolean hyper = SparseLU.isHyper(arg.countNonzeros(), myRowDim, myStatistics.ftranDensity, HYPER_FTRAN_L);
        myL.solveColumnEtas(arg, mySearch, hyper);
        myR.solveRowEtas(arg, mySearch);
    }

    /**
     * Solve with [L] and then apply the R-etas (in update order).
     */
    private void ftranLowerDense(final double[] arg) {
        myL.solveColumnEtas(arg);
        myR.solveRowEtas(arg);
    }

    /**
     * Solve with [U].
     *
     * @param rhs    The right hand side, indexed by row (reset when done)
     * @param result The solution, indexed by column (assumed reset)
     */
    private void ftranUpper(final DensityTrackingArray rhs, final DensityTrackingArray result) {
        boolean hyper = SparseLU.isHyper(rhs.countNonzeros(), myRowDim, myStatistics.ftranDensity, HYPER_FTRAN_U);
        myU.solve(rhs, result, mySequence, mySearch, hyper);
    }

    /**
     * Copy a vector in row order to one in pivot order (as used by the factors from {@link #getFactors()}).
     */
    private void toPivotOrder(final double[] rowIndexed, final double[] pivotOrdered) {
        int[] index = mySequence.getReversePivotOrder();
        for (int i = 0; i < myRowDim; i++) {
            pivotOrdered[index[i]] = rowIndexed[i];
        }
    }

    /**
     * A copy, in row order, of a vector in pivot order (as used by the factors from {@link #getFactors()}).
     */
    private double[] toRowOrder(final double[] pivotOrdered) {
        int[] index = mySequence.getReversePivotOrder();
        double[] retVal = new double[myRowDim];
        for (int i = 0; i < myRowDim; i++) {
            retVal[i] = pivotOrdered[index[i]];
        }
        return retVal;
    }

    /**
     * Forrest-Tomlin update, with the spike already in {@link Work#spike}. Fails, leaving the decomposition
     * unchanged, if the new pivot is not finite or not significant compared to the largest pivot.
     */
    private boolean update(final int columnIndex) {

        if (myWork.unitRowColumn != columnIndex) {
            myWork.columnIndexed.set(columnIndex, ONE);
            myWork.unitRow.reset();
            this.btranUpper(myWork.columnIndexed, myWork.unitRow);
        }
        myWork.forget();

        int[] unitRowIndices = myWork.unitRow.indices();
        int nbUnitRow = myWork.unitRow.countNonzeros();

        int leavingSlot = mySequence.slotOfPosition[columnIndex];
        int pivotRow = mySequence.row[leavingSlot];
        double oldPivot = mySequence.diagonal[leavingSlot];

        double newPivot = oldPivot * myWork.unitRow.dot(myWork.spike.values);
        double magnitude = Math.abs(newPivot);

        if (!Double.isFinite(newPivot) || magnitude <= Math.max(myStatistics.maxPivot, magnitude) * this.getDimensionalEpsilon()) {
            return false;
        }

        myU.delete(leavingSlot, mySequence);

        int newSlot = mySequence.replace(leavingSlot, newPivot);

        int uEntries = myU.append(newSlot, myWork.spike, myDropTolerance, mySequence);

        int rEntries = myR.size;
        for (int k = 0; k < nbUnitRow; k++) {
            int i = unitRowIndices[k];
            double value = myWork.unitRow.values[i];
            if (i != pivotRow && value != ZERO) {
                double r = -oldPivot * value;
                if (Math.abs(r) > myDropTolerance) {
                    myR.add(i, r);
                }
            }
        }
        myR.close(pivotRow);

        myStatistics.updated(uEntries + myR.size - rEntries, magnitude);

        return this.computed(true);
    }

    @Override
    protected boolean checkSolvability() {
        return this.isSquare() && this.isFullRank();
    }

    /**
     * [A] = [P][L][U]
     */
    MatrixDecomposition.Factor<Double> getFactorL() {
        return new FactorL();
    }

    /**
     * [A] = [P][L][U]
     */
    MatrixDecomposition.Factor<Double> getFactorP() {
        return new FactorPivot<>(this.makeIdentity(myRowDim), mySequence.getRowPivot(), true);
    }

    /**
     * Always empty, the column permutation (if any) is part of [U].
     */
    Optional<MatrixDecomposition.Factor<Double>> getFactorQ() {
        return Optional.empty();
    }

    /**
     * [A] = [P][L][U]
     */
    MatrixDecomposition.Factor<Double> getFactorU() {
        return new FactorU();
    }

}
