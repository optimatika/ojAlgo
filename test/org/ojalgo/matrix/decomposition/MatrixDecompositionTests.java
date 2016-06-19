/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ojalgo.FunctionalityTest;
import org.ojalgo.TestUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * MatrixDecompositionPackageTests
 *
 * @author apete
 */
public abstract class MatrixDecompositionTests extends FunctionalityTest {

    static final boolean DEBUG = false;

    public static final List<MatrixDecomposition<Double>> getAllPrimitive() {

        final List<MatrixDecomposition<Double>> retVal = new ArrayList<>();

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getCholeskyPrimitive()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getLUPrimitive()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getQRPrimitive()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getSingularValuePrimitive()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getEigenvaluePrimitiveSymmetric()));

        return Collections.unmodifiableList(retVal);
    }

    public static final Bidiagonal<?>[] getBidiagonalAll() {
        return new Bidiagonal<?>[] { new BidiagonalDecomposition.Big(), Bidiagonal.COMPLEX.make(), Bidiagonal.PRIMITIVE.make() };
    }

    @SuppressWarnings("unchecked")
    public static final Bidiagonal<Double>[] getBidiagonalPrimitive() {
        return (Bidiagonal<Double>[]) new Bidiagonal<?>[] { new BidiagonalDecomposition.Primitive() };
    }

    public static final Cholesky<?>[] getCholeskyAll() {
        return new Cholesky<?>[] { Cholesky.BIG.make(), Cholesky.COMPLEX.make(), Cholesky.PRIMITIVE.make(), new RawCholesky() };
    }

    @SuppressWarnings("unchecked")
    public static final Cholesky<Double>[] getCholeskyPrimitive() {
        return (Cholesky<Double>[]) new Cholesky<?>[] { new CholeskyDecomposition.Primitive(), new RawCholesky() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllGeneral() {
        return new Eigenvalue<?>[] { Eigenvalue.PRIMITIVE.make(), new RawEigenvalue.Dynamic() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllNonsymmetric() {
        return new Eigenvalue<?>[] { Eigenvalue.PRIMITIVE.make(false) };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllSymmetric() {
        return new Eigenvalue<?>[] { Eigenvalue.PRIMITIVE.make(true) };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new DynamicEvD.Primitive(), new RawEigenvalue.Dynamic() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveNonsymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new GeneralEvD.Primitive(), new RawEigenvalue.General() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new HermitianEvD.Primitive(), new RawEigenvalue.Symmetric() };
    }

    public static final Hessenberg<?>[] getHessenbergAll() {
        return new Hessenberg<?>[] { Hessenberg.BIG.make(), Hessenberg.COMPLEX.make(), Hessenberg.PRIMITIVE.make() };
    }

    @SuppressWarnings("unchecked")
    public static final Hessenberg<Double>[] getHessenbergPrimitive() {
        return (Hessenberg<Double>[]) new Hessenberg<?>[] { new HessenbergDecomposition.Primitive() };
    }

    public static final LU<?>[] getLUAll() {
        return new LU<?>[] { LU.BIG.make(), LU.COMPLEX.make(), LU.PRIMITIVE.make(), new RawLU() };
    }

    @SuppressWarnings("unchecked")
    public static final LU<Double>[] getLUPrimitive() {
        return (LU<Double>[]) new LU<?>[] { new LUDecomposition.Primitive(), new RawLU() };
    }

    public static final QR<?>[] getQRAll() {
        return new QR<?>[] { QR.BIG.make(), QR.COMPLEX.make(), QR.PRIMITIVE.make(), new RawQR() };
    }

    @SuppressWarnings("unchecked")
    public static final QR<Double>[] getQRPrimitive() {
        return (QR<Double>[]) new QR<?>[] { new QRDecomposition.Primitive(), new RawQR() };
    }

    public static final Schur<?>[] getSchurAll() {
        return new Schur<?>[] { Schur.makePrimitive() };
    }

    @SuppressWarnings("unchecked")
    public static final Schur<Double>[] getSchurPrimitive() {
        return (Schur<Double>[]) new Schur<?>[] { new SchurDecomposition.Primitive() };
    }

    public static final SingularValue<?>[] getSingularValueAll() {
        return new SingularValue<?>[] { new SVDold30.Big(), new SVDold30.Complex(), new SVDold30.Primitive(), new SVDnew32.Primitive(),
                new RawSingularValue() };
    }

    @SuppressWarnings("unchecked")
    public static final SingularValue<Double>[] getSingularValuePrimitive() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { /*
                                                                   * new SVDold30.Primitive (),
                                                                   */new SVDnew32.Primitive(), new RawSingularValue() };
    }

    public static final Tridiagonal<?>[] getTridiagonalAll() {
        return new Tridiagonal<?>[] { Tridiagonal.BIG.make(), Tridiagonal.COMPLEX.make(), Tridiagonal.PRIMITIVE.make() };
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
        suite.addTestSuite(DecompositionProblems.class);
        suite.addTestSuite(DegenerateLUCase.class);
        suite.addTestSuite(DesignCase.class);
        suite.addTestSuite(EigenvalueTest.class);
        suite.addTestSuite(HessenbergTest.class);
        suite.addTestSuite(LDLTest.class);
        suite.addTestSuite(LUTest.class);
        suite.addTestSuite(QRTest.class);
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
