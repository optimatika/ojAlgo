/*
 * Copyright 1997-2019 Optimatika
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

public class QuadraticCase extends MultiaryFunctionTests {

    private MatrixStore<Double> myArg1;
    private MatrixStore<Double> myArg2;

    private PureQuadraticFunction<Double> myPureQuadraticFunction;
    private QuadraticFunction<Double> myQuadraticFunction;

    @BeforeEach
    public void setupL() {

        int arity = 9;

        PhysicalStore<Double> quadratic = Primitive64Store.FACTORY.makeFilled(arity, arity, new Uniform());
        PhysicalStore<Double> linear = Primitive64Store.FACTORY.make(arity, 1);

        myPureQuadraticFunction = PureQuadraticFunction.makePrimitive(quadratic);
        myQuadraticFunction = QuadraticFunction.makePrimitive(quadratic, linear);

        myArg1 = Primitive64Store.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));
        myArg2 = myArg1.transpose();
    }

    @Test
    public void testGetGradient() {

        TestUtils.assertEquals(myPureQuadraticFunction.getGradient(myArg1), myPureQuadraticFunction.getGradient(myArg2));
        TestUtils.assertEquals(myQuadraticFunction.getGradient(myArg1), myQuadraticFunction.getGradient(myArg2));

        TestUtils.assertEquals(myPureQuadraticFunction.getGradient(myArg1), myQuadraticFunction.getGradient(myArg1));
        TestUtils.assertEquals(myPureQuadraticFunction.getGradient(myArg2), myQuadraticFunction.getGradient(myArg2));
    }

    @Test
    public void testGetHessian() {

        TestUtils.assertEquals(myPureQuadraticFunction.getHessian(myArg1), myPureQuadraticFunction.getHessian(myArg2));
        TestUtils.assertEquals(myQuadraticFunction.getHessian(myArg1), myQuadraticFunction.getHessian(myArg2));

        TestUtils.assertEquals(myPureQuadraticFunction.getHessian(myArg1), myQuadraticFunction.getHessian(myArg1));
        TestUtils.assertEquals(myPureQuadraticFunction.getHessian(myArg2), myQuadraticFunction.getHessian(myArg2));
    }

    @Test
    public void testGetLinearFactors() {

        TestUtils.assertEquals(myPureQuadraticFunction.getLinearFactors(), myQuadraticFunction.getLinearFactors());
    }

    @Test
    public void testInvoke() {

        TestUtils.assertEquals(myPureQuadraticFunction.invoke(myArg1), myPureQuadraticFunction.invoke(myArg2));
        TestUtils.assertEquals(myQuadraticFunction.invoke(myArg1), myQuadraticFunction.invoke(myArg2));

        TestUtils.assertEquals(myPureQuadraticFunction.invoke(myArg1), myQuadraticFunction.invoke(myArg1));
        TestUtils.assertEquals(myPureQuadraticFunction.invoke(myArg2), myQuadraticFunction.invoke(myArg2));
    }

}
