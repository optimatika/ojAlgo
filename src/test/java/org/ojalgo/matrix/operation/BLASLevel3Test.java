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

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * The level 3 BLAS operations ({@link GEMM}, {@link SYMM}, {@link SYRK}, {@link SYR2K}, {@link TRMM} and
 * {@link TRSM}) against a plain evaluation of their definitions: all option combinations, alpha and beta 0, 1
 * and other, matrices with offsets and padding (leading dimension larger than the number of rows) as well as
 * matrices filling their arrays exactly.
 * <p>
 * Each case is run through the pure Java implementation, the dispatching {@code invoke}, and the native
 * implementation when there is one (run with {@code --enable-native-access=ojalgo,ALL-UNNAMED} to include
 * it). Elements outside the result must be left untouched, inputs must not change, a C that need not be set
 * (beta 0) and the triangle or diagonal of A that is not referenced are filled with NaN.
 */
public class BLASLevel3Test {

    enum Implementation {
        DISPATCH, JAVA, NATIVE, OFF_HEAP;
    }

    /**
     * Sets the off-heap thresholds of the operations that switch between a critical and an off-heap native
     * call, so that the native implementation is called critically (NATIVE) or off-heap (OFF_HEAP) whatever
     * the size, and restores them when closed. With any other implementation they are left as they are.
     */
    static final class OffHeapLimits implements AutoCloseable {

        private static void set(final double[] values) {
            GEMM.OFF_HEAP_DIMENSION = values[0];
            GEMM.OFF_HEAP_THRESHOLD = values[1];
            SYMM.OFF_HEAP_DIMENSION = values[2];
            SYMM.OFF_HEAP_THRESHOLD = values[3];
            SYRK.OFF_HEAP_DIMENSION = values[4];
            SYRK.OFF_HEAP_THRESHOLD = values[5];
            SYR2K.OFF_HEAP_DIMENSION = values[6];
            SYR2K.OFF_HEAP_THRESHOLD = values[7];
            TRMM.OFF_HEAP_DIMENSION = values[8];
            TRMM.OFF_HEAP_THRESHOLD = values[9];
            TRSM.OFF_HEAP_DIMENSION = values[10];
            TRSM.OFF_HEAP_THRESHOLD = values[11];
            GETRS.OFF_HEAP_DIMENSION = values[12];
            GETRS.OFF_HEAP_THRESHOLD = values[13];
            POTRS.OFF_HEAP_DIMENSION = values[14];
            POTRS.OFF_HEAP_THRESHOLD = values[15];
            TRTRS.OFF_HEAP_DIMENSION = values[16];
            TRTRS.OFF_HEAP_THRESHOLD = values[17];
        }

        static OffHeapLimits of(final Implementation implementation) {
            return new OffHeapLimits(implementation);
        }

        private final double[] saved;

        private OffHeapLimits(final Implementation implementation) {
            saved = new double[] { GEMM.OFF_HEAP_DIMENSION, GEMM.OFF_HEAP_THRESHOLD, SYMM.OFF_HEAP_DIMENSION, SYMM.OFF_HEAP_THRESHOLD, SYRK.OFF_HEAP_DIMENSION,
                    SYRK.OFF_HEAP_THRESHOLD, SYR2K.OFF_HEAP_DIMENSION, SYR2K.OFF_HEAP_THRESHOLD, TRMM.OFF_HEAP_DIMENSION, TRMM.OFF_HEAP_THRESHOLD,
                    TRSM.OFF_HEAP_DIMENSION, TRSM.OFF_HEAP_THRESHOLD, GETRS.OFF_HEAP_DIMENSION, GETRS.OFF_HEAP_THRESHOLD, POTRS.OFF_HEAP_DIMENSION,
                    POTRS.OFF_HEAP_THRESHOLD, TRTRS.OFF_HEAP_DIMENSION, TRTRS.OFF_HEAP_THRESHOLD };
            if (implementation == Implementation.NATIVE || implementation == Implementation.OFF_HEAP) {
                double[] limits = new double[saved.length];
                Arrays.fill(limits, implementation == Implementation.OFF_HEAP ? 0.0 : Double.POSITIVE_INFINITY);
                OffHeapLimits.set(limits);
            }
        }

