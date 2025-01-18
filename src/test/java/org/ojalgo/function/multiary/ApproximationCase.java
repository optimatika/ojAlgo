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
package org.ojalgo.function.multiary;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class ApproximationCase extends MultiaryFunctionTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 14);

    @Test
    public void testFirstOrderApproximation() {

        for (int arity = 1; arity < 10; arity++) {

            PhysicalStore<Double> linear = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));

            PhysicalStore<Double> zero = R064Store.FACTORY.make(arity, 1);
            PhysicalStore<Double> point = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-10, 20));

            AffineFunction<Double> orgFunc = AffineFunction.factory(R064Store.FACTORY).coefficients(linear).make(linear.size());

            PhysicalStore<Double> arg = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-10, 20));

            Double expected = orgFunc.invoke(arg);

            MultiaryFunction<Double> maclaurinApprox = orgFunc.toFirstOrderApproximation(zero);
            MultiaryFunction<Double> taylorApprox = orgFunc.toFirstOrderApproximation(point);

            Double orgVal = orgFunc.invoke(arg);
            Double maclaurinVal = maclaurinApprox.invoke(arg);
            Double taylorVal = taylorApprox.invoke(arg);

            TestUtils.assertEquals(expected, orgVal, ACCURACY);
            TestUtils.assertEquals(expected, maclaurinVal, ACCURACY);
            TestUtils.assertEquals(expected, taylorVal, ACCURACY);
        }
    }

    @Test
    public void testSecondOrderApproximation() {

        for (int arity = 1; arity < 10; arity++) {

            PhysicalStore<Double> quadratic = R064Store.FACTORY.makeFilled(arity, arity, new Uniform());
            PhysicalStore<Double> linear = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));

            PhysicalStore<Double> zero = R064Store.FACTORY.make(arity, 1);
            PhysicalStore<Double> point = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-10, 20));

            QuadraticFunction<Double> orgFunc = QuadraticFunction.factory(R064Store.FACTORY).quadratic(quadratic).linear(linear).make(linear.size());

            PhysicalStore<Double> arg = R064Store.FACTORY.makeFilled(arity, 1, new Uniform(-10, 20));

            Double expected = orgFunc.invoke(arg);

            MultiaryFunction<Double> maclaurinApprox = orgFunc.toSecondOrderApproximation(zero);
            MultiaryFunction<Double> taylorApprox = orgFunc.toSecondOrderApproximation(point);

            Double orgVal = orgFunc.invoke(arg);
            Double maclaurinVal = maclaurinApprox.invoke(arg);
            Double taylorVal = taylorApprox.invoke(arg);

            TestUtils.assertEquals(expected, orgVal, ACCURACY);
            TestUtils.assertEquals(expected, maclaurinVal, ACCURACY);
            TestUtils.assertEquals(expected, taylorVal, ACCURACY);
        }
    }

}
