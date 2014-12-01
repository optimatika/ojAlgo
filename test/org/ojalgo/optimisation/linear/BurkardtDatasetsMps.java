/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se) Permission is hereby granted, free of charge, to any person
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
package org.ojalgo.optimisation.linear;

import static org.ojalgo.constant.BigMath.*;

import java.io.File;
import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.access.Access1D;
import org.ojalgo.function.BigFunction;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: http://people.sc.fsu.edu/~burkardt/datasets/mps/mps.html
 *
 * @author apete
 */
public class BurkardtDatasetsMps extends OptimisationLinearTests {

    private static final String COMPOSITION_NOT_VALID = " Composition not valid!";
    private static final String PATH = "./test/org/ojalgo/optimisation/linear/";
    private static final NumberContext PRECISION = new NumberContext(7, 6);
    private static final String SOLUTION_NOT_VALID = "Solution not valid!";

    public BurkardtDatasetsMps() {
        super();
    }

    public BurkardtDatasetsMps(final String someName) {
        super(someName);
    }

    /**
     * Defines a problem of 57 rows and 97 columns. Seems to be the same model as adlittle at netlib. Netlib also
     * provides the solution. <a
     * href="http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/netlib/adlittle.html">adlittle@netlib</a> Found
     * this info somewhere on the net: "With 56 constraints and 97 variables adlittle is one of its smaller members.
     * While being in fact feasible, adlittle suffers from ill--posedness. Perturbing the right hand side of the
     * equality constraints by subtracting a tiny multiple of the 96th column of the equation matrix renders the linear
     * program infeasible. Running this problem through CPLEX and lp_solve does again return a solution without any
     * warnings." FAIL: Hittar bara en lösning 0.0, oavsett om jag minimerrar eller maximerar. 2010-04-19 lp_solve =>
     * 225494.96316238
     */
    public void testMPSadlittle() {

        final File tmpFile = new File(PATH + "adlittle.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpModel = ExpressionsBasedModel.make(tmpMPS);

        //tmpModel.options.debug(LinearSolver.class);

        TestUtils.assertTrue(tmpModel.validate());

        final BigDecimal tmpExpVal = new BigDecimal("225494.96316238446"); // Stated to be .22549496316e+6
        final double tmpActVal = tmpModel.minimise().getValue();

        TestUtils.assertEquals(tmpExpVal.doubleValue(), tmpActVal, PRECISION);

        if (!tmpModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }
    }

    /**
     * Defines a problem of 28 rows and 32 columns. Seems to be the same model as afiro at netlib. Netlib also provides
     * the solution. <a
     * href="http://www-new.mcs.anl.gov/otc/Guide/TestProblems/LPtest/netlib/afiro.html">afiro@netlib</a> OK! 2010-04-19
     * lp_solve => -464.75314286
     */
    public void testMPSafiro() {

        final File tmpFile = new File(PATH + "afiro.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpModel = ExpressionsBasedModel.make(tmpMPS);

        TestUtils.assertTrue(tmpModel.validate());

        final BigDecimal tmpExpVal = new BigDecimal("-.46475314286e+3");
        final double tmpActVal = tmpModel.minimise().getValue();
        TestUtils.assertEquals(tmpExpVal.doubleValue(), tmpActVal, PRECISION);

        if (!tmpModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }
    }

    /**
     * A simple test file primarily useful for checking the compression and decompression programs. Don't think this
     * model was designed to be solved; so I don't try to.
     */
    public void testMPSempstest() {

        final File tmpFile = new File(PATH + "empstest.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpModel = ExpressionsBasedModel.make(tmpMPS);

        this.assertMinMaxVal(tmpModel, null, null);
    }

    /**
     * An example taken from Maros, which defines a problem of 3 rows and 4 columns. There's also a model named maros at
     * netlib, but that's a different much larger model. ERROR: Något tar lång tid, och sedan blir det ArithmaticError.
     * 2010-04-19 lp_solve => 128.33333333
     */
    public void testMPSmaros() {

        final File tmpFile = new File(PATH + "maros.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpModel = ExpressionsBasedModel.make(tmpMPS);

        this.assertMinMaxVal(tmpModel, BigFunction.DIVIDE.invoke(new BigDecimal("385"), THREE), new BigDecimal("197.5"));
    }

    /**
     * An example taken from Nazareth, which defines a problem of 3 rows and 3 column. OK! 2010-04-19 lp_solve => This
     * problem is unbounded (maximisation seems OK.)
     */
    public void testMPSnazareth() {

        final File tmpFile = new File(PATH + "nazareth.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpModel = ExpressionsBasedModel.make(tmpMPS);

        this.assertMinMaxVal(tmpModel, null, BigFunction.DIVIDE.invoke(HUNDRED.add(TEN), THREE));
    }

    /**
     * A simple problem with 4 rows and 3 variables. I got my version from here (same numbers but different names): <a
     * href="http://en.wikipedia.org/wiki/MPS_(format)">testprob@wikipedia</a> and/or <a
     * href="http://lpsolve.sourceforge.net/5.5/mps-format.htm">testprob@lpsolve</a>
     */
    public void testMPStestprob() {

        final Variable[] tmpVariables = new Variable[] { new Variable("YTWO-").weight(FOUR.negate()).lower(ZERO).upper(ONE),
                new Variable("XONE").weight(ONE).lower(ZERO).upper(FOUR), new Variable("YTWO+").weight(FOUR).lower(ZERO).upper(ONE),
                new Variable("ZTHREE").weight(NINE).lower(ZERO).upper(null) };
        final ExpressionsBasedModel tmpExpModel = new ExpressionsBasedModel(tmpVariables);
        tmpExpModel.setMinimisation();
        final int tmpLength = tmpExpModel.countVariables();

        final Expression retVal = tmpExpModel.addExpression("LIM1");

        for (int i1 = 0; i1 < tmpLength; i1++) {
            retVal.setLinearFactor(i1, new BigDecimal[] { ONE.negate(), ONE, ONE, ZERO }[i1]);
        }
        final Expression tmpAddWeightExpression = retVal;
        tmpAddWeightExpression.upper(FIVE);
        final int tmpLength1 = tmpExpModel.countVariables();

        final Expression retVal1 = tmpExpModel.addExpression("LIM2");

        for (int i1 = 0; i1 < tmpLength1; i1++) {
            retVal1.setLinearFactor(i1, new BigDecimal[] { ZERO, ONE, ZERO, ONE }[i1]);
        }
        final Expression tmpAddWeightExpression2 = retVal1;
        tmpAddWeightExpression2.lower(TEN);
        final int tmpLength2 = tmpExpModel.countVariables();

        final Expression retVal2 = tmpExpModel.addExpression("MYEQN");

        for (int i1 = 0; i1 < tmpLength2; i1++) {
            retVal2.setLinearFactor(i1, new BigDecimal[] { ONE, ZERO, ONE.negate(), ONE }[i1]);
        }
        final Expression tmpAddWeightExpression3 = retVal2;
        tmpAddWeightExpression3.level(SEVEN);

        TestUtils.assertTrue(tmpExpModel.validate());

        final File tmpFile = new File(PATH + "testprob.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.makeFromFile(tmpFile);
        final ExpressionsBasedModel tmpActModel = ExpressionsBasedModel.make(tmpMPS);

        TestUtils.assertTrue(tmpActModel.validate());

        final double tmpExpVal = tmpExpModel.minimise().getValue();
        final double tmpActVal = tmpActModel.minimise().getValue();
        TestUtils.assertEquals(tmpExpVal, tmpActVal, PRECISION);

        final Access1D<BigDecimal> tmpExpSolution = tmpExpModel.getVariableValues();
        final Access1D<BigDecimal> tmpActSolution = tmpActModel.getVariableValues();

        TestUtils.assertEquals(tmpVariables.length, tmpExpSolution.count());
        TestUtils.assertEquals(tmpVariables.length, tmpActSolution.count());

        for (int i = 0; i < tmpVariables.length; i++) {
            TestUtils.assertEquals(tmpVariables[i].getName(), tmpExpSolution.doubleValue(i), tmpActSolution.doubleValue(i), PRECISION);
        }

        if (!tmpExpModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }

        if (!tmpActModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }

        this.assertMinMaxVal(tmpActModel, new BigDecimal("54"), new BigDecimal("80"));
    }

    private void assertMinMaxVal(final ExpressionsBasedModel aModel, final BigDecimal aExpMinVal, final BigDecimal aExpMaxVal) {

        TestUtils.assertTrue(aModel.validate());

        if (aExpMinVal != null) {

            TestUtils.assertEquals(aExpMinVal.doubleValue(), aModel.minimise().getValue(), PRECISION);

            if (!aModel.validate(PRECISION)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }

        if (aExpMaxVal != null) {

            TestUtils.assertEquals(aExpMaxVal.doubleValue(), aModel.maximise().getValue(), PRECISION);

            if (!aModel.validate(PRECISION)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }
    }
}
