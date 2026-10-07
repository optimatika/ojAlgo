/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.store;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.constant.PrimitiveMath;

/**
 * Filling or modifying (part of) a row, column or diagonal through
 * {@link TransformableRegion#regionByTransposing()} must give the same result as the transposed operation on
 * the base store.
 */
public class TransposedRegionTest extends MatrixStoreTests {

    private static final UnaryFunction<Double> MODIFIER = PrimitiveMath.MULTIPLY.second(3.0);
    private static final PrimitiveFunction.Nullary SUPPLIER = () -> 7.0;
    private static final Double VALUE = Double.valueOf(5.0);

    /**
     * A non-square (3 x 5) base, so mixing up rows and columns shows, with distinct non-zero elements.
     */
    private static R064Store newBase() {
        R064Store retVal = R064Store.FACTORY.make(3, 5);
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 3; i++) {
                retVal.set(i, j, 10 * (i + 1) + j);
            }
        }
        return retVal;
    }

    private static void assertSameAsBase(final Consumer<TransformableRegion<Double>> viaTransposed, final Consumer<R064Store> onBase) {

        R064Store expected = TransposedRegionTest.newBase();
        onBase.accept(expected);

        R064Store actual = TransposedRegionTest.newBase();
        viaTransposed.accept(actual.regionByTransposing());

        TestUtils.assertEquals(expected, actual);
    }

    @Test
    public void testFillColumn() {
        TransposedRegionTest.assertSameAsBase(region -> region.fillColumn(1, 2, VALUE), base -> base.fillRow(2, 1, VALUE));
        TransposedRegionTest.assertSameAsBase(region -> region.fillColumn(1, 2, SUPPLIER), base -> base.fillRow(2, 1, SUPPLIER));
        TransposedRegionTest.assertSameAsBase(region -> region.fillColumn(2, VALUE), base -> base.fillRow(2, VALUE));
    }

    @Test
    public void testFillDiagonal() {
        TransposedRegionTest.assertSameAsBase(region -> region.fillDiagonal(0, 1, VALUE), base -> base.fillDiagonal(1, 0, VALUE));
        TransposedRegionTest.assertSameAsBase(region -> region.fillDiagonal(0, 1, SUPPLIER), base -> base.fillDiagonal(1, 0, SUPPLIER));
        TransposedRegionTest.assertSameAsBase(region -> region.fillDiagonal(VALUE), base -> base.fillDiagonal(VALUE));
        TransposedRegionTest.assertSameAsBase(region -> region.fillDiagonal(SUPPLIER), base -> base.fillDiagonal(SUPPLIER));
    }

    @Test
    public void testFillRow() {
        TransposedRegionTest.assertSameAsBase(region -> region.fillRow(1, 0, VALUE), base -> base.fillColumn(0, 1, VALUE));
        TransposedRegionTest.assertSameAsBase(region -> region.fillRow(1, 0, SUPPLIER), base -> base.fillColumn(0, 1, SUPPLIER));
        TransposedRegionTest.assertSameAsBase(region -> region.fillRow(3, VALUE), base -> base.fillColumn(3, VALUE));
    }

    @Test
    public void testModifyColumnDiagonalRow() {
        TransposedRegionTest.assertSameAsBase(region -> region.modifyColumn(1, 2, MODIFIER), base -> base.modifyRow(2, 1, MODIFIER));
        TransposedRegionTest.assertSameAsBase(region -> region.modifyDiagonal(0, 1, MODIFIER), base -> base.modifyDiagonal(1, 0, MODIFIER));
        TransposedRegionTest.assertSameAsBase(region -> region.modifyRow(1, 0, MODIFIER), base -> base.modifyColumn(0, 1, MODIFIER));
    }

}
