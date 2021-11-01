/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.optimisation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;

import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.convex.ConvexSolver;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * Base class for MPS-file based tests.
 *
 * @author apete
 */
public interface ModelFileMPS {

    String OPTIMISATION_RSRC = "/optimisation/";
    String SOLUTION_NOT_VALID = "Solution not valid!";

    static void assertValues(final ExpressionsBasedModel model, final BigDecimal expMinVal, final BigDecimal expMaxVal, final Map<String, BigDecimal> solution,
            final NumberContext precision) {

        //        model.options.debug(LinearSolver.class);
        //        model.options.mip_defer = 0.0;

        TestUtils.assertTrue(model.validate());

        if (expMinVal != null) {

            Result result = model.minimise();

            TestUtils.assertStateNotLessThanOptimal(result);

            TestUtils.assertTrue(ModelFileMPS.SOLUTION_NOT_VALID, model.validate(result, precision, BasicLogger.DEBUG));

            TestUtils.assertEquals(expMinVal, result.getValue(), precision);
        }

        if (expMaxVal != null) {

            Result result = model.maximise();

            TestUtils.assertStateNotLessThanOptimal(result);

            TestUtils.assertTrue(ModelFileMPS.SOLUTION_NOT_VALID, model.validate(result, precision, BasicLogger.DEBUG));

            TestUtils.assertEquals(expMaxVal, result.getValue(), precision);
        }

        if (solution != null) {
            for (Variable tmpVariable : model.getVariables()) {
                BigDecimal tmpValue = solution.get(tmpVariable.getName());
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
    }

    static ExpressionsBasedModel makeAndAssert(final String dataset, final String modelName, final String expMinValString, final String expMaxValString,
            final boolean relax, final NumberContext precision, final Map<String, BigDecimal> solution) {

        ExpressionsBasedModel model = ModelFileMPS.makeModel(dataset, modelName, relax);

        BigDecimal expMinVal = expMinValString != null ? new BigDecimal(expMinValString) : null;
        BigDecimal expMaxVal = expMaxValString != null ? new BigDecimal(expMaxValString) : null;

        ModelFileMPS.assertValues(model, expMinVal, expMaxVal, solution, precision);

        return model;
    }

    static ExpressionsBasedModel makeModel(final String dataset, final String name, final boolean relax) {

        try (InputStream input = ExpressionsBasedModel.class.getResourceAsStream(OPTIMISATION_RSRC + dataset + "/" + name)) {

            ExpressionsBasedModel model = ExpressionsBasedModel.parse(input, FileFormat.MPS);

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
            // model.options.progress(IntegerSolver.class);
            model.options.debug(ConvexSolver.class);
            // model.options.validate = false;

            TestUtils.assertTrue(model.validate());

            return model;

        } catch (IOException cause) {
            TestUtils.fail(cause);
            return null;
        }
    }

}
