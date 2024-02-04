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

import java.util.function.DoubleUnaryOperator;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.constant.ComplexMath;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.series.PeriodicFunction;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.matrix.MatrixC128;
import org.ojalgo.matrix.MatrixC128.DenseReceiver;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.TransformableRegion;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D.ColumnView;
import org.ojalgo.structure.Access2D.RowView;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D;

/**
 * The discrete Fourier transform (DFT) converts a finite sequence of equally-spaced samples of a function
 * into a same-length sequence of equally-spaced samples of the discrete-time Fourier transform (DTFT), which
 * is a complex-valued function of frequency. The interval at which the DTFT is sampled is the reciprocal of
 * the duration of the input sequence. An inverse DFT is a Fourier series, using the DTFT samples as
 * coefficients of complex sinusoids at the corresponding DTFT frequencies. The DFT is therefore said to be a
 * frequency domain representation of the original input sequence.
 * <p>
 * The fast Fourier transform (FFT) is an algorithm for computing the DFT; it achieves its high speed by
 * storing and reusing results of computations as it progresses.
 * <p>
 * Calling the factory method {@linkplain #newInstance(int)} will return an FFT implementation if the size is
 * a power of 2, and a full matrix implementation otherwise.
 */
public abstract class DiscreteFourierTransform implements DataTransform<Access1D<?>, MatrixStore<ComplexNumber>> {

    public static final class Directive {

        /**
         * Assume input complex (if false, will assume NOT complex and run simpler code).
         */
        public final boolean complex;
        /**
         * Conjugate input before transforming and the output after (before returning). This is how the
         * inverse transform is performed.
         */
        public final boolean conjugate;
        /**
         * Scale the output by the number of elements. Either the transform or the inverse transform needs to
         * be scaled. Usually it's done on the inverse.
         */
        public final boolean scale;

        public Directive(final boolean complex, final boolean conjugate, final boolean scale) {
            super();
            this.complex = complex;
            this.conjugate = conjugate;
            this.scale = scale;
        }

        Directive withComplex(final boolean complex) {
            return new Directive(complex, conjugate, scale);
        }

    }

    static final class FFT extends DiscreteFourierTransform {

        /**
         * Perform the remaining stage calculations (stage>=1). This is essentially "the algorithm".
         */
        private static void doStages(final int nbStages, final ComplexNumber[] roots, final double[] workRe, final double[] workIm) {

            int size = roots.length;

            int nbHalf = 2; // Half the number of lanes per set
            int nbLanesPerSet = 4;
            int nbSets = size / 4;

            int index1, index2;

            for (int stage = 2; stage < nbStages; stage++) {

                nbHalf = nbLanesPerSet;
                nbLanesPerSet *= 2;
                nbSets /= 2;

                for (int set = 0; set < nbSets; set++) {

                    index1 = set * nbLanesPerSet;
                    index2 = index1 + nbHalf;

                    FFT.update(workRe, workIm, index1, index2);

                    for (int lane = 1; lane < nbHalf; lane++) {

                        index1++;
                        index2++;

                        FFT.update(workRe, workIm, index1, index2, roots[lane * nbSets]);
                    }
                }
            }
        }

        private static void setup0(final Access1D<?> input, final boolean complex, final boolean conjugate, final double[] workRe, final double[] workIm) {

            if (complex) {
                ComplexNumber value = ComplexNumber.valueOf(input.get(0));
                if (conjugate) {
                    workRe[0] = value.doubleValue();
                    workIm[0] = -value.i;
                } else {
                    workRe[0] = value.doubleValue();
                    workIm[0] = value.i;
                }
            } else {
                workRe[0] = input.doubleValue(0);
                workIm[0] = PrimitiveMath.ZERO;
            }
        }

        private static void setup0(final double[] input, final double[] workRe, final double[] workIm) {
            workRe[0] = input[0];
            workIm[0] = PrimitiveMath.ZERO;
        }

