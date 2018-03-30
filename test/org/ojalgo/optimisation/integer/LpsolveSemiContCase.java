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

import java.io.File;
import java.math.BigDecimal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.type.context.NumberContext;

/**
 * A collection of datasets found here: http://lpsolve.sourceforge.net/5.5/semi-cont.htm Semicontinous
 * variables not supported at the moment
 *
 * @author apete
 */
public class LpsolveSemiContCase extends OptimisationIntegerTests {

    private static final String COMPOSITION_NOT_VALID = " Composition not valid!";
    private static final String PATH = "./src/org/ojalgo/optimisation/linear/mps/";
    private static final NumberContext PRECISION = new NumberContext(7, 6);
    private static final String SOLUTION_NOT_VALID = "Solution not valid!";

    @Test
    public void testDummy() {

    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testSemiContNot() {

        final File tmpFile = new File(PATH + "lpsolve_sc_not.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        tmpModel.minimise();

        TestUtils.assertTrue(tmpModel.validate());

        final BigDecimal tmpExpVal = new BigDecimal("3.93333");
        final double tmpActVal = tmpModel.minimise().getValue();

        if (!tmpModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }

        TestUtils.assertEquals(tmpExpVal.doubleValue(), tmpActVal, PRECISION);
    }

    @Test
    @Disabled("Underscored before JUnit 5")
    public void testSemiContOrg() {

        final File tmpFile = new File(PATH + "lpsolve_sc_org.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        tmpModel.minimise();

        TestUtils.assertTrue(tmpModel.validate());

        final BigDecimal tmpExpVal = new BigDecimal("6.83333");
        final double tmpActVal = tmpModel.maximise().getValue();

        if (!tmpModel.validate(PRECISION)) {
            TestUtils.fail(SOLUTION_NOT_VALID);
        }

        TestUtils.assertEquals(tmpExpVal.doubleValue(), tmpActVal, PRECISION);
    }
}
