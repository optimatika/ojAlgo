package org.ojalgo.matrix.decomposition;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Factor;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.Solver;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.transformation.InvertibleFactor;
import org.ojalgo.netio.BasicLogger;

public class DecompositionFactorTest extends MatrixDecompositionTests {

    static void doBtranTest(final R064Store body, final double[] rhs, final Solver<Double> solver) {

        double[] expected = rhs.clone();
        solver.btran(expected);

        double[] actual = rhs.clone();
        InvertibleFactor.btran(solver.getFactors(), actual);

        TestUtils.assertEquals(expected, actual);

        R064Store exp = R064Store.FACTORY.make(1, rhs.length);
        exp.fillMatching(rhs);

        R064Store x = R064Store.FACTORY.make(1, actual.length);
        x.fillMatching(actual);

        MatrixStore<Double> act = x.multiply(body);

        TestUtils.assertEquals(exp, act);
    }

    static void doFtranTest(final R064Store body, final double[] rhs, final Solver<Double> solver) {

        double[] expected = rhs.clone();
        solver.ftran(expected);

        double[] actual = rhs.clone();
        InvertibleFactor.ftran(solver.getFactors(), actual);

        TestUtils.assertEquals(expected, actual);

        R064Store exp = R064Store.FACTORY.make(rhs.length, 1);
        exp.fillMatching(rhs);

        R064Store x = R064Store.FACTORY.make(actual.length, 1);
        x.fillMatching(actual);

        MatrixStore<Double> act = body.multiply(x);

        TestUtils.assertEquals(exp, act);
    }

    @Test
    public void testDenseLU() {

        R064Store body = R064Store.FACTORY.make(5, 5);
        body.fillAll(0.0);
        body.set(0, 1, 2.0);
        body.set(0, 2, 1.0);
        body.set(1, 0, 3.0);
        body.set(1, 1, 1.0);
        body.set(1, 3, 4.0);
        body.set(2, 0, 1.0);
        body.set(2, 2, 5.0);
        body.set(2, 4, 2.0);
        body.set(3, 1, 2.0);
        body.set(3, 3, 3.0);
        body.set(3, 4, 1.0);
        body.set(4, 0, 4.0);
        body.set(4, 2, 1.0);
        body.set(4, 4, 6.0);
        double[] rhs = { 1, 2, 3, 4, 5 };

        DenseLU.R064 solver = new DenseLU.R064();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        solver.decompose(body);

        MatrixStore<Double> factorP = solver.getFactorP().get();
        MatrixStore<Double> factorL = solver.getFactorL().get();
        MatrixStore<Double> factorU = solver.getFactorU().get();
        Optional<Factor<Double>> optQ = solver.getFactorQ();

        MatrixStore<Double> recreated = factorP.multiply(factorL).multiply(factorU);
        if (optQ.isPresent()) {
            recreated = recreated.multiply(optQ.get().get());
        }

        TestUtils.assertEquals(body, recreated);
    }

    @Test
    public void testDenseQR() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        DenseQR.R064 solver = new DenseQR.R064();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        solver.decompose(body);

        MatrixStore<Double> factorQ = solver.getFactorQ().get();
        MatrixStore<Double> factorR = solver.getFactorR().get();

        MatrixStore<Double> recreated = factorQ.multiply(factorR);

