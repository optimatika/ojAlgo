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
package org.ojalgo.optimisation.convex;

import static org.ojalgo.matrix.store.Primitive64Store.FACTORY;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
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

        Primitive64Store mtrxQ = FACTORY.rows(new double[][] { { 2, 3 }, { 3, 10 } });
        Primitive64Store mtrxC = FACTORY.column(-0.5, 0); // Defined negated the ojAlgo way

        ConvexSolver unconstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).build();

        Primitive64Store unconstrainedX = FACTORY.column(-0.45, 0.14);

        TestUtils.assertEquals(unconstrainedX, unconstrainedSolver.solve(), accuracy);

        Primitive64Store mtrxAI = FACTORY.rows(new double[][] { { 3, 2 }, { 15, -3 } });
        Primitive64Store mtrxBI = FACTORY.column(-2, 1);

        ConvexSolver equalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).equalities(mtrxAI, mtrxBI).build();

        Primitive64Store equalityX = FACTORY.column(-0.10, -0.85);
        Primitive64Store equalityL = FACTORY.column(3.55, -0.56);

        Result equalitySolution = equalityConstrainedSolver.solve();
        Access1D<?> equalityMultipliers = equalitySolution.getMultipliers().get();

        TestUtils.assertEquals(equalityX, equalitySolution, accuracy);
        TestUtils.assertEquals(equalityL, equalityMultipliers, accuracy);

        ConvexSolver inequalityConstrainedSolver = ConvexSolver.newBuilder().objective(mtrxQ, mtrxC).inequalities(mtrxAI, mtrxBI).build();

        Primitive64Store inequalityX = FACTORY.column(-0.81, 0.21);
        Primitive64Store inequalityL = FACTORY.column(0.16, 0);

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
     * Copy of {@link ConvexProblems#testP20200924()} and the modified... <br>
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

        Primitive64Store Q = FACTORY.rows(new double[][] { { 6, 2, 1 }, { 2, 5, 2 }, { 1, 2, 4 } });
        Primitive64Store C = FACTORY.column(-8, -3, -3);
        Primitive64Store AE = FACTORY.rows(new double[][] { { 1, 0, 1 }, { 0, 1, 1 } });
        Primitive64Store BE = FACTORY.column(3, 0);

        Primitive64Store expectedX = FACTORY.column(2, -1, 1);
        Primitive64Store expectedDual = FACTORY.column(-3, 2);

        MatrixStore<Double> bodyKKT = Q.right(AE.transpose()).below(AE);
        MatrixStore<Double> rhsKKT = C.negate().below(BE);
        MatrixStore<Double> solutionKKT = expectedX.below(expectedDual);

        SolverTask<Double> equationSolver = SolverTask.PRIMITIVE.make(bodyKKT, rhsKKT);
        MatrixStore<Double> combinedSolution = equationSolver.solve(bodyKKT, rhsKKT);

        TestUtils.assertEquals(solutionKKT, combinedSolution, accuracy);

        Builder builder = ConvexSolver.newBuilder();
        builder.objective(Q, C.negate());
        builder.equalities(AE, BE);
        ConvexSolver solver = builder.build();

        Result result = solver.solve();

        TestUtils.assertEquals(expectedX, result, accuracy);

        // [A][x] = [b]
        MatrixStore<Double> computedB = AE.multiply(expectedX);
        TestUtils.assertEquals(BE, computedB, accuracy);

        // [Q][x] + [A]<sup>T</sup>[L] = - [C]
        MatrixStore<Double> computedC = Q.multiply(expectedX).add(AE.transpose().multiply(expectedDual)).negate();
        TestUtils.assertEquals(C, computedC, accuracy);

        Optional<Access1D<?>> multipliers = result.getMultipliers();
        TestUtils.assertTrue("No multipliers present", multipliers.isPresent());
        TestUtils.assertEquals("Lagrangian Multipliers differ", expectedDual, multipliers.get(), accuracy);

        //  Test similar system where each equality constraint are converted into two inequality constraints.
        //  The result should be the same.
        Builder ieBuilder = ConvexSolver.newBuilder();
        ieBuilder.objective(Q, C.negate());
        MatrixStore<Double> AI = AE.below(AE.negate());
        MatrixStore<Double> BI = BE.below(BE.negate());
        ieBuilder.inequalities(AI, BI);
        ConvexSolver ieSolver = ieBuilder.build();
        Result ieResult = ieSolver.solve();
        TestUtils.assertEquals(expectedX, result, accuracy);

        Optional<Access1D<?>> ieMultipliers = ieResult.getMultipliers();
        TestUtils.assertTrue("No multipliers present", ieMultipliers.isPresent());

        Primitive64Store expectedInequalityDual = FACTORY.column(0, 2, 3, 0);
        TestUtils.assertEquals(expectedInequalityDual, ieMultipliers.get(), accuracy);

    }

}
