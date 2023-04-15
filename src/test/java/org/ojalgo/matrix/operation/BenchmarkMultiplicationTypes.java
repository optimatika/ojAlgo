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
package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

@State(Scope.Benchmark)
public class BenchmarkMultiplicationTypes {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkMultiplicationTypes.class);
    }

    //@Param({ "3", "4", "9", "10" })
    @Param({ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" })
    public int complexity;

    public Primitive64Store left;
    public Primitive64Store product;
    public Primitive64Store right;

    MultiplyBoth.Primitive MB;
    MultiplyLeft.Primitive64 ML;
    MultiplyNeither.Primitive64 MN;
    MultiplyRight.Primitive64 MR;

    @Benchmark
    public Primitive64Store multiplyBoth() {
        MB.invoke(product, left, complexity, right);
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyLeft() {
        ML.invoke(product.data, left, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyNeither() {
        MN.invoke(product.data, left.data, complexity, right.data);
        return product;
    }

    @Benchmark
    public Primitive64Store multiplyRight() {
        MR.invoke(product.data, left.data, complexity, right);
        return product;
    }

    @Setup
    public void setup() {

        left = Primitive64Store.FACTORY.makeFilled(complexity, complexity, Normal.standard());
        right = Primitive64Store.FACTORY.makeFilled(complexity, complexity, Normal.standard());
        product = Primitive64Store.FACTORY.make(complexity, complexity);

        ML = MultiplyLeft.newPrimitive64(complexity, complexity);
        MR = MultiplyRight.newPrimitive64(complexity, complexity);
        MN = MultiplyNeither.newPrimitive64(complexity, complexity);
        MB = MultiplyBoth.newPrimitive64(complexity, complexity);
    }
}
