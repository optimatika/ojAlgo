/*
 * Copyright 1997-2020 Optimatika
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
import org.ojalgo.array.Array1D;
import org.ojalgo.array.operation.ArrayOperation;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Bidiagonal;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.Hessenberg;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.decomposition.Tridiagonal;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.structure.StructureAnyD;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

/**
 * JUnitUtils
 *
 * @author apete
 */
public abstract class TestUtils {

    private static NumberContext EQUALS = new NumberContext(12, 14, RoundingMode.HALF_EVEN);

    public static void assertBounds(final Comparable<?> lower, final Access1D<?> values, final Comparable<?> upper, final NumberContext precision) {
        for (ElementView1D<?, ?> tmpValue : values.elements()) {
            TestUtils.assertBounds(lower, tmpValue.get(), upper, precision);
        }
    }

    public static void assertBounds(final Comparable<?> lower, final Comparable<?> value, final Comparable<?> upper, final NumberContext precision) {

        BigDecimal tmpLower = TypeUtils.toBigDecimal(lower, precision);
        BigDecimal tmpValue = TypeUtils.toBigDecimal(value, precision);
        BigDecimal tmpUpper = TypeUtils.toBigDecimal(upper, precision);

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

    public static void assertEquals(final Comparable<?> expected, final Comparable<?> actual) {
        TestUtils.assertEquals(expected, actual, EQUALS);
    }

    public static void assertEquals(final Comparable<?> expected, final Comparable<?> actual, final NumberContext context) {
        TestUtils.assertEquals("Number != Number", expected, actual, context);
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
        if ((expected instanceof Structure2D) && (actual instanceof Structure2D)) {
            TestUtils.assertEquals(message + ", different countRows()", ((Structure2D) expected).countRows(), ((Structure2D) actual).countRows());
            TestUtils.assertEquals(message + ", different countColumns()", ((Structure2D) expected).countColumns(), ((Structure2D) actual).countColumns());
        } else if ((expected instanceof StructureAnyD) && (actual instanceof StructureAnyD)) {
            TestUtils.assertEquals(message + ", different shape()", ((StructureAnyD) expected).shape(), ((StructureAnyD) actual).shape());
        }

        double tmpFrobNormDiff = 0.0;
        double tmpFrobNormExpt = 0.0;
        for (long i = 0L; i < expected.count(); i++) {
            tmpFrobNormDiff = PrimitiveMath.HYPOT.invoke(tmpFrobNormDiff, actual.doubleValue(i) - expected.doubleValue(i));
            tmpFrobNormExpt = PrimitiveMath.HYPOT.invoke(tmpFrobNormExpt, expected.doubleValue(i));
        }
        TestUtils.assertTrue(message + ", large norm differences " + tmpFrobNormDiff + " !<< " + tmpFrobNormExpt,
                context.isSmall(tmpFrobNormExpt, tmpFrobNormDiff));
    }

    public static void assertEquals(final String message, final Comparable<?> expected, final Comparable<?> actual) {
        TestUtils.assertEquals(message, expected, actual, EQUALS);
    }

    public static void assertEquals(final String message, final Comparable<?> expected, final Comparable<?> actual, final NumberContext precision) {

        if ((expected instanceof Quaternion) || (actual instanceof Quaternion)) {

            Quaternion tmpExpected = Quaternion.valueOf(expected);
            Quaternion tmpActual = Quaternion.valueOf(actual);

            if (!!precision.isDifferent(tmpExpected.scalar(), tmpActual.scalar())) {
                // Assertions.fail(() -> message + " (scalar)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (scalar)" + ": " + expected + " != " + actual);
            }
            if (!!precision.isDifferent(tmpExpected.i, tmpActual.i)) {
                // Assertions.fail(() -> message + " (i)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (i)" + ": " + expected + " != " + actual);
            }
            if (!!precision.isDifferent(tmpExpected.j, tmpActual.j)) {
                // Assertions.fail(() -> message + " (j)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (j)" + ": " + expected + " != " + actual);
            }
            if (!!precision.isDifferent(tmpExpected.k, tmpActual.k)) {
                // Assertions.fail(() -> message + " (k)" + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + " (k)" + ": " + expected + " != " + actual);
            }

        } else if ((expected instanceof ComplexNumber) || (actual instanceof ComplexNumber)) {

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

        } else {

            if (precision.isDifferent(Scalar.doubleValue(expected), Scalar.doubleValue(actual))) {
                // Assertions.fail(() -> message + ": " + expected + " != " + actual);
                Assertions.assertEquals(expected, actual, () -> message + ": " + expected + " != " + actual);
            }
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

    public static void assertEquivalent(final Optimisation.Result expected, final Optimisation.Result actual) {
        TestUtils.assertOptimisationResult("Optimisation.Result != Optimisation.Result", expected, actual, EQUALS, true, true, true, true);
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

    public static void assertInRange(final int first, final int limit, final int actual) {
        if ((first > actual) || (actual >= limit)) {
            TestUtils.fail("Not in range!");
        }
    }

    public static void assertNotNullOrEmpty(final String actual) {
        if (actual == null) {
            TestUtils.fail("Is null!");
        }
        if (actual.length() <= 0) {
            TestUtils.fail("Is empty!");
        }
    }

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

    public static void fail(final Throwable problem) {
        Assertions.fail(problem.getMessage(), problem);
    }

    public static PhysicalStore<ComplexNumber> makeRandomComplexStore(final int numberOfRows, final int numberOfColumns) {

        PhysicalStore<ComplexNumber> retVal = GenericStore.COMPLEX.makeZero(numberOfRows, numberOfColumns);

        Uniform tmpArgGen = new Uniform(PrimitiveMath.ZERO, PrimitiveMath.TWO_PI);

        for (int j = 0; j < numberOfColumns; j++) {
            for (int i = 0; i < numberOfRows; i++) {
                retVal.set(i, j, ComplexNumber.makePolar(PrimitiveMath.E, tmpArgGen.doubleValue()).add(PrimitiveMath.PI));
            }
        }

        return retVal;
    }

    public static void minimiseAllBranchLimits() {
        ArrayOperation.setAllOperationThresholds(2);
    }

    static void assertOptimisationResult(final String message, final Optimisation.Result expected, final Optimisation.Result actual,
            final NumberContext context, final boolean state, final boolean value, final boolean solution, final boolean multipliers) {

        State expectedState = expected.getState();

        if (state) {

            State actualState = actual.getState();

            boolean failed = false;

            if (expectedState == actualState) {

            } else if (expectedState.isDistinct() && !actualState.isDistinct()) {
                failed = true;
            } else if (expectedState.isOptimal() && !actualState.isOptimal()) {
                failed = true;
            } else if (expectedState.isFeasible() && !actualState.isFeasible()) {
                failed = true;
            } else if (expectedState.isApproximate() && !actualState.isApproximate()) {
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
