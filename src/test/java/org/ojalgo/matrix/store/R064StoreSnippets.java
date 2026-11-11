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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.LU;

/**
 * The examples in the Javadoc of {@link R064Store}, included with {@code {@snippet}}. The code between the
 * region markers is what the Javadoc shows; the assertions after it check that it still does what the
 * comments say.
 */
public class R064StoreSnippets {

    @SuppressWarnings("unused")
    @Test
    public void testIntroduction() {

        // @start region="introduction"
        PhysicalStore<Double> A = R064Store.FACTORY.rows(new double[][] { { 4, -2, 1 }, { -2, 4, -2 }, { 1, -2, 4 } });
        PhysicalStore<Double> b = R064Store.FACTORY.column(11.0, -16.0, 17.0);

        LU<Double> lu = LU.R064.make(A);

        if (lu.compute(A)) { // false if A is singular
            MatrixStore<Double> x = lu.getSolution(b); // [1, -2, 3]
        }

        A.set(0, 0, 5.0); // mutable: changed in place
        A.modifyAll(PrimitiveMath.DIVIDE.by(2.0));

        PhysicalStore<Double> Ab = R064Store.FACTORY.make(3, 1);
        Ab.fillByMultiplying(A, b); // into existing memory, which can be reused
        // @end

        TestUtils.assertTrue(lu.isSolvable());
        TestUtils.assertEquals(R064Store.FACTORY.column(1.0, -2.0, 3.0), lu.getSolution(b));
        TestUtils.assertEquals(2.5, A.doubleValue(0, 0));
        TestUtils.assertEquals(R064Store.FACTORY.column(52.0, -60.0, 55.5), Ab);
    }

}
