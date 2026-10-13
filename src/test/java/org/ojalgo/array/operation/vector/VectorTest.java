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
package org.ojalgo.array.operation.vector;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.random.Normal;

class VectorTest {

    @Test
    public void testFullScale() {

        ArrayR064 array1 = (ArrayR064) ArrayR064.FACTORY.makeFilled(789, Normal.standard());
        ArrayR064 array2 = (ArrayR064) ArrayR064.FACTORY.copy(array1);

        TestUtils.assertEquals(array1, array2);

        VectorScale.scalePlain(array1.data, 2.0);
        VectorScale.scaleVector(array2.data, 2.0);

        TestUtils.assertEquals(array1, array2);
    }

    @Test
    public void testStridedScale() {

        ArrayR064 array1 = (ArrayR064) ArrayR064.FACTORY.makeFilled(789, Normal.standard());
        ArrayR064 array2 = (ArrayR064) ArrayR064.FACTORY.copy(array1);

        TestUtils.assertEquals(array1, array2);

        VectorScale.scalePlain(array1.data, 2.0, 4, 700, 9);
        VectorScale.scaleVector(array2.data, 2.0, 4, 700, 9);

        TestUtils.assertEquals(array1, array2);
    }

}
