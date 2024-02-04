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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.operation.AggregateAll;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Mac Pro. 2015-06-24 => ?
 *
 * <pre>
 * </pre>
 *
 * MacBook Air. 2015-06-26 => ?
 *
 * <pre>
# Run complete. Total time: 00:01:08

Benchmark                   (dim)  (z)   Mode  Cnt       Score       Error    Units
ThresholdAggregateAll.tune     64    1  thrpt    3  175909,896 ± 15008,403  ops/min
ThresholdAggregateAll.tune     64    2  thrpt    3   76096,684 ± 39072,946  ops/min
ThresholdAggregateAll.tune    128    1  thrpt    3   45671,825 ± 20224,418  ops/min
ThresholdAggregateAll.tune    128    2  thrpt    3   45355,715 ± 15589,210  ops/min
ThresholdAggregateAll.tune    256    1  thrpt    3   11106,307 ±  2944,770  ops/min
ThresholdAggregateAll.tune    256    2  thrpt    3   15898,111 ± 41469,780  ops/min
ThresholdAggregateAll.tune    512    1  thrpt    3    2608,882 ±   940,576  ops/min
ThresholdAggregateAll.tune    512    2  thrpt    3    4174,439 ±   357,190  ops/min
 * </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class ThresholdAggregateAll extends AbstractThresholdTuner {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(ThresholdAggregateAll.class);
    }

    @Param({ "64", "128", "256", "512" })
    public int dim;

    @Param({ "1", "2" })
    public int z;

    PhysicalStore<Double> target;

    @Override
    @Setup
    public void setup() {

        AggregateAll.THRESHOLD = dim / z;

        final Uniform tmpSupplier = new Uniform();

        target = Primitive64Store.FACTORY.makeFilled(dim, dim, tmpSupplier);
    }

    @Override
    @Benchmark
    public Object tune() {
        for (final Aggregator tmpAggregator : Aggregator.values()) {
            target.aggregateAll(tmpAggregator);
        }
        return target;
    }

}
