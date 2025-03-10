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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.context.NumberContext;

/**
 * In many ways similar to InPlaceDecomposition but this class is hardwired to work with double[][] data. Most
 * of it's originates from JAMA, but have been significantly refactored or even (re)written from scratch.
 *
 * @author apete
 */
abstract class RawDecomposition extends AbstractDecomposition<Double, R064Store> {

    static RawStore make(final int nbRows, final int nbCols) {
        return RawStore.FACTORY.make(nbRows, nbCols);
    }

    private int myColDim;
    private double[][] myInternalData;
    private RawStore myInternalStore;
    private int myRowDim;

    RawDecomposition() {
        super(R064Store.FACTORY);
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    protected boolean checkSymmetry() {
        boolean retVal = myRowDim == myColDim;
        for (int i = 0; retVal && i < myRowDim; i++) {
            for (int j = 0; retVal && j < i; j++) {
                retVal &= NumberContext.compare(myInternalData[i][j], myInternalData[j][i]) == 0;
            }
        }
        return retVal;
    }

    protected double[][] getInternalData() {
        return myInternalData;
    }

    protected RawStore getInternalStore() {
        return myInternalStore;
    }

    RawStore newRawStore(final int m, final int n) {
        return RawStore.FACTORY.make(m, n);
    }

    double[][] reset(final Structure2D template, final boolean transpose) {

        this.reset();

        int templateRows = template.getRowDim();
        int templateCols = template.getColDim();

        int internalRows = transpose ? templateCols : templateRows;
        int internalCols = transpose ? templateRows : templateCols;

        if (myInternalData == null || myRowDim != templateRows || myColDim != templateCols) {

            myInternalStore = RawStore.FACTORY.make(internalRows, internalCols);
            myInternalData = myInternalStore.data;

            myRowDim = templateRows;
            myColDim = templateCols;
        }

        return myInternalData;
    }

    RawStore wrap(final double[][] data) {
        return RawStore.wrap(data);
    }

}
