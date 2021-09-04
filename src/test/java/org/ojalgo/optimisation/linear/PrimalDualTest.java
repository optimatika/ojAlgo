/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexProblems;
import org.ojalgo.optimisation.convex.ConvexSolver;

public class PrimalDualTest extends OptimisationLinearTests {

    /**
     * @param primModel Assume to maximise
     * @param dualModel Assume to minimise
     * @param detailed TODO
     */
    private static void doCompare(final ExpressionsBasedModel primModel, final ExpressionsBasedModel dualModel, final double optimalValue,
            final DenseArray<Double> optimalX, final DenseArray<Double> optimalY, final boolean detailed) {

        if (DEBUG) {
            primModel.options.debug(LinearSolver.class);
            dualModel.options.debug(LinearSolver.class);
        }

        Result primResult = primModel.maximise();

        if (DEBUG) {
            BasicLogger.debug(primModel);
            BasicLogger.debug(primResult);
        }

        Result dualResult = dualModel.minimise();

        if (DEBUG) {
            BasicLogger.debug(dualModel);
            BasicLogger.debug(dualResult);
        }

        TestUtils.assertEquals(optimalValue, primResult.getValue(), PrimitiveMath.RELATIVELY_SMALL);
        TestUtils.assertEquals(optimalValue, dualResult.getValue(), PrimitiveMath.RELATIVELY_SMALL);
        TestUtils.assertEquals(optimalX, primResult);
        TestUtils.assertEquals(optimalY, dualResult);
        TestUtils.assertEquals(primResult.getState().isOptimal(), dualResult.getState().isOptimal());

        ConvexSolver.Builder primConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(primModel, primConvex);

        Result primModelPrimSolver = PrimalSimplex.doSolve(primConvex, primModel.options, false);
        Result primModelDualSolver = DualSimplex.doSolve(primConvex, dualModel.options, false);

        ConvexSolver.Builder dualConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(dualModel, dualConvex);

        Result dualModelPrimSolver = PrimalSimplex.doSolve(dualConvex, dualModel.options, false);
        Result dualModelDualSolver = DualSimplex.doSolve(dualConvex, primModel.options, false);

        if (DEBUG) {

            BasicLogger.debug();
            BasicLogger.debug("Result/Solution");
            BasicLogger.debug("\tMultipliers");

            BasicLogger.debug();
            BasicLogger.debug("primModelPrimSolver");
            BasicLogger.debug(primModelPrimSolver);
            BasicLogger.debug("\t" + primModelPrimSolver.getMultipliers().get());

            BasicLogger.debug();
            BasicLogger.debug("primModelDualSolver");
            BasicLogger.debug(primModelDualSolver);
            BasicLogger.debug("\t" + primModelDualSolver.getMultipliers().get());

            BasicLogger.debug();
            BasicLogger.debug("dualModelPrimSolver");
            BasicLogger.debug(dualModelPrimSolver);
            BasicLogger.debug("\t" + dualModelPrimSolver.getMultipliers().get());

            BasicLogger.debug();
            BasicLogger.debug("dualModelDualSolver");
            BasicLogger.debug(dualModelDualSolver);
            BasicLogger.debug("\t" + dualModelDualSolver.getMultipliers().get());
        }

        TestUtils.assertStateAndSolution(primResult, primModelPrimSolver);
        TestUtils.assertStateAndSolution(primResult, primModelDualSolver);
        TestUtils.assertStateAndSolution(dualResult, dualModelPrimSolver);
        TestUtils.assertStateAndSolution(dualResult, dualModelDualSolver);

        if (detailed) {
            // Sometimes the presolver finds additional lower/upper constraints on the variables
            // In those cases these tests may fail.
            TestUtils.assertEquals(primModelPrimSolver.getMultipliers().get(), primModelDualSolver.getMultipliers().get());
        }
        TestUtils.assertEquals(dualModelPrimSolver.getMultipliers().get(), dualModelDualSolver.getMultipliers().get());
    }

