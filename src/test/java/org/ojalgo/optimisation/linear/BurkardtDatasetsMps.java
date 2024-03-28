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

import static org.ojalgo.function.constant.BigMath.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * A small collection of datasets found here: http://people.sc.fsu.edu/~jburkardt/datasets/mps/
 *
 * @author apete
 */
public class BurkardtDatasetsMps extends OptimisationLinearTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(11, 9);

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("burkardt", modelName, false);

        // model.options.debug(Optimisation.Solver.class);
        // model.options.debug(IntegerSolver.class);
        // model.options.debug(ConvexSolver.class);
        // model.options.debug(LinearSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.mip_defer = 0.25;
        // model.options.mip_gap = 1.0E-5;

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, BurkardtDatasetsMps.ACCURACY);

        return model;
    }

    /**
     * Defines a problem of 57 rows and 97 columns. Seems to be the same model as adlittle at netlib. Netlib
     * also provides the solution.
     * <a href="http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/netlib/adlittle.html" >
     * adlittle@netlib</a>
     * <p>
     * Found this info somewhere on the net: "With 56 constraints and 97 variables adlittle is one of its
     * smaller members. While being in fact feasible, adlittle suffers from ill--posedness. Perturbing the
     * right hand side of the equality constraints by subtracting a tiny multiple of the 96th column of the
     * equation matrix renders the linear program infeasible.
     * <p>
     * Running this problem through CPLEX and lp_solve does again return a solution without any warnings."
     * FAIL: Hittar bara en lösning 0.0, oavsett om jag minimerrar eller maximerar.
     * <p>
     * 2010-04-19 lp_solve => 225494.96316238
     */
    @Test
    public void testMPSadlittle() {
        BurkardtDatasetsMps.doTest("adlittle.mps", "225494.96316238446", null);
    }

    /**
     * Defines a problem of 28 rows and 32 columns. Seems to be the same model as afiro at netlib. Netlib also
     * provides the solution.
     * <a href="http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/netlib/afiro.html">afiro@netlib</a>
     * OK! 2010-04-19 lp_solve => -464.75314286
     */
    @Test
    public void testMPSafiro() {
        BurkardtDatasetsMps.doTest("afiro.mps", "-.46475314286e+3", null);
    }

    /**
     * A simple test file primarily useful for checking the compression and decompression programs. Don't
     * think this model was designed to be solved; so I don't try to.
     */
    @Test
    public void testMPSempstest() {
        BurkardtDatasetsMps.doTest("empstest.mps", null, null);
    }

    /**
     * An example taken from Istvan Maros, Computational Techniques of the Simplex Method, Kluwer, 2003, page
     * 93. It defines a problem of 3 rows and 4 columns. There's also a model named maros at netlib, but
     * that's a different much larger model.
     *
     * @see #testMPSmarosCorrected()
     */
    @Test
    public void testMPSmaros() {
        BurkardtDatasetsMps.doTest("maros.mps", BigMath.DIVIDE.invoke(new BigDecimal("385"), THREE).toString(), "197.5");
    }

    /**
     * Discovered that the problem described in the comment (inside the MPS file) does not match the MPS
     * declaration, and the comment version makes more sense than the declaration. (The book has the same
     * mistake.)
     *
     * <pre>
     * RANGES
     *     RANGE1    BALANCE           10.0
     * </pre>
     *
     * should instead be
     *
     * <pre>
     * RANGES
     *     RANGE1    RES2              10.0
     * </pre>
     *
     * Further the MPS file does not contain an OBJSENSE section, but the comment/description states that it's
     * a maximisation problem. Funny thing is that when minimising the correct and incorrect models give the
     * same solution, but when maximising they differ, and the minimisation solution is much more "even"
     * (numbers better suited for a calculation example).
     * <p>
     * Results using CPLEX:
     * <p>
     * MIN: OPTIMAL 128.33333333333331 @ { 3.33333333333333, 13.33333333333333, 2E+1, 0 }
     * <p>
     * MAX: OPTIMAL 177.24137931034483 @ { 29.31034482758621, 12.06896551724138, -1E+1, 13.79310344827586 }
     *
     * @see #testMPSmaros()
     */
    @Test
    public void testMPSmarosCorrected() {
        BurkardtDatasetsMps.doTest("maros_corrected.mps", BigMath.DIVIDE.invoke(new BigDecimal("385"), THREE).toString(), "177.24137931034483");
    }

    /**
     * An example taken from Nazareth, which defines a problem of 3 rows and 3 column. OK! 2010-04-19 lp_solve
     * => This problem is unbounded (maximisation seems OK.)
     */
    @Test
    public void testMPSnazareth() {
        BurkardtDatasetsMps.doTest("nazareth.mps", null, BigMath.DIVIDE.invoke(HUNDRED.add(TEN), THREE).toString());
    }

    /**
     * A simple problem with 4 rows and 3 variables. I got my version from here (same numbers but different
     * names): <a href="http://en.wikipedia.org/wiki/MPS_(format)">testprob@wikipedia</a> and/or
     * <a href="http://lpsolve.sourceforge.net/5.5/mps-format.htm">testprob@lpsolve</a>
     */
    @Test
    public void testMPStestprob() {

        ExpressionsBasedModel parsedModel = BurkardtDatasetsMps.doTest("testprob.mps", "54", "80");

        ExpressionsBasedModel reimplementedModel = new ExpressionsBasedModel();


        Variable tmpXONE = reimplementedModel.newVariable("XONE").weight(ONE).lower(ZERO).upper(FOUR);
        Variable tmpYTWO = reimplementedModel.newVariable("YTWO").weight(FOUR).lower(NEG).upper(ONE);
        Variable tmpZTHREE = reimplementedModel.newVariable("ZTHREE").weight(NINE).lower(ZERO).upper(null);

        Variable[] tmpVariables = { tmpXONE, tmpYTWO, tmpZTHREE };


        Expression tmpLIM1 = reimplementedModel.newExpression("LIM1");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM1.set(v, new BigDecimal[] { ONE, ONE, ZERO }[v]);
        }
        tmpLIM1.upper(FIVE);

        Expression tmpLIM2 = reimplementedModel.newExpression("LIM2");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM2.set(v, new BigDecimal[] { ONE, ZERO, ONE }[v]);
        }
        tmpLIM2.lower(TEN);

        Expression tmpMYEQN = reimplementedModel.newExpression("MYEQN");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpMYEQN.set(v, new BigDecimal[] { ZERO, ONE.negate(), ONE }[v]);
        }
        tmpMYEQN.level(SEVEN);

        TestUtils.assertTrue(reimplementedModel.validate());
        TestUtils.assertTrue(parsedModel.validate());

        Result tmpExpMinRes = reimplementedModel.minimise();
        Result tmpActMinRes = parsedModel.minimise();

        TestUtils.assertEquals(tmpExpMinRes.getValue(), tmpActMinRes.getValue(), BurkardtDatasetsMps.ACCURACY);

        TestUtils.assertEquals(tmpVariables.length, tmpExpMinRes.count());
        TestUtils.assertEquals(tmpVariables.length, tmpActMinRes.count());

        TestUtils.assertStateAndSolution(tmpExpMinRes, tmpActMinRes, BurkardtDatasetsMps.ACCURACY);

        for (int i = 0; i < tmpVariables.length; i++) {
            TestUtils.assertEquals(tmpVariables[i].getName(), tmpExpMinRes.doubleValue(i), tmpActMinRes.doubleValue(i), BurkardtDatasetsMps.ACCURACY);
        }

        if (!reimplementedModel.validate(tmpExpMinRes, BurkardtDatasetsMps.ACCURACY)) {
            TestUtils.fail("Solution not valid!");
        }

        if (!parsedModel.validate(tmpActMinRes, BurkardtDatasetsMps.ACCURACY)) {
            TestUtils.fail("Solution not valid!");
        }
    }
}
