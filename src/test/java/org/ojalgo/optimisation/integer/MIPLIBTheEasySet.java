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
package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * According to the MIPLIB (https://miplib.zib.de/) definition: ‘Easy’ means that the instance could be solved
 * within less than one hour and with at most 16 threads, using an out-of-the-box solver on standard desktop
 * computing hardware, ‘hard’ stands for instances, that have been solved in longer runs possibly using
 * nonstandard hardware and/or algorithms, whereas ‘open’ means, that the instance has not yet been reported
 * solved.
 * <p>
 * In addition to being labelled 'easy' the test cases here have no more than 100 variables (any kind) and
 * actually have a solution (no infeasible models).
 * <p>
 * The tag 'slow' means getting a solution takes to long. The tag 'unstable' means the returned solution is
 * not optimal (state OPTIMAL and correct value).
 *
 * @author apete
 */
public class MIPLIBTheEasySet extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static void doTest(final String modelName, final String expMinValString, final String expMaxValString) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", modelName, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.validate = false;
        // model.options.progress(IntegerSolver.class);
        // model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(NumberContext.of(3)));
        // model.options.integer(SIMPLE_FIFO_STRATEGY);
        // model.options.iterations_abort = 10;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, ACCURACY);
    }

    /**
     * Sports timetabling (basketball) instance: assign games to rounds with home/away patterns,
     * rest/fairness, and conflict constraints. Pure 0–1 set-partitioning with heavy symmetry and many mutual
     * exclusions. Small “easy” variant here (≤100 vars). Solvers benefit from strong presolve (fixings,
     * clique/cover), symmetry-aware branching, and good primal heuristics.
     * <p>
     * https://miplib.zib.de/instance_details_b-ball.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 300s suffice with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 300s suffice with optimal solution (actually found after less than 5s)
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈0.01s
     * <li>2025-08-18: 300s suffice with optimal solution
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testB_ball() {
        MIPLIBTheEasySet.doTest("b-ball.mps", "-1.5", null);
    }

    /**
     * Small 0–1 production/assignment model with fixed-charge style linking. Weak LP relaxation and many
     * near-symmetric choices stress presolve (fixings, tightening), knapsack cover/clique cuts, and primal
     * heuristics to get an incumbent quickly.
     * <p>
     * https://miplib.zib.de/instance_details_ej.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈600s
     * <li>2025-08-18: 300s suffice with feasible solution, but 40178
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testEj() {
        MIPLIBTheEasySet.doTest("ej.mps", "25508", null);
    }

    /**
     * Airline flight/crew planning prototype: assign flights to periods/resources while respecting
     * connection, turnaround/maintenance, and capacity rules. Mostly set- partitioning/packing with side
     * constraints; solvers should exploit strong presolve, clique/cover cuts, and branching on packing
     * structures.
     * <p>
     * https://miplib.zib.de/instance_details_flugpl.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 1s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 0s finished with optimal solution
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈0.01s
     * <li>2025-08-18: ≈0.06s
     * </ul>
     */
    @Test
    public void testFlugpl() {
        MIPLIBTheEasySet.doTest("flugpl.mps", "1201500", null);
    }

    /**
     * Generated general integer program with a mix of equality/inequality rows and both binary and
     * general-integer variables. Tests scaling, bound tightening, MIR/GMI cuts, and robust branching on
     * general integers.
     * <p>
     * https://miplib.zib.de/instance_details_gen-ip002.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈110s
     * <li>2025-08-18: ≈280s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testGen_ip002() {
        MIPLIBTheEasySet.doTest("gen-ip002.mps", "-4783.733392", null);
    }

    /**
     * Generated general integer program with dense-ish constraints and mixed signs in costs. Emphasizes
     * presolve (row/column dominance), mixed-integer rounding and Gomory cuts, plus variable selection that
     * handles degeneracy.
     * <p>
     * https://miplib.zib.de/instance_details_gen-ip016.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX takes forever
     * <li>2025-08-18: 300s suffice with feasible solution, but -9458.724...
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testGen_ip016() {
        MIPLIBTheEasySet.doTest("gen-ip016.mps", "-9476.155197", null);
    }

    /**
     * Generated general IP featuring a blend of packing and covering rows with general-integer variables.
     * Useful for exercising integrality detection, implied bound strengthening, and cut separation.
     * <p>
     * https://miplib.zib.de/instance_details_gen-ip021.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈48s
     * <li>2025-08-18: ≈128s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testGen_ip021() {
        MIPLIBTheEasySet.doTest("gen-ip021.mps", "2361.454195", null);
    }

    /**
     * Generated general IP with moderately tight relaxations but several nearly parallel rows, creating
     * degeneracy in the LP. Benefits from scaling, strong branching, and careful cut management to avoid
     * tailing off.
     * <p>
     * https://miplib.zib.de/instance_details_gen-ip036.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈52s
     * <li>2025-08-18: ≈215s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testGen_ip036() {
        MIPLIBTheEasySet.doTest("gen-ip036.mps", "-4606.67961", null);
    }

    /**
     * Generated general IP where a few key constraints drive most fixings. Good for testing probing/presolve,
     * MIR/cover cuts, and branching on inferred structure rather than raw indices.
     * <p>
     * https://miplib.zib.de/instance_details_gen-ip054.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈57s
     * <li>2025-08-18: 300s suffice with feasible solution, but 6848.773...
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testGen_ip054() {
        MIPLIBTheEasySet.doTest("gen-ip054.mps", "6840.965642", null);
    }

    /**
     * Grid/graph placement instance on a 4×6 structure with pairwise conflicts and assignment constraints;
     * effectively a small independent-set/graph-coloring style 0–1 IP. Needs clique lifting, symmetry
     * breaking, and conflict-graph reasoning.
     * <p>
     * https://miplib.zib.de/instance_details_gr4x6.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 0s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 0s finished with optimal solution
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈0.05s
     * <li>2025-08-18: ≈0.03s
     * </ul>
     */
    @Test
    public void testGr4x6() {
        MIPLIBTheEasySet.doTest("gr4x6.mps", "202.35", null);
    }

    /**
     * Market-share subset selection: pick a subset whose aggregate attributes match exact targets. Pure 0–1
     * equalities with extreme symmetry; stresses knapsack cover cuts, extended probing/fixings, and branching
     * on counts or aggregates.
     * <p>
     * https://miplib.zib.de/instance_details_markshare_4_0.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 15s finished with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 11s finished with optimal solution
     * <li>2022-04-18: 22s finished with optimal solution
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈1s
     * <li>2025-08-18: ≈23s
     * </ul>
     */
    @Test
    public void testMarkshare_4_0() {
        MIPLIBTheEasySet.doTest("markshare_4_0.mps", "1", null);
    }

    /**
     * A tighter market-share variant with additional attributes/targets. Even more symmetric and
     * knapsack-like; requires strong presolve, parity reasoning, and effective node selection to close the
     * gap quickly.
     * <p>
     * https://miplib.zib.de/instance_details_markshare_5_0.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈2s
     * <li>2025-08-18: 300s suffice with feasible solution, but 4
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testMarkshare_5_0() {
        MIPLIBTheEasySet.doTest("markshare_5_0.mps", "1", null);
    }

    /**
     * Original market-share prototype instance. Small, highly structured pseudo- Boolean equalities emphasize
     * exact-sum feasibility over complex objectives; good for testing 0–1 inference, covers, and symmetry
     * handling.
     * <p>
     * https://miplib.zib.de/instance_details_markshare1.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX takes long time
     * <li>2025-08-18: 300s suffice with feasible solution, but 9
     * </ul>
     */
    @Tag("slow")
    @Tag("unstable")
    @Test
    public void testMarkshare1() {
        MIPLIBTheEasySet.doTest("markshare1.mps", "1", null);
    }

    /**
     * Small but pathological mixed-binary: many easy incumbents yet a stubborn dual bound; highly degenerate
     * LP relaxation. Benefits from strong branching, conflict analysis/learning, local branching, and
     * disciplined cut management. https://miplib.zib.de/instance_details_neos5.html
     * <p>
     * 2 or 3 integer solutions, including the optimal solution, is found immediately. Still the branching
     * goes on "forever", and in addition the generated LP branch-problems get harder and harder to solve for
     * the LP solver.
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2019-01-28: 300s suffice with optimal solution
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: 300s suffice with optimal solution found (after just a few seconds)
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈6s
     * <li>2025-08-18: 300s suffice with optimal solution
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testNeos5() {
        MIPLIBTheEasySet.doTest("neos5.mps", "15", null);
    }

    /**
     * Packing/covering instance with multiple overlapping knapsacks and very tight constraints. Incumbents
     * appear quickly, but proving optimality takes deep search plus cover/flow/MIR cuts and aggressive node
     * presolve.
     * <p>
     * https://miplib.zib.de/instance_details_pk1.html
     * <p>
     * Mac Pro (Early 2009)
     * <ul>
     * <li>2013-04-01: (suffice=4h abort=8h) Stopped with optimal integer solution after 1h50min
     * <li>2013-12-08: (suffice=4h abort=8h) Stopped with optimal integer solution after 412s
     * <li>2015-11-07: (suffice=4h abort=8h) Stopped with optimal integer solution after 372s
     * <li>2017-10-20: (suffice=4h abort=8h) Stopped with optimal integer solution after 796s
     * <li>2017-10-20: (suffice=5min abort=1h) Stopped with optimal integer solution after 5min
     * <li>2018-02-07: (suffice=5min, abort=15min, mip_gap=0.001) Suffice with optimal solution
     * <li>2018-02-07: (suffice=15min, abort=15min, mip_gap=0.001) Found optimal solution in 344s
     * <li>2018-04-47: (suffice=5min, abort=15min, mip_gap=0.001) Found optimal solution in 227s
     * <li>2018-08-16: sufficed: <11.0> but was: <14.0>
     * <li>2019-01-28: 300s expected: <11.0> but was: <11.999999999999979>
     * </ul>
     * MacBook Pro (16-inch, 2019)
     * <ul>
     * <li>2022-02-11: Finished with optimal solution in 251s. (optimal solution found after about 150s)
     * <li>2022-03-17: Finished with optimal solution in 159s.
     * </ul>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: CPLEX ≈4s
     * <li>2025-08-18: ≈33s
     * </ul>
     */
    @Test
    public void testPk1() {
        MIPLIBTheEasySet.doTest("pk1.mps", "11", null);
    }

    /**
     * Industrial selection/scheduling instance with precedence and resource limits; contains indicator-style
     * implications modeled via big-M constraints. Requires numerically safe M values, bound strengthening,
     * and effective probing/presolve.
     * <p>
     * https://miplib.zib.de/instance_details_supportcase21i.html
     * <p>
     * MacBook Air (15-inch, M2, 2023)
     * <ul>
     * <li>2025-08-18: Can't parse the MPS file
     * </ul>
     */
    @Test
    @Disabled("Can't parse the MPS file")
    public void testSupportcase21i() {
        MIPLIBTheEasySet.doTest("supportcase21i.mps", "20", null);
    }

}