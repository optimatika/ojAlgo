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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.function.BigFunction;
import org.ojalgo.scalar.ComplexNumber;

public abstract class AXPY implements BLAS1 {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] vectorY, final int offsetY, final int stepY, final BigDecimal scalar, final BigDecimal[] vectorX,
            final int offsetX, final int stepX, final int count) {
        for (int i = 0; i < count; i++) {
            vectorY[offsetY + (i * stepY)] = BigFunction.ADD.invoke(BigFunction.MULTIPLY.invoke(scalar, vectorX[offsetX + (i * stepX)]), vectorY[offsetY
                    + (i * stepY)]); // y += ax
        }
    }

    public static void invoke(final ComplexNumber[] vectorY, final int offsetY, final int stepY, final ComplexNumber scalar, final ComplexNumber[] vectorX,
            final int offsetX, final int stepX, final int count) {
        for (int i = 0; i < count; i++) {
            vectorY[offsetY + (i * stepY)] = scalar.multiply(vectorX[offsetX + (i * stepX)]).add(vectorY[offsetY + (i * stepY)]); // y += ax
        }
    }

    public static void invoke(final double[] vectorY, final int offsetY, final int stepY, final double scalar, final double[] vectorX, final int offsetX,
            final int stepX, final int count) {
        for (int i = 0; i < count; i++) {
            vectorY[offsetY + (i * stepY)] += scalar * vectorX[offsetX + (i * stepX)]; // y += ax
        }
    }

}
