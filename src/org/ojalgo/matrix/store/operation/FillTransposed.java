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

import org.ojalgo.access.Access2D;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;

public final class FillTransposed extends MatrixOperation {

    public static final FillTransposed SETUP = new FillTransposed();

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int tmpIndex = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[tmpIndex++] = TypeUtils.toBigDecimal(source.get(j, i));
            }
        }
    }

    public static void invoke(final ComplexNumber[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int tmpIndex = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[tmpIndex++] = ComplexNumber.valueOf((Number) source.get(j, i));
            }
        }
    }

    public static void invoke(final double[] data, final int structure, final int firstColumn, final int limitColumn, final Access2D<?> source) {
        int tmpIndex = structure * firstColumn;
        for (int j = firstColumn; j < limitColumn; j++) {
            for (int i = 0; i < structure; i++) {
                data[tmpIndex++] = source.doubleValue(j, i);
            }
        }
    }

    private FillTransposed() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
