/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * The examples in the Javadoc of {@link MatrixR064}, included with {@code {@snippet}}. The code between the
 * region markers is what the Javadoc shows; the assertions after it check that it still does what the
 * comments say.
 */
public class MatrixR064Snippets {

    @Test
    public void testIntroduction() {

        // @start region="introduction"
        MatrixR064 A = MatrixR064.FACTORY.rows(new double[][] { { 4, -2, 1 }, { -2, 4, -2 }, { 1, -2, 4 } });
        MatrixR064 b = MatrixR064.FACTORY.column(11.0, -16.0, 17.0);

        MatrixR064 x = A.solve(b); // [1, -2, 3]

        double determinant = A.getDeterminant(); // 36
        MatrixR064 inverse = A.invert();
        MatrixR064 halved = A.onAll(PrimitiveMath.DIVIDE.by(2.0));
        // @end

        TestUtils.assertEquals(MatrixR064.FACTORY.column(1.0, -2.0, 3.0), x);
        TestUtils.assertEquals(36.0, determinant);
        TestUtils.assertEquals(MatrixR064.FACTORY.makeIdentity(3), inverse.multiply(A));
        TestUtils.assertEquals(2.0, halved.doubleValue(0, 0));
    }

}
