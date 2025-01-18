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
package org.ojalgo.data.transform;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.polynomial.PolynomialR064;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.matrix.MatrixC128;
import org.ojalgo.matrix.MatrixC128.DenseReceiver;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

final class DiscreteFourierTransformTest extends DataTransformTests {

    @Test
    public void testBitReversal() {

        TestUtils.assertEquals(new int[] { 0 }, DiscreteFourierTransform.getBitReversedIndices(1));

        TestUtils.assertEquals(new int[] { 0, 1 }, DiscreteFourierTransform.getBitReversedIndices(2));

        TestUtils.assertEquals(new int[] { 0, 2, 1, 3 }, DiscreteFourierTransform.getBitReversedIndices(4));

        TestUtils.assertEquals(new int[] { 0, 4, 2, 6, 1, 5, 3, 7 }, DiscreteFourierTransform.getBitReversedIndices(8));

        TestUtils.assertEquals(new int[] { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 }, DiscreteFourierTransform.getBitReversedIndices(16));
    }

    @Test
    @Tag("unstable")
    public void testCompareImplementationsUsingRandomInput() {

        NumberContext accuracy = NumberContext.of(8);

        for (int power = 0; power <= 8; power++) {

            int dim = PowerOf2.powerOfInt2(power);

            PhysicalStore<Double> input = R064Store.FACTORY.makeFilled(dim, 1, Uniform.of(-2, 4));

            DiscreteFourierTransform full = new DiscreteFourierTransform.FullMatrix(dim);
            DiscreteFourierTransform fast = new DiscreteFourierTransform.FFT(dim);

            MatrixStore<ComplexNumber> expected = full.transform(input);
            MatrixStore<ComplexNumber> actual = fast.transform(input);

            if (DEBUG) {
                BasicLogger.debugMatrix("Full", expected);
                BasicLogger.debugMatrix("Fast", actual);
            }
            TestUtils.assertComplexEquals(expected, actual, accuracy);

            expected = full.inverse(expected);
            actual = fast.inverse(actual);

            if (DEBUG) {
                BasicLogger.debugMatrix("Input", input);
                BasicLogger.debugMatrix("Full", expected);
                BasicLogger.debugMatrix("Fast", actual);
            }
            TestUtils.assertEquals(input, expected, accuracy);
            TestUtils.assertEquals(input, actual, accuracy);
        }
    }

