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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ojalgo.FunctionalityTest;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.jama.JamaCholesky;
import org.ojalgo.matrix.jama.JamaEigenvalue;
import org.ojalgo.matrix.jama.JamaLU;
import org.ojalgo.matrix.jama.JamaQR;
import org.ojalgo.matrix.jama.JamaSingularValue;

/**
 * MatrixDecompositionPackageTests
 *
 * @author apete
 */
public abstract class MatrixDecompositionTests extends FunctionalityTest {

    static final boolean DEBUG = true;

    public static final Bidiagonal<?>[] getBidiagonalAll() {
        return new Bidiagonal<?>[] { new BidiagonalDecomposition.Big(), BidiagonalDecomposition.makeComplex(), BidiagonalDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Bidiagonal<Double>[] getBidiagonalPrimitive() {
        return (Bidiagonal<Double>[]) new Bidiagonal<?>[] { new BidiagonalDecomposition.Primitive() };
    }

    public static final Cholesky<?>[] getCholeskyAll() {
        return new Cholesky<?>[] { CholeskyDecomposition.makeBig(), CholeskyDecomposition.makeComplex(), CholeskyDecomposition.makeJama(),
                CholeskyDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Cholesky<Double>[] getCholeskyPrimitive() {
        return (Cholesky<Double>[]) new Cholesky<?>[] { new JamaCholesky(), new CholeskyDecomposition.Primitive() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllGeneral() {
        return new Eigenvalue<?>[] { EigenvalueDecomposition.makeJama(), EigenvalueDecomposition.makePrimitive() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllNonsymmetric() {
        return new Eigenvalue<?>[] { EigenvalueDecomposition.makePrimitive(false) };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllSymmetric() {
        return new Eigenvalue<?>[] { EigenvalueDecomposition.makePrimitive(true) };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new JamaEigenvalue.General(), new GeneralEvD.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveNonsymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new JamaEigenvalue.Nonsymmetric(), new NonsymmetricEvD.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new JamaEigenvalue.Symmetric(), new HermitianEvD32.Primitive() };
    }

    public static final Hessenberg<?>[] getHessenbergAll() {
        return new Hessenberg<?>[] { HessenbergDecomposition.makeBig(), HessenbergDecomposition.makeComplex(), HessenbergDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Hessenberg<Double>[] getHessenbergPrimitive() {
        return (Hessenberg<Double>[]) new Hessenberg<?>[] { new HessenbergDecomposition.Primitive() };
    }

    public static final LU<?>[] getLUAll() {
        return new LU<?>[] { LUDecomposition.makeBig(), LUDecomposition.makeComplex(), LUDecomposition.makeJama(), LUDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final LU<Double>[] getLUPrimitive() {
        return (LU<Double>[]) new LU<?>[] { new JamaLU(), new LUDecomposition.Primitive() };
    }

    public static final QR<?>[] getQRAll() {
        return new QR<?>[] { QRDecomposition.makeBig(), QRDecomposition.makeComplex(), QRDecomposition.makeJama(), QRDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final QR<Double>[] getQRPrimitive() {
        return (QR<Double>[]) new QR<?>[] { new JamaQR(), new QRDecomposition.Primitive() };
    }

    public static final Schur<?>[] getSchurAll() {
        return new Schur<?>[] { SchurDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Schur<Double>[] getSchurPrimitive() {
        return (Schur<Double>[]) new Schur<?>[] { new SchurDecomposition.Primitive() };
    }

    public static final SingularValue<?>[] getSingularValueAll() {
        return new SingularValue<?>[] { new SVDold30.Big(), new SVDold30.Complex(), new JamaSingularValue(), new SVDold30.Primitive(), new SVDnew32.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final SingularValue<Double>[] getSingularValuePrimitive() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { new JamaSingularValue(), /* new SVDold30.Primitive(), */new SVDnew32.Primitive() };
    }

    public static final Tridiagonal<?>[] getTridiagonalAll() {
        return new Tridiagonal<?>[] { TridiagonalDecomposition.makeBig(), TridiagonalDecomposition.makeComplex(), TridiagonalDecomposition.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Tridiagonal<Double>[] getTridiagonalPrimitive() {
        return (Tridiagonal<Double>[]) new Tridiagonal<?>[] { new TridiagonalDecomposition.Primitive(), new TridiagonalAltDecomp() };
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite(MatrixDecompositionTests.class.getPackage().getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(BidiagonalTest.class);
        suite.addTestSuite(CompareJamaAndPrimitive.class);
        suite.addTestSuite(DegenerateLUCase.class);
        suite.addTestSuite(DesignCase.class);
        suite.addTestSuite(EigenvalueTest.class);
        suite.addTestSuite(HessenbergTest.class);
        suite.addTestSuite(LUTest.class);
        suite.addTestSuite(QRTest.class);
        suite.addTestSuite(ReportedProblems.class);
        suite.addTestSuite(SchurTest.class);
        suite.addTestSuite(SingularValueTest.class);
        suite.addTestSuite(SVDbyEvD.class);
        suite.addTestSuite(TestJama.class);
        suite.addTestSuite(TestSolveAndInvert.class);
        suite.addTestSuite(TridiagonalizeCase.class);
        //$JUnit-END$
        return suite;
    }

    protected MatrixDecompositionTests() {
        super();
    }

    protected MatrixDecompositionTests(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        TestUtils.minimiseAllBranchLimits();
    }

}
