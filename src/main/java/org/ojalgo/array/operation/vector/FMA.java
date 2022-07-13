/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.array.operation.vector;

import org.ojalgo.function.special.MissingMath;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;

public abstract class FMA extends VectorOperation {

    public static void daxpy(final double[] y, final double a, final double[] x) {
        FMA.invoke(y, a, x, y); // y = a x + y
    }

    public static void invoke(final double[] data, final double multiplier, final double[] base, final double[] addend) {

        int limit = MissingMath.min(data.length, base.length, addend.length);

        int i = 0;

        int bound = SPECIES_DOUBLE.loopBound(limit);
        int length = SPECIES_DOUBLE.length();

        DoubleVector mv = DoubleVector.broadcast(SPECIES_DOUBLE, multiplier);

        for (; i < bound; i += length) {
            DoubleVector bv = DoubleVector.fromArray(SPECIES_DOUBLE, base, i);
            DoubleVector av = DoubleVector.fromArray(SPECIES_DOUBLE, addend, i);
            DoubleVector dv = mv.lanewise(VectorOperators.FMA, bv, av);
            dv.intoArray(data, i);
        }

        for (; i < limit; i++) {
            data[i] = Math.fma(multiplier, base[i], addend[i]);
        }
    }

    public static void invoke(final double[] data, final double[] multiplier, final double[] base, final double[] addend) {

        int limit = MissingMath.min(data.length, multiplier.length, base.length, addend.length);

        int i = 0;

        int bound = SPECIES_DOUBLE.loopBound(limit);
        int length = SPECIES_DOUBLE.length();

        for (; i < bound; i += length) {
            DoubleVector mv = DoubleVector.fromArray(SPECIES_DOUBLE, multiplier, i);
            DoubleVector bv = DoubleVector.fromArray(SPECIES_DOUBLE, base, i);
            DoubleVector av = DoubleVector.fromArray(SPECIES_DOUBLE, addend, i);
            DoubleVector dv = mv.lanewise(VectorOperators.FMA, bv, av);
            dv.intoArray(data, i);
        }

        for (; i < limit; i++) {
            data[i] = Math.fma(multiplier[i], base[i], addend[i]);
        }
    }

}
