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
package org.ojalgo.matrix.operation;

import java.util.function.IntSupplier;

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;

public final class HouseholderLeft implements BLAS2 {

    public static IntSupplier PARALLELISM = Parallelism.CORES;
    public static int THRESHOLD = 128;

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.divider();

    public static void call(final double[] data, final int structure, final int first, final int limit, final Householder.Primitive64 householder) {
        HouseholderLeft.divide(first, limit, (f, l) -> {
            HouseholderLeft.invoke(data, structure, f, l, householder);
        });
    }

    public static void call(final float[] data, final int structure, final int first, final int limit, final Householder.Primitive32 householder) {
        HouseholderLeft.divide(first, limit, (f, l) -> {
            HouseholderLeft.invoke(data, structure, f, l, householder);
        });
    }

    public static <N extends Scalar<N>> void call(final N[] data, final int structure, final int first, final int limit,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {
        HouseholderLeft.divide(first, limit, (f, l) -> {
            HouseholderLeft.invoke(data, structure, f, l, householder, scalar);
        });
    }

    public static void invoke(final double[] data, final int structure, final int first, final int limit, final Householder.Primitive64 householder) {

        double[] hVector = householder.vector;
        int hFirst = householder.first;
        double hBeta = householder.beta;

        double tmpScale;
        for (int j = first; j < limit; j++) {
            tmpScale = DOT.invoke(data, j * structure, hVector, 0, hFirst, structure);
            tmpScale *= hBeta;
            AXPY.invoke(data, j * structure, -tmpScale, hVector, 0, hFirst, structure);
        }
    }

    public static void invoke(final float[] data, final int structure, final int first, final int limit, final Householder.Primitive32 householder) {

        float[] hVector = householder.vector;
        int hFirst = householder.first;
        float hBeta = householder.beta;

        float tmpScale;
        for (int j = first; j < limit; j++) {
            tmpScale = DOT.invoke(data, j * structure, hVector, 0, hFirst, structure);
            tmpScale *= hBeta;
            AXPY.invoke(data, j * structure, -tmpScale, hVector, 0, hFirst, structure);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {

        N[] hVector = householder.vector;
        int hFirst = householder.first;
        N hBeta = householder.beta;

        Scalar<N> tmpScale;
        int tmpIndex;
        for (int j = first; j < limit; j++) {
            tmpScale = scalar.zero();
            tmpIndex = hFirst + j * structure;
            for (int i = hFirst; i < structure; i++) {
                tmpScale = tmpScale.add(hVector[i].conjugate().multiply(data[tmpIndex++]));
            }
            tmpScale = tmpScale.multiply(hBeta);
            tmpIndex = hFirst + j * structure;
            for (int i = hFirst; i < structure; i++) {
                data[tmpIndex] = data[tmpIndex].subtract(tmpScale.multiply(hVector[i])).get();
                tmpIndex++;
            }
        }
    }

    static void divide(final int first, final int limit, final Conquerer conquerer) {
        DIVIDER.parallelism(PARALLELISM).threshold(THRESHOLD).divide(first, limit, conquerer);
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
