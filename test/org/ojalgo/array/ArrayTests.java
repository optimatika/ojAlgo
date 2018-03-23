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
package org.ojalgo.array;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ojalgo.TestUtils;
import org.ojalgo.random.Uniform;

/**
 * ArrayPackageTests
 *
 * @author apete
 */
public abstract class ArrayTests {

    static void doTestRandomSetAndGetBack(final BasicArray<Double> array, final long expectedCount) {

        TestUtils.assertEquals(expectedCount, array.count());

        final Uniform tmpUniform = new Uniform();

        final Map<Long, Double> pairs = new HashMap<>();

        for (int i = 0; i < 100; i++) {

            final long tmpIndex = Uniform.randomInteger(expectedCount);
            final double tmpValue = tmpUniform.doubleValue();

            array.set(tmpIndex, tmpValue);

            TestUtils.assertEquals(tmpValue, array.doubleValue(tmpIndex));

            pairs.put(tmpIndex, tmpValue);
        }

        for (final Entry<Long, Double> pair : pairs.entrySet()) {
            TestUtils.assertEquals(pair.getValue().doubleValue(), array.doubleValue(pair.getKey().longValue()));
        }

    }

}