        TestUtils.assertEquals(body, recreated);
    }

    @Test
    public void testDenseSingularValue() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        DenseSingularValue.R064 solver = new DenseSingularValue.R064();
        solver.decompose(body);

        double[] ftranResult = rhs.clone();
        solver.ftran(ftranResult);

        R064Store x = R064Store.FACTORY.make(ftranResult.length, 1);
        x.fillMatching(ftranResult);

        R064Store b = R064Store.FACTORY.make(rhs.length, 1);
        b.fillMatching(rhs);

        TestUtils.assertEquals(b, body.multiply(x));

        double[] btranResult = rhs.clone();
        solver.btran(btranResult);

        R064Store xb = R064Store.FACTORY.make(1, btranResult.length);
        xb.fillMatching(btranResult);

        R064Store bb = R064Store.FACTORY.make(1, rhs.length);
        bb.fillMatching(rhs);

        TestUtils.assertEquals(bb, xb.multiply(body));
    }

    @Test
    public void testHermitianEvD() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        HermitianEvD.R064 solver = new HermitianEvD.R064();
        solver.decompose(body);

        double[] ftranResult = rhs.clone();
        solver.ftran(ftranResult);

        R064Store x = R064Store.FACTORY.make(ftranResult.length, 1);
        x.fillMatching(ftranResult);

        R064Store b = R064Store.FACTORY.make(rhs.length, 1);
        b.fillMatching(rhs);

        TestUtils.assertEquals(b, body.multiply(x));

        double[] btranResult = rhs.clone();
        solver.btran(btranResult);

        R064Store xb = R064Store.FACTORY.make(1, btranResult.length);
        xb.fillMatching(btranResult);

        R064Store bb = R064Store.FACTORY.make(1, rhs.length);
        bb.fillMatching(rhs);

        TestUtils.assertEquals(bb, xb.multiply(body));
    }

    @Test
    public void testRawEigenvalueSymmetric() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        RawEigenvalue.Symmetric solver = new RawEigenvalue.Symmetric();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        TestUtils.assertTrue(solver.getFactors().size() > 1);
    }

    @Test
    public void testRawLU() {

        R064Store body = R064Store.FACTORY.make(5, 5);
        body.fillAll(0.0);
        body.set(0, 1, 2.0);
        body.set(0, 2, 1.0);
        body.set(1, 0, 3.0);
        body.set(1, 1, 1.0);
        body.set(1, 3, 4.0);
        body.set(2, 0, 1.0);
        body.set(2, 2, 5.0);
        body.set(2, 4, 2.0);
        body.set(3, 1, 2.0);
        body.set(3, 3, 3.0);
        body.set(3, 4, 1.0);
        body.set(4, 0, 4.0);
        body.set(4, 2, 1.0);
        body.set(4, 4, 6.0);
        double[] rhs = { 1, 2, 3, 4, 5 };

        RawLU solver = new RawLU();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        solver.decompose(body);

        MatrixStore<Double> factorP = solver.getFactorP().get();
        MatrixStore<Double> factorL = solver.getFactorL().get();
        MatrixStore<Double> factorU = solver.getFactorU().get();
        Optional<Factor<Double>> optQ = solver.getFactorQ();

        MatrixStore<Double> recreated = factorP.multiply(factorL).multiply(factorU);
        if (optQ.isPresent()) {
            recreated = recreated.multiply(optQ.get().get());
        }

        TestUtils.assertEquals(body, recreated);
    }

    @Test
    public void testRawQR() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        RawQR solver = new RawQR();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        solver.decompose(body);

        MatrixStore<Double> factorQ = solver.getFactorQ().get();
        MatrixStore<Double> factorR = solver.getFactorR().get();

        MatrixStore<Double> recreated = factorQ.multiply(factorR);

        TestUtils.assertEquals(body, recreated);
    }

    @Test
    public void testRawSingularValue() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        RawSingularValue solver = new RawSingularValue();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        TestUtils.assertTrue(solver.getFactors().size() > 1);
    }

    @Test
    public void testSolverBtran() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        for (Solver<Double> solver : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            solver.decompose(body);
            if (solver.getFactors().size() == 1) {
                BasicLogger.debug("Not yet implemented: " + solver.getClass());
                continue;
            }

            DecompositionFactorTest.doBtranTest(body, rhs, solver);
        }
    }

    @Test
    public void testSolverFtran() {

        R064Store body = R064Store.FACTORY.makeSPD(5);
        double[] rhs = { 1, 2, 3, 4, 5 };

        for (Solver<Double> solver : MatrixDecompositionTests.getPrimitiveMatrixDecompositionSolver()) {

            solver.decompose(body);
            if (solver.getFactors().size() == 1) {
                BasicLogger.debug("Not yet implemented: " + solver.getClass());
                continue;
            }

            DecompositionFactorTest.doFtranTest(body, rhs, solver);
        }
    }

    @Test
    public void testSparseLU() {

        R064Store body = R064Store.FACTORY.make(5, 5);
        body.fillAll(0.0);
        body.set(0, 1, 2.0);
        body.set(0, 2, 1.0);
        body.set(1, 0, 3.0);
        body.set(1, 1, 1.0);
        body.set(1, 3, 4.0);
        body.set(2, 0, 1.0);
        body.set(2, 2, 5.0);
        body.set(2, 4, 2.0);
        body.set(3, 1, 2.0);
        body.set(3, 3, 3.0);
        body.set(3, 4, 1.0);
        body.set(4, 0, 4.0);
        body.set(4, 2, 1.0);
        body.set(4, 4, 6.0);
        double[] rhs = { 1, 2, 3, 4, 5 };

        SparseLU solver = new SparseLU();
        solver.decompose(body);

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);

        solver.decompose(body);

        MatrixStore<Double> factorP = solver.getFactorP().get();
        MatrixStore<Double> factorL = solver.getFactorL().get();
        MatrixStore<Double> factorU = solver.getFactorU().get();
        Optional<Factor<Double>> optQ = solver.getFactorQ();

        MatrixStore<Double> recreated = factorP.multiply(factorL).multiply(factorU);
        if (optQ.isPresent()) {
            recreated = recreated.multiply(optQ.get().get());
        }

        TestUtils.assertEquals(body, recreated);
    }

    @Test
    public void testSparseLUPostUpdate() {

        R064Store body = R064Store.FACTORY.make(5, 5);
        body.fillAll(0.0);
        body.set(0, 1, 2.0);
        body.set(0, 2, 1.0);
        body.set(1, 0, 3.0);
        body.set(1, 1, 1.0);
        body.set(1, 3, 4.0);
        body.set(2, 0, 1.0);
        body.set(2, 2, 5.0);
        body.set(2, 4, 2.0);
        body.set(3, 1, 2.0);
        body.set(3, 3, 3.0);
        body.set(3, 4, 1.0);
        body.set(4, 0, 4.0);
        body.set(4, 2, 1.0);
        body.set(4, 4, 6.0);

        SparseLU solver = new SparseLU();
        solver.decompose(body);

        SparseStore<Double> newColumn = SparseStore.R064.make(5, 1);
        newColumn.set(0, 0, 1.0);
        newColumn.set(2, 0, 3.0);
        newColumn.set(4, 0, 2.0);

        TestUtils.assertTrue(solver.updateColumn(2, newColumn));

        body.fillColumn(2, newColumn.sliceColumn(0));

        double[] rhs = { 1, 2, 3, 4, 5 };

        DecompositionFactorTest.doFtranTest(body, rhs, solver);

        DecompositionFactorTest.doBtranTest(body, rhs, solver);
    }

}
