/*
 * Copyright 1997-2024 Optimatika
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

public final class HouseholderLeft implements MatrixOperation {

    public static IntSupplier PARALLELISM = Parallelism.THREADS;
    public static int THRESHOLD = 128;

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.divider();

    public static void call(final double[] data, final int structure, final int first, final double[] hVector, final int hFirst, final double hBeta) {

        int nbCols = data.length / structure;

        if (nbCols > THRESHOLD) {
            HouseholderLeft.divide(first, nbCols, (f, l) -> HouseholderLeft.invoke(data, structure, f, l, hVector, hFirst, hBeta));
        } else {
            HouseholderLeft.invoke(data, structure, first, nbCols, hVector, hFirst, hBeta);
        }
    }

    public static void call(final double[] data, final int structure, final int first, final Householder.Primitive64 householder) {
        HouseholderLeft.call(data, structure, first, householder.vector, householder.first, householder.beta);
    }

    public static void call(final double[][] data, final int structure, final int first, final double[] hVector, final int hFirst, final double hBeta) {

        int nbCols = data.length;

        if (nbCols > THRESHOLD) {
            HouseholderLeft.divide(first, nbCols, (f, l) -> HouseholderLeft.invoke(data, structure, f, l, hVector, hFirst, hBeta));
        } else {
            HouseholderLeft.invoke(data, structure, first, nbCols, hVector, hFirst, hBeta);
        }
    }

    public static void call(final double[][] data, final int structure, final int first, final Householder.Primitive64 householder) {
        HouseholderLeft.call(data, structure, first, householder.vector, householder.first, householder.beta);
    }

    public static void call(final float[] data, final int structure, final int first, final Householder.Primitive32 householder) {

        int nbCols = data.length / structure;

        if (nbCols > THRESHOLD) {
            HouseholderLeft.divide(first, nbCols, (f, l) -> HouseholderLeft.invoke(data, structure, f, l, householder));
        } else {
            HouseholderLeft.invoke(data, structure, first, nbCols, householder);
        }
    }

    public static <N extends Scalar<N>> void call(final N[] data, final int structure, final int first, final Householder.Generic<N> householder,
            final Scalar.Factory<N> scalar) {

        int nbCols = data.length / structure;

        if (nbCols > THRESHOLD) {
            HouseholderLeft.divide(first, nbCols, (f, l) -> HouseholderLeft.invoke(data, structure, f, l, householder, scalar));
        } else {
            HouseholderLeft.invoke(data, structure, first, nbCols, householder, scalar);
        }
    }

    private static void doColumn(final double[] data, final int offset, final double[] vector, final double beta, final int first, final int limit) {
        double scale = beta * DOT.invoke(data, offset, vector, 0, first, limit);
        AXPY.invoke(data, offset, -scale, vector, 0, first, limit);
    }

    private static void doColumn(final float[] data, final int offset, final float[] vector, final float beta, final int first, final int limit) {
        float scale = beta * DOT.invoke(data, offset, vector, 0, first, limit);
        AXPY.invoke(data, offset, -scale, vector, 0, first, limit);
    }

    static void divide(final int first, final int limit, final Conquerer conquerer) {
        DIVIDER.parallelism(PARALLELISM).threshold(THRESHOLD).divide(first, limit, conquerer);
    }

    static void invoke(final double[] data, final int structure, final int first, final int limit, final double[] hVector, final int hFirst,
            final double hBeta) {
        for (int j = first; j < limit; j++) {
            HouseholderLeft.doColumn(data, j * structure, hVector, hBeta, hFirst, structure);
        }
    }

    static void invoke(final double[][] data, final int structure, final int first, final int limit, final double[] hVector, final int hFirst,
            final double hBeta) {
        for (int j = first; j < limit; j++) {
            HouseholderLeft.doColumn(data[j], 0, hVector, hBeta, hFirst, structure);
        }
    }

    static void invoke(final float[] data, final int structure, final int first, final int limit, final Householder.Primitive32 householder) {

        float[] hVector = householder.vector;
        int hFirst = householder.first;
        float hBeta = householder.beta;

        for (int j = first; j < limit; j++) {
            HouseholderLeft.doColumn(data, j * structure, hVector, hBeta, hFirst, structure);
        }
    }

    static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit, final Householder.Generic<N> householder,
            final Scalar.Factory<N> scalar) {

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

    private HouseholderLeft() {
        super();
    }

}
