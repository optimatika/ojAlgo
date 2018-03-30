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
import org.ojalgo.array.blas.DOT;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;

public final class HouseholderLeft extends MatrixOperation {

    public static final HouseholderLeft SETUP = new HouseholderLeft();

    public static int THRESHOLD = 128;

    public static void invoke(final BigDecimal[] data, final int structure, final int first, final int limit, final Householder.Big householder) {

        final BigDecimal[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final BigDecimal tmpBeta = householder.beta;

        BigDecimal tmpScale;
        int tmpIndex;
        for (int j = first; j < limit; j++) {
            tmpScale = BigMath.ZERO;
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                tmpScale = BigFunction.ADD.invoke(tmpScale, BigFunction.MULTIPLY.invoke(tmpHouseholderVector[i], data[tmpIndex++]));
            }
            tmpScale = BigFunction.MULTIPLY.invoke(tmpScale, tmpBeta);
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                data[tmpIndex] = BigFunction.SUBTRACT.invoke(data[tmpIndex], BigFunction.MULTIPLY.invoke(tmpScale, tmpHouseholderVector[i]));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final ComplexNumber[] data, final int structure, final int first, final int limit, final Householder.Complex householder) {

        final ComplexNumber[] tmpHouseholderVector = householder.vector;
        final int tmpFirstNonZero = householder.first;
        final ComplexNumber tmpBeta = householder.beta;

        ComplexNumber tmpScale;
        int tmpIndex;
        for (int j = first; j < limit; j++) {
            tmpScale = ComplexNumber.ZERO;
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                tmpScale = tmpScale.add(tmpHouseholderVector[i].conjugate().multiply(data[tmpIndex++]));
            }
            tmpScale = tmpScale.multiply(tmpBeta);
            tmpIndex = tmpFirstNonZero + (j * structure);
            for (int i = tmpFirstNonZero; i < structure; i++) {
                data[tmpIndex] = data[tmpIndex].subtract(tmpScale.multiply(tmpHouseholderVector[i]));
                tmpIndex++;
            }
        }
    }

    public static void invoke(final double[] data, final int structure, final int first, final int limit, final Householder.Primitive householder) {

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

    public static <N extends Number & Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit,
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

    private HouseholderLeft() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
