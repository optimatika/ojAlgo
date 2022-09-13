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

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.decomposition.HermitianEvD.Primitive;
import org.ojalgo.matrix.decomposition.MatrixDecomposition.RankRevealing;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
@Disabled
public class ExtremeElementsCase extends MatrixDecompositionTests {

    /**
     * 146 = (308/2) - (16/2)
     */
    static final NumberContext ACCURACY = NumberContext.of(12, 146);

    private static void performInvertTest(final Primitive64Store original, final InverterTask<Double> task, final NumberContext context) {

        String clazz = task.getClass().toString();

        try {

            MatrixStore<Double> tmpInverse = task.invert(original);

            MatrixStore<Double> tmpExpected = Primitive64Store.FACTORY.makeIdentity(original.countRows());
            MatrixStore<Double> tmpActual = original.multiply(tmpInverse);

            TestUtils.assertEquals(clazz, tmpExpected, tmpActual, context);

        } catch (RecoverableCondition cause) {
            TestUtils.fail(clazz + " " + cause.toString());
        }
    }

    private static void performSolveTest(final Primitive64Store body, final Primitive64Store rhs, final SolverTask<Double> task, final NumberContext accuracy) {

        String clazz = task.getClass().toString();

        try {

            MatrixStore<Double> solution = task.solve(body, rhs);

            MatrixStore<Double> expected = rhs;
            MatrixStore<Double> actual = body.multiply(solution);

            TestUtils.assertEquals(clazz, expected, actual, accuracy);

        } catch (RecoverableCondition cause) {
            TestUtils.fail(clazz + " " + cause.toString());
        }
    }

    static void doTestInvert(final boolean large) {

        for (int precision = 1; precision <= 16; precision++) {
            NumberContext accuracy = ACCURACY.withoutScale().withPrecision(precision);

            for (int dim = 1; dim <= 10; dim++) {

                // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
                for (int exp = 0; exp < 300; exp++) {
                    double scale = POWER.invoke(TEN, large ? exp : -exp);

                    Primitive64Store original = Primitive64Store.FACTORY.makeSPD(dim);
                    if (DEBUG) {
                        BasicLogger.debug("Scale exp={} => factor={} and context={}", exp, scale, accuracy);
                        BasicLogger.debug("Original (unscaled) {}", original.toString());

                    }
                    original.modifyAll(MULTIPLY.by(scale));

                    ExtremeElementsCase.performInvertTest(original, InverterTask.PRIMITIVE.make(original), accuracy);

                    List<MatrixDecomposition<Double>> allDecomps = MatrixDecompositionTests.getPrimitiveAll();
                    for (MatrixDecomposition<Double> decomp : allDecomps) {

                        if (DEBUG) {
                            BasicLogger.debug("{} at dim={} for scale={}", decomp.getClass(), dim, scale);
                        }
                        if (decomp instanceof MatrixDecomposition.Solver) {
                            ExtremeElementsCase.performInvertTest(original, (InverterTask<Double>) decomp, accuracy);
                        }
                    }
                }
            }
        }
    }

    static void doTestSVD(final boolean large) {

        for (int precision = 1; precision < 16; precision++) {
            NumberContext accuracy = ACCURACY.withPrecision(precision);

            for (int dim = 2; dim <= 10; dim++) {

                // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
                for (int exp = 0; exp < 308; exp++) {
                    double scale = POWER.invoke(TEN, large ? exp : -exp);

                    Primitive64Store original = Primitive64Store.FACTORY.makeSPD(dim);
                    if (DEBUG) {
                        BasicLogger.debug();
                        BasicLogger.debug("Scale exp={} => factor={} and context={}", exp, scale, accuracy);
                        BasicLogger.debug("Original (unscaled) {}", original.toString());

                    }
                    original.modifyAll(MULTIPLY.by(scale));

                    ExtremeElementsCase.performInvertTest(original, InverterTask.PRIMITIVE.make(original), accuracy);

                    SingularValue<Double>[] allDecomps = MatrixDecompositionTests.getPrimitiveSingularValue();
                    for (SingularValue<Double> decomp : allDecomps) {

                        if (DEBUG) {
                            BasicLogger.debug("{} at precision= {}, dim={}, exp={} and scale={}", decomp.getClass(), precision, dim, exp, scale);
                        }
                        decomp.decompose(original);

                        if (precision == 2 && dim == 2 && exp == 0) {
                            BasicLogger.debug();
                        }
                        decomp.decompose(original);

                        TestUtils.assertEquals(original, decomp, accuracy);
                    }
                }
            }
        }
    }

