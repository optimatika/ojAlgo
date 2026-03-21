/*
 * Copyright 1997-2025 Optimatika
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

import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.DOT;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.R064CSR;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access2D;

public abstract class SubstituteForwards implements MatrixOperation {

    public static int THRESHOLD = 64;

    public static void invoke(final double[] data, final Access2D<?> body, final boolean unitDiagonal, final boolean conjugated, final double[] work) {

        double tmpVal;

        if (conjugated) {

            for (int i = 0, limit = data.length; i < limit; i++) {

                for (int j = 0; j <= i; j++) {
                    work[j] = body.doubleValue(j, i);
                }

                tmpVal = data[i] - DOT.invoke(data, work, 0, i);

                if (!unitDiagonal) {
                    tmpVal /= work[i];
                }

                data[i] = tmpVal;
            }

        } else {

            for (int i = 0, limit = data.length; i < limit; i++) {

                for (int j = 0; j <= i; j++) {
                    work[j] = body.doubleValue(i, j);
                }

                tmpVal = data[i] - DOT.invoke(data, work, 0, i);

                if (!unitDiagonal) {
                    tmpVal /= work[i];
                }

                data[i] = tmpVal;
            }
        }
    }

    /**
     * Single-RHS forward substitution operating directly on raw column-major body data. Uses AXPY-style
     * column sweep: each solved element immediately updates all subsequent elements, reading body columns
     * contiguously for optimal cache locality.
     *
     * @param data          Single-column RHS, overwritten with the solution
     * @param bodyData      Column-major body data (lower triangular, or upper when conjugated)
     * @param bodyStructure Row count (stride between columns) of the body
     * @param unitDiagonal  Assume the body has a unit diagonal
     * @param conjugated    Body is transposed: lower part stored in upper columns
     */
    public static void invoke(final double[] data, final double[] bodyData, final int bodyStructure, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = 0; j < n; j++) {

                double solved = data[j] - DOT.invoke(bodyData, j * bodyStructure, 1, data, 0, 1, j);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
            }

        } else {

            for (int j = 0; j < n; j++) {

                double solved = data[j] - DOT.invoke(bodyData, j, bodyStructure, data, 0, 1, j);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
            }
        }
    }

    /**
     * Single-RHS forward substitution for row-major (double[][]) body data. Each row of the body array
     * corresponds to a matrix row.
     *
     * @param data         Single-column RHS, overwritten with the solution
     * @param bodyRows     Row-major body data (bodyRows[i][j] = body element at row i, col j)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Body is transposed: lower part stored as rows of the upper part
     */
    public static void invoke(final double[] data, final double[][] bodyRows, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = 0; j < n; j++) {

                if (!unitDiagonal) {
                    data[j] /= bodyRows[j][j];
                }

                double solved = data[j];
                if (solved != PrimitiveMath.ZERO) {
                    AXPY.invoke(data, 0, -solved, bodyRows[j], 0, j + 1, n);
                }
            }

        } else {

            for (int j = 0; j < n; j++) {

                double solved = data[j] - DOT.invoke(bodyRows[j], 0, data, 0, 0, j);

                if (!unitDiagonal) {
                    solved /= bodyRows[j][j];
                }

                data[j] = solved;
            }
        }
    }

    /**
     * @param data         RHS data that will be overwritten with the solution
     * @param structure    The structure (number of rows) in data
     * @param first        The first (incl) column/solution to handle
     * @param limit        The last (excl) column/solution to handle
     * @param body         The equation system body (assumed to be lower/left triangular)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Assume the body is conjugated/transposed so that the lower/left part is actually
     *                     stored in the upper/right part.
     * @param identity     Assume the RHS is an identity matrix (disregard the actual elements)
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
     * Single-RHS forward substitution with a unit-diagonal lower triangular CSR body. Iterates rows top-down,
     * subtracting the sparse dot product of each row with the current solution.
     * <p>
     * Matches SparseLU's L factor ftran: L is unit lower triangular with only the strictly lower part stored.
     *
     * @param arg  RHS overwritten with the solution
     * @param body CSR representation of the strictly lower triangular entries
     */
    public static void invoke(final double[] arg, final R064CSR body) {
        int[] rowPointers = body.pointers;
        int[] colIndices = body.indices;
        double[] values = body.values;
        for (int i = 1, dim = body.getMinDim(); i < dim; i++) {
            double sum = 0.0;
            for (int k = rowPointers[i], limit = rowPointers[i + 1]; k < limit; k++) {
                sum += values[k] * arg[colIndices[k]];
            }
            arg[i] -= sum;
        }
    }

    /**
     * Single-RHS forward substitution with a non-unit-diagonal upper triangular body stored as CSR + separate
     * diagonal. This solves U<sup>T</sup>x = b by iterating rows top-down: divide by the diagonal, then
     * scatter updates to subsequent elements.
     * <p>
     * Matches SparseLU's U factor btran: U<sup>T</sup> is lower triangular, with the off-diagonal part stored
     * as the rows of U and the diagonal in a separate array.
     *
     * @param arg      RHS overwritten with the solution
     * @param body     CSR representation of the strictly upper triangular entries
     * @param diagonal Diagonal elements
     */
    public static void invoke(final double[] arg, final R064CSR body, final double[] diagonal) {
        int[] rowPointers = body.pointers;
        int[] colIndices = body.indices;
        double[] values = body.values;
        for (int i = 0, dim = body.getMinDim(); i < dim; i++) {
            double solved = arg[i] / diagonal[i];
            arg[i] = solved;
            for (int k = rowPointers[i], limit = rowPointers[i + 1]; k < limit; k++) {
                arg[colIndices[k]] -= solved * values[k];
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

    /**
     * Single-RHS forward substitution for column-major float body data.
     *
     * @see #invoke(double[], double[], int, boolean, boolean)
     */
    public static void invoke(final float[] data, final float[] bodyData, final int bodyStructure, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = 0; j < n; j++) {

                if (!unitDiagonal) {
                    data[j] /= bodyData[j + j * bodyStructure];
                }

                float solved = data[j];
                if (solved != 0F) {
                    AXPY.invoke(data, 0, -solved, bodyData, j * bodyStructure, j + 1, n);
                }
            }

        } else {

            for (int j = 0; j < n; j++) {

                float solved = data[j] - DOT.invoke(bodyData, j, bodyStructure, data, 0, 1, j);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
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
     * Single-RHS forward substitution for {@link Scalar}-typed data. Handles complex and other generic number
     * types correctly via conjugate/multiply/divide on the scalar type.
     *
     * @param data         Single-column RHS (length n), overwritten with the solution
     * @param body         Lower triangular body (or upper when conjugated)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Body is conjugate-transposed
     * @param scalar       Factory for the scalar type
     */
    public static <N extends Scalar<N>> void invoke(final N[] data, final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated,
            final Scalar.Factory<N> scalar) {

        int n = data.length;
        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns(), n);
        N[] bodyRow = scalar.newArrayInstance(diagDim);

        for (int i = 0; i < diagDim; i++) {

            for (int j = 0; j <= i; j++) {
                bodyRow[j] = conjugated ? body.get(j, i).conjugate().get() : body.get(i, j);
            }

            Scalar<N> tmpVal = scalar.zero();
            for (int j = 0; j < i; j++) {
                tmpVal = tmpVal.add(bodyRow[j].multiply(data[j]));
            }
            tmpVal = data[i].subtract(tmpVal);

            if (!unitDiagonal) {
                tmpVal = tmpVal.divide(bodyRow[i]);
            }

            data[i] = tmpVal.get();
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
