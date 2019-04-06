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

import org.ojalgo.array.BasicArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.structure.Access2D;

/**
 * Computes Q while decomposing.
 *
 * @author apete
 */
class SimultaneousTridiagonal extends TridiagonalDecomposition<Double> {

    private BasicArray<Double> myDiagD;
    private BasicArray<Double> myDiagE;

    SimultaneousTridiagonal() {
        super(PrimitiveDenseStore.FACTORY);
    }

    public boolean decompose(final Access2D.Collectable<Double, ? super PhysicalStore<Double>> matrix) {

        this.setInPlace(matrix);

        final int size = this.getMinDim();

        if ((myDiagD == null) || (myDiagD.count() == size)) {
            myDiagD = Primitive64Array.make(size);
            myDiagE = Primitive64Array.make(size);
        }

        this.getInPlace().tred2(myDiagD, myDiagE, true);

        return this.computed(true);
    }

    @Override
    protected void supplyDiagonalTo(final double[] d, final double[] e) {
        myDiagD.supplyTo(d);
        myDiagE.supplyTo(e);
    }

    @Override
    MatrixStore<Double> makeD() {

        final DiagonalBasicArray<Double> tmpAccess = new DiagonalBasicArray<>(myDiagD, myDiagE, myDiagE, PrimitiveMath.ZERO);

        return this.wrap(tmpAccess).get();
    }

    @Override
    DecompositionStore<Double> makeQ() {
        return this.getInPlace();
    }

}
