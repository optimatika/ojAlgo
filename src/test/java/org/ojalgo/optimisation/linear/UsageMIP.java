/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.BigMath.ONE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.optimisation.ModelEntity;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.ConstraintType;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;
import org.ojalgo.type.keyvalue.EntryPair;

/**
 * Specifically test features and behaviour for when used as a MIP sub-solver.
 */
public class UsageMIP extends OptimisationLinearTests {

    private static final NumberContext SIMILAR = NumberContext.of(4);

    static Set<Integer> getAllModelIndices(final EntityMap map) {

        Set<Integer> retVal = new HashSet<>();

        for (int i = 0, limit = map.countModelVariables(); i < limit; i++) {
            retVal.add(Integer.valueOf(map.indexOf(i)));
        }

        return retVal;
    }

    @Test
    public void testCombined() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> {

            ExpressionsBasedModel model = LinearDesignTestCases.makeModelPSmith338act14().model;

            model.getVariable(0).level(ONE);
            model.getVariable(1).lower(ONE);
            model.getVariable(2).level(null);

            return model;
        };

        this.performWith3(modelFactory);
    }

    @Test
    public void testFixed() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> {

            ExpressionsBasedModel model = LinearDesignTestCases.makeModelPSmith338act14().model;

            model.getVariable(0).level(ONE);

            return model;
        };

        this.performWith3(modelFactory);
    }

    @Test
    public void testMIP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.addVariable("X").binary().weight(1);
        Variable y = model.addVariable("Y").binary().weight(1);

        Expression obj = model.addExpression("Obj").level(1);
        obj.set(x, 1);
        obj.set(y, 1);

        Optimisation.Result result = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(1, result.getValue());
        TestUtils.assertEquals(1, result.doubleValue(0) + result.doubleValue(1));
    }

    @Test
    public void testPlain() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> LinearDesignTestCases.makeModelPSmith338act14().model;

        this.performWith3(modelFactory);
    }

    @Test
    public void testShifted() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> {

            ExpressionsBasedModel model = LinearDesignTestCases.makeModelPSmith338act14().model;

            model.getVariable(1).lower(ONE);

            return model;
        };

        this.performWith3(modelFactory);
    }

    @Test
    public void testStrange() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> {

            ExpressionsBasedModel model = LinearDesignTestCases.makeModelPSmith338act14().model;

            model.getVariable(0).level(ONE);
            model.getVariable(2).level(null);

            return model;
        };

        this.performWith3(modelFactory);
    }

    @Test
    public void testUnconstrained() {

        Supplier<ExpressionsBasedModel> modelFactory = () -> {

            ExpressionsBasedModel model = LinearDesignTestCases.makeModelPSmith338act14().model;

            model.getVariable(2).level(null);

            return model;
        };

        this.performWith3(modelFactory);
    }

    private void performWith3(final Supplier<ExpressionsBasedModel> modelFactory) {

        Optimisation.Options options = new Optimisation.Options();

        if (DEBUG) {
            //  options.debug(LinearSolver.class);
        }

        boolean[][] integer = new boolean[7][];
        integer[0] = new boolean[] { true, false, false };
        integer[1] = new boolean[] { false, true, false };
        integer[2] = new boolean[] { false, false, true };
        integer[3] = new boolean[] { true, true, false };
        integer[4] = new boolean[] { false, true, true };
        integer[5] = new boolean[] { true, false, true };
        integer[6] = new boolean[] { true, true, true };

        for (int i = 0; i < integer.length; i++) {

            ExpressionsBasedModel model = modelFactory.get();

            TestUtils.assertEquals(3, model.getVariables().size());

            for (int j = 0; j < integer[i].length; j++) {
                model.getVariable(j).setInteger(integer[i][j]);
            }

            Function<LinearStructure, SimplexTableau> referenceFactory = DenseTableau::new;
            SimplexTableau referenceTableau = SimplexTableauSolver.build(model, referenceFactory);
            SimplexTableauSolver referenceSolver = referenceTableau.newSimplexTableauSolver(options);
            LinearStructure referenceMap = referenceSolver.getEntityMap();
            Optimisation.Result referenceResult = referenceSolver.solve();

            if (!referenceResult.getState().isFeasible()) {
                continue;
            }

            boolean[] refIntegers = referenceSolver.integers(model);
            Collection<Equation> referenceCutCandidates = referenceSolver.generateCutCandidates(PrimitiveMath.ELEVENTH, refIntegers);

            if (DEBUG) {
                BasicLogger.debug();
                BasicLogger.debug();
                BasicLogger.debug();
                BasicLogger.debug("Integers: {}", Arrays.toString(refIntegers));
                BasicLogger.debug(model);
                BasicLogger.debug(referenceResult);
                BasicLogger.debug(referenceCutCandidates);
            }

            for (Function<LinearStructure, SimplexTableau> factory : OptimisationLinearTests.TABLEAU_FACTORIES) {

                SimplexTableau tableau = SimplexTableauSolver.build(model, factory);

                SimplexTableauSolver solver = tableau.newSimplexTableauSolver(options);

                LinearStructure map = solver.getEntityMap();

                Optimisation.Result result = solver.solve();

                boolean[] integers = solver.integers(model);
                Collection<Equation> cutCandidates = solver.generateCutCandidates(PrimitiveMath.ELEVENTH, integers);

                if (DEBUG) {
                    BasicLogger.debug();
                    BasicLogger.debug("Tableau {}: {}", tableau.getClass().getSimpleName(), result);
                    BasicLogger.debug("Tableau {}: {}", tableau.getClass().getSimpleName(), cutCandidates);
                }

                this.doTest(referenceCutCandidates, referenceMap, cutCandidates, map);
            }

            for (Function<LinearStructure, SimplexStore> factory : OptimisationLinearTests.STORE_FACTORIES) {

                SimplexStore store = SimplexSolver.build(model, factory);

                PhasedSimplexSolver solver = store.newPhasedSimplexSolver(options);

                LinearStructure map = solver.getEntityMap();

                Optimisation.Result result = solver.solve();

                boolean[] integers = solver.integers(model);
                Collection<Equation> cutCandidates = solver.generateCutCandidates(PrimitiveMath.ELEVENTH, integers);

                if (DEBUG) {
                    BasicLogger.debug();
                    BasicLogger.debug("Store {}: {}", store.getClass().getSimpleName(), result);
                    BasicLogger.debug("Store {}: {}", store.getClass().getSimpleName(), cutCandidates);
                }

                this.doTest(referenceCutCandidates, referenceMap, cutCandidates, map);
            }
        }
    }

    void doTest(final Collection<Equation> expectedCandidates, final LinearStructure expectedMap, final Collection<Equation> actualCandidates,
            final LinearStructure actualMap) {

        TestUtils.assertEquals(expectedCandidates.size(), actualCandidates.size());
        TestUtils.assertEquals(UsageMIP.getAllModelIndices(expectedMap), UsageMIP.getAllModelIndices(actualMap));
        // TestUtils.assertEquals(expectedMap.countSlackVariables(), actualMap.countSlackVariables());

        for (Equation expected : expectedCandidates) {

            boolean matchCut = false;

            for (Equation actual : actualCandidates) {

                TestUtils.assertEquals(actualMap.countModelVariables() + actualMap.countSlackVariables(), actual.size());

                if (!SIMILAR.isDifferent(expected.dot(expected), actual.dot(actual))) {

                    matchCut = true;

                    for (int m = 0; m < expectedMap.countModelVariables(); m++) {
                        //  TestUtils.assertEquals(expected.doubleValue(m), actual.doubleValue(m));
                    }

                    for (int e = 0; e < expectedMap.countSlackVariables(); e++) {

                        EntryPair<ModelEntity<?>, ConstraintType> slackE = expectedMap.getSlack(e);

                        boolean matchSlack = false;

                        for (int a = 0; a < actualMap.countSlackVariables(); a++) {

                            EntryPair<ModelEntity<?>, ConstraintType> slackA = actualMap.getSlack(a);

                            if (slackE.equals(slackA)) {

                                matchSlack = true;

                                TestUtils.assertEquals(expected.doubleValue(expectedMap.countModelVariables() + e),
                                        actual.doubleValue(actualMap.countModelVariables() + a));
                            }
                        }

                        TestUtils.assertTrue(matchSlack);
                    }
                }
            }

            TestUtils.assertTrue(matchCut);
        }
    }

}
