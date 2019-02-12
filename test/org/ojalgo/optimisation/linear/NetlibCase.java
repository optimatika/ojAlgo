/*
 * Copyright 1997-2018 Optimatika Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ModelFileMPS;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: http://www.numerical.rl.ac.uk/cute/netlib.html
 *
 * @author apete
 */
public class NetlibCase extends OptimisationLinearTests implements ModelFileMPS {

    static final NumberContext PRECISION = NumberContext.getGeneral(8, 6);

    static void doTest(final String name, final String expMinValString, final String expMaxValString) {
        ModelFileMPS.makeAndAssert("netlib", name, expMinValString, expMaxValString, false, PRECISION, null);
    }

    @Test
    @Tag("slow")
    public void test25FV47() {
        NetlibCase.doTest("25FV47.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void test80BAU3B() {
        NetlibCase.doTest("80BAU3B.SIF", "", null);
    }

    @Test
    public void testADLITTLE() {
        NetlibCase.doTest("ADLITTLE.SIF", "225494.96316238036", null);
    }

    @Test
    public void testAFIRO() {
        NetlibCase.doTest("AFIRO.SIF", "-464.7531428571429", null);
    }

    @Test
    public void testAGG() {
        NetlibCase.doTest("AGG.SIF", "-3.599176728657652E7", null);
    }

    @Test
    public void testAGG2() {
        NetlibCase.doTest("AGG2.SIF", "-2.0239252355977114E7", null);
    }

    @Test
    public void testAGG3() {
        NetlibCase.doTest("AGG3.SIF", "1.031211593508922E7", null);
    }

    @Test
    public void testBANDM() {
        NetlibCase.doTest("BANDM.SIF", "-158.6280184501187", null);
    }

    @Test
    public void testBEACONFD() {
        NetlibCase.doTest("BEACONFD.SIF", "33592.4858072", null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -30.81214985
     */
    @Test
    public void testBLEND() {
        NetlibCase.doTest("BLEND.SIF", "-3.0812149846E+01", null);
    }

    @Test
    @Tag("slow")
    public void testBNL1() {
        NetlibCase.doTest("BNL1.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testBNL2() {
        NetlibCase.doTest("BNL2.SIF", "", null);
    }

    @Test
    public void testBOEING1() {
        NetlibCase.doTest("BOEING1.SIF", "-335.2135675071266", null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -315.01872802
     */
    @Test
    public void testBOEING2() {
        NetlibCase.doTest("BOEING2.SIF", "-3.1501872802E+02", null);
    }

    @Test
    public void testBORE3D() {
        NetlibCase.doTest("BORE3D.SIF", "1373.0803942085367", null);
    }

    @Test
    public void testBRANDY() {
        NetlibCase.doTest("BRANDY.SIF", "1518.509896488128", null);
    }

    @Test
    public void testCAPRI() {
        NetlibCase.doTest("CAPRI.SIF", "2690.0129137681624", null);
    }

    @Test
    @Tag("slow")
    public void testCRE_A() {
        NetlibCase.doTest("CRE-A.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testCRE_B() {
        NetlibCase.doTest("CRE-B.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testCRE_C() {
        NetlibCase.doTest("CRE-C.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testCRE_D() {
        NetlibCase.doTest("CRE-D.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testCYCLE() {
        NetlibCase.doTest("CYCLE.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testCZPROB() {
        NetlibCase.doTest("CZPROB.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testD2Q06C() {
        NetlibCase.doTest("D2Q06C.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testD6CUBE() {
        NetlibCase.doTest("D6CUBE.SIF", "", null);
    }

    @Test
    public void testDEGEN2() {
        NetlibCase.doTest("DEGEN2.SIF", "-1435.1779999999999", null);
    }

    @Test
    @Tag("slow")
    public void testDEGEN3() {
        NetlibCase.doTest("DEGEN3.SIF", "", null);
    }

    @Test
    @Tag("slow")
    public void testDFL001() {
        NetlibCase.doTest("DFL001.SIF", "", null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -1749.90012991
     */
    @Test
    public void testKB2() {
        NetlibCase.doTest("KB2.SIF", "-1.74990012991E+03", null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -70.00000000
     */
    @Test
    public void testSC50B() {
        NetlibCase.doTest("SC50B.SIF", "-7.0000000000E+01", null);
    }

    /**
     * OK! 2010-04-19 lp_solve => -415.73224074
     */
    @Test
    public void testSHARE2B() {
        NetlibCase.doTest("SHARE2B.SIF", "-4.1573224074E+02", null);
    }

}
