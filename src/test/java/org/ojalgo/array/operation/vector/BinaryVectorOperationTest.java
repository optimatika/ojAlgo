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
package org.ojalgo.array.operation.vector;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;

class BinaryVectorOperationTest {

    static double[] copy(final double[] original) {

        double[] copy = new double[original.length];

        System.arraycopy(original, 0, copy, 0, original.length);

        return copy;
    }

    static double[] newDouble(final int length) {
        return new double[length];
    }

    static double[] newDouble(final int length, final double constant) {
        return BinaryVectorOperationTest.newDouble(length, PrimitiveFunction.nullary(constant));
    }

    static double[] newDouble(final int length, final PrimitiveFunction.Nullary supplier) {
        return ((ArrayR064) ArrayR064.FACTORY.makeFilled(length, supplier)).data;
    }

    @Test
    public void testSubtractSV() {

        ArrayR064 result = (ArrayR064) ArrayR064.FACTORY.makeFilled(789, PrimitiveFunction.nullary(2.18));
        ArrayR064 right = (ArrayR064) ArrayR064.FACTORY.copy(result);

        BinaryVectorOperation.invoke(result.data, 2.18, PrimitiveMath.SUBTRACT, right.data);

        double largest = result.aggregateAll(Aggregator.LARGEST).doubleValue();

        TestUtils.assertEquals(0.0, largest);
    }

    @Test
    public void testSubtractSVi() {

        double left = -0.5;
        double[] right1 = BinaryVectorOperationTest.newDouble(567, Uniform.standard());
        double[] right2 = BinaryVectorOperationTest.copy(right1);

        for (int i = 5; i < 500; i += 3) {
            right1[i] = PrimitiveMath.ADD.invoke(left, right1[i]);
        }

        BinaryVectorOperation.invoke(right2, left, PrimitiveMath.ADD, 5, 500, 3);

        TestUtils.assertEquals(right1, right2);
    }

    @Test
    public void testSubtractVS() {

        ArrayR064 result = (ArrayR064) ArrayR064.FACTORY.makeFilled(789, PrimitiveFunction.nullary(3.14));
        ArrayR064 left = (ArrayR064) ArrayR064.FACTORY.copy(result);

        BinaryVectorOperation.invoke(result.data, left.data, PrimitiveMath.SUBTRACT, 3.14);

        double largest = result.aggregateAll(Aggregator.LARGEST).doubleValue();

        TestUtils.assertEquals(0.0, largest);
    }

    @Test
    public void testSubtractVSi() {

        double[] left1 = BinaryVectorOperationTest.newDouble(789, Uniform.standard());
        double[] left2 = BinaryVectorOperationTest.copy(left1);
        double right = 0.5;

        for (int i = 7; i < 700; i += 3) {
            left1[i] = PrimitiveMath.SUBTRACT.invoke(left1[i], right);
        }

        BinaryVectorOperation.invoke(left2, PrimitiveMath.SUBTRACT, right, 7, 700, 3);

        TestUtils.assertEquals(left1, left2);
    }

    @Test
    public void testSubtractVV() {

        ArrayR064 result = (ArrayR064) ArrayR064.FACTORY.makeFilled(789, Normal.standard());
        ArrayR064 left = (ArrayR064) ArrayR064.FACTORY.copy(result);
        ArrayR064 right = (ArrayR064) ArrayR064.FACTORY.copy(result);

        TestUtils.assertEquals(left, right);

        BinaryVectorOperation.invoke(result.data, left.data, PrimitiveMath.SUBTRACT, right.data);

        double largest = result.aggregateAll(Aggregator.LARGEST).doubleValue();

        TestUtils.assertEquals(0.0, largest);
    }

}
