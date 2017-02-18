/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.access.Access2D;
import org.ojalgo.access.Mutate2D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.SparseArray;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;

final class Tableau implements Access2D<Double>, Mutate2D {

    private double myInfeasibility;
    private final long myNumberOfConstraints;
    private final long myNumberOfVariables;
    private double myObjective;
    private final Primitive64Array myPhase1Weights;
    private final Primitive64Array myRHS;
    private final SparseArray<Double>[] myRows;
    private final Primitive64Array myWeights;

    @SuppressWarnings("unchecked")
    Tableau(final int numberOfConstraints, final int numberOfVariables) {

        super();

        myNumberOfConstraints = numberOfConstraints;
        myNumberOfVariables = numberOfVariables;

        myRows = new SparseArray[numberOfConstraints];
        for (int r = 0; r < numberOfConstraints; r++) {
            myRows[r] = SparseArray.make(Primitive64Array.FACTORY, myNumberOfVariables, 4);
        }
        myRHS = Primitive64Array.make((int) myNumberOfConstraints);
        myWeights = Primitive64Array.make((int) myNumberOfVariables);
        myPhase1Weights = Primitive64Array.make((int) myNumberOfVariables);
    }

    public void add(final long row, final long col, final double addend) {
        if (row < myNumberOfConstraints) {
            if (col < myNumberOfVariables) {
                myRows[(int) row].add(col, addend);
            } else {
                myRHS.add(row, addend);
            }
        } else if (row == myNumberOfConstraints) {
            if (col < myNumberOfVariables) {
                myWeights.add(col, addend);
            } else {
                myObjective += addend;
            }
        } else {
            if (col < myNumberOfVariables) {
                myPhase1Weights.add(col, addend);
            } else {
                myInfeasibility += addend;
            }
        }
    }

    public void add(final long row, final long col, final Number addend) {
        this.add(row, col, addend.doubleValue());
    }

    public long countColumns() {
        return myNumberOfVariables + 1L;
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
            if (col < myNumberOfVariables) {
                return myRows[(int) row].doubleValue(col);
            } else {
                return myRHS.doubleValue(row);
            }
        } else if (row == myNumberOfConstraints) {
            if (col < myNumberOfVariables) {
                return myWeights.doubleValue(col);
            } else {
                return myObjective;
            }
        } else {
            if (col < myNumberOfVariables) {
                return myPhase1Weights.doubleValue(col);
            } else {
                return myInfeasibility;
            }
        }
    }

    public Double get(final long row, final long col) {
        return this.doubleValue(row, col);
    }

    public void set(final long row, final long col, final double value) {
        if (row < myNumberOfConstraints) {
            if (col < myNumberOfVariables) {
                myRows[(int) row].set(col, value);
            } else {
                myRHS.set(row, value);
            }
        } else if (row == myNumberOfConstraints) {
            if (col < myNumberOfVariables) {
                myWeights.set(col, value);
            } else {
                myObjective = value;
            }
        } else {
            if (col < myNumberOfVariables) {
                myPhase1Weights.set(col, value);
            } else {
                myInfeasibility = value;
            }
        }
    }

    public void set(final long row, final long col, final Number value) {
        this.set(row, col, value.doubleValue());
    }

    /**
     * @return The phase 1 objective function value
     */
    double getInfeasibility() {
        return myInfeasibility;
    }

    Primitive64Array getRHS() {
        return myRHS;
    }

    void pivot(final int row, final int col) {

        final SparseArray<Double> tmpPivotRow = myRows[row];
        final double tmpPivotElement = tmpPivotRow.doubleValue(col);

        if (PrimitiveFunction.ABS.invoke(tmpPivotElement) < ONE) {
            final UnaryFunction<Double> tmpModifier = DIVIDE.second(tmpPivotElement);
            tmpPivotRow.modifyAll(tmpModifier);
            myRHS.modifyOne(row, tmpModifier);
        } else if (tmpPivotElement != ONE) {
            final UnaryFunction<Double> tmpModifier = MULTIPLY.second(ONE / tmpPivotElement);
            tmpPivotRow.modifyAll(tmpModifier);
            myRHS.modifyOne(row, tmpModifier);
        }

        final double tmpPivotRHS = myRHS.doubleValue(row);

        double tmpVal;

        for (int i = 0; i < row; i++) {
            final SparseArray<Double> tmpY = myRows[i];
            tmpVal = -tmpY.doubleValue(col);
            tmpPivotRow.axpy(tmpVal, tmpY);
            myRHS.add(i, (tmpVal * tmpPivotRHS));
        }
        for (int i = row + 1; i < myRows.length; i++) {
            final SparseArray<Double> tmpY = myRows[i];
            tmpVal = -tmpY.doubleValue(col);
            tmpPivotRow.axpy(tmpVal, tmpY);
            myRHS.add(i, tmpVal * tmpPivotRHS);
        }

        tmpVal = -myWeights.doubleValue(col);
        tmpPivotRow.axpy(tmpVal, myWeights);
        myObjective += tmpVal * tmpPivotRHS;

        tmpVal = -myPhase1Weights.doubleValue(col);
        tmpPivotRow.axpy(tmpVal, myPhase1Weights);
        myInfeasibility += tmpVal * tmpPivotRHS;
    }

}