    static void doTestRank(final boolean large) {

        for (int dim = 1; dim <= 10; dim++) {

            // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
            for (int exp = 0; exp < 308; exp++) {
                double scale = POWER.invoke(TEN, large ? exp : -exp);

                Primitive64Store matrix = Primitive64Store.FACTORY.makeSPD(dim);
                matrix.modifyAll(MULTIPLY.by(scale));

                SingularValue<Double> reference = SingularValue.PRIMITIVE.make(matrix);
                reference.decompose(matrix);

                if (DEBUG) {
                    BasicLogger.debug();
                    BasicLogger.debug("Reference at dim={} for scale={} => rank={} {}", dim, scale, reference.getRank(), reference.isFullRank());
                    BasicLogger.debug("Singular Values: {}", reference.getSingularValues());
                    BasicLogger.debug("Matrix (unscaled) {}", matrix.toString());
                }

                List<RankRevealing<Double>> decomps = MatrixDecompositionTests.getPrimitiveMatrixDecompositionRankRevealing();
                for (RankRevealing<Double> revealer : decomps) {
                    revealer.decompose(matrix);

                    if (DEBUG) {
                        BasicLogger.debug("{} at dim={} for scale={} => rank={} {}", revealer.getClass(), dim, scale, revealer.getRank(),
                                revealer.isFullRank());
                    }

                    TestUtils.assertEquals(reference.getRank(), revealer.getRank());
                    TestUtils.assertEquals(reference.isFullRank(), revealer.isFullRank());
                }
            }
        }

    }

