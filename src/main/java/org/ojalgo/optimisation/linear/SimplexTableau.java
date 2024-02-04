/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.CorePrimitiveOperation;
import org.ojalgo.array.operation.IndexOf;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ProblemStructure;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D;
import org.ojalgo.type.IndexSelector;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.context.NumberContext;

abstract class SimplexTableau implements Access2D<Double>, Mutate2D {

    static abstract class DenseTableau extends SimplexTableau {

        DenseTableau(final LinearStructure linearStructure) {
            super(linearStructure);
        }

    }

    static final class RawTableau extends DenseTableau {

        private final int myColDim;
        private final double[][] myRaw;

        RawTableau(final LinearStructure linearStructure) {

            super(linearStructure);

            int nbRows = m + 2;
            int nbCols = n + 1;

            myRaw = new double[nbRows][nbCols];
            myColDim = nbCols;
        }

        RawTableau(final SimplexTableau toCopy) {

            super(toCopy.structure);

            myRaw = toCopy.toRawCopy2D();
            myColDim = toCopy.getColDim();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myRaw[row][col];
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myRaw.length;
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myRaw[row][col] = value;
        }

        private void doPivot(final int row, final int col, final double[] pivotRow) {

            for (int i = 0, limit = myRaw.length; i < limit; i++) {
                if (i != row) {
                    double[] dataRow = myRaw[i];
                    double colVal = dataRow[col];
                    if (colVal != ZERO) {
                        AXPY.invoke(dataRow, 0, -colVal, pivotRow, 0, 0, myColDim);
                    }
                }
            }
        }

        private void scale(final double[] pivotRow, final int col) {
            double pivotElement = pivotRow[col];
            if (pivotElement != ONE) {
                CorePrimitiveOperation.divide(pivotRow, 0, myColDim, 1, pivotRow, pivotElement);
            }
        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = IndexOf.indexOf(included, index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            // Array1D<Double> currentRow = myTransposed.sliceColumn(row);
            ArrayR064 currentRow = ArrayR064.wrap(myRaw[row]);
            double currentRHS = currentRow.doubleValue(myColDim - 1);

            final ArrayR064 auxiliaryRow = ArrayR064.make(myColDim);
            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, value - currentRHS);
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, currentRHS - value);
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(m);

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            this.scale(auxiliaryRow.data, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow.data);

            // myTransposed.fillColumn(row, auxiliaryRow);
            myRaw[row] = auxiliaryRow.data;

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        @Override
        double getInfeasibility() {
            return myRaw[m + 1][n];
        }

        @Override
        double getValue() {
            return myRaw[m][n];
        }

        @Override
        Primitive2D newConstraintsBody() {

            return new Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return myRaw[row][col];
                }

                @Override
                public int getColDim() {
                    return structure.countVariables();
                }

