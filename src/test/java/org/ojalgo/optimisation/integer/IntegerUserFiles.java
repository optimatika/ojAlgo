/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation.integer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.type.context.NumberContext;

/**
 * User supplied MIP models. May or may not have had specific problems. Tests mostly just verify that the
 * problem/model can still be solved.
 */
public class IntegerUserFiles extends OptimisationIntegerTests implements ModelFileTest {

    private static final NumberContext ACCURACY = NumberContext.of(8);

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString) {
        return IntegerUserFiles.doTest(modelName, expMinValString, expMaxValString, ACCURACY);
    }

    private static ExpressionsBasedModel doTest(final String modelName, final String expMinValString, final String expMaxValString,
            final NumberContext accuracy) {

        ExpressionsBasedModel model = ModelFileTest.makeModel("usersupplied", modelName, false);

        // model.options.debug(IntegerSolver.class);
        // model.options.progress(IntegerSolver.class);
        // model.options.validate = false;
        // model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(NumberContext.of(4)));

        ModelFileTest.assertValues(model, expMinValString, expMaxValString, accuracy);

        return model;
    }

    /**
     * <ul>
     * <li>v51.0.0 took about 30min to solve, first integer soltion after 30s
     * <li>v51.1.0 (WIP) ≈6min
     * </ul>
     */
    @Test
    @Tag("slow")
    public void testBigBinary() {
        IntegerUserFiles.doTest("BigBinary.ebm", null, "139.4070725458");
    }

    /**
     * <ul>
     * <li>v51.1.0 (WIP) ≈1min (times halved with every digit less required – gap)
     * </ul>
     */
    @Test
    @Tag("slow")
    public void testEnergyApp() {
        IntegerUserFiles.doTest("EnergyApp.ebm", "2316538.192374359", null);
    }

}
