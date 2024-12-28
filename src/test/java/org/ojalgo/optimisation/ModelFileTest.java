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
package org.ojalgo.optimisation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * Base class for file based optimisation tests.
 *
 * @author apete
 */
public interface ModelFileTest {

    /**
     * expMinValString or expMaxValString can be null, empty string or a number-string.
     * <ul>
     * <li>null means that direction is not tested.
     * <li>A number-string is the expected optimal solution value.
     * <li>An empty string means unbounded in that direction.
     * <li>If both are empty string the problem is infeasible (rather than unbounded in both directions).
     * </ul>
     */
    static void assertValues(final ExpressionsBasedModel model, final String expMinValString, final String expMaxValString, final NumberContext accuracy) {

        BigDecimal expMinVal = expMinValString != null && !expMinValString.isEmpty() ? new BigDecimal(expMinValString) : null;
        BigDecimal expMaxVal = expMaxValString != null && !expMaxValString.isEmpty() ? new BigDecimal(expMaxValString) : null;

        TestUtils.assertTrue(model.validate());

        Result result = null;

        if (expMinValString != null) {

            result = model.minimise();

            if (expMinValString.isEmpty()) {

                if (expMaxValString != null && expMaxValString.isEmpty()) {
                    // Infeasible

                    TestUtils.assertStateLessThanFeasible(result);

                } else {
                    // Unbounded

                    TestUtils.assertStateUnboundedOrLessThanFeasible(result);
                }

            } else {

                TestUtils.assertStateNotLessThanOptimal(result);

                TestUtils.assertEquals(expMinVal, result.getValue(), accuracy);

                TestUtils.assertTrue("Minimisation solution not valid!", model.validate(result, accuracy, BasicLogger.DEBUG));
            }
        }

        if (expMaxValString != null) {

            result = model.maximise();

            if (expMaxValString.isEmpty()) {

                if (expMinValString != null && expMinValString.isEmpty()) {
                    // Infeasible

                    TestUtils.assertStateLessThanFeasible(result);

                } else {
                    // Unbounded

                    TestUtils.assertStateUnboundedOrLessThanFeasible(result);
                }

            } else {

                TestUtils.assertStateNotLessThanOptimal(result);

                TestUtils.assertEquals(expMaxVal, result.getValue(), accuracy);

                TestUtils.assertTrue("Maximisation solution not valid!", model.validate(result, accuracy, BasicLogger.DEBUG));
            }
        }
    }

    static ExpressionsBasedModel makeModel(final String dataset, final String name, final boolean relax) {

        ExpressionsBasedModel.FileFormat format = ExpressionsBasedModel.FileFormat.from(name);

        try (InputStream input = ExpressionsBasedModel.class.getResourceAsStream("/optimisation/" + dataset + "/" + name)) {

            ExpressionsBasedModel model = ExpressionsBasedModel.parse(input, format);

            if (relax) {
                model.relax(true);
                for (Variable tmpVar : model.getVariables()) {
                    tmpVar.relax();
                }
            }

            model.options.time_suffice = 5L * CalendarDateUnit.MINUTE.toDurationInMillis();
            model.options.time_abort = 15L * CalendarDateUnit.MINUTE.toDurationInMillis();

            TestUtils.assertTrue(model.validate());

            return model;

        } catch (IOException cause) {
            TestUtils.fail(cause);
            return null;
        }
    }

}
