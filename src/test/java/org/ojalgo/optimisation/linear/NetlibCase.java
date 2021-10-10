/*
 * Copyright 1997-2021 Optimatika
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
import org.ojalgo.optimisation.ModelFileMPS;
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
public class NetlibCase extends OptimisationLinearTests implements ModelFileMPS {

    static void doTest(final String name, final String expMinValString, final String expMaxValString, final NumberContext accuracy) {
        ModelFileMPS.makeAndAssert("netlib", name, expMinValString, expMaxValString, false, accuracy, null);
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void test25FV47() {
        NetlibCase.doTest("25FV47.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void test80BAU3B() {
        NetlibCase.doTest("80BAU3B.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testADLITTLE() {
        NetlibCase.doTest("ADLITTLE.SIF", "225494.96316238036", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAFIRO() {
        NetlibCase.doTest("AFIRO.SIF", "-464.7531428571429", "3438.2920999999997", NumberContext.of(7, 4));
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
        NetlibCase.doTest("AGG.SIF", "-3.599176728657652E7", "2.8175579434489565E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAGG2() {
        NetlibCase.doTest("AGG2.SIF", "-2.0239252355977114E7", "5.71551859632249E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testAGG3() {
        NetlibCase.doTest("AGG3.SIF", "1.031211593508922E7", "5.746768863949547E9", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBANDM() {
        NetlibCase.doTest("BANDM.SIF", "-158.6280184501187", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBEACONFD() {
        NetlibCase.doTest("BEACONFD.SIF", "33592.4858072", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -30.81214985
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBLEND() {
        NetlibCase.doTest("BLEND.SIF", "-3.0812149846E+01", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Objective defined by ojAlgo's current result
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testBNL1() {
        NetlibCase.doTest("BNL1.SIF", "1977.629561522682", "1977.629561522682", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testBNL2() {
        NetlibCase.doTest("BNL2.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBOEING1() {
        NetlibCase.doTest("BOEING1.SIF", "-335.2135675071266", "286.9746573387996", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -315.01872802
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBOEING2() {
        NetlibCase.doTest("BOEING2.SIF", "-3.1501872802E+02", "-73.36896910872208", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBORE3D() {
        NetlibCase.doTest("BORE3D.SIF", "1373.0803942085367", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testBRANDY() {
        NetlibCase.doTest("BRANDY.SIF", "1518.509896488128", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testCAPRI() {
        NetlibCase.doTest("CAPRI.SIF", "2690.0129137681624", null, NumberContext.of(7, 4));
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
    public void testCRE_A() {
        NetlibCase.doTest("CRE-A.SIF", "2.9889732905677114E7", "2.9889732905677114E7", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testCRE_B() {
        NetlibCase.doTest("CRE-B.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
    public void testCRE_C() {
        NetlibCase.doTest("CRE-C.SIF", "2.996133067602781E7", "2.996133067602781E7", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testCRE_D() {
        NetlibCase.doTest("CRE-D.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testCYCLE() {
        NetlibCase.doTest("CYCLE.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("CZPROB.SIF", "2185196.6988565763", "2185196.6988565763", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testD2Q06C() {
        NetlibCase.doTest("D2Q06C.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testD6CUBE() {
        NetlibCase.doTest("D6CUBE.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testDEGEN2() {
        NetlibCase.doTest("DEGEN2.SIF", "-1435.1779999999999", "-1226.12", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testDEGEN3() {
        NetlibCase.doTest("DEGEN3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-15: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-15: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testDFL001() {
        NetlibCase.doTest("DFL001.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testE226() {
        NetlibCase.doTest("E226.SIF", "-18.751929066370547", "104.53796068931459", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testETAMACRO() {
        NetlibCase.doTest("ETAMACRO.SIF", "-755.7152312325337", "258.71905646302014", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFFFFF800() {
        NetlibCase.doTest("FFFFF800.SIF", "555679.5648174941", "1858776.4328128027", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFINNIS() {
        NetlibCase.doTest("FINNIS.SIF", "172791.0655956116", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("FIT1D.SIF", "-9146.378092421019", "-9146.378092421019", NumberContext.of(7, 4));
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
        NetlibCase.doTest("FIT1P.SIF", "42246.94459638855", "42246.94459638855", NumberContext.of(7, 4));
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
    public void testFIT2D() {
        NetlibCase.doTest("FIT2D.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testFIT2P() {
        NetlibCase.doTest("FIT2P.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-14: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testFORPLAN() {
        NetlibCase.doTest("FORPLAN.SIF", "-664.2189612722054", "2862.4274777342266", NumberContext.of(7, 4));
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
        NetlibCase.doTest("GANGES.SIF", "-109585.73612927811", "-109585.73612927811", NumberContext.of(7, 4));
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
        NetlibCase.doTest("GFRD-PNC.SIF", "6902235.999548811", "6902235.999548811", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testGREENBEA() {
        NetlibCase.doTest("GREENBEA.SIF", "-1.74990012991E+03", "-1.74990012991E+03", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testGREENBEB() {
        NetlibCase.doTest("GREENBEB.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testGROW15() {
        NetlibCase.doTest("GROW15.SIF", "-1.068709412935753E8", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testGROW22() {
        NetlibCase.doTest("GROW22.SIF", "-1.608343364825636E8", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testGROW7() {
        NetlibCase.doTest("GROW7.SIF", "-4.7787811814711526E7", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testISRAEL() {
        NetlibCase.doTest("ISRAEL.SIF", "-896644.8218630457", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -1749.90012991
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testKB2() {
        NetlibCase.doTest("KB2.SIF", "-1.74990012991E+03", "0.0", NumberContext.of(7, 4));
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
        NetlibCase.doTest("KEN-07.SIF", "-6.795204433816869E8", "-6.795204433816869E8", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testKEN_11() {
        NetlibCase.doTest("KEN-11.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testKEN_13() {
        NetlibCase.doTest("KEN-13.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testKEN_18() {
        NetlibCase.doTest("KEN-18.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testLOTFI() {
        NetlibCase.doTest("LOTFI.SIF", "-25.26470606188002", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("MAROS.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testMAROS_R7() {
        NetlibCase.doTest("MAROS-R7.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("MODSZK1.SIF", "320.6197293824883", "320.6197293824883", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testNESM() {
        NetlibCase.doTest("NESM.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("OSA-07.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("OSA-14.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testOSA_30() {
        NetlibCase.doTest("OSA-30.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testOSA_60() {
        NetlibCase.doTest("OSA-60.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("PDS-02.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPDS_06() {
        NetlibCase.doTest("PDS-06.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPDS_10() {
        NetlibCase.doTest("PDS-10.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPDS_20() {
        NetlibCase.doTest("PDS-20.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-14: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPEROLD() {
        NetlibCase.doTest("PEROLD.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPILOT() {
        NetlibCase.doTest("PILOT.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPILOT_JA() {
        NetlibCase.doTest("PILOT-JA.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("PILOT-WE.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testPILOT4() {
        NetlibCase.doTest("PILOT4.SIF", "-2581.1392612778604", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPILOT87() {
        NetlibCase.doTest("PILOT87.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testPILOTNOV() {
        NetlibCase.doTest("PILOTNOV.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testQAP12() {
        NetlibCase.doTest("QAP12.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testQAP15() {
        NetlibCase.doTest("QAP15.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testQAP8() {
        NetlibCase.doTest("QAP8.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testRECIPELP() {
        NetlibCase.doTest("RECIPELP.SIF", "-266.616", "-104.818", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC105() {
        NetlibCase.doTest("SC105.SIF", "-52.202061211707246", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC205() {
        NetlibCase.doTest("SC205.SIF", "-52.202061211707246", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC50A() {
        NetlibCase.doTest("SC50A.SIF", "-64.57507705856449", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -70.00000000
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSC50B() {
        NetlibCase.doTest("SC50B.SIF", "-7.0000000000E+01", "0.0", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSCAGR25() {
        NetlibCase.doTest("SCAGR25.SIF", "-1.475343306076852E7", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCAGR7() {
        NetlibCase.doTest("SCAGR7.SIF", "-2331389.8243309837", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCFXM1() {
        NetlibCase.doTest("SCFXM1.SIF", "18416.75902834894", null, NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCFXM2() {
        NetlibCase.doTest("SCFXM2.SIF", "36660.261564998815", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("SCFXM3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCORPION() {
        NetlibCase.doTest("SCORPION.SIF", "1878.1248227381068", null, NumberContext.of(7, 4));
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
    public void testSCRS8() {
        NetlibCase.doTest("SCRS8.SIF", "960.0206152764557", "960.0206152764557", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCSD1() {
        NetlibCase.doTest("SCSD1.SIF", "8.666666674333367", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("SCSD6.SIF", "50.5", "50.5", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SCSD8.SIF", "905", "905", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSCTAP1() {
        NetlibCase.doTest("SCTAP1.SIF", "1412.25", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("SCTAP2.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SCTAP3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSEBA() {
        NetlibCase.doTest("SEBA.SIF", "15711.6", "35160.46056", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSHARE1B() {
        NetlibCase.doTest("SHARE1B.SIF", "-76589.31857918584", "74562.53714565346", NumberContext.of(5, 4));
    }

    /**
     * <pre>
     * 2010-04-19: lp_solve => -415.73224074
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSHARE2B() {
        NetlibCase.doTest("SHARE2B.SIF", "-4.1573224074E+02", "-265.0981144446295", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHELL.SIF", "1.208825346E9", "1.208825346E9", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", "1793324.5379703548", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04S.SIF", "1798714.7004453915", "1798714.7004453915", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", "1793324.5379703548", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", "1793324.5379703548", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", "1793324.5379703548", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", "1793324.5379703548", NumberContext.of(7, 4));
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
        NetlibCase.doTest("SIERRA.SIF", "1.5394362183631932E7", "1.5394362183631932E7", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    public void testSTAIR() {
        NetlibCase.doTest("STAIR.SIF", "-251.26695119296787", "-208.79999", NumberContext.of(7, 4));
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
        NetlibCase.doTest("STANDATA.SIF", "1257.6995", "1257.6995", NumberContext.of(7, 4));
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
        NetlibCase.doTest("STANDGUB.SIF", "1257.6995", "1257.6995", NumberContext.of(7, 4));
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
        NetlibCase.doTest("STANDMPS.SIF", "1406.0175", "1406.0175", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testSTOCFOR1() {
        NetlibCase.doTest("STOCFOR1.SIF", "-41131.97621943626", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("STOCFOR2.SIF", "-39024.4085378819", "-39024.4085378819", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testSTOCFOR3() {
        NetlibCase.doTest("STOCFOR3.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Tagged as slow since too large for promotional/community version of CPLEX
     * 2019-02-13: Tagged as unstable since ojAlgo takes too long or fails validation
     * </pre>
     */
    @Test
    @Tag("slow")
    public void testTRUSS() {
        NetlibCase.doTest("TRUSS.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("TUFF.SIF", "0.29214776509361284", "0.8949901867574317", NumberContext.of(7, 4));
    }

    /**
     * <pre>
     * 2019-02-13: Objective obtained/verified by CPLEX
     * </pre>
     */
    @Test
    public void testVTP_BASE() {
        NetlibCase.doTest("VTP-BASE.SIF", "129831.46246136136", null, NumberContext.of(7, 4));
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
        NetlibCase.doTest("WOOD1P.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
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
        NetlibCase.doTest("WOODW.SIF", "1234567890", "1234567890", NumberContext.of(7, 4));
    }

}
