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
package org.ojalgo.array.operation;

import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;

public final class HouseholderLeft implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static void invoke(final double[] data, final int structure, final int first, final int limit, final Householder.Primitive64 householder) {

        final double[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final double tmpBeta = householder.beta;

        double tmpScale;
        for (int j = first; j < limit; j++) {
            tmpScale = DOT.invoke(data, j * structure, tmpHouseholderVector, 0, tmpFirstNonZero, structure);
            tmpScale *= tmpBeta;
            AXPY.invoke(data, j * structure, -tmpScale, tmpHouseholderVector, 0, tmpFirstNonZero, structure);
        }
    }

    public static void invoke(final float[] data, final int structure, final int first, final int limit, final Householder.Primitive32 householder) {

        final float[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final float tmpBeta = householder.beta;

        float tmpScale;
        for (int j = first; j < limit; j++) {
            tmpScale = DOT.invoke(data, j * structure, tmpHouseholderVector, 0, tmpFirstNonZero, structure);
            tmpScale *= tmpBeta;
            AXPY.invoke(data, j * structure, -tmpScale, tmpHouseholderVector, 0, tmpFirstNonZero, structure);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {

        final N[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final N tmpBeta = householder.beta;

        Scalar<N> tmpScale;
        int tmpIndex;
        for (int j = first; j < limit; j++) {
            tmpScale = scalar.zero();
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                tmpScale = tmpScale.add(tmpHouseholderVector[i].conjugate().multiply(data[tmpIndex++]));
            }
            tmpScale = tmpScale.multiply(tmpBeta);
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                data[tmpIndex] = data[tmpIndex].subtract(tmpScale.multiply(tmpHouseholderVector[i])).get();
                tmpIndex++;
            }
        }
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
