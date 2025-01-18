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
package org.ojalgo.random;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.NumberList;
import org.ojalgo.random.SampleSet.CombineableSet;

/**
 * SampleSetTest
 *
 * @author apete
 */
public class SampleSetTest extends RandomTests {

    @Test
    public void testCollector() {

        double[] array = new double[] { 1, 2, 3, 4, 5 };

        DoubleSummaryStatistics expected = Arrays.stream(array).summaryStatistics();

        SampleSet actual = Arrays.stream(array).boxed().collect(SampleSet.newCollector());

        TestUtils.assertEquals(expected.getAverage(), actual.getMean());
        TestUtils.assertEquals(expected.getCount(), actual.count());
        TestUtils.assertEquals(expected.getMax(), actual.getMaximum());
        TestUtils.assertEquals(expected.getMin(), actual.getMinimum());

        actual = Arrays.stream(array).collect(SampleSet::newCombineableSet, CombineableSet::consume, CombineableSet::combine).getResults();

        TestUtils.assertEquals(expected.getAverage(), actual.getMean());
        TestUtils.assertEquals(expected.getCount(), actual.count());
        TestUtils.assertEquals(expected.getMax(), actual.getMaximum());
        TestUtils.assertEquals(expected.getMin(), actual.getMinimum());
    }

    @Test
    public void testCovariance() {

        SampleSet sampleSet1 = SampleSet.wrap(1, 2, 3, 4, 5);
        SampleSet sampleSet2 = SampleSet.wrap(5, 4, 3, 2, 1);

        TestUtils.assertEquals(2.5, sampleSet1.getVariance());
        TestUtils.assertEquals(2.5, sampleSet2.getVariance());

        TestUtils.assertEquals(-2.5, sampleSet1.getCovariance(sampleSet2));

        TestUtils.assertEquals(-1, sampleSet1.getCorrelation(sampleSet2));

        sampleSet2.swap(-5, -4, -3, -2, -1);

        TestUtils.assertEquals(2.5, sampleSet2.getVariance());

        TestUtils.assertEquals(2.5, sampleSet1.getCovariance(sampleSet2));

        TestUtils.assertEquals(1, sampleSet1.getCorrelation(sampleSet2));
    }

    @Test
    public void testEmptySet() {

        SampleSet sampleSet = SampleSet.wrap(new double[] { });

        try {

            // The key thing is that all methods return something
            // 0.0, NaN, Inf... doesn't really matter...
            TestUtils.assertEquals(0.0, sampleSet.getFirst());
            TestUtils.assertEquals(0.0, sampleSet.getInterquartileRange());
            TestUtils.assertEquals(0.0, sampleSet.getLargest());
            TestUtils.assertEquals(0.0, sampleSet.getLast());
            TestUtils.assertEquals(0.0, sampleSet.getMaximum());
            TestUtils.assertEquals(Double.NaN, sampleSet.getMean());
            TestUtils.assertEquals(0.0, sampleSet.getMedian());
            TestUtils.assertEquals(0.0, sampleSet.getMinimum());
            TestUtils.assertEquals(0.0, sampleSet.getQuartile1());
            TestUtils.assertEquals(0.0, sampleSet.getQuartile2());
            TestUtils.assertEquals(0.0, sampleSet.getQuartile3());
            TestUtils.assertTrue(Double.isInfinite(sampleSet.getSmallest()));
            TestUtils.assertEquals(0.0, sampleSet.getStandardDeviation());
            TestUtils.assertEquals(0.0, sampleSet.getSumOfSquares());
            TestUtils.assertEquals(0.0, sampleSet.getVariance());

        } catch (Exception exception) {
            // Important NOT to throw an exception!
            TestUtils.fail(exception.getMessage());
        }
    }

    @Test
    public void testLargest() {

        SampleSet sampleSet = SampleSet.wrap(1, 2, 3, 4, -5);

        TestUtils.assertEquals(5, sampleSet.getLargest());
    }

    @Test
    public void testMaximum() {

        SampleSet sampleSet = SampleSet.wrap(1, 2, 3, 4, -5);

        TestUtils.assertEquals(4, sampleSet.getMaximum());
    }

    @Test
    public void testMeanValue() {

        NumberList<Double> data = NumberList.factory(ArrayR064.FACTORY).make();
        SampleSet sampleSet = SampleSet.wrap(data);

        double value = 1.004;
        for (int i = 0; i < 100; i++) {
            data.add(value);
        }
        TestUtils.assertEquals(value, sampleSet.getMean(), 1E-14);

        sampleSet.swap(1, 2, 3, 4, 5);

        TestUtils.assertEquals(3, sampleSet.getMean());
    }

    @Test
    public void testMinimum() {

        SampleSet sampleSet = SampleSet.wrap(1, -2, -3, -4, -5);

        TestUtils.assertEquals(-5, sampleSet.getMinimum());
    }

    @Test
    public void testQuartileEx1() {

        SampleSet sampleSet = SampleSet.wrap(6, 7, 15, 36, 39, 40, 41, 42, 43, 47, 49);

        TestUtils.assertEquals(20.25, sampleSet.getQuartile1());
        TestUtils.assertEquals(40.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(42.75, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileEx2() {

        SampleSet sampleSet = SampleSet.wrap(7, 15, 36, 39, 40, 41);

        TestUtils.assertEquals(15.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(37.5, sampleSet.getQuartile2());
        TestUtils.assertEquals(40.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize0() {

        ArrayR064 tmpSamples = ArrayR064.wrap(new double[] { });
        SampleSet sampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(0.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(0.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(0.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize1() {

        SampleSet sampleSet = SampleSet.wrap(100.0);

        TestUtils.assertEquals(100.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(100.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(100.0, sampleSet.getQuartile3());

    }

    @Test
    public void testQuartileSize2() {

        SampleSet sampleSet = SampleSet.wrap(100.0, 200.0);

        TestUtils.assertEquals(100.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(150.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(200.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize3() {

        SampleSet sampleSet = SampleSet.wrap(100.0, 200.0, 300.0);

        TestUtils.assertEquals(125.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(200.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(275.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize4() {

        SampleSet sampleSet = SampleSet.wrap(100.0, 200.0, 300.0, 400.0);

        TestUtils.assertEquals(150.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(250.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(350.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize6() {

        SampleSet sampleSet = SampleSet.wrap(100.0, 200.0, 300.0, 400.0, 500.0, 600.0);

        TestUtils.assertEquals(200.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(350.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(500.0, sampleSet.getQuartile3());
    }

    @Test
    public void testQuartileSize8() {

        SampleSet sampleSet = SampleSet.wrap(100.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0);

        TestUtils.assertEquals(250.0, sampleSet.getQuartile1());
        TestUtils.assertEquals(450.0, sampleSet.getQuartile2());
        TestUtils.assertEquals(650.0, sampleSet.getQuartile3());
    }

    @Test
    public void testSmallest() {

        SampleSet sampleSet = SampleSet.wrap(1, -2, -3, -4, -5);

        TestUtils.assertEquals(1, sampleSet.getSmallest());
    }

    @Test
    public void testStandardDeviation() {

        SampleSet sampleSet = SampleSet.wrap(1, 2, 3, 4, 5);

        TestUtils.assertEquals(1.5811388300841898, sampleSet.getStandardDeviation());
    }

}
