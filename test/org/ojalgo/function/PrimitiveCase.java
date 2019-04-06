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
package org.ojalgo.function;

import static org.ojalgo.function.PrimitiveFunction.*;
import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Tests {@linkplain org.ojalgo.function.PrimitiveFunction} members. Ok to use infinity and Nan both as input
 * arguments and results.
 *
 * @author apete
 */
public class PrimitiveCase {

    @Test
    public void testACOSH() {
        TestUtils.assertEquals(ZERO, PrimitiveMath.ACOSH.invoke(ONE), MACHINE_EPSILON);
    }

    @Test
    public void testASINH() {
        TestUtils.assertEquals(ZERO, PrimitiveMath.ASINH.invoke(ZERO), MACHINE_EPSILON);
    }

    @Test
    public void testATANH() {
        TestUtils.assertEquals(ZERO, PrimitiveMath.ATANH.invoke(ZERO), MACHINE_EPSILON);
        TestUtils.assertEquals(POSITIVE_INFINITY, PrimitiveMath.ATANH.invoke(ONE), MACHINE_EPSILON);
        TestUtils.assertEquals(NEGATIVE_INFINITY, PrimitiveMath.ATANH.invoke(NEG), MACHINE_EPSILON);
    }

    @Test
    public void testHYPOT() {

        TestUtils.assertEquals(FIVE, PrimitiveMath.HYPOT.invoke(FOUR, THREE), MACHINE_EPSILON);
        TestUtils.assertEquals(NaN, PrimitiveMath.HYPOT.invoke(NaN, NaN), MACHINE_EPSILON);

    }

    @Test
    public void testMinMax() {

        TestUtils.assertEquals(Math.min(2, -78), FunctionUtils.min(2, -78));
        TestUtils.assertEquals(PrimitiveMath.MAX.invoke(2, -78), FunctionUtils.max(2, -78));

        TestUtils.assertEquals(67, FunctionUtils.max(new int[] { 67 }));
        TestUtils.assertEquals(67, FunctionUtils.min(new int[] { 67 }));

        TestUtils.assertEquals(FunctionUtils.max(67, -76), FunctionUtils.max(new int[] { 67, -76 }));
        TestUtils.assertEquals(FunctionUtils.min(67, -76), FunctionUtils.min(new int[] { 67, -76 }));

        TestUtils.assertEquals(FunctionUtils.max(0, 67, -76), FunctionUtils.max(new int[] { 0, 67, -76 }));
        TestUtils.assertEquals(FunctionUtils.min(0, 67, -76), FunctionUtils.min(new int[] { 0, 67, -76 }));

        TestUtils.assertEquals(FunctionUtils.max(0, 67, -76, 80), FunctionUtils.max(new int[] { 0, 67, -76, 80 }));
        TestUtils.assertEquals(FunctionUtils.min(0, 67, -76, -80), FunctionUtils.min(new int[] { 0, 67, -76, -80 }));

        TestUtils.assertEquals(FunctionUtils.max(80, 0, 67, -76), FunctionUtils.max(new int[] { 80, 0, 67, -76 }));
        TestUtils.assertEquals(FunctionUtils.min(-80, 0, 67, -76), FunctionUtils.min(new int[] { -80, 0, 67, -76 }));

        TestUtils.assertEquals(80, FunctionUtils.max(new int[] { 0, 67, -76, 80 }));
        TestUtils.assertEquals(-80, FunctionUtils.min(new int[] { 0, 67, -76, -80 }));

        TestUtils.assertEquals(80, FunctionUtils.max(new int[] { 80, 0, 67, -76 }));
        TestUtils.assertEquals(-80, FunctionUtils.min(new int[] { -80, 0, 67, -76 }));

    }

    @Test
    public void testPOWER() {

        TestUtils.assertEquals(ONE, PrimitiveMath.POWER.invoke(ZERO, 0), MACHINE_EPSILON);
        TestUtils.assertEquals(ONE, PrimitiveMath.POWER.invoke(PI, 0), MACHINE_EPSILON);
        TestUtils.assertEquals(ONE, PrimitiveMath.POWER.invoke(E, 0), MACHINE_EPSILON);

        TestUtils.assertEquals(ZERO, PrimitiveMath.POWER.invoke(ZERO, 1), MACHINE_EPSILON);
        TestUtils.assertEquals(PI, PrimitiveMath.POWER.invoke(PI, 1), MACHINE_EPSILON);
        TestUtils.assertEquals(E, PrimitiveMath.POWER.invoke(E, 1), MACHINE_EPSILON);

        TestUtils.assertEquals(ZERO * ZERO, PrimitiveMath.POWER.invoke(ZERO, 2), MACHINE_EPSILON);
        TestUtils.assertEquals(PI * PI, PrimitiveMath.POWER.invoke(PI, 2), MACHINE_EPSILON);
        TestUtils.assertEquals(E * E, PrimitiveMath.POWER.invoke(E, 2), MACHINE_EPSILON);

        TestUtils.assertEquals(1 / ZERO, PrimitiveMath.POWER.invoke(ZERO, -1), MACHINE_EPSILON);
        TestUtils.assertEquals(1 / PI, PrimitiveMath.POWER.invoke(PI, -1), MACHINE_EPSILON);
        TestUtils.assertEquals(1 / E, PrimitiveMath.POWER.invoke(E, -1), MACHINE_EPSILON);
    }

}
