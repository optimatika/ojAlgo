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
package org.ojalgo.matrix.store.operation;

import org.ojalgo.array.blas.AXPY;
import org.ojalgo.scalar.Scalar;

public final class ApplyLDU extends MatrixOperation {

    public static final ApplyLDU SETUP = new ApplyLDU();

    public static int THRESHOLD = 256;

    public static void invoke(final double[] data, final int structure, final int firstColumn, final int columnLimit, final double[] multipliers,
            final int iterationPoint, final boolean hermitian) {
        for (int j = firstColumn; j < columnLimit; j++) {
            final double tmpScalar = hermitian ? multipliers[j] : data[iterationPoint + (j * structure)];
            final int tmpFirstRow = hermitian ? j : iterationPoint + 1;
            AXPY.invoke(data, j * structure, -tmpScalar, multipliers, 0, tmpFirstRow, structure);
        }
    }

    public static <N extends Number & Scalar<N>> void invoke(final N[] data, final int structure, final int firstColumn, final int columnLimit,
            final N[] multipliers, final int iterationPoint, final boolean hermitian) {
        for (int j = firstColumn; j < columnLimit; j++) {
            final Scalar<N> tmpScalar = hermitian ? multipliers[j].conjugate() : data[iterationPoint + (j * structure)];
            final int tmpFirstRow = hermitian ? j : iterationPoint + 1;
            AXPY.invoke(data, j * structure, tmpScalar.negate().get(), multipliers, 0, tmpFirstRow, structure);
        }
    }

    private ApplyLDU() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
