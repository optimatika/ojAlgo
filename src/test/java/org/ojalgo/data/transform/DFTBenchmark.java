package org.ojalgo.data.transform;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * <pre>
Benchmark            (power)   Mode  Cnt           Score           Error    Units
DFTBenchmark.fft           0  thrpt    3  3124513790.555 ± 305627548.431  ops/min
DFTBenchmark.fft           1  thrpt    3  2398675511.894 ± 249205811.045  ops/min
DFTBenchmark.fft           2  thrpt    3  1718574817.845 ±   4230212.102  ops/min
DFTBenchmark.fft           3  thrpt    3   960108856.886 ±   1458412.598  ops/min
DFTBenchmark.fft           4  thrpt    3   491959971.868 ±   5637718.754  ops/min
DFTBenchmark.fft           5  thrpt    3   240706104.224 ±   1040731.406  ops/min
DFTBenchmark.fft           6  thrpt    3   111525835.267 ±    421865.235  ops/min
DFTBenchmark.fft           7  thrpt    3    51483058.691 ±    182468.393  ops/min
DFTBenchmark.fft           8  thrpt    3    23777894.972 ±     73809.368  ops/min
DFTBenchmark.matrix        0  thrpt    3  1576229925.659 ±  12786183.061  ops/min
DFTBenchmark.matrix        1  thrpt    3   982580395.880 ±   2184563.826  ops/min
DFTBenchmark.matrix        2  thrpt    3   535794233.758 ±   5384707.106  ops/min
DFTBenchmark.matrix        3  thrpt    3   185259013.686 ±   2036997.500  ops/min
DFTBenchmark.matrix        4  thrpt    3    58275950.781 ±    585827.402  ops/min
DFTBenchmark.matrix        5  thrpt    3    15247998.933 ±    142923.255  ops/min
DFTBenchmark.matrix        6  thrpt    3     4214174.031 ±    173260.531  ops/min
DFTBenchmark.matrix        7  thrpt    3     1071304.704 ±     26403.251  ops/min
DFTBenchmark.matrix        8  thrpt    3      262046.213 ±      4266.517  ops/min
 * </pre>
 */
@State(Scope.Benchmark)
public class DFTBenchmark {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(DFTBenchmark.class);
    }

    @Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8" })
    public int power;

    DiscreteFourierTransform.FFT fft;
    R064Store input;
    DiscreteFourierTransform.FullMatrix matrix;

    @Benchmark
    public MatrixStore<ComplexNumber> fft() {
        return fft.transform(input.data);
    }

    @Benchmark
    public MatrixStore<ComplexNumber> matrix() {
        return matrix.transform(input.data);
    }

    @Setup
    public void setup() {
        int size = PowerOf2.powerOfInt2(power);
        input = R064Store.FACTORY.makeFilled(size, 1, Uniform.of(-2, 4));
        fft = new DiscreteFourierTransform.FFT(input.size());
        matrix = new DiscreteFourierTransform.FullMatrix(input.size());

    }

}
