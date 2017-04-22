/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.ojalgo;

import org.ojalgo.access.Access1D;
import org.ojalgo.array.blas.DOT;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Normal;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * # VM invoker: /Library/Java/JavaVirtualMachines/jdk1.8.0.jdk/Contents/Home/jre/bin/java # VM options:
 * -Dfile.encoding=UTF-8 # Warmup: 20 iterations, 1 s each # Measurement: 20 iterations, 1 s each # Threads: 1
 * thread, will synchronize iterations # Benchmark mode: Throughput, ops/time
 *
 * <pre>
 * Benchmark                        (columns)  (complexity)  (rows)   Mode  Samples    Score  Score error  Units
 *
 * o.o.b.BenchLeft.multiplyLeft1          100           100     100  thrpt       20  859,320        6,760  ops/s
 * o.o.b.BenchLeft.multiplyLeft2          100           100     100  thrpt       20  857,220        6,308  ops/s
 * o.o.b.BenchLeft.multiplyLeft3          100           100     100  thrpt       20  857,804        4,672  ops/s
 * o.o.b.BenchLeft.multiplyLeft4          100           100     100  thrpt       20  858,448        3,849  ops/s
 *
 * o.o.b.BenchLeft.multiplyLeft1          100           100    1000  thrpt       20   84,237        0,691  ops/s
 * o.o.b.BenchLeft.multiplyLeft2          100           100    1000  thrpt       20   84,148        0,429  ops/s
 * o.o.b.BenchLeft.multiplyLeft3          100           100    1000  thrpt       20   84,191        0,504  ops/s
 * o.o.b.BenchLeft.multiplyLeft4          100           100    1000  thrpt       20   83,959        0,384  ops/s
 *
 *
 * o.o.b.BenchLeft.multiplyLeft1          100          1000     100  thrpt       20   80,067        0,527  ops/s
 * o.o.b.BenchLeft.multiplyLeft2          100          1000     100  thrpt       20   80,165        0,225  ops/s
 * o.o.b.BenchLeft.multiplyLeft3          100          1000     100  thrpt       20   80,260        0,389  ops/s
 * o.o.b.BenchLeft.multiplyLeft4          100          1000     100  thrpt       20   79,950        0,504  ops/s
 *
 *
 * o.o.b.BenchLeft.multiplyLeft1          100          1000    1000  thrpt       20    7,482        0,046  ops/s
 * o.o.b.BenchLeft.multiplyLeft2          100          1000    1000  thrpt       20    7,462        0,033  ops/s
 * o.o.b.BenchLeft.multiplyLeft3          100          1000    1000  thrpt       20    7,438        0,047  ops/s
 * o.o.b.BenchLeft.multiplyLeft4          100          1000    1000  thrpt       20    7,493        0,044  ops/s
 *
 * o.o.b.BenchLeft.multiplyLeft1         1000           100     100  thrpt       20   86,321        0,509  ops/s
 * o.o.b.BenchLeft.multiplyLeft2         1000           100     100  thrpt       20   86,610        0,322  ops/s
 * o.o.b.BenchLeft.multiplyLeft3         1000           100     100  thrpt       20   86,589        0,425  ops/s
 * o.o.b.BenchLeft.multiplyLeft4         1000           100     100  thrpt       20   86,534        0,313  ops/s
 *
 * o.o.b.BenchLeft.multiplyLeft1         1000           100    1000  thrpt       20    8,435        0,066  ops/s
 * o.o.b.BenchLeft.multiplyLeft2         1000           100    1000  thrpt       20    8,494        0,048  ops/s
 * o.o.b.BenchLeft.multiplyLeft3         1000           100    1000  thrpt       20    8,465        0,057  ops/s
 * o.o.b.BenchLeft.multiplyLeft4         1000           100    1000  thrpt       20    8,399        0,030  ops/s
 *
 * o.o.b.BenchLeft.multiplyLeft1         1000          1000     100  thrpt       20    7,073        0,060  ops/s
 * o.o.b.BenchLeft.multiplyLeft2         1000          1000     100  thrpt       20    6,980        0,064  ops/s
 * o.o.b.BenchLeft.multiplyLeft3         1000          1000     100  thrpt       20    6,910        0,071  ops/s
 * o.o.b.BenchLeft.multiplyLeft4         1000          1000     100  thrpt       20    6,989        0,061  ops/s
 *
 * o.o.b.BenchLeft.multiplyLeft1         1000          1000    1000  thrpt       20    0,693        0,008  ops/s
 * o.o.b.BenchLeft.multiplyLeft2         1000          1000    1000  thrpt       20    0,693        0,008  ops/s
 * o.o.b.BenchLeft.multiplyLeft3         1000          1000    1000  thrpt       20    0,696        0,009  ops/s
 * o.o.b.BenchLeft.multiplyLeft4         1000          1000    1000  thrpt       20    0,698        0,007  ops/s
 * </pre>
 *
 * <pre>
 * # Run complete. Total time: 00:12:07

