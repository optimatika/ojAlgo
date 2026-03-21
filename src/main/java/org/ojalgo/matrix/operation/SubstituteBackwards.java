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

public abstract class SubstituteBackwards implements MatrixOperation {

    public static int THRESHOLD = 64;

    /**
     * Single-RHS backward substitution operating directly on raw column-major body data. Uses AXPY-style
     * column sweep iterating from the last row to the first: each solved element immediately updates all
     * preceding elements, reading body columns contiguously.
     *
     * @param data          Single-column RHS, overwritten with the solution
     * @param bodyData      Column-major body data (upper triangular, or lower when conjugated)
     * @param bodyStructure Row count (stride between columns) of the body
     * @param unitDiagonal  Assume the body has a unit diagonal
     * @param conjugated    Body is transposed: upper part stored in lower columns
     */
    public static void invoke(final double[] data, final Access2D<?> body, final boolean unitDiagonal, final boolean conjugated, final double[] work) {

        if (conjugated) {

            for (int i = data.length - 1; i >= 0; i--) {

                for (int j = i; j < data.length; j++) {
                    work[j] = body.doubleValue(j, i);
                }

                double tmpVal = data[i] - DOT.invoke(data, work, i + 1, data.length);

                if (!unitDiagonal) {
                    tmpVal /= work[i];
                }

                data[i] = tmpVal;
            }

        } else {

            for (int i = data.length - 1; i >= 0; i--) {

                for (int j = i; j < data.length; j++) {
                    work[j] = body.doubleValue(i, j);
                }

                double tmpVal = data[i] - DOT.invoke(data, work, i + 1, data.length);

                if (!unitDiagonal) {
                    tmpVal /= work[i];
                }

                data[i] = tmpVal;
            }
        }
    }

