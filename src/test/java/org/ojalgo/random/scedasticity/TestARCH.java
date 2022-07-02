/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.random.scedasticity;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.process.StationaryNormalProcess;

public class TestARCH extends RandomScedasticityTests {

    /**
     * The mean/expected value should be constant
     */
    @Test
    public void testConstant() {

        double mean = 10.0;
        double variance = 2.0;

        for (int q = 1; q < 10; q++) {

            ARCH model = ARCH.newInstance(q, mean, variance);

            StationaryNormalProcess process = StationaryNormalProcess.of(model);

            double expected = process.getExpected();

            for (int t = 0; t < 10; t++) {

                process.step();

                double actual = process.getExpected();

                if (DEBUG) {
                    BasicLogger.debug("p={}, q={}, exp={}, act={}", q, expected, actual);
                }
                TestUtils.assertEquals(expected, actual);
            }

            TestUtils.assertFalse(variance == process.getVariance());
        }
    }

    /**
     * @see TestGARCH#testStandard()
     */
    @Test
    public void testDecrease() {

        double mean = 10.0;
        double variance = 2.0;

        for (int q = 1; q < 10; q++) {

            ARCH model = ARCH.newInstance(q, mean, variance);

            double stdDev = model.getStandardDeviation();

            model.update(mean + stdDev - 1.0); // Less than the standard/expected error

            if (DEBUG) {
                BasicLogger.debug("q={}, exp={}, act={}", q, stdDev, model.getStandardDeviation());
            }
            TestUtils.assertTrue(stdDev > model.getStandardDeviation());
        }
    }

    /**
     * @see TestGARCH#testStandard()
     */
    @Test
    public void testIncrease() {

        double mean = 10.0;
        double variance = 2.0;

        for (int q = 1; q < 10; q++) {

            ARCH model = ARCH.newInstance(q, mean, variance);

            double stdDev = model.getStandardDeviation();

            model.update(mean + stdDev + 1.0); // More than the standard/expected error

            if (DEBUG) {
                BasicLogger.debug("q={}, exp={}, act={}", q, stdDev, model.getStandardDeviation());
            }
            TestUtils.assertTrue(stdDev < model.getStandardDeviation());
        }
    }

    /**
     * Updates with the standard deviation should not change the variance
     */
    @Test
    public void testStandard() {

        double mean = 10.0;
        double variance = 2.0;

        for (int q = 1; q < 10; q++) {

            ARCH model = ARCH.newInstance(q, mean, variance);

            double stdDev = model.getStandardDeviation();

            model.update(mean + stdDev); // Exactly the standard/expected error

            if (DEBUG) {
                BasicLogger.debug("q={}, exp={}, act={}", q, stdDev, model.getStandardDeviation());
            }
            TestUtils.assertEquals(stdDev, model.getStandardDeviation());
        }
    }

}