        private static void setup1(final Access1D<?> input, final boolean complex, final boolean conjugate, final double[] workRe, final double[] workIm) {

            if (complex) {

                ComplexNumber value1 = ComplexNumber.valueOf(input.get(0));
                ComplexNumber value2 = ComplexNumber.valueOf(input.get(1));

                FFT.toWork(0, 1, value1.doubleValue(), conjugate ? -value1.i : value1.i, value2.doubleValue(), conjugate ? -value2.i : value2.i, workRe,
                        workIm);

            } else {

                double re1 = input.doubleValue(0);
                double re2 = input.doubleValue(1);

                workRe[0] = re1 + re2;
                workRe[1] = re1 - re2;

                workIm[0] = PrimitiveMath.ZERO;
                workIm[1] = PrimitiveMath.ZERO;
            }
        }

        private static void setup1(final double[] input, final double[] workRe, final double[] workIm) {

            double re1 = input[0];
            double re2 = input[1];

            workRe[0] = re1 + re2;
            workRe[1] = re1 - re2;

            workIm[0] = PrimitiveMath.ZERO;
            workIm[1] = PrimitiveMath.ZERO;
        }

        private static void setup2(final Access1D<?> input, final boolean complex, final boolean conjugate, final int[] reversed, final double[] workRe,
                final double[] workIm) {

            double re1, re2, re3, re4;
            double re01, re02, re03, re04;

            if (complex) {

                double im1, im2, im3, im4;
                double im01, im02, im03, im04;

                for (int index1 = 0, index2, index3, index4, size = reversed.length; index1 < size; index1 += 4) {
                    index2 = index1 + 1;
                    index3 = index1 + 2;
                    index4 = index1 + 3;

                    ComplexNumber value1 = ComplexNumber.valueOf(input.get(reversed[index1]));
                    ComplexNumber value2 = ComplexNumber.valueOf(input.get(reversed[index2]));
                    ComplexNumber value3 = ComplexNumber.valueOf(input.get(reversed[index3]));
                    ComplexNumber value4 = ComplexNumber.valueOf(input.get(reversed[index4]));

                    if (conjugate) {

                        re1 = value1.doubleValue();
                        im1 = -value1.i;

                        re2 = value2.doubleValue();
                        im2 = -value2.i;

                        re3 = value3.doubleValue();
                        im3 = -value3.i;

                        re4 = value4.doubleValue();
                        im4 = -value4.i;

                    } else {

                        re1 = value1.doubleValue();
                        im1 = value1.i;

                        re2 = value2.doubleValue();
                        im2 = value2.i;

                        re3 = value3.doubleValue();
                        im3 = value3.i;

                        re4 = value4.doubleValue();
                        im4 = value4.i;
                    }

                    re01 = re1 + re2;
                    im01 = im1 + im2;

                    re02 = re1 - re2;
                    im02 = im1 - im2;

                    re03 = re3 + re4;
                    im03 = im3 + im4;

                    re04 = re3 - re4;
                    im04 = im3 - im4;

                    workRe[index1] = re01 + re03;
                    workRe[index2] = re02 + im04;
                    workRe[index3] = re01 - re03;
                    workRe[index4] = re02 - im04;

                    workIm[index1] = im01 + im03;
                    workIm[index2] = im02 - re04;
                    workIm[index3] = im01 - im03;
                    workIm[index4] = im02 + re04;
                }

            } else {

                for (int index1 = 0, index2, index3, index4, size = reversed.length; index1 < size; index1 += 4) {
                    index2 = index1 + 1;
                    index3 = index1 + 2;
                    index4 = index1 + 3;

                    re1 = input.doubleValue(reversed[index1]);
                    re2 = input.doubleValue(reversed[index2]);
                    re3 = input.doubleValue(reversed[index3]);
                    re4 = input.doubleValue(reversed[index4]);

                    re01 = re1 + re2;
                    re02 = re1 - re2;
                    re03 = re3 + re4;
                    re04 = re3 - re4;

                    workRe[index1] = re01 + re03;
                    workRe[index2] = re02;
                    workRe[index3] = re01 - re03;
                    workRe[index4] = re02;

                    workIm[index1] = PrimitiveMath.ZERO;
                    workIm[index2] = -re04;
                    workIm[index3] = PrimitiveMath.ZERO;
                    workIm[index4] = re04;
                }
            }
        }