        @Override
        public void close() {
            OffHeapLimits.set(saved);
        }

    }

    /**
     * Column-major matrix storage in an array, with an offset and a leading dimension.
     */
    static final class Storage {

        final double[] data;
        final int ld;
        final int offset;

        Storage(final Random random, final int rows, final int columns, final boolean tight) {
            int padding = tight ? 0 : 2;
            offset = tight ? 0 : 3;
            ld = Math.max(1, rows + padding);
            data = new double[offset + ld * columns + padding];
            for (int p = 0; p < data.length; p++) {
                data[p] = random.nextDouble() - 0.5;
            }
        }

        double get(final int row, final int col) {
            return data[this.index(row, col)];
        }

        int index(final int row, final int col) {
            return offset + row + col * ld;
        }

        void set(final int row, final int col, final double value) {
            data[this.index(row, col)] = value;
        }

    }

    private static final NumberContext ACCURACY = NumberContext.of(12);
    private static final double[] ALPHAS = { 0.0, 1.0, 0.7 };
    private static final double[] BETAS = { 0.0, 1.0, -0.3 };
    private static final boolean[] BOOLEANS = { false, true };
    /**
     * m, n, k – including the shapes where GEMM is a level 2 operation (m, n or k is 1), and k leaving 1, 2
     * and 3 after a multiple of 4; the larger ones pass the native thresholds.
     */
    private static final int[][] SIZES = { { 1, 1, 1 }, { 1, 9, 6 }, { 7, 1, 5 }, { 6, 8, 1 }, { 3, 4, 2 }, { 13, 10, 11 }, { 17, 19, 18 }, { 40, 33, 37 } };

    private static boolean isAvailable(final Implementation implementation, final boolean nativeAvailable) {
        return implementation != Implementation.NATIVE && implementation != Implementation.OFF_HEAP || nativeAvailable;
    }

    /**
     * A symmetric matrix stored in one triangle; the other (strict) triangle is filled with NaN.
     */
    private static Storage newSymmetric(final Random random, final int dim, final boolean upper, final boolean tight) {
        Storage retVal = new Storage(random, dim, dim, tight);
        for (int j = 0; j < dim; j++) {
            for (int i = 0; i < dim; i++) {
                if (upper ? i > j : i < j) {
                    retVal.set(i, j, Double.NaN);
                }
            }
        }
        return retVal;
    }

    /**
     * The expected result when C (rows-by-columns, or only its upper triangle) is scaled by beta, where beta
     * 0 sets it to 0. With beta 0 that part of C is then filled with NaN, as it must not be read.
     */
    private static double[] scaledByBeta(final Storage c, final int rows, final int columns, final boolean upperTriangle, final double beta) {
        double[] retVal = c.data.clone();
        for (int j = 0; j < columns; j++) {
            for (int i = 0, limit = upperTriangle ? j + 1 : rows; i < limit; i++) {
                retVal[c.index(i, j)] = beta == 0.0 ? 0.0 : beta * c.get(i, j);
                if (beta == 0.0) {
                    c.set(i, j, Double.NaN);
                }
            }
        }
        return retVal;
    }

    /**
     * Element of the full symmetric matrix stored in one triangle.
     */
    private static double symmetric(final Storage matrix, final boolean upper, final int row, final int col) {
        boolean stored = upper ? row <= col : row >= col;
        return stored ? matrix.get(row, col) : matrix.get(col, row);
    }

