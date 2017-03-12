/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.decomposition;

import static org.ojalgo.function.PrimitiveFunction.*;

import org.junit.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.scalar.PrimitiveScalar;

/**
 * @author apete
 */
public class TestTQL2 {

    private static final int DIM = 50;

    public void setup() {

    }

    @Test
    public void testEvD1D_2D() {

        final double[] d1 = new double[DIM];
        final double[] d2 = new double[DIM];
        final double[] e1 = new double[DIM];
        final double[] e2 = new double[DIM];

        for (int i = 0; i < d1.length; i++) {
            d1[i] = d2[i] = Math.random();
            e1[i] = e2[i] = Math.random();
        }
        e1[DIM - 1] = 0;
        e2[DIM - 1] = 0;
        EvD1D.tql2a(d1, e1, null);
        EvD2D.tql2a(d2, e2, null);

        double normArr = 0.0;
        double normDif = 0.0;
        for (int i = 0; i < d1.length; i++) {
            normArr = HYPOT.invoke(normArr, d1[i]);
            normDif = HYPOT.invoke(normDif, d1[i] - d2[i]);
        }

        TestUtils.assertTrue(PrimitiveScalar.isSmall(normArr * 10, normDif));
    }

}
