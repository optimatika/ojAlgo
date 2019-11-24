/*
 * Copyright 1997-2019 Optimatika
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

public class TDistributionTest {

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

        new TDistribution.DegreeInfinity();
        new TDistribution(Integer.MAX_VALUE);

        for (int x = -5; x <= 5; x++) {

            TestUtils.assertEquals("Density(1): " + x, s1.getDensity(x), g1.getDensity(x));
            //TestUtils.assertEquals("Distribution(1): " + x, s1.getDistribution(x), g1.getDistribution(x));

            TestUtils.assertEquals("Density(2): " + x, s2.getDensity(x), g2.getDensity(x));
            //TestUtils.assertEquals("Distribution(2): " + x, s2.getDistribution(x), g2.getDistribution(x));

            TestUtils.assertEquals("Density(3): " + x, s3.getDensity(x), g3.getDensity(x));
            TestUtils.assertEquals("Distribution(3): " + x, s3.getDistribution(x), g3.getDistribution(x));

            TestUtils.assertEquals("Density(4): " + x, s4.getDensity(x), g4.getDensity(x));
            TestUtils.assertEquals("Distribution(4): " + x, s4.getDistribution(x), g4.getDistribution(x));

            //TestUtils.assertEquals("Density(Inf): " + x, sInf.getDensity(x), gInf.getDensity(x));
            //TestUtils.assertEquals("Distribution(Inf): " + x, sInf.getDistribution(x), gInf.getDistribution(x));

        }

    }

}
