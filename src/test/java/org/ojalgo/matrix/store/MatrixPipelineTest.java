package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.data.DataProcessors;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;

public class MatrixPipelineTest extends MatrixStoreTests {

    private static final Normal NORMAL = Normal.standard();
    private static final Uniform UNIFORM = Uniform.standard();

    private static ElementsSupplier<Double> initialise() {

        // Assume you have the matrices [A],[B] and[C]
        Primitive64Store mtrxA = Primitive64Store.FACTORY.make(5, 7);
        Primitive64Store mtrxB = Primitive64Store.FACTORY.make(7, 3);
        Primitive64Store mtrxC = Primitive64Store.FACTORY.make(5, 3);
        mtrxA.fillAll(UNIFORM);
        mtrxB.fillAll(NORMAL);
        mtrxC.fillAll(UNIFORM);

        // [D] = [A][B]
        MatrixStore<Double> expD = mtrxA.multiply(mtrxB);

        ElementsSupplier<Double> placeholderD = mtrxB.premultiply(mtrxA);
        Primitive64Store actD = placeholderD.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expD", expD);
            BasicLogger.debug("actD", actD);
        }
        TestUtils.assertEquals(expD, actD);
        placeholderD.supplyTo(actD);
        TestUtils.assertEquals(expD, actD);

        // [E] = [D] - [C]
        PhysicalStore<Double> expE = expD.copy();
        expE.modifyMatching(SUBTRACT, mtrxC);

        ElementsSupplier<Double> placeholderE = placeholderD.onMatching(SUBTRACT, mtrxC);
        Primitive64Store actE = placeholderE.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expE", expE);
            BasicLogger.debug("actE", actE);
        }
        TestUtils.assertEquals(expE, actE);
        placeholderE.supplyTo(actE);
        TestUtils.assertEquals(expE, actE);

        // [F] = [E]t
        MatrixStore<Double> expF = expE.transpose();

        ElementsSupplier<Double> placeholderF = placeholderE.transpose();
        Primitive64Store actF = placeholderF.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expF", expF);
            BasicLogger.debug("actF", actF);
        }
        TestUtils.assertEquals(expF, actF);
        placeholderF.supplyTo(actF);
        TestUtils.assertEquals(expF, actF);

        return placeholderF;
    }

    @Test
    public void testOnAll() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyAll(DIVIDE.by(2.0));

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onAll(DIVIDE.by(2.0));
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testOnAny() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyAny(DataProcessors.CENTER);

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onAny(DataProcessors.CENTER);
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testOnColumns() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store args = Primitive64Store.FACTORY.makeFilled(1, 5, NORMAL);

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyMatchingInRows(MULTIPLY, args);

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onColumns(MULTIPLY, args);
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testOnMatchingLeft() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store left = Primitive64Store.FACTORY.makeFilled(3, 5, UNIFORM);

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyMatching(left, MULTIPLY);

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onMatching(left, MULTIPLY);
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testOnMatchingRight() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store right = Primitive64Store.FACTORY.makeFilled(3, 5, NORMAL);

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyMatching(DIVIDE, right);

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onMatching(DIVIDE, right);
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testOnRows() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        Primitive64Store args = Primitive64Store.FACTORY.makeFilled(3, 1, NORMAL);

        Primitive64Store expected = startingPoint.collect(Primitive64Store.FACTORY);
        expected.modifyMatchingInColumns(MULTIPLY, args);

        ElementsSupplier<Double> nextPlaceholder = startingPoint.onRows(MULTIPLY, args);
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testTranspose() {

        ElementsSupplier<Double> startingPoint = MatrixPipelineTest.initialise();

        MatrixStore<Double> expected = startingPoint.collect(Primitive64Store.FACTORY).transpose();

        ElementsSupplier<Double> nextPlaceholder = startingPoint.transpose();
        Primitive64Store actual = nextPlaceholder.collect(Primitive64Store.FACTORY);

        if (DEBUG) {
            BasicLogger.debug("expected", expected);
            BasicLogger.debug("actual", actual);
        }
        TestUtils.assertEquals(expected, actual);
        nextPlaceholder.supplyTo(actual);
        TestUtils.assertEquals(expected, actual);
    }

}
