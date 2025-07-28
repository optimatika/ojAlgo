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
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.random.Uniform;

public class ProductFormInverseTest extends OptimisationLinearTests {

    /**
     * Product Form example from: lecture6-revisedsimplex.pdf
     * <P>
     * Revised Simplex or How to Use the Simplex without the Tableau
     *
     * @see ElementaryFactorTest
     */
    @Test
    public void testLecture6Example() {

        SparseArray<Double> arr0 = SparseArray.factory(ArrayR064.FACTORY).make(3);
        arr0.set(0, 8);
        arr0.set(1, 4);
        arr0.set(2, 2);
        SparseArray<Double> arr1 = SparseArray.factory(ArrayR064.FACTORY).make(3);
        arr1.set(0, 6);
        arr1.set(1, 2);
        arr1.set(2, 1.5);
        SparseArray<Double> arr2 = SparseArray.factory(ArrayR064.FACTORY).make(3);
        arr2.set(0, 1);
        arr2.set(1, 1.5);
        arr2.set(2, 0.5);

        R064Store col0 = R064Store.FACTORY.column(8, 4, 2);
        R064Store col1 = R064Store.FACTORY.column(6, 2, 1.5);
        R064Store col2 = R064Store.FACTORY.column(1, 1.5, 0.5);

        TestUtils.assertEquals(col0, arr0);
        TestUtils.assertEquals(col1, arr1);
        TestUtils.assertEquals(col2, arr2);

        RawStore expBase0 = RawStore.wrap(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });
        RawStore expBase1 = RawStore.wrap(new double[][] { { 1, 0, 8 }, { 0, 1, 4 }, { 0, 0, 2 } });
        RawStore expBase2 = RawStore.wrap(new double[][] { { 1, 1, 8 }, { 0, 1.5, 4 }, { 0, 0.5, 2 } });

        R064Store random = R064Store.FACTORY.makeFilled(3, 1, Uniform.standard());
        R064Store exp = R064Store.FACTORY.make(3, 1);
        R064Store act = R064Store.FACTORY.make(3, 1);

        LU<Double> lu = LU.R064.make();
        lu.decompose(expBase0);
        ProductFormInverse factors = new ProductFormInverse(3, 1E-32);
        factors.reset(expBase0);
        random.supplyTo(exp);
        lu.ftran(exp);
        random.supplyTo(act);
        factors.ftran(act);
        TestUtils.assertEquals(exp, act);
        random.supplyTo(exp);
        lu.btran(exp);
        random.supplyTo(act);
        factors.btran(act);
        TestUtils.assertEquals(exp, act);

        lu.decompose(expBase1);
        factors.update(expBase1, 2, arr0);
        random.supplyTo(exp);
        lu.ftran(exp);
        random.supplyTo(act);
        factors.ftran(act);
        TestUtils.assertEquals(exp, act);
        random.supplyTo(exp);
        lu.btran(exp);
        random.supplyTo(act);
        factors.btran(act);
        TestUtils.assertEquals(exp, act);

        lu.decompose(expBase2);
        factors.update(expBase2, 1, arr2);
        random.supplyTo(exp);
        lu.ftran(exp);
        random.supplyTo(act);
        factors.ftran(act);
        TestUtils.assertEquals(exp, act);
        random.supplyTo(exp);
        lu.btran(exp);
        random.supplyTo(act);
        factors.btran(act);
        TestUtils.assertEquals(exp, act);
    }

}
