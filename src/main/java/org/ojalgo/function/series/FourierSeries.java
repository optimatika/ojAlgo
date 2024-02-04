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
package org.ojalgo.function.series;

import java.util.function.DoubleUnaryOperator;

import org.ojalgo.data.transform.DiscreteFourierTransform;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.PrimitiveFunction.SampleDomain;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * This is the real coefficient trigonometric form of the Fourier series.
 */
public class FourierSeries implements PrimitiveFunction.Unary {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    public static FourierSeries estimate(final DoubleUnaryOperator function, final PrimitiveFunction.SampleDomain sampleDomain) {

        SampleDomain adjustedDomain = sampleDomain.adjustToPowerOf2();

        MatrixStore<ComplexNumber> transform = DiscreteFourierTransform.sample(function, adjustedDomain);

        int size = transform.size();
        int length = (size - 1) / 2;
        double half = size / 2D;

        double[] cosCoefficients = new double[length];
        double[] sinCoefficients = new double[length];

        double magnitude = PrimitiveMath.ONE;

        ComplexNumber base = transform.get(0).divide(size);
        double baseNorm = base.norm();

        if (!ACCURACY.isSmall(magnitude, baseNorm)) {
            cosCoefficients[0] = base.getReal();
            sinCoefficients[0] = base.getImaginary();
            magnitude = baseNorm;
        }

        for (int i = 1; i < length; i++) {

            ComplexNumber coefficient = transform.get(size - i).divide(half);
            double real = coefficient.getReal();
            double imaginary = coefficient.getImaginary();

            if (!ACCURACY.isSmall(magnitude, real)) {
                cosCoefficients[i] = real;
            }
            if (!ACCURACY.isSmall(magnitude, imaginary)) {
                sinCoefficients[i] = imaginary;
            }
        }

        return new FourierSeries(adjustedDomain.period(), cosCoefficients, sinCoefficients);
    }

    public static FourierSeries estimate(final PeriodicFunction function, final int nbSamples) {
        return FourierSeries.estimate(function, function.getSampleDomain(nbSamples));
    }

    private final double myAngularFrequency;
    private final double[] myCosCoefficients;
    private final double[] mySinCoefficients;

    /**
     * @param period The period of the function
     * @param coefficients The Fourier coefficients. The first coefficient is the constant term (the real part
     *        of that complex number), then the following coefficients are the coefficients of the cos and sin
     *        terms with increasing frequency.
     */
    public FourierSeries(final double period, final ComplexNumber... coefficients) {

        super();

        if (coefficients.length == 0) {
            throw new IllegalArgumentException();
        }

        myAngularFrequency = PrimitiveMath.TWO_PI / period;

        myCosCoefficients = new double[coefficients.length];
        mySinCoefficients = new double[coefficients.length];

        for (int i = 0; i < coefficients.length; i++) {

            ComplexNumber coefficient = coefficients[i];

            if (coefficient != null) {
                myCosCoefficients[i] = coefficient.getReal();
                mySinCoefficients[i] = coefficient.getImaginary();
            }
        }
    }

    FourierSeries(final double period, final double[] cosCoefficients, final double[] sinCoefficients) {

        super();

        myAngularFrequency = PrimitiveMath.TWO_PI / period;

        myCosCoefficients = cosCoefficients;
        mySinCoefficients = sinCoefficients;

        if (myCosCoefficients.length == 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public double invoke(final double arg) {

        double retVal = PrimitiveMath.ZERO;

        for (int i = 0; i < myCosCoefficients.length; i++) {

            double coefficient = myCosCoefficients[i];

            if (Double.isFinite(retVal) && coefficient != PrimitiveMath.ZERO) {
                double a = arg * i * myAngularFrequency;
                retVal += coefficient * Math.cos(a);
            }
        }

        for (int i = 1; i < mySinCoefficients.length; i++) {

            double coefficient = mySinCoefficients[i];

            if (Double.isFinite(retVal) && coefficient != PrimitiveMath.ZERO) {
                double a = arg * i * myAngularFrequency;
                retVal += coefficient * Math.sin(a);
            }
        }

        return retVal;
    }

}
