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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public final class MipCase extends OptimisationIntegerTests {

    @Test
    public void testDummy() {
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal solution after 4h</li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 2.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with optimal solution</li>
     * </ul>
     *
     * @see RelaxedLpCase#testMarkshare_5_0()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testMarkshare_5_0() {
        MipLibCase.assertMinMaxVal("markshare_5_0.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with integer solution 6.0 rather than 1.0 after 4h
     * expected:<1.0> but was:<5.999999999999929></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 5.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<1.0> but
     * was:<5.0></li>
     * </ul>
     *
     * @see RelaxedLpCase#testMarkshare1()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testMarkshare1() {
        MipLibCase.assertMinMaxVal("markshare1.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with integer solution 14.0 rather than 1.0
     * solution after 4h expected:<1.0> but was:<14.000000000000192></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 19.0 rather than 1.0 after
     * 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<1.0> but
     * was:<19.00000000000008></li>
     * </ul>
     *
     * @see RelaxedLpCase#testMarkshare2()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testMarkshare2() {
        MipLibCase.assertMinMaxVal("markshare2.mps", new BigDecimal("1.00000000e+00"), null, false, null);
    }

    /**
     * <ul>
     * <li>2012-11-30: Solved to optimality 40005.05414200003. Don't now how fast, but within 3 hours.</li>
     * <li>2013-03-28: (MacPro) Had a time limit of 1h and that resulted in 40116.054142000015 rather than
     * 40005.0541</li>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 4h</li>
     * <li>2013-12-10: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 29min</li>
     * <li>2015-08-28: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 97min</li>
     * <li>2015-11-07: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 107min</li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with optimal integer solution after 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with optimal solution</li>
     * </ul>
     *
     * @see RelaxedLpCase#testMas76()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testMas76() {
        MipLibCase.assertMinMaxVal("mas76.mps", new BigDecimal("4.00050541e+04"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<2.07405081E7> but was:
     * <2.2769621121583242E7></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with integer solution 2.626301334355273E7 rather
     * than 2.07405081E7 after 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<2.07405081E7>
     * but was:<2.45509922222574E7></li>
     * </ul>
     *
     * @see RelaxedLpCase#testModglob()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testModglob() {
        MipLibCase.assertMinMaxVal("modglob.mps", new BigDecimal("2.07405081e+07"), null, false, null);
    }

    /**
     * <ul>
     * <li>2015-02-08: MacPro (suffice=4h abort=8h) Stopped after 4.75h: expected:<54.76> but was:<220.69>
     * </li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Aborted with no integer solution</li>
     * </ul>
     *
     * @see RelaxedLpCase#testNeos911880()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testNeos911880() {
        MipLibCase.assertMinMaxVal("neos-911880.mps", new BigDecimal("54.76"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 4h</li>
     * <li>2013-11-29: MacPro (suffice=4h abort=8h) Stopped after 4h: expected:<-41.0> but was:<-40.0></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped with optimal integer solution after 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with optimal solution</li>
     * </ul>
     *
     * @see RelaxedLpCase#testNoswot()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testNoswot() {
        MipLibCase.assertMinMaxVal("noswot.mps", new BigDecimal("-4.10000000e+01"), null, false, null);
    }

    /**
     * <ul>
     * <li>Doesn't work</li>
     * </ul>
     *
     * @see RelaxedLpCase#testP2m2p1m1p0n100()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testP2m2p1m1p0n100() {
        MipLibCase.assertMinMaxVal("p2m2p1m1p0n100.mps", new BigDecimal("Infeasible"), null, true, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 1h50min</li>
     * <li>2013-12-08: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 412s</li>
     * <li>2015-11-07: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 372s</li>
     * <li>2017-10-20: MacPro (suffice=4h abort=8h) Stopped with optimal integer solution after 796s</li>
     * <li>2017-10-20: MacPro (suffice=5min abort=1h) Stopped with optimal integer solution after 5min</li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Suffice with optimal solution</li>
     * <li>2018-02-07: MacPro (suffice=15min, abort=15min, mip_gap=0.001) Found optimal solution in 344s</li>
     * </ul>
     *
     * @see RelaxedLpCase#testPk1()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testPk1() {
        MipLibCase.assertMinMaxVal("pk1.mps", new BigDecimal("1.10000000e+01"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<7350.0> but was:<9180.0></li>
     * <li>2013-11-30: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<7350.0> but was:<8020.0></li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<7350.0> but
     * was:<7580.0></li>
     * </ul>
     *
     * @see RelaxedLpCase#testPp08a()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testPp08a() {
        MipLibCase.assertMinMaxVal("pp08a.mps", new BigDecimal("7.35000000e+03"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<7350.0> but was:<8080.0></li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<7350.0> but
     * was:<7500.0></li>
     * </ul>
     *
     * @see RelaxedLpCase#testPp08aCUTS()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testPp08aCUTS() {
        MipLibCase.assertMinMaxVal("pp08aCUTS.mps", new BigDecimal("7.35000000e+03"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-11-24: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<764772.0> but was:
     * <1012900.999999></li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Aborted with no integer solution</li>
     * </ul>
     *
     * @see RelaxedLpCase#testTimtab1()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testTimtab1() {
        MipLibCase.assertMinMaxVal("timtab1.mps", new BigDecimal("7.64772000e+05"), null, false, null);
    }

    /**
     * <ul>
     * <li>2013-04-01: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<13.75> but was:<20.25></li>
     * <li>2013-11-24: MacPro (suffice=4h abort=8h) Stopped after 4h expected:<13.75> but was:<16.5></li>
     * <li>2018-01-08: MacPro (suffice=5min abort=1h) Stopped after 5min expected:<13.75> but was:<16.25></li>
     * <li>2018-02-07: MacPro (suffice=5min, abort=15min, mip_gap=0.001) Sufficed with expected:<13.75> but
     * was:<16.0></li>
     * </ul>
     *
     * @see RelaxedLpCase#testVpm2()
     */
    @Test
    @Disabled("Underscored before JUnit 5")
    public void testVpm2() {
        MipLibCase.assertMinMaxVal("vpm2.mps", new BigDecimal("1.37500000e+01"), null, false, null);
    }

}
