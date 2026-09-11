/*
 * Copyright 1997-2026 Optimatika
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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * A set of models from MIPLIB (including older releases of that test suite). The models included here have no
 * more than 1k variables or constraints. In addition it is limited to the set of models that SCIP, HiGHS and
 * the top commercial solvers ALL can solve. Something for ojAlgo to aim for...
 * <p>
 * The definition of which models to include is not entirely deterministic. Any update to either of the
 * solvers or the benchmark code may change the outcome. Currently there are 103 models in this set.
 * <p>
 * The tag 'slow' means getting a response takes too long (regardless of what the response is). The tag
 * 'unstable' means there is (sometimes) a problem with the returned solution (possibly that we've never seen
 * one).
 * <p>
 * The purpose of this test class is to make as many as possible of the MIPLIB (easy set) models pass with the
 * default settings. There are cases where a known small configuration change makes the test pass – that's not
 * relevant here.
 *
 * @author apete
 */
public class MIPLIBTheEasySet extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = IntegerStrategy.DEFAULT.getGapTolerance();

    private static void doTest(final String modelName, final String expMinValString, final String expMaxValString) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", modelName, false);

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
     * https://miplib.zib.de/instance_details_22433.html
     * <p>
     * 429 variables, 199 expressions, density 0.0023
     * <p>
     * HiGHS 2.0s, SCIP 769ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.2s
     * </ul>
     */
    @Test
    public void test_22433() {
        MIPLIBTheEasySet.doTest("22433.mps", "21477", null);
    }

    /**
     * https://miplib.zib.de/instance_details_23588.html
     * <p>
     * 368 variables, 138 expressions, density 0.0027
     * <p>
     * HiGHS 4.1s, SCIP 1.5s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.3s
     * </ul>
     */
    @Test
    public void test_23588() {
        MIPLIBTheEasySet.doTest("23588.mps", "8090", null);
    }

    /**
     * https://miplib.zib.de/instance_details_aflow30a.html
     * <p>
     * 842 variables, 480 expressions, density 0.50
     * <p>
     * HiGHS 5.5s, SCIP 14.9s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testAflow30a() {
        MIPLIBTheEasySet.doTest("aflow30a.mps", "1158", null);
    }

    /**
     * https://miplib.zib.de/instance_details_air01.html
     * <p>
     * 771 variables, 24 expressions, density 1.0
     * <p>
     * HiGHS 53ms, SCIP 37ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.02s
     * </ul>
     */
    @Test
    public void testAir01() {
        MIPLIBTheEasySet.doTest("air01.mps", "6796", null);
    }

    /**
     * https://miplib.zib.de/instance_details_beavma.html
     * <p>
     * 390 variables, 373 expressions, density 0.88
     * <p>
     * HiGHS 70ms, SCIP 241ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testBeavma() {
        MIPLIBTheEasySet.doTest("beavma.mps", "383285", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bell3a.html
     * <p>
     * 133 variables, 124 expressions, density 0.71
     * <p>
     * HiGHS 227ms, SCIP 588ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 1s
     * </ul>
     */
    @Test
    public void testBell3a() {
        MIPLIBTheEasySet.doTest("bell3a.mps", "878430.32", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bell3b.html
     * <p>
     * 133 variables, 124 expressions, density 0.71
     * <p>
     * HiGHS 314ms, SCIP 1.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 0.3s (objective only correct to 5 digits)
     * <li>2026-09-04: 2-7s with an 8-digit gap tolerance (the default 5 digits stops within ~1e-4 of the
     * optimum, which is what made this test unstable)
     * <li>2026-09-07: default configuration again (this class mirrors the benchmark, where every solver runs
     * with its defaults). With the default gap the search legitimately stops on an incumbent within 0.007% of
     * the optimum (a +-1 shift on two general integers away from it) in about half the runs. The assertion
     * accuracy now matches the gap tolerance, so any incumbent within the band passes.
     * </ul>
     */
    @Test
    public void testBell3b() {
        MIPLIBTheEasySet.doTest("bell3b.mps", "11786160.62", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bell4.html
     * <p>
     * 117 variables, 106 expressions, density 0.71
     * <p>
     * HiGHS 858ms, SCIP 393ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: OutOfMemoryError
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testBell4() {
        MIPLIBTheEasySet.doTest("bell4.mps", "18541484.20", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bell5.html
     * <p>
     * 104 variables, 92 expressions, density 0.71
     * <p>
     * HiGHS 674ms, SCIP 143ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 21s
     * <li>2026-08-21: 16s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testBell5() {
        MIPLIBTheEasySet.doTest("bell5.mps", "8966406.49", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bienst1.html
     * <p>
     * 505 variables, 577 expressions, density 0.0020
     * <p>
     * HiGHS 17.6s, SCIP 78.4s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 37s
     * <li>2026-08-21: 26s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testBienst1() {
        MIPLIBTheEasySet.doTest("bienst1.mps", "46.75", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bienst2.html
     * <p>
     * 505 variables, 577 expressions, density 0.0020
     * <p>
     * HiGHS 281s, SCIP 142s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testBienst2() {
        MIPLIBTheEasySet.doTest("bienst2.mps", "54.6", null);
    }

    /**
     * https://miplib.zib.de/instance_details_blend2.html
     * <p>
     * 353 variables, 275 expressions, density 0.25
     * <p>
     * HiGHS 1.7s, SCIP 689ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 2s
     * <li>2026-09-05: wrong answer (7.692983) about once in 100 solves, only under CPU load: a warm node LP
     * ended primal feasible but marginally not dual feasible, was reported FEASIBLE, and the node holding the
     * optimum was pruned as infeasible. The warm path now finishes with primal iterations, and a node that is
     * neither optimal nor infeasible aborts the search instead of being pruned. Reproduced by looping the
     * model with a dozen busy threads in the same JVM.
     * </ul>
     */
    @Test
    public void testBlend2() {
        MIPLIBTheEasySet.doTest("blend2.mps", "7.598985", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bm23.html
     * <p>
     * 27 variables, 21 expressions, density 1.0
     * <p>
     * HiGHS 61ms, SCIP 150ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testBm23() {
        MIPLIBTheEasySet.doTest("bm23.mps", "34", null);
    }

    /**
     * https://miplib.zib.de/instance_details_bppc8-02.html
     * <p>
     * 232 variables, 60 expressions, density 0.0086
     * <p>
     * HiGHS 157ms, SCIP 154ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 0.4s
     * </ul>
     */
    @Test
    public void testBppc8_02() {
        MIPLIBTheEasySet.doTest("bppc8-02.mps", "507", null);
    }

    /**
     * https://miplib.zib.de/instance_details_cracpb1.html
     * <p>
     * 572 variables, 144 expressions, density 1.0
     * <p>
     * HiGHS 29ms, SCIP 69ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testCracpb1() {
        MIPLIBTheEasySet.doTest("cracpb1.mps", "22199", null);
    }

    /**
     * https://miplib.zib.de/instance_details_dcmulti.html
     * <p>
     * 548 variables, 291 expressions, density 0.95
     * <p>
     * HiGHS 3.1s, SCIP 850ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 1s
     * <li>2026-09-06: 188188.9 seen once in a suite run, 188183.5 once in 30 solves. The objective has
     * continuous variables, so every incumbent within the default relative gap (18.8 here) is a legitimate
     * stop, and which one comes first depends on thread timing. The assertion accuracy now matches the gap
     * tolerance, so any incumbent within the band passes.
     * </ul>
     */
    @Test
    public void testDcmulti() {
        MIPLIBTheEasySet.doTest("dcmulti.mps", "188182", null);
    }

    /**
     * https://miplib.zib.de/instance_details_dfn-gwin-UUM.html
     * <p>
     * 938 variables, 159 expressions, density 0.10
     * <p>
     * HiGHS 144s, SCIP 36.4s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testDfn_gwin_UUM() {
        MIPLIBTheEasySet.doTest("dfn-gwin-UUM.mps", "38752", null);
    }

    /**
     * https://miplib.zib.de/instance_details_egout.html
     * <p>
     * 141 variables, 99 expressions, density 0.78
     * <p>
     * HiGHS 11ms, SCIP 2.8ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 1s
     * </ul>
     */
    @Test
    public void testEgout() {
        MIPLIBTheEasySet.doTest("egout.mps", "568.101", null);
    }

    /**
     * https://miplib.zib.de/instance_details_enigma.html
     * <p>
     * 100 variables, 22 expressions, density 0.090
     * <p>
     * HiGHS 118ms, SCIP 116ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testEnigma() {
        MIPLIBTheEasySet.doTest("enigma.mps", "0.0", null);
    }

    /**
     * https://miplib.zib.de/instance_details_enlight_hard.html
     * <p>
     * 200 variables, 101 expressions, density 0.50
     * <p>
     * HiGHS 3.8s, SCIP 2.0ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 21s
     * <li>2026-08-21: 15s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testEnlight_hard() {
        MIPLIBTheEasySet.doTest("enlight_hard.mps", "37", null);
    }

    /**
     * https://miplib.zib.de/instance_details_enlight13.html
     * <p>
     * 338 variables, 170 expressions, density 0.50
     * <p>
     * HiGHS 127s, SCIP 5ms
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testEnlight13() {
        MIPLIBTheEasySet.doTest("enlight13.mps", "71", null);
    }

    /**
     * https://miplib.zib.de/instance_details_enlight8.html
     * <p>
     * 128 variables, 65 expressions, density 0.50
     * <p>
     * HiGHS 1.0s, SCIP 2.2ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 0.5s
     * </ul>
     */
    @Test
    public void testEnlight8() {
        MIPLIBTheEasySet.doTest("enlight8.mps", "27", null);
    }

    /**
     * https://miplib.zib.de/instance_details_exp-1-500-5-5.html
     * <p>
     * 990 variables, 551 expressions, density 1.0
     * <p>
     * HiGHS 1.7s, SCIP 1.0s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 603s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testExp_1_500_5_5() {
        MIPLIBTheEasySet.doTest("exp-1-500-5-5.mps", "65887", null);
    }

    /**
     * https://miplib.zib.de/instance_details_f2gap40400.html
     * <p>
     * 400 variables, 41 expressions, density 0.99
     * <p>
     * HiGHS 6.8ms, SCIP 26ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testF2gap40400() {
        MIPLIBTheEasySet.doTest("f2gap40400.mps", "20772", null);
    }

    /**
     * https://miplib.zib.de/instance_details_fixnet3.html
     * <p>
     * 878 variables, 479 expressions, density 1.00
     * <p>
     * HiGHS 47ms, SCIP 18ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testFixnet3() {
        MIPLIBTheEasySet.doTest("fixnet3.mps", "51973", null);
    }

    /**
     * https://miplib.zib.de/instance_details_fixnet4.html
     * <p>
     * 878 variables, 479 expressions, density 0.99
     * <p>
     * HiGHS 182ms, SCIP 749ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testFixnet4() {
        MIPLIBTheEasySet.doTest("fixnet4.mps", "8936", null);
    }

    /**
     * https://miplib.zib.de/instance_details_fixnet6.html
     * <p>
     * 878 variables, 479 expressions, density 0.90
     * <p>
     * HiGHS 1.5s, SCIP 5.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testFixnet6() {
        MIPLIBTheEasySet.doTest("fixnet6.mps", "3983", null);
    }

    /**
     * https://miplib.zib.de/instance_details_flugpl.html
     * <p>
     * 18 variables, 19 expressions, density 1.0
     * <p>
     * HiGHS 42ms, SCIP 77ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.2s
     * <li>2026-08-21: 0.02s
     * <li>2026-09-05: wrong answer (1202100) about once in 500 solves: node bounds overwrote a variable that
     * presolve had fixed from an equality row already marked redundant, so the node LP lacked that row and
     * its "optimal" point was discarded by validation. Node bounds now only ever tighten (NodeKey).
     * </ul>
     */
    @Test
    public void testFlugpl() {
        MIPLIBTheEasySet.doTest("flugpl.mps", "1201500", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen.html
     * <p>
     * 870 variables, 781 expressions, density 0.67
     * <p>
     * HiGHS 12ms, SCIP 11ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 4s
     * <li>2026-08-21: 7s
     * </ul>
     */
    @Test
    public void testGen() {
        MIPLIBTheEasySet.doTest("gen.mps", "112313.362718", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip021.html
     * <p>
     * 35 variables, 29 expressions, density 1.0
     * <p>
     * HiGHS 263s, SCIP 158s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testGen_ip021() {
        MIPLIBTheEasySet.doTest("gen-ip021.mps", "2361.4541951916", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip036.html
     * <p>
     * 29 variables, 47 expressions, density 1.0
     * <p>
     * HiGHS 281s, SCIP 145s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testGen_ip036() {
        MIPLIBTheEasySet.doTest("gen-ip036.mps", "-4606.6796098376", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gr4x6.html
     * <p>
     * 48 variables, 35 expressions, density 1.0
     * <p>
     * HiGHS 7.2ms, SCIP 3.1ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testGr4x6() {
        MIPLIBTheEasySet.doTest("gr4x6.mps", "202.35", null);
    }

    /**
     * https://miplib.zib.de/instance_details_graphdraw-gemcutter.html
     * <p>
     * 166 variables, 475 expressions, density 0.23
     * <p>
     * HiGHS 163s, SCIP 188s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 366s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testGraphdraw_gemcutter() {
        MIPLIBTheEasySet.doTest("graphdraw-gemcutter.mps", "7118.5", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gt2.html
     * <p>
     * 188 variables, 30 expressions, density 0.49
     * <p>
     * HiGHS 28ms, SCIP 19ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testGt2() {
        MIPLIBTheEasySet.doTest("gt2.mps", "21166", null);
    }

    /**
     * https://miplib.zib.de/instance_details_ic97_tension.html
     * <p>
     * 703 variables, 320 expressions, density 0.28
     * <p>
     * HiGHS 13.2s, SCIP 8.1s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 363s
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testIc97_tension() {
        MIPLIBTheEasySet.doTest("ic97_tension.mps", "3942", null);
    }

    /**
     * https://miplib.zib.de/instance_details_lseu.html
     * <p>
     * 89 variables, 29 expressions, density 0.96
     * <p>
     * HiGHS 163ms, SCIP 417ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testLseu() {
        MIPLIBTheEasySet.doTest("lseu.mps", "1120", null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_4_0.html
     * <p>
     * 34 variables, 5 expressions, density 0.12
     * <p>
     * HiGHS 80s, SCIP 42.2s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMarkshare_4_0() {
        MIPLIBTheEasySet.doTest("markshare_4_0.mps", "1", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mas76.html
     * <p>
     * 151 variables, 13 expressions, density 1.0
     * <p>
     * HiGHS 225s, SCIP 64.9s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 4s
     * <li>2026-08-21: 2s
     * </ul>
     */
    @Test
    public void testMas76() {
        MIPLIBTheEasySet.doTest("mas76.mps", "40005.054", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-1-100-1.html
     * <p>
     * 251 variables, 152 expressions, density 1.0
     * <p>
     * HiGHS 6.5s, SCIP 45.9s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 454s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_1_100_1() {
        MIPLIBTheEasySet.doTest("mik-250-1-100-1.mps", "-66729", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-20-75-1.html
     * <p>
     * 270 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 3.0s, SCIP 3.1s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_20_75_1() {
        MIPLIBTheEasySet.doTest("mik-250-20-75-1.mps", "-49716", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-20-75-2.html
     * <p>
     * 270 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 3.2s, SCIP 3.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_20_75_2() {
        MIPLIBTheEasySet.doTest("mik-250-20-75-2.mps", "-50768", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-20-75-3.html
     * <p>
     * 270 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 5.1s, SCIP 7.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_20_75_3() {
        MIPLIBTheEasySet.doTest("mik-250-20-75-3.mps", "-52242", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-20-75-4.html
     * <p>
     * 270 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 27.5s, SCIP 8.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_20_75_4() {
        MIPLIBTheEasySet.doTest("mik-250-20-75-4.mps", "-52301", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mik-250-20-75-5.html
     * <p>
     * 270 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 2.1s, SCIP 2.2s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 603s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testMik_250_20_75_5() {
        MIPLIBTheEasySet.doTest("mik-250-20-75-5.mps", "-51532", null);
    }

    /**
     * https://miplib.zib.de/instance_details_misc01.html
     * <p>
     * 83 variables, 55 expressions, density 0.012
     * <p>
     * HiGHS 118ms, SCIP 271ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testMisc01() {
        MIPLIBTheEasySet.doTest("misc01.mps", "563.5", null);
    }

    /**
     * https://miplib.zib.de/instance_details_misc02.html
     * <p>
     * 59 variables, 40 expressions, density 0.017
     * <p>
     * HiGHS 32ms, SCIP 115ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testMisc02() {
        MIPLIBTheEasySet.doTest("misc02.mps", "1690", null);
    }

    /**
     * https://miplib.zib.de/instance_details_misc03.html
     * <p>
     * 160 variables, 97 expressions, density 0.0063
     * <p>
     * HiGHS 389ms, SCIP 705ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testMisc03() {
        MIPLIBTheEasySet.doTest("misc03.mps", "3360", null);
    }

    /**
     * https://miplib.zib.de/instance_details_misc05.html
     * <p>
     * 136 variables, 301 expressions, density 0.0074
     * <p>
     * HiGHS 438ms, SCIP 968ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 1s
     * </ul>
     */
    @Test
    public void testMisc05() {
        MIPLIBTheEasySet.doTest("misc05.mps", "2984.5", null);
    }

    /**
     * https://miplib.zib.de/instance_details_misc07.html
     * <p>
     * 260 variables, 213 expressions, density 0.0038
     * <p>
     * HiGHS 23.3s, SCIP 75.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 6s
     * <li>2026-08-21: 3s
     * </ul>
     */
    @Test
    public void testMisc07() {
        MIPLIBTheEasySet.doTest("misc07.mps", "2810", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mod008.html
     * <p>
     * 319 variables, 7 expressions, density 1.0
     * <p>
     * HiGHS 1.3s, SCIP 539ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testMod008() {
        MIPLIBTheEasySet.doTest("mod008.mps", "307", null);
    }

    /**
     * https://miplib.zib.de/instance_details_mod013.html
     * <p>
     * 96 variables, 63 expressions, density 1.0
     * <p>
     * HiGHS 420ms, SCIP 492ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.02s
     * </ul>
     */
    @Test
    public void testMod013() {
        MIPLIBTheEasySet.doTest("mod013.mps", "280.95", null);
    }

    /**
     * https://miplib.zib.de/instance_details_modglob.html
     * <p>
     * 422 variables, 292 expressions, density 1.0
     * <p>
     * HiGHS 404ms, SCIP 118ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testModglob() {
        MIPLIBTheEasySet.doTest("modglob.mps", "20740508", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-1425699.html
     * <p>
     * 105 variables, 90 expressions, density 0.74
     * <p>
     * HiGHS 73ms, SCIP 2.9ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testNeos_1425699() {
        MIPLIBTheEasySet.doTest("neos-1425699.mps", "3179698977", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-1430701.html
     * <p>
     * 312 variables, 669 expressions, density 0.50
     * <p>
     * HiGHS 5.1s, SCIP 2.0s
     */
    @Tag("slow")
    @Test
    public void testNeos_1430701() {
        MIPLIBTheEasySet.doTest("neos-1430701.mps", "-77", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-2624317-amur.html
     * <p>
     * 524 variables, 343 expressions, density 0.061
     * <p>
     * HiGHS 7.4s, SCIP 58.6s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 26min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNeos_2624317_amur() {
        MIPLIBTheEasySet.doTest("neos-2624317-amur.mps", "3.5223968", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-3610040-iskar.html
     * <p>
     * 430 variables, 336 expressions, density 0.0023
     * <p>
     * HiGHS 801ms, SCIP 1.0s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 149s
     * <li>2026-08-21: 198s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testNeos_3610040_iskar() {
        MIPLIBTheEasySet.doTest("neos-3610040-iskar.mps", "37", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-3610051-istra.html
     * <p>
     * 805 variables, 710 expressions, density 0.0012
     * <p>
     * HiGHS 4.6s, SCIP 722ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNeos_3610051_istra() {
        MIPLIBTheEasySet.doTest("neos-3610051-istra.mps", "49", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-3610173-itata.html
     * <p>
     * 844 variables, 748 expressions, density 0.0012
     * <p>
     * HiGHS 1.7s, SCIP 458ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 297s
     * <li>2026-08-21: 341s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testNeos_3610173_itata() {
        MIPLIBTheEasySet.doTest("neos-3610173-itata.mps", "148", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-3611447-jijia.html
     * <p>
     * 472 variables, 378 expressions, density 0.0021
     * <p>
     * HiGHS 840ms, SCIP 1.2s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 72s
     * <li>2026-08-21: 48s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testNeos_3611447_jijia() {
        MIPLIBTheEasySet.doTest("neos-3611447-jijia.mps", "107", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-3611689-kaihu.html
     * <p>
     * 421 variables, 324 expressions, density 0.0024
     * <p>
     * HiGHS 1.2s, SCIP 725ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 121s
     * <li>2026-08-21: 57s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testNeos_3611689_kaihu() {
        MIPLIBTheEasySet.doTest("neos-3611689-kaihu.mps", "119", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-5192052-neckar.html
     * <p>
     * 180 variables, 58 expressions, density 1.0
     * <p>
     * HiGHS 25ms, SCIP 7.5ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testNeos_5192052_neckar() {
        MIPLIBTheEasySet.doTest("neos-5192052-neckar.mps", "-11670000", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-911880.html
     * <p>
     * 888 variables, 84 expressions, density 0.054
     * <p>
     * HiGHS 8.8s, SCIP 11.2s
     */
    @Tag("slow")
    @Test
    public void testNeos_911880() {
        MIPLIBTheEasySet.doTest("neos-911880.mps", "54.76", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos-911970.html
     * <p>
     * 888 variables, 108 expressions, density 0.054
     * <p>
     * HiGHS 3.9s, SCIP 16.0s
     */
    @Tag("slow")
    @Test
    public void testNeos_911970() {
        MIPLIBTheEasySet.doTest("neos-911970.mps", "54.76", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos17.html
     * <p>
     * 535 variables, 487 expressions, density 0.91
     * <p>
     * HiGHS 3.7s, SCIP 5.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNeos17() {
        MIPLIBTheEasySet.doTest("neos17.mps", "0.15", null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos5.html
     * <p>
     * 63 variables, 64 expressions, density 1.0
     * <p>
     * HiGHS 59.0s, SCIP 154s
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNeos5() {
        MIPLIBTheEasySet.doTest("neos5.mps", "15", null);
    }

    /**
     * https://miplib.zib.de/instance_details_nexp-50-20-1-1.html
     * <p>
     * 490 variables, 541 expressions, density 0.50
     * <p>
     * HiGHS 27ms, SCIP 132ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNexp_50_20_1_1() {
        MIPLIBTheEasySet.doTest("nexp-50-20-1-1.mps", "29", null);
    }

    /**
     * https://miplib.zib.de/instance_details_noswot.html
     * <p>
     * 128 variables, 183 expressions, density 0.20
     * <p>
     * HiGHS 39.7s, SCIP 73.6s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testNoswot() {
        MIPLIBTheEasySet.doTest("noswot.mps", "-41", null);
    }

    /**
     * https://miplib.zib.de/instance_details_opt1217.html
     * <p>
     * 769 variables, 65 expressions, density 0.0013
     * <p>
     * HiGHS 75ms, SCIP 167ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 135s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testOpt1217() {
        MIPLIBTheEasySet.doTest("opt1217.mps", "-16", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0033.html
     * <p>
     * 33 variables, 17 expressions, density 1.0
     * <p>
     * HiGHS 6.3ms, SCIP 6.1ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testP0033() {
        MIPLIBTheEasySet.doTest("p0033.mps", "3089", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0040.html
     * <p>
     * 40 variables, 24 expressions, density 1.0
     * <p>
     * HiGHS 2.8ms, SCIP 1.3ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testP0040() {
        MIPLIBTheEasySet.doTest("p0040.mps", "62027", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0201.html
     * <p>
     * 201 variables, 134 expressions, density 1.0
     * <p>
     * HiGHS 494ms, SCIP 416ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testP0201() {
        MIPLIBTheEasySet.doTest("p0201.mps", "7615", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0282.html
     * <p>
     * 282 variables, 242 expressions, density 1.0
     * <p>
     * HiGHS 82ms, SCIP 160ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testP0282() {
        MIPLIBTheEasySet.doTest("p0282.mps", "258411", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0291.html
     * <p>
     * 291 variables, 253 expressions, density 1.00
     * <p>
     * HiGHS 14ms, SCIP 7.2ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.03s
     * </ul>
     */
    @Test
    public void testP0291() {
        MIPLIBTheEasySet.doTest("p0291.mps", "5223.7490", null);
    }

    /**
     * https://miplib.zib.de/instance_details_p0548.html
     * <p>
     * 548 variables, 177 expressions, density 0.76
     * <p>
     * HiGHS 42ms, SCIP 54ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testP0548() {
        MIPLIBTheEasySet.doTest("p0548.mps", "8691", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pigeon-08.html
     * <p>
     * 344 variables, 602 expressions, density 0.023
     * <p>
     * HiGHS 3.1s, SCIP 16.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 47s
     * <li>2026-08-21: 34s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testPigeon_08() {
        MIPLIBTheEasySet.doTest("pigeon-08.mps", "-7000", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pipex.html
     * <p>
     * 48 variables, 26 expressions, density 1.0
     * <p>
     * HiGHS 64ms, SCIP 47ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.02s
     * </ul>
     */
    @Test
    public void testPipex() {
        MIPLIBTheEasySet.doTest("pipex.mps", "788.263", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pk1.html
     * <p>
     * 86 variables, 46 expressions, density 0.012
     * <p>
     * HiGHS 137s, SCIP 156s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 2s
     * </ul>
     */
    @Test
    public void testPk1() {
        MIPLIBTheEasySet.doTest("pk1.mps", "11", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pp08a.html
     * <p>
     * 240 variables, 137 expressions, density 0.73
     * <p>
     * HiGHS 696ms, SCIP 786ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testPp08a() {
        MIPLIBTheEasySet.doTest("pp08a.mps", "7350.0", null);
    }

    /**
     * https://miplib.zib.de/instance_details_pp08aCUTS.html
     * <p>
     * 240 variables, 247 expressions, density 0.73
     * <p>
     * HiGHS 853ms, SCIP 1.8s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 311s
     * <li>2026-08-21: 301s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testPp08aCUTS() {
        MIPLIBTheEasySet.doTest("pp08aCUTS.mps", "7350.0", null);
    }

    /**
     * https://miplib.zib.de/instance_details_prod1.html
     * <p>
     * 250 variables, 209 expressions, density 0.0040
     * <p>
     * HiGHS 39.9s, SCIP 10.0s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 117s
     * <li>2026-08-21: 23min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testProd1() {
        MIPLIBTheEasySet.doTest("prod1.mps", "-56", null);
    }

    /**
     * https://miplib.zib.de/instance_details_prod2.html
     * <p>
     * 301 variables, 212 expressions, density 0.0033
     * <p>
     * HiGHS 95.8s, SCIP 88.4s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 93min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testProd2() {
        MIPLIBTheEasySet.doTest("prod2.mps", "-62", null);
    }

    /**
     * https://miplib.zib.de/instance_details_r50x360.html
     * <p>
     * 720 variables, 411 expressions, density 1.0
     * <p>
     * HiGHS 26.4s, SCIP 52.6s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testR50x360() {
        MIPLIBTheEasySet.doTest("r50x360.mps", "1653", null);
    }

    /**
     * https://miplib.zib.de/instance_details_ran12x21.html
     * <p>
     * 504 variables, 286 expressions, density 1.0
     * <p>
     * HiGHS 10.9s, SCIP 23.4s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testRan12x21() {
        MIPLIBTheEasySet.doTest("ran12x21.mps", "3664", null);
    }

    /**
     * https://miplib.zib.de/instance_details_ran13x13.html
     * <p>
     * 338 variables, 196 expressions, density 1.0
     * <p>
     * HiGHS 13.2s, SCIP 20.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 143s
     * <li>2026-08-21: 142s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testRan13x13() {
        MIPLIBTheEasySet.doTest("ran13x13.mps", "3252", null);
    }

    /**
     * https://miplib.zib.de/instance_details_ran16x16.html
     * <p>
     * 512 variables, 289 expressions, density 1.0
     * <p>
     * HiGHS 50.5s, SCIP 64.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testRan16x16() {
        MIPLIBTheEasySet.doTest("ran16x16.mps", "3823", null);
    }

    /**
     * https://miplib.zib.de/instance_details_rgn.html
     * <p>
     * 180 variables, 25 expressions, density 0.44
     * <p>
     * HiGHS 126ms, SCIP 104ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testRgn() {
        MIPLIBTheEasySet.doTest("rgn.mps", "82.2", null);
    }

    /**
     * https://miplib.zib.de/instance_details_rout.html
     * <p>
     * 556 variables, 292 expressions, density 0.0018
     * <p>
     * HiGHS 63.8s, SCIP 21.6s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testRout() {
        MIPLIBTheEasySet.doTest("rout.mps", "1077.56", null);
    }

    /**
     * https://miplib.zib.de/instance_details_sample2.html
     * <p>
     * 67 variables, 46 expressions, density 0.49
     * <p>
     * HiGHS 13ms, SCIP 2.1ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testSample2() {
        MIPLIBTheEasySet.doTest("sample2.mps", "375", null);
    }

    /**
     * https://miplib.zib.de/instance_details_sentoy.html
     * <p>
     * 60 variables, 31 expressions, density 1.0
     * <p>
     * HiGHS 125ms, SCIP 213ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testSentoy() {
        MIPLIBTheEasySet.doTest("sentoy.mps", "-7772", null);
    }

    /**
     * https://miplib.zib.de/instance_details_set1al.html
     * <p>
     * 712 variables, 493 expressions, density 0.66
     * <p>
     * HiGHS 39ms, SCIP 58ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * <li>2026-08-22: 0.3s (flow cover cut propagation)
     * </ul>
     */
    @Test
    public void testSet1al() {
        MIPLIBTheEasySet.doTest("set1al.mps", "15869.75", null);
    }

    /**
     * https://miplib.zib.de/instance_details_set1ch.html
     * <p>
     * 712 variables, 493 expressions, density 0.66
     * <p>
     * HiGHS 666ms, SCIP 210ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testSet1ch() {
        MIPLIBTheEasySet.doTest("set1ch.mps", "54537.75", null);
    }

    /**
     * https://miplib.zib.de/instance_details_set1cl.html
     * <p>
     * 712 variables, 493 expressions, density 0.66
     * <p>
     * HiGHS 43ms, SCIP 10ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 603s
     * <li>2026-08-21: 10min
     * <li>2026-08-23: 0.2s (flow cover cuts)
     * </ul>
     */
    @Test
    public void testSet1cl() {
        MIPLIBTheEasySet.doTest("set1cl.mps", "6484.25", null);
    }

    /**
     * https://miplib.zib.de/instance_details_sp150x300d.html
     * <p>
     * 600 variables, 451 expressions, density 0.50
     * <p>
     * HiGHS 60ms, SCIP 898ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testSp150x300d() {
        MIPLIBTheEasySet.doTest("sp150x300d.mps", "69", null);
    }

    /**
     * https://miplib.zib.de/instance_details_stein15.html
     * <p>
     * 15 variables, 37 expressions, density 1.0
     * <p>
     * HiGHS 23ms, SCIP 21ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testStein15() {
        MIPLIBTheEasySet.doTest("stein15.mps", "9", null);
    }

    /**
     * https://miplib.zib.de/instance_details_stein27.html
     * <p>
     * 27 variables, 119 expressions, density 1.0
     * <p>
     * HiGHS 835ms, SCIP 567ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.2s
     * </ul>
     */
    @Test
    public void testStein27() {
        MIPLIBTheEasySet.doTest("stein27.mps", "18", null);
    }

    /**
     * https://miplib.zib.de/instance_details_stein45.html
     * <p>
     * 45 variables, 332 expressions, density 1.0
     * <p>
     * HiGHS 13.1s, SCIP 7.3s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 5s
     * <li>2026-08-21: 10s
     * </ul>
     */
    @Tag("slow")
    @Test
    public void testStein45() {
        MIPLIBTheEasySet.doTest("stein45.mps", "30", null);
    }

    /**
     * https://miplib.zib.de/instance_details_stein9.html
     * <p>
     * 9 variables, 14 expressions, density 1.0
     * <p>
     * HiGHS 11ms, SCIP 4.6ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.1s
     * <li>2026-08-21: 0.01s
     * </ul>
     */
    @Test
    public void testStein9() {
        MIPLIBTheEasySet.doTest("stein9.mps", "5", null);
    }

    /**
     * https://miplib.zib.de/instance_details_supportcase14.html
     * <p>
     * 304 variables, 235 expressions, density 0.98
     * <p>
     * HiGHS 51ms, SCIP 51ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 1s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testSupportcase14() {
        MIPLIBTheEasySet.doTest("supportcase14.mps", "288", null);
    }

    /**
     * https://miplib.zib.de/instance_details_supportcase16.html
     * <p>
     * 319 variables, 131 expressions, density 0.98
     * <p>
     * HiGHS 44ms, SCIP 80ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 2s
     * <li>2026-08-21: 0.1s
     * </ul>
     */
    @Test
    public void testSupportcase16() {
        MIPLIBTheEasySet.doTest("supportcase16.mps", "288", null);
    }

    /**
     * https://miplib.zib.de/instance_details_timtab1.html
     * <p>
     * 397 variables, 172 expressions, density 0.32
     * <p>
     * HiGHS 42.6s, SCIP 44.1s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testTimtab1() {
        MIPLIBTheEasySet.doTest("timtab1.mps", "764772", null);
    }

    /**
     * https://miplib.zib.de/instance_details_timtab1CUTS.html
     * <p>
     * 397 variables, 372 expressions, density 0.32
     * <p>
     * HiGHS 77.1s, SCIP 55.0s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 601s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testTimtab1CUTS() {
        MIPLIBTheEasySet.doTest("timtab1CUTS.mps", "764772", null);
    }

    /**
     * https://miplib.zib.de/instance_details_vpm1.html
     * <p>
     * 378 variables, 235 expressions, density 0.44
     * <p>
     * HiGHS 11ms, SCIP 9.6ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 602s
     * <li>2026-08-21: 10min
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testVpm1() {
        MIPLIBTheEasySet.doTest("vpm1.mps", "20", null);
    }

    /**
     * https://miplib.zib.de/instance_details_vpm2.html
     * <p>
     * 378 variables, 235 expressions, density 0.44
     * <p>
     * HiGHS 2.3s, SCIP 2.0s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 237s
     * <li>2026-08-21: 520s
     * </ul>
     */
    @Tag("unstable")
    @Tag("slow")
    @Test
    public void testVpm2() {
        MIPLIBTheEasySet.doTest("vpm2.mps", "13.75", null);
    }

}
