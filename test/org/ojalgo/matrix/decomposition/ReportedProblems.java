/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.TestUtils;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public class ReportedProblems extends AbstractMatrixDecompositionTest {

    public ReportedProblems() {
        super();
    }

    public ReportedProblems(final String arg0) {
        super(arg0);
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition There is no problem...
     */
    public void testP20090923() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        final SingularValue<Double> tmpSVD = SingularValueDecomposition.makePrimitive();
        tmpSVD.compute(tmpA);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("D", tmpSVD.getD(), new NumberContext(7, 6));
            BasicLogger.debug("Q1", tmpSVD.getQ1(), new NumberContext(7, 6));
            BasicLogger.debug("Q2", tmpSVD.getQ2(), new NumberContext(7, 6));
        }

        TestUtils.assertEquals(tmpA, tmpSVD, new NumberContext(7, 6));
    }

    /**
     * Fat matrices were not QR-decomposed correctly ("R" was not created correctly).
     */
    public void testP20091012() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(5, 9));

        final QR<Double> tmpQR = QRDecomposition.makePrimitive();
        tmpQR.compute(tmpA);

        TestUtils.assertEquals(tmpA, tmpQR, new NumberContext(7, 6));
    }

    /**
     * Fat matrices were not QR-decomposed correctly ("R" was not created correctly).
     */
    public void testP20091012fixed() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] {
                { 1.5686711899234411, 5.857030526629094, 2.1798778832593637, 1.4901137152515287, 5.640993583029061 },
                { 4.890945865607895, 4.2576454398997265, 1.0251822439318778, 0.8623492557631138, 5.7457253685285545 },
                { 1.6397137349990025, 0.6795594856277076, 4.7101325736711095, 2.0823473021899517, 2.2159317240940233 } });

        final QR<Double> tmpQR = QRDecomposition.makePrimitive();
        tmpQR.compute(tmpA);

        TestUtils.assertEquals(tmpA, tmpQR, new NumberContext(7, 6));
    }

    public void testP20100512a() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.2845, 0.3597, 0.9544 }, { 0.3597, 0.6887, 0.0782 },
                { 0.9544, 0.0782, 0.1140 } });

        final Eigenvalue<Double> tmpPrimitive = EigenvalueDecomposition.makePrimitive();
        tmpPrimitive.compute(tmpA);

        TestUtils.assertEquals(tmpA, tmpPrimitive, new NumberContext(7, 6));
    }

    public void testP20100512b() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.2845, 0.3597, 0.9544 }, { 0.3597, 0.6887, 0.0782 },
                { 0.9544, 0.0782, 0.1140 } });

        final Eigenvalue<Double> tmpPrimitive = EigenvalueDecomposition.makePrimitive();
        tmpPrimitive.compute(tmpA, false);

        TestUtils.assertEquals(tmpA, tmpPrimitive, new NumberContext(7, 6));
    }

    public void testP20110126() {

        final int tmpDim = 5;

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpI = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final LU<Double> tmpDecomp = LUDecomposition.makePrimitive();

        tmpDecomp.compute(tmpA);

        TestUtils.assertEquals(tmpA, tmpDecomp, new NumberContext(7, 6));

        final MatrixStore<Double> tmpExpected = tmpDecomp.solve(tmpI);

        tmpDecomp.compute(tmpA);
        final MatrixStore<Double> tmpActual = tmpDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));
    }

    /**
     * Peter Abeles reported a problem with ojAlgo his benchmark's invert test. This test case is an attempt to recreate
     * the problem. ... The problem turned out to be a pure bug related to creating the inverse (applied the pivot row
     * order, to the identity matrix, incorrectly).
     */
    public void testP20110223() {

        final NumberContext tmpEqualsNumberContext = new NumberContext(7, 11);

        final int tmpDim = 99;
        final PhysicalStore<Double> tmpRandom = PrimitiveDenseStore.FACTORY.copy(MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpIdentity = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final MatrixDecomposition<Double> tmpRefDecomps = LUDecomposition.makeJama();
        tmpRefDecomps.compute(tmpRandom);
        final MatrixStore<Double> tmpExpected = tmpRefDecomps.getInverse();

        final MatrixDecomposition<Double> tmpTestDecomp = LUDecomposition.makePrimitive();
        tmpTestDecomp.compute(tmpRandom);
        MatrixStore<Double> tmpActual = tmpTestDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiplyLeft(tmpActual), tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiplyRight(tmpActual), tmpEqualsNumberContext);

        tmpTestDecomp.compute(tmpRandom);
        tmpActual = tmpTestDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiplyLeft(tmpActual), tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiplyRight(tmpActual), tmpEqualsNumberContext);
    }

    /**
     * A user reported problems solving complex valued (overdetermined) equation systemes.
     */
    public void testP20111213square() {

        final int tmpDim = Uniform.randomInteger(2, 6);

        final PhysicalStore<ComplexNumber> tmpSquare = MatrixUtils.makeRandomComplexStore(tmpDim, tmpDim);
        final MatrixStore<ComplexNumber> tmpHermitian = tmpSquare.conjugate().multiplyRight(tmpSquare);
        final PhysicalStore<ComplexNumber> tmpExpected = ComplexDenseStore.FACTORY.makeEye(tmpDim, tmpDim);
        MatrixStore<ComplexNumber> tmpActual;

        @SuppressWarnings("unchecked")
        final MatrixDecomposition<ComplexNumber>[] tmpCmplxDecomps = new MatrixDecomposition[] { BidiagonalDecomposition.makeComplex(),
            CholeskyDecomposition.makeComplex(), EigenvalueDecomposition.makeComplex()/*
                                                                                           * , HessenbergDecomposition.
                                                                                           * makeComplex()
                                                                                           */, LUDecomposition.makeComplex(), QRDecomposition.makeComplex(),
                SingularValueDecomposition.makeComplex() /* , TridiagonalDecomposition.makeComplex() */};

        for (final MatrixDecomposition<ComplexNumber> tmpDecomposition : tmpCmplxDecomps) {
            tmpDecomposition.compute(tmpHermitian);
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(tmpDecomposition.toString());
                BasicLogger.debug("Original", tmpHermitian);
                BasicLogger.debug("Recretaed", tmpDecomposition.reconstruct());
            }
            TestUtils.assertEquals("Recreation: " + tmpDecomposition.toString(), tmpHermitian, tmpDecomposition.reconstruct(), new NumberContext(8, 5));
            if (tmpDecomposition.isSolvable()) {
                tmpActual = tmpDecomposition.solve(tmpHermitian);
                if (MatrixDecompositionTests.DEBUG) {
                    BasicLogger.debug("Actual", tmpActual);
                }
                TestUtils.assertEquals("Solving: " + tmpDecomposition.toString(), tmpExpected, tmpActual, new NumberContext(7, 6));
            }
        }
    }

    /**
     * A user reported problems solving complex valued (overdetermined) equation systemes.
     */
    public void testP20111213tall() {

        final int tmpDim = Uniform.randomInteger(2, 6);

        final PhysicalStore<ComplexNumber> tmpTall = MatrixUtils.makeRandomComplexStore(tmpDim + tmpDim, tmpDim);
        final PhysicalStore<ComplexNumber> tmpExpected = ComplexDenseStore.FACTORY.makeEye(tmpDim, tmpDim);
        MatrixStore<ComplexNumber> tmpActual;

        @SuppressWarnings("unchecked")
        final MatrixDecomposition<ComplexNumber>[] tmpCmplxDecomps = new MatrixDecomposition[] { BidiagonalDecomposition.makeComplex()/*
                                                                                                                                       * ,
                                                                                                                                       * LUDecomposition
                                                                                                                                       * .
                                                                                                                                       * makeComplex
                                                                                                                                       * (
                                                                                                                                       * )
                                                                                                                                       */,
                QRDecomposition.makeComplex(), SingularValueDecomposition.makeComplex() };

        for (final MatrixDecomposition<ComplexNumber> tmpDecomposition : tmpCmplxDecomps) {
            tmpDecomposition.compute(tmpTall);
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(tmpDecomposition.toString());
                BasicLogger.debug("Original", tmpTall);
                BasicLogger.debug("Recretaed", tmpDecomposition.reconstruct());
            }
            TestUtils.assertEquals(tmpDecomposition.toString(), tmpTall, tmpDecomposition.reconstruct(), new NumberContext(7, 5));
            if (tmpDecomposition.isSolvable()) {
                tmpActual = tmpDecomposition.solve(tmpTall);
                if (MatrixDecompositionTests.DEBUG) {
                    BasicLogger.debug("Actual", tmpActual);
                }
                TestUtils.assertEquals(tmpDecomposition.toString(), tmpExpected, tmpActual, new NumberContext(7, 6));
            }
        }
    }

}