Benchmark                 (columns)  (complexity)  (rows)   Mode  Cnt    Score   Error  Units
MultiplyLeftImpl.changed        100           100     100  thrpt   20  842,092 ± 3,576  ops/s
MultiplyLeftImpl.defined        100           100     100  thrpt   20  838,625 ± 1,641  ops/s

MultiplyLeftImpl.changed        100           100    1000  thrpt   20   88,026 ± 0,066  ops/s
MultiplyLeftImpl.defined        100           100    1000  thrpt   20   89,866 ± 0,046  ops/s

MultiplyLeftImpl.changed        100          1000     100  thrpt   20   59,001 ± 0,054  ops/s
MultiplyLeftImpl.defined        100          1000     100  thrpt   20   82,015 ± 0,101  ops/s

MultiplyLeftImpl.changed        100          1000    1000  thrpt   20    5,607 ± 0,002  ops/s
MultiplyLeftImpl.defined        100          1000    1000  thrpt   20    7,677 ± 0,004  ops/s

MultiplyLeftImpl.changed       1000           100     100  thrpt   20   59,004 ± 0,026  ops/s
MultiplyLeftImpl.defined       1000           100     100  thrpt   20   90,617 ± 0,053  ops/s

MultiplyLeftImpl.changed       1000           100    1000  thrpt   20    5,780 ± 0,003  ops/s
MultiplyLeftImpl.defined       1000           100    1000  thrpt   20    8,858 ± 0,004  ops/s

MultiplyLeftImpl.changed       1000          1000     100  thrpt   20    5,391 ± 0,006  ops/s
MultiplyLeftImpl.defined       1000          1000     100  thrpt   20    7,166 ± 0,009  ops/s

MultiplyLeftImpl.changed       1000          1000    1000  thrpt   20    0,537 ± 0,001  ops/s
MultiplyLeftImpl.defined       1000          1000    1000  thrpt   20    0,716 ± 0,001  ops/s
 * </pre>
 *
 * <pre>
# Run complete. Total time: 00:12:07

Benchmark                 (columns)  (complexity)  (rows)   Mode  Cnt    Score   Error  Units
MultiplyLeftImpl.changed        100           100     100  thrpt   20  839,497 ± 0,835  ops/s
MultiplyLeftImpl.defined        100           100     100  thrpt   20  839,367 ± 1,182  ops/s

MultiplyLeftImpl.changed        100           100    1000  thrpt   20   88,250 ± 0,088  ops/s
MultiplyLeftImpl.defined        100           100    1000  thrpt   20   89,727 ± 0,048  ops/s

MultiplyLeftImpl.changed        100          1000     100  thrpt   20   58,897 ± 0,042  ops/s
MultiplyLeftImpl.defined        100          1000     100  thrpt   20   82,077 ± 0,086  ops/s

MultiplyLeftImpl.changed        100          1000    1000  thrpt   20    5,620 ± 0,002  ops/s
MultiplyLeftImpl.defined        100          1000    1000  thrpt   20    7,656 ± 0,004  ops/s

