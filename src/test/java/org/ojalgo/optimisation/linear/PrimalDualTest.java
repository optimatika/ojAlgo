/*
 * Copyright 1997-2023 Optimatika
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

import static org.ojalgo.function.constant.BigMath.*;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.Integration;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.convex.ConvexData;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.optimisation.convex.OptimisationConvexTests;
import org.ojalgo.optimisation.linear.LinearSolver.GeneralBuilder;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * Test cases that compare the primal and dual LP models - have to have both, as well as both the full primal
 * and dual solutions.
 * <p>
 * Typically these tests need to be executed with presolving turned off.
 */
public class PrimalDualTest extends OptimisationLinearTests implements ModelFileTest {

    static void assertPrimalDualPair(final Optimisation.Result primal, final Optimisation.Result dual) {

        TestUtils.assertStateNotLessThanOptimal(primal);
        TestUtils.assertStateNotLessThanOptimal(dual);

        TestUtils.assertEquals(primal.getValue(), dual.getValue());

        Access1D<?> primalMultipliers = primal.getMultipliers().get();
        Access1D<?> dualMultipliers = dual.getMultipliers().get();

        for (int i = 0; i < primal.size(); i++) {
            TestUtils.assertEquals(primal.doubleValue(i), dualMultipliers.doubleValue(i));
        }

        for (int i = 0; i < dual.size(); i++) {
            TestUtils.assertEquals(dual.doubleValue(i), primalMultipliers.doubleValue(i));
        }
    }

    static void assertResultAndFullSolution(final ExpressionsBasedModel model, final Optimisation.Sense sense, final Optimisation.Result expectedResult,
            final Map<String, Double> optimalSolution) {

        Result actualResult = sense == Sense.MIN ? model.minimise() : model.maximise();

        TestUtils.assertResult(expectedResult, actualResult);

        for (Variable variable : model.getVariables()) {
            TestUtils.assertEquals(optimalSolution.get(variable.getName()).doubleValue(), variable.getValue().doubleValue());
        }

        for (KeyedPrimitive<EntryPair<ModelEntity<?>, ConstraintType>> matched : actualResult.getMatchedMultipliers()) {
            TestUtils.assertEquals(optimalSolution.get(matched.getKey().getKey().getName()).doubleValue(), matched.doubleValue());
        }
    }

    /**
     * The results from the primal and dual variants should be exactly the same, in every aspect. Further
     * there solutions should be the same as for the model. The objective function value as well as the
     * multipliers may be different from the model.
     */
    static void comparePrimalAndDualSolvers(final ExpressionsBasedModel model, final Optimisation.Sense sense) {

        ExpressionsBasedModel.clearPresolvers();

        Result modResult = sense == Sense.MIN ? model.minimise() : model.maximise();

        ConvexSolver.Builder convex = ConvexSolver.newBuilder();
        ConvexSolver.copy(model, convex);

        ConvexData<Double> convexData = OptimisationConvexTests.getOptimisationData(convex);
        Result primResult = PrimalSimplex.doSolve(convexData, model.options, false);
        Result dualResult = DualSimplex.doSolve(convexData, model.options, false);

        if (DEBUG) {

            BasicLogger.debug(model);

            BasicLogger.debug("Model: {}", modResult);
            BasicLogger.debug("Primal: {}", primResult);
            BasicLogger.debug("Dual: {}", dualResult);

            BasicLogger.debug("Primal multipliers: {}", primResult.getMultipliers().get());
            BasicLogger.debug("Dual multipliers: {}", dualResult.getMultipliers().get());
        }

        TestUtils.assertStateAndSolution(modResult, primResult);
        TestUtils.assertStateAndSolution(modResult, dualResult);

        TestUtils.assertResult(primResult, dualResult);
    }

    @AfterEach
    public void doAfterEach() {
        ExpressionsBasedModel.clearIntegrations();
        ExpressionsBasedModel.resetPresolvers();
    }

    @BeforeEach
    public void doBeforeEach() {
        ExpressionsBasedModel.clearPresolvers();
    }