    /**
     * Element-wise comparison relative to the largest expected element. A result element can be much smaller
     * than the terms summed to get it (cancellation), and then a different summation order gives a large
     * relative difference for that element.
     */
    static void assertArray(final String message, final double[] expected, final double[] actual) {
        double magnitude = 0.0;
        for (int p = 0; p < expected.length; p++) {
            magnitude = Math.max(magnitude, Math.abs(expected[p]));
        }
        for (int p = 0; p < expected.length; p++) {
            if (!ACCURACY.isSmall(magnitude, actual[p] - expected[p])) {
                TestUtils.assertEquals(message + " at " + p, expected[p], actual[p], ACCURACY);
            }
        }
    }

    static void assertUnchanged(final String message, final double[] before, final double[] after) {
        TestUtils.assertTrue(message + " input changed", Arrays.equals(before, after));
    }

    static void doTestGEMM(final Implementation implementation, final boolean tight, final boolean transposeA, final boolean transposeB, final int m,
            final int n, final int k, final double alpha, final double beta) {

        Random random = new Random(m + 31 * n + 961 * k);

        Storage a = transposeA ? new Storage(random, k, m, tight) : new Storage(random, m, k, tight);
        Storage b = transposeB ? new Storage(random, n, k, tight) : new Storage(random, k, n, tight);
        Storage c = new Storage(random, m, n, tight);

        double[] expected = c.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int l = 0; l < k; l++) {
                    sum += (transposeA ? a.get(l, i) : a.get(i, l)) * (transposeB ? b.get(j, l) : b.get(l, j));
                }
                expected[c.index(i, j)] = alpha * sum + (beta == 0.0 ? 0.0 : beta * c.get(i, j));
                if (beta == 0.0) {
                    c.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();
        double[] bBefore = b.data.clone();

        switch (implementation) {
            case JAVA             ->
                GEMM.JVM.invoke(transposeA, transposeB, m, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    GEMM.Native.invoke(transposeA, transposeB, m, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
                }
            }
            default               ->
                GEMM.invoke(transposeA, transposeB, m, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
        }

        String message = "GEMM " + implementation + " tight=" + tight + " transA=" + transposeA + " transB=" + transposeB + " " + m + "x" + n + "x" + k
                + " alpha=" + alpha + " beta=" + beta;
        BLASLevel3Test.assertArray(message, expected, c.data);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
        BLASLevel3Test.assertUnchanged(message, bBefore, b.data);
    }

    static void doTestSYMM(final Implementation implementation, final boolean tight, final boolean left, final boolean upper, final int m, final int n,
            final double alpha, final double beta) {

        Random random = new Random(m + 31 * n);

        int dimA = left ? m : n;
        Storage a = BLASLevel3Test.newSymmetric(random, dimA, upper, tight);
        Storage b = new Storage(random, m, n, tight);
        Storage c = new Storage(random, m, n, tight);

        double[] expected = c.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int l = 0; l < dimA; l++) {
                    sum += left ? BLASLevel3Test.symmetric(a, upper, i, l) * b.get(l, j) : b.get(i, l) * BLASLevel3Test.symmetric(a, upper, l, j);
                }
                expected[c.index(i, j)] = alpha * sum + (beta == 0.0 ? 0.0 : beta * c.get(i, j));
                if (beta == 0.0) {
                    c.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();
        double[] bBefore = b.data.clone();

        switch (implementation) {
            case JAVA             -> SYMM.JVM.invoke(left, upper, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    SYMM.Native.invoke(left, upper, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
                }
            }
            default               -> SYMM.invoke(left, upper, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
        }

        String message = "SYMM " + implementation + " tight=" + tight + " left=" + left + " upper=" + upper + " " + m + "x" + n + " alpha=" + alpha + " beta="
                + beta;
        BLASLevel3Test.assertArray(message, expected, c.data);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
        BLASLevel3Test.assertUnchanged(message, bBefore, b.data);
    }

    static void doTestSYR2K(final Implementation implementation, final boolean tight, final boolean upper, final boolean transpose, final int n, final int k,
            final double alpha, final double beta) {

        Random random = new Random(n + 31 * k);

        Storage a = transpose ? new Storage(random, k, n, tight) : new Storage(random, n, k, tight);
        Storage b = transpose ? new Storage(random, k, n, tight) : new Storage(random, n, k, tight);
        Storage c = new Storage(random, n, n, tight);

        double[] expected = c.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = upper ? 0 : j, limit = upper ? j + 1 : n; i < limit; i++) {
                double sum = 0.0;
                for (int l = 0; l < k; l++) {
                    double ail = transpose ? a.get(l, i) : a.get(i, l);
                    double ajl = transpose ? a.get(l, j) : a.get(j, l);
                    double bil = transpose ? b.get(l, i) : b.get(i, l);
                    double bjl = transpose ? b.get(l, j) : b.get(j, l);
                    sum += ail * bjl + bil * ajl;
                }
                expected[c.index(i, j)] = alpha * sum + (beta == 0.0 ? 0.0 : beta * c.get(i, j));
                if (beta == 0.0) {
                    c.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();
        double[] bBefore = b.data.clone();

        switch (implementation) {
            case JAVA             ->
                SYR2K.JVM.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    SYR2K.Native.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
                }
            }
            default               -> SYR2K.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld, beta, c.data, c.offset, c.ld);
        }

        String message = "SYR2K " + implementation + " tight=" + tight + " upper=" + upper + " trans=" + transpose + " " + n + "x" + k + " alpha=" + alpha
                + " beta=" + beta;
        BLASLevel3Test.assertArray(message, expected, c.data);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
        BLASLevel3Test.assertUnchanged(message, bBefore, b.data);
    }

