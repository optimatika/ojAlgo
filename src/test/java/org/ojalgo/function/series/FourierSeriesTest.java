/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.function.series;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction.SampleDomain;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.context.NumberContext;

public class FourierSeriesTest extends SeriesFunctionTests {

    private static final NumberContext ACCURACY = NumberContext.of(2);

    private static final double INITIAL = ZERO - PI - E;

    private static final double NUDGE = TEN * MACHINE_EPSILON;

    private static final SampleDomain SAMPLE = new SampleDomain(TWO * TWO_PI, 16);

    private static boolean isCloseTooAnyMultipleOfPI(final double arg) {
        double cantidate = arg;
        while (cantidate < PI) {
            cantidate += PI;
        }
        while (cantidate > TWO_PI) {
            cantidate -= PI;
        }
        cantidate = Math.min(cantidate - PI, TWO_PI - cantidate);
        return cantidate < NINTH;
    }

    static void doTest(final DoubleUnaryOperator function, final SampleDomain period) {

        FourierSeries series = FourierSeries.estimate(function, period);

        for (double arg = INITIAL; arg < TEN; arg += NINTH) {
            double expected = function.applyAsDouble(arg);
            double actual = series.invoke(arg);
            if (DEBUG) {
                BasicLogger.debug("f({}) = {} & {}", arg, expected, actual);
            }
            if (!FourierSeriesTest.isCloseTooAnyMultipleOfPI(arg)) {
                // This is to avoid the discontinuities some functions have at multiples of PI.
                TestUtils.assertEquals("" + arg, expected, actual, ACCURACY);
            }
        }
    }

    static void doTest(final PeriodicFunction function, final int nbSamples) {
        FourierSeriesTest.doTest(function, function.getSampleDomain(nbSamples));
    }

    @Test
    public void testCos() {
        FourierSeriesTest.doTest(Math::cos, SAMPLE);
    }

    /**
     * https://www.math.kit.edu/iana3/lehre/fourierana2014w/media/fstable141127.pdf
     */
    @Test
    public void testTableItem01() {

        PeriodicFunction function = PeriodicFunction.ofCentered(Math::abs);

        TestUtils.assertEquals(PI, function.applyAsDouble(-PI));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(-HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(ZERO));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(HALF_PI));
        TestUtils.assertEquals(PI, function.applyAsDouble(PI));

        // Next period
        TestUtils.assertEquals(PI, function.applyAsDouble(PI));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(TWO_PI - HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(TWO_PI));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(TWO_PI + HALF_PI));
        TestUtils.assertEquals(PI, function.applyAsDouble(TWO_PI + PI));

        // Previous period
        TestUtils.assertEquals(PI, function.applyAsDouble(-TWO_PI - PI));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(-TWO_PI - HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(-TWO_PI));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(-TWO_PI + HALF_PI));
        TestUtils.assertEquals(PI, function.applyAsDouble(-PI));

        FourierSeriesTest.doTest(function, 64);
    }

    /**
     * https://www.math.kit.edu/iana3/lehre/fourierana2014w/media/fstable141127.pdf
     */
    @Test
    public void testTableItem02() {

        PeriodicFunction function = PeriodicFunction.ofCentered(DoubleUnaryOperator.identity());

        TestUtils.assertEquals(-PI, function.applyAsDouble(-PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(ZERO));
        TestUtils.assertEquals(PI, function.applyAsDouble(PI - NUDGE));

        FourierSeriesTest.doTest(function, 256);
    }

    /**
     * https://www.math.kit.edu/iana3/lehre/fourierana2014w/media/fstable141127.pdf
     */
    @Test
    public void testTableItem03() {

        PeriodicFunction function = PeriodicFunction.of(arg -> PI - arg);

        TestUtils.assertEquals(PI, function.applyAsDouble(ZERO));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(PI));
        TestUtils.assertEquals(-PI, function.applyAsDouble(TWO_PI - NUDGE));

        FourierSeriesTest.doTest(function, 256);
    }

    /**
     * https://www.math.kit.edu/iana3/lehre/fourierana2014w/media/fstable141127.pdf
     */
    @Test
    public void testTableItem04() {

        PeriodicFunction function = PeriodicFunction.ofCentered(arg -> arg <= ZERO ? ZERO : arg);

        TestUtils.assertEquals(ZERO, function.applyAsDouble(-PI + NUDGE));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(-HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(ZERO));
        TestUtils.assertEquals(HALF_PI, function.applyAsDouble(HALF_PI));
        TestUtils.assertEquals(PI, function.applyAsDouble(PI - NUDGE));

        FourierSeriesTest.doTest(function, 4096);
    }

    /**
     * https://www.math.kit.edu/iana3/lehre/fourierana2014w/media/fstable141127.pdf
     */
    @Test
    public void testTableItem05() {

        PeriodicFunction function = PeriodicFunction.ofCentered(arg -> Math.sin(arg) * Math.sin(arg));
        DoubleUnaryOperator series = arg -> 0.5 - 0.5 * Math.cos(2 * arg);

        TestUtils.assertEquals(ZERO, function.applyAsDouble(-PI));
        TestUtils.assertEquals(ONE, function.applyAsDouble(-HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(ZERO));
        TestUtils.assertEquals(ONE, function.applyAsDouble(HALF_PI));
        TestUtils.assertEquals(ZERO, function.applyAsDouble(PI - NUDGE));

        FourierSeriesTest.doTest(function, 8);
    }

    @Test
    public void testSawtooth() {
        FourierSeriesTest.doTest(PeriodicFunction.SAWTOOTH, 256);
    }

    @Test
    public void testSin() {
        FourierSeriesTest.doTest(Math::sin, SAMPLE);
    }

    @Test
    public void testSine() {
        FourierSeriesTest.doTest(PeriodicFunction.SINE, 8);
    }

    @Test
    public void testSquare() {
        FourierSeriesTest.doTest(PeriodicFunction.SQUARE, 128);
    }

    @Test
    public void testTriangle() {
        FourierSeriesTest.doTest(PeriodicFunction.TRIANGLE, 32);
    }

}
