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
package org.ojalgo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Structure2D;
import org.ojalgo.access.StructureAnyD;
import org.ojalgo.array.Array1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Hessenberg;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.decomposition.Tridiagonal;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.operation.MatrixOperation;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * JUnitUtils
 *
 * @author apete
 */
public abstract class TestUtils {

    private static final NumberContext EQUALS = new NumberContext(12, 14, RoundingMode.HALF_EVEN);

    public static void assertBounds(final Number lower, final Access1D<?> values, final Number upper, final NumberContext precision) {
        for (final Number tmpValue : values) {
            TestUtils.assertBounds(lower, tmpValue, upper, precision);
        }
    }

    public static void assertBounds(final Number lower, final Number value, final Number upper, final NumberContext precision) {

        final BigDecimal tmpLower = TypeUtils.toBigDecimal(lower, precision);
        final BigDecimal tmpValue = TypeUtils.toBigDecimal(value, precision);
        final BigDecimal tmpUpper = TypeUtils.toBigDecimal(upper, precision);

        if ((tmpValue.compareTo(tmpLower) == -1) || (tmpValue.compareTo(tmpUpper) == 1)) {
            Assertions.fail("!(" + tmpLower.toPlainString() + " <= " + tmpValue.toPlainString() + " <= " + tmpUpper.toPlainString() + ")");
        }
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual, final NumberContext context) {
        TestUtils.assertEquals("Access1D<?> != Access1D<?>", expected, actual, context);
    }

    public static void assertEquals(final boolean expected, final boolean actual) {
        Assertions.assertEquals(expected, actual);
    }

    public static void assertEquals(final ComplexNumber expected, final ComplexNumber actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final ComplexNumber expected, final ComplexNumber actual, final NumberContext context) {
        TestUtils.assertEquals("ComplexNumber != ComplexNumber", expected, actual, context);
    }

    public static void assertEquals(final double expected, final ComplexNumber actual, final NumberContext context) {
        TestUtils.assertEquals("ComplexNumber.re", expected, actual.doubleValue(), context);
        TestUtils.assertEquals("ComplexNumber.im", PrimitiveMath.ZERO, actual.i, context);
    }

    public static void assertEquals(final double expected, final double actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final double expected, final double actual, final double delta) {
        Assertions.assertEquals(expected, actual, delta);
    }

    public static void assertEquals(final double expected, final double actual, final NumberContext context) {
        TestUtils.assertEquals("double != double", expected, actual, context);
    }

    public static void assertEquals(final int expected, final int actual) {
        Assertions.assertEquals(expected, actual);
    }

    public static void assertEquals(final int[] expected, final int[] actual) {
        TestUtils.assertEquals(Arrays.toString(expected) + " != " + Arrays.toString(actual), expected, actual);
    }

    public static void assertEquals(final long expected, final long actual) {
        TestUtils.assertEquals("long != long", expected, actual);
    }

