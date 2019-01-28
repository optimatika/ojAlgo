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
package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

/**
 * Problems/models from "The Easy Set" - instances that could be solved within less than 1h (by top tier
 * native code solvers on modern powerful hardware). https://miplib.zib.de/set_easy.html Here we limit the set
 * to instances with <=100 variables and constraints that are not infeasible.
 *
 * @author apete
 */
public final class MIPLIBTheEasySet extends AbstractCaseFileMPS {

    /**
     * https://miplib.zib.de/instance_details_b-ball.html
     */
    @Test
    public void testB_ball() {
        AbstractCaseFileMPS.assertMinMaxVal("b-ball.mps", new BigDecimal("-1.5"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_ej.html
     */
    @Test
    public void testEj() {
        AbstractCaseFileMPS.assertMinMaxVal("ej.mps", new BigDecimal("25508"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_flugpl.html
     */
    @Test
    public void testFlugpl() {
        AbstractCaseFileMPS.assertMinMaxVal("flugpl.mps", new BigDecimal("1201500"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip002.html
     * <ul>
     * <li>2019-01-28: MacPro sufficed 300s - expected: <-4783.733392> but was: <-4763.2729736></li>
     * </ul>
     */
    @Test
    public void testGen_ip002() {
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip002.mps", new BigDecimal("-4783.733392"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip021.html
     */
    @Test
    public void testGen_ip021() {
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip021.mps", new BigDecimal("2361.45419519"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip036.html
     */
    @Test
    public void testGen_ip036() {
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip036.mps", new BigDecimal("-4606.67961"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gen-ip054.html
     * <ul>
     * <li>2019-01-28: MacPro sufficed 300s - expected: <6840.966> but was: <6852.406055845></li>
     * </ul>
     */
    @Test
    public void testGen_ip054() {
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip054.mps", new BigDecimal("6840.966"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_gr4x6.html
     */
    @Test
    public void testGr4x6() {
        AbstractCaseFileMPS.assertMinMaxVal("gr4x6.mps", new BigDecimal("202.349999999998"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_4_0.html
     * <ul>
     * <li>2019-01-28: MacPro found optimal solution in 19s</li>
     * </ul>
     */
    @Test
    public void testMarkshare_4_0() {
        AbstractCaseFileMPS.assertMinMaxVal("markshare_4_0.mps", new BigDecimal("1"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_5_0.html
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal solution after 4h</li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 2.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with optimal solution</li>
     * <li>2018-08-16: MacPro sufficed with optimal solution</li>
     * <li>2019-01-28: MacPro sufficed 300s - <1.0> but was: <4.999999999999991></li>
     * </ul>
     *
     * @see RelaxedLpCase#testMarkshare_5_0()
     */
    @Test
    public void testMarkshare_5_0() {
        AbstractCaseFileMPS.assertMinMaxVal("markshare_5_0.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare1.html
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with integer solution 6.0 rather than 1.0 after 4h
     * expected:<1.0> but was:<5.999999999999929></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 5.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<1.0> but
     * was:<5.0></li>
     * <li>2018-08-16: MacPro sufficed: <1.0> but was: <8.0></li>
     * <li>2019-01-28: MacPro sufficed 300s - expected: <1.0> but was: <8.000000000000066></li>
     * </ul>
     *
     * @see RelaxedLpCase#testMarkshare1()
     */
    @Test
    public void testMarkshare1() {
        AbstractCaseFileMPS.assertMinMaxVal("markshare1.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_neos5.html
     * <ul>
     * <li>2019-01-28: MacPro sufficed with optimal solution after 300s</li>
     * </ul>
     */
    @Test
    public void testNeos5() {
        AbstractCaseFileMPS.assertMinMaxVal("neos5.mps", new BigDecimal("15"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_pk1.html
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 1h50min</li>
     * <li>2013-12-08: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 412s</li>
     * <li>2015-11-07: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 372s</li>
     * <li>2017-10-20: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 796s</li>
     * <li>2017-10-20: MacPro (suffice=5min abort=1h) Stopped with optimal integer solution after 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Suffice with optimal solution</li>
     * <li>2018-02-07: MacPro (suffice=15min, abort=15min, mip_gap=0.001) Found optimal solution in 344s</li>
     * <li>2018-04-47: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Found optimal solution in 227s</li>
     * <li>2018-08-16: MacPro sufficed: <11.0> but was: <14.0></li>
     * <li>2019-01-28: MacPro sufficed 300s: expected: <11.0> but was: <14.000000000000073></li>
     * </ul>
     *
     * @see RelaxedLpCase#testPk1()
     */
    @Test
    public void testPk1() {
        AbstractCaseFileMPS.assertMinMaxVal("pk1.mps", new BigDecimal("1.10000000e+01"), null, false, null);
    }

}
