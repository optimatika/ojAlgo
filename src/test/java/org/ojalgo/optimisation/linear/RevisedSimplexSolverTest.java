/*
 * Copyright 1997-2024 Optimatika
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

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver.GeneralBuilder;

public class RevisedSimplexSolverTest extends OptimisationLinearTests {

    static void assertEquals(final Result expected, final Result actual) {

        TestUtils.assertEquals(expected.getState(), actual.getState());

        if (expected.getState().isFeasible()) {
            for (int i = 0; i < expected.size(); i++) {
                TestUtils.assertEquals(expected.doubleValue(i), actual.doubleValue(i));
            }
        }

        if (Double.isFinite(expected.getValue())) {
            TestUtils.assertEquals(expected.getValue(), actual.getValue());
        }
    }

    static void doTestDualVariants(final ExpressionsBasedModel model, final Result expected) {

        ExpressionsBasedModel simplified = model.simplify();

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        DualSimplexSolver tableauSolver = TableauStore.build(simplified).newDualSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, tableauSolver);

        DualSimplexSolver revisedSolver = RevisedStore.build(simplified).newDualSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, revisedSolver);
    }

    static void doTestDualVariants(final LinearSolver.GeneralBuilder builder, final Result expected, final int... basis) {

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        DualSimplexSolver tableauSolver = TableauStore.build(builder, basis).newDualSimplexSolver(options, basis);
        RevisedSimplexSolverTest.doTestOneVariant(expected, tableauSolver);

        DualSimplexSolver revisedSolver = RevisedStore.build(builder, basis).newDualSimplexSolver(options, basis);
        RevisedSimplexSolverTest.doTestOneVariant(expected, revisedSolver);
    }

    static void doTestOneVariant(final ExpressionsBasedModel model, final Result expected, final SimplexSolver solver) {

        String solverClassSimpleName = solver.getClass().getSimpleName();

        if (DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug("==================================================");
            BasicLogger.debug(solverClassSimpleName);
            BasicLogger.debug("==================================================");
            BasicLogger.debug();
        }

        Result actual = solver.solve();

        if (DEBUG) {
            BasicLogger.debug();
            BasicLogger.debug(solverClassSimpleName + ": " + actual);
            BasicLogger.debug();
        }

        if (model != null) {
            TestUtils.assertSolutionValid(model, expected);
        }

        RevisedSimplexSolverTest.assertEquals(expected, actual);
    }

    static void doTestOneVariant(final Result expected, final SimplexSolver solver) {
        RevisedSimplexSolverTest.doTestOneVariant(null, expected, solver);
    }

    static void doTestPhasedVariants(final ExpressionsBasedModel model, final Result expected) {

        ExpressionsBasedModel simplified = model.simplify();

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        PhasedSimplexSolver tableauSolver = TableauStore.build(simplified).newPhasedSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, tableauSolver);

        PhasedSimplexSolver revisedSolver = RevisedStore.build(simplified).newPhasedSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, revisedSolver);
    }

    static void doTestPhasedVariants(final LinearSolver.GeneralBuilder builder, final Result expected) {

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        PhasedSimplexSolver tableauSolver = SimplexStore.build(builder).newPhasedSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(expected, tableauSolver);

        PhasedSimplexSolver revisedSolver = SimplexStore.build(builder).newPhasedSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(expected, revisedSolver);
    }

    static void doTestPrimalVariants(final ExpressionsBasedModel model, final Result expected) {

        ExpressionsBasedModel simplified = model.simplify();

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        PrimalSimplexSolver tableauSolver = TableauStore.build(simplified).newPrimalSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, tableauSolver);

        PrimalSimplexSolver revisedSolver = RevisedStore.build(simplified).newPrimalSimplexSolver(options);
        RevisedSimplexSolverTest.doTestOneVariant(model, expected, revisedSolver);
    }

    static void doTestPrimalVariants(final LinearSolver.GeneralBuilder builder, final Result expected, final int... basis) {

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            options.debug(LinearSolver.class);
        }

        PrimalSimplexSolver tableauSolver = TableauStore.build(builder, basis).newPrimalSimplexSolver(options, basis);
        RevisedSimplexSolverTest.doTestOneVariant(expected, tableauSolver);

        PrimalSimplexSolver revisedSolver = RevisedStore.build(builder, basis).newPrimalSimplexSolver(options, basis);
        RevisedSimplexSolverTest.doTestOneVariant(expected, revisedSolver);
    }

    /**
     * https://people.math.carleton.ca/~kcheung/math/notes/MATH5801/06/6_3_revised_dual_simplex.html
     */
    @Test
    public void testDualCarletonKcheungInfeasible() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(-4, -2, 0, 1, 3).equality(0, 2, -1, 1, 2, 0).equality(-1, 1, 1, -1, -2, 0).equality(5, 1, 2, 0,
                0, 1);

        Optimisation.Result expected = Result.of(State.INFEASIBLE);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected, 0, 1, 2);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://people.math.carleton.ca/~kcheung/math/notes/MATH5801/06/6_3_revised_dual_simplex.html
     * <p>
     * The example states that {0,1} should be a dual feasible basis, but since those columns do not make out
     * an identity matrix it cannot be used as a starting basis here.
     */
    @Test
    public void testDualCarletonKcheungWE1() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(0, 0, 0).equality(2, 1, 1, -1).equality(3, 1, -1, 2);

        Optimisation.Result expected = Result.of(0.0, State.OPTIMAL, 7.0 / 3.0, 0.0, 1.0 / 3.0);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://people.math.carleton.ca/~kcheung/math/notes/MATH5801/06/6_3_revised_dual_simplex.html
     */
    @Test
    public void testDualCarletonKcheungWE2() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(4, 3, 0, -1).equality(1, 3, -1, -1, 0).equality(3, -1, 2, 1, -1);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 2.0, 0.0, 5.0, 0.0);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected, 2, 3);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://people.math.carleton.ca/~kcheung/math/notes/MATH5801/06/6_3_revised_dual_simplex.html
     */
    @Test
    public void testDualCarletonKcheungWE3() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(-3, -4, 0, 0, 0, 0).equality(6, 1, 1, 0, 1, 0, 0).equality(10, 2, 1, 1, 0, 0, 0)
                .equality(4, -1, 1, 0, 0, 1, 0).equality(5, 1, 1, 0, 0, 0, 1);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 0.5, 4.5, 4.5, 1.0, 0.0, 0.0);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected, 0, 1, 2, 5);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://faculty.math.illinois.edu/~mlavrov/docs/482-fall-2019/lecture14.pdf
     */
    @Test
    public void testDualIllinoisMlavrov() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(1, 1).inequality(-6, -2, -1).inequality(-7, -3, -1).inequality(-8, -1, -3);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 2.0, 2.0);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://personal.math.ubc.ca/~israel/m340/dualrev.pdf
     * <p>
     * Bad example. The solution given is: 0.0, 0.0, 3.0, 0.5, 0.0, 0.0, 2.0
     * <p>
     * 0.0, 0.0, 3.0, 1.5, 0.0, 4.0, 0.0 is also feasible, and has the same objective function value.
     */
    @Test
    public void testDualIsraelM340() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(1, 2, 1, 0).inequality(-3, 3, -1, -1, 0).inequality(-2, 1, 0, 0, -4).inequality(6, -3, 2, 1, 2);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 0.0, 0.0, 3.0, 0.5);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * Unbounded variables, multiple optimal solutions, and optimal solution with negative values
     *
     * @see CommonsMathSimplexSolverTest#testMath434NegativeVariable()
     */
    @Test
    public void testMath434NegativeVariable() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable v0 = model.addVariable();
        Variable v1 = model.addVariable();
        Variable v2 = model.addVariable().weight(1);

        model.addExpression().add(v0, 1).add(v1, 1).level(5);
        model.addExpression().add(v2, 1).lower(-10);

        // 5, 0, -10 and 0, 5, -10 (or any linear kombination) are both optimal
        Optimisation.Result expected = Optimisation.Result.of(-10, State.OPTIMAL, 5, 0, -10);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);

        LinearSolver.GeneralBuilder builder = LinearSolver.newGeneralBuilder(0.0, 0.0, 1.0);
        builder.equality(5, 1, 1, 0).inequality(10, 0, 0, -1).lower(Double.NEGATIVE_INFINITY);
        // Set up with all variables unbounded and then an inequality to limit "lower(-10)"
        // That's different from what the EBM would result in.

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * Copy / reimplementation of {@link LinearDesignTestCases#testP20130409b()}. The implementation of the
     * primal solver had problems with this.
     */
    @Test
    public void testP20130409b() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.newVariable("x1");
        Variable x2013 = model.newVariable("x2013");
        Variable x2014 = model.newVariable("x2014");
        Variable x2015 = model.newVariable("x2015");

        x2013.lower(1245L);
        x2014.lower(1269L);

        Expression obj = model.addExpression("obj");
        obj.set(x1, 1);
        obj.weight(1);

        Expression c1 = model.addExpression("c1");
        c1.set(x1, 1);
        c1.lower(0);

        Expression c2 = model.addExpression("c2");
        c2.set(x2014, -5000);
        c2.set(x2013, 5100);
        c2.set(x1, -1);
        c2.upper(0);

        Expression c3 = model.addExpression("c3");
        c3.set(x2014, -5000);
        c3.set(x2013, 5100);
        c3.set(x1, 1);
        c3.lower(0);

        Expression c4 = model.addExpression("c4");
        c4.set(x2014, 150);
        c4.set(x2013, 5100);
        c4.set(x2015, -5000);
        c4.set(x1, -1);
        c4.upper(0);

        Expression c5 = model.addExpression("c5");
        c5.set(x2014, 150);
        c5.set(x2013, 5100);
        c5.set(x2015, -5000);
        c5.set(x1, 1);
        c5.lower(0);

        Expression c6 = model.addExpression("c6");
        c6.set(x2015, 5000);
        c6.set(x2014, 5000);
        c6.set(x2013, 5000);
        c6.level(19105000);

        Result expected = Result.of(4850, State.OPTIMAL, 4850, 1245, 1269, 1307);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);
    }

    /**
     * Variation of {@link #testP20130409b()} without the unbounded variable – split in positive and negative
     * part each with a lower bound of 0.0.
     */
    @Test
    public void testP20130409bMod1() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.addVariable("x1").lower(0).weight(1);
        Variable x2013 = model.addVariable("x2013").lower(1245);
        Variable x2014 = model.addVariable("x2014").lower(1269);
        Variable x2015p = model.addVariable("x2015p").lower(0);
        Variable x2015n = model.addVariable("x2015n").lower(0);

        Expression c2 = model.addExpression("c2");
        c2.set(x1, -1);
        c2.set(x2013, 5100);
        c2.set(x2014, -5000);
        c2.upper(0);

        Expression c3 = model.addExpression("c3");
        c3.set(x1, 1);
        c3.set(x2013, 5100);
        c3.set(x2014, -5000);
        c3.lower(0);

        Expression c4 = model.addExpression("c4");
        c4.set(x1, -1);
        c4.set(x2013, 5100);
        c4.set(x2014, 150);
        c4.set(x2015p, -5000);
        c4.set(x2015n, 5000);
        c4.upper(0);

        Expression c5 = model.addExpression("c5");
        c5.set(x1, 1);
        c5.set(x2013, 5100);
        c5.set(x2014, 150);
        c5.set(x2015p, -5000);
        c5.set(x2015n, 5000);
        c5.lower(0);

        Expression c6 = model.addExpression("c6");
        c6.set(x2013, 5000);
        c6.set(x2014, 5000);
        c6.set(x2015p, 5000);
        c6.set(x2015n, -5000);
        c6.level(19105000);

        Result expected = Result.of(4850, State.OPTIMAL, 4850, 1245, 1269, 1307, 0);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);
    }

    /**
     * Further variation of {@link #testP20130409b()} and {@link #testP20130409bMod1()} with all variable
     * lower bounds shifted to be 0.0.
     */
    @Test
    public void testP20130409bMod2() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x1 = model.addVariable("x1").lower(0).weight(1);
        Variable x2013 = model.addVariable("x2013").lower(0); // 1245
        Variable x2014 = model.addVariable("x2014").lower(0); // 1269
        Variable x2015p = model.addVariable("x2015p").lower(0);
        Variable x2015n = model.addVariable("x2015n").lower(0);

        Expression c2 = model.addExpression("c2");
        c2.set(x1, -1);
        c2.set(x2013, 5100);
        c2.set(x2014, -5000);
        c2.upper(0 - (5100 * 1245) - (-5000 * 1269));

        Expression c3 = model.addExpression("c3");
        c3.set(x1, 1);
        c3.set(x2013, 5100);
        c3.set(x2014, -5000);
        c3.lower(0 - (5100 * 1245) - (-5000 * 1269));

        Expression c4 = model.addExpression("c4");
        c4.set(x1, -1);
        c4.set(x2013, 5100);
        c4.set(x2014, 150);
        c4.set(x2015p, -5000);
        c4.set(x2015n, 5000);
        c4.upper(0 - (5100 * 1245) - (150 * 1269));

        Expression c5 = model.addExpression("c5");
        c5.set(x1, 1);
        c5.set(x2013, 5100);
        c5.set(x2014, 150);
        c5.set(x2015p, -5000);
        c5.set(x2015n, 5000);
        c5.lower(0 - (5100 * 1245) - (150 * 1269));

        Expression c6 = model.addExpression("c6");
        c6.set(x2013, 5000);
        c6.set(x2014, 5000);
        c6.set(x2015p, 5000);
        c6.set(x2015n, -5000);
        c6.level(19_105_000 - (5000 * 1245) - (5000 * 1269));

        Result expected = Result.of(4850, State.OPTIMAL, 4850, 0, 0, 1307, 0);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);
    }

    /**
     * https://cs.brown.edu/courses/csci2580/HTML/lecture6-revisedsimplex.pdf
     */
    @Test
    public void testPrimalBrownCsci2580() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(-60, -30, -20).inequality(48, 8, 6, 1).inequality(20, 4, 2, 1.5).inequality(8, 2, 0.5, 0.5);

        Optimisation.Result expected = Optimisation.Result.of(-300, State.OPTIMAL, 3, 4, 0);

        RevisedSimplexSolverTest.doTestPrimalVariants(builder, expected);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://people.math.carleton.ca/~kcheung/math/notes/MATH5801/05/5_1_simplex.html
     */
    @Test
    public void testPrimalCarletonKcheung() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(1, 3, -1, 3).equality(4, 1, 1, 1, 1).equality(2, 1, -1, 2, -1);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 0.0, 2.0, 2.0, 0.0);

        RevisedSimplexSolverTest.doTestPrimalVariants(builder, expected, 0, 1);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://homepages.rpi.edu/~mitchj/handouts/upperbounds/
     */
    @Test
    public void testPrimalMitchellBounds() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(0, 0, -2, -3).equality(4, 1, 0, 1, 1).equality(1, 0, 1, -1, -2).upper(10, 4, 5, 1);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 1.0, 4.0, 3.0, 0.0);

        RevisedSimplexSolverTest.doTestPrimalVariants(builder, expected, 0, 1);

        RevisedSimplexSolverTest.doTestDualVariants(builder, expected, 0, 1);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * https://en.wikipedia.org/wiki/Revised_simplex_method
     */
    @Test
    public void testPrimalWikipedia() {

        GeneralBuilder builder = LinearSolver.newGeneralBuilder().objective(-2, -3, -4).inequality(10, 3, 2, 1).inequality(15, 2, 5, 3);

        Optimisation.Result expected = Result.of(State.OPTIMAL, 0.0, 0.0, 5.0, 5.0, 0.0);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    @Test
    public void testShitingRange() {

        Random random = new Random();

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X");
        Variable y = model.addVariable("Y");

        double b1 = 10.0 * random.nextDouble();
        double b2 = -10.0 * random.nextDouble();

        double a1 = (2.0 * random.nextDouble()) - 1.0;
        double a2 = (2.0 * random.nextDouble()) - 1.0;

        model.addExpression().add(x, a1).add(y, 1.0).upper(b1);
        model.addExpression().add(x, a2).add(y, 1.0).lower(b2);

        for (int i = -2; i <= 2; i++) {

            x.weight(2.0 * (random.nextDouble() - 0.5));
            x.lower(i - 1).upper(i + 1);

            y.weight(2.0 * (random.nextDouble() - 0.5));
            y.lower(null).upper(null);

            Result min = model.minimise();
            Result max = model.maximise();

            TestUtils.assertStateNotLessThanFeasible(min);
            TestUtils.assertStateNotLessThanFeasible(max);

            TestUtils.assertSolutionFeasible(model, min);
            TestUtils.assertSolutionFeasible(model, max);

            TestUtils.assertTrue(max.getValue() > min.getValue());
        }
    }

    /**
     * Problem with "solve unconstrained"
     *
     * @see CommonsMathSimplexSolverTest#testSingleVariableAndConstraint()
     */
    @Test
    public void testSingleVariableAndConstraint() {

        Optimisation.Result expected = Optimisation.Result.of(-30, Optimisation.State.OPTIMAL, 10);

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Variable variable = model.addVariable().lower(0).weight(-3);
        model.addExpression().set(variable, 1).upper(10);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);

        GeneralBuilder builder = LinearSolver.newGeneralBuilder(-3);
        builder.inequality(10, 1);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);

        builder.reset();

        builder.objective(-3).upper(10);

        RevisedSimplexSolverTest.doTestPhasedVariants(builder, expected);
    }

    /**
     * Unbounded variables, and optimal solution with negative values
     *
     * @see CommonsMathSimplexSolverTest#testSolutionWithNegativeDecisionVariable()
     */
    @Test
    public void testSolutionWithNegativeDecisionVariable() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable v0 = model.addVariable().weight(2);
        Variable v1 = model.addVariable().weight(-1);

        model.addExpression().add(v0, 1).add(v1, 2).upper(14);
        model.addExpression().add(v0, 1).add(v1, 1).lower(6);

        Result expected = Result.of(-12, State.OPTIMAL, -2, 8);

        RevisedSimplexSolverTest.doTestPhasedVariants(model, expected);
    }

    // TODO Example https://www.uobabylon.edu.iq/eprints/publication_11_20693_31.pdf
    // TODO Example http://faculty.ndhu.edu.tw/~ywan/courses/network/notes/Lect_6_Revised_Simplex_new.pdf
    // TODO Example https://cyberlab.engr.uconn.edu/wp-content/uploads/sites/2576/2018/09/lecture9.pdf
    // TODO Example http://cgm.cs.mcgill.ca/~avis/courses/567/notes/ch10.pdf
    // TODO Example https://faculty.math.illinois.edu/~mlavrov/docs/482-fall-2019/lecture14.pdf
    // TODO Example https://personal.math.ubc.ca/~israel/m340/dualrev.pdf
    // TODO Example http://cgm.cs.mcgill.ca/~avis/courses/567/notes/ch10.pdf
    // TODO https://co-at-work.zib.de/slides/Mittwoch_16.9/Gurobi_LP.pdf

}