    private static void doEvaluate(final ExpressionsBasedModel model, final boolean minimise) {

        Result modResult = minimise ? model.minimise() : model.maximise();

        ConvexSolver.Builder convex = ConvexSolver.getBuilder();
        ConvexSolver.copy(model, convex);

        Result primResult = PrimalSimplex.doSolve(convex, model.options, false);
        Result dualResult = DualSimplex.doSolve(convex, model.options, false);

        if (DEBUG) {

            BasicLogger.debug(modResult);
            BasicLogger.debug(primResult);
            BasicLogger.debug(dualResult);

            BasicLogger.debug(primResult.getMultipliers().get());
            BasicLogger.debug(dualResult.getMultipliers().get());
        }

        TestUtils.assertStateAndSolution(modResult, primResult);
        TestUtils.assertStateAndSolution(modResult, dualResult);

        TestUtils.assertEquals(primResult.getMultipliers().get(), dualResult.getMultipliers().get());
    }

    LinearSolver.ModelIntegration LINEAR_INTEGRATION = new LinearSolver.ModelIntegration();

    public PrimalDualTest() {
        super();
    }

    /**
     * https://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15859-f11/www/notes/lecture05.pdf
     */
    @Test
    public void testCaseCMU() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel();
        Variable x1 = primModel.addVariable("X1").lower(0).weight(2);
        Variable x2 = primModel.addVariable("X2").lower(0).weight(3);
        primModel.addExpression().set(x1, 4).set(x2, 8).upper(12);
        primModel.addExpression().set(x1, 2).set(x2, 1).upper(3);
        primModel.addExpression().set(x1, 3).set(x2, 2).upper(4);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel();
        Variable y1 = dualModel.addVariable("Y1").lower(0).weight(12);
        Variable y2 = dualModel.addVariable("Y2").lower(0).weight(3);
        Variable y3 = dualModel.addVariable("Y3").lower(0).weight(4);
        dualModel.addExpression().set(y1, 4).set(y2, 2).set(y3, 3).lower(2);
        dualModel.addExpression().set(y1, 8).set(y2, 1).set(y3, 2).lower(3);

        double optimalValue = 4.75;
        DenseArray<Double> optimalX = Primitive64Array.FACTORY.copy(new double[] { 0.5, 1.25 });
        DenseArray<Double> optimalY = Primitive64Array.FACTORY.copy(new double[] { 5.0 / 16.0, 0.0, 0.25 });

