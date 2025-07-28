package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;

class BasisRepresentationTest {

    private static final double TOL = 1e-10;

    private BasisRepresentation[] createVariants(final int dim) {
        return new BasisRepresentation[] { new ProductFormInverse(dim, 1e-6), new DecomposedInverse(false, dim), new DecomposedInverse(true, dim) };
    }

    @Test
    void testSimplexUpdateScenario() {
        int dim = 2;
        double[][] basisData = { { 1.0, 0.0 }, { 1.0, 1.0 } };
        PhysicalStore<Double> basis = R064Store.FACTORY.rows(basisData);
        SparseArray<Double> updateCol = SparseArray.factory(ArrayR064.FACTORY).make(dim);
        updateCol.set(0, 1.0);
        updateCol.set(1, 1.0);
        double[] vecData = { 10.0, 11.0 };
        PhysicalStore<Double> vec = R064Store.FACTORY.columns(new double[][] { vecData });

        for (BasisRepresentation rep : this.createVariants(dim)) {
            rep.reset(basis);
            rep.update(basis, 0, updateCol);
            PhysicalStore<Double> ftranResult = R064Store.FACTORY.make(dim, 1);
            vec.supplyTo(ftranResult);
            rep.ftran(ftranResult);
            PhysicalStore<Double> btranResult = R064Store.FACTORY.make(dim, 1);
            vec.supplyTo(btranResult);
            rep.btran(btranResult);

        }
    }

    @Test
    void testSmallBasisUpdateAndFtranBtran() {
        // Initial basis: [[1, 1], [1, -1]]
        double[][] initialBasisData = { { 1.0, 1.0 }, { 1.0, -1.0 } };
        MatrixStore<Double> initialBasis = R064Store.FACTORY.rows(initialBasisData);
        int dim = 2;

        // New column to replace column 0: [1, 2]
        SparseArray<Double> newCol = SparseArray.factory(ArrayR064.FACTORY).make(dim);
        newCol.set(0, 1.0);
        newCol.set(1, 2.0);

        // After update, basis should be [[1, 1], [2, -1]]
        double[][] updatedBasisData = { { 1.0, 1.0 }, { 2.0, -1.0 } };
        MatrixStore<Double> updatedBasis = R064Store.FACTORY.rows(updatedBasisData);

        // Test vector for ftran/btran before update
        PhysicalStore<Double> vecBefore = R064Store.FACTORY.column(4.0, 2.0);
        // Test vector for ftran/btran after update (matches simplex debug output)
        PhysicalStore<Double> vecAfter = R064Store.FACTORY.column(10.0, 11.0);

        // Reference LU decomposition for updated basis
        LU<Double> lu = LU.R064.make(updatedBasis);
        lu.decompose(updatedBasis);

        for (BasisRepresentation rep : this.createVariants(dim)) {

            // Reset to initial basis
            rep.reset(initialBasis);

            // ftran before update
            PhysicalStore<Double> ftranResult = R064Store.FACTORY.make(2, 1);
            vecBefore.supplyTo(ftranResult);
            rep.ftran(ftranResult);

            // btran before update
            PhysicalStore<Double> btranResult = R064Store.FACTORY.make(2, 1);
            vecBefore.supplyTo(btranResult);
            rep.btran(btranResult);

            // Update basis: replace column 0 with newCol
            rep.update(updatedBasis, 0, newCol);

            // ftran after update
            PhysicalStore<Double> ftranResult2 = R064Store.FACTORY.make(2, 1);
            vecAfter.supplyTo(ftranResult2);
            rep.ftran(ftranResult2);

            // btran after update
            PhysicalStore<Double> btranResult2 = R064Store.FACTORY.make(2, 1);
            vecAfter.supplyTo(btranResult2);
            rep.btran(btranResult2);

            // Reference solutions using LU (after update)
            PhysicalStore<Double> expectedFtran = R064Store.FACTORY.copy(vecAfter);
            lu.ftran(expectedFtran);
            PhysicalStore<Double> expectedBtran = R064Store.FACTORY.copy(vecAfter);
            lu.btran(expectedBtran);

            // Assert results are close after update
            for (int i = 0; i < dim; i++) {
                Assertions.assertEquals(expectedFtran.get(i, 0), ftranResult2.get(i, 0), TOL, rep.getClass().getSimpleName() + ": ftran");
                Assertions.assertEquals(expectedBtran.get(i, 0), btranResult2.get(i, 0), TOL, rep.getClass().getSimpleName() + ": btran");
            }
        }
    }
}