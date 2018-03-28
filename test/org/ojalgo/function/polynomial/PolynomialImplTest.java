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
package org.ojalgo.function.polynomial;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class PolynomialImplTest {

    @Test
    public void testEstimation() {

        final int tmpMaxSamples = 9;

        for (int tmpSamples = 1; tmpSamples <= tmpMaxSamples; tmpSamples++) {

            final Uniform tmpRndm = new Uniform(-100, 200);

            final double[] x = new double[tmpSamples];
            final double[] y = new double[tmpSamples];

            for (int i = 0; i < tmpSamples; i++) {
                x[i] = tmpRndm.doubleValue();
                y[i] = tmpRndm.doubleValue();
            }

            final int tmpDegree = tmpSamples - 1;
            final PrimitivePolynomial tmpPoly = new PrimitivePolynomial(tmpDegree);

            tmpPoly.estimate(Access1D.wrap(x), Access1D.wrap(y));

            final NumberContext tmpEquals = new NumberContext(7, 14);
            for (int i = 0; i < tmpSamples; i++) {
                TestUtils.assertEquals(y[i], tmpPoly.invoke(x[i]), tmpEquals);
            }
        }
    }

    @Test
    public void testEvaluation() {

        final int tmpDegree = 10;

        final PrimitivePolynomial tmpPoly = new PrimitivePolynomial(tmpDegree);

        tmpPoly.set(0, 5.0);

        for (double i = -100.0; i <= 100; i = i + 10.0) {
            TestUtils.assertEquals(5.0, tmpPoly.invoke(i), 1E-14 / PrimitiveMath.THREE);
        }

        tmpPoly.set(1, 1.0);

        for (double i = -100.0; i <= 100; i = i + 10.0) {
            TestUtils.assertEquals(5.0 + i, tmpPoly.invoke(i), 1E-14 / PrimitiveMath.THREE);
        }

        tmpPoly.set(2, 10.0);

        for (double i = -100.0; i <= 100; i = i + 10.0) {
            TestUtils.assertEquals(5.0 + i + (10.0 * (i * i)), tmpPoly.invoke(i), 1E-14 / PrimitiveMath.THREE);
        }
    }

}
