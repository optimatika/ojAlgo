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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: http://www.numerical.rl.ac.uk/cute/netlib.html
 * <p>
 * When specifying min/max null means unbounded in that direction and "1234567890" represents an unknown or
 * unverified value. And if the min and max values are set to the same value then one of them (max) is not
 * verified.
 *
 * <pre>
 * 2021-10-16: Modified to also verify maximisation (some models are unbounded in this case).
 * </pre>
 *
 * @author apete
 */
public class CuteNetlibCase extends OptimisationLinearTests implements ModelFileTest {

    private static void doTest(final String name, final String expMinValString, final String expMaxValString, final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", name, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * 2022-02-10: Objective value set to what ojAlgo returns
     * </pre>
     */
    @Test
    @Tag("slow")
    public void test25FV47() {
        CuteNetlibCase.doTest("25FV47.SIF", "5501.845888286646", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void test80BAU3B() {
        CuteNetlibCase.doTest("80BAU3B.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testADLITTLE() {
        CuteNetlibCase.doTest("ADLITTLE.SIF", "225494.96316238036", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAFIRO() {
        CuteNetlibCase.doTest("AFIRO.SIF", "-464.7531428571429", "3438.2920999999997", NumberContext.of(7, 4));
    }

    /**
     * This file contained this section:
     *
     * <pre>
     * BJECT BOUND
     *
     *   Solution
     *
     * LO SOLTN                   ???
     * </pre>
     *
     * As far as I know that's not standard MPS. Assume it's something SIF specific, but don't understand what
     * it would do. Removed this section.
     *
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAGG() {
        CuteNetlibCase.doTest("AGG.SIF", "-3.599176728657652E7", "2.8175579434489565E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAGG2() {
        CuteNetlibCase.doTest("AGG2.SIF", "-2.0239252355977114E7", "5.71551859632249E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAGG3() {
        CuteNetlibCase.doTest("AGG3.SIF", "1.031211593508922E7", "5.746768863949547E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBANDM() {
        CuteNetlibCase.doTest("BANDM.SIF", "-158.6280184501187", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBEACONFD() {
        CuteNetlibCase.doTest("BEACONFD.SIF", "33592.4858072", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -30.81214985
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBLEND() {
        CuteNetlibCase.doTest("BLEND.SIF", "-3.0812149846E+01", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testBNL1() {
        CuteNetlibCase.doTest("BNL1.SIF", "1977.629561522682", "1977.629561522682", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testBNL2() {
        CuteNetlibCase.doTest("BNL2.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBOEING1() {
        CuteNetlibCase.doTest("BOEING1.SIF", "-335.2135675071266", "286.9746573387996", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -315.01872802
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBOEING2() {
        CuteNetlibCase.doTest("BOEING2.SIF", "-3.1501872802E+02", "-73.36896910872208", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBORE3D() {
        CuteNetlibCase.doTest("BORE3D.SIF", "1373.0803942085367", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBRANDY() {
        CuteNetlibCase.doTest("BRANDY.SIF", "1518.509896488128", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testCAPRI() {
        CuteNetlibCase.doTest("CAPRI.SIF", "2690.0129137681624", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * 2019-02-19: Current solution from ojAlgo, but probably just feasible and not optimal
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_A() {
        CuteNetlibCase.doTest("CRE-A.SIF", "2.9889732905677114E7", "2.9889732905677114E7", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_B() {
        CuteNetlibCase.doTest("CRE-B.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * 2019-02-19: Current solution from ojAlgo, but probably just feasible and not optimal
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_C() {
        CuteNetlibCase.doTest("CRE-C.SIF", "2.996133067602781E7", "2.996133067602781E7", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_D() {
        CuteNetlibCase.doTest("CRE-D.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCYCLE() {
        CuteNetlibCase.doTest("CYCLE.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testCZPROB() {
        CuteNetlibCase.doTest("CZPROB.SIF", "2185196.6988565763", "3089066.71321333", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testD2Q06C() {
        CuteNetlibCase.doTest("D2Q06C.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testD6CUBE() {
        CuteNetlibCase.doTest("D6CUBE.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testDEGEN2() {
        CuteNetlibCase.doTest("DEGEN2.SIF", "-1435.1779999999999", "-1226.12", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testDEGEN3() {
        CuteNetlibCase.doTest("DEGEN3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-15: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-15: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testDFL001() {
        CuteNetlibCase.doTest("DFL001.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * Optimal value is stated to be -1.8751929066E+01 but there is a "RHS" of -7.113 given for the objective
     * row which should shift the solution by that amount to instead be -1.1638929066e+01. Using CPLEX to
     * parse the MPS file and then solve the problem confirms this.
     *
     * <pre>
     * 2021-10-01:
     * Dual simplex - Optimal:  Objective = -1.1638929066e+01
     * Solution time =    0.01 sec.  Iterations = 256 (58)
     * Deterministic time = 4.70 ticks  (710.92 ticks/sec)
     * </pre>
     */
    @Test
    public void testE226() {
        CuteNetlibCase.doTest("E226.SIF", "-11.638929066370546", "111.65096068931459", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testETAMACRO() {
        CuteNetlibCase.doTest("ETAMACRO.SIF", "-755.7152312325337", "258.71905646302014", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFFFFF800() {
        CuteNetlibCase.doTest("FFFFF800.SIF", "555679.5648174941", "1858776.4328128027", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFINNIS() {
        CuteNetlibCase.doTest("FINNIS.SIF", "172791.0655956116", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-19: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testFIT1D() {
        CuteNetlibCase.doTest("FIT1D.SIF", "-9146.378092421019", "80453.99999999999", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * 2019-02-19: Current solution from ojAlgo, but probably just feasible and not optimal
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testFIT1P() {
        CuteNetlibCase.doTest("FIT1P.SIF", "9146.378092420955", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * 2019-02-19: java.lang.OutOfMemoryError: Java heap space
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testFIT2D() {
        CuteNetlibCase.doTest("FIT2D.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testFIT2P() {
        CuteNetlibCase.doTest("FIT2P.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-14: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFORPLAN() {
        CuteNetlibCase.doTest("FORPLAN.SIF", "-664.2189612722054", "2862.4274777342266", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testGANGES() {
        CuteNetlibCase.doTest("GANGES.SIF", "-109585.73612927811", "-2.24E-12", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-15: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-15: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testGFRD_PNC() {
        CuteNetlibCase.doTest("GFRD-PNC.SIF", "6902235.999548811", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testGREENBEA() {
        CuteNetlibCase.doTest("GREENBEA.SIF", "-1.74990012991E+03", "-1.74990012991E+03", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testGREENBEB() {
        CuteNetlibCase.doTest("GREENBEB.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testGROW15() {
        CuteNetlibCase.doTest("GROW15.SIF", "-1.068709412935753E8", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testGROW22() {
        CuteNetlibCase.doTest("GROW22.SIF", "-1.608343364825636E8", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testGROW7() {
        CuteNetlibCase.doTest("GROW7.SIF", "-4.7787811814711526E7", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testISRAEL() {
        CuteNetlibCase.doTest("ISRAEL.SIF", "-896644.8218630457", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -1749.90012991
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testKB2() {
        CuteNetlibCase.doTest("KB2.SIF", "-1.74990012991E+03", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testKEN_07() {
        CuteNetlibCase.doTest("KEN-07.SIF", "-6.795204433816869E8", "-1.61949281194431E8", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testKEN_11() {
        CuteNetlibCase.doTest("KEN-11.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testKEN_13() {
        CuteNetlibCase.doTest("KEN-13.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testKEN_18() {
        CuteNetlibCase.doTest("KEN-18.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testLOTFI() {
        CuteNetlibCase.doTest("LOTFI.SIF", "-25.26470606188002", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testMAROS() {
        CuteNetlibCase.doTest("MAROS.SIF", "-58063.743701138235", "-10623.409207717115", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testMAROS_R7() {
        CuteNetlibCase.doTest("MAROS-R7.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-15: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-15: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testMODSZK1() {
        CuteNetlibCase.doTest("MODSZK1.SIF", "320.6197293824883", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testNESM() {
        CuteNetlibCase.doTest("NESM.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testOSA_07() {
        CuteNetlibCase.doTest("OSA-07.SIF", "535722.517299352", "4332086.205299969", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testOSA_14() {
        CuteNetlibCase.doTest("OSA-14.SIF", "1106462.8447362552", "9377699.405100001", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testOSA_30() {
        CuteNetlibCase.doTest("OSA-30.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testOSA_60() {
        CuteNetlibCase.doTest("OSA-60.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPDS_02() {
        CuteNetlibCase.doTest("PDS-02.SIF", "2.885786201E10", "2.931365171E10", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_06() {
        CuteNetlibCase.doTest("PDS-06.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_10() {
        CuteNetlibCase.doTest("PDS-10.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_20() {
        CuteNetlibCase.doTest("PDS-20.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPEROLD() {
        CuteNetlibCase.doTest("PEROLD.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT() {
        CuteNetlibCase.doTest("PILOT.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT_JA() {
        CuteNetlibCase.doTest("PILOT-JA.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPILOT_WE() {
        CuteNetlibCase.doTest("PILOT-WE.SIF", "-2720107.5328449034", "20770.464669007524", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testPILOT4() {
        CuteNetlibCase.doTest("PILOT4.SIF", "-2581.1392612778604", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT87() {
        CuteNetlibCase.doTest("PILOT87.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOTNOV() {
        CuteNetlibCase.doTest("PILOTNOV.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testQAP12() {
        CuteNetlibCase.doTest("QAP12.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testQAP15() {
        CuteNetlibCase.doTest("QAP15.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testQAP8() {
        CuteNetlibCase.doTest("QAP8.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testRECIPELP() {
        CuteNetlibCase.doTest("RECIPELP.SIF", "-266.616", "-104.818", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC105() {
        CuteNetlibCase.doTest("SC105.SIF", "-52.202061211707246", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC205() {
        CuteNetlibCase.doTest("SC205.SIF", "-52.202061211707246", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC50A() {
        CuteNetlibCase.doTest("SC50A.SIF", "-64.57507705856449", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -70.00000000
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC50B() {
        CuteNetlibCase.doTest("SC50B.SIF", "-7.0000000000E+01", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSCAGR25() {
        CuteNetlibCase.doTest("SCAGR25.SIF", "-1.475343306076852E7", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCAGR7() {
        CuteNetlibCase.doTest("SCAGR7.SIF", "-2331389.8243309837", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCFXM1() {
        CuteNetlibCase.doTest("SCFXM1.SIF", "18416.75902834894", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCFXM2() {
        CuteNetlibCase.doTest("SCFXM2.SIF", "36660.261564998815", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSCFXM3() {
        CuteNetlibCase.doTest("SCFXM3.SIF", "54901.2545497515", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCORPION() {
        CuteNetlibCase.doTest("SCORPION.SIF", "1878.1248227381068", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * 2019-02-15: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testSCRS8() {
        CuteNetlibCase.doTest("SCRS8.SIF", "960.0206152764557", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCSD1() {
        CuteNetlibCase.doTest("SCSD1.SIF", "8.666666674333367", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSCSD6() {
        CuteNetlibCase.doTest("SCSD6.SIF", "50.5", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSCSD8() {
        CuteNetlibCase.doTest("SCSD8.SIF", "905", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCTAP1() {
        CuteNetlibCase.doTest("SCTAP1.SIF", "1412.25", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSCTAP2() {
        CuteNetlibCase.doTest("SCTAP2.SIF", "1724.8071428568292", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSCTAP3() {
        CuteNetlibCase.doTest("SCTAP3.SIF", "1424.000000000573", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSEBA() {
        CuteNetlibCase.doTest("SEBA.SIF", "15711.6", "35160.46056", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSHARE1B() {
        CuteNetlibCase.doTest("SHARE1B.SIF", "-76589.31857918584", "74562.53714565346", NumberContext.of(5, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -415.73224074
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSHARE2B() {
        CuteNetlibCase.doTest("SHARE2B.SIF", "-4.1573224074E+02", "-265.0981144446295", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHELL() {
        CuteNetlibCase.doTest("SHELL.SIF", "1.208825346E9", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP04L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP04S() {
        CuteNetlibCase.doTest("SHIP04S.SIF", "1798714.7004453915", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP08L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP08S() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP12L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSHIP12S() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-15: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-15: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSIERRA() {
        CuteNetlibCase.doTest("SIERRA.SIF", "1.5394362183631932E7", "8.042913100947624E8", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSTAIR() {
        CuteNetlibCase.doTest("STAIR.SIF", "-251.26695119296787", "-208.79999", NumberContext.of(7, 2));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSTANDATA() {
        CuteNetlibCase.doTest("STANDATA.SIF", "1257.6995", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSTANDGUB() {
        CuteNetlibCase.doTest("STANDGUB.SIF", "1257.6995", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSTANDMPS() {
        CuteNetlibCase.doTest("STANDMPS.SIF", "1406.0175", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSTOCFOR1() {
        CuteNetlibCase.doTest("STOCFOR1.SIF", "-41131.97621943626", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSTOCFOR2() {
        CuteNetlibCase.doTest("STOCFOR2.SIF", "-39024.4085378819", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testSTOCFOR3() {
        CuteNetlibCase.doTest("STOCFOR3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testTRUSS() {
        CuteNetlibCase.doTest("TRUSS.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("unstable")
    public void testTUFF() {
        CuteNetlibCase.doTest("TUFF.SIF", "0.29214776509361284", "0.8949901867574317", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testVTP_BASE() {
        CuteNetlibCase.doTest("VTP-BASE.SIF", "129831.46246136136", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testWOOD1P() {
        CuteNetlibCase.doTest("WOOD1P.SIF", "1.44290241157344", "9.99999999999964", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testWOODW() {
        CuteNetlibCase.doTest("WOODW.SIF", "1.30447633308416", "6.463675062936", NumberContext.of(7, 4));
    }

}
