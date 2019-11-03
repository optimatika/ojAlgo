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
package org.ojalgo.matrix;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quaternion;

public class NormTest extends MatrixTests {

    @Test
    public void testCompareComplexNumber() {

        Random rnd = new Random();

        final ComplexNumber cmplx = ComplexNumber.of(rnd.nextDouble(), rnd.nextDouble());

        final double expected = cmplx.norm();

        SingularValue<Double> svd = SingularValue.PRIMITIVE.make(cmplx);
        svd.decompose(cmplx);
        final double actual1 = svd.getOperatorNorm();
        TestUtils.assertEquals(expected, actual1);

        Primitive64Matrix copied = Primitive64Matrix.FACTORY.copy(cmplx);
        final double actual2 = copied.norm();
        TestUtils.assertEquals(expected, actual2);
    }

    @Test
    public void testCompareQuaternion() {

        Random rnd = new Random();

        final Quaternion quat = Quaternion.of(rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble());

        final double expected = quat.norm();

        SingularValue<Double> svd = SingularValue.PRIMITIVE.make(quat);
        svd.decompose(quat);
        final double actual1 = svd.getOperatorNorm();
        TestUtils.assertEquals(expected, actual1);

        Primitive64Matrix copied = Primitive64Matrix.FACTORY.copy(quat);
        final double actual2 = copied.norm();
        TestUtils.assertEquals(expected, actual2);
    }

}