                @Override
                public int getRowDim() {
                    return m;
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    myRaw[row][col] = value;

                    if (row < structure.nbIdty) {
                        if (col >= n - m && value == 1D) {
                            RawTableau.this.update(row, col);
                        }
                    } else {
                        myRaw[m + 1][col] -= value;
                    }
                }

            };
        }

        @Override
        Primitive1D newConstraintsRHS() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myRaw[index][n];
                }

                @Override
                public void set(final int index, final double value) {

                    if (structure.nbArti > 0) {
                        myRaw[index][n - m + index] = ONE;
                    }

                    myRaw[index][n] = value;

                    if (index >= structure.nbIdty) {
                        myRaw[m + 1][n] -= value;
                    }
                }

                @Override
                public int size() {
                    return m;
                }

            };
        }

        @Override
        Primitive1D newObjective() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myRaw[m][index];
                }

                @Override
                public void set(final int index, final double value) {
                    myRaw[m][index] = value;
                }

                @Override
                public int size() {
                    return structure.countModelVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexTableauSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            double[] pivotRow = myRaw[row];

            this.scale(pivotRow, col);

            this.doPivot(row, col, pivotRow);

            this.update(row, col);
        }

        @Override
        DenseTableau toDense() {
            return this;
        }

    }

    static final class SparseTableau extends SimplexTableau {

        private double myInfeasibility = ZERO;
        private final Array1D<Double> myObjectiveWeights;
        private final DenseArray<Double> myPhase1Weights;
        private final Array1D<Double> myRHS;
        private final SparseArray<Double>[] myRows;
        private final SparseArray.SparseFactory<Double> mySparseFactory;
        private double myValue = ZERO;

        SparseTableau(final LinearStructure linearStructure) {

            super(linearStructure);

            int nbConstraints = linearStructure.countConstraints();
            int nbProblemVariables = linearStructure.countModelVariables();

            long initial = Math.max(5L, Math.round(Math.sqrt(Math.min(nbConstraints, nbProblemVariables))));
            mySparseFactory = SparseArray.factory(ArrayR064.FACTORY).initial(initial);

            // Including artificial variables
            final int totNumbVars = linearStructure.countModelVariables() + linearStructure.nbSlck + linearStructure.nbIdty + linearStructure.nbArti;

            myRows = new SparseArray[nbConstraints];
            for (int r = 0; r < nbConstraints; r++) {
                myRows[r] = mySparseFactory.make(totNumbVars);
            }

            myRHS = ARRAY1D_FACTORY.make(nbConstraints);

            myObjectiveWeights = ARRAY1D_FACTORY.make(totNumbVars);
            myPhase1Weights = DENSE_FACTORY.make(totNumbVars);
        }

        @Override
        public double doubleValue(final int row, final int col) {

            if (row < m) {
                if (col < n) {
                    return myRows[row].doubleValue(col);
                } else {
                    return myRHS.doubleValue(row);
                }
            } else if (row == m) {
                if (col < n) {
                    return myObjectiveWeights.doubleValue(col);
                }
                return myValue;
            } else if (col < n) {
                return myPhase1Weights.doubleValue(col);
            } else {
                return myInfeasibility;
            }
        }

        @Override
        public int getColDim() {
            return n + 1;
        }

        @Override
        public int getRowDim() {
            return m + 2;
        }

        @Override
        public void set(final int row, final int col, final double value) {

            if (row < m) {
                if (col < n) {
                    myRows[row].set(col, value);
                } else {
                    myRHS.set(row, value);
                }
            } else if (row == m) {
                if (col < n) {
                    myObjectiveWeights.set(col, value);
                } else {
                    myValue = value;
                }
            } else if (col < n) {
                myPhase1Weights.set(col, value);
            } else {
                myInfeasibility = value;
            }
        }

        private void doPivot(final int row, final int col, final SparseArray<Double> pivotRowBody, final double pivotRowRHS) {

            double colVal;

            for (int i = 0; i < myRows.length; i++) {
                if (i != row) {
                    SparseArray<Double> rowY = myRows[i];
                    colVal = -rowY.doubleValue(col);
                    if (colVal != ZERO) {
                        pivotRowBody.axpy(colVal, rowY);
                        myRHS.add(i, colVal * pivotRowRHS);
                    }
                }
            }

            colVal = -myObjectiveWeights.doubleValue(col);
            if (colVal != ZERO) {
                pivotRowBody.axpy(colVal, myObjectiveWeights);
                myValue += colVal * pivotRowRHS;
            }

            colVal = -myPhase1Weights.doubleValue(col);
            if (colVal != ZERO) {
                pivotRowBody.axpy(colVal, myPhase1Weights);
                myInfeasibility += colVal * pivotRowRHS;
            }
        }

        private double scale(final SparseArray<Double> pivotRowBody, final double pivotRowRHS, final int col) {

            double pivotElement = pivotRowBody.doubleValue(col);

            if (pivotElement != ONE) {
                UnaryFunction<Double> modifier = DIVIDE.second(pivotElement);
                pivotRowBody.modifyAll(modifier);
                return modifier.invoke(pivotRowRHS);
            }

            return pivotRowRHS;

        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = IndexOf.indexOf(included, index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            SparseArray<Double> currentRow = myRows[row];
            double currentRHS = myRHS.doubleValue(row);

            SparseArray<Double> auxiliaryRow = mySparseFactory.make(n);
            double auxiliaryRHS = ZERO;

            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRHS = value - currentRHS;
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRHS = currentRHS - value;
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(m);

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            auxiliaryRHS = this.scale(auxiliaryRow, auxiliaryRHS, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow, auxiliaryRHS);

            myRows[row] = auxiliaryRow;
            myRHS.set(row, auxiliaryRHS);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        /**
         * @return The phase 1 objective function value
         */
        @Override
        double getInfeasibility() {
            return myInfeasibility;
        }

        @Override
        double getValue() {
            return myValue;
        }

        @Override
        Primitive2D newConstraintsBody() {

            return new Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return myRows[row].doubleValue(col);
                }

                @Override
                public int getColDim() {
                    return structure.countVariables();
                }

                @Override
                public int getRowDim() {
                    return m;
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    myRows[row].set(col, value);

                    if (row < structure.nbIdty) {
                        if (col >= n - m && value == 1D) {
                            SparseTableau.this.update(row, col);
                        }
                    } else {
                        myPhase1Weights.add(col, -value);
                    }
                }

            };
        }

        @Override
        Primitive1D newConstraintsRHS() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myRHS.doubleValue(index);
                }

                @Override
                public void set(final int index, final double value) {

                    if (structure.nbArti > 0) {
                        myRows[index].set(n - m + index, ONE);
                    }

                    myRHS.set(index, value);

                    if (index >= structure.nbIdty) {
                        myInfeasibility -= value;
                    }
                }

                @Override
                public int size() {
                    return m;
                }

            };
        }

        @Override
        Primitive1D newObjective() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myObjectiveWeights.doubleValue(index);
                }

                @Override
                public void set(final int index, final double value) {
                    myObjectiveWeights.set(index, value);
                }

                @Override
                public int size() {
                    return structure.countModelVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexTableauSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            SparseArray<Double> pivotRowBody = myRows[row];
            double pivotRowRHS = myRHS.doubleValue(row);

            pivotRowRHS = this.scale(pivotRowBody, pivotRowRHS, col);
            myRHS.set(row, pivotRowRHS);

            this.doPivot(row, col, pivotRowBody, pivotRowRHS);

            this.update(iterationPoint);
        }

        @Override
        DenseTableau toDense() {
            return new TransposedTableau(this);
        }

    }

    static final class TransposedTableau extends DenseTableau {

        private final int myColDim;
        private final Primitive64Store myTransposed;

        TransposedTableau(final LinearStructure linearStructure) {

            super(linearStructure);

            int nbRows = m + 2;
            int nbCols = n + 1;

            myTransposed = Primitive64Store.FACTORY.make(nbCols, nbRows);
            myColDim = myTransposed.getRowDim();
        }

        TransposedTableau(final SimplexTableau toCopy) {

            super(toCopy.structure);

            myTransposed = Primitive64Store.FACTORY.transpose(toCopy);
            myColDim = myTransposed.getRowDim();
        }

        @Override
        public double doubleValue(final int row, final int col) {
            return myTransposed.doubleValue(col, row);
        }

        @Override
        public int getColDim() {
            return myColDim;
        }

        @Override
        public int getRowDim() {
            return myTransposed.getColDim();
        }

        @Override
        public void set(final int row, final int col, final double value) {
            myTransposed.set(col, row, value);
        }

        private void doPivot(final int row, final int col, final double[] pivotRowData, final int pivotRowIndexBase) {

            double[] data = myTransposed.data;

            for (int i = 0, limit = myTransposed.getColDim(); i < limit; i++) {
                if (i != row) {
                    int dataIndexBase = i * myColDim;
                    double colVal = data[dataIndexBase + col];
                    if (colVal != ZERO) {
                        AXPY.invoke(data, dataIndexBase, -colVal, pivotRowData, pivotRowIndexBase, 0, myColDim);
                    }
                }
            }
        }

        private void scale(final double[] pivotRowData, final int pivotRowIndexBase, final int col) {
            double pivotElement = pivotRowData[pivotRowIndexBase + col];
            if (pivotElement != ONE) {
                CorePrimitiveOperation.divide(pivotRowData, pivotRowIndexBase, pivotRowIndexBase + myColDim, 1, pivotRowData, pivotElement);
            }
        }

        @Override
        boolean fixVariable(final int index, final double value) {

            int row = IndexOf.indexOf(included, index);

            if (row < 0) {
                return false;
            }

            // Diff begin

            Array1D<Double> currentRow = myTransposed.sliceColumn(row);
            double currentRHS = currentRow.doubleValue(myColDim - 1);

            final ArrayR064 auxiliaryRow = ArrayR064.make(myColDim);
            if (currentRHS > value) {
                currentRow.axpy(NEG, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, value - currentRHS);
            } else if (currentRHS < value) {
                currentRow.axpy(ONE, auxiliaryRow);
                auxiliaryRow.set(index, ZERO);
                auxiliaryRow.set(myColDim - 1, currentRHS - value);
            } else {
                return true;
            }

            // Diff end

            Access1D<Double> objectiveRow = this.sliceTableauRow(m);

            int pivotCol = this.findNextPivotColumn(auxiliaryRow, objectiveRow);

            if (pivotCol < 0) {
                // TODO Problem infeasible?
                // Probably better to return true here, and have the subsequest solver.solve() return INFEASIBLE
                return false;
            }

            // Diff begin

            this.scale(auxiliaryRow.data, 0, pivotCol);

            this.doPivot(-1, pivotCol, auxiliaryRow.data, 0);

            myTransposed.fillColumn(row, auxiliaryRow);

            // Diff end

            for (ElementView1D<Double, ?> elem : this.sliceConstraintsRHS().elements()) {
                if (elem.doubleValue() < ZERO) {
                    return false;
                }
            }

            this.update(row, pivotCol);

            return true;
        }

        @Override
        double getInfeasibility() {
            return myTransposed.doubleValue(n, m + 1);
        }

        @Override
        double getValue() {
            return myTransposed.doubleValue(n, m);
        }

        @Override
        Primitive2D newConstraintsBody() {

            return new Primitive2D() {

                @Override
                public double doubleValue(final int row, final int col) {
                    return myTransposed.doubleValue(col, row);
                }

                @Override
                public int getColDim() {
                    return structure.countVariables();
                }

                @Override
                public int getRowDim() {
                    return m;
                }

                @Override
                public void set(final int row, final int col, final double value) {

                    myTransposed.set(col, row, value);

                    if (row < structure.nbIdty) {
                        if (col >= n - m && value == 1D) {
                            TransposedTableau.this.update(row, col);
                        }
                    } else {
                        myTransposed.add(col, m + 1, -value);
                    }
                }

            };
        }

        @Override
        Primitive1D newConstraintsRHS() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myTransposed.doubleValue(n, index);
                }

                @Override
                public void set(final int index, final double value) {

                    if (structure.nbArti > 0) {
                        myTransposed.set(n - m + index, index, ONE);
                    }

                    myTransposed.set(n, index, value);

                    if (index >= structure.nbIdty) {
                        myTransposed.add(n, m + 1, -value);
                    }
                }

                @Override
                public int size() {
                    return m;
                }

            };
        }

        @Override
        Primitive1D newObjective() {

            return new Primitive1D() {

                @Override
                public double doubleValue(final int index) {
                    return myTransposed.doubleValue(index, m);
                }

                @Override
                public void set(final int index, final double value) {
                    myTransposed.set(index, m, value);
                }

                @Override
                public int size() {
                    return structure.countModelVariables();
                }

            };
        }

        @Override
        void pivot(final SimplexTableauSolver.IterationPoint iterationPoint) {

            int row = iterationPoint.row;
            int col = iterationPoint.col;

            double[] data = myTransposed.data;
            int pivotRowIndexBase = row * myColDim;

            this.scale(data, pivotRowIndexBase, col);

            this.doPivot(row, col, data, pivotRowIndexBase);

            this.update(iterationPoint);
        }

        @Override
        DenseTableau toDense() {
            return this;
        }

    }

    static final Array1D.Factory<Double> ARRAY1D_FACTORY = Array1D.factory(ArrayR064.FACTORY);
    static final DenseArray.Factory<Double> DENSE_FACTORY = ArrayR064.FACTORY;

    static void copy(final ConvexData<?> builder, final SimplexTableau tableau) {

        Mutate2D body = tableau.constraintsBody();
        for (RowView<?> row : builder.getRowsAE()) {
            for (ElementView1D<?, ?> element : row.nonzeros()) {
                body.set(row.row(), element.index(), element.doubleValue());
            }

        }

        Mutate1D rhs = tableau.constraintsRHS();
        MatrixStore<?> mtrxBE = builder.getBE();
        for (int i = 0; i < mtrxBE.size(); i++) {
            rhs.set(i, mtrxBE.doubleValue(i));
        }

        Mutate1D obj = tableau.objective();
        MatrixStore<?> mtrxC = builder.getObjective().getLinearFactors(false);
        for (int i = 0; i < mtrxC.size(); i++) {
            obj.set(i, mtrxC.doubleValue(i));
        }
    }

    static boolean isSparse(final Optimisation.Options options) {
        return options.sparse != null && options.sparse.booleanValue();
    }

    static SimplexTableau make(final ConvexData<Double> builder, final Optimisation.Options options) {

        int nbConstraints = builder.countConstraints();
        int nbVariables = builder.countVariables();

        LinearStructure structure = new LinearStructure(false, 0, nbConstraints, nbVariables, 0, 0, 0);

        SimplexTableau tableau = SimplexTableau.make(structure, options);
        SimplexTableau.copy(builder, tableau);
        return tableau;
    }

    static SimplexTableau make(final LinearStructure structure, final Optimisation.Options options) {

        if (SimplexTableau.isSparse(options)) {
            return new SparseTableau(structure);
        } else {
            return new RawTableau(structure);
        }
    }

    static SimplexTableau newDense(final ConvexData<?> matrices) {

        int constrEq = matrices.countConstraints();

        LinearStructure structure = new LinearStructure(false, 0, constrEq, matrices.countVariables(), 0, 0, 0);

        SimplexTableau tableau = new TransposedTableau(structure);

        SimplexTableau.copy(matrices, tableau);

        return tableau;
    }

    static TransposedTableau newDense(final LinearSolver.Builder<?> builder) {
        return builder.newSimplexTableau(TransposedTableau::new);
    }

    static RawTableau newRaw(final LinearSolver.Builder<?> builder) {
        return builder.newSimplexTableau(RawTableau::new);
    }

    static SparseTableau newSparse(final ConvexData<?> matrices) {

        int constrEq = matrices.countConstraints();

        LinearStructure structure = new LinearStructure(false, 0, constrEq, matrices.countVariables(), 0, 0, 0);

        SparseTableau tableau = new SparseTableau(structure);

        SimplexTableau.copy(matrices, tableau);

        return tableau;
    }

    static SparseTableau newSparse(final LinearSolver.Builder<?> builder) {
        return builder.newSimplexTableau(SparseTableau::new);
    }

    private transient Primitive2D myConstraintsBody = null;
    private transient Primitive1D myConstraintsRHS = null;
    private transient Primitive1D myObjective = null;
    private int myRemainingArtificials;
    private final IndexSelector mySelector;

    final int[] included;
    /**
     * The number of constraints (upper, lower and equality)
     */
    final int m;
    /**
     * The number of variables totally (all kinds)
     */
    final int n;
    final LinearStructure structure;

    /**
     * @param nbConstraints The number of constraints.
     * @param nbPosVars The number of positive problem variables.
     * @param nbNegVars The number of negative problem variables.
     * @param nbSlackVars The number of slack variables (the number of inequality constraints). The
     *        actual/full set of slack variables is nbSlackVariables + nbIdentitySlackVariables
     * @param nbIdentityVars The number of slack variables that form an identity matrix and can reduce the
     *        need for artificial variables. If this is != 0 the nbSlackVariables must be reduced and the
     *        constraints corresponding to these slack variables must be the top/first rows in the tableau.
     * @param needDual Should there be an explicit full set of artificial variables? (to extract the dual). If
     *        this is set to true and nbIdentitySlackVariables > 0 then special care needs to taken when
     *        constructing the tableau. In that case the identity slack variables and the artificial variables
     *        must form an identity matrix in the initial tableau (towards the right hand side).
     */
    SimplexTableau(final LinearStructure linearStructure) {

        super();

        m = linearStructure.countConstraints();
        n = linearStructure.countVariablesTotally();

        included = Structure1D.newIncreasingRange(n - m, m);
        mySelector = new IndexSelector(n, included);

        structure = linearStructure;
        myRemainingArtificials = linearStructure.nbArti;
    }

    @Override
    public long countColumns() {
        return this.getColDim();
    }

    @Override
    public long countRows() {
        return this.getRowDim();
    }

    @Override
    public Double get(final long row, final long col) {
        return Double.valueOf(this.doubleValue(row, col));
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        this.set(row, col, NumberDefinition.doubleValue(value));
    }

    /**
     * The area of the tableau corresponding to the constraints' body (excluding any artificial variables).
     */
    final Primitive2D constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = this.newConstraintsBody();
        }
        return myConstraintsBody;
    }

    /**
     * The area of the tableau corresponding to the constraints' RHS.
     */
    final Primitive1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    /**
     * The number of artificial variables in the basis.
     */
    int countRemainingArtificials() {
        return myRemainingArtificials;
    }

    final int[] excluded() {
        return mySelector.getExcluded();
    }

    int findNextPivotColumn(final Access1D<Double> auxiliaryRow, final Access1D<Double> objectiveRow) {

        int retVal = -1;
        double minQuotient = MACHINE_LARGEST;

        for (ElementView1D<Double, ?> nz : auxiliaryRow.nonzeros()) {
            final int i = (int) nz.index();
            if (i >= structure.countVariables()) {
                break;
            }
            final double denominator = nz.doubleValue();
            if (denominator < -1E-8) {
                double numerator = objectiveRow.doubleValue(i);
                double quotient = Math.abs(numerator / denominator);
                if (quotient < minQuotient) {
                    minQuotient = quotient;
                    retVal = i;
                }
            }
        }

        return retVal;
    }

    abstract boolean fixVariable(final int index, final double value);

    final Collection<Equation> generateCutCandidates(final boolean[] integer, final NumberContext accuracy, final double fractionality) {

        int nbModVars = structure.countModelVariables();

        Primitive1D constraintsRHS = this.constraintsRHS();

        double[] solRHS = new double[integer.length];
        for (int i = 0; i < m; i++) {
            int j = included[i];
            if (j >= 0 && j < nbModVars) {
                solRHS[j] = constraintsRHS.doubleValue(i);
            }
        }

        if (ProblemStructure.DEBUG) {
            BasicLogger.debug("RHS: {}", Arrays.toString(solRHS));
            BasicLogger.debug("Bas: {}", Arrays.toString(included));
        }

        List<Equation> retVal = new ArrayList<>();

        boolean[] negated = new boolean[integer.length];

        for (int i = 0; i < m; i++) {
            int j = included[i];

            double rhs = constraintsRHS.doubleValue(i);

            if (j >= 0 && j < nbModVars && integer[j] && !accuracy.isInteger(rhs)) {

                Equation maybe = TableauCutGenerator.doGomoryMixedInteger(this.sliceBodyRow(i), j, rhs, integer, fractionality, negated,
                        mySelector.getExcluded());

                if (maybe != null) {
                    retVal.add(maybe);
                }
            }
        }

        return retVal;
    }

    /**
     * @return The phase 1 objective function value
     */
    abstract double getInfeasibility();

    /**
     * @return The (phase 2) objective function value
     */
    abstract double getValue();

    boolean isAbleToExtractDual() {
        return structure.nbIdty + structure.nbArti == structure.countConstraints();
    }

    boolean isArtificial(final int col) {
        return structure.isArtificialVariable(col);
    }

    final boolean isExcluded(final int index) {
        return mySelector.isExcluded(index);
    }

    final boolean isIncluded(final int index) {
        return mySelector.isIncluded(index);
    }

    /**
     * Are there any artificial variables in the basis?
     */
    boolean isRemainingArtificials() {
        return myRemainingArtificials > 0;
    }

    abstract Primitive2D newConstraintsBody();

    abstract Primitive1D newConstraintsRHS();

    abstract Primitive1D newObjective();

    /**
     * The area of the tableau corresponding to the objective function.
     */
    final Primitive1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    abstract void pivot(SimplexTableauSolver.IterationPoint iterationPoint);

    final Primitive1D sliceBodyColumn(final int col) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(index, col, value);
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    final Primitive1D sliceBodyRow(final int row) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(row, index, value);
            }

            @Override
            public int size() {
                return structure.countVariables();
            }

        };
    }

    final Primitive1D sliceConstraintsRHS() {
        return this.constraintsRHS();
    }

    /**
     * @return An array of the dual variable values (of the original problem, never phase 1).
     */
    final Primitive1D sliceDualVariables() {

        int base = n - m;

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(m, base + index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(m, base + index, value);
            }

            @Override
            public int size() {
                return m;
            }

        };
    }

    final Primitive1D sliceTableauColumn(final int col) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(index, col);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(index, col, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getRowDim();
            }

        };
    }

    final Primitive1D sliceTableauRow(final int row) {

        return new Primitive1D() {

            @Override
            public double doubleValue(final int index) {
                return SimplexTableau.this.doubleValue(row, index);
            }

            @Override
            public void set(final int index, final double value) {
                SimplexTableau.this.set(row, index, value);
            }

            @Override
            public int size() {
                return SimplexTableau.this.getColDim();
            }

        };
    }

    abstract DenseTableau toDense();

    void update(final int pivotRow, final int pivotCol) {

        int tmpOld = included[pivotRow];
        if (tmpOld >= 0) {
            mySelector.exclude(tmpOld);
        }
        if (this.isArtificial(tmpOld)) {
            --myRemainingArtificials;
        }

        int tmpNew = pivotCol;
        if (tmpNew >= 0) {
            mySelector.include(tmpNew);
        }

        included[pivotRow] = pivotCol;
    }

    void update(final long pivotRow, final long pivotCol) {
        this.update(Math.toIntExact(pivotRow), Math.toIntExact(pivotCol));
    }

    void update(final SimplexTableauSolver.IterationPoint point) {
        this.update(point.row, point.col);
    }

    /**
     * The current, phase 1 or 2, objective function value
     */
    final double value(final boolean phase1) {
        if (phase1) {
            return this.getInfeasibility();
        } else {
            return this.getValue();
        }
    }

}
