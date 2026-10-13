/*
 * Copyright 1997-2021 Optimatika
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
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

/**
 * @author apete
 */
abstract class VectorOperation {

    protected static final VectorSpecies<Double> SPECIES_DOUBLE = DoubleVector.SPECIES_PREFERRED;
    protected static final VectorSpecies<Float> SPECIES_FLOAT = FloatVector.SPECIES_PREFERRED;

    public static void main(final String... args) {

        float[] a = new float[56];
        float[] b = new float[56];
        float[] c = new float[56];

        VectorOperation.computation(a, b, c);
    }

    static void computation(final float[] a, final float[] b, final float[] c) {

        int limit = MissingMath.min(a.length, b.length, c.length);
        int bound = SPECIES_FLOAT.loopBound(limit);

        int i = 0;
        for (; i < bound; i += SPECIES_FLOAT.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES_FLOAT, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES_FLOAT, b, i);
            va.mul(va).add(vb.mul(vb)).neg().intoArray(c, i);
        }
        for (; i < limit; i++) {
            c[i] = ((a[i] * a[i]) + (b[i] * b[i])) * -1.0f;
        }
    }

}
