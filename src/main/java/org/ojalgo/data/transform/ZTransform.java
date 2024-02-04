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
package org.ojalgo.data.transform;

import java.util.function.UnaryOperator;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;

/**
 * This class implements the Z-transform for a given complex number z. The transform is applied to a sequence
 * of real numbers, and the output is a complex number. The Z-transform is defined as: Z{x[n]} = ∑x[n]z⁻ⁿ
 * where n is the index of the sequence, x[n] is the value of the sequence at index n, and z is the complex
 * number that the transform is applied to.
 * <p>
 * Choosing the complex number z in the context of the Z-transform is often application-specific and depends
 * on the goals of the analysis or the properties you want to study. There are some common choices and
 * guidelines for specific scenarios:
 * <ol>
 * <li>Unit Circle (|z| = 1): In many applications, especially in stability analysis and frequency response, z
 * is chosen to lie on the unit circle (|z| = 1). This is because the unit circle in the complex plane
 * corresponds to the frequency response of a system, and properties of a system can be analyzed by studying
 * the behavior of the transfer function along the unit circle.
 * <li>Stability Analysis: For stability analysis of discrete-time systems, z is often chosen such that all
 * poles of the system lie inside the unit circle. This ensures that the system is stable.
 * <li>Frequency Analysis: If you are interested in the frequency domain behavior of a system, you might
 * choose z to be a complex exponential, e.g., z = exp(jω) where j is the imaginary unit and ω is the angular
 * frequency.
 * <li>Arbitrary Complex Numbers: In some cases, you might be interested in the Z-transform at an arbitrary
 * complex number z for general analysis. This could be done to study the behavior of the system at a specific
 * point in the complex plane.
 * </ol>
 */
public final class ZTransform implements DataTransform<Access1D<?>, ComplexNumber> {

    static final class ZOperator implements UnaryOperator<ComplexNumber> {

        private final Access1D<?> mySequence;

        ZOperator(final Access1D<?> sequence) {
            super();
            mySequence = sequence;
        }

        @Override
        public ComplexNumber apply(final ComplexNumber z) {
            return ZTransform.doTransform(mySequence, z);
        }

    }

    /**
     * This method computes the discrete Fourier transform (DFT) of a sequence of real numbers. The DFT is
     * computed using the Z-transform with z = exp(jω) where j is the imaginary unit and ω is the angular
     * frequency. The DFT is defined as: X[k] = ∑x[n]exp(-jωkn) where n is the index of the sequence, x[n] is
     * the value of the sequence at index n, and X[k] is the value of the DFT at index k.
     * <p>
     * This method exists primarily for testing purposes. It is most likely better to use
     * {@link DiscreteFourierTransform} to compute the DFT using an FFT algorithm.
     */
    public static MatrixStore<ComplexNumber> doDFT(final Access1D<?> input) {

        UnaryOperator<ComplexNumber> operator = ZTransform.newZOperator(input);

        int size = input.size();

        GenericStore<ComplexNumber> retVal = GenericStore.C128.make(size, 1);

        double angularFrequencyFactor = PrimitiveMath.TWO_PI / size;

        for (int i = 0; i < size; i++) {
            retVal.set(i, 0, operator.apply(ComplexNumber.makePolar(PrimitiveMath.ONE, i * angularFrequencyFactor)));
        }

        return retVal;
    }

    public static UnaryOperator<ComplexNumber> newZOperator(final Access1D<?> sequence) {
        return new ZOperator(sequence);
    }

    public static ZTransform of(final ComplexNumber z) {
        return new ZTransform(z);
    }

    public static ZTransform of(final double angularFrequency) {
        return new ZTransform(ComplexNumber.makePolar(PrimitiveMath.ONE, angularFrequency));
    }

    static ComplexNumber doTransform(final Access1D<?> input, final ComplexNumber z) {

        ComplexNumber retVal = ComplexNumber.ZERO;

        for (int n = 0, N = input.size(); n < N; n++) {
            retVal = retVal.add(z.power(-n).multiply(input.doubleValue(n)));
        }

        return retVal;
    }

    private final double myNormZ;
    private final double myPhaseZ;
    private final ComplexNumber myZ;

    ZTransform(final ComplexNumber z) {

        super();

        myZ = z;
        myNormZ = z.norm();
        myPhaseZ = z.phase();
    }

    /**
     * Input is a sequence of real numbers. Output is a complex number.
     */
    @Override
    public ComplexNumber transform(final Access1D<?> input) {
        return ZTransform.doTransform(input, myZ);

    }

    double getNormZ() {
        return myNormZ;
    }

    double getPhaseZ() {
        return myPhaseZ;
    }

}