        private static void setup2(final double[] input, final int[] reversed, final double[] workRe, final double[] workIm) {

            for (int index1 = 0, index2, index3, index4, size = reversed.length; index1 < size; index1 += 4) {
                index2 = index1 + 1;
                index3 = index1 + 2;
                index4 = index1 + 3;

                double re1 = input[reversed[index1]];
                double re2 = input[reversed[index2]];
                double re3 = input[reversed[index3]];
                double re4 = input[reversed[index4]];

                double re01 = re1 + re2;
                double re02 = re1 - re2;
                double re03 = re3 + re4;
                double re04 = re3 - re4;

                workRe[index1] = re01 + re03;
                workRe[index2] = re02;
                workRe[index3] = re01 - re03;
                workRe[index4] = re02;

                workIm[index1] = PrimitiveMath.ZERO;
                workIm[index2] = -re04;
                workIm[index3] = PrimitiveMath.ZERO;
                workIm[index4] = re04;
            }
        }

        /**
         * Copy the results to the output data structure. In the copy-process transformations are performed.
         */
        private static void toOutput(final double[] workRe, final double[] workIm, final boolean conjugate, final boolean scale,
                final Mutate2D.ModifiableReceiver<ComplexNumber> output) {

            int size = workRe.length;

            if (conjugate) {
                if (scale) {
                    double divisor = size;
                    for (int i = 0; i < size; i++) {
                        output.set(i, ComplexNumber.of(workRe[i] / divisor, -workIm[i] / divisor));
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        output.set(i, ComplexNumber.of(workRe[i], -workIm[i]));
                    }
                }
            } else {
                if (scale) {
                    double divisor = size;
                    for (int i = 0; i < size; i++) {
                        output.set(i, ComplexNumber.of(workRe[i] / divisor, workIm[i] / divisor));
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        output.set(i, ComplexNumber.of(workRe[i], workIm[i]));
                    }
                }
            }
        }

        private static void toWork(final int index1, final int index2, final double re1, final double im1, final double re2, final double im2,
                final double[] workRe, final double[] workIm) {

            workRe[index1] = re1 + re2;
            workRe[index2] = re1 - re2;

            workIm[index1] = im1 + im2;
            workIm[index2] = im1 - im2;
        }

        private static void update(final double[] workRe, final double[] workIm, final int index1, final int index2) {
            FFT.toWork(index1, index2, workRe[index1], workIm[index1], workRe[index2], workIm[index2], workRe, workIm);
        }

        private static void update(final double[] workRe, final double[] workIm, final int index1, final int index2, final ComplexNumber scalar) {

            double re2 = workRe[index2];
            double im2 = workIm[index2];

            FFT.toWork(index1, index2, workRe[index1], workIm[index1], scalar.multiplyRe(re2, im2), scalar.multiplyIm(re2, im2), workRe, workIm);
        }

        private final int[] myBitReversedIndices;
        private final int myStages;
        private final ComplexNumber[] myUnitRoots;
        private final double[] myWorkIm;
        private final double[] myWorkRe;

        FFT(final int size) {

            super(size);

            myStages = DiscreteFourierTransform.toPowerOf2Exponent(size);
            if (myStages < 0) {
                throw new IllegalArgumentException();
            }

            myBitReversedIndices = DiscreteFourierTransform.lookupIndices(size);
            myUnitRoots = DiscreteFourierTransform.lookupRoots(size);

            myWorkRe = new double[size];
            myWorkIm = new double[size];
        }

        @Override
        public void transform(final Access1D<?> input, final Directive directive, final Mutate2D.ModifiableReceiver<ComplexNumber> output) {

            if (myStages == 0) {
                FFT.setup0(input, directive.complex, directive.conjugate, myWorkRe, myWorkIm);
            } else if (myStages == 1) {
                FFT.setup1(input, directive.complex, directive.conjugate, myWorkRe, myWorkIm);
            } else if (myStages == 2) {
                FFT.setup2(input, directive.complex, directive.conjugate, myBitReversedIndices, myWorkRe, myWorkIm);
            } else {
                FFT.setup2(input, directive.complex, directive.conjugate, myBitReversedIndices, myWorkRe, myWorkIm);
                FFT.doStages(myStages, myUnitRoots, myWorkRe, myWorkIm);
            }

            FFT.toOutput(myWorkRe, myWorkIm, directive.conjugate, directive.scale, output);
        }

