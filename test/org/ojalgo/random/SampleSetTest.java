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
package org.ojalgo.random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Primitive64Array;

/**
 * SampleSetTest
 *
 * @author apete
 */
public class SampleSetTest {

    @Test
    public void testEmptySet() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        try {

            // The key thing is that all methods return something
            // 0.0, NaN, Inf... doesn't really matter...
            TestUtils.assertEquals(0.0, tmpSampleSet.getFirst());
            TestUtils.assertEquals(0.0, tmpSampleSet.getInterquartileRange());
            TestUtils.assertEquals(0.0, tmpSampleSet.getLargest());
            TestUtils.assertEquals(0.0, tmpSampleSet.getLast());
            TestUtils.assertEquals(0.0, tmpSampleSet.getMaximum());
            TestUtils.assertEquals(Double.NaN, tmpSampleSet.getMean());
            TestUtils.assertEquals(0.0, tmpSampleSet.getMedian());
            TestUtils.assertEquals(0.0, tmpSampleSet.getMinimum());
            TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile1());
            TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile2());
            TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile3());
            TestUtils.assertTrue(Double.isInfinite(tmpSampleSet.getSmallest()));
            TestUtils.assertEquals(0.0, tmpSampleSet.getStandardDeviation());
            TestUtils.assertEquals(0.0, tmpSampleSet.getSumOfSquares());
            TestUtils.assertEquals(0.0, tmpSampleSet.getVariance());

        } catch (final Exception exception) {
            // Important NOT to throw an exception!
            TestUtils.fail(exception.getMessage());
        }
    }

    @Test
    public void testQuartileEx1() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 6, 7, 15, 36, 39, 40, 41, 42, 43, 47, 49 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(20.25, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(40.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(42.75, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileEx2() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 7, 15, 36, 39, 40, 41 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(15.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(37.5, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(40.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize0() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(0.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize1() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(100.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(100.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(100.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize2() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0, 200.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(100.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(150.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(200.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize3() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0, 200.0, 300.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(125.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(200.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(275.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize4() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0, 200.0, 300.0, 400.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(150.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(250.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(350.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize6() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0, 200.0, 300.0, 400.0, 500.0, 600.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(200.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(350.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(500.0, tmpSampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize8() {

        final Primitive64Array tmpSamples = Primitive64Array.wrap(new double[] { 100.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(250.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(450.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(650.0, tmpSampleSet.getQuartile3());

    }

}
