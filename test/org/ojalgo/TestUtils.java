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
package org.ojalgo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Hessenberg;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.decomposition.Tridiagonal;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

import junit.framework.Assert;

/**
 * JUnitUtils
 *
 * @author apete
 */
@SuppressWarnings("deprecation")
public abstract class TestUtils {

    private static final NumberContext EQUALS = new NumberContext(7, 14, RoundingMode.HALF_EVEN);

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
            Assert.fail("!(" + tmpLower.toPlainString() + " <= " + tmpValue.toPlainString() + " <= " + tmpUpper.toPlainString() + ")");
        }
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual, final NumberContext context) {
        TestUtils.assertEquals("Access1D<?> != Access1D<?>", expected, actual, context);
    }

    public static void assertEquals(final boolean expected, final boolean actual) {
        Assert.assertEquals(expected, actual);
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
        Assert.assertEquals(expected, actual, delta);
    }

    public static void assertEquals(final double expected, final double actual, final NumberContext context) {
        TestUtils.assertEquals("double != double", expected, actual, context);
    }

    public static void assertEquals(final int expected, final int actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(final int[] expected, final int[] actual) {
        TestUtils.assertEquals(Arrays.toString(expected) + " != " + Arrays.toString(actual), expected, actual);
    }

    public static void assertEquals(final long expected, final long actual) {
        Assert.assertEquals(expected, actual);
    }

    public static void assertEquals(final long[] expected, final long[] actual) {
        TestUtils.assertEquals(Arrays.toString(expected) + " != " + Arrays.toString(actual), expected, actual);
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Bidiagonal<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("Bidiagonal<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Cholesky<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("Cholesky<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Eigenvalue<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("Eigenvalue<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Hessenberg<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("Hessenberg<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final LU<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("LU<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final QR<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("QR<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final SingularValue<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("SingularValue<N>", expected, actual);
        }
    }

    public static <N extends Number> void assertEquals(final MatrixStore<N> expected, final Tridiagonal<N> actual, final NumberContext context) {
        if (!MatrixUtils.equals(expected, actual, context)) {
            Assert.failNotEquals("Tridiagonal<N>", expected, actual);
        }
    }

    public static void assertEquals(final Number expected, final Number actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Number expected, final Number actual, final NumberContext context) {
        TestUtils.assertEquals("Number != Number", expected, actual, context);
    }

    public static void assertEquals(final Object expected, final Object actual) {
        Assert.assertEquals(expected, actual);
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
        for (long i = 0L; i < expected.count(); i++) {
            TestUtils.assertEquals(message + " @ " + i, expected.get(i), actual.get(i), context);
        }
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
        Assert.assertEquals(message, expected, actual, delta);
    }

    public static void assertEquals(final String message, final double expected, final double actual, final NumberContext context) {
        // TestUtils.assertEquals(message, Double.valueOf(expected), Double.valueOf(actual), context);
        if (Double.isNaN(expected) && Double.isNaN(actual)) {

        } else if (context.isDifferent(expected, actual)) {
            Assert.fail(Assert.format(message, expected, actual));
        }
    }

    public static void assertEquals(final String message, final int expected, final int actual) {
        Assert.assertEquals(message, expected, actual);
    }

    public static void assertEquals(final String message, final int[] expected, final int[] actual) {
        TestUtils.assertTrue(message, Arrays.equals(expected, actual));
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
                Assert.failNotEquals(message + " (real)", expected, actual);
            }
            if (!!context.isDifferent(tmpExpected.getImaginary(), tmpActual.getImaginary())) {
                Assert.failNotEquals(message + " (imaginary)", expected, actual);
            }

        } else if ((expected instanceof Quaternion) || (actual instanceof Quaternion)) {

            final Quaternion tmpExpected = Quaternion.valueOf(expected);
            final Quaternion tmpActual = Quaternion.valueOf(actual);

            if (!!context.isDifferent(tmpExpected.scalar(), tmpActual.scalar())) {
                Assert.failNotEquals(message + " (scalar)", expected, actual);
            }
            if (!!context.isDifferent(tmpExpected.i, tmpActual.i)) {
                Assert.failNotEquals(message + " (i)", expected, actual);
            }
            if (!!context.isDifferent(tmpExpected.j, tmpActual.j)) {
                Assert.failNotEquals(message + " (j)", expected, actual);
            }
            if (!!context.isDifferent(tmpExpected.k, tmpActual.k)) {
                Assert.failNotEquals(message + " (k)", expected, actual);
            }

        } else {

            if (context.isDifferent(expected.doubleValue(), actual.doubleValue())) {
                Assert.failNotEquals(message, expected, actual);
            }
        }
    }

    public static void assertEquals(final String message, final Object expected, final Object actual) {
        Assert.assertEquals(message, expected, actual);

    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual, final NumberContext context) {
        TestUtils.assertEquals(message, (Number) expected, (Number) actual, context);
        TestUtils.assertEquals(message, (Access1D<?>) expected, (Access1D<?>) actual, context);
    }

    public static void assertFalse(final boolean condition) {
        Assert.assertFalse(condition);
    }

    public static void assertFalse(final String message, final boolean condition) {
        Assert.assertFalse(message, condition);
    }

    public static void assertStateLessThanFeasible(final Optimisation.Result actual) {
        Assert.assertFalse(actual.toString(), actual.getState().isFeasible());
    }

    public static void assertStateNotLessThanFeasible(final Optimisation.Result actual) {
        Assert.assertTrue(actual.toString(), actual.getState().isFeasible());
    }

    public static void assertStateNotLessThanOptimal(final Optimisation.Result actual) {
        Assert.assertTrue(actual.toString(), actual.getState().isOptimal());
    }

    public static void assertTrue(final boolean condition) {
        Assert.assertTrue(condition);
    }

    public static void assertTrue(final String message, final boolean condition) {
        Assert.assertTrue(message, condition);
    }

    public static void fail() {
        Assert.fail();
    }

    public static void fail(final String message) {
        Assert.fail(message);
    }

    public static void minimiseAllBranchLimits() {
        MatrixUtils.setAllOperationThresholds(2);
    }

    private TestUtils() {
        super();
    }

}
