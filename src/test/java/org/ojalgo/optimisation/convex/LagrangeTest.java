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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.matrix.store.R064Store.FACTORY;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.matrix.task.SolverTask;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.convex.ConvexSolver.Builder;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

public class LagrangeTest extends OptimisationConvexTests {

    /**
     * Multivariable Quadratic Programming example taken from:
     * https://people.duke.edu/~hpgavin/cee201/LagrangeMultipliers.pdf
     */
    @Test
    public void testGavinAndScruggsExample() {

        NumberContext accuracy = NumberContext.of(2); // Example solutions are very much rounded

        RawStore mtrxQ = RawStore.wrap(new double[][] { { 2, 3 }, { 3, 10 } });
        R064Store mtrxC = FACTORY.column(-0.5, 0); // Defined negated the ojAlgo way

        ConvexSolver unconstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).build();

        R064Store unconstrainedX = FACTORY.column(-0.45, 0.14);

        TestUtils.assertEquals(unconstrainedX, unconstrainedSolver.solve(), accuracy);

        RawStore mtrxAI = RawStore.wrap(new double[][] { { 3, 2 }, { 15, -3 } });
        R064Store mtrxBI = FACTORY.column(-2, 1);

        ConvexSolver equalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).equalities(mtrxAI, mtrxBI).build();

        R064Store equalityX = FACTORY.column(-0.10, -0.85);
        R064Store equalityL = FACTORY.column(3.55, -0.56);

        Result equalitySolution = equalityConstrainedSolver.solve();
        Access1D<?> equalityMultipliers = equalitySolution.getMultipliers().get();

        TestUtils.assertEquals(equalityX, equalitySolution, accuracy);
        TestUtils.assertEquals(equalityL, equalityMultipliers, accuracy);

        ConvexSolver inequalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).inequalities(mtrxAI, mtrxBI).build();

        R064Store inequalityX = FACTORY.column(-0.81, 0.21);
        R064Store inequalityL = FACTORY.column(0.16, 0);

        if (DEBUG) {
            inequalityConstrainedSolver.options.debug(Optimisation.Solver.class);
            inequalityConstrainedSolver.options.validate = true;
        }

        Result inequalitySolution = inequalityConstrainedSolver.solve();
        Access1D<?> inequalityMultipliers = inequalitySolution.getMultipliers().get();

        TestUtils.assertEquals(inequalityX, inequalitySolution, accuracy);
        TestUtils.assertEquals(inequalityL, inequalityMultipliers, accuracy);

        // The first constraint is active, and the second is not
        // Setting the first as an equality constraint, and only the other as an
        // inequality should give the same result.

        ConvexSolver mixConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).equalities(mtrxAI.row(0), mtrxBI.row(0))
                .inequalities(mtrxAI.row(1), mtrxBI.row(1)).build();

        Result mixSolution = mixConstrainedSolver.solve();
        Access1D<?> mixMultipliers = mixSolution.getMultipliers().get();

        TestUtils.assertEquals(inequalityX, mixSolution, accuracy);
        TestUtils.assertEquals(inequalityL, mixMultipliers, accuracy);
    }

    /**
     * Copy of {@link ConvexProblems#testP20200924()} and then modified...
     * <p>
     * Nocedal & Wright give the multipliers [3, -2] (because they defined KKT that way) but ojAlgo returns
     * [-3, 2] <br>
     * Test for https://github.com/optimatika/ojAlgo/issues/280.
     * <p>
     * 2020-09-24: No multipliers was returned by org.ojalgo.optimisation.convex classes :
     * </p>
     * Test from 'Numerical Optimization', 2ed, (2006), Jorge Nocedal and Stephen J. Wright. QP Example 16.2
     * p453 minimize function F(x1,x2,x3) = 3*x1*x1 + 2*x1*x2 + x1*x3 + 2.5*x2*x2 + 2*x2*x3 + 2*x3*x3 - 8*x1 -
     * 3*x2 - 3*x3 constraints x1 + x3 = 3, x2 + x3 = 0 result: x = [2, -1, 1]' multipliers = [3, -2]'
     *
     * @throws RecoverableCondition
     */
    @Test
    public void testNocedalAndWrightExample() throws RecoverableCondition {

        NumberContext accuracy = NumberContext.of(12);

        RawStore mtrxQ = RawStore.wrap(new double[][] { { 6, 2, 1 }, { 2, 5, 2 }, { 1, 2, 4 } });
        R064Store mtrxC = FACTORY.column(8, 3, 3); // Negated, because that how ojAgo expects it
        RawStore mtrxAE = RawStore.wrap(new double[][] { { 1, 0, 1 }, { 0, 1, 1 } });
        R064Store mtrxBE = FACTORY.column(3, 0);

        R064Store expectedX = FACTORY.column(2, -1, 1);
        R064Store expectedDual = FACTORY.column(-3, 2); // Negated, because N&W defined the KKT that way. Don't know why.

        MatrixStore<Double> bodyKKT = mtrxQ.right(mtrxAE.transpose()).below(mtrxAE);
        MatrixStore<Double> rhsKKT = mtrxC.below(mtrxBE);
        MatrixStore<Double> solutionKKT = expectedX.below(expectedDual);

        SolverTask<Double> equationSolver = SolverTask.R064.make(bodyKKT, rhsKKT);
        MatrixStore<Double> combinedSolution = equationSolver.solve(bodyKKT, rhsKKT);

        TestUtils.assertEquals(solutionKKT, combinedSolution, accuracy);

        Builder builder = ConvexSolver.newBuilder();
        builder.objective(mtrxQ, mtrxC);
        builder.equalities(mtrxAE, mtrxBE);

        Result result = builder.solve();

        TestUtils.assertEquals(expectedX, result, accuracy);

        // [A][x] = [b]
        MatrixStore<Double> computedB = mtrxAE.multiply(expectedX);
        TestUtils.assertEquals(mtrxBE, computedB, accuracy);

        // [Q][x] + [A]<sup>T</sup>[L] = [C]
        MatrixStore<Double> computedC = mtrxQ.multiply(expectedX).add(mtrxAE.transpose().multiply(expectedDual));
        TestUtils.assertEquals(mtrxC, computedC, accuracy);

        Optional<Access1D<?>> multipliers = result.getMultipliers();
        TestUtils.assertTrue("No multipliers present", multipliers.isPresent());
        TestUtils.assertEquals("Lagrangian Multipliers differ", expectedDual, multipliers.get(), accuracy);

        //  Test similar system where each equality constraint are converted into two inequality constraints.
        //  The result should be the same.
        Builder altBuilder = ConvexSolver.newBuilder();
        altBuilder.objective(mtrxQ, mtrxC);
        MatrixStore<Double> altAI = mtrxAE.below(mtrxAE.negate());
        MatrixStore<Double> altBI = mtrxBE.below(mtrxBE.negate());
        altBuilder.inequalities(altAI, altBI);

        Optimisation.Options options = new Optimisation.Options();
        Result altResult = altBuilder.build(options).solve();
        TestUtils.assertEquals(expectedX, result, accuracy);

        Optional<Access1D<?>> altMultipliers = altResult.getMultipliers();
        TestUtils.assertTrue("No multipliers present", altMultipliers.isPresent());

        R064Store expectedInequalityDual = FACTORY.column(0, 2, 3, 0);
        TestUtils.assertEquals(expectedInequalityDual, altMultipliers.get(), accuracy);

    }

}
