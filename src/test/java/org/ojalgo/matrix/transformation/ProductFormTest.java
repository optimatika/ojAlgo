/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.matrix.transformation;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

public class ProductFormTest {

    /**
     * Product Form example from: lecture6-revisedsimplex.pdf
     * <P>
     * Revised Simplex or How to Use the Simplex without the Tableau
     */
    @Test
    public void testLecture6Example() {

        Primitive64Store col0 = Primitive64Store.FACTORY.column(8, 4, 2);
        Primitive64Store col1 = Primitive64Store.FACTORY.column(6, 2, 1.5);
        Primitive64Store col2 = Primitive64Store.FACTORY.column(1, 1.5, 0.5);

        Primitive64Store sol = Primitive64Store.FACTORY.make(3, 1);

        MatrixStore<Double> base0 = Primitive64Store.FACTORY.makeIdentity(3);
        MatrixStore<Double> mtrx0 = base0;
        InvertibleFactor<Double> factor0 = new IdentityFactor(3);

        factor0.ftran(col0, sol);
        ElementaryFactor factor1 = ElementaryFactor.from(sol, 2);
        PhysicalStore<Double> mtrx1 = Primitive64Store.FACTORY.makeEye(3, 3);
        mtrx1.fillColumn(2, sol);
        MatrixStore<Double> base1 = base0.multiply(mtrx1);

        factor0.ftran(col2, sol);
        factor1.ftran(sol);
        ElementaryFactor factor2 = ElementaryFactor.from(sol, 1);
        PhysicalStore<Double> mtrx2 = Primitive64Store.FACTORY.makeEye(3, 3);
        mtrx2.fillColumn(1, sol);
        MatrixStore<Double> base2 = base1.multiply(mtrx2);

        Primitive64Store expected = Primitive64Store.FACTORY.rows(new double[][] { { 1, 1, 8 }, { 0, 1.5, 4 }, { 0, 0.5, 2 } });

        TestUtils.assertEquals(expected, base2);

        LU<Double> lu = LU.R064.make(base2);
        lu.decompose(base2);
        MatrixStore<Double> expSol = lu.getSolution(col1);

        factor0.ftran(col1, sol);
        factor1.ftran(sol);
        factor2.ftran(sol);

        TestUtils.assertEquals(expSol, sol);

        lu.decompose(base2.transpose());
        expSol = lu.getSolution(col1);

        factor2.btran(col1, sol);
        factor1.btran(sol);
        factor0.btran(sol);

        TestUtils.assertEquals(expSol, sol);
    }

}
