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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Had some issues with the TUFF model. Had an idea to try and generate smaller models with similar properties
 * by fixing most variables to the value of a known solution, and then "simplifying" the model. This didn't
 * work well. The generated models become infeasible due to very tight numerical bounds. Need to improve how
 * these bounds are set.
 *
 * <pre>
The TUFF instance from the Netlib collection of LP (linear programming) models is notable for its challenging characteristics, which test certain aspects of an LP solver’s numerical stability and performance. The key thing for the LP solver to “get right” to solve TUFF effectively is managing numerical stability and precision. Here’s what that entails:

Key Challenges of TUFF
1.  Ill-Conditioned Basis Matrices:
TUFF features constraint matrices that can lead to basis matrices with very high or low condition numbers. This means that small changes or numerical errors can greatly affect calculations, particularly during pivoting in the simplex method.
2.  Dense Matrix Structure:
The TUFF model has a relatively high density compared to some other Netlib instances. This density can affect both memory access patterns and numerical computations, requiring the solver to be efficient in matrix operations.
3.  Degeneracy:
The TUFF instance exhibits significant degeneracy, where multiple basis pivots may lead to the same basic feasible solution. This can cause issues like cycling or excessive iterations, especially for naive simplex implementations.
4.  Numerical Precision:
Due to the combination of ill-conditioning and potential degeneracy, the TUFF instance is particularly sensitive to numerical precision. Finite precision arithmetic can lead to incorrect pivots, infeasible solutions, or convergence issues if the solver is not robustly designed.

Key Solver Requirements for TUFF
1.  Precision in Linear Algebra:
•   Using techniques like stable factorization methods (e.g., LU decomposition with partial pivoting).
•   Handling small pivot elements carefully to avoid numerical issues.
•   Implementing advanced basis update strategies to minimize accumulation of numerical errors.
2.  Handling Degeneracy:
•   The solver should use anti-cycling techniques such as Bland’s Rule or perturbation methods to prevent infinite loops or excessive iterations caused by degenerate pivots.
3.  Scaling of Problem Data:
•   Scaling the constraint matrix and objective coefficients effectively to normalize values can reduce condition numbers and improve numerical stability.
•   Equilibration scaling (row and column scaling) can help mitigate numerical instability.
4.  Refinements and Post-Solution Analysis:
•   Employing iterative refinement techniques to verify the accuracy of solutions.
•   Adjusting the primal and dual feasibility tolerances dynamically to ensure that numerical approximations do not introduce inaccuracies.
5.  Efficient Handling of Dense Structures:
•   Optimizing data structures for dense constraint matrices to improve memory usage and computational speed.

Conclusion

The TUFF instance primarily tests the solver’s ability to handle numerical challenges arising from ill-conditioning, degeneracy, and the dense structure of the problem. Solvers like CPLEX, Gurobi, and modern implementations of the simplex or interior-point methods tend to succeed on TUFF due to their advanced preprocessing, numerical stability enhancements, and robustness to degeneracy. Older or simpler solvers may struggle without these techniques.     *
 * </pre>
 *
 * <pre>
Yes, the linear programming problem named TUFF from the Netlib collection is known to have multiple optimal solutions.

Why TUFF Has Multiple Optimal Solutions

The existence of multiple optimal solutions in a linear programming problem generally occurs when:
    1.  Degeneracy at the Optimal Point: TUFF is a degenerate problem, meaning that some basic variables are at their lower or upper bounds (e.g., 0 for non-negativity constraints), and multiple combinations of basic variables yield the same objective value.
    2.  Flat Optimal Face: The problem’s feasible region includes a higher-dimensional face of the polytope that is parallel to the objective function’s direction vector. All points along this face share the same objective value, leading to multiple solutions.

In TUFF, the feasible region has such a structure, making it possible to have more than one optimal solution that achieves the same minimum or maximum objective value.

Practical Implications for Solvers

Solvers handle this situation by:
    •   Returning any one of the multiple optimal solutions (depending on the pivoting strategy used internally in the Simplex algorithm or tie-breaking in other methods like interior-point methods).
    •   Providing additional tools for exploring the solution space, such as sensitivity analysis or advanced basis manipulation, which can help identify alternative optima.

Testing and Analysis

If you’d like to explore this further:
    1.  Use an LP Solver: Experiment with TUFF in solvers like Gurobi, CPLEX, or open-source solvers like GLPK or COIN-OR.
    2.  Perturb the Objective Coefficients: Introduce a small perturbation to the objective function coefficients to see how the optimal solution changes. If the solution remains similar, it is likely one of multiple optimal points.
 * </pre>
 */
@Disabled
public class CaseTUFF extends OptimisationLinearTests {

