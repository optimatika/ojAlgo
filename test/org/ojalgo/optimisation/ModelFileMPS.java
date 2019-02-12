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
package org.ojalgo.optimisation;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.optimisation.integer.IntegerSolver;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * Base class for MPS-file based tests.
 *
 * @author apete
 */
public class ModelFileMPS {

    private static final String OPT_RSRC = "./rsrc/optimisation/";

    public static final String INT_PATH = OPT_RSRC + "miplib/";

    public static final String LIN_PATH = OPT_RSRC + "netlib/old/";

    public static String SOLUTION_NOT_VALID = "Solution not valid!";

    static final NumberContext PRECISION = NumberContext.getGeneral(8, 6);

    public static void assertMinMaxVal(final ExpressionsBasedModel model, final BigDecimal expMinVal, final BigDecimal expMaxVal) {

        TestUtils.assertTrue(model.validate());

        if (expMinVal != null) {

            TestUtils.assertEquals(expMinVal.doubleValue(), model.minimise().getValue(), PRECISION);

            if (!model.validate(PRECISION)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }

        if (expMaxVal != null) {

            TestUtils.assertEquals(expMaxVal.doubleValue(), model.maximise().getValue(), PRECISION);

            if (!model.validate(PRECISION)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }
    }

    public static ExpressionsBasedModel assertMinMaxVal(String dataset, final String modelName, final String expMinValString, final String expMaxValString,
            final boolean relax, NumberContext precision, final Map<String, BigDecimal> solution) {

        BigDecimal expMinVal = expMinValString != null ? new BigDecimal(expMinValString) : null;
        BigDecimal expMaxVal = expMaxValString != null ? new BigDecimal(expMaxValString) : null;

        final File file = new File(OPT_RSRC + dataset + "/" + modelName);
        final ExpressionsBasedModel model = MathProgSysModel.make(file).getExpressionsBasedModel();

        TestUtils.assertTrue(model.validate());

        if (relax) {

            model.relax(true);

            for (Variable tmpVar : model.getVariables()) {
                tmpVar.relax();
            }
        }

        model.options.time_suffice = 5L * CalendarDateUnit.MINUTE.toDurationInMillis();
        model.options.time_abort = 15L * CalendarDateUnit.MINUTE.toDurationInMillis();

        // model.options.debug(IntegerSolver.class);
        model.options.progress(IntegerSolver.class);
        // model.options.validate = false;

        TestUtils.assertTrue(model.validate());

        if (expMinVal != null) {

            final double minimum = model.minimise().getValue();

            if (!model.validate(precision)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }

            final double expected = expMinVal.doubleValue();
            TestUtils.assertEquals(expected, minimum, precision);
        }

        if (expMaxVal != null) {

            final double maximum = model.maximise().getValue();

            if (!model.validate(precision)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }

            final double expected = expMaxVal.doubleValue();
            TestUtils.assertEquals(expected, maximum, precision);
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
            if (!model.validate(precision)) {
                TestUtils.fail(SOLUTION_NOT_VALID);
            }
        }

        return model;
    }

}
