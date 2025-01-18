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
package org.ojalgo.type;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class FloatingPointReceptacleTest {

    @Test
    public void testConcatenations() {

        FloatingPointReceptacle receptacle = FloatingPointReceptacle.of(0, 1, 2, 3);
        receptacle.append(new float[] { 4, 5 });
        receptacle.append(FloatingPointReceptacle.of(6, 7, 8));
        receptacle.append(new double[] { 9 });
        receptacle.append(1, 0F);

        TestUtils.assertEquals(11, receptacle.size());

        double[] dbl = receptacle.toDoubles();
        float[] flt = receptacle.toFloats();

        TestUtils.assertEquals(11, dbl.length);
        TestUtils.assertEquals(11, flt.length);

        for (int i = 0; i < 11; i++) {
            TestUtils.assertEquals(i % 10, dbl[i], 1E-16);
            TestUtils.assertEquals(i % 10, flt[i], 1E-7);
        }

        double[] dbl2 = new double[111];
        receptacle.supplyTo(dbl2);
        for (int i = 0; i < 11; i++) {
            TestUtils.assertEquals(i % 10, dbl2[i], 1E-16);
        }

        double[] dbl3 = new double[1];
        receptacle.supplyTo(dbl3); // Just check no exception

    }

}