    public static void assertEquals(final long[] expected, final long[] actual) {
        TestUtils.assertEquals(Arrays.toString(expected) + " != " + Arrays.toString(actual), expected, actual);
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Bidiagonal<N> actual, final NumberContext context) {
        if (!Bidiagonal.equals(expected, actual, context)) {
            Assertions.fail(() -> "Bidiagonal<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Cholesky<N> actual, final NumberContext context) {
        if (!Cholesky.equals(expected, actual, context)) {
            Assertions.fail(() -> "Cholesky<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Eigenvalue<N> actual, final NumberContext context) {
        if (!Eigenvalue.equals(expected, actual, context)) {
            Assertions.fail(() -> "Eigenvalue<N> failed for " + expected);
        }
        if (actual.isOrdered()) {
            final MatrixStore<N> mtrxD = actual.getD();
            double bigger = Double.MAX_VALUE;
            final Array1D<ComplexNumber> tmpEigenvalues = actual.getEigenvalues();
            for (int i = 0; i < tmpEigenvalues.length; i++) {
                final ComplexNumber value = tmpEigenvalues.get(i);
                Assertions.assertTrue(bigger >= value.getModulus());
                Assertions.assertEquals(value.doubleValue(), mtrxD.doubleValue(i, i), context.epsilon());
                bigger = value.getModulus();
            }
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Hessenberg<N> actual, final NumberContext context) {
        if (!Hessenberg.equals(expected, actual, context)) {
            Assertions.fail(() -> "Hessenberg<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final LU<N> actual, final NumberContext context) {
        if (!LU.equals(expected, actual, context)) {
            Assertions.fail(() -> "LU<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final QR<N> actual, final NumberContext context) {
        if (!QR.equals(expected, actual, context)) {
            Assertions.fail(() -> "QR<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final SingularValue<N> actual, final NumberContext context) {
        if (!SingularValue.equals(expected, actual, context)) {
            Assertions.fail(() -> "SingularValue<N> failed for " + expected);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Tridiagonal<N> actual, final NumberContext context) {
        if (!Tridiagonal.equals(expected, actual, context)) {
            Assertions.fail(() -> "Tridiagonal<N> failed for " + expected);
        }
    }

    public static void assertEquals(final Number expected, final Number actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Number expected, final Number actual, final NumberContext context) {
        TestUtils.assertEquals("Number != Number", expected, actual, context);
    }

    public static void assertEquals(final Object expected, final Object actual) {
        Assertions.assertEquals(expected, actual);
    }

    public static void assertEquals(final Quaternion expected, final Quaternion actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Quaternion expected, final Quaternion actual, final NumberContext context) {
        TestUtils.assertEquals("Quaternion != Quaternion", expected, actual, context);
    }

    public static void assertEquals(final String message, final Access1D<?> expected, final Access1D<?> actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Access1D<?> expected, final Access1D<?> actual, final NumberContext context) {

        TestUtils.assertEquals(message + ", different count()", expected.count(), actual.count());
        if ((expected instanceof Structure2D) && (actual instanceof Structure2D)) {
            TestUtils.assertEquals(message + ", different countRows()", ((Structure2D) expected).countRows(), ((Structure2D) actual).countRows());
            TestUtils.assertEquals(message + ", different countColumns()", ((Structure2D) expected).countColumns(), ((Structure2D) actual).countColumns());
        } else if ((expected instanceof StructureAnyD) && (actual instanceof StructureAnyD)) {
            TestUtils.assertEquals(message + ", different shape()", ((StructureAnyD) expected).shape(), ((StructureAnyD) actual).shape());
        }

        double tmpFrobNormDiff = 0.0;
        double tmpFrobNormExpt = 0.0;
        for (long i = 0L; i < expected.count(); i++) {
            tmpFrobNormDiff = PrimitiveFunction.HYPOT.invoke(tmpFrobNormDiff, actual.doubleValue(i) - expected.doubleValue(i));
            tmpFrobNormExpt = PrimitiveFunction.HYPOT.invoke(tmpFrobNormExpt, expected.doubleValue(i));
        }
        TestUtils.assertTrue(message + ", large norm differences " + tmpFrobNormDiff + " !<< " + tmpFrobNormExpt,
                context.isSmall(tmpFrobNormExpt, tmpFrobNormDiff));
    }

    public static void assertEquals(final String message, final ComplexNumber expected, final ComplexNumber actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final ComplexNumber expected, final ComplexNumber actual, final NumberContext context) {
        TestUtils.assertEquals(message, (Number) expected, (Number) actual, context);
        TestUtils.assertEquals(message, (Access1D<?>) expected, (Access1D<?>) actual, context);
    }

    public static void assertEquals(final String message, final double expected, final double actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final double expected, final double actual, final double delta) {
        Assertions.assertEquals(expected, actual, delta, message);
    }

    public static void assertEquals(final String message, final double expected, final double actual, final NumberContext context) {
        // TestUtils.assertEquals(message, Double.valueOf(expected), Double.valueOf(actual), context);
        if (Double.isNaN(expected) && Double.isNaN(actual)) {

        } else if (context.isDifferent(expected, actual)) {
            Assertions.fail(() -> message + ": " + expected + " != " + actual);
        }
    }

    public static void assertEquals(final String message, final int expected, final int actual) {
        Assertions.assertEquals(expected, actual, message);
    }

    public static void assertEquals(final String message, final int[] expected, final int[] actual) {
        TestUtils.assertTrue(message, Arrays.equals(expected, actual));
    }

    public static void assertEquals(final String message, final long expected, final long actual) {
        Assertions.assertEquals(expected, actual, message);
    }

    public static void assertEquals(final String message, final long[] expected, final long[] actual) {
        TestUtils.assertTrue(message, Arrays.equals(expected, actual));
    }

    public static void assertEquals(final String message, final Number expected, final Number actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Number expected, final Number actual, final NumberContext context) {

        if ((expected instanceof ComplexNumber) || (actual instanceof ComplexNumber)) {

            final ComplexNumber tmpExpected = ComplexNumber.valueOf(expected);
            final ComplexNumber tmpActual = ComplexNumber.valueOf(actual);

            if (!!context.isDifferent(tmpExpected.getReal(), tmpActual.getReal())) {
                Assertions.fail(() -> message + " (real)" + ": " + expected + " != " + actual);
            }
            if (!!context.isDifferent(tmpExpected.getImaginary(), tmpActual.getImaginary())) {
                Assertions.fail(() -> message + " (imaginary)" + ": " + expected + " != " + actual);
            }

        } else if ((expected instanceof Quaternion) || (actual instanceof Quaternion)) {

            final Quaternion tmpExpected = Quaternion.valueOf(expected);
            final Quaternion tmpActual = Quaternion.valueOf(actual);

            if (!!context.isDifferent(tmpExpected.scalar(), tmpActual.scalar())) {
                Assertions.fail(() -> message + " (scalar)" + ": " + expected + " != " + actual);
            }
            if (!!context.isDifferent(tmpExpected.i, tmpActual.i)) {
                Assertions.fail(() -> message + " (i)" + ": " + expected + " != " + actual);
            }
            if (!!context.isDifferent(tmpExpected.j, tmpActual.j)) {
                Assertions.fail(() -> message + " (j)" + ": " + expected + " != " + actual);
            }
            if (!!context.isDifferent(tmpExpected.k, tmpActual.k)) {
                Assertions.fail(() -> message + " (k)" + ": " + expected + " != " + actual);
            }

        } else {

            if (context.isDifferent(expected.doubleValue(), actual.doubleValue())) {
                Assertions.fail(() -> message + ": " + expected + " != " + actual);
            }
        }
    }

    public static void assertEquals(final String message, final Object expected, final Object actual) {
        Assertions.assertEquals(expected, actual, message);

    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual, final NumberContext context) {
        TestUtils.assertEquals(message, (Number) expected, (Number) actual, context);
        TestUtils.assertEquals(message, (Access1D<?>) expected, (Access1D<?>) actual, context);
    }

    public static void assertFalse(final boolean condition) {
        Assertions.assertFalse(condition);
    }

    public static void assertFalse(final String message, final boolean condition) {
        Assertions.assertFalse(condition, message);
    }

    public static void assertStateAndSolution(final Optimisation.Result expected, final Optimisation.Result actual) {
        TestUtils.assertStateAndSolution(expected, actual, EQUALS);
    }

    public static void assertStateAndSolution(final Optimisation.Result expected, final Optimisation.Result actual, final NumberContext context) {
        TestUtils.assertStateAndSolution("Optimisation.Result != Optimisation.Result", expected, actual, context);
    }

    public static void assertStateAndSolution(final String message, final Optimisation.Result expected, final Optimisation.Result actual) {
        TestUtils.assertStateAndSolution(message, expected, actual, EQUALS);
    }

    public static void assertStateAndSolution(final String message, final Optimisation.Result expected, final Optimisation.Result actual,
            final NumberContext context) {

        TestUtils.assertEquals(message + ", different Optimisation.State", expected.getState(), actual.getState());

        if (expected.getState().isFeasible()) {
            TestUtils.assertEquals(message, expected, actual, context);
        }
    }

    public static void assertStateLessThanFeasible(final Optimisation.Result actual) {
        Assertions.assertFalse(actual.getState().isFeasible(), actual.toString());
    }

    public static void assertStateNotLessThanFeasible(final Optimisation.Result actual) {
        Assertions.assertTrue(actual.getState().isFeasible(), actual.toString());
    }

    public static void assertStateNotLessThanOptimal(final Optimisation.Result actual) {
        Assertions.assertTrue(actual.getState().isOptimal(), actual.toString());
    }

    public static void assertTrue(final boolean condition) {
        Assertions.assertTrue(condition);
    }

    public static void assertTrue(final String message, final boolean condition) {
        Assertions.assertTrue(condition, message);
    }

    public static void fail() {
        Assertions.fail("");
    }

    public static void fail(final String message) {
        Assertions.fail(message);
    }

    public static void minimiseAllBranchLimits() {
        MatrixOperation.setAllOperationThresholds(2);
    }

    private TestUtils() {
        super();
    }

}
