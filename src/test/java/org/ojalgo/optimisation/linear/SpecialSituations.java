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

import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;

/**
 * Examples from LP10-Special-Situations.pdf (Special Situations in the Simplex Algorithm)
 *
 * @author apete
 */
public class SpecialSituations extends OptimisationLinearTests {

    @Test
    public void testDegeneracy() {

        R064Store c = R064Store.FACTORY.column(new double[] { -2, -1, 0, 0, 0 });
        RawStore A = RawStore.wrap(new double[][] { { 4, 3, 1, 0, 0 }, { 4, 1, 0, 1, 0 }, { 4, 2, 0, 0, 1 } });
        R064Store b = R064Store.FACTORY.column(new double[] { 12, 8, 8 });

        R064Store x = R064Store.FACTORY.column(new double[] { 2, 0, 4, 0, 0 });

        LinearSolver.Builder builder = LinearSolver.newBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.OPTIMAL, x);
        Result actual = lp.solve();

        // Same solution as in the example
        TestUtils.assertStateAndSolution(expected, actual);

        Function<LinearStructure, SimplexTableau> factory1 = OptimisationLinearTests.TABLEAU_FACTORIES.get(0);
        for (Function<LinearStructure, SimplexTableau> factory2 : OptimisationLinearTests.TABLEAU_FACTORIES) {

            SimplexTableau tableau1 = builder.newSimplexTableau(factory1);
            SimplexTableau tableau2 = builder.newSimplexTableau(factory2);

            // Dense and spare tableau implementations behave equal
            TestUtils.assertEquals(tableau1, tableau2);

            SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
            pivot.switchToPhase2();

            pivot.row = 1;
            pivot.col = 0;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            pivot.row = 2;
            pivot.col = 1;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            pivot.row = 2;
            pivot.col = 3;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            // Correct optimal value
            TestUtils.assertEquals(4.0, tableau1.doubleValue(3, 5 + 3));

            // Same fix result
            tableau1.fixVariable(0, 1.5);
            tableau2.fixVariable(0, 1.5);
            TestUtils.assertEquals(tableau1, tableau2);

            // Make optimal again
            pivot.row = 1;
            pivot.col = 1;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            // Correct when fixing on the solver
            lp.fixVariable(0, 1.5);
            Result fixed = lp.solve();
            TestUtils.assertEquals(-4.0, fixed.getValue());
            TestUtils.assertEquals(1.5, fixed.doubleValue(0));
            TestUtils.assertEquals(1.0, fixed.doubleValue(1));
        }
    }

    @Test
    public void testMultipleOptimalSolutions() {

        R064Store c = R064Store.FACTORY.column(new double[] { -4, -14, 0, 0 });
        RawStore A = RawStore.wrap(new double[][] { { 2, 7, 1, 0 }, { 7, 2, 0, 1 } });
        R064Store b = R064Store.FACTORY.column(new double[] { 21, 21 });

        R064Store x = R064Store.FACTORY.column(new double[] { 7.0 / 3.0, 7.0 / 3.0, 0, 0 });

        LinearSolver.Builder builder = LinearSolver.newBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.OPTIMAL, x);
        Result actual = lp.solve();

        TestUtils.assertStateAndSolution(expected, actual);

        Function<LinearStructure, SimplexTableau> factory1 = OptimisationLinearTests.TABLEAU_FACTORIES.get(0);
        for (Function<LinearStructure, SimplexTableau> factory2 : OptimisationLinearTests.TABLEAU_FACTORIES) {

            SimplexTableau tableau1 = builder.newSimplexTableau(factory1);
            SimplexTableau tableau2 = builder.newSimplexTableau(factory2);

            TestUtils.assertEquals(tableau1, tableau2);

            SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
            pivot.switchToPhase2();

            pivot.row = 0;
            pivot.col = 1;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            pivot.row = 1;
            pivot.col = 0;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            TestUtils.assertEquals(42.0, tableau2.doubleValue(2, 4 + 2));
        }
    }

    @Test
    public void testUnboundedness() {

        R064Store c = R064Store.FACTORY.column(new double[] { -2, -1, 0, 0 });
        RawStore A = RawStore.wrap(new double[][] { { 1, -1, 1, 0 }, { 2, -1, 0, 1 } });
        R064Store b = R064Store.FACTORY.column(new double[] { 10, 40 });

        R064Store x = R064Store.FACTORY.column(new double[] { 30, 20, 0, 0 });

        LinearSolver.Builder builder = LinearSolver.newBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.UNBOUNDED, x);
        Result actual = lp.solve();

        TestUtils.assertStateAndSolution(expected, actual);

        Function<LinearStructure, SimplexTableau> factory1 = OptimisationLinearTests.TABLEAU_FACTORIES.get(0);
        for (Function<LinearStructure, SimplexTableau> factory2 : OptimisationLinearTests.TABLEAU_FACTORIES) {

            SimplexTableau tableau1 = builder.newSimplexTableau(factory1);
            SimplexTableau tableau2 = builder.newSimplexTableau(factory2);

            TestUtils.assertEquals(tableau1, tableau2);

            SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
            pivot.switchToPhase2();

            pivot.row = 0;
            pivot.col = 0;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            pivot.row = 1;
            pivot.col = 1;
            tableau1.pivot(pivot);
            tableau2.pivot(pivot);
            TestUtils.assertEquals(tableau1, tableau2);

            TestUtils.assertEquals(80.0, tableau2.doubleValue(2, 4 + 2));
        }
    }

}
