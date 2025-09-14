/*
 * Copyright 1997-2025 Optimatika
 *
 * Licensed under the MIT License.
 */
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests that spectral (symmetric/Hermitian) eigenvalue decompositions correctly expose singular value methods
 * through the Spectral interface.
 */
public class SpectralAsSingularValueTest extends MatrixDecompositionTests {

    private static final NumberContext ACC = NumberContext.of(8);

    private static <N extends Comparable<N>> void doTest(final MatrixStore<N> original, final SingularValue<N> svd, final Eigenvalue.Spectral<N> spectral) {

        svd.decompose(original);
        spectral.decompose(original);

        // Check eigen relation A V = V D
        MatrixStore<N> mtrxAV = original.multiply(spectral.getV());
        MatrixStore<N> mtrxVD = spectral.getV().multiply(spectral.getD());
        TestUtils.assertEquals(mtrxAV, mtrxVD, ACC);

        Array1D<Double> singularValues = svd.getSingularValues();

        Array1D<ComplexNumber> spectralEigenvalues = spectral.getEigenvalues();
        Array1D<Double> spectralSingularValues = spectral.getSingularValues();

        TestUtils.assertEquals(singularValues.size(), spectralEigenvalues.size());
        TestUtils.assertEquals(singularValues.size(), spectralSingularValues.size());

        for (int i = 0; i < singularValues.size(); i++) {
            double expected = singularValues.doubleValue(i);
            TestUtils.assertEquals(expected, spectralEigenvalues.get(i).norm(), ACC);
            TestUtils.assertEquals(expected, spectralSingularValues.doubleValue(i), ACC);
        }

        TestUtils.assertEquals(svd.getCondition(), spectral.getCondition(), ACC);
        TestUtils.assertEquals(svd.getFrobeniusNorm(), spectral.getFrobeniusNorm(), ACC);
        TestUtils.assertEquals(svd.getKyFanNorm(2), spectral.getKyFanNorm(2), ACC);
        TestUtils.assertEquals(svd.getOperatorNorm(), spectral.getOperatorNorm(), ACC);
        TestUtils.assertEquals(svd.getTraceNorm(), spectral.getTraceNorm(), ACC);

        TestUtils.assertEquals(svd.getS(), spectral.getS(), ACC);

        TestUtils.assertEquals(original, Eigenvalue.reconstruct(spectral), ACC);
        TestUtils.assertEquals(original, SingularValue.reconstruct(spectral), ACC);

        TestUtils.assertEquals(svd.getRank(), spectral.getRank(), ACC);

        boolean solvable = svd.isSolvable();
        TestUtils.assertEquals(solvable, spectral.isSolvable());

        if (solvable) {

            TestUtils.assertEquals(svd.getInverse(), spectral.getInverse());

            PhysicalStore<N> rhs = original.physical().make(original.getRowDim(), 1);
            rhs.fillAll(original.physical().scalar().one().get());

            TestUtils.assertEquals(svd.getSolution(rhs), spectral.getSolution(rhs));
        }
    }

    static void doComplex(final MatrixStore<ComplexNumber> original) {

        SingularValue<ComplexNumber> svd = new DenseSingularValue.C128();

        Eigenvalue.Spectral<ComplexNumber> evd = new HermitianEvD.C128();

        SpectralAsSingularValueTest.doTest(original, svd, evd);
    }

    static void doPrimitive(final MatrixStore<Double> original) {

        SingularValue<Double> svd1 = new DenseSingularValue.R064();
        SingularValue<Double> svd2 = new RawSingularValue();

        Eigenvalue.Spectral<Double> evd1 = new HermitianEvD.R064();
        Eigenvalue.Spectral<Double> evd2 = new RawEigenvalue.Symmetric();

        SpectralAsSingularValueTest.doTest(original, svd1, evd2);

        SpectralAsSingularValueTest.doTest(original, svd2, evd1);
    }

    static void doRational(final MatrixStore<RationalNumber> original) {

        SingularValue<RationalNumber> svd = new DenseSingularValue.Q128();

        Eigenvalue.Spectral<RationalNumber> evd = new HermitianEvD.Q128();

        SpectralAsSingularValueTest.doTest(original, svd, evd);
    }

    /**
     * Hermitian complex indefinite matrix with mixed-sign diagonal and small complex off-diagonals.
     */
    @Test
    public void testComplexIndefiniteMatrix() {

        GenericStore<ComplexNumber> hermitian = GenericStore.C128.make(4, 4);

        double[] diag = { 5, -4, 3, -2 }; // Mixed signs
        for (int i = 0; i < 4; i++) {
            hermitian.set(i, i, ComplexNumber.of(diag[i], 0.0));
        }

        ComplexNumber b01 = ComplexNumber.of(0.2, -0.05);
        ComplexNumber b02 = ComplexNumber.of(0.1, 0.04);
        ComplexNumber b03 = ComplexNumber.of(-0.15, 0.02);
        ComplexNumber b12 = ComplexNumber.of(0.05, -0.03);
        ComplexNumber b13 = ComplexNumber.of(0.07, 0.01);
        ComplexNumber b23 = ComplexNumber.of(-0.08, 0.06);

        hermitian.set(0, 1, b01);
        hermitian.set(1, 0, b01.conjugate());
        hermitian.set(0, 2, b02);
        hermitian.set(2, 0, b02.conjugate());
        hermitian.set(0, 3, b03);
        hermitian.set(3, 0, b03.conjugate());
        hermitian.set(1, 2, b12);
        hermitian.set(2, 1, b12.conjugate());
        hermitian.set(1, 3, b13);
        hermitian.set(3, 1, b13.conjugate());
        hermitian.set(2, 3, b23);
        hermitian.set(3, 2, b23.conjugate());

        SpectralAsSingularValueTest.doComplex(hermitian);
    }

