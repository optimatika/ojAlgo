/*
 * Copyright 1997-2026 Optimatika
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

import static java.lang.foreign.ValueLayout.*;
import static org.ojalgo.function.constant.PrimitiveMath.ONE;
import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.operation.AXPY;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;

/**
 * BLAS level 3 {@code ?symm}: C := alpha*A*B + beta*C or C := alpha*B*A + beta*C, where A is symmetric and B
 * and C are m-by-n.
 *
 * @see NativeLibrary
 */
public abstract class SYMM implements MatrixOperation {

    /**
     * Native {@code cblas_dsymm}, initialised only when {@link NativeLibrary#isAvailable()}. Linked as a
     * critical function so the Java arrays are used in place, without copying.
     */
    static final class Native {

        static final MethodHandle DSYMM = NativeLibrary.downcall("cblas_dsymm", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                JAVA_DOUBLE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT), true);

        static void invoke(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            int dimA = left ? m : n;

            MemorySegment segmentA = CBLAS.ofMatrix(a, offsetA, dimA, dimA, lda);
            MemorySegment segmentB = CBLAS.ofMatrix(b, offsetB, m, n, ldb);
            MemorySegment segmentC = CBLAS.ofMatrix(c, offsetC, m, n, ldc);

            try {
                DSYMM.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.side(left), CBLAS.triangle(upper), m, n, alpha, segmentA, lda, segmentB, ldb, beta, segmentC, ldc);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * Matrix size (m and n) from which the native implementation is used, if there is one.
     */
    public static int NATIVE_THRESHOLD = 16;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DSYMM != null;

    /**
     * {@code dsymm}: C := alpha*A*B + beta*C (left) or C := alpha*B*A + beta*C (right)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of A is read. When beta is 0, C need not be set on entry; when alpha is 0, A
     * and B are not read.
     *
     * @param left  A is on the left (m-by-m) if true, otherwise on the right (n-by-n)
     * @param upper The upper triangle of A is used if true, otherwise the lower
     * @param m     The number of rows of B and C
     * @param n     The number of columns of B and C
     */
    public static void invoke(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("SYMM", "m", m >= 0);
        ArrayOperation.checkArgument("SYMM", "n", n >= 0);
        ArrayOperation.checkArgument("SYMM", "lda", lda >= Math.max(1, left ? m : n));
        ArrayOperation.checkArgument("SYMM", "ldb", ldb >= Math.max(1, m));
        ArrayOperation.checkArgument("SYMM", "ldc", ldc >= Math.max(1, m));

        if (m == 0 || n == 0 || alpha == ZERO && beta == ONE) {
            return;
        }

        if (NATIVE && m >= NATIVE_THRESHOLD && n >= NATIVE_THRESHOLD) {
            Native.invoke(left, upper, m, n, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        } else {
            SYMM.invokeJava(left, upper, m, n, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        }
    }

    /**
     * The pure Java implementation of {@link #invoke}, without argument checks.
     */
    static void invokeJava(final boolean left, final boolean upper, final int m, final int n, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        if (alpha == ZERO) {
            for (int j = 0; j < n; j++) {
                int colC = offsetC + j * ldc;
                SCAL.scale(c, colC, colC + m, beta);
            }
            return;
        }

        if (left) {

            if (upper) {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    int colC = offsetC + j * ldc;
                    for (int i = 0; i < m; i++) {
                        int colA = offsetA + i * lda;
                        double temp1 = alpha * b[colB + i];
                        double temp2 = ZERO;
                        for (int k = 0; k < i; k++) {
                            c[colC + k] += temp1 * a[colA + k];
                            temp2 += b[colB + k] * a[colA + k];
                        }
                        double scaled = beta == ZERO ? ZERO : beta * c[colC + i];
                        c[colC + i] = scaled + temp1 * a[colA + i] + alpha * temp2;
                    }
                }
            } else {
                for (int j = 0; j < n; j++) {
                    int colB = offsetB + j * ldb;
                    int colC = offsetC + j * ldc;
                    for (int i = m - 1; i >= 0; i--) {
                        int colA = offsetA + i * lda;
                        double temp1 = alpha * b[colB + i];
                        double temp2 = ZERO;
                        for (int k = i + 1; k < m; k++) {
                            c[colC + k] += temp1 * a[colA + k];
                            temp2 += b[colB + k] * a[colA + k];
                        }
                        double scaled = beta == ZERO ? ZERO : beta * c[colC + i];
                        c[colC + i] = scaled + temp1 * a[colA + i] + alpha * temp2;
                    }
                }
            }

        } else {

            for (int j = 0; j < n; j++) {
                int colC = offsetC + j * ldc;
                SCAL.scale(c, colC, colC + m, beta);
                AXPY.invoke(c, colC, alpha * a[offsetA + j + j * lda], b, offsetB + j * ldb, 0, m);
                for (int k = 0; k < j; k++) {
                    double element = upper ? a[offsetA + k + j * lda] : a[offsetA + j + k * lda];
                    AXPY.invoke(c, colC, alpha * element, b, offsetB + k * ldb, 0, m);
                }
                for (int k = j + 1; k < n; k++) {
                    double element = upper ? a[offsetA + j + k * lda] : a[offsetA + k + j * lda];
                    AXPY.invoke(c, colC, alpha * element, b, offsetB + k * ldb, 0, m);
                }
            }
        }
    }

}
