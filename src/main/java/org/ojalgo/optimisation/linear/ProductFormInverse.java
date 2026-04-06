/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.type.ObjectPool;
import org.ojalgo.type.context.NumberContext;

/**
 * Product-form-of-the-inverse (PFI) {@link BasisRepresentation}. Each basis update is recorded as an eta
 * vector (elementary column operation). Solving a system with B applies the sequence of eta factors in order.
 * Periodically re-inverts via LU to bound numerical drift and factor count.
 *
 * @see SparseDecomposition
 */
final class ProductFormInverse implements BasisRepresentation {

    static final class ArrayPool extends ObjectPool<double[]> {

        private final int myDim;

        ArrayPool(final int dim) {
            super();
            myDim = dim;
        }

        @Override
        protected double[] newObject() {
            return new double[myDim];
        }

        @Override
        protected void reset(final double[] object) {
            Arrays.fill(object, ZERO);
        }
    }

    static final class ElementaryFactor implements InvertibleFactor<Double> {

        private final double[] myColumn;
        private final int myIndex;
        private final double myNegatedDiagonal;

        ElementaryFactor(final double[] column, final int index, final double diagonalElement) {
            super();
            myColumn = column;
            myIndex = index;
            myNegatedDiagonal = -diagonalElement;
        }

        @Override
        public void btran(final double[] arg) {

            double f = -arg[myIndex];

            for (int i = 0, lim = arg.length; i < lim; i++) {
                if (i != myIndex) {
                    f += myColumn[i] * arg[i];
                }
            }

            if (f != ZERO) {
                f /= myNegatedDiagonal;
                arg[myIndex] = f;
            } else {
                arg[myIndex] = ZERO;
            }
        }

        @Override
        public void btran(final PhysicalStore<Double> arg) {

            double f = -arg.doubleValue(myIndex);

            for (int i = 0, lim = arg.size(); i < lim; i++) {
                if (i != myIndex) {
                    f += myColumn[i] * arg.doubleValue(i);
                }
            }

            if (f != ZERO) {
                f /= myNegatedDiagonal;
                arg.set(myIndex, f);
            } else {
                arg.set(myIndex, ZERO);
            }
        }

        @Override
        public void ftran(final double[] arg) {

            double d = arg[myIndex];

            if (d == ZERO) {
                return;
            }

            d /= myNegatedDiagonal;

            for (int i = 0, lim = arg.length; i < lim; i++) {
                if (i == myIndex) {
                    arg[i] = -d;
                } else {
                    arg[i] += myColumn[i] * d;
                }
            }
        }

        @Override
        public void ftran(final PhysicalStore<Double> arg) {

            double d = arg.doubleValue(myIndex);

            if (d == ZERO) {
                return;
            }

            d /= myNegatedDiagonal;

            for (int i = 0, lim = arg.size(); i < lim; i++) {
                if (i == myIndex) {
                    arg.set(i, -d);
                } else {
                    arg.add(i, myColumn[i] * d);
                }
            }
        }

        @Override
        public int getColDim() {
            return myColumn.length;
        }

        @Override
        public int getRowDim() {
            return myColumn.length;
        }

        double[] getColumn() {
            return myColumn;
        }

    }

    private static final NumberContext SAFE = NumberContext.of(3);
    /**
     * Maximum number of eta factors before forcing a full refactorisation. Benchmarking showed only ~15%
     * solve-cost degradation at 200 updates (dim=1000), so 100 is conservative.
     */
    private static final int UPDATES_LIMIT = 100;

    private final ObjectPool<double[]> myArrayPool;
    private final int myDim;
    private final List<ElementaryFactor> myFactors = new ArrayList<>(UPDATES_LIMIT);
    private final LU<Double> myRoot;

    ProductFormInverse(final int dim) {

        super();

        myDim = dim;
        myRoot = LU.R064.make(dim, dim);
        myArrayPool = new ArrayPool(dim);
    }

    @Override
    public void btran(final double[] arg) {

        for (int i = myFactors.size() - 1; i >= 0; i--) {
            myFactors.get(i).btran(arg);
        }
        if (myRoot.isComputed()) {
            myRoot.ftran(arg);
        }
    }

    @Override
    public void btran(final PhysicalStore<Double> arg) {

        for (int i = myFactors.size() - 1; i >= 0; i--) {
            myFactors.get(i).btran(arg);
        }
        if (myRoot.isComputed()) {
            myRoot.ftran(arg);
        }
    }

    @Override
    public void ftran(final double[] arg) {

        if (myRoot.isComputed()) {
            myRoot.btran(arg);
        }
        for (InvertibleFactor<Double> factor : myFactors) {
            factor.ftran(arg);
        }
    }

    @Override
    public void ftran(final PhysicalStore<Double> arg) {

        if (myRoot.isComputed()) {
            myRoot.btran(arg);
        }
        for (InvertibleFactor<Double> factor : myFactors) {
            factor.ftran(arg);
        }
    }

    @Override
    public int getColDim() {
        return myDim;
    }

    @Override
    public int getRowDim() {
        return myDim;
    }

    /**
     * Update the product form inverse to reflect a replaced column.
     */
    @Override
    public void reset(final R064CSC matrix, final int[] included) {

        for (ElementaryFactor factor : myFactors) {
            myArrayPool.giveBack(factor.getColumn());
        }
        myFactors.clear();

        myRoot.decompose(matrix.columns(included).transpose());
    }

    /**
     * Update the inverse to reflect a replaced column in the basis.
     */
    @Override
    public boolean update(final R064CSC matrix, final int[] included, final int exitIndex, final int enterColumn) {

        double[] column = myArrayPool.borrow();

        matrix.supplyTo(enterColumn, column);

        this.ftran(column);

        double diagonalElement = column[exitIndex];

        if (myFactors.size() >= UPDATES_LIMIT || SAFE.isZero(diagonalElement)) {

            myArrayPool.giveBack(column);
            this.reset(matrix, included);
            return true;

        } else {

            myFactors.add(new ElementaryFactor(column, exitIndex, diagonalElement));
            return false;
        }
    }

}
