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
package org.ojalgo.random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.context.NumberContext;

public class TDistributionTest {

    private static final NumberContext ACCURACY = StandardType.MATH_032.withPrecision(2).withScale(3);

    static void doTableTest(int degreesOfFreedom, double[] values) {

        double[] probabilities = new double[] { 0.90, 0.95, 0.975, 0.99, 0.995, 0.999, 0.9995 };

        TDistribution distribution = TDistribution.of(degreesOfFreedom);

        for (int i = 0; i < probabilities.length; i++) {
            // BasicLogger.debug("Degree {} ({}): {} <=> {}", degreesOfFreedom, probabilities[i], values[i], distribution.getQuantile(probabilities[i]));
            TestUtils.assertEquals(values[i], distribution.getQuantile(probabilities[i]), ACCURACY);
        }
    }

    @Test
    public void testAgainstSpecificVariants() {

        TDistribution s1 = new TDistribution.Degree1();
        TDistribution g1 = new TDistribution(1);

        TDistribution s2 = new TDistribution.Degree2();
        TDistribution g2 = new TDistribution(2);

        TDistribution s3 = new TDistribution.Degree3();
        TDistribution g3 = new TDistribution(3);

        TDistribution s4 = new TDistribution.Degree4();
        TDistribution g4 = new TDistribution(4);

        TDistribution s5 = new TDistribution.Degree5();
        TDistribution g5 = new TDistribution(5);

        //        TDistribution sInf = new TDistribution.DegreeInfinity();
        //        TDistribution gInf = new TDistribution(Integer.MAX_VALUE);

        for (int x = -5; x <= 5; x++) {

            TestUtils.assertEquals("Density(1): " + x, s1.getDensity(x), g1.getDensity(x));
            TestUtils.assertEquals("Distribution(1): " + x, s1.getDistribution(x), g1.getDistribution(x), ACCURACY);

            TestUtils.assertEquals("Density(2): " + x, s2.getDensity(x), g2.getDensity(x));
            TestUtils.assertEquals("Distribution(2): " + x, s2.getDistribution(x), g2.getDistribution(x), ACCURACY);

            TestUtils.assertEquals("Density(3): " + x, s3.getDensity(x), g3.getDensity(x));
            TestUtils.assertEquals("Distribution(3): " + x, s3.getDistribution(x), g3.getDistribution(x), ACCURACY);

            TestUtils.assertEquals("Density(4): " + x, s4.getDensity(x), g4.getDensity(x));
            TestUtils.assertEquals("Distribution(4): " + x, s4.getDistribution(x), g4.getDistribution(x), ACCURACY);

            TestUtils.assertEquals("Density(5): " + x, s5.getDensity(x), g5.getDensity(x));
            TestUtils.assertEquals("Distribution(5): " + x, s5.getDistribution(x), g5.getDistribution(x), ACCURACY);

            //            TestUtils.assertEquals("Density(Inf): " + x, sInf.getDensity(x), gInf.getDensity(x), ACCURACY);
            //            TestUtils.assertEquals("Distribution(Inf): " + x, sInf.getDistribution(x), gInf.getDistribution(x), ACCURACY);
        }
    }

    /**
     * https://www.statisticshowto.com/tables/inverse-t-distribution-table/
     */
    @Test
    public void testTableComparison() {

        TDistributionTest.doTableTest(1, new double[] { 3.078, 6.314, 12.706, 31.821, 63.656, 318.289, 636.578 });
        TDistributionTest.doTableTest(2, new double[] { 1.886, 2.920, 4.303, 6.965, 9.925, 22.328, 31.600 });
        TDistributionTest.doTableTest(3, new double[] { 1.638, 2.353, 3.182, 4.541, 5.841, 10.214, 12.924 });
        TDistributionTest.doTableTest(4, new double[] { 1.533, 2.132, 2.776, 3.747, 4.604, 7.173, 8.610 });
        TDistributionTest.doTableTest(5, new double[] { 1.476, 2.015, 2.571, 3.365, 4.032, 5.894, 6.869 });
        TDistributionTest.doTableTest(10, new double[] { 1.372, 1.812, 2.228, 2.764, 3.169, 4.144, 4.587 });
        TDistributionTest.doTableTest(Integer.MAX_VALUE, new double[] { 1.282, 1.645, 1.960, 2.326, 2.576, 3.091, 3.291 });
    }

}
