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
package org.ojalgo.matrix.store;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.random.Uniform;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.ElementView2D;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;

public class SparsePerformance extends MatrixStoreTests {

    @Test
    public void testElementwiseMultiplication() {

        int n = 50_000;
        SparseStore<Double> a = SparseStore.PRIMITIVE.make(n, n);
        SparseStore<Double> b = SparseStore.PRIMITIVE.make(n, n);

        Stopwatch clock = new Stopwatch();

        SparseStore<Double> ab1 = SparseStore.PRIMITIVE.make(n, n);
        a.multiply(b).get().supplyTo(ab1);
        final ElementView2D<Double, ?> nnz1 = ab1.nonzeros();
        while (nnz1.hasNext()) {
            nnz1.next();
        }

        TestUtils.assertTrue(clock.stop(CalendarDateUnit.SECOND).measure < ONE);

        SparseStore<Double> ab2 = SparseStore.PRIMITIVE.make(n, n);
        a.operateOnMatching(MULTIPLY, b).supplyTo(ab2);
        final ElementView2D<Double, ?> nnz2 = ab2.nonzeros();
        while (nnz2.hasNext()) {
            nnz2.next();
        }

        TestUtils.assertTrue(clock.stop(CalendarDateUnit.SECOND).measure < TWO);
    }

    @Test
    public void testMultByOneVector() {

        int n = 100_000;

        SparseStore<Double> a = SparseStore.PRIMITIVE.make(n, n);

        Stopwatch clock = new Stopwatch();

        PrimitiveDenseStore ones = PrimitiveDenseStore.FACTORY.makeZero(n, 1);
        ones.modifyAll(ADD.second(ONE));

        MatrixStore<Double> sum = a.multiply(ones);

        ElementView1D<Double, ?> nnz = sum.nonzeros();
        while (nnz.hasNext()) {
            nnz.next();
        }

        TestUtils.assertTrue(clock.stop(CalendarDateUnit.SECOND).measure < ONE);
    }

    @Test
    public void testReduceRows() {

        int n = 100_000;
        SparseStore<Double> a = SparseStore.PRIMITIVE.make(n, n);

        Stopwatch clock = new Stopwatch();

        PrimitiveDenseStore sum = PrimitiveDenseStore.FACTORY.makeZero(n, 1);
        a.reduceRows(Aggregator.SUM).supplyTo(sum);

        ElementView1D<Double, ?> nnz = sum.get().nonzeros();
        while (nnz.hasNext()) {
            nnz.next();
        }

        TestUtils.assertTrue(clock.stop(CalendarDateUnit.SECOND).measure < ONE);
    }

    @Test
    @Tag("slow")
    public void testMatrixMultiplication() {

        int n = 100_000;

        SparseStore<Double> a = SparseStore.PRIMITIVE.make(n, n);
        SparseStore<Double> b = SparseStore.PRIMITIVE.make(n, n);

        for (int ij = 0; ij < n; ij++) {
            a.set(ij, Uniform.randomInteger(n), Math.random());
            a.set(Uniform.randomInteger(n), ij, Math.random());
            b.set(ij, Uniform.randomInteger(n), Math.random());
            b.set(Uniform.randomInteger(n), ij, Math.random());
        }

        Stopwatch clock = new Stopwatch();

        MatrixStore<Double> sum = a.multiply(b);

        ElementView1D<Double, ?> nnz = sum.nonzeros();
        while (nnz.hasNext()) {
            nnz.next();
        }

        TestUtils.assertTrue(clock.stop(CalendarDateUnit.SECOND).measure < 150);
    }

}
