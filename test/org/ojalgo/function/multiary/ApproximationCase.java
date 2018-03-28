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
package org.ojalgo.function.multiary;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Uniform;
import org.ojalgo.type.context.NumberContext;

public class ApproximationCase {

    @Test
    public void testFirstOrderApproximation() {

        final int tmpArity = 9;

        final PhysicalStore<Double> tmpLinear = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-1, 2));

        final PhysicalStore<Double> tmpPoint = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-10, 20));

        final LinearFunction<Double> tmpOrgFunc = LinearFunction.makePrimitive(tmpLinear);
        final FirstOrderApproximation<Double> tmpApprFunc = tmpOrgFunc.toFirstOrderApproximation(tmpPoint);

        final PhysicalStore<Double> tmpX = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-10, 20));

        TestUtils.assertEquals(tmpOrgFunc.invoke(tmpX), tmpApprFunc.invoke(tmpX), new NumberContext(7, 14));
    }

    @Test
    public void testSecondOrderApproximation() {

        final int tmpArity = 9;

        final PhysicalStore<Double> tmpQuadratic = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, tmpArity, new Uniform());
        final PhysicalStore<Double> tmpLinear = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-1, 2));

        final PhysicalStore<Double> tmpPoint = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-10, 20));

        final CompoundFunction<Double> tmpOrgFunc = CompoundFunction.makePrimitive(tmpQuadratic, tmpLinear);
        final SecondOrderApproximation<Double> tmpApprFunc = tmpOrgFunc.toSecondOrderApproximation(tmpPoint);

        final PhysicalStore<Double> tmpX = PrimitiveDenseStore.FACTORY.makeFilled(tmpArity, 1, new Uniform(-10, 20));

        TestUtils.assertEquals(tmpOrgFunc.invoke(tmpX), tmpApprFunc.invoke(tmpX), PrimitiveFunction.SQRT.invoke(1E-14 / PrimitiveMath.THREE));
    }

}
