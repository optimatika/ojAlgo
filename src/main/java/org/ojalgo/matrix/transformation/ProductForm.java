/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.matrix.transformation;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

public final class ProductForm implements InvertibleFactor<Double> {

    private final List<InvertibleFactor<Double>> myFactors = new ArrayList<>();
    private final int myDim;
    private final PhysicalStore<Double> myWork;

    public ProductForm(final int dim) {
        super();
        myDim = dim;
        myWork = Primitive64Store.FACTORY.make(dim, 1);
    }

    public void replace(final SparseArray<Double> values, final int col) {
        values.supplyTo(myWork);
        this.ftran(myWork);
        myFactors.add(ElementaryFactor.from(myWork, col));
    }

    public void btran(final PhysicalStore<Double> arg) {
        for (int i = myFactors.size() - 1; i >= 0; i--) {
            myFactors.get(i).btran(arg);
        }
    }

    public void ftran(final PhysicalStore<Double> arg) {
        for (InvertibleFactor<Double> invertibleFactor : myFactors) {
            invertibleFactor.ftran(arg);
        }
    }

    public void reset() {
        myFactors.clear();
    }

    public long countColumns() {
        return myDim;
    }

    public long countRows() {
        return myDim;
    }

}
