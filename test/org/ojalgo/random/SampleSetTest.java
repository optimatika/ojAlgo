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
package org.ojalgo.random;

import org.ojalgo.TestUtils;
import org.ojalgo.array.PrimitiveArray;

/**
 * SampleSetTest
 *
 * @author apete
 */
public class SampleSetTest extends RandomTests {

    public SampleSetTest() {
        super();
    }

    public SampleSetTest(final String someName) {
        super(someName);
    }

    public void testQuartileEx1() {

        final PrimitiveArray tmpSamples = PrimitiveArray.wrap(new double[] { 6, 7, 15, 36, 39, 40, 41, 42, 43, 47, 49 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(20.25, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(40.0, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(42.75, tmpSampleSet.getQuartile3());

    }

    public void testQuartileEx2() {

        final PrimitiveArray tmpSamples = PrimitiveArray.wrap(new double[] { 7, 15, 36, 39, 40, 41 });
        final SampleSet tmpSampleSet = SampleSet.wrap(tmpSamples);

        TestUtils.assertEquals(15.0, tmpSampleSet.getQuartile1());
        TestUtils.assertEquals(37.5, tmpSampleSet.getQuartile2());
        TestUtils.assertEquals(40.0, tmpSampleSet.getQuartile3());

    }

}
