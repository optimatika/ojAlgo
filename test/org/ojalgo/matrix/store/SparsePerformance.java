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
package org.ojalgo.matrix.store;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.structure.ElementView1D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;

public class SparsePerformance extends MatrixStoreTests {

    static void fill(final Mutate2D mtrx) {
        int limit = Math.toIntExact(Math.min(mtrx.countRows(), mtrx.countColumns()));
        for (int ij = 0; ij < limit; ij++) {
            mtrx.set(ij, Uniform.randomInteger(limit), Math.random());
            mtrx.set(Uniform.randomInteger(limit), ij, Math.random());
        }
    }

    static void touchNonzeros(final MatrixStore<Double> store) {
        ElementView1D<Double, ?> nz = store.nonzeros();
        while (nz.hasNext()) {
            nz.next();
        }
    }

    @Test
    public void testElementwiseMultiplication() {

        int n = 100_000;

        SparseStore<Double> mtrxA = SparseStore.PRIMITIVE.make(n, n);
        SparseStore<Double> mtrxB = SparseStore.PRIMITIVE.make(n, n);

        Stopwatch clock = new Stopwatch();

        mtrxA.operateOnMatching(PrimitiveMath.MULTIPLY, mtrxB);

        SparsePerformance.touchNonzeros(mtrxA);

        TestUtils.assertFasterThan(5, CalendarDateUnit.MILLIS, clock);

        SparsePerformance.fill(mtrxA);
        SparsePerformance.fill(mtrxB);

        clock.reset();

        mtrxA.operateOnMatching(PrimitiveMath.MULTIPLY, mtrxB);

        SparsePerformance.touchNonzeros(mtrxA);

        TestUtils.assertFasterThan(10, CalendarDateUnit.MILLIS, clock);
    }

    @Test
    @Tag("slow")
    public void testMatrixMultiplication() {

        int n = 100_000;

        SparseStore<Double> mtrxA = SparseStore.PRIMITIVE.make(n, n);
        SparseStore<Double> mtrxB = SparseStore.PRIMITIVE.make(n, n);

        Stopwatch clock = new Stopwatch();

        SparsePerformance.touchNonzeros(mtrxA.multiply(mtrxB));

        TestUtils.assertFasterThan(50, CalendarDateUnit.MILLIS, clock);

        SparsePerformance.fill(mtrxA);
        SparsePerformance.fill(mtrxB);

        clock.reset();

        SparsePerformance.touchNonzeros(mtrxA.multiply(mtrxB));

        TestUtils.assertFasterThan(50, CalendarDateUnit.SECOND, clock);
    }

    @Test
    public void testMultiplyByOneVector() {

        int n = 100_000;

        SparseStore<Double> mtrx = SparseStore.PRIMITIVE.make(n, n);

        Primitive64Store ones = Primitive64Store.FACTORY.make(n, 1);
        ones.fillAll(ONE);

        Stopwatch clock = new Stopwatch();

        SparsePerformance.touchNonzeros(mtrx.multiply(ones));

        TestUtils.assertFasterThan(50, CalendarDateUnit.MILLIS, clock);

        SparsePerformance.fill(mtrx);

        clock.reset();

        SparsePerformance.touchNonzeros(mtrx.multiply(ones));

        TestUtils.assertFasterThan(110, CalendarDateUnit.MILLIS, clock);

        clock.reset();

        SparsePerformance.touchNonzeros(mtrx.premultiply(ones).get());

        TestUtils.assertFasterThan(5, CalendarDateUnit.SECOND, clock);
    }

    @Test
    public void testReduceColumns() {

        int n = 100_000;

        SparseStore<Double> mtrx = SparseStore.PRIMITIVE.make(n, n);
        Primitive64Store vctr = Primitive64Store.FACTORY.makeZero(1, n);

        Stopwatch clock = new Stopwatch();

        mtrx.reduceColumns(Aggregator.SUM).supplyTo(vctr);
        SparsePerformance.touchNonzeros(vctr);

        TestUtils.assertFasterThan(10, CalendarDateUnit.MILLIS, clock);

        SparsePerformance.fill(mtrx);

        clock.reset();

        mtrx.reduceColumns(Aggregator.SUM).supplyTo(vctr);
        SparsePerformance.touchNonzeros(vctr);

        TestUtils.assertFasterThan(1, CalendarDateUnit.SECOND, clock);
    }

    @Test
    public void testReduceRows() {

        int n = 100_000;

        SparseStore<Double> mtrx = SparseStore.PRIMITIVE.make(n, n);
        Primitive64Store vctr = Primitive64Store.FACTORY.makeZero(n, 1);

        Stopwatch clock = new Stopwatch();

        mtrx.reduceRows(Aggregator.SUM).supplyTo(vctr);
        SparsePerformance.touchNonzeros(vctr);

        TestUtils.assertFasterThan(50, CalendarDateUnit.MILLIS, clock);

        SparsePerformance.fill(mtrx);

        clock.reset();

        mtrx.reduceRows(Aggregator.SUM).supplyTo(vctr);
        SparsePerformance.touchNonzeros(vctr);

        TestUtils.assertFasterThan(1, CalendarDateUnit.SECOND, clock);
    }

}
