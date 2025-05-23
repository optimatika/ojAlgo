/*
 * Copyright 1997-2025 Optimatika
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

import static org.ojalgo.function.constant.BigMath.ONE;
import static org.ojalgo.function.constant.BigMath.ZERO;

import java.math.BigDecimal;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

public class TestOjAlgo {

    public static void main(final String[] args) {
        TestOjAlgo.testBug1();
    }

    public static void testBug1() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable[] objective = new Variable[] { model.newVariable("X").weight(ONE), model.newVariable("Y").weight(ZERO), model.newVariable("Z").weight(ZERO) };

        objective[1].setInteger(true);

        // c1: X =0
        Expression c1 = model.newExpression("c1");
        c1.level(ZERO);
        c1.set(0, ONE);

        // c2: -X +5Y =0
        Expression c2 = model.newExpression("c2");
        c2.level(ZERO);

        c2.set(0, new BigDecimal(-1));
        c2.set(1, ONE);

        // c3: X -Z =0
        Expression c3 = model.newExpression("c3");
        c3.level(ZERO);
        // bugs with this constraint
        c3.set(0, ONE);
        c3.set(2, new BigDecimal(-1));
        // but not with this one ???
        //c3.setLinearFactor(0, new BigDecimal(-1));
        //c3.setLinearFactor(2, ONE);

        Optimisation.Result tmpResult = model.minimise();

        BasicLogger.debug(tmpResult.toString());
    }

}
