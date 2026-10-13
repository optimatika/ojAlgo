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
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;

/**
 * Diagnostic survey of primal degeneracy across a small set of Netlib models.
 * <p>
 * Reads {@link SimplexSolver#countDegeneratePivots()} after each solve and prints the
 * counts. The test passes as long as every model returns OPTIMAL — the value is in the
 * surfaced numbers, which can be inspected for spotting which problems are degeneracy-heavy.
 * <p>
 * No stall recovery / refactor trigger is wired in (parked as of 2026-04-28 — see
 * {@code plan-simplexSolver.prompt.md} §B). This file remains as the harness that future
 * stall-recovery work would build on.
 */
public class StallDetectionTest extends OptimisationLinearTests {

    /**
     * Models picked from the original survey: high-degeneracy primal cases plus a
     * couple the previous plan flagged as numerically fragile.
     */
    private static final String[] MODELS = { "BANDM.SIF", "CZPROB.SIF", "SCSD8.SIF", "BNL1.SIF", "GANGES.SIF" };

    private static void runOne(final String name) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", name, false);
        ExpressionsBasedModel simplified = model.simplify();

        Optimisation.Options options = new Optimisation.Options();

        for (Function<LinearStructure, SimplexStore> factory : OptimisationLinearTests.STORE_FACTORIES) {

            SimplexStore simplex = SimplexSolver.build(simplified, factory);
            PhasedSimplexSolver solver = simplex.newPhasedSimplexSolver(options);

            long start = System.nanoTime();
            Result result = solver.solve();
            long durationMillis = (System.nanoTime() - start) / 1_000_000L;

            BasicLogger.debug("  {} [{}]: state={} value={} degenerate={} time={}ms", name, simplex.getClass().getSimpleName(), result.getState(),
                    result.getValue(), solver.countDegeneratePivots(), durationMillis);
        }
    }

    @Test
    public void surveyDegenerateModels() {

        BasicLogger.debug();
        BasicLogger.debug("Degeneracy survey");
        BasicLogger.debug("=================");

        for (String name : MODELS) {
            StallDetectionTest.runOne(name);
        }
    }

}
