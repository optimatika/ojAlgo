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

import static org.ojalgo.matrix.decomposition.MatrixDecompositionTests.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.HermitianEvD.SimultaneousPrimitive;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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
public class ExtremeElementsCase {

    static final NumberContext PRECISION = new NumberContext().newPrecision(12).newScale(148);

    private static void performInvertTest(final PrimitiveDenseStore original, final InverterTask<Double> task, final NumberContext context) {

        try {

            final MatrixStore<Double> tmpInverse = task.invert(original);

            final MatrixStore<Double> tmpExpected = MatrixStore.PRIMITIVE.makeIdentity((int) original.countRows()).get();
            final MatrixStore<Double> tmpActual = original.multiply(tmpInverse);

            TestUtils.assertEquals(task.getClass().toString(), tmpExpected, tmpActual, context);

        } catch (final RecoverableCondition exception) {
            TestUtils.fail(task.getClass() + " " + exception.toString());
        }

    }

    private static void performSolveTest(final PrimitiveDenseStore body, final PrimitiveDenseStore rhs, final SolverTask<Double> task,
            final NumberContext context) {

        try {

            final MatrixStore<Double> tmpSolution = task.solve(body, rhs);

            final MatrixStore<Double> tmpExpected = rhs;
            final MatrixStore<Double> tmpActual = body.multiply(tmpSolution);

            TestUtils.assertEquals(task.getClass().toString(), tmpExpected, tmpActual, context);

        } catch (final RecoverableCondition exception) {
            TestUtils.fail(task.getClass() + " " + exception.toString());
        }

    }

