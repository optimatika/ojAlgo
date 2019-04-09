/*
 * Copyright 1997-2019 Optimatika
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
import org.ojalgo.optimisation.ModelFileMPS;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * A small collection of datasets found here: http://people.sc.fsu.edu/~jburkardt/datasets/mps/maros.mps
 *
 * @author apete
 */
public class BurkardtDatasetsMps extends OptimisationLinearTests implements ModelFileMPS {

    static NumberContext PRECISION = new NumberContext(11, 9);

    static ExpressionsBasedModel doTest(String modelName, String expMinValString, String expMaxValString) {
        return ModelFileMPS.makeAndAssert("burkardt", modelName, expMinValString, expMaxValString, false, BurkardtDatasetsMps.PRECISION, null);
    }

    /**
     * Defines a problem of 57 rows and 97 columns. Seems to be the same model as adlittle at netlib. Netlib
     * also provides the solution.
     * <a href="http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/netlib/adlittle.html" >
     * adlittle@netlib</a> Found this info somewhere on the net: "With 56 constraints and 97 variables
     * adlittle is one of its smaller members. While being in fact feasible, adlittle suffers from
     * ill--posedness. Perturbing the right hand side of the equality constraints by subtracting a tiny
     * multiple of the 96th column of the equation matrix renders the linear program infeasible. Running this
     * problem through CPLEX and lp_solve does again return a solution without any warnings." FAIL: Hittar
     * bara en lösning 0.0, oavsett om jag minimerrar eller maximerar. 2010-04-19 lp_solve => 225494.96316238
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
     * An example taken from Maros, which defines a problem of 3 rows and 4 columns. There's also a model
     * named maros at netlib, but that's a different much larger model. ERROR: Något tar lång tid, och sedan
     * blir det ArithmaticError. 2010-04-19 lp_solve => 128.33333333
     */
    @Test
    public void testMPSmaros() {
        BurkardtDatasetsMps.doTest("maros.mps", BigMath.DIVIDE.invoke(new BigDecimal("385"), THREE).toString(), "197.5");
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

        final Variable tmpXONE = new Variable("XONE").weight(ONE).lower(ZERO).upper(FOUR);
        final Variable tmpYTWO = new Variable("YTWO").weight(FOUR).lower(NEG).upper(ONE);
        final Variable tmpZTHREE = new Variable("ZTHREE").weight(NINE).lower(ZERO).upper(null);

        final Variable[] tmpVariables = new Variable[] { tmpXONE, tmpYTWO, tmpZTHREE };

        final ExpressionsBasedModel reimplementedModel = new ExpressionsBasedModel(tmpVariables);

        final Expression tmpLIM1 = reimplementedModel.addExpression("LIM1");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM1.set(v, new BigDecimal[] { ONE, ONE, ZERO }[v]);
        }
        tmpLIM1.upper(FIVE);

        final Expression tmpLIM2 = reimplementedModel.addExpression("LIM2");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpLIM2.set(v, new BigDecimal[] { ONE, ZERO, ONE }[v]);
        }
        tmpLIM2.lower(TEN);

        final Expression tmpMYEQN = reimplementedModel.addExpression("MYEQN");
        for (int v = 0; v < tmpVariables.length; v++) {
            tmpMYEQN.set(v, new BigDecimal[] { ZERO, ONE.negate(), ONE }[v]);
        }
        tmpMYEQN.level(SEVEN);

        TestUtils.assertTrue(reimplementedModel.validate());
        TestUtils.assertTrue(parsedModel.validate());

        final Result tmpExpMinRes = reimplementedModel.minimise();
        final Result tmpActMinRes = parsedModel.minimise();

        TestUtils.assertEquals(tmpExpMinRes.getValue(), tmpActMinRes.getValue(), BurkardtDatasetsMps.PRECISION);

        TestUtils.assertEquals(tmpVariables.length, tmpExpMinRes.count());
        TestUtils.assertEquals(tmpVariables.length, tmpActMinRes.count());

        TestUtils.assertEquals(tmpExpMinRes, tmpActMinRes, BurkardtDatasetsMps.PRECISION);

        for (int i = 0; i < tmpVariables.length; i++) {
            TestUtils.assertEquals(tmpVariables[i].getName(), tmpExpMinRes.doubleValue(i), tmpActMinRes.doubleValue(i), BurkardtDatasetsMps.PRECISION);
        }

        if (!reimplementedModel.validate(tmpExpMinRes, BurkardtDatasetsMps.PRECISION)) {
            TestUtils.fail(ModelFileMPS.SOLUTION_NOT_VALID);
        }

        if (!parsedModel.validate(tmpActMinRes, BurkardtDatasetsMps.PRECISION)) {
            TestUtils.fail(ModelFileMPS.SOLUTION_NOT_VALID);
        }
    }
}