    private static final NumberContext ACCURACY = NumberContext.of(8);
    private static final Optimisation.Result MAX_CPLEX = Optimisation.Result.parse(
            "OPTIMAL 0.8949901867574324 @ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0002517499693, 0, 11.91620599160794, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6.46640036517508, 1.35415971096586, 0, 0, 0, 3.478695, 0, 0, 0, 17.47425218834777, 0.59899791231733, 7.55636743215031, 0, 0, 0, 0, 0, 0.67149944737812, 0.7924978509149, 0, 0, 11.3862300522109, 0, 0.48854524140701, 0, 0, 0, 0, 0, 4.98222982709315, 0, 0, 23.57825569859284, 0, 0, 0, 0, 0, 0, 4.41067675142492, 0, 0, 0, 0, 0, 0, 0, 21.9327, 0, 0, 0, 0, 0, 0, 0, 5.702502, 4.2981393, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6.06818903788771, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.15079478776344, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2.3910393365673, 8.32701637731974, 0, 0, 0, 0, 0, 0, 0, 0, 3.35801204254128, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0.5546540206435, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.93181096211229, 0, 0, 0, 0, 0.0005034999386, 0, 0, 0, 0, 0, 0, 2.59182511179801, 0, 0, 0, 0, 0, 0, 0, 0, 0, 12.6089606634327, 2.53802153000518, 0, 0, 0, 0, 0, 0, 0, 0, 3.99460984777422, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.39399361414712, 4.40528634361233, 9.67400881057269, 8, 0, 2.9E+2, 1E+2, 0, 134.9993859756846, 0, 19, 0.00029, 29, 29, 0, 0, 12.847, 0, 16.62931430009743, 0.000166293143, 0, 0.00012847, 12.847, 0, 2.45370911260773, 11.50656195526434, 178.1030471647291, 5.00745425518851, 0, 0, 0, 37.93975193417659, 0, 24.73330918828632, 2.9E+2, 15, 0, 34.07938597568466, 2, 3.8930303030303, 1E+1, 8.9539696969697, 0, 4.98222982709315, 23.57850744856214, 49.60047657720833, 0, 0, 18.96390310337938, 17.604305, 21.083, 16.62931430009743, 13.78902741764079, 16.62931430009743, 178.1030471647291, 4.40528634361233, 0, 0, 0, 0, 0, 0, 2.9E+2, 0, 0, 0, 0, 65.92061402431534, 34.07938597568466, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.98222982709315, 0.0002517499693, 0, 0, 23.57825569859284, 0, 0, 0, 26.83687593862304, 0, 22.76360063858529, 0, 0, 34.99938597568464, 0, 0, 17.604305, 1.35959810337938, 17.604305, 0, 21.083, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5.3180056490237, 2.45370911260773, 0, 0, 0, 0, 16.62931430009743, 0, 13.78902741764079, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 29, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16.62931430009743, 0, 5.702502, 4.2981393, 0, 0, 0, 11.94088407285439, 0, 0, 0.0005034999386, 18.65882589116943, 0.0005034999386, 45.49570182979247, 0, 0, 0, 0, 0, 0, 0, 24.82687407866327, 0, 0, 0, 0, 36.54, 5.307, 0, 3.478695, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.41067675142492, 0, 0, 7.55636743215031, 0, 0, 0, 0, 0, 0, 4.98222982709315, 7.54, 5.307, 0, 0, 0, 0, 7.54, 5.307, 0, 0, 0, 7.54, 5.307, 0, 0, 127.0886065195554, 93.21434199256518, 26.84970560272297, 19.03633060104538, 25.05972522920811, 18.01958424696107, -0.00001008621578, -5.94880784075712, 0.10176211453744, 1100.41, 1.50884142957608, 2.3622071839052, 7.68063268408225, 0, 10.28648410336293, 0, 0, 0, 1, 0, 0.06975506115001, 916.0008387572149, 10629.14282703522, 11894.61532770095, 2.23714420782156, 1729.724614927527, 10094.04216609621, 6454.206826749304, 4291.310018649769, 2331.720421217548, 2507.584639233157, 0.45687193442509, 355.768453785535, 2384.077218652286, 1567.612737876903, 980.9711312575273, 2237.466531134458, 2417.991784874088, 0.2891788326642, 345.8242081630719, 2273.557490910291, 1257.862272314687, 809.6218920205692 }");
    private static final Optimisation.Result MIN_CPLEX = Optimisation.Result.parse(
            "OPTIMAL 0.2921477650936127 @ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10.55675233064598, 0, 5.8579416, 0, 0, 0, 0, 0, 0, 0, 5.30481153688305, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.20197532595203, 0, 0, 0, 3.078, 0, 0, 0.4106865253817, 0.603, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3.20082766328859, 0, 0, 0, 10.23493324227968, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.10905441886198, 0, 0, 0, 0.927, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.92324766935402, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3.63657028203399, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.20461500142037, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.33242296619824, 0, 0, 0, 0, 0, 2.052, 0, 0, 0, 0, 0, 0, 0.0254096726276, 0, 0, 0, 0, 0, 0, 0.1653134746183, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.24278644187545, 0, 0, 0, 0, 0, 0, 0, 0.84, 0, 0, 0, 0, 0, 0.52220486730155, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2.5E+2, 9E+1, 5.13, 9E+1, 0, 0, 0, 0, 0, 0, 0.00013425042693, 31.5, 13.42504269266594, 13.22221104, 0.0001322221104, 0, 0.00018074957307, 18.07495730733406, 0, 5.1, 10.31288110517017, 58.13740525362893, 2.06, 0, 0, 4.575, 33.29984736, 0, 0, 2.5E+2, 0, 0, 3, 0, 5.47725979010123, 0, 26.02274020989877, 0, 0, 5.8579416, 27.89496, 2.052, 0, 0, 0, 0, 13.22221104, 33.79063925527049, 13.22221104, 58.13740525362893, 0, 0, 0, 0, 0, 0, 0, 2.5E+2, 0, 0, 0, 0, 87, 3, 30.96, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5.8579416, 0, 27.89496, 0, 0, 0, 0, 0, 0, 5.13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4.575, 0, 0, 0, 8.81480736, 0, 0, 0, 5.076, 5.1, 0, 0, 0, 13.22221104, 0, 10.473313364784, 0, 0, 0, 0, 0, 0, 0, 0, 9.64723567894974, 0, 0, 0, 13.67009021153675, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13.42504269266594, 0, 0, 0, 13.22221104, 0, 0, 13.67009021153675, 0, 0, 0, 8.94138181891704, 0, 0, 0, 0, 0, 0, 0, 27.89496, 0, 0, 0, 0, 0, 0, 0, 0.84, 0, 0, 0, 31.5, 0, 0, 0, 0, 0, 0, 0, 15.48, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3.078, 0, 0, 0, 0, 0, 31.5, 0, 0, 0, 0, 0, 31.5, 0, 0, 0, 0, 18.07495730733406, 0, 0, 0, 41.48498264329299, 31.47024127964514, 8.76443295280838, 6.57253563858762, 8.18013742262116, 6.1312234952228, 0.00000303332984, -27.1435241550801, 0, 1100.41, 0.71272453936, 0.74820312688, 6.44707684238269, 0, 6.12407353739546, 0, 6.51522226634095, 5.20702783654795, 1, 18.175, 0.29168300965635, 977.4700000000001, 3537.992929898552, 4078.073982714036, 0.44634755446137, 302.5822418491667, 3652.894905549751, 1866.998608184139, 897.5324994564497, 751.6704169706741, 868.2000678738065, 0.1577408553261, 56.01100340252039, 783.8697951157543, 386.3082643645162, 198.4412036615181, 727.8937612392778, 828.773663899706, 0.00444770870853, 112.537155363887, 695.3718044192874, 384.8351829916776, 266.9639864868758 }");
    private static final int NB_VARS_IN_MODEL = 587;
    private static final NumberContext PRECISION = NumberContext.of(16);
    private static final Random RANDOM = new Random();

