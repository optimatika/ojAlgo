/*
 * Copyright 1997-2019 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Solver;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public class DecompositionProblems extends MatrixDecompositionTests {

    private static final NumberContext TOP_ACCURACY = new NumberContext(14, 14);
    private static final double ZERO = 1e-9; // tolerance for zero checks

    /**
     * Perform a QR decomposition of a matrix with the full size flag set to true. In order to find a basis
     * for the null space, we decompose the transpose of the original matrix.
     *
     * @param matrix the matrix to decompose
     */
    static void fullQR(final MatrixStore<Double> matrix) {

        // Initialize the QR decomposition object.
        QR<Double> qr = QR.PRIMITIVE.make(matrix.transpose(), true);
        // Set it to full size and confirm the setting.
        TestUtils.assertTrue("Full size flag is set to ", qr.isFullSize());

        // Perform the decomposition.
        if (!qr.decompose(matrix.transpose())) {
            TestUtils.fail("Decomposition failed?");
        }
        // Check the rank.
        int rank = qr.getRank();
        TestUtils.assertEquals(4, rank);
        int nullity = (int) matrix.countColumns() - rank;
        if (DEBUG) {
            System.out.println(String.format("The original matrix has rank %d and" + " nullity %d.", rank, nullity));
        }

        // Recover the Q matrix.
        MatrixStore<Double> q = qr.getQ();
        if (DEBUG) {
            System.out.println("Q matrix:\n" + q);
        }

        // Check whether Q has the correct number of columns.
        int rows = (int) q.countRows();
        int cols = (int) q.countColumns();
        TestUtils.assertEquals(String.format("In a full decomposition, Q should be " + "7 x 7, but here is %d by %d.", rows, cols), matrix.countColumns(),
                cols);

        // Detect which, if any, of the columns of Q are in the null space of A.
        MatrixStore<Double> product = matrix.multiply(q);
        int kernelCount = 0;
        for (int i = 0; i < product.countColumns(); i++) {
            double norm = PrimitiveDenseStore.FACTORY.columns(product.sliceColumn(i)).norm();
            if (norm < ZERO) {
                if (DEBUG) {
                    System.out.println(String.format("Column %d of Q belongs to the " + "kernel of A.", i));
                }
                kernelCount += 1;
            }
        }
        TestUtils.assertEquals(String.format("Expected nullity %d, found %d kernel" + " vectors.", nullity, kernelCount), nullity, kernelCount);
    }

    /**
     * Perform a singular value decomposition of a matrix with the full size flag set to true.
     *
     * @param matrix the matrix to decompose
     */
    static void fullSVD(final MatrixStore<Double> matrix) {
        // Initialize the SVD object.
        SingularValue<Double> svd = SingularValue.PRIMITIVE.make(matrix, true);
        // Set it to full size and confirm the setting.
        TestUtils.assertTrue("Full size flag is set to ", svd.isFullSize());
        // Perform the decomposition.
        if (!svd.decompose(matrix)) {
            TestUtils.fail("Decomposition failed?");
        }
        // Check the rank.
        int rank = svd.getRank();
        TestUtils.assertEquals(4, rank);
        int nullity = (int) matrix.countColumns() - rank;
        if (DEBUG) {
            System.out.println(String.format("The original matrix has rank %d and" + " nullity %d.", rank, nullity));
        }

        // Recover the Q2 matrix.
        MatrixStore<Double> q2 = svd.getQ2();
        if (DEBUG) {
            System.out.println("Q2 matrix:\n" + q2);
        }

        // Check whether Q2 has the correct number of columns.
        int rows = (int) q2.countRows();
        int cols = (int) q2.countColumns();
        TestUtils.assertEquals(String.format("In a full decomposition, Q should be " + "7 x 7, but here is %d by %d.", rows, cols), matrix.countColumns(),
                cols);

        // Detect which, if any, of the columns of Q2 are in the null space of A.
        MatrixStore<Double> product = matrix.multiply(q2);
        int kernelCount = 0;
        for (int i = 0; i < product.countColumns(); i++) {
            double norm = PrimitiveDenseStore.FACTORY.columns(product.sliceColumn(i)).norm();
            if (norm < ZERO) {
                if (DEBUG) {
                    System.out.println(String.format("Column %d of Q2 belongs to the " + "kernel of A.", i));
                }
                kernelCount += 1;
            }
        }
        TestUtils.assertEquals(String.format("Expected nullity %d, found %d kernel" + " vectors.", nullity, kernelCount), nullity, kernelCount);
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    /**
     * http://en.wikipedia.org/wiki/Singular_value_decomposition There is no problem...
     */
    @Test
    public void testP20090923() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 2.0 }, { 0.0, 0.0, 3.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 4.0, 0.0, 0.0, 0.0 } });

        final SingularValue<Double> tmpSVD = SingularValue.PRIMITIVE.make();
        tmpSVD.decompose(tmpA);

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
    @Test
    public void testP20091012() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.copy(TestUtils.makeRandomComplexStore(5, 9));

        final QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.decompose(tmpA);

        TestUtils.assertEquals(tmpA, tmpQR, new NumberContext(7, 6));
    }

    /**
     * Fat matrices were not QR-decomposed correctly ("R" was not created correctly).
     */
    @Test
    public void testP20091012fixed() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.5686711899234411, 5.857030526629094, 2.1798778832593637, 1.4901137152515287, 5.640993583029061 },
                        { 4.890945865607895, 4.2576454398997265, 1.0251822439318778, 0.8623492557631138, 5.7457253685285545 },
                        { 1.6397137349990025, 0.6795594856277076, 4.7101325736711095, 2.0823473021899517, 2.2159317240940233 } });

        final QR<Double> tmpQR = QR.PRIMITIVE.make(tmpA);
        tmpQR.decompose(tmpA);

        TestUtils.assertEquals(tmpA, tmpQR, new NumberContext(7, 6));
    }

    @Test
    public void testP20100512() {

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 0.2845, 0.3597, 0.9544 }, { 0.3597, 0.6887, 0.0782 }, { 0.9544, 0.0782, 0.1140 } });

        final Eigenvalue<Double> tmpPrimitive = Eigenvalue.PRIMITIVE.make();
        tmpPrimitive.decompose(tmpA);

        TestUtils.assertEquals(tmpA, tmpPrimitive, TOP_ACCURACY);
    }

    @Test
    public void testP20110126() {

        final int tmpDim = 5;

        final PhysicalStore<Double> tmpA = PrimitiveDenseStore.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpI = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final LU<Double> tmpDecomp = LU.PRIMITIVE.make();

        tmpDecomp.decompose(tmpA);

        TestUtils.assertEquals(tmpA, tmpDecomp, new NumberContext(7, 6));

        final MatrixStore<Double> tmpExpected = tmpDecomp.getSolution(tmpI);

        tmpDecomp.decompose(tmpA);
        final MatrixStore<Double> tmpActual = tmpDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, new NumberContext(7, 6));
    }

    /**
     * Peter Abeles reported a problem with ojAlgo his benchmark's invert test. This test case is an attempt
     * to recreate the problem. ... The problem turned out to be a pure bug related to creating the inverse
     * (applied the pivot row order, to the identity matrix, incorrectly).
     */
    @Test
    public void testP20110223() {

        final NumberContext tmpEqualsNumberContext = new NumberContext(7, 11);

        final int tmpDim = 99;
        final PhysicalStore<Double> tmpRandom = PrimitiveDenseStore.FACTORY.copy(TestUtils.makeRandomComplexStore(tmpDim, tmpDim));
        final PhysicalStore<Double> tmpIdentity = PrimitiveDenseStore.FACTORY.makeEye(tmpDim, tmpDim);

        final LU<Double> tmpRefDecomps = new RawLU();
        tmpRefDecomps.decompose(tmpRandom);
        final MatrixStore<Double> tmpExpected = tmpRefDecomps.getInverse();

        final LU<Double> tmpTestDecomp = LU.PRIMITIVE.make();
        tmpTestDecomp.decompose(tmpRandom);
        MatrixStore<Double> tmpActual = tmpTestDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, tmpEqualsNumberContext);
        final MatrixStore<Double> left = tmpActual;
        TestUtils.assertEquals(tmpIdentity, left.multiply(tmpRandom), tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiply(tmpActual), tmpEqualsNumberContext);

        tmpTestDecomp.decompose(tmpRandom);
        tmpActual = tmpTestDecomp.getInverse();

        TestUtils.assertEquals(tmpExpected, tmpActual, tmpEqualsNumberContext);
        final MatrixStore<Double> left1 = tmpActual;
        TestUtils.assertEquals(tmpIdentity, left1.multiply(tmpRandom), tmpEqualsNumberContext);
        TestUtils.assertEquals(tmpIdentity, tmpRandom.multiply(tmpActual), tmpEqualsNumberContext);
    }

    /**
     * A user reported problems solving complex valued (overdetermined) equation systemes.
     */
    @Test
    @Tag("unstable")
    public void testP20111213square() {

        final int tmpDim = Uniform.randomInteger(2, 6);

        final PhysicalStore<ComplexNumber> tmpSquare = TestUtils.makeRandomComplexStore(tmpDim, tmpDim);
        final MatrixStore<ComplexNumber> tmpHermitian = tmpSquare.conjugate().multiply(tmpSquare);
        final PhysicalStore<ComplexNumber> tmpExpected = GenericDenseStore.COMPLEX.makeEye(tmpDim, tmpDim);
        MatrixStore<ComplexNumber> tmpActual;

        @SuppressWarnings("unchecked")
        final MatrixDecomposition<ComplexNumber>[] tmpCmplxDecomps = new MatrixDecomposition[] { Bidiagonal.COMPLEX.make(), Cholesky.COMPLEX.make(),
                Eigenvalue.COMPLEX.make(MatrixDecomposition.TYPICAL,
                        true)/*
                              * , HessenbergDecomposition. makeComplex()
                              */,
                LU.COMPLEX.make(), QR.COMPLEX.make(),
                SingularValue.COMPLEX.make() /*
                                              * , TridiagonalDecomposition . makeComplex ( )
                                              */ };

        for (final MatrixDecomposition<ComplexNumber> tmpDecomposition : tmpCmplxDecomps) {
            tmpDecomposition.decompose(tmpHermitian);
            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(tmpDecomposition.toString());
                BasicLogger.debug("Original", tmpHermitian);
                BasicLogger.debug("Recretaed", tmpDecomposition.reconstruct());
            }
            TestUtils.assertEquals("Recreation: " + tmpDecomposition.toString(), tmpHermitian, tmpDecomposition.reconstruct(), new NumberContext(8, 5));
            if ((tmpDecomposition instanceof MatrixDecomposition.Solver<?>) && ((Solver) tmpDecomposition).isSolvable()) {
                tmpActual = ((Solver) tmpDecomposition).getSolution(tmpHermitian);
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
    @Test
    public void testP20111213tall() {

        final int tmpDim = Uniform.randomInteger(2, 6);

        final PhysicalStore<ComplexNumber> original = TestUtils.makeRandomComplexStore(tmpDim + tmpDim, tmpDim);
        final PhysicalStore<ComplexNumber> identity = GenericDenseStore.COMPLEX.makeEye(tmpDim, tmpDim);
        MatrixStore<ComplexNumber> solution;

        @SuppressWarnings("unchecked")
        final MatrixDecomposition<ComplexNumber>[] tmpCmplxDecomps = new MatrixDecomposition[] { QR.COMPLEX.make(), SingularValue.COMPLEX.make(),
                Bidiagonal.COMPLEX.make() };

        for (final MatrixDecomposition<ComplexNumber> decomp : tmpCmplxDecomps) {

            decomp.decompose(original);

            if (MatrixDecompositionTests.DEBUG) {
                BasicLogger.debug(decomp.toString());
                BasicLogger.debug("Original", original);
                BasicLogger.debug("Recretaed", decomp.reconstruct());
            }
            TestUtils.assertEquals(decomp.toString(), original, decomp.reconstruct(), new NumberContext(7, 5));

            if ((decomp instanceof MatrixDecomposition.Solver<?>) && ((Solver<ComplexNumber>) decomp).isSolvable()) {

                solution = ((Solver<ComplexNumber>) decomp).getSolution(original);
                if (MatrixDecompositionTests.DEBUG) {
                    BasicLogger.debug("Actual", solution);
                }
                TestUtils.assertEquals(decomp.toString(), identity, solution, new NumberContext(7, 6));
            }
        }
    }

    /**
     * A user reported problems related to calculating the pseudoinverse for large (2000x2000) matrices.
     */
    @Test
    @Tag("slow")
    public void testP20160419() {

        final PrimitiveDenseStore tmpOrg = PrimitiveDenseStore.FACTORY.makeFilled(2000, 2000, new Normal());

        final SingularValue<Double> tmpRaw = new RawSingularValue();

        try {

            final MatrixStore<Double> tmpInv = tmpRaw.invert(tmpOrg);

            TestUtils.assertEquals(tmpOrg, tmpOrg.multiply(tmpInv).multiply(tmpOrg), NumberContext.getGeneral(6, 6));

        } catch (final RecoverableCondition exception) {
            exception.printStackTrace();
        }

    }

    /**
     * A user discovered that some large (relatively uniform) matrices causes the algorithm to never finsh
     * https://github.com/optimatika/ojAlgo/issues/22
     */
    @Test
    @Tag("slow")
    public void testP20160510InvertLargeMatrix() {

        final double[][] data = new double[3000][3000];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length; j++) {
                data[i][j] = 0.9;
            }
        }
        data[0][1] = 1.01;

        final PrimitiveMatrix input = PrimitiveMatrix.FACTORY.rows(data);
        try {
            // final SingularValue<Double> svd = SingularValue.make(input);
            final SingularValue<Double> svd = new SingularValueDecomposition.Primitive();
            svd.invert(input);
        } catch (final RecoverableCondition exception) {
            // TODO Auto-generated catch block
            exception.printStackTrace();
        }

        // The issue:can't  be reached here!!!
    }

    /**
     * This program tests the behavior of the QR and SVD decompositions on matrices with more columns than
     * rows. The desire is to compute a basis for the (right) null space of a matrix.
     * https://stackoverflow.com/questions/50847140 https://github.com/optimatika/ojAlgo/issues/104
     *
     * @author Paul A. Rubin
     */
    @Test
    public void testP20180614() {

        // We start by creating a 5 x 7 matrix with rank 4.
        double[][] data = new double[][] { { 3, -6, 6, 9, 1, 3, 2 }, { -2, 4, 5, 3, 2, 7, 5 }, { -7, 14, 3, -4, 0, 0, 0 }, { 0, 0, -2, -2, 3, -5, -8 },
                { -1, 2, 7, 6, 2, 0, -2 } };
        MatrixStore<Double> matrixA = PrimitiveDenseStore.FACTORY.rows(data);
        // Print the matrix to verify it.
        if (DEBUG) {
            System.out.println("A matrix:\n" + matrixA);
        }
        // Try computing a full size QR decomposition.
        if (DEBUG) {
            System.out.println("\nAttempting QR decomposition of A' ...");
        }
        DecompositionProblems.fullQR(matrixA);

        // Repeat this with a square matrix (padding with rows of zeros).
        int rows = data.length;
        int cols = data[0].length;
        MatrixStore<Double> zeros = PrimitiveDenseStore.FACTORY.makeZero(cols - rows, cols);
        MatrixStore<Double> matrixB = matrixA.logical().below(zeros).copy();
        if (DEBUG) {
            // Print B to verify it.
            System.out.println("\nB matrix:\n" + matrixB);
            // Try computing a full size QR decomposition.
            System.out.println("\nAttempting QR decomposition of B' ...");
        }

        DecompositionProblems.fullQR(matrixB);

        // We repeat this experiment using SVD rather than QR, beginning with
        // the original matrix A.
        if (DEBUG) {
            System.out.println("\nAttempting singular value decomposition of A ...");
        }

        DecompositionProblems.fullSVD(matrixA);

        // Finally, we confirm that it works with the padded matrix.
        if (DEBUG) {
            System.out.println("\nAttempting singular value decomposition of B ...");
        }

        DecompositionProblems.fullSVD(matrixB);
    }

    /**
     * There was a problem extracting the eigenpairs (java.lang.ArrayIndexOutOfBoundsException)
     * https://github.com/optimatika/ojAlgo/issues/105
     */
    @Test
    public void testP20180617() {

        final NumberContext precision = new NumberContext(12, 8);

        PrimitiveDenseStore matrix = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.730967787376657, 0.24053641567148587, 0.6374174253501083 },
                { 0.24053641567148587, 0.5975452777972018, 0.3332183994766498 }, { 0.6374174253501083, 0.3332183994766498, 0.8791825178724801 } });

        for (Eigenvalue<Double> evd : MatrixDecompositionTests.getPrimitiveEigenvalue()) {

            String className = evd.getClass().getName();

            evd.decompose(matrix);

            TestUtils.assertEquals(matrix, evd, precision);

            if (DEBUG) {
                BasicLogger.debug("D", evd.getD());
                BasicLogger.debug("V", evd.getV());
            }

            MatrixStore<Double> d = evd.getD();
            Array1D<ComplexNumber> values = evd.getEigenvalues();
            MatrixStore<ComplexNumber> vectors = evd.getEigenvectors();

            TestUtils.assertEquals(className, values.get(0), d.doubleValue(0, 0));
            TestUtils.assertEquals(className, values.get(0), evd.getEigenpair(0).value);
            TestUtils.assertEquals(className, vectors.sliceColumn(0), evd.getEigenpair(0).vector);

            TestUtils.assertEquals(className, values.get(1), d.doubleValue(1, 1));
            TestUtils.assertEquals(className, values.get(1), evd.getEigenpair(1).value);
            TestUtils.assertEquals(className, vectors.sliceColumn(1), evd.getEigenpair(1).vector);

            TestUtils.assertEquals(className, values.get(2), d.doubleValue(2, 2));
            TestUtils.assertEquals(className, values.get(2), evd.getEigenpair(2).value);
            TestUtils.assertEquals(className, vectors.sliceColumn(2), evd.getEigenpair(2).vector);
        }

    }

}
