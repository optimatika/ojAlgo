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

    @SuppressWarnings("unchecked")
    public static Cholesky<Double>[] getCholeskyPrimitive() {
        return (Cholesky<Double>[]) new Cholesky<?>[]{new CholeskyDecomposition.Primitive(), new RawCholesky()};
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getEigenvaluePrimitiveGeneral() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[]{new NewGeneralEvD.Primitive(), new RawEigenvalue.Dynamic(), new OldGeneralEvD.Primitive()};
    }

    @SuppressWarnings("unchecked")
    public static Eigenvalue<Double>[] getEigenvaluePrimitiveSymmetric() {
        return (Eigenvalue<Double>[]) new Eigenvalue<?>[]{new HermitianEvD.DeferredPrimitive(), new HermitianEvD.SimultaneousPrimitive(),
                new RawEigenvalue.Symmetric()};
    }

    @SuppressWarnings("unchecked")
    public static LU<Double>[] getLUPrimitive() {
        return (LU<Double>[]) new LU<?>[]{new LUDecomposition.Primitive(), new RawLU()};
    }

    @SuppressWarnings("unchecked")
    private static QR<Double>[] getQRPrimitive() {
        return (QR<Double>[]) new QR<?>[]{new QRDecomposition.Primitive(), new RawQR()};
    }

    @SuppressWarnings("unchecked")
    public static SingularValue<Double>[] getSingularValuePrimitive() {
        return (SingularValue<Double>[]) new SingularValue<?>[]{ /*
                                                                   * new SVDold30.Primitive (),
                                                                   */new SingularValueDecomposition.Primitive(), new RawSingularValue()};
    }

}
