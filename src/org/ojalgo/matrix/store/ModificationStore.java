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
package org.ojalgo.matrix.store;

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.scalar.Scalar;

public class ModificationStore<N extends Number> extends LogicalStore<N> {

    private final UnaryFunction<N> myFunction;

    public ModificationStore(final MatrixStore<N> aBase, final UnaryFunction<N> aFunc) {

        super((int) aBase.countRows(), (int) aBase.countColumns(), aBase);

        myFunction = aFunc;
    }

    private ModificationStore(final int aRowDim, final int aColDim, final MatrixStore<N> aBase) {

        super(aRowDim, aColDim, aBase);

        myFunction = null;
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myFunction.invoke(this.getBase().doubleValue(aRow, aCol));
    }

    public N get(final long aRow, final long aCol) {
        return myFunction.invoke(this.getBase().get(aRow, aCol));
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public Scalar<N> toScalar(final long row, final long column) {
        return this.factory().scalar().convert(this.get(row, column));
    }

}
