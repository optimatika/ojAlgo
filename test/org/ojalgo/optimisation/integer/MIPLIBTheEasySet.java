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
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2019-01-28: 300s suffice with optimal solution</li>
     * </ul>
     */
    @Test
    public void testB_ball() {
        AbstractCaseFileMPS.assertMinMaxVal("b-ball.mps", new BigDecimal("-1.5"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("ej.mps", new BigDecimal("25508"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("flugpl.mps", new BigDecimal("1201500"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip002.mps", new BigDecimal("-4783.733392"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip021.mps", new BigDecimal("2361.45419519"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip036.mps", new BigDecimal("-4606.67961"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("gen-ip054.mps", new BigDecimal("6840.966"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("gr4x6.mps", new BigDecimal("202.35"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("markshare_4_0.mps", new BigDecimal("1"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare_5_0.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2013-04-01: (suffice=4h abort=8h) Stopped with optimal solution after 4h</li>
     * <li>2018-01-08: (suffice=5min abort=1h) Stopped with integer solution 2.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with optimal solution</li>
     * <li>2018-08-16: sufficed with optimal solution</li>
     * <li>2019-01-28: 300s expected: <1.0> but was: <1.9999999999999953></li>
     * </ul>
     */
    @Test
    public void testMarkshare_5_0() {
        AbstractCaseFileMPS.assertMinMaxVal("markshare_5_0.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * https://miplib.zib.de/instance_details_markshare1.html
     * <ul>
     * Mac Pro (Early 2009)
     * <li>2013-04-01: (suffice=4h abort=8h) Stopped with integer solution 6.0 rather than 1.0 after 4h
     * expected:<1.0> but was:<5.999999999999929></li>
     * <li>2018-01-08: (suffice=5min abort=1h) Stopped with integer solution 5.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<1.0> but
     * was:<5.0></li>
     * <li>2018-08-16: sufficed: <1.0> but was: <8.0></li>
     * <li>2019-01-28: 300s expected: <1.0> but was: <6.000000000000018></li>
     * </ul>
     */
    @Test
    public void testMarkshare1() {
        AbstractCaseFileMPS.assertMinMaxVal("markshare1.mps", new BigDecimal("1.00000000e+00"), null, false, null);
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
        AbstractCaseFileMPS.assertMinMaxVal("neos5.mps", new BigDecimal("15"), null, false, null);
    }

    /**
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
        AbstractCaseFileMPS.assertMinMaxVal("pk1.mps", new BigDecimal("1.10000000e+01"), null, false, null);
    }

}
