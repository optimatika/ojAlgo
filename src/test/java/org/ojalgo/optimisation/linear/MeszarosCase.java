/*
 * Copyright 1997-2024 Optimatika
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
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of optimisation models found here: http://old.sztaki.hu/~meszaros/public_ftp/lptestset/
 * <p>
 * When specifying min/max "" means unbounded in that direction and "1234567890" represents an unknown or
 * unverified value. If both min and max are "" then the problem is infeasible.
 *
 * @author apete
 */
public class MeszarosCase extends OptimisationLinearTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(7);

    private static void doTest(final String name, final String expMinValString, final String expMaxValString, final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("meszaros", name, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        //        long time = 10_000L;
        //        model.options.time_abort = time;
        //        model.options.time_suffice = time;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);
    }

    @Test
    public void testBGDBG1() {
        MeszarosCase.doTest("bgdbg1.mps", "", "", ACCURACY);
    }

    @Test
    public void testBGPRTR() {
        MeszarosCase.doTest("bgprtr.mps", "", "", ACCURACY);
    }

    @Test
    public void testBOX1() {
        MeszarosCase.doTest("box1.mps", "", "", ACCURACY);
    }

    @Test
    public void testCHEMCOM() {
        MeszarosCase.doTest("chemcom.mps", "", "", ACCURACY);
    }

    @Test
    public void testCPLEX2() {
        MeszarosCase.doTest("cplex2.mps", "", "", ACCURACY);
    }

    @Test
    public void testEX72A() {
        MeszarosCase.doTest("ex72a.mps", "", "", ACCURACY);
    }

    @Test
    public void testEX73A() {
        MeszarosCase.doTest("ex73a.mps", "", "", ACCURACY);
    }

    @Test
    public void testFARM() {
        MeszarosCase.doTest("farm.mps", "17500.0", "99250.0", ACCURACY);
    }

    @Test
    public void testFOREST6() {
        MeszarosCase.doTest("forest6.mps", "", "", ACCURACY);
    }

    @Test
    public void testGALENET() {
        MeszarosCase.doTest("galenet.mps", "", "", ACCURACY);
    }

    @Test
    public void testGAMS10A() {
        MeszarosCase.doTest("gams10a.mps", "1.0", "1.0", ACCURACY);
    }

    @Test
    public void testGAMS10AM() {
        MeszarosCase.doTest("gams10am.mps", "", "", ACCURACY);
    }

    @Test
    public void testGAMS30A() {
        MeszarosCase.doTest("gams30a.mps", "1.0", "1.0", ACCURACY);
    }

    @Test
    public void testGAMS30AM() {
        MeszarosCase.doTest("gams30am.mps", "", "", ACCURACY);
    }

    @Test
    public void testITEST2() {
        MeszarosCase.doTest("itest2.mps", "", "", ACCURACY);
    }

    @Test
    public void testITEST6() {
        MeszarosCase.doTest("itest6.mps", "", "", ACCURACY);
    }

    @Test
    public void testKLEEMIN3() {
        MeszarosCase.doTest("kleemin3.mps", "-10000.0", "0.0", ACCURACY);
    }

    @Test
    public void testKLEEMIN4() {
        MeszarosCase.doTest("kleemin4.mps", "-1000000.0", "0.0", ACCURACY);
    }

    @Test
    public void testKLEEMIN5() {
        MeszarosCase.doTest("kleemin5.mps", "-1.0E8", "0.0", ACCURACY);
    }

    @Test
    public void testKLEEMIN6() {
        MeszarosCase.doTest("kleemin6.mps", "-1.0E10", "0.0", ACCURACY);
    }

    @Test
    public void testKLEEMIN7() {
        MeszarosCase.doTest("kleemin7.mps", "-1.0E12", "0.0", ACCURACY);
    }

    @Test
    public void testKLEEMIN8() {
        MeszarosCase.doTest("kleemin8.mps", "-1.0E14", "0.0", ACCURACY);
    }

    @Test
    public void testKLEIN1() {
        MeszarosCase.doTest("klein1.mps", "", "", ACCURACY);
    }

    @Test
    public void testMONDOU2() {
        MeszarosCase.doTest("mondou2.mps", "", "", ACCURACY);
    }

    @Test
    public void testMULTI() {
        MeszarosCase.doTest("multi.mps", "44404.676525921444", "1710682.4620709224", ACCURACY);
    }

    @Test
    public void testNSIC1() {
        MeszarosCase.doTest("nsic1.mps", "-9168554.0", "0.0", ACCURACY);
    }

    @Test
    public void testNSIC2() {
        MeszarosCase.doTest("nsic2.mps", "-8203512.0", "0.0", ACCURACY);
    }

    @Test
    public void testNUG05() {
        MeszarosCase.doTest("nug05.mps", "50.00", "90.00", ACCURACY);
    }

    @Test
    public void testNUG06() {
        MeszarosCase.doTest("nug06.mps", "86.00", "162.00", ACCURACY);
    }

    @Test
    public void testORSWQ2() {
        MeszarosCase.doTest("orswq2.mps", "0.48474294650012384", "21.12496288632921", ACCURACY);
    }

    @Test
    public void testP0033() {
        MeszarosCase.doTest("p0033.mps", "2520.5717391304333", "5299.698867924529", ACCURACY);
    }

    @Test
    public void testP0040() {
        MeszarosCase.doTest("p0040.mps", "61796.54505246026", "69540.52232082837", ACCURACY);
    }

    @Test
    public void testP0201() {
        MeszarosCase.doTest("p0201.mps", "6875", "15300", ACCURACY);
    }

    @Test
    public void testP0282() {
        MeszarosCase.doTest("p0282.mps", "176867.50334911313", "1117857.0", ACCURACY);
    }

    @Test
    public void testP0291() {
        MeszarosCase.doTest("p0291.mps", "1705.128761238722", "329881.9825", ACCURACY);
    }

    @Test
    public void testP0548() {
        MeszarosCase.doTest("p0548.mps", "315.25490196074924", "89460.04807308235", ACCURACY);
    }

    @Test
    public void testPANG() {
        MeszarosCase.doTest("pang.mps", "", "", ACCURACY);
    }

    @Test
    public void testPROBLEM() {
        MeszarosCase.doTest("problem.mps", "0.0", "", ACCURACY);
    }

    @Test
    public void testQUAL() {
        MeszarosCase.doTest("qual.mps", "", "", ACCURACY);
    }

    @Test
    public void testREACTOR() {
        MeszarosCase.doTest("reactor.mps", "", "", ACCURACY);
    }

    @Test
    public void testREFINE() {
        MeszarosCase.doTest("refine.mps", "-392691.8", "156296.6", ACCURACY);
    }

    @Test
    public void testREFINERY() {
        MeszarosCase.doTest("refinery.mps", "", "", ACCURACY);
    }

    @Test
    public void testSC205_2R_16() {
        MeszarosCase.doTest("sc205-2r-16.mps", "-55.38771399798585", "0.0", ACCURACY);
    }

    @Test
    public void testSC205_2R_27() {
        MeszarosCase.doTest("sc205-2r-27.mps", "-15.10574018126889", "0.0", ACCURACY);
    }

    @Test
    public void testSC205_2R_32() {
        MeszarosCase.doTest("sc205-2r-32.mps", "-55.38771399798587", "0.0", ACCURACY);
    }

    @Test
    public void testSC205_2R_4() {
        MeszarosCase.doTest("sc205-2r-4.mps", "-60.42296072507552", "0.0", ACCURACY);
    }

    @Test
    public void testSC205_2R_8() {
        MeszarosCase.doTest("sc205-2r-8.mps", "-60.4229607250755", "0.0", ACCURACY);
    }

    @Test
    public void testSCAGR7_2B_16() {
        MeszarosCase.doTest("scagr7-2b-16.mps", "-832902.1508426666", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2B_4() {
        MeszarosCase.doTest("scagr7-2b-4.mps", "-832941.3274346666", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2C_16() {
        MeszarosCase.doTest("scagr7-2c-16.mps", "-832260.0342453334", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2C_4() {
        MeszarosCase.doTest("scagr7-2c-4.mps", "-832260.0342453332", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2R_16() {
        MeszarosCase.doTest("scagr7-2r-16.mps", "-832902.1512346667", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2R_4() {
        MeszarosCase.doTest("scagr7-2r-4.mps", "-832902.1508426665", "", ACCURACY);
    }

    @Test
    public void testSCAGR7_2R_8() {
        MeszarosCase.doTest("scagr7-2r-8.mps", "-832902.1508426666", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2B_16() {
        MeszarosCase.doTest("scrs8-2b-16.mps", "112.104275504", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2B_4() {
        MeszarosCase.doTest("scrs8-2b-4.mps", "112.1042754", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2C_16() {
        MeszarosCase.doTest("scrs8-2c-16.mps", "112.431725508", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2C_4() {
        MeszarosCase.doTest("scrs8-2c-4.mps", "112.1042754", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2C_8() {
        MeszarosCase.doTest("scrs8-2c-8.mps", "112.41590875", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2R_16() {
        MeszarosCase.doTest("scrs8-2r-16.mps", "123.1765647328", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2R_4() {
        MeszarosCase.doTest("scrs8-2r-4.mps", "123.17656468", "", ACCURACY);
    }

    @Test
    public void testSCRS8_2R_8() {
        MeszarosCase.doTest("scrs8-2r-8.mps", "1122.9708421736038", "", ACCURACY);
    }

    @Test
    public void testSCSD8_2B_4() {
        MeszarosCase.doTest("scsd8-2b-4.mps", "15.25", "", ACCURACY);
    }

    @Test
    public void testSCSD8_2C_4() {
        MeszarosCase.doTest("scsd8-2c-4.mps", "15.0", "", ACCURACY);
    }

    @Test
    public void testSCSD8_2R_4() {
        MeszarosCase.doTest("scsd8-2r-4.mps", "15.5", "", ACCURACY);
    }

    @Test
    public void testSCTAP1_2B_4() {
        MeszarosCase.doTest("sctap1-2b-4.mps", "239.25", "", ACCURACY);
    }

    @Test
    public void testSCTAP1_2C_4() {
        MeszarosCase.doTest("sctap1-2c-4.mps", "236.25", "", ACCURACY);
    }

    @Test
    public void testSCTAP1_2R_4() {
        MeszarosCase.doTest("sctap1-2r-4.mps", "280.5", "", ACCURACY);
    }

    @Test
    public void testVOL1() {
        MeszarosCase.doTest("vol1.mps", "", "", ACCURACY);
    }

    @Test
    public void testWOODINFE() {
        MeszarosCase.doTest("woodinfe.mps", "", "", ACCURACY);
    }

    @Test
    public void testZED() {
        MeszarosCase.doTest("zed.mps", "-15060.64524063498", "-1472.513052630716", ACCURACY);
    }

}