MultiplyLeftImpl.changed       1000           100     100  thrpt   20   58,988 ± 0,025  ops/s
MultiplyLeftImpl.defined       1000           100     100  thrpt   20   90,710 ± 0,044  ops/s

MultiplyLeftImpl.changed       1000           100    1000  thrpt   20    5,779 ± 0,003  ops/s
MultiplyLeftImpl.defined       1000           100    1000  thrpt   20    8,854 ± 0,007  ops/s

MultiplyLeftImpl.changed       1000          1000     100  thrpt   20    5,369 ± 0,006  ops/s
MultiplyLeftImpl.defined       1000          1000     100  thrpt   20    7,229 ± 0,010  ops/s

MultiplyLeftImpl.changed       1000          1000    1000  thrpt   20    0,539 ± 0,001  ops/s
MultiplyLeftImpl.defined       1000          1000    1000  thrpt   20    0,716 ± 0,001  ops/s
 * </pre>
 *
 * <pre>
 # Run complete. Total time: 00:12:05

Benchmark                 (columns)  (complexity)  (rows)   Mode  Cnt    Score    Error  Units
MultiplyLeftImpl.changed        100           100     100  thrpt   20  838,874 ±  0,572  ops/s
MultiplyLeftImpl.defined        100           100     100  thrpt   20  839,698 ±  0,665  ops/s

MultiplyLeftImpl.changed        100           100    1000  thrpt   20   88,011 ±  0,098  ops/s
MultiplyLeftImpl.defined        100           100    1000  thrpt   20   89,798 ±  0,101  ops/s

MultiplyLeftImpl.changed        100          1000     100  thrpt   20   58,911 ±  0,040  ops/s
MultiplyLeftImpl.defined        100          1000     100  thrpt   20   82,040 ±  0,061  ops/s

MultiplyLeftImpl.changed        100          1000    1000  thrpt   20    5,621 ±  0,003  ops/s
MultiplyLeftImpl.defined        100          1000    1000  thrpt   20    7,675 ±  0,005  ops/s

MultiplyLeftImpl.changed       1000           100     100  thrpt   20   58,822 ±  0,305  ops/s
MultiplyLeftImpl.defined       1000           100     100  thrpt   20   89,097 ±  0,046  ops/s

MultiplyLeftImpl.changed       1000           100    1000  thrpt   20    5,784 ±  0,005  ops/s
MultiplyLeftImpl.defined       1000           100    1000  thrpt   20    8,859 ±  0,005  ops/s

MultiplyLeftImpl.changed       1000          1000     100  thrpt   20    5,361 ±  0,010  ops/s
MultiplyLeftImpl.defined       1000          1000     100  thrpt   20    7,260 ±  0,013  ops/s

MultiplyLeftImpl.changed       1000          1000    1000  thrpt   20    0,537 ±  0,001  ops/s
MultiplyLeftImpl.defined       1000          1000    1000  thrpt   20    0,715 ±  0,002  ops/s
 * </pre>
 *
 * @formatter:on
 *
 *               <pre>
 * # Run complete. Total time: 00:12:06

Benchmark                 (columns)  (complexity)  (rows)   Mode  Cnt    Score   Error  Units
MultiplyLeftImpl.changed        100           100     100  thrpt   20  841,502 ± 0,641  ops/s
MultiplyLeftImpl.defined        100           100     100  thrpt   20  838,432 ± 0,623  ops/s

MultiplyLeftImpl.changed        100           100    1000  thrpt   20   84,927 ± 0,453  ops/s
MultiplyLeftImpl.defined        100           100    1000  thrpt   20   89,839 ± 0,055  ops/s

MultiplyLeftImpl.changed        100          1000     100  thrpt   20   59,059 ± 0,127  ops/s
MultiplyLeftImpl.defined        100          1000     100  thrpt   20   82,064 ± 0,064  ops/s

MultiplyLeftImpl.changed        100          1000    1000  thrpt   20    5,615 ± 0,003  ops/s
MultiplyLeftImpl.defined        100          1000    1000  thrpt   20    7,638 ± 0,004  ops/s

