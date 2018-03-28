/*
 * Copyright 1997-2018 Optimatika
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

import java.io.File;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.BigMath;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.MathProgSysModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Optimisation.State;

public final class SpecificBranchCase extends MipLibCase {

    @Test
    public void testVpm2FirstBranch() {

        final File tmpFile = new File(MipLibCase.PATH + "vpm2.mps");
        final MathProgSysModel tmpMPS = MathProgSysModel.make(tmpFile);
        final ExpressionsBasedModel tmpModel = tmpMPS.getExpressionsBasedModel();

        TestUtils.assertTrue(tmpModel.validate());

        final ExpressionsBasedModel tmpLowerBranchModel = tmpModel.relax(false);
        final ExpressionsBasedModel tmpUpperBranchModel = tmpModel.relax(false);

        tmpLowerBranchModel.getVariable(106).upper(BigMath.ZERO);
        tmpUpperBranchModel.getVariable(106).lower(BigMath.ONE);

        final Optimisation.Result tmpLowerResult = tmpLowerBranchModel.minimise();
        final Optimisation.Result tmpUpperResult = tmpUpperBranchModel.minimise();

        final State tmpLowerState = tmpLowerResult.getState();
        final State tmpUpperState = tmpUpperResult.getState();

        if (!tmpLowerState.isFeasible() && !tmpUpperState.isFeasible()) {
            TestUtils.fail("Both these branches cannot be infeasible!");
        }

        tmpLowerBranchModel.minimise();
        if (tmpLowerState.isFeasible() && !tmpLowerBranchModel.validate(MipLibCase.PRECISION)) {
            TestUtils.fail(MipLibCase.SOLUTION_NOT_VALID);
        }

        tmpUpperBranchModel.minimise();
        if (tmpUpperState.isFeasible() && !tmpUpperBranchModel.validate(MipLibCase.PRECISION)) {
            TestUtils.fail(MipLibCase.SOLUTION_NOT_VALID);
        }
    }

}
