/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.scalar;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

public class QuaternionTest extends ScalarTests {

    @Test
    public void testLogExpAndBackAgain() {

        double[] tmpArguments = { PrimitiveMath.NEG, PrimitiveMath.ZERO, PrimitiveMath.ONE };

        for (double s : tmpArguments) {
            for (double i : tmpArguments) {
                for (double j : tmpArguments) {
                    for (double k : tmpArguments) {

                        Quaternion tmpQuaternion = Quaternion.of(s, i, j, k);

                        Quaternion tmpLog = QuaternionMath.LOG.invoke(tmpQuaternion);
                        Quaternion tmpExp = QuaternionMath.EXP.invoke(tmpQuaternion);

                        Quaternion tmpExpLog = QuaternionMath.EXP.invoke(tmpLog);
                        Quaternion tmpLogExp = QuaternionMath.LOG.invoke(tmpExp);

                        TestUtils.assertEquals(tmpQuaternion, tmpExpLog);
                        TestUtils.assertEquals(tmpQuaternion, tmpLogExp);
                    }
                }
            }
        }
    }

    @Test
    public void testPolarForm() {

        double[] tmpArguments = { PrimitiveMath.NEG, PrimitiveMath.ZERO, PrimitiveMath.ONE };

        for (double s : tmpArguments) {
            for (double i : tmpArguments) {
                for (double j : tmpArguments) {
                    for (double k : tmpArguments) {

                        Quaternion tmpExpected = Quaternion.of(s, i, j, k);

                        double tmpNorm = tmpExpected.norm();
                        double[] tmpUnit = tmpExpected.unit();
                        double tmpAngle = tmpExpected.angle();

                        Quaternion tmpActual = Quaternion.makePolar(tmpNorm, tmpUnit, tmpAngle);

                        TestUtils.assertEquals(tmpExpected, tmpActual);
                    }
                }
            }
        }
    }

    @Test
    public void testRandomMultiplication() {

        Quaternion normalizedRandomRotation = Quaternion.of(Math.random(), Math.random(), Math.random(), Math.random()).signum();

        TestUtils.assertEquals(normalizedRandomRotation, normalizedRandomRotation.toMultiplicationMatrix());

        Quaternion randomQuat = Quaternion.of(Math.random(), Math.random(), Math.random());

        Quaternion quatResult = normalizedRandomRotation.multiply(randomQuat);

        MatrixStore<Double> vctrResult = normalizedRandomRotation.toMultiplicationMatrix().multiply(randomQuat.toMultiplicationVector());

        TestUtils.assertEquals(vctrResult, quatResult.toMultiplicationVector());
    }

    @Test
    public void testRandomRotation() {

        Quaternion normalizedRandomRotation = Quaternion.of(Math.random(), Math.random(), Math.random(), Math.random()).signum();

        Quaternion randomPureQuat = Quaternion.of(Math.random(), Math.random(), Math.random());

        Quaternion quatResult = normalizedRandomRotation.multiply(randomPureQuat).multiply(normalizedRandomRotation.conjugate());

        MatrixStore<Double> vctrResult = normalizedRandomRotation.toRotationMatrix().multiply(randomPureQuat.vector());

        TestUtils.assertEquals(vctrResult, quatResult.vector());

        PhysicalStore<Double> vector = randomPureQuat.vector();
        normalizedRandomRotation.transform(vector);

        TestUtils.assertEquals(vctrResult, vector);
    }

    @Test
    public void testRotationMatrixMathWorksExample() {

        double nmbr = 1.0 / Math.sqrt(2.0);

        Quaternion rotQuat = Quaternion.of(nmbr, nmbr, 0.0, 0.0);

        Primitive64Store expected = Primitive64Store.FACTORY.columns(new double[][] { { 1, 0, 0 }, { 0, 0, 1 }, { 0, -1, 0 } });

        MatrixStore<Double> actual = rotQuat.toRotationMatrix();

        TestUtils.assertEquals(expected, actual);

    }

}
