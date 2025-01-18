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
package org.ojalgo.type;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.ojalgo.BenchmarkUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * <pre>
Benchmark                                  Mode  Cnt   Score   Error   Units
ParseLongBenchmark.parseLong              thrpt    3  53.374 ± 2.100  ops/us
ParseLongBenchmark.parseNumberDefinition  thrpt    3  49.529 ± 0.306  ops/us
 * </pre>
 */
@State(Scope.Benchmark)
public class ParseLongBenchmark {

    private static final long HALF = Long.MAX_VALUE / 2L;
    private static final Random RANDOM = new Random();

    public static void main(final String[] args) throws Exception {
        BenchmarkUtils.run(BenchmarkUtils.options().timeUnit(TimeUnit.MICROSECONDS), ParseLongBenchmark.class);
    }

    String string = "123456789";

    @Benchmark
    public long parseLong() {
        return Long.parseLong(string);
    }

    @Benchmark
    public long parseNumberDefinition() {
        return NumberDefinition.parseLong(string);
    }

    @Setup
    public void setup() {
        string = Long.toString(RANDOM.nextLong() - HALF);
    }

}
