package org.ojalgo;

import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class OrderMatrixMultiplication {

    static int DIM = 30;

    public static void main(final String[] args) throws RunnerException {
        //        final Options opt = new OptionsBuilder().include(".*" + TestMatrixMultiplication.class.getSimpleName() + ".*").forks(1).warmupIterations(9)
        //                .measurementIterations(5).measurementTime(TimeValue.minutes(1)).timeUnit(TimeUnit.MINUTES).build();
        final Options opt = new OptionsBuilder().include(".*" + OrderMatrixMultiplication.class.getSimpleName() + ".*").forks(3).warmupIterations(9)
                .measurementIterations(9).build();
        new Runner(opt).run();
    }

    double[][] left;
    double[][] prod;
    double[][] right;

    @Benchmark
    public double[][] faster() {

        double[] tmpProdI;
        double[] tmpRightC;
        double tmpLeftIC;

        for (int i = 0; i < DIM; i++) {
            tmpProdI = prod[i];

            for (int c = 0; c < DIM; c++) {
                tmpRightC = right[c];

                tmpLeftIC = left[i][c];
                for (int j = 0; j < DIM; j++) {
                    tmpProdI[j] += tmpLeftIC * tmpRightC[j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopCIJ() {

        for (int c = 0; c < DIM; c++) {
            for (int i = 0; i < DIM; i++) {
                for (int j = 0; j < DIM; j++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopCJI() {

        for (int c = 0; c < DIM; c++) {
            for (int j = 0; j < DIM; j++) {
                for (int i = 0; i < DIM; i++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopICJ() {

        for (int i = 0; i < DIM; i++) {
            for (int c = 0; c < DIM; c++) {
                for (int j = 0; j < DIM; j++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopIJC() {

        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                for (int c = 0; c < DIM; c++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopJCI() {

        for (int j = 0; j < DIM; j++) {
            for (int c = 0; c < DIM; c++) {
                for (int i = 0; i < DIM; i++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    }

    @Benchmark
    public double[][] loopJIC() {

        for (int j = 0; j < DIM; j++) {
            for (int i = 0; i < DIM; i++) {
                for (int c = 0; c < DIM; c++) {
                    prod[i][j] += left[i][c] * right[c][j];
                }
            }
        }

        return prod;
    };

    @Setup
    public void setup() {
        left = RawStore.FACTORY.makeFilled(DIM, DIM, new Uniform()).data;
        right = RawStore.FACTORY.makeFilled(DIM, DIM, new Uniform()).data;
        prod = new double[DIM][DIM];
    }

}