    /**
     * https://www.youtube.com/watch?v=x3QxJnI9jNI
     */
    @Test
    public void testDrUnderwoodsPhysicsYouTubePage() {

        // y = f(x)
        double[] x = { 0, 2, 4, 6 };
        double[] y = { 1, 4, 3, 2 };

        double dx = 2;
        int N = 4;

        ComplexNumber[] amplitudes = new ComplexNumber[N];
        double[] frequencies = new double[N]; // angular frequencies, p

        double angularIncrement = PrimitiveMath.TWO_PI / N;

        for (int n = 0; n < frequencies.length; n++) {
            frequencies[n] = n * PrimitiveMath.TWO_PI / (N * dx);
        }

        ComplexNumber[] expectedAmplitudes = new ComplexNumber[N];
        double[] expectedAmplitudeNorms = new double[N];
        expectedAmplitudes[0] = ComplexNumber.of(10, 0);
        expectedAmplitudes[1] = ComplexNumber.of(-2, -2);
        expectedAmplitudes[2] = ComplexNumber.of(-2, 0);
        expectedAmplitudes[3] = ComplexNumber.of(-2, 2);
        expectedAmplitudeNorms[0] = 10.0;
        expectedAmplitudeNorms[1] = 2.0 * PrimitiveMath.SQRT_TWO;
        expectedAmplitudeNorms[2] = 2.0;
        expectedAmplitudeNorms[3] = 2.0 * PrimitiveMath.SQRT_TWO;

        // Directly

        for (int n = 0; n < amplitudes.length; n++) {

            ComplexNumber amplitude = ComplexNumber.ZERO;

            for (int r = 0; r < N; r++) {
                amplitude = amplitude.add(ComplexNumber.makePolar(y[r], -angularIncrement * n * r));
            }

            amplitudes[n] = amplitude;
        }

        for (int n = 0; n < amplitudes.length; n++) {
            if (DEBUG) {
                BasicLogger.debug("norm({})={}", amplitudes[n], amplitudes[n].norm());
            }
            TestUtils.assertEquals(expectedAmplitudes[n], amplitudes[n]);
            TestUtils.assertEquals(expectedAmplitudeNorms[n], amplitudes[n].norm());
        }

        // Using ComplexNumber.newUnitRoot(N)

        ComplexNumber w = ComplexNumber.newUnitRoot(N);

        for (int n = 0; n < amplitudes.length; n++) {

            ComplexNumber amplitude = ComplexNumber.ZERO;

            for (int r = 0; r < N; r++) {
                amplitude = amplitude.add(w.power(n * r).multiply(y[r]));
            }

            amplitudes[n] = amplitude;
        }

        for (int n = 0; n < amplitudes.length; n++) {
            if (DEBUG) {
                BasicLogger.debug("norm({})={}", amplitudes[n], amplitudes[n].norm());
            }
            TestUtils.assertEquals(expectedAmplitudes[n], amplitudes[n]);
            TestUtils.assertEquals(expectedAmplitudeNorms[n], amplitudes[n].norm());
        }

        // Using DiscreteFourierTransform

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(y.length);

        MatrixStore<ComplexNumber> actual = transformer.transform(y);

        for (int n = 0; n < amplitudes.length; n++) {
            ComplexNumber ampl = actual.get(n);
            if (DEBUG) {
                BasicLogger.debug("norm({})={}", ampl, ampl.norm());
            }
            TestUtils.assertEquals(expectedAmplitudes[n], ampl);
            TestUtils.assertEquals(expectedAmplitudeNorms[n], ampl.norm());
        }
    }

    /**
     * https://www.youtube.com/watch?v=hsi9-x1Ts4Y
     */
    @Test
    public void testECAcademy() {

        MatrixC128 input = MatrixC128.FACTORY.column(0.0, 1.0, 2.0, 3.0);

        DenseReceiver receiver = MatrixC128.FACTORY.newDenseBuilder(4, 1);
        receiver.set(0, ComplexNumber.of(6, 0));
        receiver.set(1, ComplexNumber.of(-2, 2));
        receiver.set(2, ComplexNumber.of(-2, 0));
        receiver.set(3, ComplexNumber.of(-2, -2));
        MatrixC128 expected = receiver.get();

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(input.size());

        MatrixStore<ComplexNumber> actual = transformer.transform(input);

        if (DEBUG) {
            BasicLogger.debugMatrix("Expected", expected);
            BasicLogger.debugMatrix("Actual", actual);
        }
        TestUtils.assertComplexEquals(expected, actual);

        MatrixStore<ComplexNumber> inverse = transformer.inverse(actual);
        if (DEBUG) {
            BasicLogger.debugMatrix("Input", input);
            BasicLogger.debugMatrix("Inverse", inverse);
        }
        TestUtils.assertComplexEquals(input, inverse);

    }

    /**
     * https://www.youtube.com/watch?v=RHXvrzH0XvA
     */
    @Test
    public void testEnggCourseMadeEasy() {

        MatrixC128 input = MatrixC128.FACTORY.column(1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0);

        DenseReceiver receiver = MatrixC128.FACTORY.newDenseBuilder(8, 1);
        receiver.set(0, ComplexNumber.of(4, 0));
        receiver.set(1, ComplexNumber.of(1, -2.4142135623730945));
        receiver.set(2, ComplexNumber.of(0, 0));
        receiver.set(3, ComplexNumber.of(1, -0.4142135623730945));
        receiver.set(4, ComplexNumber.of(0, 0));
        receiver.set(5, ComplexNumber.of(1, 0.4142135623730945));
        receiver.set(6, ComplexNumber.of(0, 0));
        receiver.set(7, ComplexNumber.of(1, 2.4142135623730945));
        MatrixC128 expected = receiver.get();

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(input.size());

        MatrixStore<ComplexNumber> actual = transformer.transform(input);

        if (DEBUG) {
            BasicLogger.debugMatrix("Expected", expected);
            BasicLogger.debugMatrix("Actual", actual);
        }
        TestUtils.assertComplexEquals(expected, actual);

        MatrixStore<ComplexNumber> inverse = transformer.inverse(actual);
        if (DEBUG) {
            BasicLogger.debugMatrix("Input", input);
            BasicLogger.debugMatrix("Inverse", inverse);
        }
        TestUtils.assertComplexEquals(input, inverse);
    }