    static void doTestSYRK(final Implementation implementation, final boolean tight, final boolean upper, final boolean transpose, final int n, final int k,
            final double alpha, final double beta) {

        Random random = new Random(n + 31 * k);

        Storage a = transpose ? new Storage(random, k, n, tight) : new Storage(random, n, k, tight);
        Storage c = new Storage(random, n, n, tight);

        double[] expected = c.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = upper ? 0 : j, limit = upper ? j + 1 : n; i < limit; i++) {
                double sum = 0.0;
                for (int l = 0; l < k; l++) {
                    sum += (transpose ? a.get(l, i) : a.get(i, l)) * (transpose ? a.get(l, j) : a.get(j, l));
                }
                expected[c.index(i, j)] = alpha * sum + (beta == 0.0 ? 0.0 : beta * c.get(i, j));
                if (beta == 0.0) {
                    c.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();

        switch (implementation) {
            case JAVA             -> SYRK.JVM.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, beta, c.data, c.offset, c.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    SYRK.Native.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, beta, c.data, c.offset, c.ld);
                }
            }
            default               -> SYRK.invoke(upper, transpose, n, k, alpha, a.data, a.offset, a.ld, beta, c.data, c.offset, c.ld);
        }

        String message = "SYRK " + implementation + " tight=" + tight + " upper=" + upper + " trans=" + transpose + " " + n + "x" + k + " alpha=" + alpha
                + " beta=" + beta;
        BLASLevel3Test.assertArray(message, expected, c.data);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
    }

    static void doTestTRMM(final Implementation implementation, final boolean tight, final boolean left, final boolean upper, final boolean transposeA,
            final boolean unitDiagonal, final int m, final int n, final double alpha) {

        Random random = new Random(m + 31 * n);

        int dimA = left ? m : n;
        Storage a = BLASLevel3Test.newTriangular(random, dimA, upper, unitDiagonal, tight);
        Storage b = new Storage(random, m, n, tight);

        double[] expected = b.data.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int l = 0; l < dimA; l++) {
                    sum += left ? BLASLevel3Test.triangular(a, upper, transposeA, unitDiagonal, i, l) * b.get(l, j)
                            : b.get(i, l) * BLASLevel3Test.triangular(a, upper, transposeA, unitDiagonal, l, j);
                }
                expected[b.index(i, j)] = alpha * sum;
            }
        }
        if (alpha == 0.0) {
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    b.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();

        switch (implementation) {
            case JAVA             -> TRMM.JVM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    TRMM.Native.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                }
            }
            default               -> TRMM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
        }

        String message = "TRMM " + implementation + " tight=" + tight + " left=" + left + " upper=" + upper + " transA=" + transposeA + " unit=" + unitDiagonal
                + " " + m + "x" + n + " alpha=" + alpha;
        BLASLevel3Test.assertArray(message, expected, b.data);
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
    }

    /**
     * Checked through the residual: op(A)*X (left) or X*op(A) (right) must equal alpha*B.
     */
    static void doTestTRSM(final Implementation implementation, final boolean tight, final boolean left, final boolean upper, final boolean transposeA,
            final boolean unitDiagonal, final int m, final int n, final double alpha) {

        Random random = new Random(m + 31 * n);

        int dimA = left ? m : n;
        Storage a = BLASLevel3Test.newTriangular(random, dimA, upper, unitDiagonal, tight);
        Storage b = new Storage(random, m, n, tight);

        double[] rhs = b.data.clone();
        if (alpha == 0.0) {
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    b.set(i, j, Double.NaN);
                }
            }
        }

        double[] aBefore = a.data.clone();

        switch (implementation) {
            case JAVA             -> TRSM.JVM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
            case NATIVE, OFF_HEAP -> {
                try (OffHeapLimits limits = OffHeapLimits.of(implementation)) {
                    TRSM.Native.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
                }
            }
            default               -> TRSM.invoke(left, upper, transposeA, unitDiagonal, m, n, alpha, a.data, a.offset, a.ld, b.data, b.offset, b.ld);
        }

        double[] expected = rhs.clone();
        double[] actual = rhs.clone();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                double sum = 0.0;
                for (int l = 0; l < dimA; l++) {
                    sum += left ? BLASLevel3Test.triangular(a, upper, transposeA, unitDiagonal, i, l) * b.get(l, j)
                            : b.get(i, l) * BLASLevel3Test.triangular(a, upper, transposeA, unitDiagonal, l, j);
                }
                expected[b.index(i, j)] = alpha * rhs[b.index(i, j)];
                actual[b.index(i, j)] = sum;
            }
        }

        String message = "TRSM " + implementation + " tight=" + tight + " left=" + left + " upper=" + upper + " transA=" + transposeA + " unit=" + unitDiagonal
                + " " + m + "x" + n + " alpha=" + alpha;
        BLASLevel3Test.assertArray(message, expected, actual);
        for (int p = 0; p < rhs.length; p++) {
            if (p < b.offset || (p - b.offset) % b.ld >= m || p >= b.index(0, n)) {
                TestUtils.assertEquals(message + " outside B at " + p, rhs[p], b.data[p]);
            }
        }
        BLASLevel3Test.assertUnchanged(message, aBefore, a.data);
    }

    /**
     * A triangular matrix with a dominant diagonal. The other (strict) triangle is filled with NaN, and so is
     * the diagonal if unit.
     */
    static Storage newTriangular(final Random random, final int dim, final boolean upper, final boolean unitDiagonal, final boolean tight) {
        Storage retVal = new Storage(random, dim, dim, tight);
        for (int j = 0; j < dim; j++) {
            for (int i = 0; i < dim; i++) {
                if (i == j) {
                    retVal.set(i, j, unitDiagonal ? Double.NaN : 2.0 + random.nextDouble());
                } else if (upper ? i > j : i < j) {
                    retVal.set(i, j, Double.NaN);
                } else {
                    retVal.set(i, j, retVal.get(i, j) / dim);
                }
            }
        }
        return retVal;
    }

    /**
     * Element of op(T) where T is the triangular matrix stored in {@code matrix}.
     */
    static double triangular(final Storage matrix, final boolean upper, final boolean transpose, final boolean unitDiagonal, final int row, final int col) {
        int i = transpose ? col : row;
        int j = transpose ? row : col;
        if (i == j) {
            return unitDiagonal ? 1.0 : matrix.get(i, j);
        }
        boolean inTriangle = upper ? i < j : i > j;
        return inTriangle ? matrix.get(i, j) : 0.0;
    }

    /**
     * With alpha 0, A and B are not read: the dispatching invoke must work with empty arrays for them, also
     * at sizes where the native implementation is used otherwise. C is scaled by beta (and not read when beta
     * is 0); for TRMM and TRSM, B is set to 0 (and not read).
     */
    @Test
    public void testAlphaZero() {

        double[] none = new double[0];
        int m = 40, n = 30, k = 50;

        for (double beta : new double[] { 0.0, 0.5 }) {

            Storage c = new Storage(new Random(1), m, n, false);
            double[] expected = BLASLevel3Test.scaledByBeta(c, m, n, false, beta);
            GEMM.invoke(false, false, m, n, k, 0.0, none, 0, m, none, 0, k, beta, c.data, c.offset, c.ld);
            BLASLevel3Test.assertArray("GEMM beta=" + beta, expected, c.data);

            c = new Storage(new Random(2), m, n, false);
            expected = BLASLevel3Test.scaledByBeta(c, m, n, false, beta);
            SYMM.invoke(true, true, m, n, 0.0, none, 0, m, none, 0, m, beta, c.data, c.offset, c.ld);
            BLASLevel3Test.assertArray("SYMM beta=" + beta, expected, c.data);

            c = new Storage(new Random(3), m, m, false);
            expected = BLASLevel3Test.scaledByBeta(c, m, m, true, beta);
            SYRK.invoke(true, false, m, k, 0.0, none, 0, m, beta, c.data, c.offset, c.ld);
            BLASLevel3Test.assertArray("SYRK beta=" + beta, expected, c.data);

            c = new Storage(new Random(4), m, m, false);
            expected = BLASLevel3Test.scaledByBeta(c, m, m, true, beta);
            SYR2K.invoke(true, false, m, k, 0.0, none, 0, m, none, 0, m, beta, c.data, c.offset, c.ld);
            BLASLevel3Test.assertArray("SYR2K beta=" + beta, expected, c.data);
        }

        Storage b = new Storage(new Random(5), m, n, false);
        double[] expected = BLASLevel3Test.scaledByBeta(b, m, n, false, 0.0);
        TRMM.invoke(true, true, false, false, m, n, 0.0, none, 0, m, b.data, b.offset, b.ld);
        BLASLevel3Test.assertArray("TRMM", expected, b.data);

        b = new Storage(new Random(6), m, n, false);
        expected = BLASLevel3Test.scaledByBeta(b, m, n, false, 0.0);
        TRSM.invoke(true, true, false, false, m, n, 0.0, none, 0, m, b.data, b.offset, b.ld);
        BLASLevel3Test.assertArray("TRSM", expected, b.data);
    }

    @Test
    public void testArgumentChecks() {

        double[] data = new double[16];

        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEMM.invoke(false, false, 4, 2, 2, 1.0, data, 0, 3, data, 0, 2, 0.0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> GEMM.invoke(false, false, -1, 2, 2, 1.0, data, 0, 1, data, 0, 2, 0.0, data, 0, 1));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> SYRK.invoke(true, true, 4, 2, 1.0, data, 0, 1, 0.0, data, 0, 4));
        TestUtils.assertThrows(IllegalArgumentException.class, () -> TRSM.invoke(false, true, false, false, 2, 4, 1.0, data, 0, 3, data, 0, 2));

        if (GEMM.NATIVE) {
            double[] tooShort = new double[7];
            TestUtils.assertThrows(IndexOutOfBoundsException.class,
                    () -> GEMM.Native.invoke(false, false, 4, 2, 2, 1.0, data, 0, 4, data, 0, 2, 0.0, tooShort, 0, 4));
        }
    }

    @Test
    public void testGEMM() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, GEMM.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean transposeA : BOOLEANS) {
                        for (boolean transposeB : BOOLEANS) {
                            for (int[] size : SIZES) {
                                for (double alpha : ALPHAS) {
                                    for (double beta : BETAS) {
                                        BLASLevel3Test.doTestGEMM(implementation, tight, transposeA, transposeB, size[0], size[1], size[2], alpha, beta);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The cache blocked Java implementation, with the block thresholds lowered so that blocks, including
     * partial edge blocks, occur at test sizes.
     */
    @Test
    public void testGEMMBlocked() {

        int blockSize = GEMM.BLOCK_SIZE;
        int blockThreshold = GEMM.BLOCK_THRESHOLD;

        try {

            GEMM.BLOCK_SIZE = 20;
            GEMM.BLOCK_THRESHOLD = 32;

            for (boolean tight : BOOLEANS) {
                for (boolean transposeA : BOOLEANS) {
                    for (boolean transposeB : BOOLEANS) {
                        for (int[] size : new int[][] { { 70, 45, 66 }, { 41, 9, 60 } }) {
                            for (double alpha : ALPHAS) {
                                for (double beta : BETAS) {
                                    BLASLevel3Test.doTestGEMM(Implementation.JAVA, tight, transposeA, transposeB, size[0], size[1], size[2], alpha, beta);
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            GEMM.BLOCK_SIZE = blockSize;
            GEMM.BLOCK_THRESHOLD = blockThreshold;
        }
    }

    /**
     * The multi-threaded Java implementation, with the threshold lowered so that it splits at test sizes.
     */
    @Test
    public void testGEMMParallel() {

        int threshold = GEMM.THRESHOLD;

        try {

            GEMM.THRESHOLD = 6;

            for (boolean tight : BOOLEANS) {
                for (boolean transposeA : BOOLEANS) {
                    for (boolean transposeB : BOOLEANS) {
                        for (int[] size : new int[][] { { 70, 45, 66 }, { 9, 60, 41 }, { 41, 9, 60 } }) {
                            for (double alpha : ALPHAS) {
                                for (double beta : BETAS) {
                                    BLASLevel3Test.doTestGEMM(Implementation.JAVA, tight, transposeA, transposeB, size[0], size[1], size[2], alpha, beta);
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            GEMM.THRESHOLD = threshold;
        }
    }

    @Test
    public void testSYMM() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, SYMM.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean left : BOOLEANS) {
                        for (boolean upper : BOOLEANS) {
                            for (int[] size : SIZES) {
                                for (double alpha : ALPHAS) {
                                    for (double beta : BETAS) {
                                        BLASLevel3Test.doTestSYMM(implementation, tight, left, upper, size[0], size[1], alpha, beta);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSYR2K() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, SYR2K.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transpose : BOOLEANS) {
                            for (int[] size : SIZES) {
                                for (double alpha : ALPHAS) {
                                    for (double beta : BETAS) {
                                        BLASLevel3Test.doTestSYR2K(implementation, tight, upper, transpose, size[0], size[2], alpha, beta);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSYRK() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, SYRK.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transpose : BOOLEANS) {
                            for (int[] size : SIZES) {
                                for (double alpha : ALPHAS) {
                                    for (double beta : BETAS) {
                                        BLASLevel3Test.doTestSYRK(implementation, tight, upper, transpose, size[0], size[2], alpha, beta);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testTRMM() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, TRMM.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean left : BOOLEANS) {
                        for (boolean upper : BOOLEANS) {
                            for (boolean transposeA : BOOLEANS) {
                                for (boolean unitDiagonal : BOOLEANS) {
                                    for (int[] size : SIZES) {
                                        for (double alpha : ALPHAS) {
                                            BLASLevel3Test.doTestTRMM(implementation, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The blocked Java implementation, with the block size lowered so that several blocks, including a
     * partial last block, occur at test sizes.
     */
    @Test
    public void testTRMMBlocked() {

        int blockSize = TRMM.BLOCK_SIZE;

        try {

            TRMM.BLOCK_SIZE = 5;

            for (boolean tight : BOOLEANS) {
                for (boolean left : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transposeA : BOOLEANS) {
                            for (boolean unitDiagonal : BOOLEANS) {
                                for (int[] size : new int[][] { { 23, 17 }, { 17, 23 }, { 5, 9 }, { 9, 5 } }) {
                                    for (double alpha : ALPHAS) {
                                        BLASLevel3Test.doTestTRMM(Implementation.JAVA, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            TRMM.BLOCK_SIZE = blockSize;
        }
    }

    /**
     * The multi-threaded Java implementation, with the threshold lowered so that the blocks on the diagonal
     * are split at test sizes.
     */
    @Test
    public void testTRMMParallel() {

        int blockSize = TRMM.BLOCK_SIZE;
        int threshold = TRMM.THRESHOLD;

        try {

            TRMM.BLOCK_SIZE = 16;
            TRMM.THRESHOLD = 2;

            for (boolean tight : BOOLEANS) {
                for (boolean left : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transposeA : BOOLEANS) {
                            for (boolean unitDiagonal : BOOLEANS) {
                                for (int[] size : new int[][] { { 70, 45 }, { 45, 70 }, { 9, 60 }, { 60, 9 } }) {
                                    for (double alpha : ALPHAS) {
                                        BLASLevel3Test.doTestTRMM(Implementation.JAVA, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            TRMM.BLOCK_SIZE = blockSize;
            TRMM.THRESHOLD = threshold;
        }
    }

    @Test
    public void testTRSM() {
        for (Implementation implementation : Implementation.values()) {
            if (BLASLevel3Test.isAvailable(implementation, TRSM.NATIVE)) {
                for (boolean tight : BOOLEANS) {
                    for (boolean left : BOOLEANS) {
                        for (boolean upper : BOOLEANS) {
                            for (boolean transposeA : BOOLEANS) {
                                for (boolean unitDiagonal : BOOLEANS) {
                                    for (int[] size : SIZES) {
                                        for (double alpha : ALPHAS) {
                                            BLASLevel3Test.doTestTRSM(implementation, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The blocked Java implementation, with the block size lowered so that several blocks, including a
     * partial last block, occur at test sizes.
     */
    @Test
    public void testTRSMBlocked() {

        int blockSize = TRSM.BLOCK_SIZE;

        try {

            TRSM.BLOCK_SIZE = 5;

            for (boolean tight : BOOLEANS) {
                for (boolean left : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transposeA : BOOLEANS) {
                            for (boolean unitDiagonal : BOOLEANS) {
                                for (int[] size : new int[][] { { 23, 17 }, { 17, 23 }, { 5, 9 }, { 9, 5 } }) {
                                    for (double alpha : ALPHAS) {
                                        BLASLevel3Test.doTestTRSM(Implementation.JAVA, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            TRSM.BLOCK_SIZE = blockSize;
        }
    }

    /**
     * The multi-threaded Java implementation, with the threshold lowered so that the blocks on the diagonal
     * are split at test sizes.
     */
    @Test
    public void testTRSMParallel() {

        int blockSize = TRSM.BLOCK_SIZE;
        int threshold = TRSM.THRESHOLD;

        try {

            TRSM.BLOCK_SIZE = 16;
            TRSM.THRESHOLD = 2;

            for (boolean tight : BOOLEANS) {
                for (boolean left : BOOLEANS) {
                    for (boolean upper : BOOLEANS) {
                        for (boolean transposeA : BOOLEANS) {
                            for (boolean unitDiagonal : BOOLEANS) {
                                for (int[] size : new int[][] { { 70, 45 }, { 45, 70 }, { 9, 60 }, { 60, 9 } }) {
                                    for (double alpha : ALPHAS) {
                                        BLASLevel3Test.doTestTRSM(Implementation.JAVA, tight, left, upper, transposeA, unitDiagonal, size[0], size[1], alpha);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } finally {
            TRSM.BLOCK_SIZE = blockSize;
            TRSM.THRESHOLD = threshold;
        }
    }

}
