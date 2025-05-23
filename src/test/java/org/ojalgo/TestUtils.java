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
package org.ojalgo;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Hessenberg;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.decomposition.Tridiagonal;
import org.ojalgo.matrix.operation.MatrixOperation;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.StructureAnyD;
import org.ojalgo.tensor.Tensor;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.NumberDefinition;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * JUnitUtils
 *
 * @author apete
 */
public abstract class TestUtils /* extends Assertions */ {

    private static final NumberContext EQUALS = NumberContext.of(12);

    public static void assertArrayEquals(final byte[] expected, final byte[] actual) {
        Assertions.assertArrayEquals(expected, actual);
    }

    public static void assertArrayEquals(final double[] expected, final double[] actual) {
        Assertions.assertArrayEquals(expected, actual);

    }

    public static void assertArrayEquals(final long[] expected, final long[] actual) {
        Assertions.assertArrayEquals(expected, actual);
    }

    public static void assertBounds(final Comparable<?> lower, final Access1D<?> values, final Comparable<?> upper, final NumberContext precision) {
        for (ElementView1D<?, ?> tmpValue : values.elements()) {
            TestUtils.assertBounds(lower, tmpValue.get(), upper, precision);
        }
    }

    public static void assertBounds(final Comparable<?> lower, final Comparable<?> value, final Comparable<?> upper, final NumberContext precision) {

        BigDecimal tmpLower = TypeUtils.toBigDecimal(lower, precision);
        BigDecimal tmpValue = TypeUtils.toBigDecimal(value, precision);
        BigDecimal tmpUpper = TypeUtils.toBigDecimal(upper, precision);

        if (tmpValue.compareTo(tmpLower) < 0 || tmpValue.compareTo(tmpUpper) > 0) {
            Assertions.fail("!(" + tmpLower.toPlainString() + " <= " + tmpValue.toPlainString() + " <= " + tmpUpper.toPlainString() + ")");
        }
    }

    public static void assertComplexEquals(final Access1D<ComplexNumber> expected, final Access1D<ComplexNumber> actual) {
        TestUtils.assertComplexEquals(expected, actual, EQUALS);
    }

    public static void assertComplexEquals(final Access1D<ComplexNumber> expected, final Access1D<ComplexNumber> actual, final NumberContext context) {
        TestUtils.assertEquals(expected, actual, context);
        for (int i = 0; i < expected.size(); i++) {
            TestUtils.assertEquals(expected.get(i), actual.get(i), context);
        }
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Access1D<?> expected, final Access1D<?> actual, final NumberContext context) {
        TestUtils.assertEquals("Access1D<?> != Access1D<?>", expected, actual, context);
    }

    public static void assertEquals(final BigDecimal expected, final BigDecimal actual, final MathContext context) {
        boolean equal = expected.round(context).compareTo(actual.round(context)) == 0;
        if (!equal) {
            TestUtils.fail(expected + " != " + actual);
        }
    }

    public static void assertEquals(final boolean expected, final boolean actual) {
        Assertions.assertEquals(expected, actual);
    }

    public static void assertEquals(final Comparable<?> expected, final Comparable<?> actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Comparable<?> expected, final Comparable<?> actual, final NumberContext context) {
        TestUtils.assertEquals("Number != Number", expected, actual, context);
    }

