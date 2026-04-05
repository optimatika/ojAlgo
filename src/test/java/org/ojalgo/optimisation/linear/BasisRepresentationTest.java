package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;

class BasisRepresentationTest {

    private static final double TOL = 1e-10;

    private static BasisRepresentation[] createVariants(final int dim) {
        return new BasisRepresentation[] { new ProductFormInverse(dim), new SparseDecomposition(dim), new DenseDecomposition(dim) };
    }

    @Test
    void testSimplexUpdateScenario() {
        int dim = 2;

        R064CSC.Builder builder = R064CSC.newBuilder();
        builder.set(0, 0, 1.0);
        builder.set(1, 0, 1.0);
        builder.set(1, 1, 1.0);
        R064CSC initialCSC = builder.build();

        int[] included = { 0, 1 };

        R064CSC.Builder updBuilder = R064CSC.newBuilder();
        updBuilder.set(0, 0, 1.0);
        updBuilder.set(1, 0, 1.0);
        updBuilder.set(1, 1, 1.0);
        R064CSC updatedCSC = updBuilder.build();

        double[] vecData = { 10.0, 11.0 };
        PhysicalStore<Double> vec = R064Store.FACTORY.columns(new double[][] { vecData });

        for (BasisRepresentation rep : BasisRepresentationTest.createVariants(dim)) {
            rep.reset(initialCSC, included);
            rep.update(updatedCSC, included, 0, 0);
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

        int dim = 2;

        R064CSC.Builder initialBuilder = R064CSC.newBuilder();
        initialBuilder.set(0, 0, 1.0);
        initialBuilder.set(1, 0, 1.0);
        initialBuilder.set(0, 1, 1.0);
        initialBuilder.set(1, 1, -1.0);
        R064CSC initialCSC = initialBuilder.build();

        int[] included = { 0, 1 };

        R064CSC.Builder updatedBuilder = R064CSC.newBuilder();
        updatedBuilder.set(0, 0, 1.0);
        updatedBuilder.set(1, 0, 2.0);
        updatedBuilder.set(0, 1, 1.0);
        updatedBuilder.set(1, 1, -1.0);
        R064CSC updatedCSC = updatedBuilder.build();

        double[][] updatedBasisData = { { 1.0, 1.0 }, { 2.0, -1.0 } };
        MatrixStore<Double> updatedBasis = R064Store.FACTORY.rows(updatedBasisData);

        PhysicalStore<Double> vecBefore = R064Store.FACTORY.column(4.0, 2.0);
        PhysicalStore<Double> vecAfter = R064Store.FACTORY.column(10.0, 11.0);

        LU<Double> lu = LU.R064.make(updatedBasis);
        lu.decompose(updatedBasis);

        for (BasisRepresentation rep : BasisRepresentationTest.createVariants(dim)) {

            rep.reset(initialCSC, included);

            PhysicalStore<Double> ftranResult = R064Store.FACTORY.make(2, 1);
            vecBefore.supplyTo(ftranResult);
            rep.ftran(ftranResult);

            PhysicalStore<Double> btranResult = R064Store.FACTORY.make(2, 1);
            vecBefore.supplyTo(btranResult);
            rep.btran(btranResult);

            rep.update(updatedCSC, included, 0, 0);

            PhysicalStore<Double> ftranResult2 = R064Store.FACTORY.make(2, 1);
            vecAfter.supplyTo(ftranResult2);
            rep.ftran(ftranResult2);

            PhysicalStore<Double> btranResult2 = R064Store.FACTORY.make(2, 1);
            vecAfter.supplyTo(btranResult2);
            rep.btran(btranResult2);

            PhysicalStore<Double> expectedFtran = R064Store.FACTORY.copy(vecAfter);
            lu.ftran(expectedFtran);
            PhysicalStore<Double> expectedBtran = R064Store.FACTORY.copy(vecAfter);
            lu.btran(expectedBtran);

            for (int i = 0; i < dim; i++) {
                Assertions.assertEquals(expectedFtran.get(i, 0), ftranResult2.get(i, 0), TOL, rep.getClass().getSimpleName() + ": ftran");
                Assertions.assertEquals(expectedBtran.get(i, 0), btranResult2.get(i, 0), TOL, rep.getClass().getSimpleName() + ": btran");
            }
        }
    }
}
