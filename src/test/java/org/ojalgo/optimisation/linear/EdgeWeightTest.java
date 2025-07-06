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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;

/**
 * Tests for steepest edge and devex edge weight calculations. These tests verify that: 1. Edge weights are
 * computed correctly for both tableau and revised simplex 2. Devex updates maintain mathematical properties
 * 3. Edge weights improve pivot selection
 */
public class EdgeWeightTest extends OptimisationLinearTests {

    private static void doTestDual(final LinearSolver.Builder builder) {

        Optimisation.Options options = new Optimisation.Options();

        DenseTableau tableau = builder.newSimplexStore(DenseTableau::new);
        DualSimplexSolver tSolver = tableau.newDualSimplexSolver(options);
        Result tResult = tSolver.solve();

        RevisedStore revised = builder.newSimplexStore(RevisedStore::new);
        DualSimplexSolver rSolver = revised.newDualSimplexSolver(options);
        Result rResult = rSolver.solve();

        TestUtils.assertStateAndSolution(tResult, rResult);

        EdgeWeightTest.verifyDualEdgeWeights(tableau, revised);
    }

    private static void doTestPrimal(final LinearSolver.Builder builder) {

        Optimisation.Options options = new Optimisation.Options();

        DenseTableau tableau = builder.newSimplexStore(DenseTableau::new);
        PrimalSimplexSolver tSolver = tableau.newPrimalSimplexSolver(options);
        Result tResult = tSolver.solve();

        RevisedStore revised = builder.newSimplexStore(RevisedStore::new);
        PrimalSimplexSolver rSolver = revised.newPrimalSimplexSolver(options);
        Result rResult = rSolver.solve();

        TestUtils.assertStateAndSolution(tResult, rResult);

        EdgeWeightTest.verifyPrimalEdgeWeights(tableau, revised);
    }

    private static void verifyDualEdgeWeights(final DenseTableau tableau, final RevisedStore revised) {

        boolean weightsChanged = false;

        for (int i = 0; i < tableau.included.length; i++) {

            double tWeight = tableau.edgeWeights[i];
            double rWeight = revised.edgeWeights[i];

            TestUtils.assertTrue(tWeight > 0);
            TestUtils.assertTrue(tWeight < 1e6);

            if (rWeight != 1.0) {
                weightsChanged = true;
            }

            TestUtils.assertEquals(tWeight, rWeight);
        }

        TestUtils.assertTrue(weightsChanged);
    }

    private static void verifyPrimalEdgeWeights(final DenseTableau tableau, final RevisedStore revised) {

        boolean weightsChanged = false;

        for (int je = 0; je < tableau.excluded.length; je++) {

            double tWeight = tableau.edgeWeights[je];
            double rWeight = revised.edgeWeights[je];

            TestUtils.assertTrue(tWeight > 0);
            TestUtils.assertTrue(tWeight < 1e6);

            if (rWeight != 1.0) {
                weightsChanged = true;
            }

            TestUtils.assertEquals(tWeight, rWeight);
        }

        TestUtils.assertTrue(weightsChanged);
    }

    @Test
    public void testComplex4x4NonOrderedBasis() {

        // This problem is designed to force the simplex algorithm to perform pivots
        // that result in non-increasing order in the included/excluded arrays
        LinearSolver.Builder builder = LinearSolver.newBuilder(-10.0, -8.0, -6.0, -4.0);

        // Constraints designed to create a complex pivot sequence
        builder.inequality(20.0, 3.0, 2.0, 1.0, 1.0);
        builder.inequality(15.0, 1.0, 3.0, 2.0, 1.0);
        builder.inequality(12.0, 2.0, 1.0, 3.0, 1.0);
        builder.inequality(18.0, 1.0, 1.0, 1.0, 3.0);

        // Bounds that will force interesting pivot choices
        builder.lower(0.0, 0.0, 0.0, 0.0);
        builder.upper(10.0, 8.0, 6.0, 4.0);

        EdgeWeightTest.doTestDual(builder);

        EdgeWeightTest.doTestPrimal(builder);
    }

    @Test
    public void testLargeProblemWithArtificials() {

        // A larger problem with artificial variables that should create
        // more complex basis ordering during phase 1 and phase 2
        LinearSolver.Builder builder = LinearSolver.newBuilder(-5.0, -4.0, -3.0, -2.0, -1.0);

        // Mix of equality and inequality constraints to create artificial variables
        builder.equality(10.0, 2.0, 1.0, 1.0, 1.0, 1.0);
        builder.inequality(8.0, 1.0, 2.0, 1.0, 1.0, 1.0);
        builder.inequality(6.0, 1.0, 1.0, 2.0, 1.0, 1.0);
        builder.equality(12.0, 1.0, 1.0, 1.0, 2.0, 1.0);
        builder.inequality(9.0, 1.0, 1.0, 1.0, 1.0, 2.0);

        // Bounds that will force multiple pivots
        builder.lower(0.0, 0.0, 0.0, 0.0, 0.0);
        builder.upper(5.0, 4.0, 3.0, 2.0, 1.0);

        EdgeWeightTest.doTestDual(builder);

        EdgeWeightTest.doTestPrimal(builder);
    }

    @Test
    public void testSimple2x2() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-1.0, -1.0);
        builder.inequality(2.0, 1.0, 1.0);
        builder.inequality(3.0, 1.0, 2.0);

        builder.lower(1.0, 0.0);
        builder.upper(2.0, 1.0);

        EdgeWeightTest.doTestDual(builder);

        EdgeWeightTest.doTestPrimal(builder);
    }

    @Test
    public void testSimple3x3() {

        LinearSolver.Builder builder = LinearSolver.newBuilder(-3.0, -2.0, -1.0);
        builder.inequality(10.0, 2.0, 1.0, 1.0);
        builder.inequality(8.0, 1.0, 2.0, 1.0);
        builder.inequality(7.0, 1.0, 1.0, 2.0);

        builder.lower(4.0, 2.0, -1.0);
        builder.upper(5.0, 3.0, 0.0);

        EdgeWeightTest.doTestDual(builder);

        EdgeWeightTest.doTestPrimal(builder);
    }

}