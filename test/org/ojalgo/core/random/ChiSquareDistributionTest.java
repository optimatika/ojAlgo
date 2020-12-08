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
package org.ojalgo.core.random;

import static org.ojalgo.core.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;
import org.ojalgo.core.type.StandardType;
import org.ojalgo.core.type.context.NumberContext;

public class ChiSquareDistributionTest {

    private static final NumberContext ACCURACY = StandardType.MATH_032.withPrecision(2).withScale(3);

    static void doTestLowerTail(final int degreesOfFreedom, final double[] criticalValues) {

        double[] probabilities = new double[] { 0.10, 0.05, 0.025, 0.01, 0.001 };

        ChiSquareDistribution distribution = ChiSquareDistribution.of(degreesOfFreedom);

        for (int i = 0; i < probabilities.length; i++) {
            // BasicLogger.debug("Degree {} ({}): {} <=> {}", degreesOfFreedom, probabilities[i], criticalValues[i], distribution.getQuantile(probabilities[i]));
            TestUtils.assertEquals(criticalValues[i], distribution.getQuantile(probabilities[i]), ACCURACY);
        }
    }

    static void doTestUpperTail(final int degreesOfFreedom, final double[] criticalValues) {

        double[] probabilities = new double[] { 0.90, 0.95, 0.975, 0.99, 0.999 };

        ChiSquareDistribution distribution = ChiSquareDistribution.of(degreesOfFreedom);

        for (int i = 0; i < probabilities.length; i++) {
            // BasicLogger.debug("Degree {} ({}): {} <=> {}", degreesOfFreedom, probabilities[i], criticalValues[i], distribution.getQuantile(probabilities[i]));
            TestUtils.assertEquals(criticalValues[i], distribution.getQuantile(probabilities[i]), ACCURACY);
        }
    }

    @Test
    public void testApproximation() {

        NumberContext accuracy1 = StandardType.MATH_032;
        NumberContext accuracy2 = accuracy1.withPrecision(2);

        int degree = 100;

        Normal normal = new Normal(degree, Math.sqrt(2 * degree));

        ChiSquareDistribution approximation = new ChiSquareDistribution.NormalApproximation(degree);
        ChiSquareDistribution general = new ChiSquareDistribution(degree);

        TestUtils.assertEquals(normal.getExpected(), approximation.getExpected(), accuracy1);
        TestUtils.assertEquals(normal.getVariance(), approximation.getVariance(), accuracy1);
        TestUtils.assertEquals(normal.getStandardDeviation(), approximation.getStandardDeviation(), accuracy1);

        TestUtils.assertEquals(normal.getExpected(), general.getExpected(), accuracy1);
        TestUtils.assertEquals(normal.getVariance(), general.getVariance(), accuracy1);
        TestUtils.assertEquals(normal.getStandardDeviation(), general.getStandardDeviation(), accuracy1);

        for (int i = 1; i < 10; i++) {
            double p = i / TEN;
            double x = normal.getQuantile(p);

            TestUtils.assertEquals(normal.getDensity(x), approximation.getDensity(x), accuracy1);
            TestUtils.assertEquals(p, approximation.getDistribution(x), accuracy1);
            TestUtils.assertEquals(x, approximation.getQuantile(p), accuracy1);

            TestUtils.assertEquals(normal.getDensity(x), general.getDensity(x), accuracy2);
            TestUtils.assertEquals(p, general.getDistribution(x), accuracy2);
            TestUtils.assertEquals(x, general.getQuantile(p), accuracy2);
        }
    }

    @Test
    public void testDegree2() {

        ChiSquareDistribution specific = new ChiSquareDistribution.Degree2();
        ChiSquareDistribution general = new ChiSquareDistribution(2);

        TestUtils.assertEquals(general.getExpected(), specific.getExpected());
        TestUtils.assertEquals(general.getVariance(), specific.getVariance());
        TestUtils.assertEquals(general.getStandardDeviation(), specific.getStandardDeviation());

        for (int i = 0; i <= 16; i++) {

            double x = i / TWO; // [0,8]
            double p = i / (EIGHT + EIGHT); // [0,1]
            // BasicLogger.debug("x={} & p={}", x, p);

            TestUtils.assertEquals(general.getDensity(x), specific.getDensity(x));
            TestUtils.assertEquals(general.getDistribution(x), specific.getDistribution(x));

            TestUtils.assertEquals(general.getQuantile(p), specific.getQuantile(p));

            // BasicLogger.debug("Distribution: gen={} & spec={}", general.getDistribution(x), specific.getDistribution(x));

        }

    }

    /**
     * https://www.itl.nist.gov/div898/handbook/eda/section3/eda3674.htm
     */
    @Test
    public void testTableComparison() {

        ChiSquareDistributionTest.doTestUpperTail(1, new double[] { 2.706, 3.841, 5.024, 6.635, 10.828 });
        ChiSquareDistributionTest.doTestUpperTail(2, new double[] { 4.605, 5.991, 7.378, 9.210, 13.816 });
        ChiSquareDistributionTest.doTestUpperTail(5, new double[] { 9.236, 11.070, 12.833, 15.086, 20.515 });
        ChiSquareDistributionTest.doTestUpperTail(10, new double[] { 15.987, 18.307, 20.483, 23.209, 29.588 });
        ChiSquareDistributionTest.doTestUpperTail(20, new double[] { 28.412, 31.410, 34.170, 37.566, 45.315 });
        ChiSquareDistributionTest.doTestUpperTail(50, new double[] { 63.167, 67.505, 71.420, 76.154, 86.661 });
        ChiSquareDistributionTest.doTestUpperTail(100, new double[] { 118.498, 124.342, 129.561, 135.807, 149.449 });

        ChiSquareDistributionTest.doTestLowerTail(1, new double[] { .016, .004, .001, .000, .000 });
        ChiSquareDistributionTest.doTestLowerTail(2, new double[] { .211, .103, .051, .020, .002 });
        ChiSquareDistributionTest.doTestLowerTail(5, new double[] { 1.610, 1.145, .831, .554, .210 });
        ChiSquareDistributionTest.doTestLowerTail(10, new double[] { 4.865, 3.940, 3.247, 2.558, 1.479 });
        ChiSquareDistributionTest.doTestLowerTail(20, new double[] { 12.443, 10.851, 9.591, 8.260, 5.921 });
        ChiSquareDistributionTest.doTestLowerTail(50, new double[] { 37.689, 34.764, 32.357, 29.707, 24.674 });
        ChiSquareDistributionTest.doTestLowerTail(100, new double[] { 82.358, 77.929, 74.222, 70.065, 61.918 });
    }

}
