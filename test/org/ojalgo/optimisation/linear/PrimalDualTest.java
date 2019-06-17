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

    LinearSolver.ModelIntegration LINEAR_INTEGRATION = new LinearSolver.ModelIntegration();

    public PrimalDualTest() {
        super();
    }

    @Test
    public void testP20080117() {

        ExpressionsBasedModel model = ConvexProblems.buildP20080117();

        ConvexSolver.Builder convex = ConvexSolver.getBuilder();
        ConvexSolver.copy(model, convex);

        SimplexTableau primTab1 = PrimalSimplex.build(model);
        SimplexTableau primTab2 = PrimalSimplex.build(convex, model.options);
        SimplexTableau primTab3 = DualSimplex.build(convex, model.options);

        PrimalSimplex solver1 = new PrimalSimplex(primTab1, model.options);

        PrimalSimplex solver2 = new PrimalSimplex(primTab2, model.options);
        DualSimplex solver3 = new DualSimplex(primTab3, model.options);

        Result solve1 = solver1.solve();
        Result solve2 = solver2.solve();
        Result solve3 = solver3.solve();

        if (DEBUG) {
            BasicLogger.debug(solve1);
            BasicLogger.debug(solve2);
            BasicLogger.debug(solve3);

        }

        Result result1 = LINEAR_INTEGRATION.toModelState(solve1, model);
        Result result2 = PrimalSimplex.toConvexState(solve2, convex);
        Result result3 = DualSimplex.toConvexState(solve3, convex);

        if (DEBUG) {

            BasicLogger.debug(result1);
            BasicLogger.debug(result2);
            BasicLogger.debug(result3);

            BasicLogger.debug(result2.getMultipliers().get());
            BasicLogger.debug(result3.getMultipliers().get());
        }

        TestUtils.assertStateAndSolution(result1, result2);
        TestUtils.assertStateAndSolution(result1, result3);

        TestUtils.assertEquals(result2.getMultipliers().get(), result3.getMultipliers().get());
    }

    /**
     * http://courses.mai.liu.se/GU/TAOP88/Fo/h-TAOP88_04_LPdual.pdf
     */
    @Test
    public void testTAOP88() {

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

        this.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY);
    }

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

        this.doCompare(primModel, dualModel, optimalValue, optimalX, optimalY);
    }

    /**
     * @param primModel Assume to maximise
     * @param dualModel Assume to minimise
     */
    private void doCompare(final ExpressionsBasedModel primModel, final ExpressionsBasedModel dualModel, final double optimalValue,
            final DenseArray<Double> optimalX, final DenseArray<Double> optimalY) {

        if (DEBUG) {
            primModel.options.debug(LinearSolver.class);
            dualModel.options.debug(LinearSolver.class);
        }

        Result primResult = primModel.maximise();
        Result dualResult = dualModel.minimise();

        TestUtils.assertEquals(optimalValue, primResult.getValue(), PrimitiveMath.RELATIVELY_SMALL);
        TestUtils.assertEquals(optimalValue, dualResult.getValue(), PrimitiveMath.RELATIVELY_SMALL);
        TestUtils.assertEquals(optimalX, primResult);
        TestUtils.assertEquals(optimalY, dualResult);
        TestUtils.assertEquals(primResult.getState().isOptimal(), dualResult.getState().isOptimal());

        ConvexSolver.Builder primConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(primModel, primConvex);

        ConvexSolver.Builder dualConvex = ConvexSolver.getBuilder();
        ConvexSolver.copy(dualModel, dualConvex);

        Result primModelPrimSolver = PrimalSimplex.solve(primConvex, primModel.options);
        Result primModelDualSolver = DualSimplex.solve(primConvex, dualModel.options);
        Result dualModelPrimSolver = PrimalSimplex.solve(dualConvex, dualModel.options);
        Result dualModelDualSolver = DualSimplex.solve(dualConvex, primModel.options);

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

        TestUtils.assertEquals(primModelPrimSolver.getMultipliers().get(), primModelDualSolver.getMultipliers().get());
        TestUtils.assertEquals(dualModelPrimSolver.getMultipliers().get(), dualModelDualSolver.getMultipliers().get());
    }

}
