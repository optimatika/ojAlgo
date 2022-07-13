package org.ojalgo.matrix.operation;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

@State(Scope.Benchmark)
public class VectorisedMultiplication {

    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().warmupIterations(10).jvmArgsAppend("--enable-preview", "--add-modules", "jdk.incubator.vector"),
                VectorisedMultiplication.class);
    }

    double[] left;
    int m, complexity, n;
    double[] product;
    double[] right;

    @Benchmark
    public double[] existing() {

        MultiplyNeither.fillMxN(product, left, complexity, right);

        return product;
    }

    @Setup
    public void setup() {

        int dim = 2048;

        m = dim;
        complexity = dim;
        n = dim;

        ArrayR064 tmpLeft = ArrayR064.make(m * complexity);
        tmpLeft.fillAll(Uniform.standard());
        left = tmpLeft.data;

        ArrayR064 tmpRight = ArrayR064.make(complexity * n);
        tmpRight.fillAll(Uniform.standard());
        right = tmpRight.data;

        product = new double[m * n];
    }

    public double[] test() {

        for (int j = 0; j < n; j++) {
            for (int c = 0; c < complexity; c++) {
                for (int i = 0; i < m; i++) {
                    product[i + j * m] = Math.fma(left[i + c * m], right[c + j * complexity], product[i + j * m]);
                }
            }
        }

        return product;
    }

    @Benchmark
    public double[] vector() {

        int bound = SPECIES.loopBound(m);
        int length = SPECIES.length();

        for (int j = 0; j < n; j++) {

            for (int i = 0; i < m; i += length) {

                final int i1 = i;
                final int j1 = j;
                DoubleVector pv = DoubleVector.zero(SPECIES);

                for (int c = 0; c < complexity; c++) {

                    DoubleVector lv = DoubleVector.fromArray(SPECIES, left, i1 + c * m);
                    DoubleVector rv = DoubleVector.broadcast(SPECIES, right[c + j1 * complexity]);

                    pv = lv.lanewise(VectorOperators.FMA, rv, pv);

                }

                pv.intoArray(product, i1 + j1 * m);
            }
        }

        return product;
    }

    @Benchmark
    public double[] vector33() {

        int bound = SPECIES.loopBound(m);
        int length = SPECIES.length();

        int[] map = new int[length];
        for (int i = 0; i < map.length; i++) {
            map[i] = i * m;
        }

        for (int j = 0; j < n; j += length) {
            for (int i = 0; i < m; i++) {

                DoubleVector pv = DoubleVector.zero(SPECIES);

                for (int c = 0; c < complexity; c++) {

                    DoubleVector lv = DoubleVector.broadcast(SPECIES, left[i + c * m]);

                    DoubleVector rv = DoubleVector.fromArray(SPECIES, right, c + j * complexity, map, 0);

                    pv = lv.lanewise(VectorOperators.FMA, rv, pv);

                }

                pv.intoArray(product, i + j * m, map, 0);
            }
        }

        return product;
    }

}
