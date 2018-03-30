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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.array.blas.AXPY;
import org.ojalgo.scalar.Scalar;

public final class ApplyLDL extends MatrixOperation {

    public static final ApplyLDL SETUP = new ApplyLDL();

    public static int THRESHOLD = 256;

    public static void invoke(final BigDecimal[] data, final int structure, final int firstColumn, final int columnLimit, final BigDecimal[] multipliers,
            final int iterationPoint) {
        final BigDecimal tmpDiagVal = data[iterationPoint + (iterationPoint * structure)];
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, tmpDiagVal.multiply(multipliers[j]).negate(), multipliers, 0, j, structure);
        }
    }

    public static void invoke(final double[] data, final int structure, final int firstColumn, final int columnLimit, final double[] multipliers,
            final int iterationPoint) {
        final double tmpDiagVal = data[iterationPoint + (iterationPoint * structure)];
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, -(tmpDiagVal * multipliers[j]), multipliers, 0, j, structure);
        }
    }

    public static <N extends Number & Scalar<N>> void invoke(final N[] data, final int structure, final int firstColumn, final int columnLimit,
            final N[] multipliers, final int iterationPoint) {
        final Scalar<N> tmpDiagVal = data[iterationPoint + (iterationPoint * structure)];
        for (int j = firstColumn; j < columnLimit; j++) {
            AXPY.invoke(data, j * structure, tmpDiagVal.multiply(multipliers[j].conjugate()).negate().get(), multipliers, 0, j, structure);
        }
    }

    private ApplyLDL() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
