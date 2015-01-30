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

/**
 * MatrixDecompositionPackageTests
 *
 * @author apete
 */
public abstract class MatrixDecompositionTests extends FunctionalityTest {

    static final boolean DEBUG = true;

    public static final Bidiagonal<?>[] getBidiagonalAll() {
        return new Bidiagonal<?>[] { new BidiagonalDecomposition.Big(), Bidiagonal.makeComplex(), Bidiagonal.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Bidiagonal<Double>[] getBidiagonalPrimitive() {
        return (Bidiagonal<Double>[]) new Bidiagonal<?>[] { new BidiagonalDecomposition.Primitive() };
    }

    public static final Cholesky<?>[] getCholeskyAll() {
        return new Cholesky<?>[] { Cholesky.makeBig(), Cholesky.makeComplex(), new RawCholesky(), Cholesky.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Cholesky<Double>[] getCholeskyPrimitive() {
        return (Cholesky<Double>[]) new Cholesky<?>[] { new RawCholesky(), new CholeskyDecomposition.Primitive() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllGeneral() {
        return new Eigenvalue<?>[] { new RawEigenvalue.General(), Eigenvalue.makePrimitive() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllNonsymmetric() {
        return new Eigenvalue<?>[] { Eigenvalue.makePrimitive(false) };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllSymmetric() {
        return new Eigenvalue<?>[] { Eigenvalue.makePrimitive(true) };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new RawEigenvalue.General(), new GeneralEvD.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveNonsymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new RawEigenvalue.Nonsymmetric(), new NonsymmetricEvD.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new RawEigenvalue.Symmetric(), new HermitianEvD32.Primitive() };
    }

    public static final Hessenberg<?>[] getHessenbergAll() {
        return new Hessenberg<?>[] { Hessenberg.makeBig(), Hessenberg.makeComplex(), Hessenberg.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Hessenberg<Double>[] getHessenbergPrimitive() {
        return (Hessenberg<Double>[]) new Hessenberg<?>[] { new HessenbergDecomposition.Primitive() };
    }

    public static final LU<?>[] getLUAll() {
        return new LU<?>[] { LU.makeBig(), LU.makeComplex(), new RawLU(), LU.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final LU<Double>[] getLUPrimitive() {
        return (LU<Double>[]) new LU<?>[] { new RawLU(), new LUDecomposition.Primitive() };
    }

    public static final QR<?>[] getQRAll() {
        return new QR<?>[] { QR.makeBig(), QR.makeComplex(), new RawQR(), QR.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final QR<Double>[] getQRPrimitive() {
        return (QR<Double>[]) new QR<?>[] { new RawQR(), new QRDecomposition.Primitive() };
    }

    public static final Schur<?>[] getSchurAll() {
        return new Schur<?>[] { Schur.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Schur<Double>[] getSchurPrimitive() {
        return (Schur<Double>[]) new Schur<?>[] { new SchurDecomposition.Primitive() };
    }

    public static final SingularValue<?>[] getSingularValueAll() {
        return new SingularValue<?>[] { new SVDold30.Big(), new SVDold30.Complex(), new RawSingularValue(), new SVDold30.Primitive(), new SVDnew32.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static final SingularValue<Double>[] getSingularValuePrimitive() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { new RawSingularValue(), /* new SVDold30.Primitive(), */new SVDnew32.Primitive() };
    }

    public static final Tridiagonal<?>[] getTridiagonalAll() {
        return new Tridiagonal<?>[] { Tridiagonal.makeBig(), Tridiagonal.makeComplex(), Tridiagonal.makePrimitive() };
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
        suite.addTestSuite(VerySmallCase.class);
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