    static void doTestInvert(final boolean large) {

        for (int precision = 1; precision <= 16; precision++) {
            final NumberContext tmpContext = NumberContext.getGeneral(precision, Integer.MIN_VALUE);

            for (int dim = 1; dim <= 10; dim++) {

                // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
                for (int exp = 0; exp < 308; exp++) {
                    final double tmpScale = PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, large ? exp : -exp);

                    final PrimitiveDenseStore tmpOriginal = MatrixUtils.makeSPD(dim);
                    if (DEBUG) {
                        BasicLogger.debug("Scale exp={} => factor={} and context={}", exp, tmpScale, tmpContext);
                        BasicLogger.debug("Original (unscaled) {}", tmpOriginal.toString());

                    }
                    tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(tmpScale));

                    ExtremeElementsCase.performInvertTest(tmpOriginal, InverterTask.PRIMITIVE.make(tmpOriginal), tmpContext);

                    final List<MatrixDecomposition<Double>> tmpAllDecomps = MatrixDecompositionTests.getAllPrimitive();
                    for (final MatrixDecomposition<Double> tmpDecomp : tmpAllDecomps) {

                        if (DEBUG) {
                            BasicLogger.debug("{} at dim={} for scale={}", tmpDecomp.getClass(), dim, tmpScale);
                        }
                        if (tmpDecomp instanceof MatrixDecomposition.Solver) {
                            ExtremeElementsCase.performInvertTest(tmpOriginal, (InverterTask<Double>) tmpDecomp, tmpContext);
                        }
                    }
                }
            }
        }
    }

    static void doTestSolve(final boolean large) {

        for (int precision = 1; precision <= 16; precision++) {
            final NumberContext tmpContext = NumberContext.getGeneral(precision, Integer.MIN_VALUE);

            for (int dim = 1; dim <= 10; dim++) {

                // exp = 308 could potentially create numbers that are 2E308 which is larger than Double.MAX_VALUE
                for (int exp = 0; exp < 308; exp++) {
                    final double tmpScale = PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, large ? exp : -exp);

                    final PrimitiveDenseStore tmpBody = MatrixUtils.makeSPD(dim);
                    final PrimitiveDenseStore tmpRHS = PrimitiveDenseStore.FACTORY.makeFilled(dim, 1, new Uniform());
                    if (DEBUG) {
                        BasicLogger.debug("Scale exp={} => factor={} and context={}", exp, tmpScale, tmpContext);
                        BasicLogger.debug("Body (unscaled) {}", tmpBody.toString());
                        BasicLogger.debug("RHS (unscaled) {}", tmpRHS.toString());
                    }
                    final UnaryFunction<Double> tmpModifier = PrimitiveFunction.MULTIPLY.second(tmpScale);
                    tmpBody.modifyAll(tmpModifier);
                    tmpRHS.modifyAll(tmpModifier);

                    ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, SolverTask.PRIMITIVE.make(tmpBody, tmpRHS), tmpContext);

                    final List<MatrixDecomposition<Double>> tmpAllDecomps = MatrixDecompositionTests.getAllPrimitive();
                    for (final MatrixDecomposition<Double> tmpDecomp : tmpAllDecomps) {

                        if (DEBUG) {
                            BasicLogger.debug("{} at dim={} for scale={}", tmpDecomp.getClass(), dim, tmpScale);
                        }
                        if (tmpDecomp instanceof MatrixDecomposition.Solver) {
                            ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, (SolverTask<Double>) tmpDecomp, tmpContext);
                        }
                    }
                }
            }
        }
    }

    static MatrixStore<Double> getVerySmall() {

        final long tmpDim = 5L;

        final PrimitiveDenseStore tmpRndm = PrimitiveDenseStore.FACTORY.makeZero(tmpDim, tmpDim);

        for (long j = 0L; j < tmpDim; j++) {
            for (long i = 0L; i < tmpDim; i++) {
                tmpRndm.set(i, j, Uniform.randomInteger(4));
            }
        }

        return tmpRndm.transpose().multiply(tmpRndm).multiply(1E-150);
    }

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testEvD() {

        final MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        final Eigenvalue<RationalNumber> tmpBig = Eigenvalue.RATIONAL.make(true);
        final Eigenvalue<ComplexNumber> tmpComplex = Eigenvalue.COMPLEX.make(true);
        final Eigenvalue<Double> tmpPrimitive = Eigenvalue.PRIMITIVE.make();
        final Eigenvalue<Double> tmpJama = new RawEigenvalue.Dynamic();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big: {}", tmpBig.getEigenvalues());
            BasicLogger.debug("Complex: {}", tmpComplex.getEigenvalues());
            BasicLogger.debug("Primitive: {}", tmpPrimitive.getEigenvalues());
            BasicLogger.debug("Jama: {}", tmpJama.getEigenvalues());
        }

        // TestUtils.assertEquals("QR.Q Big vs Complex", tmpBig.getQ(), tmpComplex.getQ());
        // TestUtils.assertEquals("QR.Q Complex vs Primitive", tmpComplex.getQ(), tmpPrimitive.getQ());
        // TestUtils.assertEquals("QR.Q Primitive vs Jama", tmpPrimitive.getQ(), tmpJama.getQ());

        TestUtils.assertEquals("EvD Big vs Complex", tmpBig.getEigenvalues().get(0), tmpComplex.getEigenvalues().get(0), PRECISION);
        TestUtils.assertEquals("EvD Complex vs Primitive", tmpComplex.getEigenvalues().get(0), tmpPrimitive.getEigenvalues().get(0), PRECISION);
        TestUtils.assertEquals("EvD Primitive vs Jama", tmpPrimitive.getEigenvalues().get(0), tmpJama.getEigenvalues().get(0), PRECISION);

        TestUtils.assertEquals("Big.reconstruct()", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        TestUtils.assertEquals("trace() Big vs Complex", tmpBig.getTrace(), tmpComplex.getTrace(), PRECISION);
        TestUtils.assertEquals("trace() Complex vs Primitive", tmpComplex.getTrace(), tmpPrimitive.getTrace(), PRECISION);
        TestUtils.assertEquals("trace() Primitive vs Jama", tmpPrimitive.getTrace(), tmpJama.getTrace(), PRECISION);

        TestUtils.assertEquals("det() Big vs Complex", tmpBig.getDeterminant(), tmpComplex.getDeterminant(), PRECISION);
        TestUtils.assertEquals("det() Complex vs Primitive", tmpComplex.getDeterminant(), tmpPrimitive.getDeterminant(), PRECISION);
        TestUtils.assertEquals("det() Primitive vs Jama", tmpPrimitive.getDeterminant(), tmpJama.getDeterminant(), PRECISION);

    }

    @Test
    public void testInvertEvD_10_307_1() {

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY.rows(new double[][] {
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
        tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, 307)));

        final RawEigenvalue.Symmetric tmpAlgorithm = new RawEigenvalue.Symmetric();

        final NumberContext tmpContext = NumberContext.getGeneral(1, Integer.MIN_VALUE);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, tmpContext);
    }

    @Test
    public void testInvertEvD_3_155_1() {

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.509726074514643, 0.6439543946598099, 1.2096354379603502 },
                { 0.6439543946598099, 1.134228320145167, 0.8341376835908743 }, { 1.2096354379603502, 0.8341376835908743, 1.6999093634457072 } });
        tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, 155)));

        final SimultaneousPrimitive tmpAlgorithm = new HermitianEvD.SimultaneousPrimitive();

        final NumberContext tmpContext = NumberContext.getGeneral(1, Integer.MIN_VALUE);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, tmpContext);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testInvertOverflow() {
        ExtremeElementsCase.doTestInvert(true);
    }

    @Test
    public void testInvertSVD_6_307_2() {

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY.rows(
                new double[][] { { 1.7951923814808213, 0.659451350679988, 0.7107146253894259, 0.5763579411022435, 0.7199441830503458, 0.6356947473097578 },
                        { 0.659451350679988, 1.829297873115869, 0.7411968989569697, 0.6010777087922337, 0.7508223087524556, 0.6629594475153139 },
                        { 0.7107146253894259, 0.7411968989569697, 1.8937643794649044, 0.6478032355134435, 0.8091884190528792, 0.7144954285155056 },
                        { 0.5763579411022435, 0.6010777087922337, 0.6478032355134435, 1.7248031476721892, 0.6562158066095086, 0.5794240042274624 },
                        { 0.7199441830503458, 0.7508223087524556, 0.8091884190528792, 0.6562158066095086, 1.905371077260138, 0.7237740848430495 },
                        { 0.6356947473097578, 0.6629594475153139, 0.7144954285155056, 0.5794240042274624, 0.7237740848430495, 1.7994225826534653 } });
        tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, 307)));

        final RawSingularValue tmpAlgorithm = new RawSingularValue();

        final NumberContext tmpContext = NumberContext.getGeneral(2, Integer.MIN_VALUE);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, tmpContext);
    }

    @Test
    public void testInvertSVD_7_307_1() {

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY.rows(new double[][] {
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
        tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, 307)));

        final SingularValueDecomposition.Primitive tmpAlgorithm = new SingularValueDecomposition.Primitive();

        final NumberContext tmpContext = NumberContext.getGeneral(1, Integer.MIN_VALUE);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, tmpContext);
    }

    @Test
    public void testInvertTask_2_155_1() {

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 1.7755876870972727, 0.5243083105843722 }, { 0.5243083105843722, 1.6760142267686806 } });
        tmpOriginal.modifyAll(PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, 155)));

        final InverterTask<Double> tmpAlgorithm = InverterTask.PRIMITIVE.make(tmpOriginal);

        final NumberContext tmpContext = NumberContext.getGeneral(1, Integer.MIN_VALUE);

        ExtremeElementsCase.performInvertTest(tmpOriginal, tmpAlgorithm, tmpContext);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testInvertUnderflow() {
        ExtremeElementsCase.doTestInvert(true);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testLU() {

        final MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        final LU<RationalNumber> tmpBig = LU.RATIONAL.make();
        final LU<ComplexNumber> tmpComplex = LU.COMPLEX.make();
        final LU<Double> tmpPrimitive = LU.PRIMITIVE.make();
        final LU<Double> tmpJama = new RawLU();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (DEBUG) {
            BasicLogger.debug("Big.L", tmpBig.getL());
            BasicLogger.debug("Complex.L", tmpComplex.getL());
            BasicLogger.debug("Primitive.L", tmpPrimitive.getL());
            BasicLogger.debug("Jama.L", tmpJama.getL());
        }

        TestUtils.assertEquals("L Big vs Complex", tmpBig.getL(), tmpComplex.getL(), PRECISION);
        TestUtils.assertEquals("L Complex vs Primitive", tmpComplex.getL(), tmpPrimitive.getL(), PRECISION);
        TestUtils.assertEquals("L Primitive vs Jama", tmpPrimitive.getL(), tmpJama.getL(), PRECISION);

        TestUtils.assertEquals("U Big vs Complex", tmpBig.getU(), tmpComplex.getU(), PRECISION);
        TestUtils.assertEquals("U Complex vs Primitive", tmpComplex.getU(), tmpPrimitive.getU(), PRECISION);
        TestUtils.assertEquals("U Primitive vs Jama", tmpPrimitive.getU(), tmpJama.getU(), PRECISION);

        TestUtils.assertEquals("Big.reconstruct()", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("Complex.reconstruct()", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("Primitive.reconstruct()", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("Jama.reconstruct()", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

    @Disabled("Underscored before JUnit 5")
    @Test
    public void testQR() {

        final MatrixStore<Double> tmpProblematic = ExtremeElementsCase.getVerySmall();

        final QR<RationalNumber> tmpBig = QR.RATIONAL.make();
        final QR<ComplexNumber> tmpComplex = QR.COMPLEX.make();
        final QR<Double> tmpPrimitive = QR.PRIMITIVE.make();
        final QR<Double> tmpJama = new RawQR();

        TestUtils.assertTrue("Big.compute()", tmpBig.decompose(MatrixStore.RATIONAL.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Complex.compute()", tmpComplex.decompose(MatrixStore.COMPLEX.makeWrapper(tmpProblematic)));
        TestUtils.assertTrue("Primitive.compute()", tmpPrimitive.decompose(tmpProblematic));
        TestUtils.assertTrue("Jama.compute()", tmpJama.decompose(tmpProblematic));

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big Q", tmpBig.getQ());
            BasicLogger.debug("Complex Q", tmpComplex.getQ());
            BasicLogger.debug("Primitive Q", tmpPrimitive.getQ());
            BasicLogger.debug("Jama Q", tmpJama.getQ());
        }

        TestUtils.assertEquals("QR.reconstruct() Big", tmpProblematic, tmpBig.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Complex", tmpProblematic, tmpComplex.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Primitive", tmpProblematic, tmpPrimitive.reconstruct(), PRECISION);
        TestUtils.assertEquals("QR.reconstruct() Jama", tmpProblematic, tmpJama.reconstruct(), PRECISION);

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(tmpProblematic);

        TestUtils.assertEquals("rank() SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("rank() SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("rank() SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("rank() SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

    @Test
    public void testSolveLU_1_16_1() {

        final PrimitiveDenseStore tmpBody = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.7259687987824925 } });
        final PrimitiveDenseStore tmpRHS = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.6533251061005759 } });

        final UnaryFunction<Double> tmpSecond = PrimitiveFunction.MULTIPLY.second(PrimitiveFunction.POWER.invoke(PrimitiveMath.TEN, -16));
        tmpBody.modifyAll(tmpSecond);
        tmpRHS.modifyAll(tmpSecond);

        final SolverTask<Double> tmpAlgorithm = new LUDecomposition.Primitive();

        final NumberContext tmpContext = NumberContext.getGeneral(1, Integer.MIN_VALUE);

        ExtremeElementsCase.performSolveTest(tmpBody, tmpRHS, tmpAlgorithm, tmpContext);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testSolveOverflow() {
        ExtremeElementsCase.doTestSolve(true);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testSolveUnderflow() {
        ExtremeElementsCase.doTestSolve(false);
    }

}