    public static void main(final String... args) {

        for (int nbVarsInReducedModel = 2; nbVarsInReducedModel < NB_VARS_IN_MODEL; nbVarsInReducedModel++) {

            Set<Integer> remaining = new TreeSet<>();

            while (remaining.size() < nbVarsInReducedModel) {
                remaining.add(RANDOM.nextInt(NB_VARS_IN_MODEL));
            }

            // MIN

            ExpressionsBasedModel minModel = ModelFileTest.makeModel("netlib", "TUFF.SIF", false);

            for (int i = 0; i < NB_VARS_IN_MODEL; i++) {
                if (!remaining.contains(i)) {
                    BigDecimal fixed = PRECISION.toBigDecimal(MIN_CPLEX.doubleValue(i));
                    minModel.getVariable(i).level(fixed);
                }
            }

            Result actualMinRes = minModel.minimise();

            try {
                TestUtils.assertStateNotLessThanOptimal(actualMinRes);
                TestUtils.assertEquals(MIN_CPLEX.getValue(), actualMinRes.getValue(), ACCURACY);
            } catch (final AssertionError cause) {
                BasicLogger.debug("Failed MIN: {}", remaining);
                throw new RuntimeException(cause);
            }

            // MAX

            ExpressionsBasedModel maxModel = ModelFileTest.makeModel("netlib", "TUFF.SIF", false);

            for (int i = 0; i < NB_VARS_IN_MODEL; i++) {
                if (!remaining.contains(i)) {
                    BigDecimal fixed = PRECISION.toBigDecimal(MAX_CPLEX.doubleValue(i));
                    maxModel.getVariable(i).level(fixed);
                }
            }

            Result actualMaxRes = maxModel.maximise();

            try {
                TestUtils.assertStateNotLessThanOptimal(actualMaxRes);
                TestUtils.assertEquals(MAX_CPLEX.getValue(), actualMaxRes.getValue(), ACCURACY);
            } catch (final AssertionError cause) {
                BasicLogger.debug("Failed MAX: {}", remaining);
                throw new RuntimeException(cause);
            }
        }
    }

