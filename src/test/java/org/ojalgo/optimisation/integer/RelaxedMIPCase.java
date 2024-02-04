/*
 * Copyright 1997-2024 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software", to deal
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

public class RelaxedMIPCase extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8, 6);

    private static void doTest(final String name, final String expMinValString, final String expMaxValString, final Map<String, BigDecimal> solution) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", name, true);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, ACCURACY);
    }

    /**
     * https://miplib.zib.de/instance_details_b-ball.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s suffice with optimal solution</li>
     * </ul>
     */
    @Test
    public void testB_ball() {
        RelaxedMIPCase.doTest("b-ball.mps", "-1.818181818181818", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_ej.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 900s terminated without finding any feasible solution</li>
     * </ul>
     */
    @Test
    public void testEj() {
        RelaxedMIPCase.doTest("ej.mps", "1", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_flugpl.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 1s finsihed with optimal solution</li>
     * </ul>
     */
    @Test
    public void testFlugpl() {
        RelaxedMIPCase.doTest("flugpl.mps", "1167185.7255923203", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip002.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s expected: <-4783.733392> but was: <-4778.1844607></li>
     * </ul>
     */
    @Test
    public void testGen_ip002() {
        RelaxedMIPCase.doTest("gen-ip002.mps", "-4840.541961300889", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip021.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s expected: <2361.45419519> but was: <2362.7631500641996></li>
     * </ul>
     */
    @Test
    public void testGen_ip021() {
        RelaxedMIPCase.doTest("gen-ip021.mps", "2327.84258448875", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip036.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s expected: <-4606.67961> but was: <-4602.60643892></li>
     * </ul>
     */
    @Test
    public void testGen_ip036() {
        RelaxedMIPCase.doTest("gen-ip036.mps", "-4632.29815287346", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip054.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s expected: <6840.966> but was: <6852.1883509></li>
     * </ul>
     */
    @Test
    public void testGen_ip054() {
        RelaxedMIPCase.doTest("gen-ip054.mps", "6765.2090427", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gr4x6.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 0s finsihed with optimal solution</li>
     * </ul>
     */
    @Test
    public void testGr4x6() {
        RelaxedMIPCase.doTest("gr4x6.mps", "185.55", null, null);
    }

    /**
     * There are 2 different objective function rows, "N F52" and "N F53". Must pick and use "F52".
     */
    @Test
    public void testMad() {
        RelaxedMIPCase.doTest("mad.mps", "0", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_4_0.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 15s finsihed with optimal solution</li>
     * </ul>
     */
    @Test
    public void testMarkshare_4_0() {
        RelaxedMIPCase.doTest("markshare_4_0.mps", "0", null, null);
    }

    /**
     * <p>
     * <a href="http://miplib.zib.de/miplib2010/markshare_5_0.php">MIPLIB 2010</a>
     * https://miplib.zib.de/instance_details_markshare_5_0.html
     * </p>
     * <p>
     * N/A in MIPLIB 2003
     * </p>
     * <p>
     * LP: 0.00000000e+00
     * </p>
     * <p>
     * MIP: 1.00000000e+00
     * </p>
     */
    @Test
    public void testMarkshare_5_0() {
        RelaxedMIPCase.doTest("markshare_5_0.mps", "0.00000000e+00", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare1.html
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/markshare1.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 0.00000000e+00
     * </p>
     * <p>
     * MIP: 1.00000000e+00
     * </p>
     */
    @Test
    public void testMarkshare1() {
        RelaxedMIPCase.doTest("markshare1.mps", "0", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/markshare2.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 0.00000000e+00
     * </p>
     * <p>
     * MIP: 1.00000000e+00
     * </p>
     */
    @Test
    public void testMarkshare2() {
        RelaxedMIPCase.doTest("markshare2.mps", "0", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/mas76.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 3.88939036e+04
     * </p>
     * <p>
     * MIP: 4.00050541e+04
     * </p>
     */
    @Test
    public void testMas76() {
        RelaxedMIPCase.doTest("mas76.mps", "3.88939036e+04", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/modglob.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 2.04309476e+07
     * </p>
     * <p>
     * MIP: 2.07405081e+07
     * </p>
     */
    @Test
    public void testModglob() {
        RelaxedMIPCase.doTest("modglob.mps", "2.04309476e+07", null, null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos5.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s suffice with optimal solution</li>
     * </ul>
     */
    @Test
    public void testNeos5() {
        RelaxedMIPCase.doTest("neos5.mps", "13", null, null);
    }

    /**
     * <p>
     * <a href="http://miplib.zib.de/miplib2010/neos-911880.php">MIPLIB 2010</a>
     * </p>
     * <p>
     * N/A in MIPLIB 2003
     * </p>
     * <p>
     * LP: 23.26
     * </p>
     * <p>
     * MIP: 54.76
     * </p>
     */
    @Test
    @Tag("new_lp_problem")
    public void testNeos911880() {
        RelaxedMIPCase.doTest("neos-911880.mps", "23.26", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/noswot.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: -4.30000000e+01
     * </p>
     * <p>
     * MIP: -4.10000000e+01
     * </p>
     */
    @Test
    public void testNoswot() {
        RelaxedMIPCase.doTest("noswot.mps", "-43", null, null);
    }

    /**
     * <p>
     * <a href="http://miplib.zib.de/miplib2010/p2m2p1m1p0n100.php">MIPLIB 2010</a>
     * </p>
     * <p>
     * N/A in MIPLIB 2003
     * </p>
     * <p>
     * LP: 80424
     * </p>
     * <p>
     * MIP: Infeasible
     * </p>
     */
    @Test
    public void testP2m2p1m1p0n100() {
        RelaxedMIPCase.doTest("p2m2p1m1p0n100.mps", "80424", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/pk1.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 1.47389881e-09
     * </p>
     * <p>
     * MIP: 1.10000000e+01
     * </p>
     * https://miplib.zib.de/instance_details_pk1.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2013-04-01: (suffice=4h abort=8h) Stopped with optimal integer solution after 1h50min</li>
     * <li>2013-12-08: (suffice=4h abort=8h) Stopped with optimal integer solution after 412s</li>
     * <li>2015-11-07: (suffice=4h abort=8h) Stopped with optimal integer solution after 372s</li>
     * <li>2017-10-20: (suffice=4h abort=8h) Stopped with optimal integer solution after 796s</li>
     * <li>2017-10-20: (suffice=5min abort=1h) Stopped with optimal integer solution after 5min</li>
     * <li>2018-02-07: (suffice=5min, abort=15min, mip_gap=0.001) Suffice with optimal solution</li>
     * <li>2018-02-07: (suffice=15min, abort=15min, mip_gap=0.001) Found optimal solution in 344s</li>
     * <li>2018-04-47: (suffice=5min, abort=15min, mip_gap=0.001) Found optimal solution in 227s</li>
     * <li>2018-08-16: sufficed: <11.0> but was: <14.0></li>
     * <li>2019-01-28: 300s expected: <11.0> but was: <11.999999999999979></li>
     * </ul>
     */
    @Test
    public void testPk1() {

        // Solution obtained from lp_solve_5.5.2.0 with relaxed integer constraints
        // apete$ lp_solve -noint -mps ./pk1.mps
        final HashMap<String, BigDecimal> tmpSolution = new HashMap<>();

        tmpSolution.put("x1", BigDecimal.valueOf(0));
        tmpSolution.put("x2", BigDecimal.valueOf(0.600797));
        tmpSolution.put("x3", BigDecimal.valueOf(0.321453));
        tmpSolution.put("x4", BigDecimal.valueOf(0));
        tmpSolution.put("x5", BigDecimal.valueOf(0.00226982));
        tmpSolution.put("x6", BigDecimal.valueOf(1));
        tmpSolution.put("x7", BigDecimal.valueOf(1));
        tmpSolution.put("x8", BigDecimal.valueOf(0.294429));
        tmpSolution.put("x9", BigDecimal.valueOf(0));
        tmpSolution.put("x10", BigDecimal.valueOf(1));
        tmpSolution.put("x11", BigDecimal.valueOf(0));
        tmpSolution.put("x12", BigDecimal.valueOf(1));
        tmpSolution.put("x13", BigDecimal.valueOf(0));
        tmpSolution.put("x14", BigDecimal.valueOf(0));
        tmpSolution.put("x15", BigDecimal.valueOf(0));
        tmpSolution.put("x16", BigDecimal.valueOf(0.460606));
        tmpSolution.put("x17", BigDecimal.valueOf(1));
        tmpSolution.put("x18", BigDecimal.valueOf(0.968373));
        tmpSolution.put("x19", BigDecimal.valueOf(0));
        tmpSolution.put("x20", BigDecimal.valueOf(1));
        tmpSolution.put("x21", BigDecimal.valueOf(0));
        tmpSolution.put("x22", BigDecimal.valueOf(0.997254));
        tmpSolution.put("x23", BigDecimal.valueOf(0.0698961));
        tmpSolution.put("x24", BigDecimal.valueOf(0.409359));
        tmpSolution.put("x25", BigDecimal.valueOf(1));
        tmpSolution.put("x26", BigDecimal.valueOf(1));
        tmpSolution.put("x27", BigDecimal.valueOf(0.330775));
        tmpSolution.put("x28", BigDecimal.valueOf(0));
        tmpSolution.put("x29", BigDecimal.valueOf(0));
        tmpSolution.put("x30", BigDecimal.valueOf(0.698349));
        tmpSolution.put("x31", BigDecimal.valueOf(0));
        tmpSolution.put("x32", BigDecimal.valueOf(0));
        tmpSolution.put("x33", BigDecimal.valueOf(0));
        tmpSolution.put("x34", BigDecimal.valueOf(0.13724));
        tmpSolution.put("x35", BigDecimal.valueOf(1));
        tmpSolution.put("x36", BigDecimal.valueOf(0.927129));
        tmpSolution.put("x37", BigDecimal.valueOf(1));
        tmpSolution.put("x38", BigDecimal.valueOf(1));
        tmpSolution.put("x39", BigDecimal.valueOf(0));
        tmpSolution.put("x40", BigDecimal.valueOf(0));
        tmpSolution.put("x41", BigDecimal.valueOf(0.381025));
        tmpSolution.put("x42", BigDecimal.valueOf(0));
        tmpSolution.put("x43", BigDecimal.valueOf(0));
        tmpSolution.put("x44", BigDecimal.valueOf(1));
        tmpSolution.put("x45", BigDecimal.valueOf(1));
        tmpSolution.put("x46", BigDecimal.valueOf(0));
        tmpSolution.put("x47", BigDecimal.valueOf(0));
        tmpSolution.put("x48", BigDecimal.valueOf(0.947062));
        tmpSolution.put("x49", BigDecimal.valueOf(0));
        tmpSolution.put("x50", BigDecimal.valueOf(0));
        tmpSolution.put("x51", BigDecimal.valueOf(0));
        tmpSolution.put("x52", BigDecimal.valueOf(1));
        tmpSolution.put("x53", BigDecimal.valueOf(1));
        tmpSolution.put("x54", BigDecimal.valueOf(0));
        tmpSolution.put("x55", BigDecimal.valueOf(1));
        tmpSolution.put("x56", BigDecimal.valueOf(0));
        tmpSolution.put("x57", BigDecimal.valueOf(0));
        tmpSolution.put("x58", BigDecimal.valueOf(0));
        tmpSolution.put("x59", BigDecimal.valueOf(0));
        tmpSolution.put("x60", BigDecimal.valueOf(0));
        tmpSolution.put("x61", BigDecimal.valueOf(0));
        tmpSolution.put("x62", BigDecimal.valueOf(0));
        tmpSolution.put("x63", BigDecimal.valueOf(0));
        tmpSolution.put("x64", BigDecimal.valueOf(0));
        tmpSolution.put("x65", BigDecimal.valueOf(0));
        tmpSolution.put("x66", BigDecimal.valueOf(0));
        tmpSolution.put("x67", BigDecimal.valueOf(0));
        tmpSolution.put("x68", BigDecimal.valueOf(0));
        tmpSolution.put("x69", BigDecimal.valueOf(0));
        tmpSolution.put("x70", BigDecimal.valueOf(0));
        tmpSolution.put("x71", BigDecimal.valueOf(0));
        tmpSolution.put("x72", BigDecimal.valueOf(0));
        tmpSolution.put("x73", BigDecimal.valueOf(0));
        tmpSolution.put("x74", BigDecimal.valueOf(0));
        tmpSolution.put("x75", BigDecimal.valueOf(0));
        tmpSolution.put("x76", BigDecimal.valueOf(0));
        tmpSolution.put("x77", BigDecimal.valueOf(0));
        tmpSolution.put("x78", BigDecimal.valueOf(0));
        tmpSolution.put("x79", BigDecimal.valueOf(0));
        tmpSolution.put("x80", BigDecimal.valueOf(0));
        tmpSolution.put("x81", BigDecimal.valueOf(0));
        tmpSolution.put("x82", BigDecimal.valueOf(0));
        tmpSolution.put("x83", BigDecimal.valueOf(0));
        tmpSolution.put("x84", BigDecimal.valueOf(0));
        tmpSolution.put("x85", BigDecimal.valueOf(0));
        tmpSolution.put("x86", BigDecimal.valueOf(0));

        RelaxedMIPCase.doTest("pk1.mps", "0", null, tmpSolution);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/pp08a.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 2.74834524e+03
     * </p>
     * <p>
     * MIP: 7.35000000e+03
     * </p>
     */
    @Test
    public void testPp08a() {
        RelaxedMIPCase.doTest("pp08a.mps", "2.74834524e+03", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/pp08aCUTS.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 5.48060616e+03
     * </p>
     * <p>
     * MIP: 7.35000000e+03
     * </p>
     */
    @Test
    public void testPp08aCUTS() {
        RelaxedMIPCase.doTest("pp08aCUTS.mps", "5.48060616e+03", null, null);
    }

    /**
     * <p>
     * <a href="http://miplib.zib.de/miplib2010/timtab1.php">MIPLIB 2010</a>
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/timtab1.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 28694
     * </p>
     * <p>
     * MIP: 764772
     * </p>
     */
    @Test
    public void testTimtab1() {
        RelaxedMIPCase.doTest("timtab1.mps", "2.86940000e+04", null, null);
    }

    /**
     * <p>
     * N/A in MIPLIB 2010
     * </p>
     * <p>
     * <a href="http://miplib.zib.de/miplib2003/miplib2003/vpm2.php">MIPLIB 2003</a>
     * </p>
     * <p>
     * LP: 9.88926460e+00
     * </p>
     * <p>
     * MIP: 1.37500000e+01
     * </p>
     */
    @Test
    public void testVpm2() {
        RelaxedMIPCase.doTest("vpm2.mps", "9.88926460e+00", null, null);
    }

}
