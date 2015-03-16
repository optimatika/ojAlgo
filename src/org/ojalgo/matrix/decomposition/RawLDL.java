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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.type.context.NumberContext;

public final class RawLDL extends RawDecomposition implements LDL<Double> {

    public boolean compute(final Access2D<?> matrix) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isFullSize() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSolvable() {
        // TODO Auto-generated method stub
        return false;
    }

    public MatrixStore<Double> reconstruct() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<Double> solve(final Access2D<Double> rhs) {
        // TODO Auto-generated method stub
        return null;
    }

    public Double calculateDeterminant(final Access2D<Double> matrix) {
        // TODO Auto-generated method stub
        return null;
    }

    public Double getDeterminant() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<Double> getL() {
        // TODO Auto-generated method stub
        return null;
    }

    public MatrixStore<Double> getD() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRank() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isSquareAndNotSingular() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public MatrixStore<Double> getInverse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean compute(final RawStore matrix) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    RawStore solve(final RawStore rhs) {
        // TODO Auto-generated method stub
        return null;
    }

}
