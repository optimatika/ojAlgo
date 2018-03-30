/*
 * Copyright 1997-2018 Optimatika
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

import java.math.BigDecimal;
import java.util.HashMap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public final class RelaxedLpCase extends MipLibCase {

    /**
     * <p>
     * <a href="http://miplib.zib.de/miplib2010/markshare_5_0.php">MIPLIB 2010</a>
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
        MipLibCase.assertMinMaxVal("markshare_5_0.mps", new BigDecimal("0.00000000e+00"), null, true, null);
    }

    /**
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
        MipLibCase.assertMinMaxVal("markshare1.mps", new BigDecimal("0.00000000e+00"), null, true, null);
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
        MipLibCase.assertMinMaxVal("markshare2.mps", new BigDecimal("0.00000000e+00"), null, true, null);
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
        MipLibCase.assertMinMaxVal("mas76.mps", new BigDecimal("3.88939036e+04"), null, true, null);
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
        MipLibCase.assertMinMaxVal("modglob.mps", new BigDecimal("2.04309476e+07"), null, true, null);
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
    public void testNeos911880() {
        MipLibCase.assertMinMaxVal("neos-911880.mps", new BigDecimal("23.26"), null, true, null);
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
        MipLibCase.assertMinMaxVal("noswot.mps", new BigDecimal("-43.0"), null, true, null);

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
        MipLibCase.assertMinMaxVal("p2m2p1m1p0n100.mps", new BigDecimal("80424"), null, true, null);
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
     */
    @Test
    @Disabled("Underscored before JUnit 5")
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

        MipLibCase.assertMinMaxVal("pk1.mps", new BigDecimal("1.47389881e-09"), null, true, tmpSolution);
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
        MipLibCase.assertMinMaxVal("pp08a.mps", new BigDecimal("2.74834524e+03"), null, true, null);
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
        MipLibCase.assertMinMaxVal("pp08aCUTS.mps", new BigDecimal("5.48060616e+03"), null, true, null);
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
        MipLibCase.assertMinMaxVal("timtab1.mps", new BigDecimal("2.86940000e+04"), null, true, null);
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
        MipLibCase.assertMinMaxVal("vpm2.mps", new BigDecimal("9.88926460e+00"), null, true, null);
    }

}
