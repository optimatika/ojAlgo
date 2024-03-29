/*
 * Copyright 1997-2024 Optimatika
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

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
abstract class MultiplyThresholdTuner extends ThresholdTuner {

    static final class CodeAndData {

        private static final Uniform UNIFORM = new Uniform();

        final MatrixStore<Double> left;
        final MatrixStore<Double> right;
        final R064Store target;

        CodeAndData(final int dim) {
            this(dim, false, false);
        }

        CodeAndData(final int dim, final boolean transposeLeft, final boolean transposeRight) {

            R064Store tmpL = R064Store.FACTORY.makeFilled(dim, dim, UNIFORM);
            left = transposeLeft ? tmpL.transpose() : tmpL;

            R064Store tmpR = R064Store.FACTORY.makeFilled(dim, dim, UNIFORM);
            right = transposeRight ? tmpR.transpose() : tmpR;

            target = R064Store.FACTORY.make(dim, dim);
        }

        R064Store execute() {
            target.fillByMultiplying(left, right);
            return target;
        }

    }

    MultiplyThresholdTuner.CodeAndData benchmark = null;

    @Override
    @Benchmark
    public final R064Store tune() {
        return benchmark.execute();
    }

}