        @Override
        public MatrixStore<ComplexNumber> transform(final double... input) {

            if (myStages == 0) {
                FFT.setup0(input, myWorkRe, myWorkIm);
            } else if (myStages == 1) {
                FFT.setup1(input, myWorkRe, myWorkIm);
            } else if (myStages == 2) {
                FFT.setup2(input, myBitReversedIndices, myWorkRe, myWorkIm);
            } else {
                FFT.setup2(input, myBitReversedIndices, myWorkRe, myWorkIm);
                FFT.doStages(myStages, myUnitRoots, myWorkRe, myWorkIm);
            }

            PhysicalStore<ComplexNumber> output = GenericStore.C128.makeDense(input.length, 1);

            FFT.toOutput(myWorkRe, myWorkIm, DEFAULT.conjugate, DEFAULT.scale, output);

            return output;
        }

    }

    static final class FullMatrix extends DiscreteFourierTransform {

        private final ComplexNumber myDivisor;
        private final PhysicalStore<ComplexNumber> myVandermondeMatrix;

        FullMatrix(final int size) {

            super(size);

            myVandermondeMatrix = GenericStore.C128.make(size, size);
            DiscreteFourierTransform.generate(myVandermondeMatrix);

            myDivisor = ComplexNumber.valueOf(size);
        }

        @Override
        public void transform(final Access1D<?> input, final Directive directive, final Mutate2D.ModifiableReceiver<ComplexNumber> output) {

            MatrixStore<ComplexNumber> column = GenericStore.C128.makeWrapperColumn(input);

            if (directive.conjugate) {
                column = column.onAll(ComplexMath.CONJUGATE);
            }

            myVandermondeMatrix.multiply(column, TransformableRegion.cast(output));

            if (directive.conjugate) {
                output.modifyAll(ComplexMath.CONJUGATE);
            }

            if (directive.scale) {
                output.modifyAll(ComplexMath.DIVIDE.by(myDivisor));
            }
        }

    }

    static final class Single extends DiscreteFourierTransform {

        Single() {
            super(1);
        }

        @Override
        public void transform(final Access1D<?> input, final Directive directive, final Mutate2D.ModifiableReceiver<ComplexNumber> output) {
            output.fillMatching(input);
        }

    }

    private static final int[][] BIT_REVERSED_INDICES = new int[31][];
    private static final ComplexNumber[][] UNIT_ROOTS = new ComplexNumber[31][];

    static final Directive DEFAULT = new Directive(false, false, false);
    static final Directive INVERSE = new Directive(true, true, true);

    public static int[] getBitReversedIndices(final int size) {
        return DiscreteFourierTransform.lookupIndices(size).clone();
    }

    public static Access1D<ComplexNumber> getUnitRoots(final int size) {
        return Access1D.wrap(DiscreteFourierTransform.lookupRoots(size));
    }

    public static MatrixStore<ComplexNumber> inverse2D(final MatrixStore<?> input) {

        PhysicalStore<ComplexNumber> retVal = GenericStore.C128.makeDense(input.getRowDim(), input.getColDim());

        DiscreteFourierTransform.transform2D(input, INVERSE, retVal);

        return retVal;
    }

    /**
     * Will return a functioning instance for any size, but if you want fast transformation (FFT) it needs to
     * be a power of 2.
     */
    public static DiscreteFourierTransform newInstance(final int size) {

        if (size < 1) {
            throw new IllegalArgumentException();
        } else if (size == 1) {
            return new Single();
        } else if (PowerOf2.isPowerOf2(size)) {
            return new FFT(size);
        } else {
            return new FullMatrix(size);
        }
    }

    public static <M extends Mutate2D> M newVandermonde(final Factory2D<M> factory, final int size) {

        M matrix = factory.make(size, size);

        DiscreteFourierTransform.generate(matrix);

        return matrix;
    }

    public static MatrixC128 newVandermondeMatrix(final int size) {

        DenseReceiver receiver = MatrixC128.FACTORY.makeDense(size, size);

        DiscreteFourierTransform.generate(receiver);

        return receiver.get();
    }