    @Test
    public void testGetUnitRoots() {

        for (int e = 9; e >= 0; e--) {

            int powerOf2 = PowerOf2.powerOfInt2(e);

            ComplexNumber[] expected = ComplexNumber.newUnitRoots(powerOf2);
            Access1D<ComplexNumber> actual = DiscreteFourierTransform.getUnitRoots(powerOf2);

            for (int i = 0; i < powerOf2; i++) {
                TestUtils.assertEquals(expected[i], actual.get(i));
            }
        }
    }

    /**
     * Primarily tests there are no obvious problems using dimensions not power of 2.
     */
    @Test
    public void testNonPowerOf2() {

        NumberContext accuracy = NumberContext.of(8);

        for (int dimension = 2; dimension <= 16; dimension++) {

            PhysicalStore<Double> input = R064Store.FACTORY.makeFilled(dimension, 1, Uniform.of(-2, 4));

            DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(dimension);

            MatrixStore<ComplexNumber> transformed = transformer.transform(input);
            MatrixStore<ComplexNumber> reverted = transformer.inverse(transformed);

            if (DEBUG) {
                BasicLogger.debugMatrix("Input", input);
                BasicLogger.debugMatrix("Transformed", transformed);
                BasicLogger.debugMatrix("Reverted", reverted);
            }

            TestUtils.assertEquals(input, reverted, accuracy);
        }
    }

    @Test
    public void testShiftAndRevertEven() {

        for (int c = 2; c < 10; c += 2) {
            for (int r = 2; r < 10; r += 2) {

                R064Store original = R064Store.FACTORY.make(r, c);

                for (int j = 0; j < c; j++) {
                    for (int i = 0; i < r; i++) {
                        original.set(i, j, i + j);
                    }
                }

                if (DEBUG) {
                    BasicLogger.debugMatrix("Original " + r + "x" + c, original);
                }

                MatrixStore<Double> shifted = DiscreteFourierTransform.shift(original);

                if (DEBUG) {
                    BasicLogger.debugMatrix("Shifted " + r + "x" + c, shifted);
                }

                int cr = r / 2;
                int cc = c / 2;

                TestUtils.assertEquals("(" + cr + "," + cc + ")", PrimitiveMath.ZERO, shifted.doubleValue(cr, cc));

                MatrixStore<Double> reverted = DiscreteFourierTransform.shift(shifted);

                if (DEBUG) {
                    BasicLogger.debugMatrix("Reverted " + r + "x" + c, reverted);
                }

                TestUtils.assertEquals(original, reverted);
            }
        }
    }

    @Test
    public void testShiftCenterEvenAndOdd() {

        for (int c = 3; c < 10; c++) {
            for (int r = 3; r < 10; r++) {

                R064Store original = R064Store.FACTORY.make(r, c);

                for (int j = 0; j < c; j++) {
                    for (int i = 0; i < r; i++) {
                        original.set(i, j, i + j);
                    }
                }

                if (DEBUG) {
                    BasicLogger.debugMatrix("Original " + r + "x" + c, original);
                }

                MatrixStore<Double> shifted = DiscreteFourierTransform.shift(original);

                if (DEBUG) {
                    BasicLogger.debugMatrix("Shifted " + r + "x" + c, shifted);
                }

                int cr = r / 2;
                int cc = c / 2;

                TestUtils.assertEquals("(" + cr + "," + cc + ")", PrimitiveMath.ZERO, shifted.doubleValue(cr, cc));
            }
        }
    }

