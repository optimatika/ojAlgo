/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.access.Mutate1D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.DenseArray.Factory;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

final class SparseTableau extends SimplexTableau {

    class ConstraintsBody implements Mutate2D {

        public void add(final long row, final long col, final double addend) {
            myRows[(int) row].add(col, addend);
            myPhase1Weights.add(col, -addend);
        }

        public void add(final long row, final long col, final Number addend) {
            this.add(row, col, addend.doubleValue());
        }

        public long countColumns() {
            return myNumberOfVariables;
        }

        public long countRows() {
            return myNumberOfConstraints;
        }

        public void set(final long row, final long col, final double value) {
            myRows[(int) row].set(col, value);
            myPhase1Weights.add(col, -value);
        }

        public void set(final long row, final long col, final Number value) {
            this.set(row, col, value.doubleValue());
        }

    }

    class ConstraintsRHS implements Mutate1D {

        public void add(final long index, final double addend) {
            myRows[(int) index].set(myNumberOfVariables + index, ONE);
            myRHS.add(index, addend);
            myInfeasibility -= addend;
        }

        public void add(final long index, final Number addend) {
            this.add(index, addend.doubleValue());
        }

        public long count() {
            return myNumberOfConstraints;
        }

        public void set(final long index, final double value) {
            myRows[(int) index].set(myNumberOfVariables + index, ONE);
            myRHS.set(index, value);
            myInfeasibility -= value;
        }

        public void set(final long index, final Number value) {
            this.set(index, value.doubleValue());
        }

    }

    class Objective implements Mutate1D {

        public void add(final long index, final double addend) {
            myObjectiveWeights.add(index, addend);
        }

        public void add(final long index, final Number addend) {
            this.add(index, addend.doubleValue());
        }

        public long count() {
            return myNumberOfVariables;
        }

        public void set(final long index, final double value) {
            myObjectiveWeights.set(index, value);
        }

        public void set(final long index, final Number value) {
            this.set(index, value.doubleValue());
        }

    }

    private transient ConstraintsBody myConstraintsBody = null;
    private transient ConstraintsRHS myConstraintsRHS = null;
    private double myInfeasibility = ZERO;
    private final int myNumberOfConstraints;
    private final long myNumberOfVariables;
    private transient Objective myObjective = null;
    private final DenseArray<Double> myObjectiveWeights;
    private final DenseArray<Double> myPhase1Weights;
    private final DenseArray<Double> myRHS;
    private final SparseArray<Double>[] myRows;
    private double myValue = ZERO;

    @SuppressWarnings("unchecked")
    SparseTableau(final int numberOfConstraints, final int numberOfVariables) {

        super();

        myNumberOfConstraints = numberOfConstraints;
        myNumberOfVariables = numberOfVariables;

        // Including artificial variables
        final int totNumbVars = numberOfVariables + numberOfConstraints;

        final Factory<Double> denseFactory = Primitive64Array.FACTORY;

        final SparseFactory<Double> sparseFactory = SparseArray.factory(denseFactory, totNumbVars).initial(3).limit(totNumbVars);

        myRows = new SparseArray[numberOfConstraints];
        for (int r = 0; r < numberOfConstraints; r++) {
            myRows[r] = sparseFactory.make();
        }
        myRHS = denseFactory.makeZero(numberOfConstraints);

        myObjectiveWeights = denseFactory.makeZero(totNumbVars);
        myPhase1Weights = denseFactory.makeZero(totNumbVars);
    }

    public long countColumns() {
        return myNumberOfVariables + myNumberOfConstraints + 1L;
    }

    public long countConstraints() {
        return myNumberOfConstraints;
    }

    public long countRows() {
        return myNumberOfConstraints + 2L;
    }

    public long countVariables() {
        return myNumberOfVariables;
    }

    public double doubleValue(final long row, final long col) {
        if (row < myNumberOfConstraints) {
            if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                return myRows[(int) row].doubleValue(col);
            } else {
                return myRHS.doubleValue(row);
            }
        } else if (row == myNumberOfConstraints) {
            if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                return myObjectiveWeights.doubleValue(col);
            } else {
                return myValue;
            }
        } else {
            if (col < (myNumberOfVariables + myNumberOfConstraints)) {
                return myPhase1Weights.doubleValue(col);
            } else {
                return myInfeasibility;
            }
        }
    }

    public Double get(final long row, final long col) {
        return this.doubleValue(row, col);
    }

    ConstraintsBody constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = new ConstraintsBody();
        }
        return myConstraintsBody;
    }

    ConstraintsRHS constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = new ConstraintsRHS();
        }
        return myConstraintsRHS;
    }

    /**
     * @return The phase 1 objective function value
     */
    double getInfeasibility() {
        return myInfeasibility;
    }

    Objective objective() {
        if (myObjective == null) {
            myObjective = new Objective();
        }
        return myObjective;
    }

    @Override
    protected void pivot(final IterationPoint iterationPoint) {

        final int row = iterationPoint.row;
        final int col = iterationPoint.col;

        final SparseArray<Double> pivotRow = myRows[row];
        final double pivotElement = pivotRow.doubleValue(col);

        if (PrimitiveFunction.ABS.invoke(pivotElement) < ONE) {
            final UnaryFunction<Double> tmpModifier = DIVIDE.second(pivotElement);
            pivotRow.modifyAll(tmpModifier);
            myRHS.modifyOne(row, tmpModifier);
        } else if (pivotElement != ONE) {
            final UnaryFunction<Double> tmpModifier = MULTIPLY.second(ONE / pivotElement);
            pivotRow.modifyAll(tmpModifier);
            myRHS.modifyOne(row, tmpModifier);
        }

        final double pivotedRHS = myRHS.doubleValue(row);

        double colVal;

        for (int i = 0; i < myRows.length; i++) {
            if (i != row) {
                final SparseArray<Double> rowY = myRows[i];
                colVal = -rowY.doubleValue(col);
                if (colVal != ZERO) {
                    pivotRow.axpy(colVal, rowY);
                    myRHS.add(i, colVal * pivotedRHS);
                }
            }
        }

        colVal = -myObjectiveWeights.doubleValue(col);
        if (colVal != ZERO) {
            pivotRow.axpy(colVal, myObjectiveWeights);
            myValue += colVal * pivotedRHS;
        }

        // TODO Stop updating phase 1 objective hen in phase 2.

        colVal = -myPhase1Weights.doubleValue(col);
        if (colVal != ZERO) {
            pivotRow.axpy(colVal, myPhase1Weights);
            myInfeasibility += colVal * pivotedRHS;
        }

    }

    PrimitiveDenseStore transpose() {

        final PrimitiveDenseStore retVal = PrimitiveDenseStore.FACTORY.makeZero(this.countColumns(), this.countRows());

        for (int i = 0; i < myRows.length; i++) {
            for (final NonzeroView<Double> nz : myRows[i].nonzeros()) {
                retVal.set(nz.index(), i, nz.doubleValue());
            }
        }

        retVal.fillColumn(myRows.length, myObjectiveWeights);
        retVal.fillColumn(myRows.length + 1, myPhase1Weights);

        retVal.fillRow(myNumberOfVariables + myNumberOfConstraints, myRHS);

        retVal.fillOne(myNumberOfVariables + myNumberOfConstraints, myNumberOfConstraints, myValue);
        retVal.fillOne(myNumberOfVariables + myNumberOfConstraints, myNumberOfConstraints + 1, myInfeasibility);

        return retVal;
    }

}
