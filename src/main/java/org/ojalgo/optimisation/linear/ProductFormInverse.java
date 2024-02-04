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

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.array.SparseArray.NonzeroView;
import org.ojalgo.array.SparseArray.SparseFactory;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.type.ObjectPool;

final class ProductFormInverse implements InvertibleFactor<Double> {

    static final class ArrayPool extends ObjectPool<SparseArray<Double>> {

        private static final SparseFactory<Double> FACTORY = SparseArray.factory(ArrayR064.FACTORY);

        private final int myDim;

        ArrayPool(final int dim) {
            super();
            myDim = dim;
        }

        @Override
        protected SparseArray<Double> newObject() {
            return FACTORY.make(myDim);
        }

        @Override
        protected void reset(final SparseArray<Double> object) {
            object.reset();
        }
    }

    static final class ElementaryFactor implements InvertibleFactor<Double> {

        private final SparseArray<Double> myColumn;
        private final int myIndex;
        private final double myNegatedDiagonal;

        ElementaryFactor(final SparseArray<Double> column, final int index, final double diagonalElement) {
            super();
            myColumn = column;
            myIndex = index;
            myNegatedDiagonal = -diagonalElement;
        }

        public void btran(final PhysicalStore<Double> arg) {

            double f = -arg.doubleValue(myIndex);

            for (NonzeroView<Double> nz : myColumn.nonzeros()) {
                long index = nz.index();
                if (index != myIndex) {
                    f += nz.doubleValue() * arg.doubleValue(index);
                }
            }

            if (f != ZERO) {
                f /= myNegatedDiagonal;
                arg.set(myIndex, f);
            } else {
                arg.set(myIndex, ZERO);
            }
        }

        public long countColumns() {
            return myColumn.count();
        }

        public long countRows() {
            return myColumn.count();
        }

        public void ftran(final PhysicalStore<Double> arg) {

            double d = arg.doubleValue(myIndex);

            if (d == ZERO) {
                return;
            }

            d /= myNegatedDiagonal;

            for (NonzeroView<Double> nz : myColumn.nonzeros()) {
                long index = nz.index();
                if (index == myIndex) {
                    arg.set(index, -d);
                } else {
                    arg.add(index, nz.doubleValue() * d);
                }
            }
        }

        SparseArray<Double> getColumn() {
            return myColumn;
        }

    }

    private final ObjectPool<SparseArray<Double>> myArrayPool;
    private final int myDim;
    private final List<ElementaryFactor> myFactors = new ArrayList<>();
    private final LU<Double> myRoot;
    private final Primitive64Store myWork;
    private final double myScalingThreshold;

    ProductFormInverse(final int dim, final double scalingThreshold) {

        super();

        myDim = dim;
        myRoot = LU.R064.make(dim, dim);
        myWork = Primitive64Store.FACTORY.make(dim, 1);
        myArrayPool = new ArrayPool(dim);
        myScalingThreshold = scalingThreshold;
    }

    public void btran(final PhysicalStore<Double> arg) {
        for (int i = myFactors.size() - 1; i >= 0; i--) {
            myFactors.get(i).btran(arg);
        }
        if (myRoot.isSolvable()) {
            myRoot.ftran(arg);
        }
    }

    public long countColumns() {
        return myDim;
    }

    public long countRows() {
        return myDim;
    }

    public void ftran(final PhysicalStore<Double> arg) {
        if (myRoot.isSolvable()) {
            myRoot.btran(arg);
        }
        for (InvertibleFactor<Double> factor : myFactors) {
            factor.ftran(arg);
        }
    }

    private void clearFactors() {
        for (ElementaryFactor factor : myFactors) {
            myArrayPool.giveBack(factor.getColumn());
        }
        myFactors.clear();
    }

    private ElementaryFactor newFactor(final Primitive64Store values, final int col, final double diagonalElement) {

        SparseArray<Double> sparse = myArrayPool.borrow();

        for (int i = 0, limit = values.size(); i < limit; i++) {
            double value = values.doubleValue(i);
            if (value != ZERO) {
                sparse.set(i, value);
            }
        }

        return new ElementaryFactor(sparse, col, diagonalElement);
    }

    void reset() {
        this.clearFactors();
        myRoot.reset();
    }

    /**
     * Update the product form inverse to reflect a replaced column.
     *
     * @param basis Full basis, with the column already exchanged.
     */
    void reset(final MatrixStore<Double> basis) {
        this.clearFactors();
        myRoot.decompose(basis.transpose());
    }

    /**
     * Update the product form inverse to reflect a replaced column.
     *
     * @param col The column, of the basis, that was exchanged.
     * @param values The (non zero) values of that column.
     */
    void update(final int col, final SparseArray<Double> values) {

        values.supplyTo(myWork);

        this.ftran(myWork);

        double diagonalElement = myWork.doubleValue(col);

        myFactors.add(this.newFactor(myWork, col, diagonalElement));
    }

    /**
     * Update the inverse to reflect a replaced column in the basis.
     *
     * @param basis Full basis, with the column already exchanged.
     * @param index The index, of the column, that was exchanged.
     * @param values The (non zero) values of that column.
     */
    void update(final MatrixStore<Double> basis, final int index, final SparseArray<Double> values) {

        values.supplyTo(myWork);

        this.ftran(myWork);

        double diagonalElement = myWork.doubleValue(index);

        if (Math.abs(diagonalElement) >= myScalingThreshold) {
            myFactors.add(this.newFactor(myWork, index, diagonalElement));
        } else {
            this.clearFactors();
            myRoot.decompose(basis.transpose());
        }
    }

}
