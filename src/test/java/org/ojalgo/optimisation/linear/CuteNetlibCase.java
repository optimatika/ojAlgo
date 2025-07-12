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
 * A collection of linear optimisation problems found here: http://www.numerical.rl.ac.uk/cute/netlib.html
 * <p>
 * netlib is an industry standard collection of linear optimisation problems.
 * <P>
 * The Constrained and Unconstrained Testing Environment with safe threads (CUTEst) for optimization software.
 * <p>
 * Min/Max have have been verified by CPLEX or OR-Tools (GLOP).
 * <ul>
 * <li>One problem is disabled since we have not been able to get a solution from either CPLEX or OR-Tools.
 * <li>Some problems are tagged as "slow" because they take too long for ojAlgo to solve to be suitable as a
 * unit test.
 * <li>Some problems are tagged as "unstable" because ojAlgo does not return the same solution as CPLEX or
 * OR-Tools.
 * </ul>
 * The description of each problem is AI-generated, and may not be entirely accurate. The problems are
 * described in terms of their size, structure, and the numerical challenges they present to linear
 * programming solvers. The descriptions are intended to provide context for the problems and to highlight the
 * specific aspects that make them interesting or challenging for optimization algorithms.
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

        // long time = 10_000L;
        // model.options.time_abort = time;
        // model.options.time_suffice = time;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);
    }

    /**
     * 25FV47 is a large-scale linear programming problem from the netlib collection that tests the solver's
     * ability to handle problems with significant size and complexity. This problem has 822 rows and 1571
     * columns with 11,127 nonzeros, making it a substantial test case for computational efficiency and
     * numerical stability. The problem is known for its challenging structure that can expose weaknesses in
     * simplex implementations, particularly around basis factorization and pivot selection strategies. It
     * tests the solver's ability to maintain numerical accuracy while processing large constraint matrices
     * and handling potential degeneracy issues that arise in real-world optimization problems.
     */
    @Test
    public void test25FV47() {
        CuteNetlibCase.doTest("25FV47.SIF", "5501.845888286646", null, ACCURACY);
    }

    /**
     * 80BAU3B is a very large-scale linear programming problem with 2263 rows and 9799 columns, containing
     * 29,063 nonzeros. This problem tests the solver's ability to handle extremely large constraint matrices
     * and demonstrates the importance of efficient sparse matrix operations. The problem's size makes it
     * particularly challenging for memory management and computational efficiency, testing the solver's
     * ability to maintain numerical stability while processing massive amounts of data. It serves as a
     * benchmark for testing the scalability of linear programming algorithms and their ability to handle
     * real-world problems of substantial size.
     */
    @Test
    public void test80BAU3B() {
        CuteNetlibCase.doTest("80BAU3B.SIF", "987224.1924090903", null, ACCURACY);
    }

    /**
     * ADLITTLE is a medium-sized linear programming problem with 57 rows and 97 columns that represents a
     * real-world optimization scenario. This problem tests the solver's ability to handle problems with a mix
     * of constraint types and variable bounds, including both equality and inequality constraints. The
     * problem structure includes various constraint coefficients that test numerical precision and the
     * solver's ability to handle different scaling factors. It serves as a good benchmark for testing the
     * robustness of linear programming algorithms on problems of moderate size with realistic constraint
     * structures. 53x149
     */
    @Test
    @Tag("bm1000")
    public void testADLITTLE() {
        CuteNetlibCase.doTest("ADLITTLE.SIF", "225494.96316238036", null, ACCURACY);
    }

    /**
     * AFIRO is a small but well-known linear programming problem with 28 rows and 32 columns that serves as a
     * fundamental test case for linear programming solvers. This problem is particularly interesting because
     * it tests the solver's ability to handle problems with multiple optimal solutions and degenerate cases.
     * The problem structure includes both equality and inequality constraints with various coefficient
     * patterns, making it an excellent test for numerical stability and pivot selection strategies. AFIRO is
     * often used as a first validation case for new linear programming implementations due to its manageable
     * size and known solution characteristics. 24x56
     */
    @Test
    @Tag("bm1000")
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
     * AGG is a large-scale linear programming problem with 489 rows and 163 columns that tests the solver's
     * ability to handle problems with significant constraint density and complex objective functions. This
     * problem is particularly challenging due to its large objective value range and the presence of both
     * minimization and maximization objectives, testing the solver's numerical precision and scaling
     * capabilities. The problem structure includes various constraint types and coefficient patterns that can
     * expose weaknesses in basis factorization and pivot selection algorithms. AGG serves as a benchmark for
     * testing the robustness of linear programming solvers on problems with challenging numerical
     * characteristics. This file contained this section:
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
     */
    @Test
    @Tag("bm1000")
    public void testAGG() {
        CuteNetlibCase.doTest("AGG.SIF", "-3.599176728657652E7", "2.8175579434489565E9", ACCURACY);
    }

    /**
     * AGG2 is a variant of the AGG problem with 517 rows and 302 columns that tests similar numerical
     * challenges as AGG but with a different constraint structure. This problem is particularly interesting
     * because it demonstrates how slight modifications to problem structure can significantly impact solver
     * performance and numerical behavior. The problem tests the solver's ability to handle problems with
     * large objective value ranges and complex constraint matrices, making it a good benchmark for testing
     * numerical stability and computational efficiency in linear programming algorithms.
     */
    @Test
    @Tag("bm1000")
    public void testAGG2() {
        CuteNetlibCase.doTest("AGG2.SIF", "-2.0239252355977114E7", "5.71551859632249E9", ACCURACY);
    }

    /**
     * AGG3 is another variant of the AGG problem family with 517 rows and 302 columns that provides a third
     * perspective on the numerical challenges present in this problem class. This problem tests the solver's
     * ability to handle problems with positive objective values and complex constraint structures,
     * complementing the testing provided by AGG and AGG2. The problem serves as a benchmark for testing the
     * consistency of linear programming solvers across different problem formulations and objective function
     * characteristics.
     */
    @Test
    @Tag("bm1000")
    public void testAGG3() {
        CuteNetlibCase.doTest("AGG3.SIF", "1.031211593508922E7", "5.746768863949547E9", ACCURACY);
    }

    /**
     * BANDM is a linear programming problem with 306 rows and 472 columns that tests the solver's ability to
     * handle problems with banded matrix structures. This problem is particularly interesting because it
     * demonstrates how matrix structure can influence solver performance and numerical behavior. The banded
     * nature of the constraint matrix can be exploited by specialized algorithms, making BANDM a good test
     * case for evaluating the efficiency of different linear programming approaches and their ability to
     * recognize and utilize problem structure.
     */
    @Test
    @Tag("bm1000")
    public void testBANDM() {
        CuteNetlibCase.doTest("BANDM.SIF", "-158.6280184501187", null, ACCURACY);
    }

    /**
     * BEACONFD is a linear programming problem with 174 rows and 262 columns that represents a real-world
     * optimization scenario. This problem tests the solver's ability to handle problems with realistic
     * constraint structures and coefficient patterns that arise in practical applications. The problem
     * includes various constraint types and variable bounds that test the solver's numerical precision and
     * ability to handle different scaling factors. BEACONFD serves as a benchmark for testing the robustness
     * of linear programming algorithms on problems with realistic problem characteristics.
     */
    @Test
    @Tag("bm1000")
    public void testBEACONFD() {
        CuteNetlibCase.doTest("BEACONFD.SIF", "33592.4858072", null, ACCURACY);
    }

    /**
     * BLEND is a classic linear programming problem with 75 rows and 83 columns that represents an oil
     * refinery blending problem. This problem is particularly interesting because it tests the solver's
     * ability to handle problems with specific industry-relevant structures and constraint patterns. The
     * blending problem involves mixing different components to meet quality specifications while minimizing
     * cost, creating a problem structure that tests numerical precision and constraint handling. BLEND serves
     * as a benchmark for testing linear programming solvers on problems with realistic industrial
     * applications and specific constraint characteristics. 72x155
     */
    @Test
    @Tag("bm1000")
    public void testBLEND() {
        CuteNetlibCase.doTest("BLEND.SIF", "-3.0812149846E+01", null, ACCURACY);
    }

    /**
     * BNL1 is a large-scale linear programming problem with 644 rows and 1175 columns that tests the solver's
     * ability to handle problems with significant size and complexity. This problem is particularly
     * challenging due to its large constraint matrix and the presence of both minimization and maximization
     * objectives, testing the solver's numerical precision and computational efficiency. The problem
     * structure includes various constraint types and coefficient patterns that can expose weaknesses in
     * basis factorization and pivot selection algorithms. BNL1 serves as a benchmark for testing the
     * robustness of linear programming solvers on large-scale problems with challenging numerical
     * characteristics.
     */
    @Test
    public void testBNL1() {
        CuteNetlibCase.doTest("BNL1.SIF", "1977.629561522682", "2342.2468416744728", ACCURACY);
    }

    /**
     * BNL2 is an even larger linear programming problem with 2325 rows and 3489 columns that represents an
     * extreme test case for linear programming solvers. This problem tests the solver's ability to handle
     * problems with massive constraint matrices and complex numerical characteristics. The problem's size
     * makes it particularly challenging for memory management and computational efficiency, testing the
     * solver's ability to maintain numerical stability while processing enormous amounts of data. BNL2 serves
     * as a benchmark for testing the scalability limits of linear programming algorithms and their ability to
     * handle problems of extreme size.
     */
    @Test
    @Tag("unstable")
    public void testBNL2() {
        CuteNetlibCase.doTest("BNL2.SIF", "1811.2365403585452", null, ACCURACY);
    }

    /**
     * BOEING1 is a linear programming problem with 351 rows and 384 columns that represents aircraft flap
     * settings optimization for economical operations. This problem is particularly interesting because it
     * tests the solver's ability to handle problems with specific engineering applications and realistic
     * constraint structures. The problem includes various constraint types related to aircraft operations,
     * testing the solver's numerical precision and ability to handle problems with industry-specific
     * characteristics. BOEING1 serves as a benchmark for testing linear programming solvers on problems with
     * realistic engineering applications and complex constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testBOEING1() {
        CuteNetlibCase.doTest("BOEING1.SIF", "-335.2135675071266", "286.9746573387996", ACCURACY);
    }

    /**
     * BOEING2 is a variant of the BOEING1 problem with 167 rows and 143 columns that provides a different
     * perspective on aircraft optimization problems. This problem tests the solver's ability to handle
     * problems with similar engineering applications but different problem structures and constraint
     * characteristics. The problem includes various constraint types related to aircraft operations, testing
     * the solver's numerical precision and ability to handle problems with industry-specific characteristics.
     * BOEING2 serves as a benchmark for testing the consistency of linear programming solvers across
     * different problem formulations within the same application domain. 144x287
     */
    @Test
    @Tag("bm1000")
    public void testBOEING2() {
        CuteNetlibCase.doTest("BOEING2.SIF", "-3.1501872802E+02", "-73.36896910872208", ACCURACY);
    }

    /**
     * BORE3D is a linear programming problem with 234 rows and 315 columns that tests the solver's ability to
     * handle problems with specific geometric or structural characteristics. This problem is particularly
     * interesting because it may involve three-dimensional constraints or relationships that test the
     * solver's ability to handle problems with complex spatial or structural relationships. The problem
     * structure includes various constraint types and coefficient patterns that can expose weaknesses in
     * numerical algorithms and constraint handling capabilities.
     */
    @Test
    @Tag("bm1000")
    public void testBORE3D() {
        CuteNetlibCase.doTest("BORE3D.SIF", "1373.0803942085367", null, ACCURACY);
    }

    /**
     * BRANDY is a linear programming problem with 221 rows and 249 columns that represents a real-world
     * optimization scenario. This problem tests the solver's ability to handle problems with realistic
     * constraint structures and coefficient patterns that arise in practical applications. The problem
     * includes various constraint types and variable bounds that test the solver's numerical precision and
     * ability to handle different scaling factors. BRANDY serves as a benchmark for testing the robustness of
     * linear programming algorithms on problems with realistic problem characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("bm1000")
    public void testBRANDY() {
        CuteNetlibCase.doTest("BRANDY.SIF", "1518.509896488128", null, ACCURACY);
    }

    /**
     * CAPRI is a linear programming problem with 272 rows and 353 columns that tests the solver's ability to
     * handle problems with specific constraint structures and coefficient patterns. This problem is
     * particularly interesting because it may involve constraints related to capacity planning or resource
     * allocation, testing the solver's ability to handle problems with realistic operational constraints. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and constraint handling capabilities.
     */
    @Test
    @Tag("bm1000")
    public void testCAPRI() {
        CuteNetlibCase.doTest("CAPRI.SIF", "2690.0129137681624", null, ACCURACY);
    }

    /**
     * CRE-A is a large-scale linear programming problem from the Kennington subset that tests the solver's
     * ability to handle problems with significant size and complexity. This problem is particularly
     * challenging due to its large constraint matrix and complex numerical characteristics. The problem
     * structure includes various constraint types and coefficient patterns that can expose weaknesses in
     * basis factorization and pivot selection algorithms. CRE-A serves as a benchmark for testing the
     * robustness of linear programming solvers on large-scale problems with challenging numerical
     * characteristics.
     */
    @Test
    @Tag("unstable")
    public void testCRE_A() {
        CuteNetlibCase.doTest("CRE-A.SIF", "2.3595407060971607E7", "4.000288201473081E7", ACCURACY);
    }

    /**
     * CRE-B is another large-scale linear programming problem from the Kennington subset that provides a
     * different perspective on the numerical challenges present in this problem class. This problem tests the
     * solver's ability to handle problems with large constraint matrices and complex numerical
     * characteristics, complementing the testing provided by CRE-A. The problem structure includes various
     * constraint types and coefficient patterns that can expose weaknesses in numerical algorithms and
     * computational efficiency.
     */
    @Test
    @Tag("unstable")
    public void testCRE_B() {
        CuteNetlibCase.doTest("CRE-B.SIF", "2.3129639886832364E7", "7.634368362305094E7", ACCURACY);
    }

    /**
     * CRE-C is a third large-scale linear programming problem from the Kennington subset that provides
     * additional testing of numerical challenges in this problem class. This problem tests the solver's
     * ability to handle problems with large constraint matrices and complex numerical characteristics,
     * further complementing the testing provided by CRE-A and CRE-B. The problem structure includes various
     * constraint types and coefficient patterns that can expose weaknesses in numerical algorithms and
     * computational efficiency.
     */
    @Test
    @Tag("unstable")
    public void testCRE_C() {
        CuteNetlibCase.doTest("CRE-C.SIF", "2.5275116140880212E7", "3.762512696726111E7", ACCURACY);
    }

    /**
     * CRE-D is a fourth large-scale linear programming problem from the Kennington subset that completes the
     * testing of numerical challenges in this problem class. This problem tests the solver's ability to
     * handle problems with large constraint matrices and complex numerical characteristics, providing
     * comprehensive testing when combined with CRE-A, CRE-B, and CRE-C. The problem structure includes
     * various constraint types and coefficient patterns that can expose weaknesses in numerical algorithms
     * and computational efficiency.
     */
    @Test
    @Tag("unstable")
    public void testCRE_D() {
        CuteNetlibCase.doTest("CRE-D.SIF", "2.4454969764549244E7", "7.373382453297935E7", ACCURACY);
    }

    /**
     * CYCLE is a large-scale linear programming problem with 1904 rows and 2857 columns that tests the
     * solver's ability to handle problems with significant degeneracy and cycling issues. This problem is
     * particularly challenging because it can cause simplex algorithms to cycle or take excessive iterations
     * due to degenerate pivots. The problem structure includes various constraint types and coefficient
     * patterns that can expose weaknesses in anti-cycling strategies and numerical stability. CYCLE serves as
     * a benchmark for testing the robustness of linear programming solvers against degeneracy and cycling
     * problems.
     */
    @Test
    @Tag("slow")
    public void testCYCLE() {
        CuteNetlibCase.doTest("CYCLE.SIF", "-5.2263930248941", "995.8104649596411", ACCURACY);
    }

    /**
     * CZPROB is a large-scale linear programming problem with 930 rows and 3523 columns that tests the
     * solver's ability to handle problems with significant size and complexity. This problem is particularly
     * challenging due to its large constraint matrix and the presence of both minimization and maximization
     * objectives, testing the solver's numerical precision and computational efficiency. The problem
     * structure includes various constraint types and coefficient patterns that can expose weaknesses in
     * basis factorization and pivot selection algorithms. CZPROB serves as a benchmark for testing the
     * robustness of linear programming solvers on large-scale problems with challenging numerical
     * characteristics.
     */
    @Test
    public void testCZPROB() {
        CuteNetlibCase.doTest("CZPROB.SIF", "2185196.6988565763", "3089066.71321333", ACCURACY);
    }

    /**
     * D2Q06C is a very large-scale linear programming problem with 2172 rows and 5167 columns that represents
     * an extreme test case for linear programming solvers. This problem tests the solver's ability to handle
     * problems with massive constraint matrices and complex numerical characteristics. The problem's size
     * makes it particularly challenging for memory management and computational efficiency, testing the
     * solver's ability to maintain numerical stability while processing enormous amounts of data. D2Q06C
     * serves as a benchmark for testing the scalability limits of linear programming algorithms and their
     * ability to handle problems of extreme size.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testD2Q06C() {
        CuteNetlibCase.doTest("D2Q06C.SIF", "122784.21081418857", null, ACCURACY);
    }

    /**
     * D6CUBE is a linear programming problem with 416 rows and 6184 columns that tests the solver's ability
     * to handle problems with specific geometric characteristics related to six-dimensional cube
     * triangulation. This problem is particularly interesting because it involves finding the minimum
     * cardinality of triangulations of a 6-dimensional cube, creating a problem structure that tests the
     * solver's ability to handle problems with complex geometric relationships and high-dimensional
     * constraints. The problem serves as a benchmark for testing linear programming solvers on problems with
     * specific mathematical and geometric applications.
     */
    @Test
    @Tag("unstable")
    public void testD6CUBE() {
        CuteNetlibCase.doTest("D6CUBE.SIF", "315.49166666667315", "693.0000000000005", ACCURACY);
    }

    /**
     * DEGEN2 is a linear programming problem with 445 rows and 534 columns that is specifically designed to
     * test the solver's ability to handle degenerate problems. This problem is particularly challenging
     * because it exhibits significant degeneracy, where multiple basis pivots may lead to the same basic
     * feasible solution. The problem structure includes various constraint types and coefficient patterns
     * that can cause cycling or excessive iterations in simplex algorithms. DEGEN2 serves as a benchmark for
     * testing the robustness of linear programming solvers against degeneracy problems and their anti-cycling
     * strategies.
     */
    @Test
    @Tag("bm1000")
    public void testDEGEN2() {
        CuteNetlibCase.doTest("DEGEN2.SIF", "-1435.1779999999999", "-1226.12", ACCURACY);
    }

    /**
     * DEGEN3 is a larger linear programming problem with 1504 rows and 1818 columns that is specifically
     * designed to test the solver's ability to handle degenerate problems at scale. This problem is
     * particularly challenging because it exhibits significant degeneracy across a larger problem size, where
     * multiple basis pivots may lead to the same basic feasible solution. The problem structure includes
     * various constraint types and coefficient patterns that can cause cycling or excessive iterations in
     * simplex algorithms. DEGEN3 serves as a benchmark for testing the robustness of linear programming
     * solvers against degeneracy problems in larger-scale scenarios.
     */
    @Test
    public void testDEGEN3() {
        CuteNetlibCase.doTest("DEGEN3.SIF", "-987.2940000000001", "-876.2800000000008", ACCURACY);
    }

    /**
     * DFL001 is a large-scale linear programming problem with 6071 rows and 12230 columns that represents a
     * real-world airline schedule planning (fleet assignment) problem. This problem is particularly
     * interesting because it was preprocessed by a modified version of the KORBX System preprocessor, which
     * significantly reduced the problem size while maintaining the essential characteristics. The problem
     * tests the solver's ability to handle large-scale real-world optimization problems with complex
     * constraint structures and variable relationships. DFL001 serves as a benchmark for testing linear
     * programming solvers on problems with realistic airline operations and fleet management applications.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testDFL001() {
        CuteNetlibCase.doTest("DFL001.SIF", "1.126639604667184E7", null, ACCURACY);
    }

    /**
     * E226 is a linear programming problem with 224 rows and 282 columns that tests the solver's ability to
     * handle problems with specific numerical characteristics related to objective function shifts. This
     * problem is particularly interesting because it demonstrates how objective function modifications can
     * affect the optimal solution value, testing the solver's numerical precision and ability to handle
     * problems with specific objective function structures. The problem structure includes various constraint
     * types and coefficient patterns that can expose weaknesses in numerical algorithms and objective
     * function handling. Optimal value is stated to be -1.8751929066E+01 but there is a "RHS" of -7.113 given
     * for the objective row which should shift the solution by that amount to instead be -1.1638929066e+01.
     * Using CPLEX to parse the MPS file and then solve the problem confirms this.
     *
     * <pre>
     * 2021-10-01:
     * Dual simplex - Optimal:  Objective = -1.1638929066e+01
     * Solution time =    0.01 sec.  Iterations = 256 (58)
     * Deterministic time = 4.70 ticks  (710.92 ticks/sec)
     * </pre>
     */
    @Test
    @Tag("bm1000")
    public void testE226() {
        CuteNetlibCase.doTest("E226.SIF", "-11.638929066370546", "111.65096068931459", ACCURACY);
    }

    /**
     * ETAMACRO is a linear programming problem with 401 rows and 688 columns that tests the solver's ability
     * to handle problems with macro-level constraints and complex objective functions. This problem is
     * particularly interesting because it may involve constraints that represent aggregated or macro-level
     * relationships, testing the solver's ability to handle problems with specific constraint structures and
     * objective function characteristics. The problem structure includes various constraint types and
     * coefficient patterns that can expose weaknesses in numerical algorithms and constraint handling
     * capabilities.
     */
    @Test
    @Tag("bm1000")
    public void testETAMACRO() {
        CuteNetlibCase.doTest("ETAMACRO.SIF", "-755.7152312325337", "258.71905646302014", ACCURACY);
    }

    /**
     * FFFFF800 (also known as POWELL) is a linear programming problem with 525 rows and 854 columns that
     * tests the solver's ability to handle problems with specific numerical characteristics and constraint
     * structures. This problem is particularly interesting because it may involve constraints with specific
     * patterns or relationships that test the solver's numerical precision and ability to handle problems
     * with particular objective function characteristics. The problem structure includes various constraint
     * types and coefficient patterns that can expose weaknesses in numerical algorithms and computational
     * efficiency.
     */
    @Test
    @Tag("bm1000")
    public void testFFFFF800() {
        CuteNetlibCase.doTest("FFFFF800.SIF", "555679.5648174941", "1858776.4328128027", ACCURACY);
    }

    /**
     * FINNIS is a linear programming problem with 498 rows and 614 columns that represents a model for the
     * selection of alternative fuel types. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with specific industry applications and realistic constraint
     * structures related to fuel selection and energy optimization. The problem includes various constraint
     * types and variable bounds that test the solver's numerical precision and ability to handle problems
     * with industry-specific characteristics. FINNIS serves as a benchmark for testing linear programming
     * solvers on problems with realistic energy and fuel optimization applications.
     */
    @Test
    @Tag("bm1000")
    public void testFINNIS() {
        CuteNetlibCase.doTest("FINNIS.SIF", "172791.0655956116", null, ACCURACY);
    }

    /**
     * FIT1D is a linear programming problem with 25 rows and 1026 columns that represents a data fitting
     * problem with a large number of variables. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with a small number of constraints but a large number of variables,
     * creating a specific problem structure that can expose weaknesses in variable selection and basis
     * management algorithms. The problem structure includes various constraint types and coefficient patterns
     * that can test the solver's efficiency in handling problems with high variable-to-constraint ratios.
     */
    @Test
    public void testFIT1D() {
        CuteNetlibCase.doTest("FIT1D.SIF", "-9146.378092421019", "80453.99999999999", ACCURACY);
    }

    /**
     * FIT1P is a linear programming problem with 628 rows and 1677 columns that represents a data fitting
     * problem with a different structure than FIT1D. This problem is particularly interesting because it
     * tests the solver's ability to handle problems with a larger number of constraints and variables,
     * creating a different problem structure that can expose weaknesses in constraint handling and basis
     * management algorithms. The problem structure includes various constraint types and coefficient patterns
     * that can test the solver's efficiency in handling problems with different constraint-to-variable
     * ratios.
     */
    @Test
    public void testFIT1P() {
        CuteNetlibCase.doTest("FIT1P.SIF", "9146.378092420955", null, ACCURACY);
    }

    /**
     * FIT2D is a linear programming problem with 26 rows and 10500 columns that represents a two-dimensional
     * data fitting problem with an extremely large number of variables. This problem is particularly
     * challenging because it tests the solver's ability to handle problems with a very small number of
     * constraints but an enormous number of variables, creating a specific problem structure that can expose
     * weaknesses in memory management and variable selection algorithms. The problem structure includes
     * various constraint types and coefficient patterns that can test the solver's efficiency in handling
     * problems with extremely high variable-to-constraint ratios.
     */
    @Test
    public void testFIT2D() {
        CuteNetlibCase.doTest("FIT2D.SIF", "-68464.29329383196", "393548.6499999999", ACCURACY);
    }

    /**
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testFIT2P() {
        CuteNetlibCase.doTest("FIT2P.SIF", "68464.29329383207", null, ACCURACY);
    }

    /**
     * FORPLAN is a linear programming problem with 162 rows and 421 columns that represents a forest planning
     * optimization problem. This problem is particularly interesting because it tests the solver's ability to
     * handle problems with specific industry applications related to forestry and resource management. The
     * problem includes various constraint types and variable bounds that test the solver's numerical
     * precision and ability to handle problems with industry-specific characteristics. FORPLAN serves as a
     * benchmark for testing linear programming solvers on problems with realistic forestry and resource
     * planning applications.
     */
    @Test
    @Tag("bm1000")
    public void testFORPLAN() {
        CuteNetlibCase.doTest("FORPLAN.SIF", "-664.2189612722054", "2862.4274777342266", ACCURACY);
    }

    /**
     * GANGES is a linear programming problem with 1309 rows and 1681 columns that represents a large-scale
     * optimization problem with specific numerical characteristics. This problem is particularly interesting
     * because it tests the solver's ability to handle problems with a large number of constraints and
     * variables, creating a specific problem structure that can expose weaknesses in constraint handling and
     * basis management algorithms. The problem structure includes various constraint types and coefficient
     * patterns that can test the solver's efficiency in handling large-scale problems with complex numerical
     * characteristics.
     */
    @Test
    public void testGANGES() {
        CuteNetlibCase.doTest("GANGES.SIF", "-109585.73612927811", "-2.24E-12", ACCURACY);
    }

    /**
     * GFRD-PNC is a large-scale linear programming problem with 1092 rows and 542 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. GFRD-PNC serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testGFRD_PNC() {
        CuteNetlibCase.doTest("GFRD-PNC.SIF", "6902235.999548811", null, ACCURACY);
    }

    /**
     * GREENBEA is a large-scale linear programming problem with 2393 rows and 5405 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. GREENBEA serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testGREENBEA() {
        CuteNetlibCase.doTest("GREENBEA.SIF", "-7.255524812984598E7", null, ACCURACY);
    }

    /**
     * GREENBEB is a large-scale linear programming problem with 2393 rows and 5405 columns that represents a
     * variant of the GREENBEA problem with different bounds. This problem is particularly interesting because
     * it tests the solver's ability to handle problems with specific constraint structures and objective
     * function characteristics that may arise in real-world applications. The problem structure includes
     * various constraint types and coefficient patterns that can expose weaknesses in numerical algorithms
     * and computational efficiency. GREENBEB serves as a benchmark for testing linear programming solvers on
     * problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testGREENBEB() {
        CuteNetlibCase.doTest("GREENBEB.SIF", "-4302260.261206587", null, ACCURACY);
    }

    /**
     * GROW15 is a linear programming problem with 301 rows and 645 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. GROW15 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testGROW15() {
        CuteNetlibCase.doTest("GROW15.SIF", "-1.068709412935753E8", "0.0", ACCURACY);
    }

    /**
     * GROW22 is a linear programming problem with 441 rows and 946 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. GROW22 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testGROW22() {
        CuteNetlibCase.doTest("GROW22.SIF", "-1.608343364825636E8", "0.0", ACCURACY);
    }

    /**
     * GROW7 is a linear programming problem with 141 rows and 301 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. GROW7 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testGROW7() {
        CuteNetlibCase.doTest("GROW7.SIF", "-4.7787811814711526E7", "0.0", ACCURACY);
    }

    /**
     * ISRAEL is a linear programming problem with 175 rows and 142 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. ISRAEL serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testISRAEL() {
        CuteNetlibCase.doTest("ISRAEL.SIF", "-896644.8218630457", null, ACCURACY);
    }

    /**
     * KB2 is a linear programming problem with 44 rows and 41 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. KB2 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testKB2() {
        CuteNetlibCase.doTest("KB2.SIF", "-1.74990012991E+03", "0.0", ACCURACY);
    }

    /**
     * KEN-07 is a large-scale linear programming problem with 2427 rows and 3602 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. KEN-07 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testKEN_07() {
        CuteNetlibCase.doTest("KEN-07.SIF", "-6.795204433816869E8", "-1.61949281194431E8", ACCURACY);
    }

    /**
     * KEN-11 is a large-scale linear programming problem with 14695 rows and 21349 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. KEN-11 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testKEN_11() {
        CuteNetlibCase.doTest("KEN-11.SIF", "-6.972382262519971E9", "-1.287957080545934E9", ACCURACY);
    }

    /**
     * KEN-13 is a large-scale linear programming problem with 28633 rows and 42659 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. KEN-13 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testKEN_13() {
        CuteNetlibCase.doTest("KEN-13.SIF", "-1.0257394789482431E10", "-2.241281190609764E9", ACCURACY);
    }

    /**
     * KEN-18 is an extremely large-scale linear programming problem with 105,127 rows and 154,699 columns
     * that represents the largest member of the KEN family of optimization problems. This problem is
     * particularly challenging because it tests the solver's ability to handle problems of massive scale,
     * with over 100,000 constraints and 150,000 variables, making it one of the largest problems in the
     * netlib collection. The problem structure includes various constraint types and coefficient patterns
     * that can expose weaknesses in memory management, numerical algorithms, and computational efficiency
     * when dealing with problems of this magnitude. KEN-18 serves as a benchmark for testing the absolute
     * scalability limits of linear programming solvers and their ability to handle problems that approach the
     * practical limits of current computational resources. The problem's size makes it particularly
     * challenging for memory management and computational efficiency, testing the solver's ability to
     * maintain numerical stability while processing enormous amounts of data.
     */
    @Test
    @Disabled
    public void testKEN_18() {
        CuteNetlibCase.doTest("KEN-18.SIF", "1234567890", "1234567890", ACCURACY);
    }

    /**
     * LOTFI is a linear programming problem with 154 rows and 308 columns that involves audit staff
     * scheduling. This problem is particularly interesting because it represents a real-world multi-objective
     * linear programming problem that has been aggregated into a single objective. The problem tests the
     * solver's ability to handle problems with realistic scheduling constraints and operational
     * relationships. LOTFI serves as a benchmark for testing linear programming solvers on problems with
     * realistic business applications and complex constraint structures related to personnel scheduling and
     * resource allocation.
     */
    @Test
    @Tag("bm1000")
    public void testLOTFI() {
        CuteNetlibCase.doTest("LOTFI.SIF", "-25.26470606188002", null, ACCURACY);
    }

    /**
     * MAROS is a large-scale linear programming problem that represents an industrial production/allocation
     * model. This problem is particularly interesting because it tests the solver's ability to handle
     * problems with specific industrial applications and complex constraint structures. The problem includes
     * various constraint types and variable bounds that test the solver's numerical precision and ability to
     * handle problems with industry-specific characteristics. MAROS serves as a benchmark for testing linear
     * programming solvers on problems with realistic industrial applications and complex constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testMAROS() {
        CuteNetlibCase.doTest("MAROS.SIF", "-58063.743701138235", "-10623.409207717115", ACCURACY);
    }

    /**
     * MAROS-R7 is a large-scale linear programming problem that represents an image restoration problem done
     * via a goal programming approach. This problem is particularly interesting because it has a structured
     * constraint matrix with a band matrix in the first section and another band matrix with bandwidth 2 in
     * the second section. The problem tests the solver's ability to handle problems with specific matrix
     * structures and numerical characteristics that arise in image processing applications. MAROS-R7 serves
     * as a benchmark for testing linear programming solvers on problems with specific mathematical structures
     * and applications.
     */
    @Test
    @Tag("slow")
    public void testMAROS_R7() {
        CuteNetlibCase.doTest("MAROS-R7.SIF", "1497185.1664800502", null, ACCURACY);
    }

    /**
     * MODSZK1 is a linear programming problem that represents a real-life multi-sector economic planning
     * model. This problem is particularly interesting because it is very degenerate and demonstrates that a
     * dual simplex algorithm may require up to 10 times fewer iterations than a primal simplex algorithm. The
     * problem tests the solver's ability to handle problems with significant degeneracy and specific
     * numerical characteristics that can expose differences between primal and dual simplex approaches.
     * MODSZK1 serves as a benchmark for testing the efficiency of different linear programming algorithms on
     * degenerate problems.
     */
    @Test
    public void testMODSZK1() {
        CuteNetlibCase.doTest("MODSZK1.SIF", "320.6197293824883", null, ACCURACY);
    }

    /**
     * NESM is a large-scale linear programming problem with 2923 rows and 662 columns that represents a
     * real-world optimization scenario. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with a large number of constraints and a moderate number of
     * variables, creating a specific problem structure that can expose weaknesses in constraint handling and
     * basis management algorithms. The problem structure includes various constraint types and coefficient
     * patterns that can test the solver's efficiency in handling large-scale problems with complex numerical
     * characteristics.
     */
    @Test
    public void testNESM() {
        CuteNetlibCase.doTest("NESM.SIF", "1.4076036487562722E7", "3.6088214327411644E7", ACCURACY);
    }

    /**
     * OSA-07 is a large-scale linear programming problem with 1118 rows and 25067 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. OSA-07 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testOSA_07() {
        CuteNetlibCase.doTest("OSA-07.SIF", "535722.517299352", "4332086.205299969", ACCURACY);
    }

    /**
     * OSA-14 is a large-scale linear programming problem with 2337 rows and 52461 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. OSA-14 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testOSA_14() {
        CuteNetlibCase.doTest("OSA-14.SIF", "1106462.8447362552", "9377699.405100001", ACCURACY);
    }

    /**
     * OSA-30 is a large-scale linear programming problem with 4350 rows and 100024 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. OSA-30 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testOSA_30() {
        CuteNetlibCase.doTest("OSA-30.SIF", "2142139.873209757", "1.78441602883E7", ACCURACY);
    }

    /**
     * OSA-60 is a large-scale linear programming problem with 10280 rows and 232966 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. OSA-60 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testOSA_60() {
        CuteNetlibCase.doTest("OSA-60.SIF", "4044072.503163047", "4.25114540365E7", ACCURACY);
    }

    /**
     * PDS-02 is a large-scale linear programming problem with 2953 rows and 7716 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PDS-02 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testPDS_02() {
        CuteNetlibCase.doTest("PDS-02.SIF", "2.885786201E10", "2.931365171E10", ACCURACY);
    }

    /**
     * PDS-06 is a large-scale linear programming problem with 9881 rows and 28655 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PDS-06 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_06() {
        CuteNetlibCase.doTest("PDS-06.SIF", "2.77610376E10", "2.931366991E10", ACCURACY);
    }

    /**
     * PDS-10 is a large-scale linear programming problem with 16558 rows and 48763 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PDS-10 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testPDS_10() {
        CuteNetlibCase.doTest("PDS-10.SIF", "2.6727094976E10", "2.931368811E10", ACCURACY);
    }

    /**
     * PDS-20 is a large-scale linear programming problem with 33874 rows and 105728 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PDS-20 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    @Tag("unstable")
    public void testPDS_20() {
        CuteNetlibCase.doTest("PDS-20.SIF", "2.382165864E10", "2.931372451E10", ACCURACY);
    }

    /**
     * PEROLD is a linear programming problem with 626 rows and 1376 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. PEROLD serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("unstable")
    public void testPEROLD() {
        CuteNetlibCase.doTest("PEROLD.SIF", "-9380.755278235429", "-5878.539464724801", ACCURACY);
    }

    /**
     * PILOT is a large-scale linear programming problem with 1441 rows and 3652 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PILOT serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testPILOT() {
        CuteNetlibCase.doTest("PILOT.SIF", "-557.4897292730852", "-422.4724550733185", ACCURACY);
    }

    /**
     * PILOT-JA is a large-scale linear programming problem with 941 rows and 1988 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PILOT-JA serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testPILOT_JA() {
        CuteNetlibCase.doTest("PILOT-JA.SIF", "-6113.1364655813495", "-3095.1845377674813", ACCURACY);
    }

    /**
     * PILOT-WE is a large-scale linear programming problem with 723 rows and 2789 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PILOT-WE serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testPILOT_WE() {
        CuteNetlibCase.doTest("PILOT-WE.SIF", "-2720107.5328449034", "20770.464669007524", ACCURACY);
    }

    /**
     * PILOT4 is a linear programming problem with 411 rows and 1000 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. PILOT4 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testPILOT4() {
        CuteNetlibCase.doTest("PILOT4.SIF", "-2581.1392612778604", "0.0", ACCURACY);
    }

    /**
     * PILOT87 is a large-scale linear programming problem with 2031 rows and 4883 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PILOT87 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testPILOT87() {
        CuteNetlibCase.doTest("PILOT87.SIF", "301.7103473330999", null, ACCURACY);
    }

    /**
     * PILOTNOV is a large-scale linear programming problem with 976 rows and 2172 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. PILOTNOV serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testPILOTNOV() {
        CuteNetlibCase.doTest("PILOTNOV.SIF", "-4497.27618821887", "-957.3524818279784", ACCURACY);
    }

    /**
     * QAP12 is a linear programming problem with 3193 rows and 8856 columns that represents a quadratic
     * assignment problem with 12 facilities. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with specific combinatorial optimization characteristics and
     * complex constraint structures. The problem structure includes various constraint types and coefficient
     * patterns that can expose weaknesses in numerical algorithms and computational efficiency. QAP12 serves
     * as a benchmark for testing linear programming solvers on problems with specific combinatorial
     * optimization applications.
     */
    @Test
    @Tag("unstable")
    public void testQAP12() {
        CuteNetlibCase.doTest("QAP12.SIF", "522.8943505591718", "1104.1482908677572", ACCURACY);
    }

    /**
     * QAP15 is a linear programming problem with 6331 rows and 18000 columns that represents a quadratic
     * assignment problem with 15 facilities. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with specific combinatorial optimization characteristics and
     * complex constraint structures. The problem structure includes various constraint types and coefficient
     * patterns that can expose weaknesses in numerical algorithms and computational efficiency. QAP15 serves
     * as a benchmark for testing linear programming solvers on problems with specific combinatorial
     * optimization applications.
     */
    @Test
    @Tag("unstable")
    public void testQAP15() {
        CuteNetlibCase.doTest("QAP15.SIF", "1040.9940409587314", "2109.777554651187", ACCURACY);
    }

    /**
     * QAP8 is a linear programming problem with 912 rows and 2048 columns that represents a quadratic
     * assignment problem with 8 facilities. This problem is particularly interesting because it tests the
     * solver's ability to handle problems with specific combinatorial optimization characteristics and
     * complex constraint structures. The problem structure includes various constraint types and coefficient
     * patterns that can expose weaknesses in numerical algorithms and computational efficiency. QAP8 serves
     * as a benchmark for testing linear programming solvers on problems with specific combinatorial
     * optimization applications.
     */
    @Test
    @Tag("unstable")
    public void testQAP8() {
        CuteNetlibCase.doTest("QAP8.SIF", "2.0350000000E+02", null, ACCURACY);
    }

    /**
     * RECIPELP is a linear programming problem with 92 rows and 180 columns that represents a recipe
     * optimization problem. This problem is particularly interesting because it tests the solver's ability to
     * handle problems with specific constraint structures and objective function characteristics that may
     * arise in real-world applications. The problem structure includes various constraint types and
     * coefficient patterns that can expose weaknesses in numerical algorithms and computational efficiency.
     * RECIPELP serves as a benchmark for testing linear programming solvers on problems with specific
     * numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testRECIPELP() {
        CuteNetlibCase.doTest("RECIPELP.SIF", "-266.616", "-104.818", ACCURACY);
    }

    /**
     * SC105 is a linear programming problem with 106 rows and 103 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SC105 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSC105() {
        CuteNetlibCase.doTest("SC105.SIF", "-52.202061211707246", "0.0", ACCURACY);
    }

    /**
     * SC205 is a linear programming problem with 206 rows and 203 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SC205 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSC205() {
        CuteNetlibCase.doTest("SC205.SIF", "-52.202061211707246", "0.0", ACCURACY);
    }

    /**
     * SC50A is a linear programming problem with 51 rows and 48 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SC50A serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSC50A() {
        CuteNetlibCase.doTest("SC50A.SIF", "-64.57507705856449", "0.0", ACCURACY);
    }

    /**
     * SC50B is a linear programming problem with 51 rows and 48 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SC50B serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSC50B() {
        CuteNetlibCase.doTest("SC50B.SIF", "-7.0000000000E+01", "0.0", ACCURACY);
    }

    /**
     * SCAGR25 is a linear programming problem with 472 rows and 500 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCAGR25 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCAGR25() {
        CuteNetlibCase.doTest("SCAGR25.SIF", "-1.475343306076852E7", null, ACCURACY);
    }

    /**
     * SCAGR7 is a linear programming problem with 130 rows and 140 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCAGR7 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCAGR7() {
        CuteNetlibCase.doTest("SCAGR7.SIF", "-2331389.8243309837", null, ACCURACY);
    }

    /**
     * SCFXM1 is a linear programming problem with 331 rows and 457 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCFXM1 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCFXM1() {
        CuteNetlibCase.doTest("SCFXM1.SIF", "18416.75902834894", null, ACCURACY);
    }

    /**
     * SCFXM2 is a linear programming problem with 661 rows and 914 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCFXM2 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCFXM2() {
        CuteNetlibCase.doTest("SCFXM2.SIF", "36660.261564998815", null, ACCURACY);
    }

    /**
     * SCFXM3 is a linear programming problem with 991 rows and 1371 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCFXM3 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCFXM3() {
        CuteNetlibCase.doTest("SCFXM3.SIF", "54901.2545497515", null, ACCURACY);
    }

    /**
     * SCORPION is a linear programming problem with 389 rows and 358 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCORPION serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCORPION() {
        CuteNetlibCase.doTest("SCORPION.SIF", "1878.1248227381068", null, ACCURACY);
    }

    /**
     * SCRS8 is a linear programming problem with 491 rows and 1169 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCRS8 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCRS8() {
        CuteNetlibCase.doTest("SCRS8.SIF", "904.2969538007912", null, ACCURACY);
    }

    /**
     * SCSD1 is a linear programming problem with 78 rows and 760 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCSD1 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCSD1() {
        CuteNetlibCase.doTest("SCSD1.SIF", "8.666666674333367", null, ACCURACY);
    }

    /**
     * SCSD6 is a linear programming problem with 148 rows and 1350 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCSD6 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCSD6() {
        CuteNetlibCase.doTest("SCSD6.SIF", "50.5", null, ACCURACY);
    }

    /**
     * SCSD8 is a linear programming problem with 398 rows and 2750 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCSD8 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCSD8() {
        CuteNetlibCase.doTest("SCSD8.SIF", "905", null, ACCURACY);
    }

    /**
     * SCTAP1 is a linear programming problem with 301 rows and 480 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCTAP1 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSCTAP1() {
        CuteNetlibCase.doTest("SCTAP1.SIF", "1412.25", null, ACCURACY);
    }

    /**
     * SCTAP2 is a linear programming problem with 1091 rows and 1880 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCTAP2 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCTAP2() {
        CuteNetlibCase.doTest("SCTAP2.SIF", "1724.8071428568292", null, ACCURACY);
    }

    /**
     * SCTAP3 is a linear programming problem with 1481 rows and 2480 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SCTAP3 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSCTAP3() {
        CuteNetlibCase.doTest("SCTAP3.SIF", "1424.000000000573", null, ACCURACY);
    }

    /**
     * SEBA is a linear programming problem with 516 rows and 1028 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SEBA serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSEBA() {
        CuteNetlibCase.doTest("SEBA.SIF", "15711.6", "35160.46056", ACCURACY);
    }

    /**
     * SHARE1B is a linear programming problem with 118 rows and 225 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SHARE1B serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     * 110x330
     */
    @Test
    @Tag("bm1000")
    public void testSHARE1B() {
        CuteNetlibCase.doTest("SHARE1B.SIF", "-76589.31857918584", "74562.53714565346", ACCURACY.withPrecision(3));
    }

    /**
     * SHARE2B is a linear programming problem with 97 rows and 79 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. SHARE2B serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     * 93x172
     */
    @Test
    @Tag("bm1000")
    public void testSHARE2B() {
        CuteNetlibCase.doTest("SHARE2B.SIF", "-4.1573224074E+02", "-265.0981144446295", ACCURACY);
    }

    /**
     * SHELL is a large-scale linear programming problem with 536 rows and 1775 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHELL serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHELL() {
        CuteNetlibCase.doTest("SHELL.SIF", "1.208825346E9", null, ACCURACY);
    }

    /**
     * SHIP04L is a large-scale linear programming problem with 403 rows and 2118 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP04L serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP04L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SHIP04S is a large-scale linear programming problem with 403 rows and 1458 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP04S serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP04S() {
        CuteNetlibCase.doTest("SHIP04S.SIF", "1798714.7004453915", null, ACCURACY);
    }

    /**
     * SHIP08L is a large-scale linear programming problem with 779 rows and 4283 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP08L serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP08L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SHIP08S is a large-scale linear programming problem with 779 rows and 2387 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP08S serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP08S() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SHIP12L is a large-scale linear programming problem with 1152 rows and 5427 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP12L serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP12L() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SHIP12S is a large-scale linear programming problem with 1152 rows and 2763 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SHIP12S serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSHIP12S() {
        CuteNetlibCase.doTest("SHIP04L.SIF", "1793324.5379703548", null, ACCURACY);
    }

    /**
     * SIERRA is a large-scale linear programming problem with 1228 rows and 2036 columns that represents a
     * specific type of optimization problem with particular numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. SIERRA serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    public void testSIERRA() {
        CuteNetlibCase.doTest("SIERRA.SIF", "1.5394362183631932E7", "8.042913100947624E8", ACCURACY);
    }

    /**
     * STAIR is a linear programming problem with 357 rows and 467 columns that represents a specific type of
     * optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. STAIR serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSTAIR() {
        CuteNetlibCase.doTest("STAIR.SIF", "-251.26695119296787", "-208.79999", ACCURACY);
    }

    /**
     * STANDATA is a linear programming problem with 360 rows and 1075 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. STANDATA serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSTANDATA() {
        CuteNetlibCase.doTest("STANDATA.SIF", "1257.6995", null, ACCURACY);
    }

    /**
     * STANDGUB is a linear programming problem with 360 rows and 1075 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. STANDGUB serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSTANDGUB() {
        CuteNetlibCase.doTest("STANDGUB.SIF", "1257.6995", null, ACCURACY);
    }

    /**
     * STANDMPS is a linear programming problem with 468 rows and 1075 columns that represents a specific type
     * of optimization problem with particular numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. STANDMPS serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSTANDMPS() {
        CuteNetlibCase.doTest("STANDMPS.SIF", "1406.0175", null, ACCURACY);
    }

    /**
     * STOCFOR1 is a linear programming problem with 118 rows and 111 columns that represents a stochastic
     * programming problem with specific numerical characteristics. This problem is particularly interesting
     * because it tests the solver's ability to handle problems with specific constraint structures and
     * objective function characteristics that may arise in real-world applications. The problem structure
     * includes various constraint types and coefficient patterns that can expose weaknesses in numerical
     * algorithms and computational efficiency. STOCFOR1 serves as a benchmark for testing linear programming
     * solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    @Tag("bm1000")
    public void testSTOCFOR1() {
        CuteNetlibCase.doTest("STOCFOR1.SIF", "-41131.97621943626", null, ACCURACY);
    }

    /**
     * STOCFOR2 is a large-scale linear programming problem with 2158 rows and 2031 columns that represents a
     * stochastic programming problem with specific numerical characteristics. This problem is particularly
     * interesting because it tests the solver's ability to handle problems with specific constraint
     * structures and objective function characteristics that may arise in real-world applications. The
     * problem structure includes various constraint types and coefficient patterns that can expose weaknesses
     * in numerical algorithms and computational efficiency. STOCFOR2 serves as a benchmark for testing linear
     * programming solvers on problems with specific numerical characteristics and constraint relationships.
     */
    @Test
    public void testSTOCFOR2() {
        CuteNetlibCase.doTest("STOCFOR2.SIF", "-39024.4085378819", null, ACCURACY);
    }

    /**
     * STOCFOR3 is a very large-scale linear programming problem with 16676 rows and 15695 columns that
     * represents a stochastic programming problem with specific numerical characteristics. This problem is
     * particularly interesting because it tests the solver's ability to handle problems with specific
     * constraint structures and objective function characteristics that may arise in real-world applications.
     * The problem structure includes various constraint types and coefficient patterns that can expose
     * weaknesses in numerical algorithms and computational efficiency. STOCFOR3 serves as a benchmark for
     * testing linear programming solvers on problems with specific numerical characteristics and constraint
     * relationships.
     */
    @Test
    @Tag("slow")
    public void testSTOCFOR3() {
        CuteNetlibCase.doTest("STOCFOR3.SIF", "-39976.78394364959", null, ACCURACY);
    }

    /**
     * TRUSS is a large-scale linear programming problem with 1001 rows and 8806 columns that represents a
     * structural optimization problem for truss design. This problem is particularly interesting because it
     * tests the solver's ability to handle problems with specific engineering applications and complex
     * constraint structures related to structural analysis and design optimization. The problem includes
     * various constraint types and variable bounds that test the solver's numerical precision and ability to
     * handle problems with industry-specific characteristics. TRUSS serves as a benchmark for testing linear
     * programming solvers on problems with realistic engineering applications and complex constraint
     * relationships.
     */
    @Test
    @Tag("unstable")
    public void testTRUSS() {
        CuteNetlibCase.doTest("TRUSS.SIF", "458815.84718561685", null, ACCURACY);
    }

    /**
     * TUFF is a linear programming problem with 334 rows and 587 columns that is known for its challenging
     * numerical characteristics. This problem is particularly interesting because it tests the solver's
     * ability to handle problems with ill-conditioned basis matrices, dense matrix structures, and
     * significant degeneracy. The problem can cause issues like cycling or excessive iterations in simplex
     * algorithms, making it a benchmark for testing numerical stability and anti-cycling strategies. TUFF is
     * also known to have multiple optimal solutions, testing the solver's ability to handle problems with
     * non-unique solutions.
     */
    @Test
    @Tag("bm1000")
    public void testTUFF() {
        CuteNetlibCase.doTest("TUFF.SIF", "0.292147765093613", "0.894990186757432", ACCURACY);
    }

    /**
     * VTP-BASE is a linear programming problem with 199 rows and 203 columns that represents a base case for
     * a specific optimization problem. This problem is particularly interesting because it tests the solver's
     * ability to handle problems with balanced constraint and variable counts, creating a specific problem
     * structure that can expose weaknesses in basis management and pivot selection algorithms. The problem
     * structure includes various constraint types and coefficient patterns that can test the solver's
     * efficiency in handling problems with specific numerical characteristics.
     */
    @Test
    @Tag("bm1000")
    public void testVTP_BASE() {
        CuteNetlibCase.doTest("VTP-BASE.SIF", "129831.46246136136", null, ACCURACY);
    }

    /**
     */
    @Test
    @Tag("unstable")
    public void testWOOD1P() {
        CuteNetlibCase.doTest("WOOD1P.SIF", "1.44290241157344", "9.99999999999964", ACCURACY);
    }

    /**
     */
    @Test
    public void testWOODW() {
        CuteNetlibCase.doTest("WOODW.SIF", "1.30447633308416", "6.463675062936", ACCURACY);
    }

}
