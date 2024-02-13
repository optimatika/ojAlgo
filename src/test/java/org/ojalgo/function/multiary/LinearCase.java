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
package org.ojalgo.function.multiary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

public class LinearCase extends MultiaryFunctionTests {

    private LinearFunction<Double> myLinearFunction1;
    private AffineFunction<Double> myAffineFunction1;
    private QuadraticFunction<Double> myQuadraticFunction1;

    private LinearFunction<Double> myLinearFunction2;
    private AffineFunction<Double> myAffineFunction2;
    private QuadraticFunction<Double> myQuadraticFunction2;

    private MatrixStore<Double> myArg;

    @BeforeEach
    public void setup() {

        int arity = 9;

        PhysicalStore<Double> quadratic = Primitive64Store.FACTORY.make(arity, arity);
        PhysicalStore<Double> linear = Primitive64Store.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));

        myLinearFunction1 = LinearFunction.factory(Primitive64Store.FACTORY).coefficients(linear).make(linear.size());
        myAffineFunction1 = AffineFunction.factory(Primitive64Store.FACTORY).coefficients(linear).make(linear.size());
        myQuadraticFunction1 = QuadraticFunction.factory(Primitive64Store.FACTORY).quadratic(quadratic).linear(linear).make(linear.size());

        myLinearFunction2 = LinearFunction.factory(Primitive64Store.FACTORY).coefficients(linear.transpose()).make(linear.transpose().size());
        myAffineFunction2 = AffineFunction.factory(Primitive64Store.FACTORY).coefficients(linear.transpose()).make(linear.transpose().size());
        myQuadraticFunction2 = QuadraticFunction.factory(Primitive64Store.FACTORY).quadratic(quadratic).linear(linear.transpose()).make(linear.transpose().size());

        myArg = Primitive64Store.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));
    }

    @Test
    public void testGetGradient() {

        TestUtils.assertEquals(myLinearFunction1.getGradient(myArg), myLinearFunction2.getGradient(myArg));
        TestUtils.assertEquals(myAffineFunction1.getGradient(myArg), myAffineFunction2.getGradient(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.getGradient(myArg), myQuadraticFunction2.getGradient(myArg));

        TestUtils.assertEquals(myLinearFunction1.getGradient(myArg), myAffineFunction1.getGradient(myArg));
        TestUtils.assertEquals(myAffineFunction2.getGradient(myArg), myQuadraticFunction2.getGradient(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.getGradient(myArg), myLinearFunction2.getGradient(myArg));
    }

    @Test
    public void testGetHessian() {

        TestUtils.assertEquals(myLinearFunction1.getHessian(myArg), myLinearFunction2.getHessian(myArg));
        TestUtils.assertEquals(myAffineFunction1.getHessian(myArg), myAffineFunction2.getHessian(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.getHessian(myArg), myQuadraticFunction2.getHessian(myArg));

        TestUtils.assertEquals(myLinearFunction1.getHessian(myArg), myAffineFunction1.getHessian(myArg));
        TestUtils.assertEquals(myAffineFunction2.getHessian(myArg), myQuadraticFunction2.getHessian(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.getHessian(myArg), myLinearFunction2.getHessian(myArg));
    }

    @Test
    public void testGetLinearFactors() {

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(false), myLinearFunction2.getLinearFactors(false));
        TestUtils.assertEquals(myAffineFunction1.getLinearFactors(false), myAffineFunction2.getLinearFactors(false));
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(false), myQuadraticFunction2.getLinearFactors(false));

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(false), myAffineFunction1.getLinearFactors(false));
        TestUtils.assertEquals(myAffineFunction2.getLinearFactors(false), myQuadraticFunction2.getLinearFactors(false));
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(false), myLinearFunction2.getLinearFactors(false));

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(true), myLinearFunction2.getLinearFactors(true));
        TestUtils.assertEquals(myAffineFunction1.getLinearFactors(true), myAffineFunction2.getLinearFactors(true));
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(true), myQuadraticFunction2.getLinearFactors(true));

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(true), myAffineFunction1.getLinearFactors(true));
        TestUtils.assertEquals(myAffineFunction2.getLinearFactors(true), myQuadraticFunction2.getLinearFactors(true));
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(true), myLinearFunction2.getLinearFactors(true));
    }

    @Test
    public void testInvoke() {

        TestUtils.assertEquals(myLinearFunction1.invoke(myArg), myLinearFunction2.invoke(myArg));
        TestUtils.assertEquals(myAffineFunction1.invoke(myArg), myAffineFunction2.invoke(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.invoke(myArg), myQuadraticFunction2.invoke(myArg));

        TestUtils.assertEquals(myLinearFunction1.invoke(myArg), myAffineFunction1.invoke(myArg));
        TestUtils.assertEquals(myAffineFunction2.invoke(myArg), myQuadraticFunction2.invoke(myArg));
        TestUtils.assertEquals(myQuadraticFunction1.invoke(myArg), myLinearFunction2.invoke(myArg));
    }

}
