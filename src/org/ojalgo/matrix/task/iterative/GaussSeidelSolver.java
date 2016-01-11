/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.TaskException;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.type.context.NumberContext;

public class GaussSeidelSolver extends StationaryIterativeSolver {

    protected static final class Row implements Comparable<Row> {

        /**
         * The row index of the original body matrix, [A].
         */
        public final int index;
        /**
         * The nonzero elements of this row
         */
        private final SparseArray<Double> myElements;
        private double myPivot = ZERO;

        public Row(final int row, final long numberOfColumns) {
            super();
            index = row;
            myElements = SparseArray.makePrimitive(numberOfColumns);
        }

        public int compareTo(final Row other) {
            return Integer.compare(index, other.index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Row)) {
                return false;
            }
            final Row other = (Row) obj;
            if (index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + index;
            return result;
        }

        public void set(final long index, final double value) {
            myElements.set(index, value);
            if (index == this.index) {
                myPivot = value;
            }
        }

        public void solve(final PhysicalStore<Double> x, final double rhs, final double relaxation) {

            double tmpIncrement = rhs;

            tmpIncrement -= myElements.dot(x);

            tmpIncrement *= relaxation;

            tmpIncrement /= myPivot;

            x.add(index, tmpIncrement);
        }

        @Override
        public String toString() {
            return index + ": " + myElements.toString();
        }

    }

    private Access2D<?> myRHS;
    private final List<Row> myRows = new ArrayList<>();

    public GaussSeidelSolver() {
        super();
    }

    public GaussSeidelSolver(final int iterationsLimit) {
        super(iterationsLimit);
    }

    public GaussSeidelSolver(final NumberContext terminationContext) {
        super(terminationContext);
    }

    public GaussSeidelSolver(final NumberContext terminationContext, final int iterationsLimit) {
        super(terminationContext, iterationsLimit);
    }

    @Override
    public MatrixStore<Double> iterate(final PhysicalStore<Double> current, final double relaxation) {

        for (int r = 0; r < myRows.size(); r++) {
            final Row tmpRow = myRows.get(r);
            tmpRow.solve(current, myRHS.doubleValue(r), relaxation);
        }

        return current;
    }

    /**
     * A variation of {@linkplain #solve(Access2D, Access2D, DecompositionStore)} where you do not supply the
     * equation system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final MatrixStore<Double> resolve(final Access2D<?> rhs, final DecompositionStore<Double> preallocated) throws TaskException {

        this.setup(rhs);

        double tmpCurrNorm = NEG;
        double tmpLastNorm = tmpCurrNorm;

        int tmpIterations = 0;
        final int tmpIterationsLimit = this.getIterationsLimit();
        final NumberContext tmpCntxt = this.getTerminationContext();
        do {

            this.iterate(preallocated, this.getRelaxationFactor());

            tmpLastNorm = tmpCurrNorm;
            tmpCurrNorm = preallocated.aggregateAll(Aggregator.NORM2);

            tmpIterations++;

        } while ((tmpIterations < tmpIterationsLimit) && tmpCntxt.isDifferent(tmpLastNorm, tmpCurrNorm));

        return preallocated;
    }

    /**
     * A variation of {@linkplain #solve(MatrixStore, MatrixStore)} where you do not supply the equation
     * system <code>body</code>. It is assumed to have been set up beforehand.
     */
    public final Optional<MatrixStore<Double>> resolve(final MatrixStore<Double> rhs) {
        try {
            return Optional.of(this.resolve(rhs, this.preallocate(rhs)));
        } catch (final TaskException xcptn) {
            return Optional.empty();
        }
    }

    public void setup(final Access2D<?> rhs) {
        myRHS = rhs;
    }

    @Override
    public void setup(final Access2D<?> body, final Access2D<?> rhs) {

        final long tmpDim = rhs.countRows();

        for (int i = 0; i < tmpDim; i++) {
            final Row tmpRow = new Row(i, tmpDim);
            for (int j = 0; j < tmpDim; j++) {
                final double tmpVal = body.doubleValue(i, j);
                if (!PrimitiveScalar.isSmall(ONE, tmpVal)) {
                    tmpRow.set(j, tmpVal);
                }
            }
            myRows.add(tmpRow);
        }

        myRHS = rhs;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        for (final Row tmpRow : myRows) {
            retVal.append(tmpRow.toString());
            retVal.append("\n");
        }

        return retVal.toString();
    }

    protected boolean add(final Row row) {
        final boolean retVal = myRows.add(row);
        Collections.sort(myRows);
        return retVal;
    }

    protected long countRows() {
        return myRows.size();
    }

    protected double doubleValue(final int row, final int column) {
        return myRows.get(row).myElements.doubleValue(column);
    }

    protected DecompositionStore<Double> preallocate(final Structure2D templateRHS) {
        if (templateRHS.countColumns() != 1L) {
            throw new IllegalArgumentException("The RHS must have precisely 1 column!");
        }
        return PrimitiveDenseStore.FACTORY.makeZero(templateRHS.countRows(), 1L);

    }

    protected boolean remove(final Row row) {
        return myRows.remove(row);
    }

}
