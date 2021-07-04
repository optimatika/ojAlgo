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
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.concurrent.DivideAndConquer.Conquerer;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.concurrent.ProcessingService;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;

public final class HouseholderRight implements BLAS2 {

    public static IntSupplier PARALLELISM = Parallelism.CORES;
    public static int THRESHOLD = 512;

    private static final DivideAndConquer.Divider DIVIDER = ProcessingService.INSTANCE.divider();

    public static void call(final double[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Primitive64 householder, final double[] work) {
        HouseholderRight.divide(first, limit, (f, l) -> HouseholderRight.invoke(data, structure, f, l, numberOfColumns, householder, work));
    }

    public static void call(final float[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Primitive32 householder, final float[] work) {
        HouseholderRight.divide(first, limit, (f, l) -> HouseholderRight.invoke(data, structure, f, l, numberOfColumns, householder, work));
    }

    public static <N extends Scalar<N>> void call(final N[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {
        HouseholderRight.divide(first, limit, (f, l) -> HouseholderRight.invoke(data, structure, f, l, numberOfColumns, householder, scalar));
    }

    public static void invoke(final double[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Primitive64 householder, final double[] work) {

        double[] hVector = householder.vector;
        int hFirst = householder.first;
        double hBeta = householder.beta;

        for (int j = hFirst; j < numberOfColumns; j++) {
            AXPY.invoke(work, 0, hBeta * hVector[j], data, j * structure, first, limit);
        }
        for (int j = hFirst; j < numberOfColumns; j++) {
            AXPY.invoke(data, j * structure, -hVector[j], work, 0, first, limit);
        }
    }

    public static void invoke(final float[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Primitive32 householder, final float[] work) {

        float[] hVector = householder.vector;
        int hFirst = householder.first;
        float hBeta = householder.beta;

        for (int j = hFirst; j < numberOfColumns; j++) {
            AXPY.invoke(work, 0, hBeta * hVector[j], data, j * structure, first, limit);
        }
        for (int j = hFirst; j < numberOfColumns; j++) {
            AXPY.invoke(data, j * structure, -hVector[j], work, 0, first, limit);
        }
    }

    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Generic<N> householder, final Scalar.Factory<N> scalar) {

        N[] hVector = householder.vector;
        int hFirst = householder.first;
        N hBeta = householder.beta;

        Scalar<N> tmpScale;
        int tmpIndex;
        for (int i = first; i < limit; i++) {
            tmpScale = scalar.zero();
            tmpIndex = i + hFirst * structure;
            for (int j = hFirst; j < numberOfColumns; j++) {
                tmpScale = tmpScale.add(hVector[j].conjugate().multiply(data[tmpIndex].conjugate()));
                tmpIndex += structure;
            }
            tmpScale = tmpScale.multiply(hBeta);
            tmpIndex = i + hFirst * structure;
            for (int j = hFirst; j < numberOfColumns; j++) {
                data[tmpIndex] = data[tmpIndex].conjugate().subtract(tmpScale.multiply(hVector[j])).conjugate().get();
                tmpIndex += structure;
            }
        }
    }

    private static void invoke2old(final double[] data, final int structure, final int first, final int limit, final int numberOfColumns,
            final Householder.Primitive64 householder) {

        double[] hVector = householder.vector;
        int hFirst = householder.first;
        double hBeta = householder.beta;

        double tmpScale;
        int tmpIndex;
        for (int i = first; i < limit; i++) {
            tmpScale = PrimitiveMath.ZERO;
            tmpIndex = i + hFirst * structure;
            for (int j = hFirst; j < numberOfColumns; j++) {
                tmpScale += hVector[j] * data[tmpIndex];
                tmpIndex += structure;
            }
            tmpScale *= hBeta;
            tmpIndex = i + hFirst * structure;
            for (int j = hFirst; j < numberOfColumns; j++) {
                data[tmpIndex] -= tmpScale * hVector[j];
                tmpIndex += structure;
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
