package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.DensityTrackingArray;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.PhysicalStore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * JMH benchmark comparing {@link SparseLU} and {@link MarkowitzLU} on decomposition, FTRAN, and BTRAN.
 * <p>
 * SparseLU uses dense {@link PhysicalStore} vectors for solve operations while MarkowitzLU uses
 * {@link DensityTrackingArray} with density-aware hyper-sparse paths.
 *
 * <pre>
Benchmark                                         (density)  (dim)   Mode  Cnt       Score       Error  Units
BenchmarkMarkowitzVsSparseLU.markowitz_btran          0.005    500  thrpt    3  163085.486 ±  7897.245  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran          0.005   1000  thrpt    3   20394.441 ±   126.657  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran          0.005   2000  thrpt    3    1418.703 ±   224.932  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.01    500  thrpt    3   53081.563 ±  4137.498  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.01   1000  thrpt    3    5426.475 ±   134.900  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.01   2000  thrpt    3     701.804 ±    75.076  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.02    500  thrpt    3   18998.919 ±   800.234  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.02   1000  thrpt    3    2616.856 ±   172.218  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_btran           0.02   2000  thrpt    3     465.041 ±    95.652  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose      0.005    500  thrpt    3    8891.703 ±   622.950  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose      0.005   1000  thrpt    3      12.846 ±     0.518  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose      0.005   2000  thrpt    3       0.205 ±     0.045  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.01    500  thrpt    3      93.694 ±     5.207  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.01   1000  thrpt    3       1.506 ±     0.221  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.01   2000  thrpt    3       0.081 ±     0.009  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.02    500  thrpt    3      10.110 ±     0.981  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.02   1000  thrpt    3       0.560 ±     0.055  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_decompose       0.02   2000  thrpt    3       0.040 ±     0.013  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran          0.005    500  thrpt    3  157489.042 ±  6313.813  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran          0.005   1000  thrpt    3   14321.760 ±  1857.936  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran          0.005   2000  thrpt    3    1028.113 ±   280.843  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.01    500  thrpt    3   45568.229 ±  6012.903  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.01   1000  thrpt    3    3925.152 ±   303.583  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.01   2000  thrpt    3     482.028 ±    66.798  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.02    500  thrpt    3   14356.591 ±   638.246  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.02   1000  thrpt    3    1959.095 ±    71.578  ops/s
BenchmarkMarkowitzVsSparseLU.markowitz_ftran           0.02   2000  thrpt    3     305.428 ±    84.513  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran             0.005    500  thrpt    3   67759.940 ± 18319.674  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran             0.005   1000  thrpt    3    3665.387 ±   237.820  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran             0.005   2000  thrpt    3     472.170 ±   344.204  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.01    500  thrpt    3   14306.646 ±   543.222  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.01   1000  thrpt    3    2124.530 ±   336.115  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.01   2000  thrpt    3     391.812 ±    99.380  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.02    500  thrpt    3    8223.109 ±  1084.026  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.02   1000  thrpt    3    1645.243 ±   231.773  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_btran              0.02   2000  thrpt    3     350.123 ±    12.697  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose         0.005    500  thrpt    3     242.358 ±    49.681  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose         0.005   1000  thrpt    3       8.849 ±     1.545  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose         0.005   2000  thrpt    3       0.395 ±     0.085  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.01    500  thrpt    3      64.995 ±    17.741  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.01   1000  thrpt    3       3.694 ±    10.513  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.01   2000  thrpt    3       0.297 ±     0.263  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.02    500  thrpt    3      18.979 ±    37.949  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.02   1000  thrpt    3       2.400 ±     2.965  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_decompose          0.02   2000  thrpt    3       0.242 ±     0.074  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran             0.005    500  thrpt    3   72766.053 ±  2788.452  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran             0.005   1000  thrpt    3    2262.922 ±    33.647  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran             0.005   2000  thrpt    3     282.440 ±     2.166  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.01    500  thrpt    3    8273.617 ±   976.728  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.01   1000  thrpt    3    1077.562 ±    70.747  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.01   2000  thrpt    3     225.986 ±    24.928  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.02    500  thrpt    3    5310.887 ±   381.211  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.02   1000  thrpt    3     882.395 ±    46.034  ops/s
BenchmarkMarkowitzVsSparseLU.sparse_ftran              0.02   2000  thrpt    3     191.600 ±     8.902  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkMarkowitzVsSparseLU {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkMarkowitzVsSparseLU.class);
    }

    @Param({ "0.005", "0.01", "0.02" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;

    ColumnsSupplier<Double> matrix;

    final SparseLU sparseLU = new SparseLU();
    MarkowitzLU markowitzLU;

    PhysicalStore<Double> denseVector;
    double[] denseArray;

    @Benchmark
    public DensityTrackingArray markowitz_btran() {
        DensityTrackingArray rhs = DensityTrackingArray.wrap(denseArray.clone());
        markowitzLU.btran(rhs);
        return rhs;
    }

    @Benchmark
    public MarkowitzLU markowitz_decompose() {
        markowitzLU.build(matrix);
        return markowitzLU;
    }

    @Benchmark
    public DensityTrackingArray markowitz_ftran() {
        DensityTrackingArray rhs = DensityTrackingArray.wrap(denseArray.clone());
        markowitzLU.ftran(rhs);
        return rhs;
    }

    @Setup(Level.Trial)
    public void setup() {
        matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);
        markowitzLU = new MarkowitzLU(dim);

        sparseLU.decompose(matrix);
        markowitzLU.build(matrix);

        denseVector = AbstractBenchmarkSparseLU.newDenseVector(dim);

        denseArray = new double[dim];
        for (int i = 0; i < dim; i++) {
            denseArray[i] = denseVector.doubleValue(i);
        }
    }

    @Benchmark
    public PhysicalStore<Double> sparse_btran() {
        sparseLU.btran(denseVector);
        return denseVector;
    }

    @Benchmark
    public SparseLU sparse_decompose() {
        sparseLU.decompose(matrix);
        return sparseLU;
    }

    @Benchmark
    public PhysicalStore<Double> sparse_ftran() {
        sparseLU.ftran(denseVector);
        return denseVector;
    }
}