    public static void assertEquals(final Comparable<?> expected, final double actual, final NumberContext context) {
        TestUtils.assertEquals(expected, Double.valueOf(actual), context);
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

    public static void assertEquals(final double[] expected, final Access1D<?> actual, final NumberContext accuracy) {
        for (int p = 0; p < expected.length; p++) {
            TestUtils.assertEquals(expected[p], actual.doubleValue(p), accuracy);
        }
    }

    public static void assertEquals(final double[] expected, final double[] actual) {
        TestUtils.assertEquals(ArrayR064.wrap(expected), ArrayR064.wrap(actual));
    }

    public static void assertEquals(final int expected, final int actual) {
        Assertions.assertEquals(expected, actual);
    }

    public static void assertEquals(final int expected, final IntSupplier actual) {
        TestUtils.assertEquals(expected, actual.getAsInt());
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

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final Bidiagonal<N> actual, final NumberContext context) {
        if (!Bidiagonal.equals(expected, actual, context)) {
            Assertions.fail(() -> "Bidiagonal<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final Cholesky<N> actual, final NumberContext context) {
        if (!Cholesky.equals(expected, actual, context)) {
            Assertions.fail(() -> "Cholesky<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final Eigenvalue<N> actual, final NumberContext context) {
        if (!Eigenvalue.equals(expected, actual, context)) {
            Assertions.fail(() -> "Eigenvalue<N> failed for " + expected);
        }
        if (actual.isOrdered()) {
            MatrixStore<N> mtrxD = actual.getD();
            double bigger = Double.MAX_VALUE;
            Array1D<ComplexNumber> tmpEigenvalues = actual.getEigenvalues();
            for (int i = 0; i < tmpEigenvalues.length; i++) {
                ComplexNumber value = tmpEigenvalues.get(i);
                Assertions.assertTrue(bigger >= value.getModulus());
                Assertions.assertEquals(value.doubleValue(), mtrxD.doubleValue(i, i), context.epsilon());
                bigger = value.getModulus();
            }
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final Hessenberg<N> actual, final NumberContext context) {
        if (!Hessenberg.equals(expected, actual, context)) {
            Assertions.fail(() -> "Hessenberg<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final LU<N> actual, final NumberContext context) {
        if (!LU.equals(expected, actual, context)) {
            Assertions.fail(() -> "LU<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final QR<N> actual, final NumberContext context) {
        if (!QR.equals(expected, actual, context)) {
            Assertions.fail(() -> "QR<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final SingularValue<N> actual, final NumberContext context) {
        if (!SingularValue.equals(expected, actual, context)) {
            Assertions.fail(() -> "SingularValue<N> failed for " + expected);
        }
    }

    public static <N extends Comparable<N>> void assertEquals(final MatrixStore<N> expected, final Tridiagonal<N> actual, final NumberContext context) {
        if (!Tridiagonal.equals(expected, actual, context)) {
            Assertions.fail(() -> "Tridiagonal<N> failed for " + expected);
        }
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
        if (expected instanceof Structure2D && actual instanceof Structure2D) {
            TestUtils.assertEquals(message + ", different countRows()", ((Structure2D) expected).countRows(), ((Structure2D) actual).countRows());
            TestUtils.assertEquals(message + ", different countColumns()", ((Structure2D) expected).countColumns(), ((Structure2D) actual).countColumns());
        } else if (expected instanceof StructureAnyD && actual instanceof StructureAnyD) {
            TestUtils.assertEquals(message + ", different shape()", ((StructureAnyD) expected).shape(), ((StructureAnyD) actual).shape());
        }

        if (!Access1D.equals(expected, actual, context)) {

            double diffNorm = 0.0;
            double expNorm = 0.0;
            for (long i = 0L; i < expected.count(); i++) {

                double act = actual.doubleValue(i);
                double exp = expected.doubleValue(i);

                diffNorm = MissingMath.hypot(diffNorm, act - exp);
                expNorm = MissingMath.hypot(expNorm, exp);
            }

            TestUtils.assertTrue(message + ", large norm differences " + diffNorm + " !<< " + expNorm, Access1D.equals(expected, actual, context));
        }
    }

    public static void assertEquals(final String message, final Comparable<?> expected, final Comparable<?> actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Comparable<?> expected, final Comparable<?> actual, final NumberContext precision) {

        if (expected instanceof Quaternion || actual instanceof Quaternion) {

            Quaternion tmpExpected = Quaternion.valueOf(expected);
            Quaternion tmpActual = Quaternion.valueOf(actual);

            if (precision.isDifferent(tmpExpected.scalar(), tmpActual.scalar())) {
                // Assertions.fail(() -> message + " (scalar)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (scalar)" + ": " + expected + " != " + actual);
            }
            if (precision.isDifferent(tmpExpected.i, tmpActual.i)) {
                // Assertions.fail(() -> message + " (i)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (i)" + ": " + expected + " != " + actual);
            }
            if (precision.isDifferent(tmpExpected.j, tmpActual.j)) {
                // Assertions.fail(() -> message + " (j)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (j)" + ": " + expected + " != " + actual);
            }
            if (precision.isDifferent(tmpExpected.k, tmpActual.k)) {
                // Assertions.fail(() -> message + " (k)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (k)" + ": " + expected + " != " + actual);
            }

        } else if (expected instanceof ComplexNumber || actual instanceof ComplexNumber) {

            ComplexNumber tmpExpected = ComplexNumber.valueOf(expected);
            ComplexNumber tmpActual = ComplexNumber.valueOf(actual);

            if (precision.isDifferent(tmpExpected.getReal(), tmpActual.getReal())) {
                // Assertions.fail(() -> message + " (real)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (real)" + ": " + expected + " != " + actual);
            }
            if (precision.isDifferent(tmpExpected.getImaginary(), tmpActual.getImaginary())) {
                // Assertions.fail(() -> message + " (imaginary)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (imaginary)" + ": " + expected + " != " + actual);
            }

        } else if (precision.isDifferent(NumberDefinition.doubleValue(expected), NumberDefinition.doubleValue(actual))) {
            // Assertions.fail(() -> message + ": " + expected + " != " + actual);
            Assertions.assertEquals(expected, actual, () -> message + ": " + expected + " != " + actual);
        }
    }

    public static void assertEquals(final String message, final ComplexNumber expected, final ComplexNumber actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final ComplexNumber expected, final ComplexNumber actual, final NumberContext context) {
        TestUtils.assertEquals(message, (Comparable<?>) expected, (Comparable<?>) actual, context);
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

    public static void assertEquals(final String message, final Object expected, final Object actual) {
        Assertions.assertEquals(expected, actual, message);

    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Quaternion expected, final Quaternion actual, final NumberContext context) {
        TestUtils.assertEquals(message, (Comparable<?>) expected, (Comparable<?>) actual, context);
        TestUtils.assertEquals(message, (Access1D<?>) expected, (Access1D<?>) actual, context);
    }

    public static void assertFalse(final boolean condition) {
        Assertions.assertFalse(condition);
    }

    public static void assertFalse(final String message, final boolean condition) {
        Assertions.assertFalse(condition, message);
    }

    public static void assertFasterThan(final double limitMeassure, final CalendarDateUnit limitUnit, final Stopwatch actualTimer) {
        CalendarDateDuration duration = actualTimer.stop(limitUnit);
        if (duration.measure > limitMeassure) {
            TestUtils.fail(duration.toString() + " > " + new CalendarDateDuration(limitMeassure, limitUnit));
        }
    }

    public static void assertInRange(final double first, final double limit, final double actual) {
        if (first > actual || actual >= limit) {
            TestUtils.fail("Not in range!");
        }
    }

    public static void assertInRange(final int first, final int limit, final int actual) {
        if (first > actual || actual >= limit) {
            TestUtils.fail("Not in range!");
        }
    }

    public static void assertLessThan(final double reference, final double actual) {
        if (actual >= reference) {
            Assertions.fail(actual + " !< " + reference);
        }
    }

    public static void assertMoreThan(final double reference, final double actual) {
        if (actual <= reference) {
            Assertions.fail(actual + " !> " + reference);
        }
    }

    public static void assertNotEquals(final Object unexpected, final Object actual) {
        Assertions.assertNotEquals(unexpected, actual);
    }

    public static void assertNotLessThan(final double reference, final double actual) {
        if (actual < reference) {
            Assertions.fail(actual + " !>= " + reference);
        }
    }

    public static void assertNotMoreThan(final double reference, final double actual) {
        if (actual > reference) {
            Assertions.fail(actual + " !<= " + reference);
        }
    }

    public static void assertNotNullOrEmpty(final String actual) {
        if (actual == null) {
            TestUtils.fail("Is null!");
        } else if (actual.length() <= 0) {
            TestUtils.fail("Is empty!");
        }
    }

    public static void assertNull(final Object actual) {
        Assertions.assertNull(actual);
    }

    /**
     * Will compare state, value, solution and multiplier if available.
     */
    public static void assertResult(final Optimisation.Result expected, final Optimisation.Result actual) {
        TestUtils.assertResult(expected, actual, EQUALS);
    }

    public static void assertResult(final Optimisation.Result expected, final Optimisation.Result actual, final NumberContext context) {
        TestUtils.assertResult("Optimisation.Result != Optimisation.Result", expected, actual, context);
    }

    public static void assertResult(final String message, final Optimisation.Result expected, final Optimisation.Result actual) {
        TestUtils.assertResult(message, expected, actual, EQUALS);
    }

    public static void assertResult(final String message, final Optimisation.Result expected, final Optimisation.Result actual, final NumberContext context) {
        TestUtils.assertOptimisationResult(message, expected, actual, context, true, true, true, true);
    }

    public static void assertSolutionFeasible(final ExpressionsBasedModel model, final Optimisation.Result solution) {
        TestUtils.assertSolutionFeasible(model, solution, EQUALS);
    }

    public static void assertSolutionFeasible(final ExpressionsBasedModel model, final Optimisation.Result solution, final NumberContext accuracy) {
        TestUtils.assertStateNotLessThanFeasible(solution);
        TestUtils.assertTrue(model.validate(solution, accuracy, BasicLogger.DEBUG));
    }

    public static void assertSolutionValid(final ExpressionsBasedModel model, final Access1D<BigDecimal> solution) {
        if (!model.validate(solution, BasicLogger.ERROR)) {
            Assertions.fail("Solution not valid!");
        }
    }

    public static void assertSolutionValid(final ExpressionsBasedModel model, final Access1D<BigDecimal> solution, final NumberContext accuracy) {
        if (!model.validate(solution, accuracy, BasicLogger.ERROR)) {
            Assertions.fail("Solution not valid!");
        }
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
        TestUtils.assertOptimisationResult(message, expected, actual, context, true, false, true, false);
    }

    public static void assertStateInfeasible(final Result actual) {
        Assertions.assertEquals(Optimisation.State.INFEASIBLE, actual.getState(), actual.toString());
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

    public static void assertStateUnbounded(final Result actual) {
        Assertions.assertEquals(Optimisation.State.UNBOUNDED, actual.getState(), actual.toString());
    }

    public static void assertStateUnboundedOrLessThanFeasible(final Result actual) {
        Assertions.assertTrue(!actual.getState().isFeasible() || Optimisation.State.UNBOUNDED.equals(actual.getState()), actual.toString());
    }

    public static void assertTensorEquals(final Tensor<?, ?> expected, final Tensor<?, ?> actual) {

        TestUtils.assertEquals(expected.rank(), actual.rank());
        TestUtils.assertEquals(expected.dimensions(), actual.dimensions());

        if (expected.rank() == 0) {
            TestUtils.assertEquals(((Scalar<?>) expected).doubleValue(), ((Scalar<?>) actual).doubleValue(), EQUALS);
        } else {
            TestUtils.assertEquals((Access1D<?>) expected, (Access1D<?>) actual, EQUALS);
        }
    }

    public static <T extends Throwable> T assertThrows(final Class<T> expectedType, final Executable executable) {
        return Assertions.assertThrows(expectedType, executable);
    }

    public static void assertTrue(final boolean condition) {
        Assertions.assertTrue(condition);
    }

    public static void assertTrue(final boolean condition, final String message) {
        TestUtils.assertTrue(message, condition);
    }

    public static void assertTrue(final String message, final boolean condition) {
        Assertions.assertTrue(condition, message);
    }

    public static <V> V fail() {
        return Assertions.fail("");
    }

    public static <V> V fail(final String message) {
        return Assertions.fail(message);
    }

    public static <V> V fail(final String message, final Throwable cause) {
        return Assertions.fail(message, cause);
    }

    public static <V> V fail(final Throwable cause) {
        return Assertions.fail(cause.getMessage(), cause);
    }

    public static InputStream getResource(final Class<?> root, final String name) {
        return root.getResourceAsStream(name);
    }

    public static InputStream getResource(final String... pathElements) {
        return TestUtils.getResource(TestUtils.class, TestUtils.buildAbsoluteResourcePath(pathElements));
    }

    public static PhysicalStore<ComplexNumber> makeRandomComplexStore(final int numberOfRows, final int numberOfColumns) {

        PhysicalStore<ComplexNumber> retVal = GenericStore.C128.make(numberOfRows, numberOfColumns);

        Uniform tmpArgGen = new Uniform(PrimitiveMath.ZERO, PrimitiveMath.TWO_PI);

        for (int j = 0; j < numberOfColumns; j++) {
            for (int i = 0; i < numberOfRows; i++) {
                retVal.set(i, j, ComplexNumber.makePolar(PrimitiveMath.E, tmpArgGen.doubleValue()).add(PrimitiveMath.PI));
            }
        }

        return retVal;
    }

    public static void minimiseAllBranchLimits() {
        MatrixOperation.setAllOperationThresholds(2);
    }

    public static void success() {
        Assertions.assertTrue(true);
    }

    private static String buildAbsoluteResourcePath(final String... pathElements) {

        File builder = new File(pathElements[0]);

        for (int i = 1; i < pathElements.length; i++) {
            builder = new File(builder, pathElements[i]);
        }

        return "/" + builder.toPath();
    }

    static void assertOptimisationResult(final String message, final Optimisation.Result expected, final Optimisation.Result actual,
            final NumberContext context, final boolean state, final boolean value, final boolean solution, final boolean multipliers) {

        State expectedState = expected.getState();

        if (state) {

            State actualState = actual.getState();

            boolean failed = false;

            if (expectedState == actualState) {
                failed = false;
            } else if (expectedState.isDistinct() && !actualState.isDistinct() || expectedState.isOptimal() && !actualState.isOptimal()) {
                failed = true;
            } else if (expectedState.isFeasible() && !actualState.isFeasible() || expectedState.isApproximate() && !actualState.isApproximate()) {
                failed = true;
            }

            if (failed) {
                TestUtils.assertEquals(message + " – State", expectedState, actualState);
            }
        }

        if (value) {
            double expectedValue = expected.getValue();
            double actualValue = actual.getValue();
            TestUtils.assertEquals(message + " – Value", expectedValue, actualValue, context);
        }

        if (solution && expectedState.isFeasible()) {
            TestUtils.assertEquals(message + " – Solution", (Access1D<?>) expected, (Access1D<?>) actual, context);
        }

        if (multipliers && expected.getMultipliers().isPresent()) {
            Access1D<?> expectedMultipliers = expected.getMultipliers().get();
            Access1D<?> actualMultipliers = actual.getMultipliers().get();
            TestUtils.assertEquals(message + " – Multipliers", expectedMultipliers, actualMultipliers, context);
        }
    }

    private TestUtils() {
        super();
    }

}