        PrimalDualTest.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY, true);
    }

    /**
     * http://courses.mai.liu.se/GU/TAOP88/Fo/h-TAOP88_04_LPdual.pdf
     */
    @Test
    public void testCaseLIU() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel();
        Variable x1 = primModel.addVariable("X1").lower(0).weight(4);
        Variable x2 = primModel.addVariable("X2").lower(0).weight(3);
        primModel.addExpression().set(x1, 2).set(x2, 3).upper(30);
        primModel.addExpression().set(x1, 1).set(x2, 0).upper(6);
        primModel.addExpression().set(x1, 6).set(x2, 4).upper(50);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel();
        Variable y1 = dualModel.addVariable("Y1").lower(0).weight(30);
        Variable y2 = dualModel.addVariable("Y2").lower(0).weight(6);
        Variable y3 = dualModel.addVariable("Y3").lower(0).weight(50);
        dualModel.addExpression().set(y1, 2).set(y2, 1).set(y3, 6).lower(4);
        dualModel.addExpression().set(y1, 3).set(y2, 0).set(y3, 4).lower(3);

        double optimalValue = 36.0;
        DenseArray<Double> optimalX = Primitive64Array.FACTORY.copy(new double[] { 3.0, 8.0 });
        DenseArray<Double> optimalY = Primitive64Array.FACTORY.copy(new double[] { 0.2, 0.0, 0.6 });

        PrimalDualTest.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY, true);
    }

    /**
     * http://web.mit.edu/15.053/www/AMP-Chapter-04.pdf
     */
    @Test
    public void testCaseMIT() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel();
        Variable x1 = primModel.addVariable("X1").lower(0).weight(6);
        Variable x2 = primModel.addVariable("X2").lower(0).weight(14);
        Variable x3 = primModel.addVariable("X3").lower(0).weight(13);
        primModel.addExpression().set(x1, 0.5).set(x2, 2.0).set(x3, 1.0).upper(24);
        primModel.addExpression().set(x1, 1.0).set(x2, 2.0).set(x3, 4.0).upper(60);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel();
        Variable y1 = dualModel.addVariable("Y1").lower(0).weight(24);
        Variable y2 = dualModel.addVariable("Y2").lower(0).weight(60);
        dualModel.addExpression().set(y1, 0.5).set(y2, 1).lower(6);
        dualModel.addExpression().set(y1, 2).set(y2, 2).lower(14);
        dualModel.addExpression().set(y1, 1).set(y2, 4).lower(13);

        double optimalValue = 294.0;
        DenseArray<Double> optimalX = Primitive64Array.FACTORY.copy(new double[] { 36.0, 0.0, 6.0 });
        DenseArray<Double> optimalY = Primitive64Array.FACTORY.copy(new double[] { 11, 0.5 });

        PrimalDualTest.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY, true);
    }

    @Test
    public void testP20080117() {

        ExpressionsBasedModel model = ConvexProblems.buildP20080117();

        PrimalDualTest.doEvaluate(model, true);
    }

    /**
     * https://en.wikipedia.org/wiki/Dual_linear_program
     */
    @Test
    public void testWikipediaExample() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel();
        Variable x1 = primModel.addVariable("X1").lower(0).weight(3);
        Variable x2 = primModel.addVariable("X2").lower(0).weight(4);
        primModel.addExpression().set(x1, 5).set(x2, 6).level(7);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel();
        Variable y1 = dualModel.addVariable("Y1").weight(7);
        dualModel.addExpression().set(y1, 5).lower(3);
        dualModel.addExpression().set(y1, 6).lower(4);

        double optimalValue = 14.0 / 3.0;
        DenseArray<Double> optimalX = Primitive64Array.FACTORY.copy(new double[] { 0.0, 7.0 / 6.0 });
        DenseArray<Double> optimalY = Primitive64Array.FACTORY.copy(new double[] { 4.0 / 6.0 });

        ConvexSolver.Builder primConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(primModel, primConvex);

        TestUtils.assertEquals(2, primConvex.countVariables());
        TestUtils.assertEquals(3, primConvex.countConstraints());
        TestUtils.assertEquals(1, primConvex.countEqualityConstraints());
        TestUtils.assertEquals(2, primConvex.countInequalityConstraints());

        Result primModelPrimSolver = PrimalSimplex.doSolve(primConvex, primModel.options, false);

        TestUtils.assertEquals(2, primModelPrimSolver.size());
        TestUtils.assertEquals(3, primModelPrimSolver.getMultipliers().get().size());

        Result primModelDualSolver = DualSimplex.doSolve(primConvex, dualModel.options, false);

        TestUtils.assertEquals(2, primModelDualSolver.size());
        TestUtils.assertEquals(3, primModelDualSolver.getMultipliers().get().size());

        ConvexSolver.Builder dualConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(dualModel, dualConvex);

        TestUtils.assertEquals(1, dualConvex.countVariables());
        TestUtils.assertEquals(2, dualConvex.countConstraints());
        TestUtils.assertEquals(0, dualConvex.countEqualityConstraints());
        TestUtils.assertEquals(2, dualConvex.countInequalityConstraints());

        Result dualModelPrimSolver = PrimalSimplex.doSolve(dualConvex, dualModel.options, false);

        TestUtils.assertEquals(1, dualModelPrimSolver.size());
        TestUtils.assertEquals(2, dualModelPrimSolver.getMultipliers().get().size());

        Result dualModelDualSolver = DualSimplex.doSolve(dualConvex, primModel.options, false);

        TestUtils.assertEquals(1, dualModelDualSolver.size());
        TestUtils.assertEquals(2, dualModelDualSolver.getMultipliers().get().size());

        PrimalDualTest.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY, false);
    }

}
