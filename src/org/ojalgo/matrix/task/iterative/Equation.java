/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Mutate1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.PhysicalStore;

public final class Equation implements Comparable<Equation>, Access1D<Double>, Mutate1D {

    /**
     * The row index of the original body matrix, [A].
     */
    public final int index;
    /**
     * The nonzero elements of this equation/row
     */
    private final SparseArray<Double> myElements;
    private double myPivot = ZERO;
    private final double myRHS;

    public Equation(final int row, final long numberOfColumns, final double rhs) {
        super();
        index = row;
        myElements = SparseArray.factory(Primitive64Array.FACTORY, numberOfColumns).make();
        myRHS = rhs;
    }

    public Equation(final int row, final long numberOfColumns, final double rhs, final int numberOfNonzeros) {
        super();
        index = row;
        myElements = SparseArray.factory(Primitive64Array.FACTORY, numberOfColumns).initial(numberOfNonzeros).make();
        myRHS = rhs;
    }

    public void add(final long index, final double addend) {
        myElements.add(index, addend);
        if (index == this.index) {
            myPivot = myElements.doubleValue(index);
        }
    }

    public void add(final long index, final Number addend) {
        this.add(index, addend.doubleValue());
    }

    /**
     * Will perform a (relaxed) GaussSeidel update.
     *
     * @param x The current solution (one element will be updated)
     * @param relaxation Typically 1.0 but could be anything (Most likely should be between 0.0 and 2.0).
     * @return The error in this equation
     */
    public double adjust(final PhysicalStore<Double> x, final double relaxation) {
        return this.calculate(x, myRHS, relaxation);
    }

    public int compareTo(final Equation other) {
        return Integer.compare(index, other.index);
    }

    public long count() {
        return myElements.count();
    }

    public double dot(final Access1D<?> vector) {
        return myElements.dot(vector);
    }

    public double doubleValue(final long index) {
        return myElements.doubleValue(index);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Equation)) {
            return false;
        }
        final Equation other = (Equation) obj;
        if (index != other.index) {
            return false;
        }
        return true;
    }

    public Double get(final long index) {
        return myElements.get(index);
    }

    /**
     * @return The element at {@link #index}
     */
    public double getPivot() {
        return myPivot;
    }

    /**
     * @return The equation RHS
     */
    public double getRHS() {
        return myRHS;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + index;
        return result;
    }

    public void initialise(final PhysicalStore<Double> x) {
        this.calculate(x, ZERO, ONE);
    }

    public void set(final long index, final double value) {
        myElements.set(index, value);
        if (index == this.index) {
            myPivot = value;
        }
    }

    public void set(final long index, final Number value) {
        this.set(index, value.doubleValue());
    }

    @Override
    public String toString() {
        return index + ": " + myElements.toString();
    }

    private double calculate(final PhysicalStore<Double> x, final double rhs, final double relaxation) {

        double tmpIncrement = rhs;

        final double tmpError = tmpIncrement -= myElements.dot(x);

        tmpIncrement *= relaxation;

        tmpIncrement /= myPivot;

        x.add(index, tmpIncrement);

        return tmpError;
    }

}