    static void doTestMax(final Set<Integer> remaining) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", "TUFF.SIF", false);

        if (DEBUG) {
            model.options.debug(LinearSolver.class);
        }

        for (int i = 0; i < NB_VARS_IN_MODEL; i++) {
            if (!remaining.contains(i)) {
                BigDecimal fixed = PRECISION.toBigDecimal(MAX_CPLEX.doubleValue(i));
                model.getVariable(i).level(fixed);
            }
        }

        ExpressionsBasedModel simplified = model.simplify();

        Result maxSimplified = simplified.maximise();

        TestUtils.assertStateNotLessThanOptimal(maxSimplified);

        Result maxLevelled = model.maximise();

        TestUtils.assertStateNotLessThanOptimal(maxLevelled);
    }

    static void doTestMin(final Set<Integer> remaining) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", "TUFF.SIF", false);

        for (int i = 0; i < NB_VARS_IN_MODEL; i++) {
            if (!remaining.contains(i)) {
                BigDecimal fixed = PRECISION.toBigDecimal(MIN_CPLEX.doubleValue(i));
                model.getVariable(i).level(fixed);
            }
        }

        ExpressionsBasedModel simplified = model.simplify();

        Result minSimplified = simplified.minimise();

        TestUtils.assertStateNotLessThanOptimal(minSimplified);

        Result minLevelled = model.minimise();

        TestUtils.assertStateNotLessThanOptimal(minLevelled);
    }

    static TreeSet<Integer> toSet(final int... indices) {
        return Arrays.stream(indices).boxed().collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Failed MAX: [66, 72, 73, 75, 103, 117, 140, 167, 213, 234, 243, 271, 307, 327, 372, 410, 413, 503, 560,
     * 571]
     * <p>
     * 20
     */
    @Test
    public void testFailure01() {

        Set<Integer> remaining = CaseTUFF.toSet(66, 72, 73, 75, 103, 117, 140, 167, 213, 234, 243, 271, 307, 327, 372, 410, 413, 503, 560, 571);

        CaseTUFF.doTestMax(remaining);
    }

    /**
     * Failed MIN: [41, 66, 71, 72, 98, 138, 152, 170, 172, 188, 217, 249, 271, 304, 313, 340, 343, 346, 357,
     * 413, 415, 427, 455, 492, 505, 507, 536, 549, 553]
     * <p>
     * 29
     */
    @Test
    public void testFailure02() {

        Set<Integer> remaining = CaseTUFF.toSet(41, 66, 71, 72, 98, 138, 152, 170, 172, 188, 217, 249, 271, 304, 313, 340, 343, 346, 357, 413, 415, 427, 455,
                492, 505, 507, 536, 549, 553);

        CaseTUFF.doTestMin(remaining);
    }

    /**
     * Failed MAX: [8, 33, 47, 54, 149, 177, 233, 234, 275, 293, 323, 330, 349, 357, 427, 458, 578]
     * <p>
     * 17
     */
    @Test
    public void testFailure03() {

        Set<Integer> remaining = CaseTUFF.toSet(8, 33, 47, 54, 149, 177, 233, 234, 275, 293, 323, 330, 349, 357, 427, 458, 578);

        CaseTUFF.doTestMax(remaining);
    }

    /**
     * Failed MAX: [87, 144, 174, 179, 270, 278, 294, 434, 471]
     * <p>
     * 9
     */
    @Test
    public void testFailure04() {

        Set<Integer> remaining = CaseTUFF.toSet(87, 144, 174, 179, 270, 278, 294, 434, 471);

        CaseTUFF.doTestMax(remaining);
    }

    /**
     * Failed MAX: [1, 71, 228, 278, 292, 368, 433]
     * <p>
     * 7
     */
    @Test
    public void testFailure05() {

        Set<Integer> remaining = CaseTUFF.toSet(1, 71, 228, 278, 292, 368, 433);

        CaseTUFF.doTestMax(remaining);
    }

    /**
     * Failed MIN: [38, 87, 116, 131, 207, 221, 292, 312, 382, 476, 496]
     * <p>
     * 11
     */
    @Test
    public void testFailure06() {

        Set<Integer> remaining = CaseTUFF.toSet(38, 87, 116, 131, 207, 221, 292, 312, 382, 476, 496);

        CaseTUFF.doTestMin(remaining);
    }

}
