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
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.linear.LinearSolver.StandardBuilder;
import org.ojalgo.optimisation.linear.SimplexTableau.SparseTableau;

/**
 * Examples from LP10-Special-Situations.pdf (Special Situations in the Simplex Algorithm)
 *
 * @author apete
 */
public class SpecialSituations extends OptimisationLinearTests {

    public SpecialSituations() {
        super();
    }

    @Test
    public void testDegeneracy() {

        Primitive64Store c = Primitive64Store.FACTORY.columns(new double[] { -2, -1, 0, 0, 0 });
        Primitive64Store A = Primitive64Store.FACTORY.rows(new double[][] { { 4, 3, 1, 0, 0 }, { 4, 1, 0, 1, 0 }, { 4, 2, 0, 0, 1 } });
        Primitive64Store b = Primitive64Store.FACTORY.columns(new double[] { 12, 8, 8 });

        Primitive64Store x = Primitive64Store.FACTORY.columns(new double[] { 2, 0, 4, 0, 0 });

        StandardBuilder builder = LinearSolver.newStandardBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.OPTIMAL, x);
        Result actual = lp.solve();

        // Same solution as in the example
        TestUtils.assertStateAndSolution(expected, actual);

        SimplexTableau dense = SimplexTableau.newDense(builder.getOptimisationData());
        SparseTableau sparse = SimplexTableau.newSparse(builder.getOptimisationData());

        // Dense and spare tableau implementations behave equal
        TestUtils.assertEquals(dense, sparse);

        SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
        pivot.switchToPhase2();

        pivot.row = 1;
        pivot.col = 0;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        pivot.row = 2;
        pivot.col = 1;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        pivot.row = 2;
        pivot.col = 3;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        // Correct optimal value
        TestUtils.assertEquals(4.0, dense.doubleValue(3, 5 + 3));

        // Same fix result
        dense.fixVariable(0, 1.5);
        sparse.fixVariable(0, 1.5);
        TestUtils.assertEquals(dense, sparse);

        // Make optimal again
        pivot.row = 1;
        pivot.col = 1;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        // Correct when fixing on the solver
        lp.fixVariable(0, 1.5);
        Result fixed = lp.solve();
        TestUtils.assertEquals(-4.0, fixed.getValue());
        TestUtils.assertEquals(1.5, fixed.doubleValue(0));
        TestUtils.assertEquals(1.0, fixed.doubleValue(1));
    }

    @Test
    public void testMultipleOptimalSolutions() {

        Primitive64Store c = Primitive64Store.FACTORY.columns(new double[] { -4, -14, 0, 0 });
        Primitive64Store A = Primitive64Store.FACTORY.rows(new double[][] { { 2, 7, 1, 0 }, { 7, 2, 0, 1 } });
        Primitive64Store b = Primitive64Store.FACTORY.columns(new double[] { 21, 21 });

        Primitive64Store x = Primitive64Store.FACTORY.columns(new double[] { 7.0 / 3.0, 7.0 / 3.0, 0, 0 });

        StandardBuilder builder = LinearSolver.newStandardBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.OPTIMAL, x);
        Result actual = lp.solve();

        TestUtils.assertStateAndSolution(expected, actual);

        SimplexTableau dense = SimplexTableau.newDense(builder.getOptimisationData());
        SparseTableau sparse = SimplexTableau.newSparse(builder.getOptimisationData());

        TestUtils.assertEquals(dense, sparse);

        SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
        pivot.switchToPhase2();

        pivot.row = 0;
        pivot.col = 1;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        pivot.row = 1;
        pivot.col = 0;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        TestUtils.assertEquals(42.0, dense.doubleValue(2, 4 + 2));
    }

    @Test
    public void testUnboundedness() {

        Primitive64Store c = Primitive64Store.FACTORY.columns(new double[] { -2, -1, 0, 0 });
        Primitive64Store A = Primitive64Store.FACTORY.rows(new double[][] { { 1, -1, 1, 0 }, { 2, -1, 0, 1 } });
        Primitive64Store b = Primitive64Store.FACTORY.columns(new double[] { 10, 40 });

        Primitive64Store x = Primitive64Store.FACTORY.columns(new double[] { 30, 20, 0, 0 });

        StandardBuilder builder = LinearSolver.newStandardBuilder().objective(c).equalities(A, b);
        LinearSolver lp = builder.build();

        Result expected = new Optimisation.Result(Optimisation.State.UNBOUNDED, x);
        Result actual = lp.solve();

        TestUtils.assertStateAndSolution(expected, actual);

        SimplexTableau dense = SimplexTableau.newDense(builder.getOptimisationData());
        SparseTableau sparse = SimplexTableau.newSparse(builder.getOptimisationData());

        TestUtils.assertEquals(dense, sparse);

        SimplexTableauSolver.IterationPoint pivot = new SimplexTableauSolver.IterationPoint();
        pivot.switchToPhase2();

        pivot.row = 0;
        pivot.col = 0;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        pivot.row = 1;
        pivot.col = 1;
        dense.pivot(pivot);
        sparse.pivot(pivot);
        TestUtils.assertEquals(dense, sparse);

        TestUtils.assertEquals(80.0, dense.doubleValue(2, 4 + 2));
    }

}
