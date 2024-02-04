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
package org.ojalgo.array.operation;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

public abstract class SubstituteForwards implements ArrayOperation {

    public static int THRESHOLD = 64;

    /**
     * @param data RHS data that will be overwritten with the solution
     * @param structure The structure (number of rows) in data
     * @param first The first (incl) column/solution to handle
     * @param limit The last (excl) column/solution to handle
     * @param body The equation system body (assumed to be lower/left triangular)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated Assume the body is conjugated/transposed so that the lower/left part is actually
     *        stored in the upper/right part.
     * @param identity Assume the RHS is an identity matrix (disregard the actual elements)
     */
    public static void invoke(final double[] data, final int structure, final int first, final int limit, final Access2D<?> body, final boolean unitDiagonal,
            final boolean conjugated, final boolean identity) {

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        double[] bodyRow = new double[diagDim];
        double tmpVal;
        int colBaseIndex;

        for (int i = 0; i < diagDim; i++) {

            for (int j = 0; j <= i; j++) {
                bodyRow[j] = conjugated ? body.doubleValue(j, i) : body.doubleValue(i, j);
            }

            for (int s = first; s < limit; s++) {
                colBaseIndex = s * structure;

                tmpVal = PrimitiveMath.ZERO;
                for (int j = identity ? s : 0; j < i; j++) {
                    tmpVal += bodyRow[j] * data[j + colBaseIndex];
                }
                if (identity) {
                    tmpVal = i == s ? PrimitiveMath.ONE - tmpVal : -tmpVal;
                } else {
                    tmpVal = data[i + colBaseIndex] - tmpVal;
                }

                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i + colBaseIndex] = tmpVal;
            }
        }
    }

    /**
     * @see #invoke(double[], int, int, int, Access2D, boolean, boolean, boolean)
     */
    public static void invoke(final double[][] data, final Access2D<?> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        int limit = data[0].length;

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        double[] bodyRow = new double[diagDim];
        double tmpVal;

        for (int i = 0; i < diagDim; i++) {

            for (int j = 0; j <= i; j++) {
                bodyRow[j] = conjugated ? body.doubleValue(j, i) : body.doubleValue(i, j);
            }

            for (int s = 0; s < limit; s++) {

                tmpVal = PrimitiveMath.ZERO;
                for (int j = identity ? s : 0; j < i; j++) {
                    tmpVal += bodyRow[j] * data[j][s];
                }
                if (identity) {
                    tmpVal = i == s ? PrimitiveMath.ONE - tmpVal : -tmpVal;
                } else {
                    tmpVal = data[i][s] - tmpVal;
                }

                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i][s] = tmpVal;
            }
        }
    }

    public static void invoke(final float[] data, final int structure, final int first, final int limit, final Access2D<?> body, final boolean unitDiagonal,
            final boolean conjugated, final boolean identity) {

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        float[] bodyRow = new float[diagDim];
        float tmpVal;
        int colBaseIndex;

        for (int i = 0; i < diagDim; i++) {

            for (int j = 0; j <= i; j++) {
                bodyRow[j] = conjugated ? body.floatValue(j, i) : body.floatValue(i, j);
            }

            for (int s = first; s < limit; s++) {
                colBaseIndex = s * structure;

                tmpVal = 0F;
                for (int j = identity ? s : 0; j < i; j++) {
                    tmpVal += bodyRow[j] * data[j + colBaseIndex];
                }
                if (identity) {
                    tmpVal = i == s ? 1F - tmpVal : -tmpVal;
                } else {
                    tmpVal = data[i + colBaseIndex] - tmpVal;
                }

                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i + colBaseIndex] = tmpVal;
            }
        }
    }

    /**
     * @see #invoke(double[], int, int, int, Access2D, boolean, boolean, boolean)
     */
    public static <N extends Scalar<N>> void invoke(final N[] data, final int structure, final int first, final int limit, final Access2D<N> body,
            final boolean unitDiagonal, final boolean conjugated, final boolean identity, final Scalar.Factory<N> scalar) {

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        N[] bodyRow = scalar.newArrayInstance(diagDim);
        Scalar<N> tmpVal;
        int colBaseIndex;

        for (int i = 0; i < diagDim; i++) {

            for (int j = 0; j <= i; j++) {
                bodyRow[j] = conjugated ? body.get(j, i).conjugate().get() : body.get(i, j);
            }

            for (int s = first; s < limit; s++) {
                colBaseIndex = s * structure;

                tmpVal = scalar.zero();
                for (int j = identity ? s : 0; j < i; j++) {
                    tmpVal = tmpVal.add(bodyRow[j].multiply(data[j + colBaseIndex]));
                }
                if (identity) {
                    tmpVal = i == s ? scalar.one().subtract(tmpVal) : tmpVal.negate();
                } else {
                    tmpVal = data[i + colBaseIndex].subtract(tmpVal);
                }

                if (!unitDiagonal) {
                    tmpVal = tmpVal.divide(bodyRow[i]);
                }

                data[i + colBaseIndex] = tmpVal.get();
            }
        }
    }

}