    /**
     * Hermitian complex-valued matrix with strictly negative eigenvalues (by Gershgorin disc construction).
     */
    @Test
    public void testComplexNegativeEigenvalues() {

        GenericStore<ComplexNumber> hermitian = GenericStore.C128.make(3, 3);

        // Diagonal (real, negative)
        hermitian.set(0, 0, ComplexNumber.of(-5.0, 0.0));
        hermitian.set(1, 1, ComplexNumber.of(-4.0, 0.0));
        hermitian.set(2, 2, ComplexNumber.of(-3.0, 0.0));

        // Off-diagonals with small magnitudes ensuring all eigenvalues remain negative
        ComplexNumber a01 = ComplexNumber.of(0.4, 0.1);
        ComplexNumber a02 = ComplexNumber.of(0.3, -0.05);
        ComplexNumber a12 = ComplexNumber.of(0.2, 0.07);

        hermitian.set(0, 1, a01);
        hermitian.set(1, 0, a01.conjugate());
        hermitian.set(0, 2, a02);
        hermitian.set(2, 0, a02.conjugate());
        hermitian.set(1, 2, a12);
        hermitian.set(2, 1, a12.conjugate());

        SpectralAsSingularValueTest.doComplex(hermitian);
    }

    /**
     * Hermitian complex positive definite matrix constructed as A * A^H + alpha*I.
     */
    @Test
    public void testComplexSPDMatrix() {

        GenericStore<ComplexNumber> A = GenericStore.C128.make(4, 4);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                A.set(r, c, ComplexNumber.of(r + c + 1.0, 0.1 * (r - c)));
            }
        }

        MatrixStore<ComplexNumber> hermitianSPD = A.multiply(A.conjugate()).add(GenericStore.C128.makeEye(4, 4));

        SpectralAsSingularValueTest.doComplex(hermitianSPD);
    }

    /**
     * Simple diagonal symmetric matrix with mixed signs
     */
    @Test
    public void testIndefiniteMatrix() {

        double[][] data = { { 4, 0, 0, 0 }, { 0, -3, 0, 0 }, { 0, 0, 2, 0 }, { 0, 0, 0, -1 } };
        MatrixStore<Double> indefinite = RawStore.wrap(data);

        SpectralAsSingularValueTest.doPrimitive(indefinite);
    }

    /**
     * Real negative definite diagonal matrix (all eigenvalues strictly negative).
     */
    @Test
    public void testNegativeDefiniteMatrix() {

        double[][] data = { { -5, 0, 0, 0, 0 }, { 0, -4, 0, 0, 0 }, { 0, 0, -3, 0, 0 }, { 0, 0, 0, -2, 0 }, { 0, 0, 0, 0, -1 } };
        MatrixStore<Double> negativeDefinite = RawStore.wrap(data);

        SpectralAsSingularValueTest.doPrimitive(negativeDefinite);
    }

    @Test
    public void testPrimitive1x1Matrices() {
        SpectralAsSingularValueTest.doPrimitive(RawStore.wrap(new double[][] { { 7.0 } })); // positive
        SpectralAsSingularValueTest.doPrimitive(RawStore.wrap(new double[][] { { 0.0 } })); // zero
        SpectralAsSingularValueTest.doPrimitive(RawStore.wrap(new double[][] { { -5.0 } })); // negative
    }

    @Test
    public void testRankDeficientPSD() {

        // Create B (rank 3) then A = B^T B (5x5, rank 3)
        R064Store B = R064Store.FACTORY.make(3, 5);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 5; c++) {
                if (c % 3 == r) {
                    B.set(r, c, r + c + 1.0);
                }
            }
        }
        MatrixStore<Double> A = B.transpose().multiply(B); // Symmetric PSD rank deficient

        SpectralAsSingularValueTest.doPrimitive(A);
    }

    @Test
    public void testRationalMixedSigns() {

        GenericStore<RationalNumber> rat = GenericStore.Q128.make(3, 3);

        // Symmetric with mixed signs and simple rational entries
        long[][] main = { { 4, 0, 0 }, { 0, -3, 0 }, { 0, 0, 2 } };
        for (int i = 0; i < 3; i++) {
            rat.set(i, i, RationalNumber.of(main[i][i], 1L));
        }

        // Off-diagonal rational entries (ensure symmetry)
        rat.set(0, 1, RationalNumber.of(1L, 2L));
        rat.set(1, 0, RationalNumber.of(1L, 2L));
        rat.set(1, 2, RationalNumber.of(-2L, 3L));
        rat.set(2, 1, RationalNumber.of(-2L, 3L));

        SpectralAsSingularValueTest.doRational(rat);
    }

    @Test
    public void testRepeatedEigenvalues() {

        double[][] diag = { { 5, 0, 0, 0, 0 }, { 0, 5, 0, 0, 0 }, { 0, 0, 5, 0, 0 }, { 0, 0, 0, 2, 0 }, { 0, 0, 0, 0, 2 } };
        MatrixStore<Double> repeated = RawStore.wrap(diag);

        SpectralAsSingularValueTest.doPrimitive(repeated);
    }

    @Test
    public void testSPDMatrix() {

        MatrixStore<Double> spd = R064Store.FACTORY.makeSPD(6);

        SpectralAsSingularValueTest.doPrimitive(spd);
    }

    @Test
    public void testZeroMatrix() {

        MatrixStore<Double> zero = R064Store.FACTORY.makeZero(5, 5);

        SpectralAsSingularValueTest.doPrimitive(zero);
    }
}