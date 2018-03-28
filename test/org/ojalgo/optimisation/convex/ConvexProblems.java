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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.constant.BigMath.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;
import org.ojalgo.ProgrammingError;
import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.BigMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.multiary.CompoundFunction;
import org.ojalgo.function.multiary.MultiaryFunction.TwiceDifferentiable;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.BigDenseStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexSolver.Builder;
import org.ojalgo.type.StandardType;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public class ConvexProblems extends OptimisationConvexTests {

    private static void builAndTestModel(final PrimitiveDenseStore[] matrices, final double[] expectedSolution, final NumberContext modelValidationContext,
            final boolean testSolverDirectly) {

        final PrimitiveDenseStore tmpExpectedSolution = PrimitiveDenseStore.FACTORY.columns(expectedSolution);

        ConvexProblems.builAndTestModel(matrices, tmpExpectedSolution, modelValidationContext, testSolverDirectly);
    }

    static void builAndTestModel(final PrimitiveDenseStore[] matrices, final PrimitiveDenseStore expectedSolution, final NumberContext modelValidationContext,
            final boolean testSolverDirectly) {

        final MatrixStore<Double> tmpPartQ = expectedSolution.transpose().multiply(matrices[2].multiply(expectedSolution));
        final MatrixStore<Double> tmpPartC = matrices[3].transpose().multiply(expectedSolution);

        final double tmpExpectedValue = tmpPartQ.multiply(HALF.doubleValue()).subtract(tmpPartC).doubleValue(0);

        final Optimisation.Result tmpExpectedResult = new Optimisation.Result(Optimisation.State.OPTIMAL, tmpExpectedValue, expectedSolution);

        final ExpressionsBasedModel tmpModel = ConvexProblems.buildModel(matrices, expectedSolution);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel, modelValidationContext);

        if (DEBUG) {
            tmpModel.options.debug(ConvexSolver.class);
            tmpModel.options.validate = false;
        }

        TestUtils.assertTrue("Expected solution not ok!", tmpModel.validate(tmpExpectedResult, modelValidationContext));
        TestUtils.assertTrue("Expected solution not ok!", tmpModel.validate(modelValidationContext)); // The expected solution is written to the variables

        // When/if the correct/optimal solution is used to kickStart ojAlgo should return that solution
        final Result tmpInitialisedModelResult = tmpModel.minimise();
        TestUtils.assertStateNotLessThanOptimal(tmpInitialisedModelResult);
        TestUtils.assertEquals(tmpExpectedResult, tmpInitialisedModelResult, modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpInitialisedModelResult.getValue(), modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpModel.objective().evaluate(tmpInitialisedModelResult).doubleValue(), modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpModel.objective().toFunction().invoke(expectedSolution).doubleValue(), modelValidationContext);

        for (final Variable tmpVariable : tmpModel.getVariables()) {
            tmpVariable.setValue(null);
        }

        // Initial variable values have been cleared
        final Result tmpUninitialisedModelResult = tmpModel.minimise();
        TestUtils.assertStateNotLessThanOptimal(tmpUninitialisedModelResult);
        TestUtils.assertEquals(tmpExpectedResult, tmpUninitialisedModelResult, modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpUninitialisedModelResult.getValue(), modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpModel.objective().evaluate(tmpUninitialisedModelResult).doubleValue(), modelValidationContext);
        TestUtils.assertEquals(tmpExpectedValue, tmpModel.objective().toFunction().invoke(expectedSolution).doubleValue(), modelValidationContext);

        if (testSolverDirectly) {

            final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(matrices);
            final ConvexSolver tmpSolver = tmpBuilder.build();
            // tmpSolver.options.debug(ConvexSolver.class);
            // tmpSolver.options.validate = false;
            final Optimisation.Result tmpResult = tmpSolver.solve();

            TestUtils.assertStateNotLessThanOptimal(tmpResult);
            TestUtils.assertEquals(tmpExpectedResult, tmpResult, NumberContext.getGeneral(2, 4));
            TestUtils.assertEquals(tmpExpectedValue, tmpModel.objective().evaluate(tmpResult).doubleValue(), NumberContext.getGeneral(4, 8));
        }
    }

    static ExpressionsBasedModel buildModel(final PrimitiveDenseStore[] matrices, final PrimitiveDenseStore expectedSolution) {

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        final int tmpNumberOfVariables = (int) matrices[3].count();

        for (int v = 0; v < tmpNumberOfVariables; v++) {
            final Variable tmpVariable = Variable.make("X" + v);
            tmpVariable.setValue(BigDecimal.valueOf(expectedSolution.doubleValue(v)));
            retVal.addVariable(tmpVariable);
        }
        if ((matrices[0] != null) && (matrices[1] != null)) {
            for (int e = 0; e < matrices[0].countRows(); e++) {
                final Expression tmpExpression = retVal.addExpression("E" + e);
                for (int v = 0; v < tmpNumberOfVariables; v++) {
                    tmpExpression.set(v, matrices[0].get(e, v));
                }
                tmpExpression.level(matrices[1].doubleValue(e));
            }
        }
        if ((matrices[4] != null) && (matrices[5] != null)) {
            for (int i = 0; i < matrices[4].countRows(); i++) {
                final Expression tmpExpression = retVal.addExpression("I" + i);
                for (int v = 0; v < tmpNumberOfVariables; v++) {
                    tmpExpression.set(v, matrices[4].get(i, v));
                }
                tmpExpression.upper(matrices[5].doubleValue(i));
            }
        }
        final Expression tmpObjQ = retVal.addExpression("Q");
        for (int r = 0; r < tmpNumberOfVariables; r++) {
            for (int v = 0; v < tmpNumberOfVariables; v++) {
                tmpObjQ.set(r, v, matrices[2].doubleValue(r, v));
            }
        }
        tmpObjQ.weight(HALF);
        final Expression tmpObjC = retVal.addExpression("C");
        for (int v = 0; v < tmpNumberOfVariables; v++) {
            tmpObjC.set(v, matrices[3].doubleValue(v));
        }
        tmpObjC.weight(NEG);

        return retVal;
    }

    static void doEarly2008(final Variable[] variables, final Access2D<?> covariances, final Access1D<?> expected) {

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(variables);

        final Expression tmpVariance = tmpModel.addExpression("Variance");
        tmpVariance.setQuadraticFactors(tmpModel.getVariables(), covariances);
        tmpVariance.weight(BigMath.PI.multiply(BigMath.E).multiply(BigMath.HALF));

        final Expression tmpBalance = tmpModel.addExpression("Balance");
        tmpBalance.setLinearFactorsSimple(tmpModel.getVariables());
        tmpBalance.level(BigMath.ONE);

        //        tmpModel.options.debug(ConvexSolver.class);
        //        tmpModel.options.validate = false;
        final Result tmpActualResult = tmpModel.minimise();

        final NumberContext tmpAccuracy = StandardType.PERCENT.newPrecision(5);

        TestUtils.assertTrue(tmpModel.validate(Array1D.BIG.copy(expected), tmpAccuracy));
        TestUtils.assertTrue(tmpModel.validate(tmpActualResult, tmpAccuracy));

        final TwiceDifferentiable<Double> tmpObjective = tmpModel.objective().toFunction();
        final double tmpExpObjFuncVal = tmpObjective.invoke(Access1D.asPrimitive1D(expected));
        final double tmpActObjFuncVal = tmpObjective.invoke(Access1D.asPrimitive1D(tmpActualResult));
        TestUtils.assertEquals(tmpExpObjFuncVal, tmpActObjFuncVal, tmpAccuracy);

        TestUtils.assertEquals(expected, tmpActualResult, tmpAccuracy);

        // Test that the LinearSolver can determine feasibility

        final ExpressionsBasedModel relaxedModel = tmpModel.relax(false);

        final Optimisation.Result tmpLinearResult = relaxedModel.minimise();
        TestUtils.assertStateNotLessThanFeasible(tmpLinearResult);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel, tmpAccuracy);
    }

    /**
     * Just make sure an obviously infeasible problem is recognised as such - this has been a problem in the
     * past
     */
    @Test
    public void testInfeasibleCase() {

        final Variable[] tmpVariables = new Variable[] { new Variable("X1").lower(ONE).upper(TWO).weight(ONE),
                new Variable("X2").lower(ONE).upper(TWO).weight(TWO), new Variable("X3").lower(ONE).upper(TWO).weight(THREE) };

        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpExprQ = tmpModel.addExpression("Q1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            for (int j = 0; j < tmpModel.countVariables(); j++) {
                tmpExprQ.set(i, i, Math.random());
            }
        } // May not be positive definite, but infeasibillity should be realised before that becomes a problem
        tmpExprQ.weight(TEN);

        // tmpModel.options.debug(ConvexSolver.class);

        final Expression tmpExprC1 = tmpModel.addExpression("C1");
        for (int i = 0; i < tmpModel.countVariables(); i++) {
            tmpExprC1.set(i, ONE);
        }
        tmpExprC1.upper(TWO);

        Optimisation.Result tmpResult = tmpModel.maximise();

        TestUtils.assertFalse(tmpResult.getState().isFeasible());

        tmpExprC1.upper(null);
        tmpExprC1.lower(SEVEN);

        tmpResult = tmpModel.maximise();

        TestUtils.assertFalse(tmpResult.getState().isFeasible());

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel, null);
    }

    /**
     * The ActiveSetSolver ended up in a loop activating/deactivating constraints. Eventually it returned
     * null, and that eventually resulted in a NullPointerException. Since Q is not positive semidefinite
     * validation has to be turned off
     */
    @Test
    public void testP20080117() {

        // create expected returns matrix
        final PrimitiveMatrix tmpReturns = PrimitiveMatrix.FACTORY.rows(new double[][] { { -0.007155942261937039 }, { -0.003665887902733331 },
                { -0.004130184341000032 }, { -0.005639860515211043 }, { 0.0007211966666666817 }, { 0.0003258225000000077 }, { -0.005754291666666666 },
                { -0.004264291666666667 }, { -0.0017500000000000003 } });

        // create covariance matrix
        final PrimitiveMatrix tmpCovariances = PrimitiveMatrix.FACTORY.rows(new double[][] {
                { 0.001561410465201063, 0.00006366128201274021, -0.0001323096896759724, 0.0000909074052724909, 0.00003172000033558704, 0.00001955483223848944,
                        -0.00013771504482647386, -0.00004858457275314645, -0.000012954723060403266 },
                { 0.00006366128201274021, 0.00016419786524761803, -0.00001566288911558343, -0.00008688646089751923, 0.0000027349925543017186,
                        0.0000012356159598500247, -0.000024367796639005863, -0.000017576048221096555, -0.0000070052245518771815 },
                { -0.0001323096896759724, -0.00001566288911558343, 0.0001430155985985913, 0.00007675339168559199, -0.00007600590426518823,
                        0.000032976538909267937, 0.00009520305608240259, 0.00007373075639042642, -0.000007477057858706954 },
                { 0.0000909074052724909, -0.00008688646089751923, 0.00007675339168559199, 0.000967519991100896, -0.0000533460293834595, 0.00008665760416026126,
                        0.00014591175388747613, 0.0001232364989586903, 0.00011097998789484925 },
                { 0.00003172000033558704, 0.0000027349925543017186, -0.00007600590426518823, -0.0000533460293834595, 0.000025267064307337795,
                        -0.00003089584520279407, -0.00005593123237578969, -0.000017013960349712132, 0.0000013056146551724419 },
                { 0.00001955483223848944, 0.0000012356159598500247, 0.000032976538909267937, 0.00008665760416026126, -0.00003089584520279407,
                        0.0001625499447274783, 0.00008242949058588471, 0.00010276895784859992, 0.0000005898510775862205 },
                { -0.00013771504482647386, -0.000024367796639005863, 0.00009520305608240259, 0.00014591175388747613, -0.00005593123237578969,
                        0.00008242949058588471, 0.000560956958802083, 0.0002838794236862429, 0.00009143821659482758 },
                { -0.00004858457275314645, -0.000017576048221096555, 0.00007373075639042642, 0.0001232364989586903, -0.000017013960349712132,
                        0.00010276895784859992, 0.0002838794236862429, 0.00021068964250359204, 0.00004461044181034483 },
                { -0.000012954723060403266, -0.0000070052245518771815, -0.000007477057858706954, 0.00011097998789484925, 0.0000013056146551724419,
                        0.0000005898510775862205, 0.00009143821659482758, 0.00004461044181034483, 0.00006761920797413792 } });

        //        final MarketEquilibrium tmpME = new MarketEquilibrium(tmpCovariances, BigMath.PI.multiply(BigMath.E));

        // create asset variables - cost and weighting constraints
        final Variable[] tmpVariables = new Variable[(int) tmpReturns.countRows()];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("VAR" + i);
            final int row = i;
            tmpVariables[i].weight(TypeUtils.toBigDecimal(tmpReturns.get(row, 0)).negate());
            // set the constraints on the asset weights
            // require at least a 2% allocation to each asset
            tmpVariables[i].lower(new BigDecimal("0.02"));
            // require no more than 80% allocation to each asset
            tmpVariables[i].upper(new BigDecimal("0.80"));
        }

        final RationalMatrix tmpExpected = RationalMatrix.FACTORY
                .rows(new double[][] { { 0.02 }, { 0.02 }, { 0.02 }, { 0.02 }, { 0.80 }, { 0.06 }, { 0.02 }, { 0.02 }, { 0.02 } });

        ConvexProblems.doEarly2008(tmpVariables, tmpCovariances, tmpExpected);
    }

    /**
     * Ended up with a singular matrix (the equation system body generated by the LagrangeSolver) that
     * resulted in a solution with NaN and Inf elements. This was not recognised and handled.
     */
    @Test
    public void testP20080118() {

        // create expected returns matrix
        final PrimitiveMatrix expectedReturnsMatrix = PrimitiveMatrix.FACTORY.rows(new double[][] { { 10.003264 }, { 9.989771 }, { 9.987513 }, { 9.988449 },
                { 9.996579 }, { 9.990690 }, { 9.994904 }, { 9.994514 }, { 9.984064 }, { 9.987534 } });

        // create covariance matrix
        final PrimitiveMatrix covarianceMatrix = PrimitiveMatrix.FACTORY.rows(new double[][] {
                { 6.483565230120298E-4, -1.3344603795915894E-4, -4.610345510893708E-4, -7.334405624030001E-4, 1.1551383115707195E-5, -0.00104145662863434,
                        -1.0725896685568462E-4, -1.221384153392056E-4, -4.173413644389791E-4, -2.4861043894946935E-4 },
                { -1.3344603795915894E-4, 0.0026045957224784455, 0.0012394355327235707, 9.243919166568456E-4, -8.653805945112411E-5, 8.100239312410631E-4,
                        4.215960274481846E-4, 5.243272007211247E-4, 0.0013062718630332956, 1.4766450293395405E-4 },
                { -4.610345510893708E-4, 0.0012394355327235707, 0.002361436913752224, 0.0020101714731002238, -1.4236763916609785E-5, 0.002120395905829043,
                        5.399158658928662E-4, 5.048790842067473E-4, 0.0014855261720730444, 4.841458106181396E-4 },
                { -7.334405624030001E-4, 9.243919166568456E-4, 0.0020101714731002238, 0.0028542819089926895, -4.311102526746861E-6, 0.0028465650900869476,
                        6.242643883624462E-4, 4.086484048798765E-4, 0.001647437646316569, 7.58419663970477E-4 },
                { 1.1551383115707195E-5, -8.653805945112411E-5, -1.4236763916609785E-5, -4.311102526746861E-6, 1.213366124417227E-4, -9.027529241741836E-5,
                        7.241389994693716E-6, -3.166855950737129E-5, -1.2445276374560802E-5, -5.3976919759028745E-5 },
                { -0.00104145662863434, 8.100239312410631E-4, 0.002120395905829043, 0.0028465650900869476, -9.027529241741836E-5, 0.0064756879298965295,
                        2.8076277564885113E-4, 3.6082073553997553E-4, 0.001945238279500792, 0.0012421132342988626 },
                { -1.0725896685568462E-4, 4.215960274481846E-4, 5.399158658928662E-4, 6.242643883624462E-4, 7.241389994693716E-6, 2.8076277564885113E-4,
                        0.0010121500024739688, 8.206099676659543E-5, 1.6129237403855146E-4, 7.550465994733837E-4 },
                { -1.221384153392056E-4, 5.243272007211247E-4, 5.048790842067473E-4, 4.086484048798765E-4, -3.166855950737129E-5, 3.6082073553997553E-4,
                        8.206099676659543E-5, 4.504461842318998E-4, 4.7980942831718363E-4, -4.763223568683059E-5 },
                { -4.173413644389791E-4, 0.0013062718630332956, 0.0014855261720730444, 0.001647437646316569, -1.2445276374560802E-5, 0.001945238279500792,
                        1.6129237403855146E-4, 4.7980942831718363E-4, 0.002228245076175045, 3.2083564921169634E-4 },
                { -2.4861043894946935E-4, 1.4766450293395405E-4, 4.841458106181396E-4, 7.58419663970477E-4, -5.3976919759028745E-5, 0.0012421132342988626,
                        7.550465994733837E-4, -4.763223568683059E-5, 3.2083564921169634E-4, 0.0017093327832123186 } });

        // create asset variables - cost and weighting constraints
        final Variable[] tmpVariables = new Variable[(int) expectedReturnsMatrix.countRows()];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("VAR" + i);
            final int row = i;
            tmpVariables[i].weight(TypeUtils.toBigDecimal(expectedReturnsMatrix.get(row, 0)).negate());
            // set the constraints on the asset weights
            // require at least a 2% allocation to each asset
            tmpVariables[i].lower(new BigDecimal("0.05"));
            // require no more than 80% allocation to each asset
            tmpVariables[i].upper(new BigDecimal("0.35"));
        }

        final RationalMatrix tmpExpected = RationalMatrix.FACTORY
                .rows(new double[][] { { 0.35 }, { 0.05 }, { 0.05 }, { 0.05 }, { 0.25 }, { 0.05 }, { 0.05 }, { 0.05 }, { 0.05 }, { 0.05 } });

        ConvexProblems.doEarly2008(tmpVariables, covarianceMatrix, tmpExpected);
    }

    /**
     * Another case of looping in the ActiveSetSolver's constraint (de)activation.
     */
    @Test
    public void testP20080124() {
        // create expected returns matrix
        final PrimitiveMatrix expectedReturnsMatrix = PrimitiveMatrix.FACTORY.rows(new double[][] { { 10.012158 }, { 9.996046 }, { 10.000744 }, { 9.990585 },
                { 9.998392 }, { 9.996614 }, { 10.010531 }, { 10.001401 }, { 9.997447 }, { 9.993817 }, { 9.998537 }, { 9.995741 }, { 9.987224 }, { 9.992392 } });
        // create covariance matrix
        final PrimitiveMatrix covarianceMatrix = PrimitiveMatrix.FACTORY.rows(new double[][] {
                { 0.0013191354374342357, 7.786471466322114E-5, -3.810886655309235E-5, -2.28102405899103E-4, -1.2589115740653127E-4, -1.3247692268411991E-5,
                        1.422624656557158E-4, -2.7176361887359125E-5, 8.675127894495302E-5, -8.116577287090551E-5, -8.468380774247271E-6, 4.930080166695193E-5,
                        -2.774138231533918E-4, -3.148322898570031E-5 },
                { 7.786471466322114E-5, 0.001028250547816086, 8.986425197170406E-4, -1.0341435238579975E-5, 6.472902968147139E-4, 2.9014435841747375E-4,
                        1.0640414444602855E-4, 5.638694128451113E-4, 6.024515366195699E-4, -1.094867665517237E-4, 6.177221606260711E-6, -5.682215091954099E-5,
                        2.7178074500896235E-4, 0.0010146062950574643 },
                { -3.810886655309235E-5, 8.986425197170406E-4, 0.0012477403456464075, -1.8104847201530489E-4, 9.299199981666304E-4, 3.486383951982303E-4,
                        1.0246402606579107E-4, 7.009722990366382E-4, 6.545695073447614E-4, -1.1680969171500155E-4, 7.123493385355658E-5, 1.559414390174896E-5,
                        1.972605480880284E-4, 9.368808845809186E-4 },
                { -2.28102405899103E-4, -1.0341435238579975E-5, -1.8104847201530489E-4, 6.250793590180099E-4, -5.4721911720097E-6, 1.3081826023829458E-4,
                        -5.644046856412501E-5, -1.1282043806099452E-5, -6.729835202722053E-5, 1.3929681542737307E-4, 3.698155248637573E-6,
                        5.0269944317023966E-5, 5.344931460074395E-4, -1.1654882792112444E-4 },
                { -1.2589115740653127E-4, 6.472902968147139E-4, 9.299199981666304E-4, -5.4721911720097E-6, 0.001181357476541527, 3.0334522038028824E-4,
                        2.6983840497611894E-4, 6.983493701701867E-4, 5.68816790613126E-4, -7.899505299987754E-5, 1.05074262063586E-5, 1.137295188785598E-4,
                        1.9732025136606058E-4, 6.631330613471645E-4 },
                { -1.3247692268411991E-5, 2.9014435841747375E-4, 3.486383951982303E-4, 1.3081826023829458E-4, 3.0334522038028824E-4, 3.372068413122505E-4,
                        1.1067468759384309E-4, 2.6589126866881173E-4, 2.1364931019670806E-4, -4.201239472520589E-5, 2.32769639721745E-5, 5.847559594073046E-6,
                        1.9925897592339058E-4, 1.9671375386540353E-4 },
                { 1.422624656557158E-4, 1.0640414444602855E-4, 1.0246402606579107E-4, -5.644046856412501E-5, 2.6983840497611894E-4, 1.1067468759384309E-4,
                        0.001484755064835215, 1.2295961703024863E-4, 1.0843198781689372E-4, -2.1292328294313923E-5, -4.152686600769749E-6, 1.163599038579726E-4,
                        -3.14739599261259E-4, 2.4519847977412686E-4 },
                { -2.7176361887359125E-5, 5.638694128451113E-4, 7.009722990366382E-4, -1.1282043806099452E-5, 6.983493701701867E-4, 2.6589126866881173E-4,
                        1.2295961703024863E-4, 5.563328439145604E-4, 4.4816730200338125E-4, -3.4729832814007256E-5, -6.028818604193519E-7, 3.192976987126335E-5,
                        1.7402262469809026E-4, 5.182632389125651E-4 },
                { 8.675127894495302E-5, 6.024515366195699E-4, 6.545695073447614E-4, -6.729835202722053E-5, 5.68816790613126E-4, 2.1364931019670806E-4,
                        1.0843198781689372E-4, 4.4816730200338125E-4, 6.277134808325468E-4, -4.988229718603287E-5, -5.5018781802344255E-6,
                        -1.3231260300518203E-5, 8.214207901880769E-5, 5.841470978796527E-4 },
                { -8.116577287090551E-5, -1.094867665517237E-4, -1.1680969171500155E-4, 1.3929681542737307E-4, -7.899505299987754E-5, -4.201239472520589E-5,
                        -2.1292328294313923E-5, -3.4729832814007256E-5, -4.988229718603287E-5, 3.5152692612068785E-4, -9.358092257358399E-6,
                        4.962216896551324E-6, 1.291957229930161E-4, -1.5046975508620905E-4 },
                { -8.468380774247271E-6, 6.177221606260711E-6, 7.123493385355658E-5, 3.698155248637573E-6, 1.05074262063586E-5, 2.32769639721745E-5,
                        -4.152686600769749E-6, -6.028818604193519E-7, -5.5018781802344255E-6, -9.358092257358399E-6, 4.8495980378967104E-5,
                        1.1704645004909169E-5, 1.814918597253607E-5, 1.2448218299234062E-5 },
                { 4.930080166695193E-5, -5.682215091954099E-5, 1.559414390174896E-5, 5.0269944317023966E-5, 1.137295188785598E-4, 5.847559594073046E-6,
                        1.163599038579726E-4, 3.192976987126335E-5, -1.3231260300518203E-5, 4.962216896551324E-6, 1.1704645004909169E-5, 1.802684481609152E-4,
                        1.0475986793792914E-5, -4.113641419540392E-5 },
                { -2.774138231533918E-4, 2.7178074500896235E-4, 1.972605480880284E-4, 5.344931460074395E-4, 1.9732025136606058E-4, 1.9925897592339058E-4,
                        -3.14739599261259E-4, 1.7402262469809026E-4, 8.214207901880769E-5, 1.291957229930161E-4, 1.814918597253607E-5, 1.0475986793792914E-5,
                        7.843917688960864E-4, 1.231995848356005E-4 },
                { -3.148322898570031E-5, 0.0010146062950574643, 9.368808845809186E-4, -1.1654882792112444E-4, 6.631330613471645E-4, 1.9671375386540353E-4,
                        2.4519847977412686E-4, 5.182632389125651E-4, 5.841470978796527E-4, -1.5046975508620905E-4, 1.2448218299234062E-5, -4.113641419540392E-5,
                        1.231995848356005E-4, 0.0011885193322126312 } });

        // create asset variables - cost and weighting constraints
        final Variable[] tmpVariables = new Variable[(int) expectedReturnsMatrix.countRows()];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("VAR" + i);
            final int row = i;
            tmpVariables[i].weight(TypeUtils.toBigDecimal(expectedReturnsMatrix.get(row, 0)).negate());
            // set the constraints on the asset weights
            // require at least a 2% allocation to each asset
            tmpVariables[i].lower(new BigDecimal("0.05"));
            // require no more than 80% allocation to each asset
            tmpVariables[i].upper(new BigDecimal("0.35"));
            // tmpVariables[i].setUpperLimit(new BigDecimal("1.00"));
        }

        final RationalMatrix tmpExpected = RationalMatrix.FACTORY
                .rows(new double[][] { { 0.3166116715239731 }, { 0.050000000001624065 }, { 0.04999999999827016 }, { 0.05000000000034928 },
                        { 0.049999999999891145 }, { 0.049999999997416125 }, { 0.08338832846287945 }, { 0.05000000000178943 }, { 0.05000000000085164 },
                        { 0.04999999999937388 }, { 0.050000000012470555 }, { 0.04999999999966884 }, { 0.050000000000484546 }, { 0.049999999995857476 } });

        ConvexProblems.doEarly2008(tmpVariables, covarianceMatrix, tmpExpected);
    }

    /**
     * Another case of looping in the ActiveSetSolver's constraint (de)activation.
     */
    @Test
    public void testP20080204() {

        // create expected returns matrix
        final PrimitiveMatrix tmpExpectedReturns = PrimitiveMatrix.FACTORY.rows(new double[][] { { 9.994620 }, { 10.011389 }, { 10.004353 }, { 9.998293 },
                { 10.056851 }, { 9.997920 }, { 9.999011 }, { 10.050971 }, { 9.989124 }, { 9.989912 } });
        // create covariance matrix
        final PrimitiveMatrix tmpCovariances = PrimitiveMatrix.FACTORY.rows(new double[][] {
                { 0.014531344652473037, 4.444675045533674E-4, 0.007234717654072837, -9.455312097865225E-4, 0.0016345464996349748, 1.5256808879495097E-4,
                        0.00226325818749439, 0.003534367267672946, -4.2669306842991344E-5, 6.902267133060073E-5 },
                { 4.444675045533674E-4, 0.008511422662647488, 0.0039821105759899845, 5.543408872612397E-4, -0.0015797828516888929, 1.3505400134130176E-4,
                        -1.5215492836142527E-4, 9.381119889780555E-4, -4.5861204247023084E-4, -2.4226694503921645E-5 },
                { 0.007234717654072837, 0.0039821105759899845, 0.031037646466036784, -0.0022701157440735394, -3.187028053841407E-4, 5.182461519304137E-4,
                        -3.681340242039795E-4, 0.001526984686166616, 1.603885118040309E-4, -1.359858314115312E-4 },
                { -9.455312097865225E-4, 5.543408872612397E-4, -0.0022701157440735394, 0.005637141895898889, 7.89377521930992E-4, 5.004781934410127E-4,
                        -9.79221967172284E-4, -2.912861228906251E-4, 7.842012412867984E-4, 0.0010866808807429532 },
                { 0.0016345464996349748, -0.0015797828516888929, -3.187028053841407E-4, 7.89377521930992E-4, 0.03263062480163135, 6.041130577612135E-5,
                        6.883489096710362E-4, 0.010830183513887228, 0.0016425608963272292, 0.002481787652249504 },
                { 1.5256808879495097E-4, 1.3505400134130176E-4, 5.182461519304137E-4, 5.004781934410127E-4, 6.041130577612135E-5, 0.001733612375709255,
                        2.8742157640452992E-5, -3.654534740999083E-4, 9.896178753749563E-5, -1.703972415991329E-5 },
                { 0.00226325818749439, -1.5215492836142527E-4, -3.681340242039795E-4, -9.79221967172284E-4, 6.883489096710362E-4, 2.8742157640452992E-5,
                        0.008167191690212253, -0.0010075092076978207, -4.293010139199468E-4, -6.615640978331292E-4 },
                { 0.003534367267672946, 9.381119889780555E-4, 0.001526984686166616, -2.912861228906251E-4, 0.010830183513887228, -3.654534740999083E-4,
                        -0.0010075092076978207, 0.013796198054188104, 0.0013541164478127973, -2.2401086720669167E-5 },
                { -4.2669306842991344E-5, -4.5861204247023084E-4, 1.603885118040309E-4, 7.842012412867984E-4, 0.0016425608963272292, 9.896178753749563E-5,
                        -4.293010139199468E-4, 0.0013541164478127973, 0.004743485149287524, 0.0011464293217708277 },
                { 6.902267133060073E-5, -2.4226694503921645E-5, -1.359858314115312E-4, 0.0010866808807429532, 0.002481787652249504, -1.703972415991329E-5,
                        -6.615640978331292E-4, -2.2401086720669167E-5, 0.0011464293217708277, 0.007398229661528494 } });

        // create asset variables - cost and weighting constraints
        final Variable[] tmpVariables = new Variable[(int) tmpExpectedReturns.countRows()];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("VAR" + i);
            final int row = i;
            tmpVariables[i].weight(TypeUtils.toBigDecimal(tmpExpectedReturns.get(row, 0)).negate());
            // set the constraints on the asset weights
            // require at least a 8% allocation to each asset
            tmpVariables[i].lower(new BigDecimal("0.08"));
            // require no more than 12% allocation to each asset
            tmpVariables[i].upper(new BigDecimal("0.12"));
        }

        // exception here...
        final RationalMatrix tmpExpected = RationalMatrix.FACTORY.rows(
                new double[][] { { 0.08000000000000602 }, { 0.12000000000002384 }, { 0.08000000000000054 }, { 0.10643232489190736 }, { 0.12000000000002252 },
                        { 0.11999999999979595 }, { 0.09356767510776097 }, { 0.11999999999998154 }, { 0.07999999999999653 }, { 0.08000000000000498 } });

        ConvexProblems.doEarly2008(tmpVariables, tmpCovariances, tmpExpected);
    }

    /**
     * Another case of looping in the ActiveSetSolver's constraint (de)activation.
     */
    @Test
    public void testP20080208() {

        // create expected returns matrix
        final PrimitiveMatrix tmpExpectedReturns = PrimitiveMatrix.FACTORY.rows(new double[][] { { 9.997829 }, { 10.008909 }, { 10.010849 }, { 9.998919 },
                { 10.055549 }, { 9.999127 }, { 9.999720 }, { 10.049002 }, { 9.988769 }, { 9.990095 } });

        // create covariance matrix
        final PrimitiveMatrix tmpCovariances = PrimitiveMatrix.FACTORY.rows(new double[][] {
                { 0.014661954677318977, 3.459112088561122E-4, 0.007798752920910871, 0.0020921425081866503, 0.001846944297640248, 1.0531906931335766E-4,
                        -2.7515614291198E-4, 0.0034083900074454894, 1.1859491261103433E-4, -0.0027421673864628264 },
                { 3.459112088561122E-4, 0.008695862475003915, 0.004154360841751649, -2.661685231819661E-4, -0.0015999007544258263, 3.590680217774603E-4,
                        -0.00186976624370318, 0.0010975416828213752, -5.512038393911129E-4, -0.0010605923775744853 },
                { 0.007798752920910871, 0.004154360841751649, 0.032945930970836965, 0.0037716078815399324, -2.2919474365382624E-4, 3.3938035033219876E-4,
                        -0.0015613122026082874, 0.0010975697179894332, 2.296422665244149E-4, -0.001709517941787044 },
                { 0.0020921425081866503, -2.661685231819661E-4, 0.0037716078815399324, 0.0057162979859706736, 5.573137056500744E-4, 4.91132887765294E-4,
                        -9.94830474250937E-4, 8.331708084069932E-4, -6.595917138470072E-4, -0.0018693519327569541 },
                { 0.001846944297640248, -0.0015999007544258263, -2.2919474365382624E-4, 5.573137056500744E-4, 0.03230071314144326, -2.2320789666419312E-4,
                        -2.2639506820057415E-4, 0.010695663287043154, 0.0014569847730040847, 0.002160537177809949 },
                { 1.0531906931335766E-4, 3.590680217774603E-4, 3.3938035033219876E-4, 4.91132887765294E-4, -2.2320789666419312E-4, 0.0017540170708301957,
                        5.153195618913916E-5, 7.339825618468765E-4, -9.309096233432093E-6, -1.814362059740286E-4 },
                { -2.7515614291198E-4, -0.00186976624370318, -0.0015613122026082874, -9.94830474250937E-4, -2.2639506820057415E-4, 5.153195618913916E-5,
                        0.00809348822665732, -0.0017672866424053742, 3.058672988166145E-4, 0.001201578905822851 },
                { 0.0034083900074454894, 0.0010975416828213752, 0.0010975697179894332, 8.331708084069932E-4, 0.010695663287043154, 7.339825618468765E-4,
                        -0.0017672866424053742, 0.013913761913235494, 0.0012785124957521252, 5.298368056593439E-4 },
                { 1.1859491261103433E-4, -5.512038393911129E-4, 2.296422665244149E-4, -6.595917138470072E-4, 0.0014569847730040847, -9.309096233432093E-6,
                        3.058672988166145E-4, 0.0012785124957521252, 0.004650801896027841, 5.437156659657787E-4 },
                { -0.0027421673864628264, -0.0010605923775744853, -0.001709517941787044, -0.0018693519327569541, 0.002160537177809949, -1.814362059740286E-4,
                        0.001201578905822851, 5.298368056593439E-4, 5.437156659657787E-4, 0.007359495478781133 } });

        // create asset variables - cost and weighting constraints
        final Variable[] tmpVariables = new Variable[(int) tmpExpectedReturns.countRows()];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("VAR" + i);
            final int row = i;
            tmpVariables[i].weight(TypeUtils.toBigDecimal(tmpExpectedReturns.get(row, 0)).negate());
            // set the constraints on the asset weights
            // require at least a 8% allocation to each asset
            tmpVariables[i].lower(new BigDecimal("0.08"));
            // require no more than 12% allocation to each asset
            tmpVariables[i].upper(new BigDecimal("0.12"));
        }

        // exception here...
        final RationalMatrix tmpExpected = RationalMatrix.FACTORY.rows(
                new double[][] { { 0.07999999999998897 }, { 0.1199999999999636 }, { 0.07999999999999526 }, { 0.08000000000004488 }, { 0.11999999999999084 },
                        { 0.12000000000018606 }, { 0.11999999999996151 }, { 0.12000000000000167 }, { 0.08000000000001738 }, { 0.08000000000005617 } });

        ConvexProblems.doEarly2008(tmpVariables, tmpCovariances, tmpExpected);

    }

    /**
     * Another case of looping in the ActiveSetSolver's constraint (de)activation. Slightly different case (I
     * believe). The main reason/difficulty seemed to be that the algorithm would both add and remove
     * constraints in the iteration. Modified the algorithm to only do one thing with each iteration - either
     * add or remove.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20080819() {

        final Factory<PrimitiveMatrix> tmpMtrxFact = PrimitiveMatrix.FACTORY;
        final NumberContext tmpEvalCntxt = StandardType.DECIMAL_032;

        final BasicMatrix[] tmpMatrices = new PrimitiveMatrix[8];

        tmpMatrices[0] = tmpMtrxFact.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0 } });
        tmpMatrices[1] = tmpMtrxFact.rows(new double[][] { { 1.0 } });
        tmpMatrices[2] = tmpMtrxFact.rows(new double[][] { { 15.889978159746546, 7.506345724913546, 0.8416674706550127, 0.435643236753381 },
                { 7.506345724913546, 8.325860065234632, 0.4230651628792374, 0.1670802923999648 },
                { 0.8416674706550127, 0.4230651628792374, 1.00134099479915, 0.6558469727234849 },
                { 0.435643236753381, 0.1670802923999648, 0.6558469727234849, 0.6420451103682865 } });
        tmpMatrices[3] = tmpMtrxFact
                .rows(new double[][] { { -0.15804736429388952 }, { -0.11226063792731895 }, { -0.10509261785657838 }, { -0.0848686735786316 } });
        tmpMatrices[4] = tmpMtrxFact.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0 },
                { -0.15804736429388952, -0.11226063792731895, -0.10509261785657838, -0.0848686735786316 }, { -1.0, 0.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0, 0.0 },
                { 0.0, 0.0, -1.0, 0.0 }, { 0.0, 0.0, 0.0, -1.0 } });
        tmpMatrices[5] = tmpMtrxFact.rows(new double[][] { { 0.9 }, { 0.8 }, { 0.7 }, { 0.6 }, { 0.0 }, { -0.1 }, { -0.2 }, { -0.3 }, { -0.4 } });
        tmpMatrices[6] = tmpMtrxFact.rows(new double[][] { { 0.1 }, { 0.2 }, { 0.3 }, { 0.4 } });
        tmpMatrices[7] = null;
        final MatrixStore<Double>[] retVal = new MatrixStore[tmpMatrices.length];

        for (int i = 0; i < retVal.length; i++) {
            if (tmpMatrices[i] != null) {
                if (i == 3) {
                    retVal[i] = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[i].negate());
                } else {
                    retVal[i] = PrimitiveDenseStore.FACTORY.copy(tmpMatrices[i]);
                }
            }
        }

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(retVal);

        // final ActiveSetSolver tmpSolver = new ActiveSetSolver(tmpMatrices);
        final ConvexSolver tmpSolver = tmpBuilder.build();

        // Test that the matrices were input in the right order
        // JUnitUtils.assertEquals(tmpSolver.getAE(), tmpMatrices[0].toPrimitiveStore(),
        // tmpEvalCntxt);
        // JUnitUtils.assertEquals(tmpSolver.getBE(), tmpMatrices[1].toPrimitiveStore(),
        // tmpEvalCntxt);
        // JUnitUtils.assertEquals(tmpSolver.getQ(), tmpMatrices[2].toPrimitiveStore(),
        // tmpEvalCntxt);
        // JUnitUtils.assertEquals(tmpSolver.getC(), tmpMatrices[3].negate().toPrimitiveStore(),
        // tmpEvalCntxt);
        // JUnitUtils.assertEquals(tmpSolver.getAI(), tmpMatrices[4].toPrimitiveStore(),
        // tmpEvalCntxt);
        // JUnitUtils.assertEquals(tmpSolver.getBI(), tmpMatrices[5].toPrimitiveStore(),
        // tmpEvalCntxt);

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertEquals(tmpMatrices[6], RationalMatrix.FACTORY.columns(tmpResult), tmpEvalCntxt);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * <p>
     * I'm trying to solve some quadratic programming systems using version 24. The ActiveSetSolver does not
     * always converge to a solution, but throws an exception, "Matrix is singular" (The exception is thrown
     * by org.ojalgo.matrix.jama.LUDecomposition). The thing is that if I run Matlabs quadprog method on the
     * exact same system, a solution is found without problems. Here is the code that produces the exception:
     * </p>
     * <p>
     * 2015-02-21: Extended the test case with a few alternatives using ExpressionsBasedModel. Numerically
     * difficult problem as the formulation includes both large and very small parameters (like 1000000000 and
     * -7.646043242556307E-15).
     * </p>
     */
    @Test
    public void testP20081014() {

        final PhysicalStore.Factory<Double, PrimitiveDenseStore> tmpFactory = PrimitiveDenseStore.FACTORY;

        final PrimitiveDenseStore[] tmpSystem = new PrimitiveDenseStore[6];
        // {[AE], [BE], [Q], [C], [AI], [BI]}

        tmpSystem[0] = tmpFactory.rows(new double[][] {
                { -0.0729971273939726, -0.31619624199405116, -0.14365990081105298, -3.4914813388431334E-15, 0.9963066090106673, 0.9989967493404447, 1.0, 0.0,
                        0.0 },
                { -2.5486810808521023E-16, 3.6687950405257466, 3.2047109656515507, 1.0, 0.08586699506600544, 0.04478275122437895, 0.0, 1.0, 0.0 },
                { -7.646043242556307E-15, -107.21808503782593, -97.434268076846, 30.0, -11.54276933307617, 7.647488207332634, 0.0, 0, 1.0 } }); // AE
        tmpSystem[1] = tmpFactory.rows(new double[][] { { 10.461669614447484 }, { -0.5328532701990767 }, { 15.782527136201711 } }); // BE

        final PrimitiveDenseStore tmpQ = tmpFactory.makeEye(9, 9);
        tmpQ.set(3, 3, 10);
        tmpQ.set(4, 4, 10);
        tmpQ.set(5, 5, 10);
        tmpQ.set(6, 6, 1000000000);
        tmpQ.set(7, 7, 1000000000);
        tmpQ.set(8, 8, 1000000000);
        tmpSystem[2] = tmpQ; // Q

        tmpSystem[3] = tmpFactory.rows(new double[][] { { 0 }, { 0 }, { 0 }, { -1 }, { -1 }, { -1 }, { 0 }, { 0 }, { 0 } }); // C

        final double[][] tmpAI = new double[18][9];
        for (int i = 0; i < 9; i++) {
            tmpAI[i][i] = 1;
            tmpAI[i + 9][i] = -1;
        }
        tmpSystem[4] = tmpFactory.rows(tmpAI); // AI

        tmpSystem[5] = tmpFactory.rows(new double[][] { { 0 }, { 0.0175 }, { 0.0175 }, { 5 }, { 5 }, { 5 }, { 100000 }, { 100000 }, { 100000 }, { 0 },
                { 0.0175 }, { 0.0175 }, { 5 }, { 5 }, { 5 }, { 100000 }, { 100000 }, { 100000 } }); // BI

        final PrimitiveDenseStore tmpMatlabSolution = tmpFactory.columns(new double[] { 0.00000000000000, -0.01750000000000, -0.01750000000000,
                0.88830035195990, 4.56989525276369, 5.00000000000000, 0.90562154243124, -1.91718419629399, 0.06390614020590 });

        // Compare to MatLab using 3 digits and 6 decimal places
        final NumberContext tmpAccuracy = NumberContext.getGeneral(3, 6);

        ConvexProblems.builAndTestModel(tmpSystem, tmpMatlabSolution, tmpAccuracy, false);
    }

    /**
     * <p>
     * Continuation of {@link #testP20081014()}.
     * </p>
     * <p>
     * Thanks for your answer, Anders, it did solve my system (even though the result state was FAILED). As
     * you might have guessed, I am using the ActiveSetSolver as a part of a larger system where the system
     * matrixes to be solved changes all the time (not the dimensions but the values of the matrixes). I still
     * get errors in certain situations. I will present a system that triggers an
     * ArrayIndexOutOfBoundsException in ActiveSetSolver. Again, Matlabs quadprog produces a correct result.
     * </p>
     * <p>
     * 2015-02-28: Var tvungen att ändra från new NumberContext(7, 6) till new NumberContext(5, 6) för
     * lösningen.
     * </p>
     */
    @Test
    public void testP20081015() {

        final PhysicalStore.Factory<Double, PrimitiveDenseStore> tmpFactory = PrimitiveDenseStore.FACTORY;

        final PrimitiveDenseStore[] tmpSystem = new PrimitiveDenseStore[6];
        // {[AE], [BE], [Q], [C], [AI], [BI]}

        tmpSystem[0] = tmpFactory.rows(new double[][] {
                { -0.6864742690952357, -0.5319998214213948, 1.2385363215384646, -3.4914813388431334E-15, 0.976619978072726, 0.8727726942384015, 1.0, 0.0, 0.0 },
                { -2.396812100141995E-15, 2.4168686217298863, -2.2145077177955423, 1.0, 0.21497306442721648, 0.48812685256175126, 0.0, 1.0, 0.0 },
                { -7.190436300425984E-14, -67.71806025910404, 77.58205842771245, 30.0, -15.23877173547103, -6.788851328706924, 0.0, 0.0, 1.0 } }); // AE
        tmpSystem[1] = tmpFactory.rows(new double[][] { { 0.459002008118756 }, { 0.002566161917554134 }, { -0.03315618953218959 } }); // BE

        tmpSystem[2] = tmpFactory.makeEye(9, 9); // Q
        tmpSystem[2].set(3, 3, 10);
        tmpSystem[2].set(4, 4, 10);
        tmpSystem[2].set(5, 5, 10);
        tmpSystem[2].set(6, 6, 1000000000);
        tmpSystem[2].set(7, 7, 1000000000);
        tmpSystem[2].set(8, 8, 1000000000);
        tmpSystem[3] = tmpFactory.rows(new double[][] { { 0 }, { 0 }, { 0 }, { -1 }, { -1 }, { 1 }, { 0 }, { 0 }, { 0 } }); // C

        final double[][] tmpAI = new double[18][9];
        for (int i = 0; i < 9; i++) {
            tmpAI[i][i] = 1;
            tmpAI[i + 9][i] = -1;
        }
        tmpSystem[4] = tmpFactory.rows(tmpAI); // AI
        tmpSystem[5] = tmpFactory.rows(new double[][] { { 0 }, { 0.0175 }, { 0.0175 }, { 0.5 }, { 0.5 }, { 0.5 }, { 100000 }, { 100000 }, { 100000 }, { 0 },
                { 0.0175 }, { 0.0175 }, { 0.5 }, { 0.5 }, { 0.5 }, { 100000 }, { 100000 }, { 100000 } }); // BI

        final PrimitiveDenseStore tmpMatlabSolution = tmpFactory.columns(new double[] { -0.00000000000000, -0.01750000000000, 0.01750000000000,
                0.13427356981778, 0.50000000000000, -0.14913060410765, 0.06986475572103, -0.08535020176844, 0.00284500680371 });

        ConvexProblems.builAndTestModel(tmpSystem, tmpMatlabSolution, NumberContext.getGeneral(4, 14), true);
    }

    /**
     * <p>
     * Continuation of {@link #testP20081014()} and {@link #testP20081015()}.
     * </p>
     * <p>
     * Originally the problem was an ArrayIndexOutOfBoundsException. When that was fixed it had the same
     * numerical difficulties as the previous versions.
     * </p>
     * <p>
     * 2015-02-28: Var tvungen att ändra från new NumberContext(7, 11) till new NumberContext(3, 3) för
     * lösningen.
     * </p>
     */
    @Test
    public void testP20081119() {

        final PhysicalStore.Factory<Double, PrimitiveDenseStore> tmpFactory = PrimitiveDenseStore.FACTORY;

        final PrimitiveDenseStore[] tmpSystem = new PrimitiveDenseStore[6];
        // {[AE], [BE], [Q], [C], [AI], [BI]}

        tmpSystem[0] = tmpFactory.rows(new double[][] {
                { -10.630019918689772, 0.15715259580856766, -24.006889886456438, -3.4914813388431334E-15, 0.9987922086746552, 0.9018272287390979, 1.0, 0.0,
                        0.0 },
                { -3.711451617763614E-14, -3.1946032406211518, 50.10466796063192, 1.0, 0.04913373475326318, 0.4320968057099691, 0.0, 1.0, 0.0 },
                { -1.1134354853290842E-12, 94.42372385635744, -1719.2020477970657, 30.0, -10.463141920669791, -4.8464591126471905, 0.0, 0.0, 1.0 } }); // AE
        tmpSystem[1] = tmpFactory.rows(new double[][] { { 14.272908058664967 }, { -3.888270819999793 }, { -0.06992907379067503 } }); // BE

        tmpSystem[2] = tmpFactory.makeEye(9, 9); // Q
        tmpSystem[2].set(3, 3, 10);
        tmpSystem[2].set(4, 4, 10);
        tmpSystem[2].set(5, 5, 10);
        tmpSystem[2].set(6, 6, 1000000000);
        tmpSystem[2].set(7, 7, 1000000000);
        tmpSystem[2].set(8, 8, 1000000000);
        tmpSystem[3] = tmpFactory.rows(new double[][] { { 0 }, { 0 }, { 0 }, { -1 }, { -1 }, { 1 }, { 0 }, { 0 }, { 0 } }); // C

        final double[][] tmpAI = new double[18][9];
        for (int i = 0; i < 9; i++) {
            tmpAI[i][i] = 1;
            tmpAI[i + 9][i] = -1;
        }
        tmpSystem[4] = tmpFactory.rows(tmpAI); // AI
        tmpSystem[5] = tmpFactory.rows(new double[][] { { 0 }, { 0.0175 }, { 0.0175 }, { 5 }, { 5 }, { 5 }, { 100000 }, { 100000 }, { 100000 }, { 0 },
                { 0.0175 }, { 0.0175 }, { 5 }, { 5 }, { 5 }, { 100000 }, { 100000 }, { 100000 } }); // BI

        final PrimitiveDenseStore tmpMatlabSolution = tmpFactory.columns(new double[] { 0.00000000000000, 0.01750000000000, -0.01750000000000, 1.46389524463679,
                5.00000000000000, 4.87681260745493, 4.45803387299108, -6.77235264210831, 0.22574508859158 });

        ConvexProblems.builAndTestModel(tmpSystem, tmpMatlabSolution, NumberContext.getGeneral(2, 14), false);
    }

    /**
     * A lower level version of {@linkplain org.ojalgo.finance.portfolio.PortfolioProblems#testP20090115()}.
     * The solver returns negative, constraint breaking, variables with STATE == OPTIMAL.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20090115() {

        final MatrixStore<Double>[] tmpMtrxs = new MatrixStore[6];

        tmpMtrxs[0] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 } });
        tmpMtrxs[1] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 } });
        tmpMtrxs[2] = PrimitiveDenseStore.FACTORY.rows(new double[][] {
                { 3.048907897157133E-4, 1.6671472561019247E-4, 4.4500080981934345E-4, -5.389129745055723E-4, -2.6090705011393183E-4, -1.2633284900760366E-4,
                        -6.485428846447651E-7 },
                { 1.6671472561019247E-4, 2.341985572849691E-4, 2.9113916450678265E-4, -4.5760873539850514E-4, 1.3078636134987255E-5, -2.354289901013046E-5,
                        -7.578030042426654E-7 },
                { 4.4500080981934345E-4, 2.9113916450678265E-4, 7.46023915996829E-4, -0.0010247176498305568, -2.6745504327902895E-4, -1.6563544154823496E-4,
                        -8.293698990696063E-7 },
                { -5.389129745055723E-4, -4.5760873539850514E-4, -0.0010247176498305568, 0.001754169535149865, 2.0293065310212377E-4, 2.1401092557826588E-4,
                        1.0252846778608953E-7 },
                { -2.6090705011393183E-4, 1.3078636134987255E-5, -2.6745504327902895E-4, 2.0293065310212377E-4, 4.632320892679136E-4, 1.7969731066037214E-4,
                        2.4953495129362833E-8 },
                { -1.2633284900760366E-4, -2.354289901013046E-5, -1.6563544154823496E-4, 2.1401092557826588E-4, 1.7969731066037214E-4, 8.346410612364995E-5,
                        -7.02099350897589E-8 },
                { -6.485428846447651E-7, -7.578030042426654E-7, -8.293698990696063E-7, 1.0252846778608953E-7, 2.4953495129362833E-8, -7.02099350897589E-8,
                        8.367244992498656E-9 } });
        tmpMtrxs[3] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -0.010638291263564232 }, { -0.013500370827906071 }, { -0.011390037735101773 },
                { -0.010385042339767682 }, { -3.812208389845893E-4 }, { -0.002315505853720011 }, { -0.0 } });
        tmpMtrxs[4] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0 } }).transpose();
        tmpMtrxs[5] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 1.0 }, { 0.0 }, { 0.0 },
                { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(tmpMtrxs);

        final ConvexSolver tmpSolver = tmpBuilder.build();

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertBounds(BigMath.ZERO, tmpResult, BigMath.ONE, StandardType.PERCENT);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * "I just tested ojalgo v.26 and experienced nullpointer-exceptions when I tried to optimize any QP
     * without equality-constraints." This test case is the same as (same numbers)
     * {@linkplain #testP20091102a()} but with the equality constraints removed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20090202() {

        final MatrixStore<Double>[] tmpMtrxs = new MatrixStore[6];

        tmpMtrxs[0] = null;
        tmpMtrxs[1] = null;
        tmpMtrxs[2] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.400491304172128, 5.429710780966787, 5.910932781021423 },
                { 5.429710780966787, 23.181215288234903, 27.883770791602895 }, { 5.910932781021423, 27.883770791602895, 34.37266787775051 } });
        tmpMtrxs[3] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.053 }, { 0.0755 }, { 0.0788 } });
        tmpMtrxs[4] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { -0.053, -0.0755, -0.0788 },
                { -1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, -1.0 } });
        tmpMtrxs[5] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 1.0 }, { -0.06 }, { 0.0 }, { 0.0 }, { 0.0 } });

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(tmpMtrxs);

        final ConvexSolver tmpSolver = tmpBuilder.build();

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

        final PhysicalStore<BigDecimal> tmpSolution = BigDenseStore.FACTORY.copy(RationalMatrix.FACTORY.columns(tmpResult));
        tmpSolution.modifyAll(new NumberContext(7, 6).getFunction(BigFunction.getSet()));
        for (final BigDecimal tmpBigDecimal : tmpSolution.asList()) {
            if ((tmpBigDecimal.compareTo(BigMath.ZERO) == -1) || (tmpBigDecimal.compareTo(BigMath.ONE) == 1)) {
                TestUtils.fail("!(0.0 <= " + tmpBigDecimal + " <= 1.0)");
            }
        }

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * Infeasible problem, but solver reports optimal solution!
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20090924() {

        final MatrixStore<Double>[] tmpMtrxs = new MatrixStore[6];

        tmpMtrxs[0] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }, { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } });
        tmpMtrxs[1] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 }, { 0.7027946085029227 } });
        tmpMtrxs[2] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0 } });
        tmpMtrxs[3] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -0.0 }, { 0.5 }, { 0.25 }, { 0.25 }, { 0.3 }, { -0.0 }, { 0.62 } });
        tmpMtrxs[4] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0 },
                { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 },
                { 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0 } });
        tmpMtrxs[5] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.17 }, { 0.52 }, { 0.3 }, { 0.3 }, { 0.3 }, { 0.15 }, { 1.0 }, { 0.31 },
                { -0.05960220972942152 }, { -0.1144024630877301 }, { -0.12289286964304823 }, { 0.0 }, { -0.02 }, { 0.0 } });

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(tmpMtrxs);

        final ConvexSolver tmpSolver = tmpBuilder.build();

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertStateLessThanFeasible(tmpResult);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * Fick state "failed" men det berodde bara på att antalet iterationer inte räckte för att hitta en
     * korrekt lösning. Ökade "iterationsLimit" så fungerade det. Felet uppstod inte varje gång, men om man
     * körde samma problem (det problem som är test caset) uppreapde gånger fick man till slut ett fel.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20091102a() {

        final MatrixStore<Double>[] tmpMtrxs = new MatrixStore[6];

        tmpMtrxs[0] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0 } });
        tmpMtrxs[1] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 } });
        tmpMtrxs[2] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.400491304172128, 5.429710780966787, 5.910932781021423 },
                { 5.429710780966787, 23.181215288234903, 27.883770791602895 }, { 5.910932781021423, 27.883770791602895, 34.37266787775051 } });
        tmpMtrxs[3] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.053 }, { 0.0755 }, { 0.0788 } });
        tmpMtrxs[4] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { -0.053, -0.0755, -0.0788 },
                { -1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, -1.0 } });
        tmpMtrxs[5] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 1.0 }, { -0.06 }, { 0.0 }, { 0.0 }, { 0.0 } });

        // Solve the same problem several times
        for (int i = 0; i < 20; i++) {

            final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(tmpMtrxs);

            final ConvexSolver tmpSolver = tmpBuilder.build();

            final Optimisation.Result tmpResult = tmpSolver.solve();

            TestUtils.assertEquals(State.OPTIMAL, tmpResult.getState());

            TestUtils.assertEquals(PrimitiveMatrix.FACTORY.rows(new double[][] { { 0.68888888888888888 }, { 0.311111111111111111 }, { 0.0 } }),
                    RationalMatrix.FACTORY.columns(tmpResult));
        }
    }

    /**
     * Infeasible problem, but solver reports optimal solution!
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testP20091102b() {

        final MatrixStore<Double>[] tmpMtrxs = new MatrixStore[6];

        tmpMtrxs[0] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 1.0, 1.0 } });
        tmpMtrxs[1] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 } });
        tmpMtrxs[2] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 3.400491304172128, 5.429710780966787, 5.910932781021423 },
                { 5.429710780966787, 23.181215288234903, 27.883770791602895 }, { 5.910932781021423, 27.883770791602895, 34.37266787775051 } });
        tmpMtrxs[3] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.053 }, { 0.0755 }, { 0.0788 } });
        tmpMtrxs[4] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0 }, { 0.0, 0.0, 1.0 }, { -0.053, -0.0755, -0.0788 },
                { -1.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0 }, { 0.0, 0.0, -1.0 } });
        tmpMtrxs[5] = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0 }, { 1.0 }, { 1.0 }, { -0.06 }, { -0.8 }, { 0.0 }, { 0.0 } });

        final ConvexSolver.Builder tmpBuilder = new ConvexSolver.Builder(tmpMtrxs);

        final ConvexSolver tmpSolver = tmpBuilder.build();

        final Optimisation.Result tmpResult = tmpSolver.solve();

        TestUtils.assertStateLessThanFeasible(tmpResult);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * ojAlgo could not solve this, but LOQO (and others) could. I re-implemented the problem code just to
     * verify there was no problem there. http://bugzilla.optimatika.se/show_bug.cgi?id=11
     */
    @Test
    public void testP20111129() {

        final Variable x1 = new Variable("X1");
        final Variable x2 = new Variable("X2").lower(BigMath.HUNDRED.negate()).upper(BigMath.HUNDRED);
        final Variable x3 = new Variable("X3").lower(BigMath.ZERO);
        final Variable x4 = new Variable("X4").lower(BigMath.ZERO);

        final Variable[] tmpVariables = new Variable[] { x1, x2, x3, x4 };
        final ExpressionsBasedModel tmpModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpObjExpr = tmpModel.addExpression("Objective");
        tmpModel.setMinimisation();
        tmpObjExpr.set(2, 2, BigMath.HALF);
        tmpObjExpr.set(3, 3, BigMath.TWO);
        tmpObjExpr.set(2, 3, BigMath.TWO.negate());
        tmpObjExpr.set(0, BigMath.THREE);
        tmpObjExpr.set(1, BigMath.TWO.negate());
        tmpObjExpr.set(2, BigMath.ONE);
        tmpObjExpr.set(3, BigMath.FOUR.negate());
        tmpObjExpr.weight(BigMath.ONE);

        Expression tmpConstrExpr;

        tmpConstrExpr = tmpModel.addExpression("C1").lower(BigMath.FOUR);
        tmpConstrExpr.set(0, BigMath.ONE);
        tmpConstrExpr.set(1, BigMath.ONE);
        tmpConstrExpr.set(2, BigMath.FOUR.negate());
        tmpConstrExpr.set(3, BigMath.TWO);

        tmpConstrExpr = tmpModel.addExpression("C2").upper(BigMath.SIX);
        tmpConstrExpr.set(0, BigMath.THREE.negate());
        tmpConstrExpr.set(1, BigMath.ONE);
        tmpConstrExpr.set(2, BigMath.TWO.negate());

        tmpConstrExpr = tmpModel.addExpression("C3").level(BigMath.NEG);
        tmpConstrExpr.set(1, BigMath.ONE);
        tmpConstrExpr.set(3, BigMath.NEG);

        tmpConstrExpr = tmpModel.addExpression("C4").level(BigMath.ZERO);
        tmpConstrExpr.set(0, BigMath.ONE);
        tmpConstrExpr.set(1, BigMath.ONE);
        tmpConstrExpr.set(2, BigMath.NEG);

        // tmpModel.options.debug(ConvexSolver.class);
        final Result tmpResult = tmpModel.minimise();
        final double tmpObjFuncVal = tmpResult.getValue();

        TestUtils.assertEquals(-5.281249989, tmpObjFuncVal, new NumberContext(7, 6));

        final double[] tmpExpected = new double[] { -1.1875, 1.5625, 0.375, 2.5625 };
        for (int i = 0; i < tmpExpected.length; i++) {
            TestUtils.assertEquals(tmpExpected[i], tmpVariables[i].getValue().doubleValue(), new NumberContext(5, 4));
        }

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel, null);
    }

    /**
     * Continuation of P20111129 http://bugzilla.optimatika.se/show_bug.cgi?id=15 Have to turn off validation
     * as Q is not positive semidefinite.
     * <p>
     * 2016-03-07: Initially the solution (from AMPL/LOQO) was stated to be:
     * <p>
     *
     * <pre>
     * 1.78684, 0.000326128, 1.78665, 0.000136478, 495.429, 0.00358488, 495.427, 0.00178874, 8.90701, 0.000339811, 8.90684, 0.000174032
     * </pre>
     * <p>
     * The ExpressionsBasedModel can only validate this solution to be correct using a very "poor" accuracy
     * context. When ExpressionsBasedModel uses CPLEX as the solver a slightly different solution is returned
     * that validates much better. Switched to using that solution as the expected solution in this test:
     * <p>
     *
     * <pre>
     * 1.7856570552, 1.216415374E-5, 1.78565097263, 6.08157995E-6, 495.426247828, 2.478968927E-5, 495.426235433, 1.239483719E-5, 8.90673094088, 6.04347562E-6, 8.90672791911, 3.02171321E-6
     * </pre>
     * </p>
     */
    @Test
    public void testP20111205() {

        final PrimitiveDenseStore tmpAE = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, -1.0, 1.0 },
                { 1.0, -1.0, -1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, -1.0, 1.0, 0.0, 0.0, 0.0, 0.0 } });
        final PrimitiveDenseStore tmpBE = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 } });
        final PrimitiveDenseStore tmpQ = PrimitiveDenseStore.FACTORY.rows(new double[][] {
                { 42.58191012032541, -42.58191012032541, 0.0, 0.0, 0.029666091804595635, -0.029666091804595635, 0.0, 0.0, 9.954580659495097, -9.954580659495097,
                        0.0, 0.0 },
                { -42.58191012032541, 42.58191012032541, 0.0, 0.0, -0.029666091804595635, 0.029666091804595635, 0.0, 0.0, -9.954580659495097, 9.954580659495097,
                        0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.029666091804595635, -0.029666091804595635, 0.0, 0.0, 0.8774199042430086, -0.8774199042430086, 0.0, 0.0, -3.537087573378497,
                        3.537087573378497, 0.0, 0.0 },
                { -0.029666091804595635, 0.029666091804595635, 0.0, 0.0, -0.8774199042430086, 0.8774199042430086, 0.0, 0.0, 3.537087573378497,
                        -3.537087573378497, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 9.954580659495097, -9.954580659495097, 0.0, 0.0, -3.537087573378497, 3.537087573378497, 0.0, 0.0, 153.76101274121527, -153.76101274121527,
                        0.0, 0.0 },
                { -9.954580659495097, 9.954580659495097, 0.0, 0.0, 3.537087573378497, -3.537087573378497, 0.0, 0.0, -153.76101274121527, 153.76101274121527,
                        0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 } });
        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.rows(
                new double[][] { { 185.8491751747291 }, { -192.3021967647291 }, { -6.45302159 }, { -6.45302159 }, { 406.4118818820076 }, { -409.5778277520076 },
                        { -3.16594587 }, { -3.16594587 }, { -352.0970015985486 }, { 339.11043506854867 }, { -12.986566530000001 }, { -12.986566530000001 } });
        final PrimitiveDenseStore tmpAI = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0 } });
        final PrimitiveDenseStore tmpBI = PrimitiveDenseStore.FACTORY
                .rows(new double[][] { { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 } });

        // Optimisation.Result from CPLEX (via ExpressionsBasedModel)
        final double[] tmpExpected = new double[] { 1.7856570552, 1.216415374E-5, 1.78565097263, 6.08157995E-6, 495.426247828, 2.478968927E-5, 495.426235433,
                1.239483719E-5, 8.90673094088, 6.04347562E-6, 8.90672791911, 3.02171321E-6 };

        final PrimitiveDenseStore[] tmpMatrices = new PrimitiveDenseStore[6];
        tmpMatrices[0] = tmpAE;
        tmpMatrices[1] = tmpBE;
        tmpMatrices[2] = tmpQ;
        tmpMatrices[3] = tmpC;
        tmpMatrices[4] = tmpAI;
        tmpMatrices[5] = tmpBI;

        // The original AMPL/LOQO solution was given with 6 digits precision and never more than 9 decimals
        final NumberContext tmpAccuracy = NumberContext.getGeneral(3, 3); // ojAlgo can only get roughly the same solution

        ConvexProblems.builAndTestModel(tmpMatrices, tmpExpected, tmpAccuracy, true);
    }

    /**
     * <p>
     * I tried to use ojAlgo to implement a norm minimization problem, but the solver fails even for very
     * simple instances. The following example is one particular simple instance. Q is the identity matrix, C
     * the zero vector. The constraints express that the solution is a probability function (AE for
     * normalization and AI for non-negativity). Q is positive definite and the solution should be (0.5, 0.5),
     * but qSolver fails.
     * </p>
     * <p>
     * apete: The problem was incorrectly specified with a transposed "C" vector. Modified the builder to
     * actually throw an exception.
     * </p>
     */
    @Test
    public void testP20140109() {

        final PrimitiveDenseStore tmpQ = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1, 0 }, { 0, 1 } });
        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0, 0 } });

        final PrimitiveDenseStore tmpAE = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1, 1 } });
        final PrimitiveDenseStore tmpBE = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1 } });

        final PrimitiveDenseStore tmpAI = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -1, 0 }, { 0, -1 } });
        final PrimitiveDenseStore tmpBI = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 0 }, { 0 } });

        try {

            final ConvexSolver qSolver = new ConvexSolver.Builder(tmpQ, tmpC).equalities(tmpAE, tmpBE).inequalities(tmpAI, tmpBI).build();

            // qSolver.options.debug(ConvexSolver.class);

            final Optimisation.Result tmpResult = qSolver.solve();

            // Shouldn't get this far. There should be an exception
            TestUtils.assertStateLessThanFeasible(tmpResult);
            TestUtils.fail();

        } catch (final ProgrammingError exception) {
            TestUtils.assertTrue("Yes!", true);
        }

        // ... and check that the correctly defined problem does solve.

        final ConvexSolver tmpCorrectSolver = new ConvexSolver.Builder(tmpQ, tmpC.transpose()).equalities(tmpAE, tmpBE).inequalities(tmpAI, tmpBI).build();

        final Optimisation.Result tmpResult = tmpCorrectSolver.solve();

        TestUtils.assertStateNotLessThanOptimal(tmpResult);
        TestUtils.assertEquals(Primitive64Array.wrap(new double[] { 0.5, 0.5 }), tmpResult);

    }

    /**
     * I’ve been using the QuadraticSolver for a while, and suddenly stumbled over an unexpected failure to
     * solve a problem. The solution state was APPROXIMATE and the solution was not correct. I tested the same
     * system with another QP-solver and got the result I expected. I then condensed the problem as much as I
     * could and made a test out of it (see below). To my surprise the test sometimes fails and sometimes
     * passes(!). I’ve been running this test (alone) in TestNG. I’m using Ojalgo v35 and Java 1.7.55. The Q
     * matrix is positive definite.
     *
     * @see "http://bugzilla.optimatika.se/show_bug.cgi?id=210"
     */
    @Test
    public void testP20140522() {

        final double[][] q = new double[][] { { 49.0, 31.0, 17.0, 6.0 }, { 31.0, 25.0, 13.0, 5.0 }, { 17.0, 13.0, 11.0, 3.5 }, { 6.0, 5.0, 3.5, 4.0 } };
        final RawStore JamaQ = RawStore.FACTORY.rows(q);

        final double[] c = new double[] { 195.0, 59.0, -1.8, -11.7 };
        final RawStore JamaC = RawStore.FACTORY.columns(c);

        final double[][] ai = new double[][] { { 1.0, 0.0, 0.0, 0.0 }, { -1.0, 0.0, 0.0, 0.0 }, { 1.0, 1.0, 0.0, 0.0 }, { -1.0, -1.0, 0.0, 0.0 },
                { 1.0, 1.0, 1.0, 0.0 }, { -1.0, -1.0, -1.0, 0.0 }, { 0.1, 0.0, 0.0, 0.0 }, { 0.01, 0.0, 0.0, 0.0 }, { 0.18, 0.1, 0.0, 0.0 },
                { -0.01, 0.0, 0.0, 0.0 }, { -0.183, -0.1, 0.0, 0.0 }, { 0.0283, 0.01, 0.0, 0.0 }, { 0.25, 0.183, 0.1, 0.0 } };
        final RawStore JamaAI = RawStore.FACTORY.rows(ai);

        final double[] bi = new double[] { 0.13, 0.87, 0.18, 0.82, 0.23, 0.77, -0.04, 99.67, -0.06, 100.33, 1.06, 99.62, -0.08 };
        final RawStore JamaBI = RawStore.FACTORY.columns(bi);

        Optimisation.Result result = null;

        try {

            final ConvexSolver.Builder qsBuilder = new ConvexSolver.Builder(JamaQ, JamaC);
            qsBuilder.inequalities(JamaAI, JamaBI);

            final ConvexSolver qSolver = qsBuilder.build();

            // qSolver.options.debug(ConvexSolver.class);

            result = qSolver.solve();

            OptimisationConvexTests.assertDirectAndIterativeEquals(qsBuilder, null);

        } catch (final Exception e) {
            e.printStackTrace();
            assert false;
        }

        final CompoundFunction<Double> tmpObj = CompoundFunction.makePrimitive(JamaQ.multiply(0.5), JamaC.multiply(-1.0));

        TestUtils.assertEquals(State.OPTIMAL, result.getState());

        final int numElm = (int) result.count();

        final double[] expectedSolution = new double[] { -0.4, 0.12, -0.0196, -2.45785 };
        tmpObj.invoke(Access1D.wrap(expectedSolution));
        tmpObj.invoke(Access1D.asPrimitive1D(result));

        JamaBI.subtract(JamaAI.multiply(PrimitiveDenseStore.FACTORY.columns(expectedSolution)));
        JamaBI.subtract(JamaAI.multiply(PrimitiveDenseStore.FACTORY.columns(result)));

        for (int i = 0; i < numElm; i++) {
            TestUtils.assertEquals(expectedSolution[i], result.doubleValue(i), 1e-4);
        }

    }

    /**
     * Issue reported at GitHub
     * <p>
     * apete: I believe there are problems with the models the user supplied, but ojAlgo fails to correctly
     * identify and report these problems. Instead ojAlgo struggles and returns different solutions with
     * sequential executions. This test is designed to (only) ensure consistency between exections. (I don't
     * know what the correct solution is.)
     * </p>
     *
     * @see <a href="https://github.com/optimatika/ojAlgo/issues/5">GitHub Issue 5</a>
     */
    @Test
    public void testP20150720() {

        final ExpressionsBasedModel tmpModel1 = P20150720.buildModel1();
        final ExpressionsBasedModel tmpModel2 = P20150720.buildModel2();
        final ExpressionsBasedModel tmpModel3 = P20150720.buildModel3();

        // The problem is with the ConvexSolver, and it is present without integer constraints
        tmpModel1.relax(true);
        tmpModel2.relax(true);
        tmpModel3.relax(true);

        final Result tmpBaseResult1 = tmpModel1.maximise();
        final Result tmpBaseResult2 = tmpModel2.maximise();
        final Result tmpBaseResult3 = tmpModel3.maximise();

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel1, null);
        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel2, null);
        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpModel3, null);

        for (int l = 0; l < 10; l++) {

            final Result tmpResult1 = tmpModel1.maximise();

            if (OptimisationConvexTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Model 1");
                BasicLogger.debug(tmpResult1);
                BasicLogger.debug(tmpModel1);
            }

            TestUtils.assertStateNotLessThanFeasible(tmpResult1);

            TestUtils.assertEquals("Model 1 State @" + l, tmpBaseResult1.getState(), tmpResult1.getState());
            TestUtils.assertEquals("Model 1 Value @" + l, tmpBaseResult1.getValue(), tmpResult1.getValue());
            TestUtils.assertEquals("Model 1 Solution @" + l, tmpBaseResult1, tmpResult1);

            final Result tmpResult2 = tmpModel2.maximise();

            if (OptimisationConvexTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Model 2");
                BasicLogger.debug(tmpResult2);
                BasicLogger.debug(tmpModel2);
            }

            TestUtils.assertStateNotLessThanFeasible(tmpResult2);

            TestUtils.assertEquals("Model 2 State @" + l, tmpBaseResult2.getState(), tmpResult2.getState());
            TestUtils.assertEquals("Model 2 Value @" + l, tmpBaseResult2.getValue(), tmpResult2.getValue());
            TestUtils.assertEquals("Model 2 Solution @" + l, tmpBaseResult2, tmpResult2);

            final Result tmpResult3 = tmpModel3.maximise();

            if (OptimisationConvexTests.DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug("Model 3");
                BasicLogger.debug(tmpResult3);
                BasicLogger.debug(tmpModel3);
            }

            TestUtils.assertStateNotLessThanFeasible(tmpResult3);

            TestUtils.assertEquals("Model 3 State @" + l, tmpBaseResult3.getState(), tmpResult3.getState());
            TestUtils.assertEquals("Model 3 Value @" + l, tmpBaseResult3.getValue(), tmpResult3.getValue());
            TestUtils.assertEquals("Model 3 Solution @" + l, tmpBaseResult3, tmpResult3);
        }

    }

    /**
     * Issue reported at GitHub. A set of problems related to when Q is zero - a linear problem. Generally the
     * ConvexSolver is not the right option to handle linear problems, but there is some desireable behaviour.
     */
    @Test
    public void testP20150809() {

        final NumberContext precision = new NumberContext(11, 14, RoundingMode.HALF_EVEN);

        final Primitive64Array tmpExpectedSolution = Primitive64Array.wrap(new double[] { 0.12, -0.05, 0.08, 0.07 });
        final Primitive64Array tmpBoundedSolution = Primitive64Array.wrap(new double[] { 99999, -99999, 99999, 99999 });

        ConvexSolver tmpSolver = P20150809.buildModel(true, false);
        Result tmpResult = tmpSolver.solve();
        TestUtils.assertStateNotLessThanOptimal(tmpResult);
        TestUtils.assertEquals(tmpExpectedSolution, tmpResult, precision);

        tmpSolver = P20150809.buildModel(true, true);
        tmpResult = tmpSolver.solve();
        TestUtils.assertStateNotLessThanOptimal(tmpResult);
        TestUtils.assertEquals(tmpExpectedSolution, tmpResult, precision);

        tmpSolver = P20150809.buildModel(false, false);
        tmpResult = tmpSolver.solve();
        TestUtils.assertEquals(Optimisation.State.UNBOUNDED, tmpResult.getState());

        tmpSolver = P20150809.buildModel(false, true);
        tmpResult = tmpSolver.solve();
        TestUtils.assertStateNotLessThanOptimal(tmpResult); // Since it is now constrained, the solver should be able find the optimal solution.
        TestUtils.assertEquals(tmpBoundedSolution, tmpResult, precision);
    }

    /**
     * Problem reported to ojalgo-user. A simple bug caused a java.lang.ArithmeticException: / by zero. ojAlgo
     * did not handle the case with inequality constraints that are all active (the set of excluded
     * constraints was empty).
     */
    @Test
    public void testP20150908() {

        final PrimitiveDenseStore tmpQ = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 2, 0 }, { 0, 2 } });
        final PrimitiveDenseStore tmpC = PrimitiveDenseStore.FACTORY.columns(new double[] { 0, 0 });
        final PrimitiveDenseStore tmpAI = PrimitiveDenseStore.FACTORY.rows(new double[][] { { -1, -1 } });
        final PrimitiveDenseStore tmpBI = PrimitiveDenseStore.FACTORY.columns(new double[] { -1 });
        final Builder tmpBuilder = new ConvexSolver.Builder(tmpQ, tmpC).inequalities(tmpAI, tmpBI);
        final ConvexSolver tmpSolver = tmpBuilder.build();
        final Optimisation.Result tmpResult = tmpSolver.solve();

        final PrimitiveDenseStore tmpExpectedSolution = PrimitiveDenseStore.FACTORY.columns(new double[] { 0.5, 0.5 });
        final Optimisation.Result tmpExpectedResult = new Optimisation.Result(Optimisation.State.OPTIMAL, 0.5, tmpExpectedSolution);

        TestUtils.assertStateNotLessThanOptimal(tmpResult);

        TestUtils.assertEquals(tmpExpectedResult, tmpResult);

        OptimisationConvexTests.assertDirectAndIterativeEquals(tmpBuilder, null);
    }

    /**
     * <p>
     * I recently upgraded to v38.2 of Ojalgo for solving some quadratic programs. It seems that somewhere in
     * the code there is an assumption that whenever there are inequality constraints there must be at least
     * one equality constraint. My problem has a bunch of inequality constraints but no equality constraints
     * and running it gives a "divide by zero" error. Again, this only seems to manifest itself when there are
     * inequality constraints but no equality constraints. I have reproduced it below with a simple example.
     * </p>
     * <p>
     * apete. Most likely the same problem as P20150908 (Cannot reproduce the problem with the leatest code.)
     * </p>
     */
    @Test
    public void testP20150922() {

        final PrimitiveDenseStore Q = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 1.0, 0 }, { 0, 1.0 } });
        final PrimitiveDenseStore C = PrimitiveDenseStore.FACTORY.columns(new double[] { 0, 0 });

        final ConvexSolver.Builder myBuilderI = new ConvexSolver.Builder(Q, C);

        final PrimitiveDenseStore AI = PrimitiveDenseStore.FACTORY.rows(new double[] { 1, 1 });
        final PrimitiveDenseStore BI = PrimitiveDenseStore.FACTORY.columns(new double[] { 1 });

        myBuilderI.inequalities(AI, BI);

        final ConvexSolver prob = myBuilderI.build();
        final Result solved = prob.solve(); // java.lang.ArithmeticException: / by zero

        if (DEBUG) {
            BasicLogger.debug(solved);
        }

        final PrimitiveDenseStore AI2 = PrimitiveDenseStore.FACTORY.rows(new double[] { 1, 1 });
        final PrimitiveDenseStore BI2 = PrimitiveDenseStore.FACTORY.columns(new double[] { 2 });
        // Discovered that you got (fixed now) a problem if you modify a builder after it has been used to build a solver
        myBuilderI.inequalities(AI2, BI2);

        final ConvexSolver prob2 = myBuilderI.build();
        final Result solved2 = prob2.solve(); // java.lang.ArithmeticException: / by zero

        if (DEBUG) {
            BasicLogger.debug(solved2);
        }

        OptimisationConvexTests.assertDirectAndIterativeEquals(myBuilderI, null);
    }

}
