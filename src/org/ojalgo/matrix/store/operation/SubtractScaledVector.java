/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

/**
 * y -= ax
 * 
 * @param data y-data
 * @param dataBaseIndex y-column base index
 * @param vector x-data
 * @param vectorBaseIndex x-column base index
 * @param scalar a
 * @param first First index
 * @param limit Index limit
 */
public final class SubtractScaledVector extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] data, final int dataBaseIndex, final BigDecimal[] vector, final int vectorBaseIndex, final BigDecimal scalar,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            data[dataBaseIndex + i] = BigFunction.SUBTRACT.invoke(data[dataBaseIndex + i], BigFunction.MULTIPLY.invoke(scalar, vector[vectorBaseIndex + i])); // y -= ax
        }
    }

    public static void invoke(final ComplexNumber[] data, final int dataBaseIndex, final ComplexNumber[] vector, final int vectorBaseIndex,
            final ComplexNumber scalar, final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            data[dataBaseIndex + i] = data[dataBaseIndex + i].subtract(scalar.multiply(vector[vectorBaseIndex + i])); // y -= ax
        }
    }

    public static void invoke(final double[] data, final int dataBaseIndex, final double[] vector, final int vectorBaseIndex, final double scalar,
            final int first, final int limit) {
        for (int i = first; i < limit; i++) {
            data[dataBaseIndex + i] -= scalar * vector[vectorBaseIndex + i]; // y -= ax
        }
    }

    private SubtractScaledVector() {
        super();
    }

}
