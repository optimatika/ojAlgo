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
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.random.Uniform;

public class ProductFormInverseTest extends OptimisationLinearTests {

    private static R064CSC buildCSC(final double[][] data) {
        int m = data.length;
        int n = data[0].length;
        R064CSC.Builder builder = R064CSC.newBuilder(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] != 0.0) {
                    builder.set(i, j, data[i][j]);
                }
            }
        }
        return builder.build();
    }

    /**
     * Product Form example from: lecture6-revisedsimplex.pdf
     * <p>
     * Revised Simplex or How to Use the Simplex without the Tableau
     *
     * @see ElementaryFactorTest
     */
    @Test
    public void testLecture6Example() {

        // Full 3x3 identity — all 3 columns form the initial basis
        R064CSC csc0 = ProductFormInverseTest.buildCSC(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });
        int[] included0 = { 0, 1, 2 };

        // After replacing column 2 with [8,4,2]
        R064CSC csc1 = ProductFormInverseTest.buildCSC(new double[][] { { 1, 0, 8 }, { 0, 1, 4 }, { 0, 0, 2 } });
        int[] included1 = { 0, 1, 2 };

        // After replacing column 1 with [1,1.5,0.5]
        R064CSC csc2 = ProductFormInverseTest.buildCSC(new double[][] { { 1, 1, 8 }, { 0, 1.5, 4 }, { 0, 0.5, 2 } });
        int[] included2 = { 0, 1, 2 };

        RawStore expBase0 = RawStore.wrap(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });
        RawStore expBase1 = RawStore.wrap(new double[][] { { 1, 0, 8 }, { 0, 1, 4 }, { 0, 0, 2 } });
        RawStore expBase2 = RawStore.wrap(new double[][] { { 1, 1, 8 }, { 0, 1.5, 4 }, { 0, 0.5, 2 } });

        R064Store random = R064Store.FACTORY.makeFilled(3, 1, Uniform.standard());
        R064Store exp = R064Store.FACTORY.make(3, 1);
        R064Store act = R064Store.FACTORY.make(3, 1);

        LU<Double> lu = LU.R064.make();
        lu.decompose(expBase0);
        ProductFormInverse factors = new ProductFormInverse(3);
        factors.reset(csc0, included0);
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
        factors.update(csc1, included1, 2, 2);
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
        factors.update(csc2, included2, 1, 1);
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
