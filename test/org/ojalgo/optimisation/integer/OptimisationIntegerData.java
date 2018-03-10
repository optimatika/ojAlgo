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

import static org.ojalgo.constant.BigMath.*;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;

public abstract class OptimisationIntegerData {

    public static ExpressionsBasedModel buildModelForP20100412() {

        final KnapsackItem[] tmpItems = { new KnapsackItem(20, 2), new KnapsackItem(30, 4) };

        final Variable[] tmpVariables = new Variable[tmpItems.length];
        for (int i = 0; i < tmpVariables.length; i++) {
            tmpVariables[i] = new Variable("Var" + String.valueOf(i));
            tmpVariables[i].lower(ZERO).upper(ONE).weight(tmpItems[i].value).integer(true);
        }

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel(tmpVariables);
        final Expression tmpTotalWeightExpr = retVal.addExpression("Total Weight");
        for (int i = 0; i < tmpItems.length; i++) {
            tmpTotalWeightExpr.set(i, tmpItems[i].weight);
        }
        tmpTotalWeightExpr.lower(ZERO).upper(THREE);

        retVal.setMaximisation();

        return retVal;
    }

    public OptimisationIntegerData() {
        super();
    }

}
