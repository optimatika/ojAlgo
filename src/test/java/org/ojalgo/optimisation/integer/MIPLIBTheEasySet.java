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
 * A 794-model pool was assembled by combining 'easy' instances from every published MIPLIB edition — 2.0,
 * 3.0, 2003, 2010 and 2017 — into a single deduplicated collection. Older editions contain smaller, simpler
 * models that were often dropped from later editions for being too easy, making them better suited for
 * testing newer non-commercial solvers. The test set was then constructed by parsing all 794 models with
 * ojAlgo's MPS reader and filtering down to at most 1 000 variables and 1 000 constraints. A few models were
 * discarded because ojAlgo does not yet support certain MPS sections (e.g. LAZYCONS). The remaining models
 * were benchmarked against three commercial/open-source solvers (CPLEX, HiGHS and SCIP) with a 5 min
 * per-solver timeout. The 94 models here are those that all three solvers solved within that timeout. They
 * range from 9 to 990 variables.
 * <p>
 * The solver times in each test method's javadoc were measured on a MacBook Air (15-inch, M4, 2025) via the
 * ojAlgo mathematical-programming-benchmark harness using ojAlgo 57.1.1-SNAPSHOT as the MPS parser.
 * <p>
 * The tag 'slow' means getting a response takes too long (regardless of what the response is). The tag
 * 'unstable' means there is some problem with the returned solution (possibly that we've never seen one).
 *
 * @author apete
 */
public class MIPLIBTheEasySet extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(7);

    /**
     * For models where the expected value is asserted to more digits than the default MIP gap tolerance
     * guarantees: the default (5 significant digits) lets the solver stop at any solution within that band,
     * so the result would depend on search order.
     */
    private static void doTest(final String modelName, final String expMinValString, final String expMaxValString, final IntegerStrategy strategy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("MIPLIB", modelName, false);

        model.options.integer(strategy);

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, ACCURACY);
    }

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
     * CPLEX 112ms, HiGHS 2.0s, OR-Tools (SCIP) 769ms
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
     * CPLEX 55ms, HiGHS 4.1s, OR-Tools (SCIP) 1.5s
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
     * CPLEX 877ms, HiGHS 5.5s, OR-Tools (SCIP) 14.9s
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
     * CPLEX 136ms, HiGHS 53ms, OR-Tools (SCIP) 37ms
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
     * CPLEX 64ms, HiGHS 70ms, OR-Tools (SCIP) 241ms
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
     * CPLEX 141ms, HiGHS 227ms, OR-Tools (SCIP) 588ms
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
     * CPLEX 490ms, HiGHS 314ms, OR-Tools (SCIP) 1.7s
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 0.3s (objective only correct to 5 digits)
     * <li>2026-09-04: 2-7s with an 8-digit gap tolerance (the default 5 digits stops within ~1e-4 of the
     * optimum, which is what made this test unstable)
     * </ul>
     */
    @Test
    public void testBell3b() {
        MIPLIBTheEasySet.doTest("bell3b.mps", "11786160.62", null, IntegerStrategy.DEFAULT.withGapTolerance(NumberContext.of(8)));
    }

    /**
     * https://miplib.zib.de/instance_details_bell4.html
     * <p>
     * 117 variables, 106 expressions, density 0.71
     * <p>
     * CPLEX 470ms, HiGHS 858ms, OR-Tools (SCIP) 393ms
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
     * CPLEX 1.8s, HiGHS 674ms, OR-Tools (SCIP) 143ms
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
     * CPLEX 1.3s, HiGHS 17.6s, OR-Tools (SCIP) 78.4s
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
     * https://miplib.zib.de/instance_details_blend2.html
     * <p>
     * 353 variables, 275 expressions, density 0.25
     * <p>
     * CPLEX 2.2s, HiGHS 1.7s, OR-Tools (SCIP) 689ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 2s
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
     * CPLEX 113ms, HiGHS 61ms, OR-Tools (SCIP) 150ms
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
     * CPLEX 12.6s, HiGHS 157ms, OR-Tools (SCIP) 154ms
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
     * CPLEX 16ms, HiGHS 29ms, OR-Tools (SCIP) 69ms
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
     * CPLEX 43ms, HiGHS 3.1s, OR-Tools (SCIP) 850ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 3s
     * <li>2026-08-21: 1s
     * </ul>
     */
    @Test
    public void testDcmulti() {
        MIPLIBTheEasySet.doTest("dcmulti.mps", "188182", null);
    }

    /**
     * https://miplib.zib.de/instance_details_egout.html
     * <p>
     * 141 variables, 99 expressions, density 0.78
     * <p>
     * CPLEX 153ms, HiGHS 11ms, OR-Tools (SCIP) 2.8ms
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
     * CPLEX 113ms, HiGHS 118ms, OR-Tools (SCIP) 116ms
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
     * CPLEX 2.4ms, HiGHS 3.8s, OR-Tools (SCIP) 2.0ms
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
     * https://miplib.zib.de/instance_details_enlight8.html
     * <p>
     * 128 variables, 65 expressions, density 0.50
     * <p>
     * CPLEX 2.6ms, HiGHS 1.0s, OR-Tools (SCIP) 2.2ms
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
     * CPLEX 958ms, HiGHS 1.7s, OR-Tools (SCIP) 1.0s
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
     * CPLEX 823ms, HiGHS 6.8ms, OR-Tools (SCIP) 26ms
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
     * CPLEX 637ms, HiGHS 47ms, OR-Tools (SCIP) 18ms
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
     * CPLEX 188ms, HiGHS 182ms, OR-Tools (SCIP) 749ms
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
     * CPLEX 181ms, HiGHS 1.5s, OR-Tools (SCIP) 5.7s
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
     * CPLEX 6.3ms, HiGHS 42ms, OR-Tools (SCIP) 77ms
     * <p>
     * MacBook Pro (2026) M5 Pro, 48GB
     * <ul>
     * <li>2026-07-17: 0.2s
     * <li>2026-08-21: 0.02s
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
     * CPLEX 78ms, HiGHS 12ms, OR-Tools (SCIP) 11ms
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
     * https://miplib.zib.de/instance_details_gr4x6.html
     * <p>
     * 48 variables, 35 expressions, density 1.0
     * <p>
     * CPLEX 2.4s, HiGHS 7.2ms, OR-Tools (SCIP) 3.1ms
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
     * CPLEX 7.8s, HiGHS 163s, OR-Tools (SCIP) 188s
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
     * https://miplib.zib.de/instance_details_gsvm2rl3.html
     * <p>
     * 241 variables, 181 expressions, density 0.75
     * <p>
     * CPLEX 24.5s, HiGHS 4.6s, OR-Tools (SCIP) 5.0s
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
    public void testGsvm2rl3() {
        MIPLIBTheEasySet.doTest("gsvm2rl3.mps", "0.33652753", null);
    }

    /**
     * https://miplib.zib.de/instance_details_gt2.html
     * <p>
     * 188 variables, 30 expressions, density 0.49
     * <p>
     * CPLEX 141ms, HiGHS 28ms, OR-Tools (SCIP) 19ms
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
     * CPLEX 7.9s, HiGHS 13.2s, OR-Tools (SCIP) 8.1s
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
     * CPLEX 98ms, HiGHS 163ms, OR-Tools (SCIP) 417ms
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
     * https://miplib.zib.de/instance_details_mas76.html
     * <p>
     * 151 variables, 13 expressions, density 1.0
     * <p>
     * CPLEX 27.1s, HiGHS 225s, OR-Tools (SCIP) 64.9s
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
     * CPLEX 1.2s, HiGHS 6.5s, OR-Tools (SCIP) 45.9s
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
     * CPLEX 17.4s, HiGHS 3.0s, OR-Tools (SCIP) 3.1s
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
     * CPLEX 35.0s, HiGHS 3.2s, OR-Tools (SCIP) 3.3s
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
     * CPLEX 5.4s, HiGHS 5.1s, OR-Tools (SCIP) 7.3s
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
     * CPLEX 23.2s, HiGHS 27.5s, OR-Tools (SCIP) 8.3s
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
     * CPLEX 1.7s, HiGHS 2.1s, OR-Tools (SCIP) 2.2s
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
     * CPLEX 10.8s, HiGHS 118ms, OR-Tools (SCIP) 271ms
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
     * CPLEX 375ms, HiGHS 32ms, OR-Tools (SCIP) 115ms
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
     * CPLEX 112ms, HiGHS 389ms, OR-Tools (SCIP) 705ms
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
     * CPLEX 48ms, HiGHS 438ms, OR-Tools (SCIP) 968ms
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
     * CPLEX 724ms, HiGHS 23.3s, OR-Tools (SCIP) 75.7s
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
     * CPLEX 120s, HiGHS 1.3s, OR-Tools (SCIP) 539ms
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
     * CPLEX 145ms, HiGHS 420ms, OR-Tools (SCIP) 492ms
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
     * CPLEX 17.2s, HiGHS 404ms, OR-Tools (SCIP) 118ms
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
     * CPLEX 36ms, HiGHS 73ms, OR-Tools (SCIP) 2.9ms
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
     * https://miplib.zib.de/instance_details_neos-2624317-amur.html
     * <p>
     * 524 variables, 343 expressions, density 0.061
     * <p>
     * CPLEX 155ms, HiGHS 7.4s, OR-Tools (SCIP) 58.6s
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
     * CPLEX 36ms, HiGHS 801ms, OR-Tools (SCIP) 1.0s
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
     * CPLEX 73ms, HiGHS 4.6s, OR-Tools (SCIP) 722ms
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
     * CPLEX 22.6s, HiGHS 1.7s, OR-Tools (SCIP) 458ms
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
     * CPLEX 201ms, HiGHS 840ms, OR-Tools (SCIP) 1.2s
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
     * CPLEX 1.0s, HiGHS 1.2s, OR-Tools (SCIP) 725ms
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
     * CPLEX 7.5ms, HiGHS 25ms, OR-Tools (SCIP) 7.5ms
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
     * https://miplib.zib.de/instance_details_neos17.html
     * <p>
     * 535 variables, 487 expressions, density 0.91
     * <p>
     * CPLEX 623ms, HiGHS 3.7s, OR-Tools (SCIP) 5.7s
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
     * https://miplib.zib.de/instance_details_nexp-50-20-1-1.html
     * <p>
     * 490 variables, 541 expressions, density 0.50
     * <p>
     * CPLEX 102ms, HiGHS 27ms, OR-Tools (SCIP) 132ms
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
     * CPLEX 1.4s, HiGHS 39.7s, OR-Tools (SCIP) 73.6s
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
     * CPLEX 161ms, HiGHS 75ms, OR-Tools (SCIP) 167ms
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
     * CPLEX 7.7s, HiGHS 6.3ms, OR-Tools (SCIP) 6.1ms
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
     * CPLEX 1.6ms, HiGHS 2.8ms, OR-Tools (SCIP) 1.3ms
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
     * CPLEX 109ms, HiGHS 494ms, OR-Tools (SCIP) 416ms
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
     * CPLEX 127ms, HiGHS 82ms, OR-Tools (SCIP) 160ms
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
     * CPLEX 173ms, HiGHS 14ms, OR-Tools (SCIP) 7.2ms
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
     * CPLEX 20.0s, HiGHS 42ms, OR-Tools (SCIP) 54ms
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
     * CPLEX 1.7s, HiGHS 3.1s, OR-Tools (SCIP) 16.7s
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
     * CPLEX 153ms, HiGHS 64ms, OR-Tools (SCIP) 47ms
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
     * CPLEX 4.1s, HiGHS 137s, OR-Tools (SCIP) 156s
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
     * CPLEX 8.4s, HiGHS 696ms, OR-Tools (SCIP) 786ms
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
     * CPLEX 109ms, HiGHS 853ms, OR-Tools (SCIP) 1.8s
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
     * CPLEX 2.6s, HiGHS 39.9s, OR-Tools (SCIP) 10.0s
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
     * CPLEX 7.9s, HiGHS 95.8s, OR-Tools (SCIP) 88.4s
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
     * CPLEX 4.1s, HiGHS 26.4s, OR-Tools (SCIP) 52.6s
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
     * CPLEX 2.2s, HiGHS 10.9s, OR-Tools (SCIP) 23.4s
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
     * CPLEX 23.4s, HiGHS 13.2s, OR-Tools (SCIP) 20.3s
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
     * CPLEX 4.8s, HiGHS 50.5s, OR-Tools (SCIP) 64.3s
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
     * CPLEX 18.2s, HiGHS 126ms, OR-Tools (SCIP) 104ms
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
     * CPLEX 5.3s, HiGHS 63.8s, OR-Tools (SCIP) 21.6s
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
     * CPLEX 161ms, HiGHS 13ms, OR-Tools (SCIP) 2.1ms
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
     * CPLEX 962ms, HiGHS 125ms, OR-Tools (SCIP) 213ms
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
     * CPLEX 25.2s, HiGHS 39ms, OR-Tools (SCIP) 58ms
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
     * CPLEX 23.1s, HiGHS 666ms, OR-Tools (SCIP) 210ms
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
     * CPLEX 2.9s, HiGHS 43ms, OR-Tools (SCIP) 10ms
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
     * CPLEX 152ms, HiGHS 60ms, OR-Tools (SCIP) 898ms
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
     * CPLEX 312ms, HiGHS 23ms, OR-Tools (SCIP) 21ms
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
     * CPLEX 553ms, HiGHS 835ms, OR-Tools (SCIP) 567ms
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
     * CPLEX 752ms, HiGHS 13.1s, OR-Tools (SCIP) 7.3s
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
     * CPLEX 96ms, HiGHS 11ms, OR-Tools (SCIP) 4.6ms
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
     * CPLEX 1.8s, HiGHS 51ms, OR-Tools (SCIP) 51ms
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
     * CPLEX 1.7s, HiGHS 44ms, OR-Tools (SCIP) 80ms
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
     * CPLEX 19.2s, HiGHS 42.6s, OR-Tools (SCIP) 44.1s
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
     * CPLEX 26.9s, HiGHS 77.1s, OR-Tools (SCIP) 55.0s
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
     * CPLEX 166ms, HiGHS 11ms, OR-Tools (SCIP) 9.6ms
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
     * CPLEX 1.5s, HiGHS 2.3s, OR-Tools (SCIP) 2.0s
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
