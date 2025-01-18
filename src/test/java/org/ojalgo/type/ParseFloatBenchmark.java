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
Benchmark                                   Mode  Cnt    Score   Error   Units
ParseFloatBenchmark.parseFloat             thrpt    3   18.548 ± 0.243  ops/us
ParseFloatBenchmark.parseNumberDefinition  thrpt    3  122.361 ± 0.302  ops/us
 * </pre>
 */
@State(Scope.Benchmark)
public class ParseFloatBenchmark {

    private static final Random RANDOM = new Random();

    public static void main(final String[] args) throws Exception {
        BenchmarkUtils.run(BenchmarkUtils.options().timeUnit(TimeUnit.MICROSECONDS), ParseFloatBenchmark.class);
    }

    String string = "1.23456789";

    @Benchmark
    public float parseFloat() {
        return Float.parseFloat(string);
    }

    @Benchmark
    public float parseNumberDefinition() {
        return NumberDefinition.parseFloat(string);
    }

    @Setup
    public void setup() {
        string = Float.toString(RANDOM.nextFloat() - 0.5F);
    }

}