    public static void invoke(final double[] data, final double[] bodyData, final int bodyStructure, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = n - 1; j >= 0; j--) {

                int count = n - j - 1;
                double solved = data[j] - DOT.invoke(bodyData, j * bodyStructure + (j + 1), 1, data, j + 1, 1, count);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
            }

        } else {

            for (int j = n - 1; j >= 0; j--) {

                int count = n - j - 1;
                double solved = data[j] - DOT.invoke(bodyData, j + (j + 1) * bodyStructure, bodyStructure, data, j + 1, 1, count);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
            }
        }
    }

    /**
     * Single-RHS backward substitution for row-major (double[][]) body data. Each row of the body array
     * corresponds to a matrix row.
     *
     * @param data         Single-column RHS, overwritten with the solution
     * @param bodyRows     Row-major body data (bodyRows[i][j] = body element at row i, col j)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Body is transposed: upper part stored as rows of the lower part
     */
    public static void invoke(final double[] data, final double[][] bodyRows, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = n - 1; j >= 0; j--) {

                if (!unitDiagonal) {
                    data[j] /= bodyRows[j][j];
                }

                double solved = data[j];
                if (solved != PrimitiveMath.ZERO) {
                    AXPY.invoke(data, 0, -solved, bodyRows[j], 0, 0, j);
                }
            }

        } else {

            for (int j = n - 1; j >= 0; j--) {

                double solved = data[j] - DOT.invoke(bodyRows[j], 0, data, 0, j + 1, n);

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
     * @param body         The equation system body (assumed to be upper/right triangular)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Assume the body is conjugated/transposed so that the upper/right part is actually
     *                     stored in the lower/left part.
     * @param hermitian    Assume the solution is hermitian/symmetric
     */
    public static void invoke(final double[] data, final int structure, final int first, final int limit, final Access2D<?> body, final boolean unitDiagonal,
            final boolean conjugated, final boolean hermitian) {

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        double[] bodyRow = new double[diagDim];
        double tmpVal;
        int colBaseIndex;

        int firstRow = hermitian ? first : 0;
        for (int i = diagDim - 1; i >= firstRow; i--) {

            for (int j = i; j < diagDim; j++) {
                bodyRow[j] = conjugated ? body.doubleValue(j, i) : body.doubleValue(i, j);
            }

            int columnLimit = hermitian ? Math.min(i + 1, limit) : limit;
            for (int s = first; s < columnLimit; s++) {
                colBaseIndex = s * structure;

                tmpVal = PrimitiveMath.ZERO;
                for (int j = i + 1; j < diagDim; j++) {
                    tmpVal += bodyRow[j] * data[j + colBaseIndex];
                }
                tmpVal = data[i + colBaseIndex] - tmpVal;
                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i + colBaseIndex] = tmpVal;
            }
        }
    }

    /**
     * Single-RHS backward substitution with a unit-diagonal lower triangular CSR body, solving L<sup>T</sup>x
     * = b. Iterates rows bottom-up, scattering updates from each solved element to preceding elements.
     * <p>
     * Matches SparseLU's L factor btran: L<sup>T</sup> is upper triangular, with the off-diagonal part stored
     * as the rows of L and a unit diagonal (not stored).
     *
     * @param arg  RHS overwritten with the solution
     * @param body CSR representation of the strictly lower triangular entries
     */
    public static void invoke(final double[] arg, final R064CSR body) {
        int[] rowPointers = body.pointers;
        int[] colIndices = body.indices;
        double[] values = body.values;
        for (int i = body.getMinDim() - 1; i > 0; i--) {
            double solved = arg[i];
            for (int k = rowPointers[i], limit = rowPointers[i + 1]; k < limit; k++) {
                arg[colIndices[k]] -= solved * values[k];
            }
        }
    }

    /**
     * Single-RHS backward substitution with a non-unit-diagonal upper triangular CSR body + separate
     * diagonal. Iterates rows bottom-up, subtracting the sparse dot product of each row with the current
     * solution and dividing by the diagonal.
     * <p>
     * Matches SparseLU's U factor ftran.
     *
     * @param arg      RHS overwritten with the solution
     * @param body     CSR representation of the strictly upper triangular entries
     * @param diagonal Diagonal elements
     */
    public static void invoke(final double[] arg, final R064CSR body, final double[] diagonal) {
        int[] rowPointers = body.pointers;
        int[] colIndices = body.indices;
        double[] values = body.values;
        for (int i = body.getMinDim() - 1; i >= 0; i--) {
            double sum = arg[i];
            for (int k = rowPointers[i], limit = rowPointers[i + 1]; k < limit; k++) {
                sum -= values[k] * arg[colIndices[k]];
            }
            arg[i] = sum / diagonal[i];
        }
    }

    /**
     * @see #invoke(double[], int, int, int, Access2D, boolean, boolean, boolean)
     */
    public static void invoke(final double[][] data, final Access2D<?> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        int limit = data[0].length;

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        double[] bodyRow = new double[diagDim];
        double tmpVal;

        for (int i = diagDim - 1; i >= 0; i--) {

            for (int j = i; j < diagDim; j++) {
                bodyRow[j] = conjugated ? body.doubleValue(j, i) : body.doubleValue(i, j);
            }

            int columnLimit = hermitian ? Math.min(i + 1, limit) : limit;
            for (int s = 0; s < columnLimit; s++) {

                tmpVal = PrimitiveMath.ZERO;
                for (int j = i + 1; j < diagDim; j++) {
                    tmpVal += bodyRow[j] * data[j][s];
                }
                tmpVal = data[i][s] - tmpVal;
                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i][s] = tmpVal;
            }
        }
    }

    /**
     * Single-RHS backward substitution for column-major float body data.
     *
     * @see #invoke(double[], double[], int, boolean, boolean)
     */
    public static void invoke(final float[] data, final float[] bodyData, final int bodyStructure, final boolean unitDiagonal, final boolean conjugated) {

        int n = data.length;

        if (conjugated) {

            for (int j = n - 1; j >= 0; j--) {

                if (!unitDiagonal) {
                    data[j] /= bodyData[j + j * bodyStructure];
                }

                float solved = data[j];
                if (solved != 0F) {
                    AXPY.invoke(data, 0, -solved, bodyData, j * bodyStructure, 0, j);
                }
            }

        } else {

            for (int j = n - 1; j >= 0; j--) {

                int count = n - j - 1;
                float solved = data[j] - DOT.invoke(bodyData, j + (j + 1) * bodyStructure, bodyStructure, data, j + 1, 1, count);

                if (!unitDiagonal) {
                    solved /= bodyData[j + j * bodyStructure];
                }

                data[j] = solved;
            }
        }
    }

    public static void invoke(final float[] data, final int structure, final int first, final int limit, final Access2D<?> body, final boolean unitDiagonal,
            final boolean conjugated, final boolean hermitian) {

        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns());
        float[] bodyRow = new float[diagDim];
        float tmpVal;
        int colBaseIndex;

        int firstRow = hermitian ? first : 0;
        for (int i = diagDim - 1; i >= firstRow; i--) {

            for (int j = i; j < diagDim; j++) {
                bodyRow[j] = conjugated ? body.floatValue(j, i) : body.floatValue(i, j);
            }

            int columnLimit = hermitian ? Math.min(i + 1, limit) : limit;
            for (int s = first; s < columnLimit; s++) {
                colBaseIndex = s * structure;

                tmpVal = 0F;
                for (int j = i + 1; j < diagDim; j++) {
                    tmpVal += bodyRow[j] * data[j + colBaseIndex];
                }
                tmpVal = data[i + colBaseIndex] - tmpVal;
                if (!unitDiagonal) {
                    tmpVal /= bodyRow[i];
                }

                data[i + colBaseIndex] = tmpVal;
            }
        }
    }

    /**
     * Single-RHS backward substitution for {@link Scalar}-typed data. Handles complex and other generic
     * number types correctly via conjugate/multiply/divide on the scalar type.
     *
     * @param data         Single-column RHS (length n), overwritten with the solution
     * @param body         Upper triangular body (or lower when conjugated)
     * @param unitDiagonal Assume the body has a unit diagonal
     * @param conjugated   Body is conjugate-transposed
     * @param scalar       Factory for the scalar type
     */
    public static <N extends Scalar<N>> void invoke(final N[] data, final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated,
            final Scalar.Factory<N> scalar) {

        int n = data.length;
        int diagDim = MissingMath.toMinIntExact(body.countRows(), body.countColumns(), n);
        N[] bodyRow = scalar.newArrayInstance(diagDim);

        for (int i = diagDim - 1; i >= 0; i--) {

            for (int j = i; j < diagDim; j++) {
                bodyRow[j] = conjugated ? body.get(j, i).conjugate().get() : body.get(i, j);
            }

            Scalar<N> tmpVal = scalar.zero();
            for (int j = i + 1; j < diagDim; j++) {
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
            final boolean unitDiagonal, final boolean conjugated, final boolean hermitian, final Scalar.Factory<N> scalar) {

        int diagDim = (int) Math.min(body.countRows(), body.countColumns());
        N[] bodyRow = scalar.newArrayInstance(diagDim);
        Scalar<N> tmpVal;
        int colBaseIndex;

        int firstRow = hermitian ? first : 0;
        for (int i = diagDim - 1; i >= firstRow; i--) {

            for (int j = i; j < diagDim; j++) {
                bodyRow[j] = conjugated ? body.get(j, i).conjugate().get() : body.get(i, j);
            }

            int columnLimit = hermitian ? Math.min(i + 1, limit) : limit;
            for (int s = first; s < columnLimit; s++) {

                colBaseIndex = s * structure;

                tmpVal = scalar.zero();
                for (int j = i + 1; j < diagDim; j++) {
                    tmpVal = tmpVal.add(bodyRow[j].multiply(data[j + colBaseIndex]));
                }
                tmpVal = data[i + colBaseIndex].subtract(tmpVal);
                if (!unitDiagonal) {
                    tmpVal = tmpVal.divide(bodyRow[i]);
                }

                data[i + colBaseIndex] = tmpVal.get();
            }
        }
    }

}
