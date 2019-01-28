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
import java.util.Map;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * Base class for MPS-file based tests.
 *
 * @author apete
 */
abstract class AbstractCaseFileMPS extends OptimisationIntegerTests {

    protected static final String PATH = "./test/org/ojalgo/optimisation/integer/";
    protected static final String SOLUTION_NOT_VALID = "Solution not valid!";

    protected static void assertMinMaxVal(final String modelName, final BigDecimal expMinVal, final BigDecimal expMaxVal, final boolean relax,
            final Map<String, BigDecimal> solution) {

        if (DEBUG) {
            BasicLogger.DEBUG.println();
            BasicLogger.DEBUG.println();
            BasicLogger.DEBUG.println(modelName);
            BasicLogger.DEBUG.println();
        }

        final File file = new File(PATH + modelName);
        final ExpressionsBasedModel model = MathProgSysModel.make(file).getExpressionsBasedModel();

        if (DEBUG) {
            BasicLogger.DEBUG.println();
            BasicLogger.DEBUG.println(model);
            BasicLogger.DEBUG.println();
        }

        if (relax) {
            model.relax(true);
        }

        if (solution != null) {
            for (final Variable tmpVariable : model.getVariables()) {
                final BigDecimal tmpValue = solution.get(tmpVariable.getName());
                if (tmpValue != null) {
                    tmpVariable.setValue(tmpValue);
                } else {
                    tmpVariable.setValue(BigMath.ZERO);
                }
            }
            if (!model.validate(new NumberContext(7, 4))) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }

        model.options.time_suffice = 5L * CalendarDateUnit.MINUTE.toDurationInMillis();
        model.options.time_abort = 15L * CalendarDateUnit.MINUTE.toDurationInMillis();

        model.options.debug(GenericSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = true;

        TestUtils.assertTrue(model.validate());

        if (expMinVal != null) {

            final double tmpMinimum = model.minimise().getValue();

            if (!model.validate(new NumberContext(7, 6))) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }

            final double tmpExpected = expMinVal.doubleValue();
            final double tmpError = expMinVal.ulp().doubleValue();
            TestUtils.assertEquals(tmpExpected, tmpMinimum, tmpError);
        }

        if (expMaxVal != null) {

            final double tmpMaximum = model.maximise().getValue();

            if (!model.validate(new NumberContext(7, 6))) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }

            final double tmpExpected = expMaxVal.doubleValue();
            final double tmpError = expMaxVal.ulp().doubleValue();
            TestUtils.assertEquals(tmpExpected, tmpMaximum, tmpError);
        }
    }

}