    /**
     * Sample, and transform, a function using the Discrete Fourier Transform.
     */
    public static MatrixStore<ComplexNumber> sample(final DoubleUnaryOperator function, final PrimitiveFunction.SampleDomain sampleDomain) {

        double[] input = sampleDomain.arguments();
        for (int i = 0; i < input.length; i++) {
            input[i] = function.applyAsDouble(input[i]);
        }

        return DiscreteFourierTransform.newInstance(input.length).transform(input);
    }

    /**
     * @see #sample(DoubleUnaryOperator, PrimitiveFunction.SampleDomain)
     */
    public static MatrixStore<ComplexNumber> sample(final PeriodicFunction function, final int nbSamples) {
        return DiscreteFourierTransform.sample(function, function.getSampleDomain(nbSamples));
    }

    /**
     * There is a symmetry in the DFT matrix. The first half of the rows (and columns) are the complex
     * conjugates of the second half. Furthermore, the first half correspond to positive frequencies, and the
     * second half to negative frequencies.
     * <p>
     * Re-arranging the elements of a matrix, shifting the first and second halves of the rows (and columns),
     * puts the zero-frequency term in the middle, and the conjugate pairs at equal distances from the centre.
     * <p>
     * This is useful when displaying the 2D DFT matrix as an image.
     * <p>
     * To revert the shift, simply call {@linkplain #shift(MatrixStore)} again. However, this will not work
     * correctly if there's an odd number of rows or columns. In that case the second call will not correctly
     * revert the position of the zero-frequency term – it will end up in the last row/column instead of in
     * the first.
     */
    public static <N extends Comparable<N>> MatrixStore<N> shift(final MatrixStore<N> matrix) {

        MatrixStore<N> retVal = matrix;

        int nbRows = matrix.getRowDim();
        int nbCols = matrix.getColDim();

        if (nbRows > 1) {

            int half = (nbRows + 1) / 2;

            MatrixStore<N> first = retVal.limits(half, -1);
            MatrixStore<N> second = retVal.offsets(half, 0);

            retVal = second.below(first);
        }

        if (nbCols > 1) {

            int half = (nbCols + 1) / 2;

            MatrixStore<N> first = retVal.limits(-1, half);
            MatrixStore<N> second = retVal.offsets(0, half);

            retVal = second.right(first);
        }

        return retVal;
    }

    /**
     * Perform a 2D Discrete Fourier Transform on the input matrix. The output will be a complex matrix of the
     * same size.
     */
    public static MatrixStore<ComplexNumber> transform2D(final MatrixStore<?> input) {

        PhysicalStore<ComplexNumber> retVal = GenericStore.C128.makeDense(input.getRowDim(), input.getColDim());

        DiscreteFourierTransform.transform2D(input, DEFAULT, retVal);

        return retVal;
    }

    public static void transform2D(final MatrixStore<?> input, final Directive directive, final TransformableRegion<ComplexNumber> output) {

        int nbRows = input.getRowDim();
        int nbCols = input.getColDim();

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(nbRows);
        PhysicalStore<ComplexNumber> workOutput = GenericStore.C128.makeDense(nbRows, 1);

        Directive directive1 = directive;
        for (ColumnView<?> view : input.columns()) {
            transformer.transform(view, directive1, workOutput);
            output.fillColumn(view.column(), workOutput);
        }

        if (nbRows != nbCols) {
            transformer = DiscreteFourierTransform.newInstance(nbCols);
            workOutput = GenericStore.C128.makeDense(nbCols, 1);
        }

        Directive directive2 = directive.withComplex(true);
        for (RowView<ComplexNumber> view : output.rows()) {
            transformer.transform(view, directive2, workOutput);
            output.fillRow(view.row(), workOutput);
        }
    }

