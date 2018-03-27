/*
 * Copyright 1997-2018 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.TestUtils;

/**
 * MatrixDecompositionPackageTests
 *
 * @author apete
 */
public abstract class MatrixDecompositionTests {

    static final boolean DEBUG = false;

    public static List<MatrixDecomposition<Double>> getAllPrimitive() {

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

    public static final Eigenvalue<?>[] getEigenvalueAllDynamic() {
        return new Eigenvalue<?>[] { new DynamicEvD.Primitive(), new RawEigenvalue.Dynamic() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllGeneral() {
        return new Eigenvalue<?>[] { new NewGeneralEvD.Primitive(), new RawEigenvalue.Dynamic(), new OldGeneralEvD.Primitive() };
    }

    public static final Eigenvalue<?>[] getEigenvalueAllHermitian() {
        return new Eigenvalue<?>[] { new HermitianEvD.DeferredPrimitive(), new HermitianEvD.SimultaneousPrimitive(), new RawEigenvalue.Symmetric(),
                new HermitianEvD.Big(), new HermitianEvD.Complex() };
    }

    @SuppressWarnings("unchecked")
    public static final Eigenvalue<Double>[] getEigenvaluePrimitiveDynamic() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new DynamicEvD.Primitive(), new RawEigenvalue.Dynamic() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getEigenvaluePrimitiveGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new NewGeneralEvD.Primitive(), new RawEigenvalue.Dynamic(), new OldGeneralEvD.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getEigenvaluePrimitiveSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new HermitianEvD.DeferredPrimitive(), new HermitianEvD.SimultaneousPrimitive(),
                new RawEigenvalue.Symmetric() };
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
        return new SingularValue<?>[] { new SingularValueDecomposition.Big(), new SingularValueDecomposition.Complex(),
                new SingularValueDecomposition.Primitive(), new RawSingularValue() };
    }

    @SuppressWarnings("unchecked")
    public static final SingularValue<Double>[] getSingularValuePrimitive() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { /*
                                                                   * new SVDold30.Primitive (),
                                                                   */new SingularValueDecomposition.Primitive(), new RawSingularValue() };
    }

    public static final Tridiagonal<?>[] getTridiagonalAll() {
        return new Tridiagonal<?>[] { Tridiagonal.BIG.make(), Tridiagonal.COMPLEX.make(), new DeferredTridiagonal.Primitive(), new SimultaneousTridiagonal() };
    }

    @SuppressWarnings("unchecked")
    public static final Tridiagonal<Double>[] getTridiagonalPrimitive() {
        return (Tridiagonal<Double>[]) new Tridiagonal<?>[] { new DeferredTridiagonal.Primitive(), new SimultaneousTridiagonal() };
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

}