    /**
     * https://www.youtube.com/watch?v=FaWSGmkboOs
     */
    @Test
    public void testSmartEngineer() {

        MatrixC128 input = MatrixC128.FACTORY.column(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);

        DenseReceiver receiver = MatrixC128.FACTORY.newDenseBuilder(8, 1);
        receiver.set(0, ComplexNumber.of(28, 0));
        receiver.set(1, ComplexNumber.of(-4, 9.656854249492376));
        receiver.set(2, ComplexNumber.of(-4, 4));
        receiver.set(3, ComplexNumber.of(-4, 1.656854249492376));
        receiver.set(4, ComplexNumber.of(-4, 0));
        receiver.set(5, ComplexNumber.of(-4, -1.656854249492376));
        receiver.set(6, ComplexNumber.of(-4, -4));
        receiver.set(7, ComplexNumber.of(-4, -9.656854249492376));
        MatrixC128 expected = receiver.get();

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(input.size());

        MatrixStore<ComplexNumber> actual = transformer.transform(input);

        if (DEBUG) {
            BasicLogger.debugMatrix("Expected", expected);
            BasicLogger.debugMatrix("Actual", actual);
        }
        TestUtils.assertComplexEquals(expected, actual);

        MatrixStore<ComplexNumber> inverse = transformer.inverse(actual);
        if (DEBUG) {
            BasicLogger.debugMatrix("Input", input);
            BasicLogger.debugMatrix("Inverse", inverse);
        }
        TestUtils.assertComplexEquals(input, inverse);
    }

    /**
     * https://cs.stackexchange.com/questions/16266/show-how-to-do-fft-by-hand
     */
    @Test
    public void testStackExchangePolynomialMultiplication() {

        PolynomialR064 polynomialA = PolynomialR064.wrap(3, 1, 0, 0);
        PolynomialR064 polynomialB = PolynomialR064.wrap(2, 0, 2, 0);
        PolynomialR064 polynomialC = PolynomialR064.wrap(6, 2, 6, 2);

        TestUtils.assertEquals(polynomialC, polynomialA.multiply(polynomialB));

        ComplexNumber unitRoot = ComplexNumber.newUnitRoot(4);
        double unitRootPhase = unitRoot.phase();

        DenseReceiver receiver = MatrixC128.FACTORY.newDenseBuilder(4, 4);
        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                receiver.set(i, j, ComplexNumber.makePolar(PrimitiveMath.ONE, i * j * unitRootPhase));
            }
        }
        MatrixC128 matrix = receiver.get();

        if (DEBUG) {
            BasicLogger.debugMatrix("Transformation matrix", matrix);
        }
        TestUtils.assertComplexEquals(matrix, DiscreteFourierTransform.newVandermondeMatrix(4));

        DiscreteFourierTransform transform = DiscreteFourierTransform.newInstance(4);

        MatrixC128 columnA = MatrixC128.FACTORY.column(polynomialA);
        MatrixC128 transfA = matrix.multiply(columnA);
        MatrixC128 columnB = MatrixC128.FACTORY.column(polynomialB);
        MatrixC128 transfB = matrix.multiply(columnB);

        MatrixC128 transfC = transfA.onMatching(ComplexMath.MULTIPLY, transfB);

        if (DEBUG) {
            BasicLogger.debugMatrix("Transformed A", transfA);
            BasicLogger.debugMatrix("Transformed B", transfB);
            BasicLogger.debugMatrix("Transformed C", transfC);
        }
        TestUtils.assertComplexEquals(transfA, transform.transform(columnA));
        TestUtils.assertComplexEquals(transfB, transform.transform(columnB));

        MatrixC128 inverse = matrix.invert();

        if (DEBUG) {
            BasicLogger.debugMatrix("Inverse transformation", inverse);
        }

        MatrixC128 columnC = inverse.multiply(transfC);

        if (DEBUG) {
            BasicLogger.debugMatrix("Column C", columnC);
        }
        TestUtils.assertEquals(polynomialC, columnC);
        TestUtils.assertEquals(polynomialC, transform.inverse(transfC));
    }

}