    /**
     * https://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15859-f11/www/notes/lecture05.pdf (lecture05.pdf)
     */
    @Test
    public void testCaseCMU() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel(); // max
        Variable x1 = primModel.newVariable("X1").lower(0).weight(2);
        Variable x2 = primModel.newVariable("X2").lower(0).weight(3);
        primModel.newExpression("Y1").set(x1, 4).set(x2, 8).upper(12);
        primModel.newExpression("Y2").set(x1, 2).set(x2, 1).upper(3);
        primModel.newExpression("Y3").set(x1, 3).set(x2, 2).upper(4);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel(); // min
        Variable y1 = dualModel.newVariable("Y1").lower(0).weight(12);
        Variable y2 = dualModel.newVariable("Y2").lower(0).weight(3);
        Variable y3 = dualModel.newVariable("Y3").lower(0).weight(4);
        dualModel.newExpression("X1").set(y1, 4).set(y2, 2).set(y3, 3).lower(2);
        dualModel.newExpression("X2").set(y1, 8).set(y2, 1).set(y3, 2).lower(3);

        double optimalValue = 4.75;
        Map<String, Double> optimalSolution = Map.of("X1", 0.5, "X2", 1.25, "Y1", 5.0 / 16.0, "Y2", 0.0, "Y3", 0.25);
        Optimisation.Result primExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 0.5, 1.25);
        Optimisation.Result dualExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 5.0 / 16.0, 0.0, 0.25);

        PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

        PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);

        PrimalDualTest.comparePrimalAndDualSolvers(primModel, Sense.MAX);

        PrimalDualTest.comparePrimalAndDualSolvers(dualModel, Sense.MIN);

        GeneralBuilder primBuilder = LinearSolver.newGeneralBuilder(-2.0, -3.0); // Negated, since max
        primBuilder.inequality(12.0, 4.0, 8.0);
        primBuilder.inequality(3.0, 2.0, 1.0);
        primBuilder.inequality(4.0, 3.0, 2.0);

        GeneralBuilder dualBuilder = LinearSolver.newGeneralBuilder(12.0, 3.0, 4.0);
        dualBuilder.inequality(-2.0, -4.0, -2.0, -3.0); // Negated, since lower
        dualBuilder.inequality(-3.0, -8.0, -1.0, -2.0); // Negated, since lower

        Result primBldrSolve = primBuilder.solve().withNegatedValue(); // Negated, since max
        TestUtils.assertResult(primExpected, primBldrSolve);

        Result dualBldrSolve = dualBuilder.solve();
        TestUtils.assertResult(dualExpected, dualBldrSolve);

        PrimalDualTest.assertPrimalDualPair(primBldrSolve, dualBldrSolve);

        for (Entry<String, Integration<LinearSolver>> entry : INTEGRATIONS.entrySet()) {

            String identifier = entry.getKey();
            ExpressionsBasedModel.Integration<LinearSolver> integration = entry.getValue();

            ExpressionsBasedModel.clearIntegrations();
            ExpressionsBasedModel.addIntegration(integration);

            if (DEBUG) {
                primModel.options.debug(LinearSolver.class);
                dualModel.options.debug(LinearSolver.class);
            }

            PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

            PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);
        }
    }

    /**
     * http://courses.mai.liu.se/GU/TAOP88/Fo/h-TAOP88_04_LPdual.pdf
     */
    @Test
    public void testCaseLIU() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel(); // max
        Variable x1 = primModel.newVariable("X1").lower(0).weight(4);
        Variable x2 = primModel.newVariable("X2").lower(0).weight(3);
        primModel.newExpression("Y1").set(x1, 2).set(x2, 3).upper(30);
        primModel.newExpression("Y2").set(x1, 1).set(x2, 0).upper(6);
        primModel.newExpression("Y3").set(x1, 6).set(x2, 4).upper(50);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel(); // min
        Variable y1 = dualModel.newVariable("Y1").lower(0).weight(30);
        Variable y2 = dualModel.newVariable("Y2").lower(0).weight(6);
        Variable y3 = dualModel.newVariable("Y3").lower(0).weight(50);
        dualModel.newExpression("X1").set(y1, 2).set(y2, 1).set(y3, 6).lower(4);
        dualModel.newExpression("X2").set(y1, 3).set(y2, 0).set(y3, 4).lower(3);

        double optimalValue = 36.0;
        Map<String, Double> optimalSolution = Map.of("X1", 3.0, "X2", 8.0, "Y1", 0.2, "Y2", 0.0, "Y3", 0.6);
        Optimisation.Result primExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 3.0, 8.0);
        Optimisation.Result dualExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 0.2, 0.0, 0.6);

        PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

        PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);

        PrimalDualTest.comparePrimalAndDualSolvers(primModel, Sense.MAX);

        PrimalDualTest.comparePrimalAndDualSolvers(dualModel, Sense.MIN);

        GeneralBuilder primBuilder = LinearSolver.newGeneralBuilder(-4.0, -3.0); // Negated, since max
        primBuilder.inequality(30.0, 2.0, 3.0);
        primBuilder.inequality(6.0, 1.0, 0.0);
        primBuilder.inequality(50.0, 6.0, 4.0);

        GeneralBuilder dualBuilder = LinearSolver.newGeneralBuilder(30.0, 6.0, 50.0);
        dualBuilder.inequality(-4.0, -2.0, -1.0, -6.0); // Negated, since lower
        dualBuilder.inequality(-3.0, -3.0, -0.0, -4.0); // Negated, since lower

        Result primBldrSolve = primBuilder.solve().withNegatedValue(); // Negated, since max
        TestUtils.assertResult(primExpected, primBldrSolve);

        Result dualBldrSolve = dualBuilder.solve();
        TestUtils.assertResult(dualExpected, dualBldrSolve);

        PrimalDualTest.assertPrimalDualPair(primBldrSolve, dualBldrSolve);

        for (Entry<String, Integration<LinearSolver>> entry : INTEGRATIONS.entrySet()) {

            String identifier = entry.getKey();
            ExpressionsBasedModel.Integration<LinearSolver> integration = entry.getValue();

            ExpressionsBasedModel.clearIntegrations();
            ExpressionsBasedModel.addIntegration(integration);

            if (DEBUG) {
                primModel.options.debug(LinearSolver.class);
                dualModel.options.debug(LinearSolver.class);
            }

            PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

            PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);
        }
    }

    /**
     * http://web.mit.edu/15.053/www/AMP-Chapter-04.pdf (AMP-Chapter-04.pdf)
     * <p>
     * https://web.fe.up.pt/~mac/ensino/docs/OT20122013/Chapter%204%20-%20Duality%20in%20Linear%20Programming.pdf
     * (Chapter 4 - Duality in Linear Programming.pdf)
     */
    @Test
    public void testCaseMIT() {

        ExpressionsBasedModel.clearPresolvers();

        ExpressionsBasedModel primModel = new ExpressionsBasedModel(); // max
        Variable x1 = primModel.newVariable("X1").lower(0).weight(6);
        Variable x2 = primModel.newVariable("X2").lower(0).weight(14);
        Variable x3 = primModel.newVariable("X3").lower(0).weight(13);
        primModel.newExpression("Y1").set(x1, 0.5).set(x2, 2.0).set(x3, 1.0).upper(24);
        primModel.newExpression("Y2").set(x1, 1.0).set(x2, 2.0).set(x3, 4.0).upper(60);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel(); // min
        Variable y1 = dualModel.newVariable("Y1").lower(0).weight(24);
        Variable y2 = dualModel.newVariable("Y2").lower(0).weight(60);
        dualModel.newExpression("X1").set(y1, 0.5).set(y2, 1.0).lower(6);
        dualModel.newExpression("X2").set(y1, 2.0).set(y2, 2.0).lower(14);
        dualModel.newExpression("X3").set(y1, 1.0).set(y2, 4.0).lower(13);

        double optimalValue = 294.0;
        Map<String, Double> optimalSolution = Map.of("X1", 36.0, "X2", 0.0, "X3", 6.0, "Y1", 11.0, "Y2", 0.5);
        Optimisation.Result primExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 36.0, 0.0, 6.0);
        Optimisation.Result dualExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 11.0, 0.5);

        PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

        PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);

        PrimalDualTest.comparePrimalAndDualSolvers(primModel, Sense.MAX);

        PrimalDualTest.comparePrimalAndDualSolvers(dualModel, Sense.MIN);

        GeneralBuilder primBuilder = LinearSolver.newGeneralBuilder(-6.0, -14.0, -13.0); // Negated, since max
        primBuilder.inequality(24.0, 0.5, 2.0, 1.0);
        primBuilder.inequality(60.0, 1.0, 2.0, 4.0);

        GeneralBuilder dualBuilder = LinearSolver.newGeneralBuilder(24.0, 60.0);
        dualBuilder.inequality(-6.0, -0.5, -1.0); // Negated, since lower
        dualBuilder.inequality(-14.0, -2.0, -2.0); // Negated, since lower
        dualBuilder.inequality(-13.0, -1.0, -4.0); // Negated, since lower

        Result primBldrSolve = primBuilder.solve().withNegatedValue(); // Negated, since max
        TestUtils.assertResult(primExpected, primBldrSolve);

        Result dualBldrSolve = dualBuilder.solve();
        TestUtils.assertResult(dualExpected, dualBldrSolve);

        PrimalDualTest.assertPrimalDualPair(primBldrSolve, dualBldrSolve);

        for (Entry<String, Integration<LinearSolver>> entry : INTEGRATIONS.entrySet()) {

            String identifier = entry.getKey();
            ExpressionsBasedModel.Integration<LinearSolver> integration = entry.getValue();

            ExpressionsBasedModel.clearIntegrations();
            ExpressionsBasedModel.addIntegration(integration);

            if (DEBUG) {
                primModel.options.debug(LinearSolver.class);
                dualModel.options.debug(LinearSolver.class);
            }

            PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

            PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Dual_linear_program
     */
    @Test
    public void testTinyDualityExampleFromWikipedia() {

        ExpressionsBasedModel primModel = new ExpressionsBasedModel(); // max
        Variable p1 = primModel.newVariable("P1").lower(ZERO).weight(THREE);
        Variable p2 = primModel.newVariable("P2").lower(ZERO).weight(FOUR);
        primModel.newExpression("D1").set(p1, FIVE).set(p2, SIX).level(SEVEN);

        ExpressionsBasedModel dualModel = new ExpressionsBasedModel(); // min
        Variable d1 = dualModel.newVariable("D1").weight(SEVEN);
        dualModel.newExpression("P1").set(d1, FIVE).lower(THREE);
        dualModel.newExpression("P2").set(d1, SIX).lower(FOUR);

        double optimalValue = 14.0 / 3.0;
        Map<String, Double> optimalSolution = Map.of("P1", 0.0, "P2", 7.0 / 6.0, "D1", 2.0 / 3.0);
        Optimisation.Result primExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 0.0, 7.0 / 6.0);
        Optimisation.Result dualExpected = Optimisation.Result.of(optimalValue, State.OPTIMAL, 2.0 / 3.0);

        PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

        PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);

        PrimalDualTest.comparePrimalAndDualSolvers(primModel, Sense.MAX);

        PrimalDualTest.comparePrimalAndDualSolvers(dualModel, Sense.MIN);

        GeneralBuilder primBuilder = LinearSolver.newGeneralBuilder(-3.0, -4.0); // Negated, since max
        primBuilder.equality(7.0, 5.0, 6.0);

        // dual builder skipped since unbounded variable

        Result primBldrSolve = primBuilder.solve().withNegatedValue(); // Negated, since max
        TestUtils.assertResult(primExpected, primBldrSolve);

        for (Entry<String, Integration<LinearSolver>> entry : INTEGRATIONS.entrySet()) {

            String identifier = entry.getKey();
            ExpressionsBasedModel.Integration<LinearSolver> integration = entry.getValue();

            ExpressionsBasedModel.clearIntegrations();
            ExpressionsBasedModel.addIntegration(integration);

            if (DEBUG) {
                primModel.options.debug(LinearSolver.class);
                dualModel.options.debug(LinearSolver.class);
            }

            PrimalDualTest.assertResultAndFullSolution(primModel, Sense.MAX, primExpected, optimalSolution);

            PrimalDualTest.assertResultAndFullSolution(dualModel, Sense.MIN, dualExpected, optimalSolution);
        }
    }

}
