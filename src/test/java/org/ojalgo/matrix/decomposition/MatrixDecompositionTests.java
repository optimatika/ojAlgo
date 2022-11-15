/*
 * Copyright 1997-2022 Optimatika
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

    static final boolean DEBUG = false;

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
        return new Bidiagonal<?>[] { new BidiagonalDecomposition.C128(), new BidiagonalDecomposition.R064(), new BidiagonalDecomposition.H256(),
                new BidiagonalDecomposition.Q128() };
    }

    public static Cholesky<?>[] getAnyCholesky() {
        return new Cholesky<?>[] { new CholeskyDecomposition.C128(), new CholeskyDecomposition.R064(), new CholeskyDecomposition.H256(),
                new CholeskyDecomposition.Q128(), new RawCholesky() };
    }

    public static List<Eigenvalue<?>> getAnyEigenvalue() {

        List<Eigenvalue<?>> retVal = new ArrayList<>(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueHermitian()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueGeneral()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getAnyEigenvalueDynamic()));

        return Collections.unmodifiableList(retVal);
    }

    public static Eigenvalue<?>[] getAnyEigenvalueDynamic() {
        return new Eigenvalue<?>[] { new DynamicEvD.R064(), new RawEigenvalue.Dynamic() };
    }

    public static Eigenvalue<?>[] getAnyEigenvalueGeneral() {
        return new Eigenvalue<?>[] { new GeneralEvD.R064(), new RawEigenvalue.General() };
    }

    public static Eigenvalue<?>[] getAnyEigenvalueHermitian() {
        return new Eigenvalue<?>[] { new HermitianEvD.R064(), new RawEigenvalue.Symmetric(), new HermitianEvD.Q128(), new HermitianEvD.C128(),
                new HermitianEvD.H256() };
    }

    public static Hessenberg<?>[] getAnyHessenberg() {
        return new Hessenberg<?>[] { new HessenbergDecomposition.C128(), new HessenbergDecomposition.R064(), new HessenbergDecomposition.H256(),
                new HessenbergDecomposition.Q128() };
    }

    public static LDL<?>[] getAnyLDL() {
        return new LDL<?>[] { new LDLDecomposition.C128(), new LDLDecomposition.R064(), new LDLDecomposition.H256(), new LDLDecomposition.Q128() };
    }

    public static LU<?>[] getAnyLU() {
        return new LU<?>[] { new LUDecomposition.C128(), new LUDecomposition.R064(), new LUDecomposition.H256(), new LUDecomposition.Q128(),
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
        return new QR<?>[] { new QRDecomposition.C128(), new QRDecomposition.R064(), new QRDecomposition.H256(), new QRDecomposition.Q128(),
                new RawQR() };
    }

    public static SingularValue<?>[] getAnySingularValue() {
        return new SingularValue<?>[] { new SingularValueDecomposition.C128(), new SingularValueDecomposition.R064(),
                new SingularValueDecomposition.H256(), new SingularValueDecomposition.Q128(), new RawSingularValue() };
    }

    public static Tridiagonal<?>[] getAnyTridiagonal() {
        return new Tridiagonal<?>[] { new DeferredTridiagonal.C128(), new DeferredTridiagonal.R064(), new DeferredTridiagonal.H256(),
                new DeferredTridiagonal.Q128(), new SimultaneousTridiagonal() };
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
        return (Bidiagonal<Double>[]) new Bidiagonal<?>[] { new BidiagonalDecomposition.R064() };
    }

    @SuppressWarnings("unchecked")
    public static Cholesky<Double>[] getPrimitiveCholesky() {
        return (Cholesky<Double>[]) new Cholesky<?>[] { new CholeskyDecomposition.R064(), new RawCholesky() };
    }

    public static List<Eigenvalue<Double>> getPrimitiveEigenvalue() {

        List<Eigenvalue<Double>> retVal = new ArrayList<>(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueSymmetric()));

        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueGeneral()));
        retVal.addAll(Arrays.asList(MatrixDecompositionTests.getPrimitiveEigenvalueDynamic()));

        return Collections.unmodifiableList(retVal);
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueDynamic() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new DynamicEvD.R064(), new RawEigenvalue.Dynamic() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new GeneralEvD.R064(), new RawEigenvalue.General() };
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getPrimitiveEigenvalueSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[] { new HermitianEvD.R064(), new RawEigenvalue.Symmetric() };
    }

    @SuppressWarnings("unchecked")
    public static Hessenberg<Double>[] getPrimitiveHessenberg() {
        return (Hessenberg<Double>[]) new Hessenberg<?>[] { new HessenbergDecomposition.R064() };
    }

    @SuppressWarnings("unchecked")
    public static LDL<Double>[] getPrimitiveLDL() {
        return (LDL<Double>[]) new LDL<?>[] { new LDLDecomposition.R064() };
    }

    @SuppressWarnings("unchecked")
    public static LU<Double>[] getPrimitiveLU() {
        return (LU<Double>[]) new LU<?>[] { new LUDecomposition.R064(), new RawLU() };
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
        return (QR<Double>[]) new QR<?>[] { new QRDecomposition.R064(), new RawQR() };
    }

    @SuppressWarnings("unchecked")
    public static SingularValue<Double>[] getPrimitiveSingularValue() {
        return (SingularValue<Double>[]) new SingularValue<?>[] { new SingularValueDecomposition.R064(), new RawSingularValue() };
    }

    @SuppressWarnings("unchecked")
    public static Tridiagonal<Double>[] getPrimitiveTridiagonal() {
        return (Tridiagonal<Double>[]) new Tridiagonal<?>[] { new DeferredTridiagonal.R064(), new SimultaneousTridiagonal() };
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

}
