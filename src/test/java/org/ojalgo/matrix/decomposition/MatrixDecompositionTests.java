/*
 * Copyright 1997-2021 Optimatika
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
 * @author apete
 */
public abstract class MatrixDecompositionTests {

    static boolean DEBUG = false;

    public static List<MatrixDecomposition<?>> getAnyAll() {

        List<MatrixDecomposition<?>> retVal = new ArrayList<>();

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyCholesky()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyLDL()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyLU()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyQR()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyBidiagonal()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyTridiagonal()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyHessenberg()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnySingularValue()));

        retVal.addAll(MatrixDecompositionTests.getAnyEigenvalue());

        return Collections.unmodifiableList(retVal);
    }

    public static Bidiagonal<?>[] getAnyBidiagonal() {
        return new Bidiagonal<?>[] { new BidiagonalDecomposition.Complex(), new BidiagonalDecomposition.Primitive(), new BidiagonalDecomposition.Quat(),
                new BidiagonalDecomposition.Rational() };
    }

    public static Cholesky<?>[] getAnyCholesky() {
        return new Cholesky<?>[] { new CholeskyDecomposition.Complex(), new CholeskyDecomposition.Primitive(), new CholeskyDecomposition.Quat(),
                new CholeskyDecomposition.Rational(), new RawCholesky() };
    }

    public static List<Eigenvalue<?>> getAnyEigenvalue() {

        List<Eigenvalue<?>> retVal = new ArrayList<>();

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueHermitian()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueGeneral()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueDynamic()));

        return Collections.unmodifiableList(retVal);
    }

    public static Eigenvalue<?>[] getAnyEigenvalueDynamic() {
        return new Eigenvalue<?>[] { new DynamicEvD.Primitive(), new RawEigenvalue.Dynamic() };
    }

    public static Eigenvalue<?>[] getAnyEigenvalueGeneral() {
        return new Eigenvalue<?>[] { new GeneralEvD.Primitive(), new RawEigenvalue.General() };
    }

    public static Eigenvalue<?>[] getAnyEigenvalueHermitian() {
        return new Eigenvalue<?>[] { new HermitianEvD.Primitive(), new RawEigenvalue.Symmetric(), new HermitianEvD.Rational(), new HermitianEvD.Complex(),
                new HermitianEvD.Quat() };
    }

    public static Hessenberg<?>[] getAnyHessenberg() {
        return new Hessenberg<?>[] { new HessenbergDecomposition.Complex(), new HessenbergDecomposition.Primitive(), new HessenbergDecomposition.Quat(),
                new HessenbergDecomposition.Rational() };
    }

    public static LDL<?>[] getAnyLDL() {
        return new LDL<?>[] { new LDLDecomposition.Complex(), new LDLDecomposition.Primitive(), new LDLDecomposition.Quat(), new LDLDecomposition.Rational() };
    }

    public static LU<?>[] getAnyLU() {
        return new LU<?>[] { new LUDecomposition.Complex(), new LUDecomposition.Primitive(), new LUDecomposition.Quat(), new LUDecomposition.Rational(),
                new RawLU() };
    }

    public static List<MatrixDecomposition.RankRevealing<?>> getAnyMatrixDecompositionRankRevealing() {

        List<MatrixDecomposition.RankRevealing<?>> retVal = new ArrayList<>();

        for (MatrixDecomposition<?> decomp : MatrixDecompositionTests.getAnyAll()) {
            if (decomp instanceof MatrixDecomposition.RankRevealing) {
                retVal.add((MatrixDecomposition.RankRevealing<?>) decomp);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public static List<MatrixDecomposition.Solver<?>> getAnyMatrixDecompositionSolver() {

        List<MatrixDecomposition.Solver<?>> retVal = new ArrayList<>();

        for (MatrixDecomposition<?> decomp : MatrixDecompositionTests.getAnyAll()) {
            if (decomp instanceof MatrixDecomposition.Solver) {
                retVal.add((MatrixDecomposition.Solver<?>) decomp);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public static QR<?>[] getAnyQR() {
        return new QR<?>[] { new QRDecomposition.Complex(), new QRDecomposition.Primitive(), new QRDecomposition.Quat(), new QRDecomposition.Rational(),
                new RawQR() };
    }

    public static SingularValue<?>[] getAnySingularValue() {
        return new SingularValue<?>[] { new SingularValueDecomposition.Complex(), new SingularValueDecomposition.Primitive(),
                new SingularValueDecomposition.Quat(), new SingularValueDecomposition.Rational(), new RawSingularValue() };
    }

    public static Tridiagonal<?>[] getAnyTridiagonal() {
        return new Tridiagonal<?>[] { new DeferredTridiagonal.Complex(), new DeferredTridiagonal.Primitive(), new DeferredTridiagonal.Quat(),
                new DeferredTridiagonal.Rational(), new SimultaneousTridiagonal() };
    }

    public static List<MatrixDecomposition<Double>> getPrimitiveAll() {

        List<MatrixDecomposition<Double>> retVal = new ArrayList<>();

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveCholesky()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveLDL()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveLU()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveQR()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveBidiagonal()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveTridiagonal()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveHessenberg()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveSingularValue()));

        retVal.addAll(MatrixDecompositionTests.getPrimitiveEigenvalue());

        return Collections.unmodifiableList(retVal);
    }

    @SuppressWarnings("unchecked")
    public static Bidiagonal<Double>[] getPrimitiveBidiagonal() {
        return (Bidiagonal<Double>[]) new Bidiagonal<?>[] { new BidiagonalDecomposition.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static Cholesky<Double>[] getPrimitiveCholesky() {
        return (Cholesky<Double>[]) new Cholesky<?>[] { new CholeskyDecomposition.Primitive(), new RawCholesky() };
    }

    public static List<Eigenvalue<Double>> getPrimitiveEigenvalue() {

        List<Eigenvalue<Double>> retVal = new ArrayList<>();

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueSymmetric()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueGeneral()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueDynamic()));

        return Collections.unmodifiableList(retVal);
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueDynamic() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new DynamicEvD.Primitive(), new RawEigenvalue.Dynamic() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new GeneralEvD.Primitive(), new RawEigenvalue.General() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new HermitianEvD.Primitive(), new RawEigenvalue.Symmetric() };
    }

    @SuppressWarnings("unchecked")
    public static Hessenberg<Double>[] getPrimitiveHessenberg() {
        return (Hessenberg<Double>[]) new Hessenberg<?>[] { new HessenbergDecomposition.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static LDL<Double>[] getPrimitiveLDL() {
        return (LDL<Double>[]) new LDL<?>[] { new LDLDecomposition.Primitive() };
    }

    @SuppressWarnings("unchecked")
    public static LU<Double>[] getPrimitiveLU() {
        return (LU<Double>[]) new LU<?>[] { new LUDecomposition.Primitive(), new RawLU() };
    }

    public static List<MatrixDecomposition.RankRevealing<Double>> getPrimitiveMatrixDecompositionRankRevealing() {

        List<MatrixDecomposition.RankRevealing<Double>> retVal = new ArrayList<>();

        for (MatrixDecomposition<Double> decomp : MatrixDecompositionTests.getPrimitiveAll()) {
            if (decomp instanceof MatrixDecomposition.RankRevealing) {
                retVal.add((MatrixDecomposition.RankRevealing<Double>) decomp);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    public static List<MatrixDecomposition.Solver<Double>> getPrimitiveMatrixDecompositionSolver() {

        List<MatrixDecomposition.Solver<Double>> retVal = new ArrayList<>();

        for (MatrixDecomposition<Double> decomp : MatrixDecompositionTests.getPrimitiveAll()) {
            if (decomp instanceof MatrixDecomposition.Solver) {
                retVal.add((MatrixDecomposition.Solver<Double>) decomp);
            }
        }

        return Collections.unmodifiableList(retVal);
    }

    @SuppressWarnings("unchecked")
    public static QR<Double>[] getPrimitiveQR() {
        return (QR<Double>[]) new QR<?>[] { new QRDecomposition.Primitive(), new RawQR() };
    }

    @SuppressWarnings("unchecked")
    public static SingularValue<Double>[] getPrimitiveSingularValue() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { new SingularValueDecomposition.Primitive(), new RawSingularValue() };
    }

    @SuppressWarnings("unchecked")
    public static Tridiagonal<Double>[] getPrimitiveTridiagonal() {
        return (Tridiagonal<Double>[]) new Tridiagonal<?>[] { new DeferredTridiagonal.Primitive(), new SimultaneousTridiagonal() };
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

}