MultiplyLeftImpl.changed       1000           100     100  thrpt   20   58,810 ± 0,027  ops/s
MultiplyLeftImpl.defined       1000           100     100  thrpt   20   90,622 ± 0,044  ops/s

MultiplyLeftImpl.changed       1000           100    1000  thrpt   20    5,772 ± 0,003  ops/s
MultiplyLeftImpl.defined       1000           100    1000  thrpt   20    8,810 ± 0,004  ops/s

MultiplyLeftImpl.changed       1000          1000     100  thrpt   20    5,408 ± 0,006  ops/s
MultiplyLeftImpl.defined       1000          1000     100  thrpt   20    7,216 ± 0,016  ops/s

MultiplyLeftImpl.changed       1000          1000    1000  thrpt   20    0,537 ± 0,001  ops/s
MultiplyLeftImpl.defined       1000          1000    1000  thrpt   20    0,725 ± 0,001  ops/s
 *               </pre>
 *
 * @author apete
 */
@State(Scope.Benchmark)
public class MultiplyLeftImpl {

    public static void main(final String[] args) throws RunnerException {
        //final Options opt = new OptionsBuilder().include(".*" + BenchLeft.class.getSimpleName() + ".*").warmupTime(TimeValue.minutes(2L)).build();
        final Options opt = new OptionsBuilder().include(MultiplyLeftImpl.class.getSimpleName()).forks(1).build();
        new Runner(opt).run();
    }

    static void invoke1(final double[] product, final int firstRow, final int rowLimit, final Access1D<?> left, final int complexity, final double[] right) {

        final int tmpColDim = right.length / complexity;
        final int tmpRowDim = product.length / tmpColDim;

        final double[] tmpLeftRow = new double[complexity];

        for (int i = firstRow; i < rowLimit; i++) {

            for (int c = 0; c < complexity; c++) {
                tmpLeftRow[c] = left.doubleValue(i + (c * tmpRowDim));
            }

            for (int j = 0; j < tmpColDim; j++) {
                product[i + (j * tmpRowDim)] = DOT.invoke(tmpLeftRow, 0, right, j * complexity, 0, complexity);
            }
        }
    }

    static void invoke2(final double[] product, final int firstRow, final int rowLimit, final Access1D<?> left, final int complexity, final double[] right) {

        final int tmpColDim = right.length / complexity;
        final int tmpRowDim = product.length / tmpColDim;

        final double[] tmpLeftRow = new double[complexity];

        for (int i = firstRow; i < rowLimit; i++) {

            final int tmpFirst = MatrixUtils.firstInRow(left, i, 0);
            final int tmpLimit = MatrixUtils.limitOfRow(left, i, complexity);

            for (int c = tmpFirst; c < tmpLimit; c++) {
                tmpLeftRow[c] = left.doubleValue(i + (c * tmpRowDim));
            }

            for (int j = 0; j < tmpColDim; j++) {
                product[i + (j * tmpRowDim)] = DOT.invoke(tmpLeftRow, 0, right, j * complexity, tmpFirst, tmpLimit);
            }
        }
    }

    @Param({ "100", "1000" })
    public int columns;
    @Param({ "100", "1000" })
    public int complexity;
    public PrimitiveDenseStore left;
    public PrimitiveDenseStore product;
    public PrimitiveDenseStore right;
    @Param({ "100", "1000" })
    public int rows;

    @Benchmark
    public PrimitiveDenseStore changed() {
        MultiplyLeftImpl.invoke2(product.data, 0, rows, left, complexity, right.data);
        return product;
    };

    @Benchmark
    public PrimitiveDenseStore defined() {
        MultiplyLeftImpl.invoke1(product.data, 0, rows, left, complexity, right.data);
        return product;
    };

    @Setup
    public void setup() {
        left = PrimitiveDenseStore.FACTORY.makeFilled(rows, complexity, new Normal());
        right = PrimitiveDenseStore.FACTORY.makeFilled(complexity, columns, new Normal());
        product = PrimitiveDenseStore.FACTORY.makeFilled(rows, columns, new Normal());
    }
}
