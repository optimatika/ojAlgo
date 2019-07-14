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
import org.ojalgo.matrix.store.PrimitiveDenseStore;
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

        PhysicalStore<Double> quadratic = PrimitiveDenseStore.FACTORY.make(arity, arity);
        PhysicalStore<Double> linear = PrimitiveDenseStore.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));

        myLinearFunction1 = LinearFunction.makePrimitive(linear);
        myAffineFunction1 = AffineFunction.makePrimitive(linear);
        myQuadraticFunction1 = QuadraticFunction.makePrimitive(quadratic, linear);

        myLinearFunction2 = LinearFunction.makePrimitive(linear.transpose());
        myAffineFunction2 = AffineFunction.makePrimitive(linear.transpose());
        myQuadraticFunction2 = QuadraticFunction.makePrimitive(quadratic, linear.transpose());

        myArg = PrimitiveDenseStore.FACTORY.makeFilled(arity, 1, new Uniform(-1, 2));
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

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(), myLinearFunction2.getLinearFactors());
        TestUtils.assertEquals(myAffineFunction1.getLinearFactors(), myAffineFunction2.getLinearFactors());
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(), myQuadraticFunction2.getLinearFactors());

        TestUtils.assertEquals(myLinearFunction1.getLinearFactors(), myAffineFunction1.getLinearFactors());
        TestUtils.assertEquals(myAffineFunction2.getLinearFactors(), myQuadraticFunction2.getLinearFactors());
        TestUtils.assertEquals(myQuadraticFunction1.getLinearFactors(), myLinearFunction2.getLinearFactors());
    }

}
