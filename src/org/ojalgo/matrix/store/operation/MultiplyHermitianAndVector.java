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

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;

import org.ojalgo.constant.BigMath;
import org.ojalgo.scalar.Scalar;

/**
 * Multiplies an hermitian (square symmetric) matrix with a vector. Will only read from the lower/left
 * triangular part of the matrix, and will only calculate the lower/left part of the results.
 *
 * @author apete
 */
public final class MultiplyHermitianAndVector extends MatrixOperation {

    public static final MultiplyHermitianAndVector SETUP = new MultiplyHermitianAndVector();

    public static int THRESHOLD = 256;

    public static void invoke(final BigDecimal[] productMatrix, final int firstRow, final int rowLimit, final BigDecimal[] hermitianMatrix,
            final BigDecimal[] rightVector, final int firstColumn) {

        final int structure = rightVector.length;

        BigDecimal tmpVal;
        for (int i = firstRow; i < rowLimit; i++) {
            tmpVal = BigMath.ZERO;
            for (int c = firstColumn; c < i; c++) {
                tmpVal = tmpVal.add(hermitianMatrix[i + (c * structure)].multiply(rightVector[c]));
            }
            for (int c = i; c < structure; c++) {
                tmpVal = tmpVal.add(hermitianMatrix[c + (i * structure)].multiply(rightVector[c]));
            }
            productMatrix[i] = tmpVal;
        }
    }

    public static void invoke(final double[] productMatrix, final int firstRow, final int rowLimit, final double[] hermitianMatrix, final double[] rightVector,
            final int firstColumn) {

        final int structure = rightVector.length;

        double tmpVal;
        for (int i = firstRow; i < rowLimit; i++) {
            tmpVal = ZERO;
            for (int c = firstColumn; c < i; c++) {
                tmpVal += hermitianMatrix[i + (c * structure)] * rightVector[c];
            }
            for (int c = i; c < structure; c++) {
                tmpVal += hermitianMatrix[c + (i * structure)] * rightVector[c];
            }
            productMatrix[i] = tmpVal;
        }
    }

    public static <N extends Number & Scalar<N>> void invoke(final N[] productMatrix, final int firstRow, final int rowLimit, final N[] hermitianMatrix,
            final N[] rightVector, final int firstColumn, final Scalar.Factory<N> scalar) {

        final int structure = rightVector.length;

        Scalar<N> tmpVal;
        for (int i = firstRow; i < rowLimit; i++) {
            tmpVal = scalar.zero();
            for (int c = firstColumn; c < i; c++) {
                tmpVal = tmpVal.add(hermitianMatrix[i + (c * structure)].multiply(rightVector[c]));
            }
            for (int c = i; c < structure; c++) {
                tmpVal = tmpVal.add(hermitianMatrix[c + (i * structure)].conjugate().multiply(rightVector[c]));
            }
            productMatrix[i] = tmpVal.get();
        }
    }

    private MultiplyHermitianAndVector() {
        super();
    }

    @Override
    public int threshold() {
        return THRESHOLD;
    }

}
