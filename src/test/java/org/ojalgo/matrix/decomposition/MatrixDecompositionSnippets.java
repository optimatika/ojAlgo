/*
 * Copyright 1997-2026 Optimatika
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.structure.Access1D;

/**
 * The examples in the Javadoc of the {@link MatrixDecomposition} interfaces – {@link LU}, {@link QR},
 * {@link Cholesky}, {@link SingularValue} and {@link Eigenvalue} – included with {@code {@snippet}}. The code
 * between the region markers is what the Javadoc shows; the assertions after it check that it still does what
 * the comments say.
 */
public class MatrixDecompositionSnippets {

    @SuppressWarnings("unused")
    @Test
    public void testCholesky() {

        // @start region="cholesky"
        MatrixStore<Double> A = R064Store.FACTORY.rows(new double[][] { { 4, -2, 1 }, { -2, 4, -2 }, { 1, -2, 4 } });

        Cholesky<Double> cholesky = Cholesky.R064.make(A);

        if (cholesky.compute(A)) { // false if A is not (numerically) positive definite
            MatrixStore<Double> L = cholesky.getL(); // lower triangular, A = L·L^T
        }
        // @end

        TestUtils.assertTrue(cholesky.isSolvable());
        MatrixStore<Double> L = cholesky.getL();
        TestUtils.assertEquals(A, L.multiply(L.transpose()));
        TestUtils.assertEquals(0.0, L.doubleValue(0, 1));

        MatrixStore<Double> indefinite = R064Store.FACTORY.rows(new double[][] { { 1, 2 }, { 2, 1 } });
        TestUtils.assertFalse(Cholesky.R064.make(indefinite).compute(indefinite));
    }

    @Test
    public void testEigenvalue() {

        // @start region="eigenvalue"
        MatrixStore<Double> S = R064Store.FACTORY.rows(new double[][] { { 2, 1 }, { 1, 2 } });

        Eigenvalue<Double> symmetric = Eigenvalue.R064.make(S, true); // true: S is symmetric (Hermitian)
        symmetric.decompose(S);

        List<Eigenpair> pairs = symmetric.getEigenpairs(); // eigenvalues 3 and 1, in descending order
        ComplexNumber largest = pairs.get(0).value;
        Access1D<ComplexNumber> vector = pairs.get(0).vector; // proportional to [1, 1]

        MatrixStore<Double> R = R064Store.FACTORY.rows(new double[][] { { 0, -1 }, { 1, 0 } }); // a rotation

        Eigenvalue<Double> general = Eigenvalue.R064.make(R, false);
        general.decompose(R);

        Array1D<ComplexNumber> values = general.getEigenvalues(); // i and -i
        // @end

        TestUtils.assertEquals(ComplexNumber.valueOf(3.0), largest);
        TestUtils.assertEquals(ComplexNumber.valueOf(1.0), pairs.get(1).value);
        TestUtils.assertEquals(vector.get(0), vector.get(1));

        TestUtils.assertEquals(0.0, values.get(0).getReal());
        TestUtils.assertEquals(0.0, values.get(1).getReal());
        TestUtils.assertEquals(1.0, Math.abs(values.get(0).getImaginary()));
        TestUtils.assertEquals(0.0, values.get(0).getImaginary() + values.get(1).getImaginary());
    }

    @Test
    public void testLU() {

        // @start region="lu"
        MatrixStore<Double> A = R064Store.FACTORY.rows(new double[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 10 } });

        LU<Double> lu = LU.R064.make(A);
        lu.decompose(A);

        boolean solvable = lu.isSolvable(); // true: square and not singular
        double determinant = lu.getDeterminant(); // -3

        MatrixStore<Double> L = lu.getL(); // unit lower triangular
        MatrixStore<Double> U = lu.getU(); // upper triangular
        int[] order = lu.getPivotOrder(); // L·U is A with its rows in this order
        // @end

        TestUtils.assertTrue(solvable);
        TestUtils.assertEquals(-3.0, determinant);
        TestUtils.assertEquals(A.rows(order), L.multiply(U));
        TestUtils.assertEquals(1.0, L.doubleValue(0, 0));
        TestUtils.assertEquals(0.0, U.doubleValue(1, 0));
    }

    @Test
    public void testQR() {

        // @start region="qr"
        MatrixStore<Double> A = R064Store.FACTORY.rows(new double[][] { { 1, 0 }, { 1, 1 }, { 1, 2 }, { 1, 3 } });
        MatrixStore<Double> b = R064Store.FACTORY.column(1.0, 3.0, 4.0, 7.0);

        QR<Double> qr = QR.R064.make(A);
        qr.decompose(A);

        MatrixStore<Double> x = qr.getSolution(b); // least squares solution [0.9, 1.9]

        MatrixStore<Double> Q = qr.getQ(); // orthonormal columns
        MatrixStore<Double> R = qr.getR(); // upper triangular, A = Q·R
        // @end

        TestUtils.assertEquals(R064Store.FACTORY.column(0.9, 1.9), x);
        TestUtils.assertEquals(A, Q.multiply(R));
        TestUtils.assertEquals(R064Store.FACTORY.makeIdentity(Q.getColDim()), Q.transpose().multiply(Q));
    }

    @Test
    public void testSingularValue() {

        // @start region="svd"
        MatrixStore<Double> A = R064Store.FACTORY.rows(new double[][] { { 1, 2 }, { 2, 4 }, { 3, 6 } }); // rank 1

        SingularValue<Double> svd = SingularValue.R064.make(A);
        svd.decompose(A);

        Array1D<Double> values = svd.getSingularValues(); // descending: sqrt(70) and 0
        int rank = svd.getRank(); // 1
        MatrixStore<Double> pseudoinverse = svd.getInverse(); // Moore-Penrose
        // @end

        TestUtils.assertEquals(Math.sqrt(70.0), values.doubleValue(0));
        TestUtils.assertEquals(0.0, values.doubleValue(1));
        TestUtils.assertEquals(1, rank);
        TestUtils.assertEquals(A, A.multiply(pseudoinverse).multiply(A));
    }

}
