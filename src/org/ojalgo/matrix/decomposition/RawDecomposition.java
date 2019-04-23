/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.Collectable;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * In many ways similar to InPlaceDecomposition but this class is hardwired to work with double[][] data. Most
 * of it's originates from JAMA, but have been significantly refactored or even (re)written from scratch.
 *
 * @author apete
 */
abstract class RawDecomposition extends AbstractDecomposition<Double> {

    private int myColDim;
    private double[][] myInternalData;
    private RawStore myInternalStore;
    private int myRowDim;

    protected RawDecomposition() {
        super();
    }

    @Override
    protected PrimitiveDenseStore allocate(final long numberOfRows, final long numberOfColumns) {
        // TODO Should use RawStore.FACTORY rather than PrimitiveDenseStore.FACTORY
        return PrimitiveDenseStore.FACTORY.makeZero(numberOfRows, numberOfColumns);
    }

    protected boolean checkSymmetry() {
        boolean retVal = myRowDim == myColDim;
        for (int i = 0; retVal && (i < myRowDim); i++) {
            for (int j = 0; retVal && (j < i); j++) {
                retVal &= (NumberContext.compare(myInternalData[i][j], myInternalData[j][i]) == 0);
            }
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    protected MatrixStore<Double> collect(final Access2D.Collectable<Double, ? super DecompositionStore<Double>> source) {
        // TODO Should use RawStore.FACTORY rather than PrimitiveDenseStore.FACTORY
        if (source instanceof MatrixStore) {
            return (MatrixStore<Double>) source;
        } else if (source instanceof Access2D) {
            return PrimitiveDenseStore.FACTORY.builder().makeWrapper((Access2D<?>) source).get();
        } else {
            return source.collect(PrimitiveDenseStore.FACTORY);
        }
    }

    protected int getColDim() {
        return myColDim;
    }

    @Override
    protected double getDimensionalEpsilon() {
        return this.getMaxDim() * MACHINE_EPSILON;
    }

    protected double[][] getInternalData() {
        return myInternalData;
    }

    protected RawStore getInternalStore() {
        return myInternalStore;
    }

    protected int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    protected int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    protected int getRowDim() {
        return myRowDim;
    }

    protected Collectable<Double, ? super PhysicalStore<Double>> wrap(Access2D<?> matrix) {
        return MatrixStore.PRIMITIVE.makeWrapper(matrix);
    }

    double[][] reset(final Structure2D template, final boolean transpose) {

        this.reset();

        final int templateRows = (int) template.countRows();
        final int templateCols = (int) template.countColumns();

        final int internalRows = transpose ? templateCols : templateRows;
        final int internalCols = transpose ? templateRows : templateCols;

        if ((myInternalData == null) || (myRowDim != templateRows) || (myColDim != templateCols)) {

            myInternalStore = RawStore.FACTORY.makeZero(internalRows, internalCols);
            myInternalData = myInternalStore.data;

            myRowDim = templateRows;
            myColDim = templateCols;
        }

        this.aspectRatioNormal(templateRows >= templateCols);

        return myInternalData;
    }

}
