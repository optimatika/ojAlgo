/*
 * Copyright 1997-2025 Optimatika
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * The CUTE/netlib collection of linear programming test problems
 * (<a href="http://www.numerical.rl.ac.uk/cute/netlib.html">www.numerical.rl.ac.uk/cute/netlib.html</a>), an
 * industry-standard benchmark suite. Each test corresponds to one SIF file under
 * {@code src/test/resources/optimisation/netlib/}.
 * <p>
 * Per-problem expected min/max values were verified with CPLEX or OR-Tools (GLOP). Per-problem javadoc lists
 * the constraint matrix size and non-zero count from the netlib README's PROBLEM SUMMARY TABLE, any
 * variable-bound or constraint-range markers (B/R/BR), the bound types declared in the SIF (UP/LO/FX/FR), and
 * the published optimal value.
 * <ul>
 * <li>{@code @Disabled} — neither CPLEX nor OR-Tools produced a reference solution, so we have nothing to
 * compare against.
 * <li>{@code @Tag("slow")} — solves but takes too long to run as a default unit test.
 * <li>{@code @Tag("unstable")} — solves but the answer differs from CPLEX/OR-Tools.
 * </ul>
 *
 * @author apete
 */
@Execution(ExecutionMode.SAME_THREAD)
public class CuteNetlibCase extends OptimisationLinearTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(6, 4);

    private static void doTest(final String name, final String expMinValString, final String expMaxValString, final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", name, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(LinearSolver.class);
        // model.options.validate = true;
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        //        model.options.linear().dual();
        //        model.options.sparse = Boolean.TRUE;

        // long time = 10_000L;
        // model.options.time_abort = time;
        // model.options.time_suffice = time;

        //        if (model.countVariables() > 10_000 || model.countExpressions() > 10_000) {
        //            BasicLogger.debug(name);
        //            throw new RuntimeException();
        //        }

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);
    }

    /**
     * 25FV47 — 822 rows × 1571 columns, 11127 non-zeros.<br>
     * Optimal value: 5.5018458883E+03.<br>
     */
    @Test
    public void test25FV47() {
        CuteNetlibCase.doTest("25FV47.SIF", "5501.845888286646", null, ACCURACY);
    }

    /**
     * 80BAU3B — 2263 rows × 9799 columns, 29063 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: 9.8723216072E+05.<br>
     */
    @Test
    public void test80BAU3B() {
        CuteNetlibCase.doTest("80BAU3B.SIF", "987224.1924090903", null, ACCURACY);
    }

    /**
     * ADLITTLE — 57 rows × 97 columns, 465 non-zeros.<br>
     * Optimal value: 2.2549496316E+05.<br>
     */
    @Test
    public void testADLITTLE() {
        CuteNetlibCase.doTest("ADLITTLE.SIF", "225494.96316238036", null, ACCURACY);
    }

    /**
     * AFIRO — 28 rows × 32 columns, 88 non-zeros.<br>
     * Optimal value: -4.6475314286E+02.<br>
     */
    @Test
    public void testAFIRO() {

        // CPLEX MIN OPTIMAL -464.7531428571429 @ { 8E+1, 25.5, 54.5, 84.80, 18.21428571428572, 0, 0, 0, 0, 0,
        // 0, 0, 18.21428571428572, 0, 19.30714285714286, 5E+2, 475.92, 24.08, 0, 215, 0, 0, 0, 0, 0, 0, 0, 0,
        // 339.9428571428572, 383.9428571428572, 0, 0 }

        // Before shift
        // [80.0, 25.499999999999993, 54.5, 0.0, 18.214285714285708, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 18.214285714285708, 0.0, 19.30714285714285, 0.0, 651.9200000000001, 24.079999999999995, 0.0,
        // 214.99999999999997, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 465.65714285714296, 561.6571428571428,
        // 0.0, 0.0, 0.0, 0.0, 512.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 556.1746428571429, 0.0,
        // 280.6928571428572, 0.0, 61.78571428571429, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]

        // After shift
        // [80.0, 25.499999999999993, 54.5, 84.8, 18.214285714285708, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 18.214285714285708, 0.0, 19.30714285714285, 500.0, 651.9200000000001, 24.079999999999995, 0.0,
        // 214.99999999999997, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 465.65714285714296, 561.6571428571428,
        // 0.0, 0.0, 0.0, 0.0, 512.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 556.1746428571429, 0.0,
        // 280.6928571428572, 0.0, 61.78571428571429, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        // OPTIMAL -819.096 @ { 80.0, 25.499999999999993, 54.5, 84.8, 18.214285714285708, 0.0, 0.0, 0.0, 0.0,
        // 0.0, 0.0, 0.0, 18.214285714285708, 0.0, 19.30714285714285, 500.0, 651.9200000000001,
        // 24.079999999999995, 0.0, 214.99999999999997, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 465.65714285714296, 561.6571428571428, 0.0, 0.0, 0.0, 0.0, 512.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 0.0, 0.0, 556.1746428571429, 0.0, 280.6928571428572, 0.0, 61.78571428571429, 0.0, 0.0, 0.0, 0.0,
        // 0.0, 0.0, 0.0, 0.0 }

        // Mapped
        // OPTIMAL NaN @ { 80.0, 25.499999999999993, 54.5, 84.8, 18.214285714285708, 0.0, 0.0, 0.0, 0.0, 0.0,
        // 0.0, 0.0, 18.214285714285708, 0.0, 19.30714285714285, 500.0, 651.9200000000001, 24.079999999999995,
        // 0.0, 214.99999999999997, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 465.65714285714296,
        // 561.6571428571428, 0.0, 0.0 }

        // Result
        // OPTIMAL -630.6960000000001 @ { 8E+1, 25.49999999999999, 54.5, 84.8, 18.21428571428571, 0, 0, 0, 0,
        // 0, 0, 0, 18.21428571428571, 0, 19.30714285714285, 5E+2, 651.9200000000001, 24.07999999999999, 0,
        // 215, 0, 0, 0, 0, 0, 0, 0, 0, 465.657142857143, 561.6571428571428, 0, 0 }

        // CPLEX MAX OPTIMAL 3438.2920999999997 @ { 54.5, 0, 54.5, 57.77, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        // 5E+2, 483.5955, 16.4045, 0, 215, 0, 0, 0, 0, 0, 0, 0, 0, 345.4253571428571, 0, 0, 389.4253571428571
        // }

        CuteNetlibCase.doTest("AFIRO.SIF", "-464.7531428571429", "3438.2920999999997", ACCURACY);
    }

    /**
     * AGG — 489 rows × 163 columns, 2541 non-zeros.<br>
     * Optimal value: -3.5991767287E+07.<br>
     */
    @Test
    public void testAGG() {
        CuteNetlibCase.doTest("AGG.SIF", "-3.599176728657652E7", "2.8175579434489565E9", ACCURACY);
    }

    /**
     * AGG2 — 517 rows × 302 columns, 4515 non-zeros.<br>
     * Optimal value: -2.0239252356E+07.<br>
     */
    @Test
    public void testAGG2() {
        CuteNetlibCase.doTest("AGG2.SIF", "-2.0239252355977114E7", "5.71551859632249E9", ACCURACY);
    }

    /**
     * AGG3 — 517 rows × 302 columns, 4531 non-zeros.<br>
     * Optimal value: 1.0312115935E+07.<br>
     */
    @Test
    public void testAGG3() {
        CuteNetlibCase.doTest("AGG3.SIF", "1.031211593508922E7", "5.746768863949547E9", ACCURACY);
    }

    /**
     * BANDM — 306 rows × 472 columns, 2659 non-zeros.<br>
     * Optimal value: -1.5862801845E+02.<br>
     */
    @Test
    public void testBANDM() {
        CuteNetlibCase.doTest("BANDM.SIF", "-158.6280184501187", null, ACCURACY);
    }

    /**
     * BEACONFD — 174 rows × 262 columns, 3476 non-zeros.<br>
     * Optimal value: 3.3592485807E+04.<br>
     */
    @Test
    public void testBEACONFD() {
        CuteNetlibCase.doTest("BEACONFD.SIF", "33592.4858072", null, ACCURACY);
    }

    /**
     * BLEND — 75 rows × 83 columns, 521 non-zeros.<br>
     * Optimal value: -3.0812149846E+01.<br>
     */
    @Test
    public void testBLEND() {
        CuteNetlibCase.doTest("BLEND.SIF", "-3.0812149846E+01", null, ACCURACY);
    }

    /**
     * BNL1 — 644 rows × 1175 columns, 6129 non-zeros.<br>
     * Optimal value: 1.9776292856E+03.<br>
     */
    @Test
    public void testBNL1() {
        CuteNetlibCase.doTest("BNL1.SIF", "1977.629561522682", "2342.2468416744728", ACCURACY);
    }

    /**
     * BNL2 — 2325 rows × 3489 columns, 16124 non-zeros.<br>
     * Optimal value: 1.8112365404E+03.<br>
     */
    @Test
    public void testBNL2() {
        CuteNetlibCase.doTest("BNL2.SIF", "1811.2365403585452", null, ACCURACY);
    }

    /**
     * BOEING1 — 351 rows × 384 columns, 3865 non-zeros.<br>
     * Has variable bounds (UP LO) and ranges on constraints.<br>
     * Optimal value: -3.3521356751E+02.<br>
     */
    @Test
    public void testBOEING1() {
        CuteNetlibCase.doTest("BOEING1.SIF", "-335.2135675071266", "286.9746573387996", ACCURACY);
    }

    /**
     * BOEING2 — 167 rows × 143 columns, 1339 non-zeros.<br>
     * Has variable bounds (UP LO) and ranges on constraints.<br>
     * Optimal value: -3.1501872802E+02.<br>
     */
    @Test
    public void testBOEING2() {
        CuteNetlibCase.doTest("BOEING2.SIF", "-3.1501872802E+02", "-73.36896910872208", ACCURACY);
    }

    /**
     * BORE3D — 234 rows × 315 columns, 1525 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: 1.3730803942E+03.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    public void testBORE3D() {
        CuteNetlibCase.doTest("BORE3D.SIF", "1373.0803942085367", null, ACCURACY);
    }

    /**
     * BRANDY — 221 rows × 249 columns, 2150 non-zeros.<br>
     * Optimal value: 1.5185098965E+03.<br>
     */
    @Test
    public void testBRANDY() {
        CuteNetlibCase.doTest("BRANDY.SIF", "1518.509896488128", null, ACCURACY);
    }

    /**
     * CAPRI — 272 rows × 353 columns, 1786 non-zeros.<br>
     * Has variable bounds (UP FX FR).<br>
     * Optimal value: 2.6900129138E+03.<br>
     */
    @Test
    public void testCAPRI() {
        CuteNetlibCase.doTest("CAPRI.SIF", "2690.0129137681624", null, ACCURACY);
    }

    /**
     * CRE-A — 3517 rows × 4067 columns.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    public void testCRE_A() {
        CuteNetlibCase.doTest("CRE-A.SIF", "2.3595407060971607E7", "4.000288201473081E7", ACCURACY);
    }

    /**
     * CRE-B.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_B() {
        CuteNetlibCase.doTest("CRE-B.SIF", "2.3129639886832364E7", "7.634368362305094E7", ACCURACY);
    }

    /**
     * CRE-C — 3069 rows × 3678 columns.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    public void testCRE_C() {
        CuteNetlibCase.doTest("CRE-C.SIF", "2.5275116140880212E7", "3.762512696726111E7", ACCURACY);
    }

    /**
     * CRE-D.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testCRE_D() {
        CuteNetlibCase.doTest("CRE-D.SIF", "2.4454969764549244E7", "7.373382453297935E7", ACCURACY);
    }

    /**
     * CYCLE — 1904 rows × 2857 columns, 21322 non-zeros.<br>
     * Has variable bounds (UP FR).<br>
     * Optimal value: -5.2263930249E+00.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    public void testCYCLE() {
        CuteNetlibCase.doTest("CYCLE.SIF", "-5.2263930248941", "995.8104649596411", ACCURACY);
    }

    /**
     * CZPROB — 930 rows × 3523 columns, 14173 non-zeros.<br>
     * Has variable bounds (FX).<br>
     * Optimal value: 2.1851966989E+06.<br>
     */
    @Test
    public void testCZPROB() {
        CuteNetlibCase.doTest("CZPROB.SIF", "2185196.6988565763", "3089066.71321333", ACCURACY);
    }

    /**
     * D2Q06C — 2172 rows × 5167 columns, 35674 non-zeros.<br>
     * Optimal value: 1.2278423615E+05.<br>
     */
    @Test
    @Tag("unstable")
    public void testD2Q06C() {
        CuteNetlibCase.doTest("D2Q06C.SIF", "122784.21081418857", null, ACCURACY);
    }

    /**
     * D6CUBE — 416 rows × 6184 columns, 43888 non-zeros.<br>
     * Has variable bounds (LO).<br>
     * Optimal value: 3.1549166667E+02.<br>
     */
    @Test
    public void testD6CUBE() {
        CuteNetlibCase.doTest("D6CUBE.SIF", "315.49166666667315", "693.0000000000005", ACCURACY);
    }

    /**
     * DEGEN2 — 445 rows × 534 columns, 4449 non-zeros.<br>
     * Optimal value: -1.4351780000E+03.<br>
     */
    @Test
    public void testDEGEN2() {
        CuteNetlibCase.doTest("DEGEN2.SIF", "-1435.1779999999999", "-1226.12", ACCURACY);
    }

    /**
     * DEGEN3 — 1504 rows × 1818 columns, 26230 non-zeros.<br>
     * Optimal value: -9.8729400000E+02.<br>
     */
    @Test
    public void testDEGEN3() {
        CuteNetlibCase.doTest("DEGEN3.SIF", "-987.2940000000001", "-876.2800000000008", ACCURACY);
    }

    /**
     * DFL001 — 6072 rows × 12230 columns, 41873 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Estimated optimal value: 1.12664E+07.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testDFL001() {
        CuteNetlibCase.doTest("DFL001.SIF", "1.126639604667184E7", null, ACCURACY);
    }

    /**
     * E226 — 224 rows × 282 columns, 2767 non-zeros.<br>
     * Optimal value: -1.8751929066E+01.<br>
     */
    @Test
    public void testE226() {
        CuteNetlibCase.doTest("E226.SIF", "-11.638929066370546", "111.65096068931459", ACCURACY);
    }

    /**
     * ETAMACRO — 401 rows × 688 columns, 2489 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: -7.5571521774E+02.<br>
     */
    @Test
    public void testETAMACRO() {
        CuteNetlibCase.doTest("ETAMACRO.SIF", "-755.7152312325337", "258.71905646302014", ACCURACY);
    }

    /**
     * FFFFF800 — 525 rows × 854 columns, 6235 non-zeros.<br>
     * Optimal value: 5.5567961165E+05.<br>
     */
    @Test
    public void testFFFFF800() {
        CuteNetlibCase.doTest("FFFFF800.SIF", "555679.5648174941", "1858776.4328128027", ACCURACY);
    }

    /**
     * FINNIS — 498 rows × 614 columns, 2714 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: 1.7279096547E+05.<br>
     */
    @Test
    public void testFINNIS() {
        CuteNetlibCase.doTest("FINNIS.SIF", "172791.0655956116", null, ACCURACY);
    }

    /**
     * FIT1D — 25 rows × 1026 columns, 14430 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -9.1463780924E+03.<br>
     */
    @Test
    public void testFIT1D() {
        CuteNetlibCase.doTest("FIT1D.SIF", "-9146.378092421019", "80453.99999999999", ACCURACY);
    }

    /**
     * FIT1P — 628 rows × 1677 columns, 10894 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: 9.1463780924E+03.<br>
     */
    @Test
    public void testFIT1P() {
        CuteNetlibCase.doTest("FIT1P.SIF", "9146.378092420955", null, ACCURACY);
    }

    /**
     * FIT2D — 26 rows × 10500 columns, 138018 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -6.8464293294E+04.<br>
     */
    @Test
    @Tag("slow")
    public void testFIT2D() {
        CuteNetlibCase.doTest("FIT2D.SIF", "-68464.29329383196", "393548.6499999999", ACCURACY);
    }

    /**
     * FIT2P — 3001 rows × 13525 columns, 60784 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: 6.8464293232E+04.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testFIT2P() {
        CuteNetlibCase.doTest("FIT2P.SIF", "68464.29329383207", null, ACCURACY);
    }

    /**
     * FORPLAN — 162 rows × 421 columns, 4916 non-zeros.<br>
     * Has variable bounds (UP FX) and ranges on constraints.<br>
     * Optimal value: -6.6421873953E+02.<br>
     */
    @Test
    public void testFORPLAN() {
        CuteNetlibCase.doTest("FORPLAN.SIF", "-664.2189612722054", "2862.4274777342266", ACCURACY);
    }

    /**
     * GANGES — 1310 rows × 1681 columns, 7021 non-zeros.<br>
     * Has variable bounds (UP LO).<br>
     * Optimal value: -1.0958636356E+05.<br>
     */
    @Test
    public void testGANGES() {
        CuteNetlibCase.doTest("GANGES.SIF", "-109585.73612927811", "-2.24E-12", ACCURACY);
    }

    /**
     * GFRD-PNC — 617 rows × 1092 columns, 3467 non-zeros.<br>
     * Has variable bounds (UP LO).<br>
     * Optimal value: 6.9022359995E+06.<br>
     */
    @Test
    public void testGFRD_PNC() {
        CuteNetlibCase.doTest("GFRD-PNC.SIF", "6902235.999548811", null, ACCURACY);
    }

    /**
     * GREENBEA — 2393 rows × 5405 columns, 31499 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: -7.2462405908E+07.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    @Tag("unstable")
    public void testGREENBEA() {
        CuteNetlibCase.doTest("GREENBEA.SIF", "-7.255524812984598E7", null, ACCURACY);
    }

    /**
     * GREENBEB — 2393 rows × 5405 columns, 31499 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: -4.3021476065E+06.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    @Tag("unstable")
    public void testGREENBEB() {
        CuteNetlibCase.doTest("GREENBEB.SIF", "-4302260.261206587", null, ACCURACY);
    }

    /**
     * GROW15 — 301 rows × 645 columns, 5665 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -1.0687094129E+08.<br>
     */
    @Test
    public void testGROW15() {
        CuteNetlibCase.doTest("GROW15.SIF", "-1.068709412935753E8", "0.0", ACCURACY);
    }

    /**
     * GROW22 — 441 rows × 946 columns, 8318 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -1.6083433648E+08.<br>
     */
    @Test
    public void testGROW22() {
        CuteNetlibCase.doTest("GROW22.SIF", "-1.608343364825636E8", "0.0", ACCURACY);
    }

    /**
     * GROW7 — 141 rows × 301 columns, 2633 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -4.7787811815E+07.<br>
     */
    @Test
    public void testGROW7() {
        CuteNetlibCase.doTest("GROW7.SIF", "-4.7787811814711526E7", "0.0", ACCURACY);
    }

    /**
     * ISRAEL — 175 rows × 142 columns, 2358 non-zeros.<br>
     * Optimal value: -8.9664482186E+05.<br>
     */
    @Test
    public void testISRAEL() {
        CuteNetlibCase.doTest("ISRAEL.SIF", "-896644.8218630457", null, ACCURACY);
    }

    /**
     * KB2 — 44 rows × 41 columns, 291 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: -1.7499001299E+03.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    public void testKB2() {
        CuteNetlibCase.doTest("KB2.SIF", "-1.74990012991E+03", "0.0", ACCURACY);
    }

    /**
     * KEN-07 — 2427 rows × 3602 columns.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    public void testKEN_07() {
        CuteNetlibCase.doTest("KEN-07.SIF", "-6.795204433816869E8", "-1.61949281194431E8", ACCURACY);
    }

    /**
     * KEN-11.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    public void testKEN_11() {
        CuteNetlibCase.doTest("KEN-11.SIF", "-6.972382262519971E9", "-1.287957080545934E9", ACCURACY);
    }

    /**
     * KEN-13.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testKEN_13() {
        CuteNetlibCase.doTest("KEN-13.SIF", "-1.0257394789482431E10", "-2.241281190609764E9", ACCURACY);
    }

    /**
     * KEN-18.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Disabled
    public void testKEN_18() {
        CuteNetlibCase.doTest("KEN-18.SIF", "1234567890", "1234567890", ACCURACY);
    }

    /**
     * LOTFI — 154 rows × 308 columns, 1086 non-zeros.<br>
     * Optimal value: -2.5264706062E+01.<br>
     */
    @Test
    public void testLOTFI() {
        CuteNetlibCase.doTest("LOTFI.SIF", "-25.26470606188002", null, ACCURACY);
    }

    /**
     * MAROS — 847 rows × 1443 columns, 10006 non-zeros.<br>
     * Has variable bounds.<br>
     * Optimal value: -5.8063743701E+04.<br>
     */
    @Test
    public void testMAROS() {
        CuteNetlibCase.doTest("MAROS.SIF", "-58063.743701138235", "-10623.409207717115", ACCURACY);
    }

    /**
     * MAROS-R7 — 3137 rows × 9408 columns, 151120 non-zeros.<br>
     * Optimal value: 1.4971851665E+06.<br>
     */
    @Test
    @Tag("slow")
    public void testMAROS_R7() {
        CuteNetlibCase.doTest("MAROS-R7.SIF", "1497185.1664800502", null, ACCURACY);
    }

    /**
     * MODSZK1 — 688 rows × 1620 columns, 4158 non-zeros.<br>
     * Has variable bounds (FR).<br>
     * Optimal value: 3.2061972906E+02.<br>
     */
    @Test
    public void testMODSZK1() {
        CuteNetlibCase.doTest("MODSZK1.SIF", "320.6197293824883", null, ACCURACY);
    }

    /**
     * NESM — 663 rows × 2923 columns, 13988 non-zeros.<br>
     * Has variable bounds (UP LO FX) and ranges on constraints.<br>
     * Optimal value: 1.4076073035E+07.<br>
     */
    @Test
    public void testNESM() {
        CuteNetlibCase.doTest("NESM.SIF", "1.4076036487562722E7", "3.6088214327411644E7", ACCURACY);
    }

    /**
     * OSA-07.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    public void testOSA_07() {
        CuteNetlibCase.doTest("OSA-07.SIF", "535722.517299352", "4332086.205299969", ACCURACY);
    }

    /**
     * OSA-14.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    public void testOSA_14() {
        CuteNetlibCase.doTest("OSA-14.SIF", "1106462.8447362552", "9377699.405100001", ACCURACY);
    }

    /**
     * OSA-30.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    public void testOSA_30() {
        CuteNetlibCase.doTest("OSA-30.SIF", "2142139.873209757", "1.78441602883E7", ACCURACY);
    }

    /**
     * OSA-60.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testOSA_60() {
        CuteNetlibCase.doTest("OSA-60.SIF", "4044072.503163047", "4.25114540365E7", ACCURACY);
    }

    /**
     * PDS-02 — 2954 rows × 7535 columns.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    public void testPDS_02() {
        CuteNetlibCase.doTest("PDS-02.SIF", "2.885786201E10", "2.931365171E10", ACCURACY);
    }

    /**
     * PDS-06.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_06() {
        CuteNetlibCase.doTest("PDS-06.SIF", "2.77610376E10", "2.931366991E10", ACCURACY);
    }

    /**
     * PDS-10.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_10() {
        CuteNetlibCase.doTest("PDS-10.SIF", "2.6727094976E10", "2.931368811E10", ACCURACY);
    }

    /**
     * PDS-20.<br>
     * From the Kennington test set (military airlift; see lp/data/kennington in the netlib distribution).<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_20() {
        CuteNetlibCase.doTest("PDS-20.SIF", "2.382165864E10", "2.931372451E10", ACCURACY);
    }

    /**
     * PEROLD — 626 rows × 1376 columns, 6026 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: -9.3807580773E+03.<br>
     */
    @Test
    public void testPEROLD() {
        CuteNetlibCase.doTest("PEROLD.SIF", "-9380.755278235429", "-5878.539464724801", ACCURACY);
    }

    /**
     * PILOT — 1442 rows × 3652 columns, 43220 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: -5.5740430007E+02.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT() {
        CuteNetlibCase.doTest("PILOT.SIF", "-557.4897292730852", "-422.4724550733185", ACCURACY);
    }

    /**
     * PILOT-JA — 941 rows × 1988 columns, 14706 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: -6.1131344111E+03.<br>
     */
    @Test
    @Tag("unstable")
    public void testPILOT_JA() {
        CuteNetlibCase.doTest("PILOT-JA.SIF", "-6113.1364655813495", "-3095.1845377674813", ACCURACY);
    }

    /**
     * PILOT-WE — 723 rows × 2789 columns, 9218 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: -2.7201027439E+06.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT_WE() {
        CuteNetlibCase.doTest("PILOT-WE.SIF", "-2720107.5328449034", "20770.464669007524", ACCURACY);
    }

    /**
     * PILOT4 — 411 rows × 1000 columns, 5145 non-zeros.<br>
     * Has variable bounds (UP FX FR PL).<br>
     * Optimal value: -2.5811392641E+03.<br>
     */
    @Test
    public void testPILOT4() {
        CuteNetlibCase.doTest("PILOT4.SIF", "-2581.1392612778604", "0.0", ACCURACY);
    }

    /**
     * PILOT87 — 2031 rows × 4883 columns, 73804 non-zeros.<br>
     * Has variable bounds.<br>
     * Optimal value: 3.0171072827E+02.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPILOT87() {
        CuteNetlibCase.doTest("PILOT87.SIF", "301.7103473330999", null, ACCURACY);
    }

    /**
     * PILOTNOV — 976 rows × 2172 columns, 13129 non-zeros.<br>
     * Has variable bounds (UP FX).<br>
     * Optimal value: -4.4972761882E+03.<br>
     */
    @Test
    @Tag("unstable")
    public void testPILOTNOV() {
        CuteNetlibCase.doTest("PILOTNOV.SIF", "-4497.27618821887", "-957.3524818279784", ACCURACY);
    }

    /**
     * QAP12 — 3193 rows × 8856 columns, 44244 non-zeros.<br>
     * Optimal value: 5.2289435056E+02.<br>
     * Non-zero count not reported in the netlib summary table.<br>
     */
    @Test
    @Tag("unstable")
    public void testQAP12() {
        CuteNetlibCase.doTest("QAP12.SIF", "522.8943505591718", "1104.1482908677572", ACCURACY);
    }

    /**
     * QAP15 — 6331 rows × 22275 columns, 110700 non-zeros.<br>
     * Optimal value: 1.0409940410E+03.<br>
     * Non-zero count not reported in the netlib summary table.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testQAP15() {
        CuteNetlibCase.doTest("QAP15.SIF", "1040.9940409587314", "2109.777554651187", ACCURACY);
    }

    /**
     * QAP8 — 913 rows × 1632 columns, 8304 non-zeros.<br>
     * Optimal value: 2.0350000000E+02.<br>
     * Non-zero count not reported in the netlib summary table.<br>
     */
    @Test
    @Tag("unstable")
    public void testQAP8() {
        CuteNetlibCase.doTest("QAP8.SIF", "2.0350000000E+02", null, ACCURACY);
    }

    /**
     * RECIPELP — 92 rows × 180 columns.<br>
     */
    @Test
    public void testRECIPELP() {
        CuteNetlibCase.doTest("RECIPELP.SIF", "-266.616", "-104.818", ACCURACY);
    }

    /**
     * SC105 — 106 rows × 103 columns, 281 non-zeros.<br>
     * Optimal value: -5.2202061212E+01.<br>
     */
    @Test
    public void testSC105() {
        CuteNetlibCase.doTest("SC105.SIF", "-52.202061211707246", "0.0", ACCURACY);
    }

    /**
     * SC205 — 206 rows × 203 columns, 552 non-zeros.<br>
     * Optimal value: -5.2202061212E+01.<br>
     */
    @Test
    public void testSC205() {
        CuteNetlibCase.doTest("SC205.SIF", "-52.202061211707246", "0.0", ACCURACY);
    }

    /**
     * SC50A — 51 rows × 48 columns, 131 non-zeros.<br>
     * Optimal value: -6.4575077059E+01.<br>
     */
    @Test
    public void testSC50A() {
        CuteNetlibCase.doTest("SC50A.SIF", "-64.57507705856449", "0.0", ACCURACY);
    }

    /**
     * SC50B — 51 rows × 48 columns, 119 non-zeros.<br>
     * Optimal value: -7.0000000000E+01.<br>
     */
    @Test
    public void testSC50B() {
        CuteNetlibCase.doTest("SC50B.SIF", "-7.0000000000E+01", "0.0", ACCURACY);
    }

    /**
     * SCAGR25 — 472 rows × 500 columns, 2029 non-zeros.<br>
     * Optimal value: -1.4753433061E+07.<br>
     */
    @Test
    public void testSCAGR25() {
        CuteNetlibCase.doTest("SCAGR25.SIF", "-1.475343306076852E7", null, ACCURACY);
    }

    /**
     * SCAGR7 — 130 rows × 140 columns, 553 non-zeros.<br>
     * Optimal value: -2.3313892548E+06.<br>
     */
    @Test
    public void testSCAGR7() {
        CuteNetlibCase.doTest("SCAGR7.SIF", "-2331389.8243309837", null, ACCURACY);
    }

    /**
     * SCFXM1 — 331 rows × 457 columns, 2612 non-zeros.<br>
     * Optimal value: 1.8416759028E+04.<br>
     */
    @Test
    public void testSCFXM1() {
        CuteNetlibCase.doTest("SCFXM1.SIF", "18416.75902834894", null, ACCURACY);
    }

    /**
     * SCFXM2 — 661 rows × 914 columns, 5229 non-zeros.<br>
     * Optimal value: 3.6660261565E+04.<br>
     */
    @Test
    public void testSCFXM2() {
        CuteNetlibCase.doTest("SCFXM2.SIF", "36660.261564998815", null, ACCURACY);
    }

    /**
     * SCFXM3 — 991 rows × 1371 columns, 7846 non-zeros.<br>
     * Optimal value: 5.4901254550E+04.<br>
     */
    @Test
    public void testSCFXM3() {
        CuteNetlibCase.doTest("SCFXM3.SIF", "54901.2545497515", null, ACCURACY);
    }

    /**
     * SCORPION — 389 rows × 358 columns, 1708 non-zeros.<br>
     * Optimal value: 1.8781248227E+03.<br>
     */
    @Test
    public void testSCORPION() {
        CuteNetlibCase.doTest("SCORPION.SIF", "1878.1248227381068", null, ACCURACY);
    }

    /**
     * SCRS8 — 491 rows × 1169 columns, 4029 non-zeros.<br>
     * Optimal value: 9.0429998619E+02.<br>
     */
    @Test
    public void testSCRS8() {
        CuteNetlibCase.doTest("SCRS8.SIF", "904.2969538007912", null, ACCURACY);
    }

    /**
     * SCSD1 — 78 rows × 760 columns, 3148 non-zeros.<br>
     * Optimal value: 8.6666666743E+00.<br>
     */
    @Test
    public void testSCSD1() {
        CuteNetlibCase.doTest("SCSD1.SIF", "8.666666674333367", null, ACCURACY);
    }

    /**
     * SCSD6 — 148 rows × 1350 columns, 5666 non-zeros.<br>
     * Optimal value: 5.0500000078E+01.<br>
     */
    @Test
    public void testSCSD6() {
        CuteNetlibCase.doTest("SCSD6.SIF", "50.5", null, ACCURACY);
    }

    /**
     * SCSD8 — 398 rows × 2750 columns, 11334 non-zeros.<br>
     * Optimal value: 9.0499999993E+02.<br>
     */
    @Test
    public void testSCSD8() {
        CuteNetlibCase.doTest("SCSD8.SIF", "905", null, ACCURACY);
    }

    /**
     * SCTAP1 — 301 rows × 480 columns, 2052 non-zeros.<br>
     * Optimal value: 1.4122500000E+03.<br>
     */
    @Test
    public void testSCTAP1() {
        CuteNetlibCase.doTest("SCTAP1.SIF", "1412.25", null, ACCURACY);
    }

    /**
     * SCTAP2 — 1091 rows × 1880 columns, 8124 non-zeros.<br>
     * Optimal value: 1.7248071429E+03.<br>
     */
    @Test
    public void testSCTAP2() {
        CuteNetlibCase.doTest("SCTAP2.SIF", "1724.8071428568292", null, ACCURACY);
    }

    /**
     * SCTAP3 — 1481 rows × 2480 columns, 10734 non-zeros.<br>
     * Optimal value: 1.4240000000E+03.<br>
     */
    @Test
    public void testSCTAP3() {
        CuteNetlibCase.doTest("SCTAP3.SIF", "1424.000000000573", null, ACCURACY);
    }

    /**
     * SEBA — 516 rows × 1028 columns, 4874 non-zeros.<br>
     * Has variable bounds (UP LO) and ranges on constraints.<br>
     * Optimal value: 1.5711600000E+04.<br>
     */
    @Test
    public void testSEBA() {
        CuteNetlibCase.doTest("SEBA.SIF", "15711.6", "35160.46056", ACCURACY);
    }

    /**
     * SHARE1B — 118 rows × 225 columns, 1182 non-zeros.<br>
     * Optimal value: -7.6589318579E+04.<br>
     */
    @Test
    public void testSHARE1B() {
        CuteNetlibCase.doTest("SHARE1B.SIF", "-76589.31857918584", "74562.53714565346", ACCURACY.withPrecision(3));
    }

    /**
     * SHARE2B — 97 rows × 79 columns, 730 non-zeros.<br>
     * Optimal value: -4.1573224074E+02.<br>
     */
    @Test
    public void testSHARE2B() {
        CuteNetlibCase.doTest("SHARE2B.SIF", "-4.1573224074E+02", "-265.0981144446295", ACCURACY);
    }

    /**
     * SHELL — 537 rows × 1775 columns, 4900 non-zeros.<br>
     * Has variable bounds (UP LO FX).<br>
     * Optimal value: 1.2088253460E+09.<br>
     */
    @Test
    public void testSHELL() {
        CuteNetlibCase.doTest("SHELL.SIF", "1.208825346E9", null, ACCURACY);
    }

    /**
     * SHIP04L — 403 rows × 2118 columns, 8450 non-zeros.<br>
     * Optimal value: 1.7933245380E+06.<br>
     */
    @Test
    public void testSHIP04L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SHIP04S — 403 rows × 1458 columns, 5810 non-zeros.<br>
     * Optimal value: 1.7987147004E+06.<br>
     */
    @Test
    public void testSHIP04S() {
        CuteNetlibCase.doTest("SHIP04S.SIF", "1798714.7004453915", null, ACCURACY);
    }

    /**
     * SHIP08L — 779 rows × 4283 columns, 17085 non-zeros.<br>
     * Optimal value: 1.9090552114E+06.<br>
     */
    @Test
    public void testSHIP08L() {
        CuteNetlibCase.doTest("SHIP08L.SIF", "1909055.211378393", null, ACCURACY);
    }

    /**
     * SHIP08S — 779 rows × 2387 columns, 9501 non-zeros.<br>
     * Optimal value: 1.9200982105E+06.<br>
     */
    @Test
    public void testSHIP08S() {
        CuteNetlibCase.doTest("SHIP08S.SIF", "1920098.210516164", null, ACCURACY);
    }

    /**
     * SHIP12L — 1152 rows × 5427 columns, 21597 non-zeros.<br>
     * Optimal value: 1.4701879193E+06.<br>
     */
    @Test
    public void testSHIP12L() {
        CuteNetlibCase.doTest("SHIP12L.SIF", "1470187.919328858", null, ACCURACY);
    }

    /**
     * SHIP12S — 1152 rows × 2763 columns, 10941 non-zeros.<br>
     * Optimal value: 1.4892361344E+06.<br>
     */
    @Test
    public void testSHIP12S() {
        CuteNetlibCase.doTest("SHIP12S.SIF", "1489236.134420166", null, ACCURACY);
    }

    /**
     * SIERRA — 1228 rows × 2036 columns, 9252 non-zeros.<br>
     * Has variable bounds (UP).<br>
     * Optimal value: 1.5394362184E+07.<br>
     */
    @Test
    public void testSIERRA() {
        CuteNetlibCase.doTest("SIERRA.SIF", "1.5394362183631932E7", "8.042913100947624E8", ACCURACY);
    }

    /**
     * STAIR — 357 rows × 467 columns, 3857 non-zeros.<br>
     * Has variable bounds (UP FX FR).<br>
     * Optimal value: -2.5126695119E+02.<br>
     */
    @Test
    public void testSTAIR() {
        CuteNetlibCase.doTest("STAIR.SIF", "-251.26695119296787", "-208.79999", ACCURACY);
    }

    /**
     * STANDATA — 360 rows × 1075 columns, 3038 non-zeros.<br>
     * Has variable bounds (UP FX).<br>
     * Optimal value: 1.2576995000E+03.<br>
     */
    @Test
    public void testSTANDATA() {
        CuteNetlibCase.doTest("STANDATA.SIF", "1257.6995", null, ACCURACY);
    }

    /**
     * STANDGUB — 362 rows × 1184 columns.<br>
     * Optimal value depends on a generalised upper bound (GUB) reformulation; see netlib README NOTES.<br>
     */
    @Test
    public void testSTANDGUB() {
        CuteNetlibCase.doTest("STANDGUB.SIF", "1257.6995", null, ACCURACY);
    }

    /**
     * STANDMPS — 468 rows × 1075 columns, 3686 non-zeros.<br>
     * Has variable bounds (UP FX).<br>
     * Optimal value: 1.4060175000E+03.<br>
     */
    @Test
    public void testSTANDMPS() {
        CuteNetlibCase.doTest("STANDMPS.SIF", "1406.0175", null, ACCURACY);
    }

    /**
     * STOCFOR1 — 118 rows × 111 columns, 474 non-zeros.<br>
     * Optimal value: -4.1131976219E+04.<br>
     */
    @Test
    public void testSTOCFOR1() {
        CuteNetlibCase.doTest("STOCFOR1.SIF", "-41131.97621943626", null, ACCURACY);
    }

    /**
     * STOCFOR2 — 2158 rows × 2031 columns, 9492 non-zeros.<br>
     * Optimal value: -3.9024408538E+04.<br>
     */
    @Test
    public void testSTOCFOR2() {
        CuteNetlibCase.doTest("STOCFOR2.SIF", "-39024.4085378819", null, ACCURACY);
    }

    /**
     * STOCFOR3 — 16676 rows × 15695 columns, 74004 non-zeros.<br>
     * Optimal value: -3.9976661576E+04.<br>
     * Non-zero count not reported in the netlib summary table.<br>
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testSTOCFOR3() {
        CuteNetlibCase.doTest("STOCFOR3.SIF", "-39976.78394364959", null, ACCURACY);
    }

    /**
     * TRUSS — 1001 rows × 8806 columns, 36642 non-zeros.<br>
     * Optimal value: 4.5881584719E+05.<br>
     * Non-zero count not reported in the netlib summary table.<br>
     */
    @Test
    @Tag("unstable")
    public void testTRUSS() {
        CuteNetlibCase.doTest("TRUSS.SIF", "458815.84718561685", null, ACCURACY);
    }

    /**
     * TUFF — 334 rows × 587 columns, 4523 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: 2.9214776509E-01.<br>
     * RHS section is empty in the original SIF.<br>
     */
    @Test
    public void testTUFF() {
        CuteNetlibCase.doTest("TUFF.SIF", "0.292147765093613", "0.894990186757432", ACCURACY);
    }

    /**
     * VTP-BASE — 199 rows × 203 columns, 914 non-zeros.<br>
     * Has variable bounds (UP LO FX FR).<br>
     * Optimal value: 1.2983146246E+05.<br>
     */
    @Test
    public void testVTP_BASE() {
        CuteNetlibCase.doTest("VTP-BASE.SIF", "129831.46246136136", null, ACCURACY);
    }

    /**
     * WOOD1P — 245 rows × 2594 columns, 70216 non-zeros.<br>
     * Optimal value: 1.4429024116E+00.<br>
     */
    @Test
    public void testWOOD1P() {
        CuteNetlibCase.doTest("WOOD1P.SIF", "1.44290241157344", "9.99999999999964", ACCURACY);
    }

    /**
     * WOODW — 1099 rows × 8405 columns, 37478 non-zeros.<br>
     * Optimal value: 1.3044763331E+00.<br>
     */
    @Test
    public void testWOODW() {
        CuteNetlibCase.doTest("WOODW.SIF", "1.30447633308416", "6.463675062936", ACCURACY);
    }

}