    static void doTestSolve(final boolean large) {

        for (int precision = 1; precision < 16; precision++) {
            final int precision1 = precision;
            NumberContext tmpContext = NumberContext.of(precision1, Integer.MIN_VALUE);

            for (int dim = 2; dim <= 10; dim++) {

                // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
                for (int exp = 0; exp < 300; exp++) {
                    double scale = POWER.invoke(TEN, large ? exp : -exp);

                    Primitive64Store tmpBody = Primitive64Store.FACTORY.makeSPD(dim);
                    Primitive64Store tmpRHS = Primitive64Store.FACTORY.makeFilled(dim, 1, new Uniform());
                    if (DEBUG) {
                        BasicLogger.debug("Scale exp={} => factor={} and context={}", exp, scale, tmpContext);
                        BasicLogger.debug("Body (unscaled) {}", tmpBody.toString());
                        BasicLogger.debug("RHS (unscaled) {}", tmpRHS.toString());
                    }
                    UnaryFunction<Double> tmpModifier = MULTIPLY.second(scale);
                    tmpBody.modifyAll(tmpModifier);
                    tmpRHS.modifyAll(tmpModifier);

                    ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, SolverTask.PRIMITIVE.make(tmpBody, tmpRHS), tmpContext);

                    List<MatrixDecomposition<Double>> tmpAllDecomps = MatrixDecompositionTests.getPrimitiveAll();
                    for (MatrixDecomposition<Double> decomp : tmpAllDecomps) {

                        if (decomp instanceof MatrixDecomposition.Solver) {
                            if (DEBUG) {
                                BasicLogger.debug("{} at precision= {}, dim={}, exp={} and scale={}", decomp.getClass(), precision, dim, exp, scale);
                            }
                            ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, (SolverTask<Double>) decomp, tmpContext);
                        }
                    }
                }
            }
        }
    }

    static MatrixStore<Double> getVerySmall() {

        long dim = 5L;

        Primitive64Store rndm = Primitive64Store.FACTORY.make(dim, dim);

        for (long j = 0L; j < dim; j++) {
            for (long i = 0L; i < dim; i++) {
                rndm.set(i, j, Uniform.randomInteger(4));
            }
        }

        return rndm.transpose().multiply(rndm).multiply(1E-150);
    }

    @Override
    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testEvD() {

        MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        Eigenvalue<RationalNumber> tmpBig = Eigenvalue.RATIONAL.make(true);
        Eigenvalue<ComplexNumber> tmpComplex = Eigenvalue.COMPLEX.make(true);
        Eigenvalue<Double> tmpPrimitive = Eigenvalue.PRIMITIVE.make();
        Eigenvalue<Double> tmpJama = new RawEigenvalue.Dynamic();

        TestUtils.assertTrue("Rational.compute()", tmpBig.decompose(GenericStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(GenericStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Rational: {}", tmpBig.getEigenvalues());
            BasicLogger.debug("Complex: {}", tmpComplex.getEigenvalues());
            BasicLogger.debug("Primitive: {}", tmpPrimitive.getEigenvalues());
            BasicLogger.debug("Jama: {}", tmpJama.getEigenvalues());
        }

        // TestUtils.assertEquals("QR.Q Rational vs Complex", tmpBig.getQ(), tmpComplex.getQ());
        // TestUtils.assertEquals("QR.Q Complex vs Primitive", tmpComplex.getQ(), tmpPrimitive.getQ());
        // TestUtils.assertEquals("QR.Q Primitive vs Jama", tmpPrimitive.getQ(), tmpJama.getQ());

        TestUtils.assertEquals("EvD Rational vs Complex", tmpBig.getEigenvalues().get(0), tmpComplex.getEigenvalues().get(0), ACCURACY);
        TestUtils.assertEquals("EvD Complex vs Primitive", tmpComplex.getEigenvalues().get(0), tmpPrimitive.getEigenvalues().get(0), ACCURACY);
        TestUtils.assertEquals("EvD Primitive vs Jama", tmpPrimitive.getEigenvalues().get(0), tmpJama.getEigenvalues().get(0), ACCURACY);

        // TODO TestUtils.assertEquals("Rational.reconstruct()", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), ACCURACY);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), ACCURACY);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpJama.reconstruct(), ACCURACY);

        // TODO TestUtils.assertEquals("trace() Rational vs Complex", tmpBig.getTrace(), tmpComplex.getTrace(), PRECISION);
        TestUtils.assertEquals("trace() Complex vs Primitive", tmpComplex.getTrace(), tmpPrimitive.getTrace(), ACCURACY);
        TestUtils.assertEquals("trace() Primitive vs Jama", tmpPrimitive.getTrace(), tmpJama.getTrace(), ACCURACY);

        TestUtils.assertEquals("det() Rational vs Complex", tmpBig.getDeterminant(), tmpComplex.getDeterminant(), ACCURACY);
        TestUtils.assertEquals("det() Complex vs Primitive", tmpComplex.getDeterminant(), tmpPrimitive.getDeterminant(), ACCURACY);
        TestUtils.assertEquals("det() Primitive vs Jama", tmpPrimitive.getDeterminant(), tmpJama.getDeterminant(), ACCURACY);
    }

    @Test
    public void testInvertEvD_10_307_1() {

        Primitive64Store tmpOriginal = Primitive64Store.FACTORY.rows(new double[][] {
                { 1.488828119167862, 0.42210916029401624, 0.3090339419657017, 0.31968488522727556, 0.32307269871880584, 0.46899580731023627,
                        0.12091920407255509, 0.03795763520492966, 0.17470282114825963, 0.3946701200769135 },
                { 0.42210916029401624, 1.8635124366670595, 0.545906918558408, 0.5647217567560566, 0.570706312407284, 0.8284787565954789, 0.21360317145069477,
                        0.06705197344564522, 0.3086116630097931, 0.6971828004646068 },
                { 0.3090339419657017, 0.545906918558408, 1.632193464017115, 0.41344326780911667, 0.417824671952357, 0.6065446573280001, 0.1563828419260192,
                        0.04908999287306165, 0.22594032001124298, 0.5104204536764679 },
                { 0.31968488522727556, 0.5647217567560566, 0.41344326780911667, 1.6539821927009415, 0.43222511886101456, 0.6274493925480824,
                        0.16177262133291218, 0.05078189352797441, 0.23372741780909156, 0.528012240705021 },
                { 0.32307269871880584, 0.570706312407284, 0.417824671952357, 0.43222511886101456, 1.660912672676802, 0.6340986950817811, 0.1634869828633994,
                        0.051320047166039655, 0.23620430969852588, 0.5336077726660703 },
                { 0.46899580731023627, 0.8284787565954789, 0.6065446573280001, 0.6274493925480824, 0.6340986950817811, 1.959428864502749, 0.23732958500300408,
                        0.07449990991899043, 0.34289134104035285, 0.7746238203382216 },
                { 0.12091920407255509, 0.21360317145069477, 0.1563828419260192, 0.16177262133291218, 0.1634869828633994, 0.23732958500300408, 1.2473654835536,
                        0.019207996469193075, 0.08840622324485663, 0.19971798116519177 },
                { 0.03795763520492966, 0.06705197344564522, 0.04908999287306165, 0.05078189352797441, 0.051320047166039655, 0.07449990991899043,
                        0.019207996469193075, 1.0776502695252994, 0.027751515547194034, 0.06269328624082444 },
                { 0.17470282114825963, 0.3086116630097931, 0.22594032001124298, 0.23372741780909156, 0.23620430969852588, 0.34289134104035285,
                        0.08840622324485663, 0.027751515547194034, 1.3573911039439759, 0.2885504830370714 },
                { 0.3946701200769135, 0.6971828004646068, 0.5104204536764679, 0.528012240705021, 0.5336077726660703, 0.7746238203382216, 0.19971798116519177,
                        0.06269328624082444, 0.2885504830370714, 1.8073801497932753 } });
        tmpOriginal.modifyAll(MULTIPLY.second(POWER.invoke(TEN, 307)));

        RawEigenvalue.Symmetric tmpAlgorithm = new RawEigenvalue.Symmetric();

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(1));
    }

    @Test
    public void testInvertEvD_3_155_1() {

        Primitive64Store tmpOriginal = Primitive64Store.FACTORY.rows(new double[][] { { 1.509726074514643, 0.6439543946598099, 1.2096354379603502 },
                { 0.6439543946598099, 1.134228320145167, 0.8341376835908743 }, { 1.2096354379603502, 0.8341376835908743, 1.6999093634457072 } });
        tmpOriginal.modifyAll(MULTIPLY.second(POWER.invoke(TEN, 155)));

        Primitive tmpAlgorithm = new HermitianEvD.Primitive();

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(1));
    }

    @Test
    public void testInvertSVD_6_307_2() {

        Primitive64Store tmpOriginal = Primitive64Store.FACTORY.rows(
                new double[][] { { 1.7951923814808213, 0.659451350679988, 0.7107146253894259, 0.5763579411022435, 0.7199441830503458, 0.6356947473097578 },
                        { 0.659451350679988, 1.829297873115869, 0.7411968989569697, 0.6010777087922337, 0.7508223087524556, 0.6629594475153139 },
                        { 0.7107146253894259, 0.7411968989569697, 1.8937643794649044, 0.6478032355134435, 0.8091884190528792, 0.7144954285155056 },
                        { 0.5763579411022435, 0.6010777087922337, 0.6478032355134435, 1.7248031476721892, 0.6562158066095086, 0.5794240042274624 },
                        { 0.7199441830503458, 0.7508223087524556, 0.8091884190528792, 0.6562158066095086, 1.905371077260138, 0.7237740848430495 },
                        { 0.6356947473097578, 0.6629594475153139, 0.7144954285155056, 0.5794240042274624, 0.7237740848430495, 1.7994225826534653 } });
        tmpOriginal.modifyAll(MULTIPLY.second(POWER.invoke(TEN, 307)));

        RawSingularValue tmpAlgorithm = new RawSingularValue();

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(2));
    }

    @Test
    public void testInvertSVD_7_307_1() {

        Primitive64Store tmpOriginal = Primitive64Store.FACTORY.rows(new double[][] {
                { 1.6630365629391541, 0.5725332799439422, 0.6293312306387542, 0.3255116741968718, 0.16197060952553563, 0.38338065513999414,
                        0.45947212690705896 },
                { 0.5725332799439422, 1.8635018216883505, 0.8196058776803916, 0.42392824070490653, 0.2109414837777316, 0.4992935723573937, 0.5983908592318098 },
                { 0.6293312306387542, 0.8196058776803916, 1.949165198143842, 0.46598388385643336, 0.23186785507316293, 0.5488258051522601, 0.6577540014446122 },
                { 0.3255116741968718, 0.42392824070490653, 0.46598388385643336, 1.4909407601202584, 0.11992999873960987, 0.283871509914158,
                        0.3402129050589385 },
                { 0.16197060952553563, 0.2109414837777316, 0.23186785507316293, 0.11992999873960987, 1.2442860900574488, 0.14125097541024584,
                        0.16928576136879764 },
                { 0.38338065513999414, 0.4992935723573937, 0.5488258051522601, 0.283871509914158, 0.14125097541024584, 1.5782194777321448, 0.4006954489432253 },
                { 0.45947212690705896, 0.5983908592318098, 0.6577540014446122, 0.3402129050589385, 0.16928576136879764, 0.4006954489432253,
                        1.6929815829013701 } });
        tmpOriginal.modifyAll(MULTIPLY.second(POWER.invoke(TEN, 307)));

        SingularValueDecomposition.Primitive tmpAlgorithm = new SingularValueDecomposition.Primitive();

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(1));
    }

    @Test
    public void testInvertTask_2_155_1() {

        Primitive64Store tmpOriginal = Primitive64Store.FACTORY
                .rows(new double[][] { { 1.7755876870972727, 0.5243083105843722 }, { 0.5243083105843722, 1.6760142267686806 } });
        tmpOriginal.modifyAll(MULTIPLY.second(POWER.invoke(TEN, 155)));

        InverterTask<Double> tmpAlgorithm = InverterTask.PRIMITIVE.make(tmpOriginal);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(1));
    }

    @Test
    public void testLU() {

        MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        LU<RationalNumber> tmpRational = LU.RATIONAL.make();
        LU<ComplexNumber> tmpComplex = LU.COMPLEX.make();
        LU<Double> tmpPrimitive = LU.PRIMITIVE.make();
        LU<Double> tmpRaw = new RawLU();

        TestUtils.assertTrue("Rational.compute()", tmpRational.decompose(GenericStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(GenericStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpRaw.decompose(tmpProblematic));

        if (DEBUG) {
            BasicLogger.debugMatrix("Rational.L", tmpRational.getL());
            BasicLogger.debugMatrix("Complex.L", tmpComplex.getL());
            BasicLogger.debugMatrix("Primitive.L", tmpPrimitive.getL());
            BasicLogger.debugMatrix("Jama.L", tmpRaw.getL());
        }

        TestUtils.assertEquals("L Rational vs Complex", tmpRational.getL(), tmpComplex.getL(), ACCURACY);
        TestUtils.assertEquals("L Complex vs Primitive", tmpComplex.getL(), tmpPrimitive.getL(), ACCURACY);
        TestUtils.assertEquals("L Primitive vs Jama", tmpPrimitive.getL(), tmpRaw.getL(), ACCURACY);

        TestUtils.assertEquals("U Rational vs Complex", tmpRational.getU(), tmpComplex.getU(), ACCURACY);
        TestUtils.assertEquals("U Complex vs Primitive", tmpComplex.getU(), tmpPrimitive.getU(), ACCURACY);
        TestUtils.assertEquals("U Primitive vs Jama", tmpPrimitive.getU(), tmpRaw.getU(), ACCURACY);

        TestUtils.assertEquals("Rational.reconstruct()", tmpProblematic, tmpRational.reconstruct(), ACCURACY);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), ACCURACY);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), ACCURACY);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpRaw.reconstruct(), ACCURACY);

        SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Rational", tmpSVD.getRank(), tmpRational.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpRaw.getRank());
    }

    @Test
    public void testOverflowInvert() {
        ExtremeElementsCase.doTestInvert(true);
    }

    @Test
    public void testOverflowRank() {
        ExtremeElementsCase.doTestRank(true);
    }

    @Test
    public void testOverflowSolve() {
        ExtremeElementsCase.doTestSolve(true);
    }

    @Test
    public void testQR() {

        MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        QR<RationalNumber> tmpBig = QR.RATIONAL.make();
        QR<ComplexNumber> tmpComplex = QR.COMPLEX.make();
        QR<Double> tmpPrimitive = QR.PRIMITIVE.make();
        QR<Double> tmpJama = new RawQR();

        TestUtils.assertTrue("Rational.compute()", tmpBig.decompose(GenericStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(GenericStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debugMatrix("Rational Q", tmpBig.getQ());
            BasicLogger.debugMatrix("Complex Q", tmpComplex.getQ());
            BasicLogger.debugMatrix("Primitive Q", tmpPrimitive.getQ());
            BasicLogger.debugMatrix("Jama Q", tmpJama.getQ());
        }

        TestUtils.assertEquals("QR.reconstruct() Rational", tmpProblematic, tmpBig.reconstruct(), ACCURACY);
        TestUtils.assertEquals("QR.reconstruct() Complex", tmpProblematic, tmpComplex.reconstruct(), ACCURACY);
        TestUtils.assertEquals("QR.reconstruct() Primitive", tmpProblematic, tmpPrimitive.reconstruct(), ACCURACY);
        TestUtils.assertEquals("QR.reconstruct() Jama", tmpProblematic, tmpJama.reconstruct(), ACCURACY);

        SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Rational", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());
    }

    @Test
    public void testSolveLU_1_16_1() {

        Primitive64Store tmpBody = Primitive64Store.FACTORY.rows(new double[][] { { 1.7259687987824925 } });
        Primitive64Store tmpRHS = Primitive64Store.FACTORY.rows(new double[][] { { 0.6533251061005759 } });

        UnaryFunction<Double> tmpSecond = MULTIPLY.second(POWER.invoke(TEN, -16));
        tmpBody.modifyAll(tmpSecond);
        tmpRHS.modifyAll(tmpSecond);

        SolverTask<Double> tmpAlgorithm = new LUDecomposition.Primitive();

        ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, tmpAlgorithm, ACCURACY.withoutScale().withPrecision(1));
    }

    @Test
    public void testUnderflowInvert() {
        ExtremeElementsCase.doTestInvert(true);
    }

    @Test
    public void testUnderflowRank() {
        ExtremeElementsCase.doTestRank(false);
    }

    @Test
    public void testUnderflowSolve() {
        ExtremeElementsCase.doTestSolve(false);
    }

    @Test
    public void testUnderflowSVD() {
        ExtremeElementsCase.doTestSVD(false);
    }

    @Test
    public void testOverflowSVD() {
        ExtremeElementsCase.doTestSVD(true);
    }

}
