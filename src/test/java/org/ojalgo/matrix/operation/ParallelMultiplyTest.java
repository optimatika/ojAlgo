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
package org.ojalgo.matrix.operation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R032Store;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.type.context.NumberContext;

/**
 * The general case of {@link MultiplyLeft}, {@link MultiplyRight}, {@link MultiplyBoth} and
 * {@link MultiplyNeither} (float and generic variants) must give the same product multi-threaded (threshold
 * lowered so that it splits at test size) as single-threaded (threshold raised so that it doesn't). The
 * non-array operands are transposed views, as in A<sup>T</sup>B.
 */
public class ParallelMultiplyTest {

    private static final int COMPLEXITY = 41;
    private static final NumberContext FLOAT_ACCURACY = NumberContext.of(6);
    private static final NumberContext GENERIC_ACCURACY = NumberContext.of(14);
    private static final int NB_COLS = 29;
    private static final int NB_ROWS = 37;
    private static final NumberContext PRIMITIVE_ACCURACY = NumberContext.of(12);

    /**
     * Runs {@code multiply} single-threaded and multi-threaded, using {@code threshold} to set the class's
     * threshold, and compares the two products with {@code assertion}.
     */
    private static <T> void compare(final Consumer<Integer> threshold, final Supplier<T> multiply, final BiConsumer<T, T> assertion) {
        try {
            threshold.accept(Integer.MAX_VALUE);
            T single = multiply.get();
            threshold.accept(4);
            T parallel = multiply.get();
            assertion.accept(single, parallel);
        } finally {
            threshold.accept(Integer.MAX_VALUE);
        }
    }

