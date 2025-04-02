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
package org.ojalgo.scalar;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.constant.QuaternionMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.Quaternion.RotationAxis;

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
    public void testMakeRotation() {
        // from MATLAB:
        // >> expected = quaternion([0 deg2rad(-20) 0], "rotvec")
        // expected =
        // quaternion
        // 0.98481 + 0i - 0.17365j + 0k

        double angle = Math.toRadians(-20);
        double expectedScalar = 0.98481;
        double expectedImaginary = -0.17365;

        Quaternion actualX = Quaternion.makeRotation(RotationAxis.X, angle);
        TestUtils.assertEquals(expectedScalar, actualX.scalar(), 1e-5);
        TestUtils.assertEquals(expectedImaginary, actualX.i, 1e-5);
        TestUtils.assertEquals(0, actualX.j, 1e-5);
        TestUtils.assertEquals(0, actualX.k, 1e-5);

        Quaternion actualY = Quaternion.makeRotation(RotationAxis.Y, angle);
        TestUtils.assertEquals(expectedScalar, actualY.scalar(), 1e-5);
        TestUtils.assertEquals(0, actualY.i, 1e-5);
        TestUtils.assertEquals(expectedImaginary, actualY.j, 1e-5);
        TestUtils.assertEquals(0, actualY.k, 1e-5);

        Quaternion actualZ = Quaternion.makeRotation(RotationAxis.Z, angle);
        TestUtils.assertEquals(expectedScalar, actualZ.scalar(), 1e-5);
        TestUtils.assertEquals(0, actualZ.i, 1e-5);
        TestUtils.assertEquals(0, actualZ.j, 1e-5);
        TestUtils.assertEquals(expectedImaginary, actualZ.k, 1e-5);
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

        MatrixStore<Double> expected = RawStore.wrap(new double[][] { { 1, 0, 0 }, { 0, 0, 1 }, { 0, -1, 0 } }).transpose();

        MatrixStore<Double> actual = rotQuat.toRotationMatrix();

        TestUtils.assertEquals(expected, actual);

    }

}