    private static ComplexNumber[] lookupRootsExponent(final int exponent) {

        if (exponent >= UNIT_ROOTS.length) {

            throw new IllegalArgumentException();

        } else if (UNIT_ROOTS[exponent] != null) {

            return UNIT_ROOTS[exponent];

        } else {

            if (exponent == 0) {

                return UNIT_ROOTS[0] = new ComplexNumber[] { ComplexNumber.ONE };

            } else if (exponent == 1) {

                return UNIT_ROOTS[1] = new ComplexNumber[] { ComplexNumber.ONE, ComplexNumber.NEG };

            } else if (exponent == 2) {

                return UNIT_ROOTS[2] = new ComplexNumber[] { ComplexNumber.ONE, ComplexNumber.N, ComplexNumber.NEG, ComplexNumber.I };

            } else {

                ComplexNumber[] half = DiscreteFourierTransform.lookupRootsExponent(exponent - 1);

                ComplexNumber[] full = UNIT_ROOTS[exponent] = new ComplexNumber[half.length + half.length];

                ComplexNumber[] unitRoots = ComplexNumber.newUnitRoots(full.length);

                for (int i = 0; i < half.length; i++) {
                    int ii = 2 * i;
                    int ii1 = ii + 1;

                    full[ii] = half[i];
                    full[ii1] = unitRoots[ii1];
                }

                return full;
            }
        }
    }

    /**
     * Function to perform bit-reversal on indices
     */
    private static void reverseBits(final int[] indices) {

        int n = indices.length;
        int bits = 31 - Integer.numberOfLeadingZeros(n);

        for (int i = 0; i < n; i++) {
            int reversedIndex = 0;
            for (int j = 0; j < bits; j++) {
                reversedIndex |= (i >> j & 1) << bits - 1 - j;
            }
            // Swap indices[i] with indices[reversedIndex]
            if (reversedIndex > i) {
                int temp = indices[i];
                indices[i] = indices[reversedIndex];
                indices[reversedIndex] = temp;
            }
        }
    }

    static void generate(final Mutate2D matrix) {

        int size = matrix.getMinDim();

        double unitRootPhase = ComplexNumber.newUnitRoot(size).phase();

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                matrix.set(i, j, ComplexNumber.makePolar(PrimitiveMath.ONE, i * j * unitRootPhase));
            }
        }
    }

    static int[] lookupIndices(final int size) {

        int exponent = DiscreteFourierTransform.toPowerOf2Exponent(size);

        int[] retVal = BIT_REVERSED_INDICES[exponent];

        if (retVal == null) {

            BIT_REVERSED_INDICES[exponent] = retVal = Structure1D.newIncreasingRange(0, size);

            DiscreteFourierTransform.reverseBits(retVal);
        }

        return retVal;
    }

    static ComplexNumber[] lookupRoots(final int size) {

        int exponent = DiscreteFourierTransform.toPowerOf2Exponent(size);

        return DiscreteFourierTransform.lookupRootsExponent(exponent);
    }

    static int toPowerOf2Exponent(final int size) {

        int exponent = PowerOf2.find(size);

        if (exponent < 0) {
            throw new IllegalArgumentException("Needs to be power of 2!");
        }

        return exponent;
    }

    private final int mySize;

    DiscreteFourierTransform(final int size) {
        super();
        mySize = size;
    }

    public final void inverse(final Access1D<?> input, final Mutate2D.ModifiableReceiver<ComplexNumber> output) {
        this.transform(input, INVERSE, output);
    }

    public final MatrixStore<ComplexNumber> inverse(final Access1D<ComplexNumber> input) {
        PhysicalStore<ComplexNumber> output = GenericStore.C128.makeDense(input.size(), 1);
        this.transform(input, INVERSE, output);
        return output;
    }

    @Override
    public final MatrixStore<ComplexNumber> transform(final Access1D<?> input) {
        PhysicalStore<ComplexNumber> output = GenericStore.C128.makeDense(input.size(), 1);
        this.transform(input, DEFAULT, output);
        return output;
    }

    public abstract void transform(Access1D<?> input, Directive directive, Mutate2D.ModifiableReceiver<ComplexNumber> output);

    public final void transform(final Access1D<?> input, final Mutate2D.ModifiableReceiver<ComplexNumber> output) {
        this.transform(input, DEFAULT, output);
    }

    public MatrixStore<ComplexNumber> transform(final double... input) {
        PhysicalStore<ComplexNumber> output = GenericStore.C128.makeDense(input.length, 1);
        this.transform(ArrayR064.wrap(input), DEFAULT, output);
        return output;
    }

    int size() {
        return mySize;
    }

}
