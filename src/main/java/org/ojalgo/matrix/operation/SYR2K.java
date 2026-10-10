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
import org.ojalgo.array.operation.DOT;
import org.ojalgo.array.operation.NativeLibrary;
import org.ojalgo.array.operation.SCAL;

/**
 * BLAS level 3 {@code ?syr2k}: C := alpha*A*B<sup>T</sup> + alpha*B*A<sup>T</sup> + beta*C or C :=
 * alpha*A<sup>T</sup>*B + alpha*B<sup>T</sup>*A + beta*C, where C is symmetric n-by-n and only its upper or
 * lower triangle is updated.
 *
 * @see NativeLibrary
 */
public abstract class SYR2K implements MatrixOperation {

    /**
     * Native {@code cblas_dsyr2k}, initialised only when {@link NativeLibrary#isAvailable()}. Linked as a
     * critical function so the Java arrays are used in place, without copying.
     */
    static final class Native {

        static final MethodHandle DSYR2K = NativeLibrary.downcall("cblas_dsyr2k", FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT,
                JAVA_DOUBLE, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, JAVA_DOUBLE, ADDRESS, JAVA_INT), true);

        static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
                final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

            MemorySegment segmentA = transpose ? CBLAS.ofMatrix(a, offsetA, k, n, lda) : CBLAS.ofMatrix(a, offsetA, n, k, lda);
            MemorySegment segmentB = transpose ? CBLAS.ofMatrix(b, offsetB, k, n, ldb) : CBLAS.ofMatrix(b, offsetB, n, k, ldb);
            MemorySegment segmentC = CBLAS.ofMatrix(c, offsetC, n, n, ldc);

            try {
                DSYR2K.invokeExact(CBLAS.COLUMN_MAJOR, CBLAS.triangle(upper), CBLAS.transpose(transpose), n, k, alpha, segmentA, lda, segmentB, ldb, beta,
                        segmentC, ldc);
            } catch (Throwable cause) {
                throw new ProgrammingError(cause);
            }
        }

    }

    /**
     * Matrix size (n and k) from which the native implementation is used, if there is one.
     */
    public static int NATIVE_THRESHOLD = 16;

    static final boolean NATIVE = NativeLibrary.isAvailable() && Native.DSYR2K != null;

    /**
     * {@code dsyr2k}: C := alpha*A*B<sup>T</sup> + alpha*B*A<sup>T</sup> + beta*C (no transpose) or C :=
     * alpha*A<sup>T</sup>*B + alpha*B<sup>T</sup>*A + beta*C (transpose)
     * <p>
     * All matrices are column-major, stored in their arrays from an offset with a leading dimension. Only the
     * upper or lower triangle of C is read and updated. When beta is 0, C need not be set on entry; when
     * alpha is 0, A and B are not read.
     *
     * @param upper     The upper triangle of C is updated if true, otherwise the lower
     * @param transpose A and B are k-by-n if true, otherwise n-by-k
     * @param n         The order of C
     * @param k         The number of columns of A and B (rows if transposed)
     */
    public static void invoke(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        ArrayOperation.checkArgument("SYR2K", "n", n >= 0);
        ArrayOperation.checkArgument("SYR2K", "k", k >= 0);
        ArrayOperation.checkArgument("SYR2K", "lda", lda >= Math.max(1, transpose ? k : n));
        ArrayOperation.checkArgument("SYR2K", "ldb", ldb >= Math.max(1, transpose ? k : n));
        ArrayOperation.checkArgument("SYR2K", "ldc", ldc >= Math.max(1, n));

        if (n == 0 || (alpha == ZERO || k == 0) && beta == ONE) {
            return;
        }

        if (NATIVE && n >= NATIVE_THRESHOLD && k >= NATIVE_THRESHOLD) {
            Native.invoke(upper, transpose, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        } else {
            SYR2K.invokeJava(upper, transpose, n, k, alpha, a, offsetA, lda, b, offsetB, ldb, beta, c, offsetC, ldc);
        }
    }

    /**
     * The pure Java implementation of {@link #invoke}, without argument checks.
     */
    static void invokeJava(final boolean upper, final boolean transpose, final int n, final int k, final double alpha, final double[] a, final int offsetA,
            final int lda, final double[] b, final int offsetB, final int ldb, final double beta, final double[] c, final int offsetC, final int ldc) {

        if (alpha == ZERO || k == 0) {
            for (int j = 0; j < n; j++) {
                int colC = offsetC + j * ldc;
                if (upper) {
                    SCAL.scale(c, colC, colC + j + 1, beta);
                } else {
                    SCAL.scale(c, colC + j, colC + n, beta);
                }
            }
            return;
        }

        if (!transpose) {
            for (int j = 0; j < n; j++) {
                int colC = offsetC + j * ldc;
                int first = upper ? 0 : j;
                int limit = upper ? j + 1 : n;
                SCAL.scale(c, colC + first, colC + limit, beta);
                for (int l = 0; l < k; l++) {
                    int colA = offsetA + l * lda;
                    int colB = offsetB + l * ldb;
                    AXPY.invoke(c, colC, alpha * b[colB + j], a, colA, first, limit);
                    AXPY.invoke(c, colC, alpha * a[colA + j], b, colB, first, limit);
                }
            }
        } else {
            for (int j = 0; j < n; j++) {
                int colAj = offsetA + j * lda;
                int colBj = offsetB + j * ldb;
                int colC = offsetC + j * ldc;
                int first = upper ? 0 : j;
                int limit = upper ? j + 1 : n;
                for (int i = first; i < limit; i++) {
                    double temp1 = DOT.invoke(a, offsetA + i * lda, b, colBj, 0, k);
                    double temp2 = DOT.invoke(b, offsetB + i * ldb, a, colAj, 0, k);
                    double temp = alpha * temp1 + alpha * temp2;
                    c[colC + i] = beta == ZERO ? temp : temp + beta * c[colC + i];
                }
            }
        }
    }

}
