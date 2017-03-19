/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Performs Householder transformation from both sides simultaneously assuming that [A] is hermitian (square
 * symmetric) [A] = [A]<sup>H</sup>. Will only read from and write to the lower/left triangular part of [A].
 *
 * @author apete
 */
public final class HouseholderHermitian extends MatrixOperation {

    public static final HouseholderHermitian SETUP = new HouseholderHermitian();

    public static void invoke(final BigDecimal[] data, final Householder.Big householder, final BigDecimal[] worker) {

        final BigDecimal[] tmpVector = householder.vector;
        final int tmpFirst = householder.first;
        final int tmpLength = tmpVector.length;
        final BigDecimal tmpBeta = householder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > MultiplyHermitianAndVector.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    MultiplyHermitianAndVector.invoke(worker, first, limit, data, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, MultiplyHermitianAndVector.THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(worker, tmpFirst, tmpLength, data, tmpVector, tmpFirst);
        }

        BigDecimal tmpVal = BigMath.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            //tmpVal += tmpVector[c] * worker[c];
            tmpVal = tmpVal.add(tmpVector[c].multiply(worker[c]));
        }
        //tmpVal *= (tmpBeta / TWO);
        tmpVal = BigFunction.DIVIDE.invoke(tmpVal.multiply(tmpBeta), BigMath.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            //worker[c] = tmpBeta * (worker[c] - (tmpVal * tmpVector[c]));
            worker[c] = tmpBeta.multiply(worker[c].subtract(tmpVal.multiply(tmpVector[c])));
        }

        if (tmpCount > HermitianRank2Update.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    HermitianRank2Update.invoke(data, first, limit, tmpVector, worker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, HermitianRank2Update.THRESHOLD);

        } else {

            HermitianRank2Update.invoke(data, tmpFirst, tmpLength, tmpVector, worker);
        }
    }

    public static void invoke(final ComplexNumber[] data, final Householder.Complex householder, final ComplexNumber[] worker) {

        final ComplexNumber[] tmpVector = householder.vector;
        final int tmpFirst = householder.first;
        final int tmpLength = tmpVector.length;
        final ComplexNumber tmpBeta = householder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > MultiplyHermitianAndVector.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    MultiplyHermitianAndVector.invoke(worker, first, limit, data, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, MultiplyHermitianAndVector.THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(worker, tmpFirst, tmpLength, data, tmpVector, tmpFirst);
        }

        ComplexNumber tmpVal = ComplexNumber.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            //tmpVal += tmpVector[c] * worker[c];
            tmpVal = tmpVal.add(tmpVector[c].conjugate().multiply(worker[c]));
        }
        //tmpVal *= (tmpBeta / TWO);
        tmpVal = ComplexFunction.DIVIDE.invoke(tmpVal.multiply(tmpBeta), ComplexNumber.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            //worker[c] = tmpBeta * (worker[c] - (tmpVal * tmpVector[c]));
            worker[c] = tmpBeta.multiply(worker[c].subtract(tmpVal.multiply(tmpVector[c])));
        }

        if (tmpCount > HermitianRank2Update.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    HermitianRank2Update.invoke(data, first, limit, tmpVector, worker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, HermitianRank2Update.THRESHOLD);

        } else {

            HermitianRank2Update.invoke(data, tmpFirst, tmpLength, tmpVector, worker);
        }
    }

    public static void invoke(final double[] data, final Householder.Primitive householder, final double[] worker) {

        final double[] tmpVector = householder.vector;
        final int tmpFirst = householder.first;
        final int tmpLength = tmpVector.length;
        final double tmpBeta = householder.beta;
        final int tmpCount = tmpLength - tmpFirst;

        if (tmpCount > MultiplyHermitianAndVector.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    MultiplyHermitianAndVector.invoke(worker, first, limit, data, tmpVector, tmpFirst);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, MultiplyHermitianAndVector.THRESHOLD);

        } else {

            MultiplyHermitianAndVector.invoke(worker, tmpFirst, tmpLength, data, tmpVector, tmpFirst);
        }

        double tmpVal = PrimitiveMath.ZERO;
        for (int c = tmpFirst; c < tmpLength; c++) {
            tmpVal += tmpVector[c] * worker[c];
        }
        tmpVal *= (tmpBeta / PrimitiveMath.TWO);
        for (int c = tmpFirst; c < tmpLength; c++) {
            worker[c] = tmpBeta * (worker[c] - (tmpVal * tmpVector[c]));
        }

        if (tmpCount > HermitianRank2Update.THRESHOLD) {

            final DivideAndConquer tmpConqurer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    HermitianRank2Update.invoke(data, first, limit, tmpVector, worker);
                }
            };

            tmpConqurer.invoke(tmpFirst, tmpLength, HermitianRank2Update.THRESHOLD);

        } else {

            HermitianRank2Update.invoke(data, tmpFirst, tmpLength, tmpVector, worker);
        }
    }

    private HouseholderHermitian() {
        super();
    }

    @Override
    public int threshold() {
        return Math.min(MultiplyHermitianAndVector.THRESHOLD, HermitianRank2Update.THRESHOLD);
    }

}