    @Test
    public void testMultiplyBoth() {

        int threshold = MultiplyBoth.THRESHOLD;

        try {

            R064Store a = R064Store.FACTORY.makeFilled(COMPLEXITY, NB_ROWS, new Uniform());
            R064Store b = R064Store.FACTORY.makeFilled(NB_COLS, COMPLEXITY, new Uniform());
            MatrixStore<Double> at = a.transpose();
            MatrixStore<Double> bt = b.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyBoth.THRESHOLD = t, () -> {
                R064Store product = R064Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyBoth.newPrimitive64(NB_ROWS, NB_COLS).invoke(product, at, COMPLEXITY, bt);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, PRIMITIVE_ACCURACY));

            GenericStore<Quadruple> ga = GenericStore.R128.makeFilled(COMPLEXITY, NB_ROWS, new Uniform());
            GenericStore<Quadruple> gb = GenericStore.R128.makeFilled(NB_COLS, COMPLEXITY, new Uniform());
            MatrixStore<Quadruple> gat = ga.transpose();
            MatrixStore<Quadruple> gbt = gb.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyBoth.THRESHOLD = t, () -> {
                GenericStore<Quadruple> product = GenericStore.R128.make(NB_ROWS, NB_COLS);
                MultiplyBoth.<Quadruple>newGeneric(NB_ROWS, NB_COLS).invoke(product, gat, COMPLEXITY, gbt);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, GENERIC_ACCURACY));

        } finally {
            MultiplyBoth.THRESHOLD = threshold;
        }
    }

    @Test
    public void testMultiplyLeft() {

        int threshold = MultiplyLeft.THRESHOLD;

        try {

            R064Store a = R064Store.FACTORY.makeFilled(COMPLEXITY, NB_ROWS, new Uniform());
            R064Store b = R064Store.FACTORY.makeFilled(COMPLEXITY, NB_COLS, new Uniform());
            MatrixStore<Double> at = a.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyLeft.THRESHOLD = t, () -> {
                R064Store product = R064Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyLeft.newPrimitive64(NB_ROWS, NB_COLS).invoke(product.data, at, COMPLEXITY, b.data);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, PRIMITIVE_ACCURACY));

            R032Store fa = R032Store.FACTORY.makeFilled(COMPLEXITY, NB_ROWS, new Uniform());
            R032Store fb = R032Store.FACTORY.makeFilled(COMPLEXITY, NB_COLS, new Uniform());
            MatrixStore<Double> fat = fa.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyLeft.THRESHOLD = t, () -> {
                R032Store product = R032Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyLeft.newPrimitive32(NB_ROWS, NB_COLS).invoke(product.data, fat, COMPLEXITY, fb.data);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, FLOAT_ACCURACY));

            GenericStore<Quadruple> ga = GenericStore.R128.makeFilled(COMPLEXITY, NB_ROWS, new Uniform());
            GenericStore<Quadruple> gb = GenericStore.R128.makeFilled(COMPLEXITY, NB_COLS, new Uniform());
            MatrixStore<Quadruple> gat = ga.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyLeft.THRESHOLD = t, () -> {
                GenericStore<Quadruple> product = GenericStore.R128.make(NB_ROWS, NB_COLS);
                MultiplyLeft.<Quadruple>newGeneric(NB_ROWS, NB_COLS).invoke(product.data, gat, COMPLEXITY, gb.data, Quadruple.FACTORY);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, GENERIC_ACCURACY));

        } finally {
            MultiplyLeft.THRESHOLD = threshold;
        }
    }

    @Test
    public void testMultiplyNeither() {

        int threshold = MultiplyNeither.THRESHOLD;

        try {

            R032Store fa = R032Store.FACTORY.makeFilled(NB_ROWS, COMPLEXITY, new Uniform());
            R032Store fb = R032Store.FACTORY.makeFilled(COMPLEXITY, NB_COLS, new Uniform());

            ParallelMultiplyTest.compare(t -> MultiplyNeither.THRESHOLD = t, () -> {
                R032Store product = R032Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyNeither.newPrimitive32(NB_ROWS, NB_COLS).invoke(product.data, fa.data, COMPLEXITY, fb.data);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, FLOAT_ACCURACY));

            GenericStore<Quadruple> ga = GenericStore.R128.makeFilled(NB_ROWS, COMPLEXITY, new Uniform());
            GenericStore<Quadruple> gb = GenericStore.R128.makeFilled(COMPLEXITY, NB_COLS, new Uniform());

            ParallelMultiplyTest.compare(t -> MultiplyNeither.THRESHOLD = t, () -> {
                GenericStore<Quadruple> product = GenericStore.R128.make(NB_ROWS, NB_COLS);
                MultiplyNeither.<Quadruple>newGeneric(NB_ROWS, NB_COLS).invoke(product.data, ga.data, COMPLEXITY, gb.data, Quadruple.FACTORY);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, GENERIC_ACCURACY));

        } finally {
            MultiplyNeither.THRESHOLD = threshold;
        }
    }

    @Test
    public void testMultiplyRight() {

        int threshold = MultiplyRight.THRESHOLD;

        try {

            R064Store a = R064Store.FACTORY.makeFilled(NB_ROWS, COMPLEXITY, new Uniform());
            R064Store b = R064Store.FACTORY.makeFilled(NB_COLS, COMPLEXITY, new Uniform());
            MatrixStore<Double> bt = b.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyRight.THRESHOLD = t, () -> {
                R064Store product = R064Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyRight.newPrimitive64(NB_ROWS, NB_COLS).invoke(product.data, a.data, COMPLEXITY, bt);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, PRIMITIVE_ACCURACY));

            R032Store fa = R032Store.FACTORY.makeFilled(NB_ROWS, COMPLEXITY, new Uniform());
            R032Store fb = R032Store.FACTORY.makeFilled(NB_COLS, COMPLEXITY, new Uniform());
            MatrixStore<Double> fbt = fb.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyRight.THRESHOLD = t, () -> {
                R032Store product = R032Store.FACTORY.make(NB_ROWS, NB_COLS);
                MultiplyRight.newPrimitive32(NB_ROWS, NB_COLS).invoke(product.data, fa.data, COMPLEXITY, fbt);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, FLOAT_ACCURACY));

            GenericStore<Quadruple> ga = GenericStore.R128.makeFilled(NB_ROWS, COMPLEXITY, new Uniform());
            GenericStore<Quadruple> gb = GenericStore.R128.makeFilled(NB_COLS, COMPLEXITY, new Uniform());
            MatrixStore<Quadruple> gbt = gb.transpose();

            ParallelMultiplyTest.compare(t -> MultiplyRight.THRESHOLD = t, () -> {
                GenericStore<Quadruple> product = GenericStore.R128.make(NB_ROWS, NB_COLS);
                MultiplyRight.<Quadruple>newGeneric(NB_ROWS, NB_COLS).invoke(product.data, ga.data, COMPLEXITY, gbt, Quadruple.FACTORY);
                return product;
            }, (single, parallel) -> TestUtils.assertEquals(single, parallel, GENERIC_ACCURACY));

        } finally {
            MultiplyRight.THRESHOLD = threshold;
        }
    }

}
