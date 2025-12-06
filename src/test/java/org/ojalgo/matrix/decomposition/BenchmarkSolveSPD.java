package org.ojalgo.matrix.decomposition;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Benchmarks alternative solvers for the quadratic term Q in convex QP.
 * <p>
 * Measures both the cost of the initial factorisation/decomposition of a dense SPD matrix Q and the cost of
 * repeated solves against that factorised system.
 * <p>
 * MacBook Air M4 2025: 2025-12-05
 *
 * <pre>
Benchmark                  (dim)        (solver)   Mode  Cnt         Score         Error  Units
BenchmarkSolveSPD.compute     10        QR_DENSE  thrpt    3   2288400.041 ±   43142.380  ops/s
BenchmarkSolveSPD.compute     10          QR_RAW  thrpt    3   1081400.868 ±   42021.614  ops/s
BenchmarkSolveSPD.compute     10        LU_DENSE  thrpt    3   4076567.205 ±   75291.583  ops/s
BenchmarkSolveSPD.compute     10          LU_RAW  thrpt    3   2150282.263 ±   48354.543  ops/s
BenchmarkSolveSPD.compute     10       LU_SPARSE  thrpt    3    543505.903 ±    2783.751  ops/s
BenchmarkSolveSPD.compute     10  CHOLESKY_DENSE  thrpt    3   4689272.640 ±  115782.518  ops/s
BenchmarkSolveSPD.compute     10    CHOLESKY_RAW  thrpt    3   2517705.381 ±   59616.637  ops/s
BenchmarkSolveSPD.compute     10       LDL_DENSE  thrpt    3   3646795.436 ±   59501.937  ops/s
BenchmarkSolveSPD.compute     10      LDL_SPARSE  thrpt    3    514488.169 ±   24975.800  ops/s
BenchmarkSolveSPD.compute     20        QR_DENSE  thrpt    3    512691.722 ±    4027.501  ops/s
BenchmarkSolveSPD.compute     20          QR_RAW  thrpt    3    300041.774 ±   11250.922  ops/s
BenchmarkSolveSPD.compute     20        LU_DENSE  thrpt    3    901400.199 ±   26116.959  ops/s
BenchmarkSolveSPD.compute     20          LU_RAW  thrpt    3    700161.117 ±   23971.981  ops/s
BenchmarkSolveSPD.compute     20       LU_SPARSE  thrpt    3    119829.297 ±    1559.575  ops/s
BenchmarkSolveSPD.compute     20  CHOLESKY_DENSE  thrpt    3   1380337.888 ±    9875.263  ops/s
BenchmarkSolveSPD.compute     20    CHOLESKY_RAW  thrpt    3    784157.558 ±   60964.608  ops/s
BenchmarkSolveSPD.compute     20       LDL_DENSE  thrpt    3    927877.566 ±   24666.095  ops/s
BenchmarkSolveSPD.compute     20      LDL_SPARSE  thrpt    3    142664.716 ±    3395.957  ops/s
BenchmarkSolveSPD.compute     50        QR_DENSE  thrpt    3     46357.376 ±    1071.759  ops/s
BenchmarkSolveSPD.compute     50          QR_RAW  thrpt    3     38467.844 ±     207.556  ops/s
BenchmarkSolveSPD.compute     50        LU_DENSE  thrpt    3     93026.496 ±    1290.770  ops/s
BenchmarkSolveSPD.compute     50          LU_RAW  thrpt    3    111894.338 ±     383.096  ops/s
BenchmarkSolveSPD.compute     50       LU_SPARSE  thrpt    3     19598.144 ±      56.261  ops/s
BenchmarkSolveSPD.compute     50  CHOLESKY_DENSE  thrpt    3    144523.807 ±   13320.397  ops/s
BenchmarkSolveSPD.compute     50    CHOLESKY_RAW  thrpt    3    125383.077 ±    2629.272  ops/s
BenchmarkSolveSPD.compute     50       LDL_DENSE  thrpt    3    117159.470 ±   62363.073  ops/s
BenchmarkSolveSPD.compute     50      LDL_SPARSE  thrpt    3     23610.945 ±     440.535  ops/s
BenchmarkSolveSPD.compute    100        QR_DENSE  thrpt    3      6768.249 ±      32.222  ops/s
BenchmarkSolveSPD.compute    100          QR_RAW  thrpt    3      6724.747 ±      23.797  ops/s
BenchmarkSolveSPD.compute    100        LU_DENSE  thrpt    3     13647.807 ±     403.831  ops/s
BenchmarkSolveSPD.compute    100          LU_RAW  thrpt    3     19799.119 ±     495.277  ops/s
BenchmarkSolveSPD.compute    100       LU_SPARSE  thrpt    3      4262.830 ±      17.624  ops/s
BenchmarkSolveSPD.compute    100  CHOLESKY_DENSE  thrpt    3     24086.125 ±     406.343  ops/s
BenchmarkSolveSPD.compute    100    CHOLESKY_RAW  thrpt    3     23554.252 ±     115.965  ops/s
BenchmarkSolveSPD.compute    100       LDL_DENSE  thrpt    3     20746.164 ±     123.772  ops/s
BenchmarkSolveSPD.compute    100      LDL_SPARSE  thrpt    3      5288.126 ±      22.308  ops/s
BenchmarkSolveSPD.compute    200        QR_DENSE  thrpt    3       770.893 ±      26.703  ops/s
BenchmarkSolveSPD.compute    200          QR_RAW  thrpt    3       909.371 ±      49.932  ops/s
BenchmarkSolveSPD.compute    200        LU_DENSE  thrpt    3      1490.238 ±      41.678  ops/s
BenchmarkSolveSPD.compute    200          LU_RAW  thrpt    3      2264.330 ±     100.544  ops/s
BenchmarkSolveSPD.compute    200       LU_SPARSE  thrpt    3       666.624 ±       1.629  ops/s
BenchmarkSolveSPD.compute    200  CHOLESKY_DENSE  thrpt    3      1294.435 ±      19.718  ops/s
BenchmarkSolveSPD.compute    200    CHOLESKY_RAW  thrpt    3      3525.919 ±     151.494  ops/s
BenchmarkSolveSPD.compute    200       LDL_DENSE  thrpt    3      2817.959 ±     292.765  ops/s
BenchmarkSolveSPD.compute    200      LDL_SPARSE  thrpt    3       998.092 ±       3.725  ops/s
BenchmarkSolveSPD.compute    500        QR_DENSE  thrpt    3        84.738 ±       2.650  ops/s
BenchmarkSolveSPD.compute    500          QR_RAW  thrpt    3        91.553 ±      16.255  ops/s
BenchmarkSolveSPD.compute    500        LU_DENSE  thrpt    3       120.182 ±       4.155  ops/s
BenchmarkSolveSPD.compute    500          LU_RAW  thrpt    3       139.832 ±       7.358  ops/s
BenchmarkSolveSPD.compute    500       LU_SPARSE  thrpt    3        45.085 ±       0.327  ops/s
BenchmarkSolveSPD.compute    500  CHOLESKY_DENSE  thrpt    3       129.855 ±       6.294  ops/s
BenchmarkSolveSPD.compute    500    CHOLESKY_RAW  thrpt    3       191.251 ±       8.242  ops/s
BenchmarkSolveSPD.compute    500       LDL_DENSE  thrpt    3       149.296 ±      12.180  ops/s
BenchmarkSolveSPD.compute    500      LDL_SPARSE  thrpt    3        85.554 ±       4.110  ops/s
BenchmarkSolveSPD.compute   1000        QR_DENSE  thrpt    3        14.669 ±       1.993  ops/s
BenchmarkSolveSPD.compute   1000          QR_RAW  thrpt    3        16.751 ±       3.783  ops/s
BenchmarkSolveSPD.compute   1000        LU_DENSE  thrpt    3        22.646 ±       1.117  ops/s
BenchmarkSolveSPD.compute   1000          LU_RAW  thrpt    3        17.993 ±       3.097  ops/s
BenchmarkSolveSPD.compute   1000       LU_SPARSE  thrpt    3         5.243 ±       0.642  ops/s
BenchmarkSolveSPD.compute   1000  CHOLESKY_DENSE  thrpt    3        29.693 ±       4.117  ops/s
BenchmarkSolveSPD.compute   1000    CHOLESKY_RAW  thrpt    3        26.285 ±       0.813  ops/s
BenchmarkSolveSPD.compute   1000       LDL_DENSE  thrpt    3        28.666 ±       1.444  ops/s
BenchmarkSolveSPD.compute   1000      LDL_SPARSE  thrpt    3        11.228 ±       1.393  ops/s
BenchmarkSolveSPD.solve       10        QR_DENSE  thrpt    3   3485680.890 ±   58664.671  ops/s
BenchmarkSolveSPD.solve       10          QR_RAW  thrpt    3   6152412.543 ±   90069.660  ops/s
BenchmarkSolveSPD.solve       10        LU_DENSE  thrpt    3   7089736.401 ±  132918.929  ops/s
BenchmarkSolveSPD.solve       10          LU_RAW  thrpt    3   7317661.590 ±  202114.461  ops/s
BenchmarkSolveSPD.solve       10       LU_SPARSE  thrpt    3   9096972.116 ±  562050.111  ops/s
BenchmarkSolveSPD.solve       10  CHOLESKY_DENSE  thrpt    3   6736786.191 ±  462723.567  ops/s
BenchmarkSolveSPD.solve       10    CHOLESKY_RAW  thrpt    3   6713270.072 ±   76843.888  ops/s
BenchmarkSolveSPD.solve       10       LDL_DENSE  thrpt    3   5082826.521 ± 1460118.434  ops/s
BenchmarkSolveSPD.solve       10      LDL_SPARSE  thrpt    3  10057427.940 ±  264257.235  ops/s
BenchmarkSolveSPD.solve       20        QR_DENSE  thrpt    3   1250616.765 ±   45892.026  ops/s
BenchmarkSolveSPD.solve       20          QR_RAW  thrpt    3   2808064.344 ±   13454.020  ops/s
BenchmarkSolveSPD.solve       20        LU_DENSE  thrpt    3   2606043.065 ±    8475.338  ops/s
BenchmarkSolveSPD.solve       20          LU_RAW  thrpt    3   2806037.941 ±   77619.941  ops/s
BenchmarkSolveSPD.solve       20       LU_SPARSE  thrpt    3   3272944.868 ±  471629.680  ops/s
BenchmarkSolveSPD.solve       20  CHOLESKY_DENSE  thrpt    3   2541920.314 ±   17506.906  ops/s
BenchmarkSolveSPD.solve       20    CHOLESKY_RAW  thrpt    3   2734984.104 ±   10352.567  ops/s
BenchmarkSolveSPD.solve       20       LDL_DENSE  thrpt    3   2215142.009 ±   39428.472  ops/s
BenchmarkSolveSPD.solve       20      LDL_SPARSE  thrpt    3   3519597.249 ±   96279.546  ops/s
BenchmarkSolveSPD.solve       50        QR_DENSE  thrpt    3    328043.834 ±    5017.263  ops/s
BenchmarkSolveSPD.solve       50          QR_RAW  thrpt    3    852276.540 ±    5566.032  ops/s
BenchmarkSolveSPD.solve       50        LU_DENSE  thrpt    3    563526.246 ±     369.517  ops/s
BenchmarkSolveSPD.solve       50          LU_RAW  thrpt    3    668905.559 ±   41467.671  ops/s
BenchmarkSolveSPD.solve       50       LU_SPARSE  thrpt    3    689873.012 ±   28414.171  ops/s
BenchmarkSolveSPD.solve       50  CHOLESKY_DENSE  thrpt    3    554509.631 ±    1640.406  ops/s
BenchmarkSolveSPD.solve       50    CHOLESKY_RAW  thrpt    3    667080.147 ±   17943.405  ops/s
BenchmarkSolveSPD.solve       50       LDL_DENSE  thrpt    3    512246.617 ±   10107.550  ops/s
BenchmarkSolveSPD.solve       50      LDL_SPARSE  thrpt    3    745515.036 ±   13759.582  ops/s
BenchmarkSolveSPD.solve      100        QR_DENSE  thrpt    3     91386.329 ±    1717.281  ops/s
BenchmarkSolveSPD.solve      100          QR_RAW  thrpt    3    267809.191 ±    4089.199  ops/s
BenchmarkSolveSPD.solve      100        LU_DENSE  thrpt    3    140687.862 ±     326.184  ops/s
BenchmarkSolveSPD.solve      100          LU_RAW  thrpt    3    188375.908 ±    6645.472  ops/s
BenchmarkSolveSPD.solve      100       LU_SPARSE  thrpt    3    174431.093 ±   48246.438  ops/s
BenchmarkSolveSPD.solve      100  CHOLESKY_DENSE  thrpt    3    141778.865 ±     757.531  ops/s
BenchmarkSolveSPD.solve      100    CHOLESKY_RAW  thrpt    3    175692.925 ±      66.047  ops/s
BenchmarkSolveSPD.solve      100       LDL_DENSE  thrpt    3    131176.538 ±     112.006  ops/s
BenchmarkSolveSPD.solve      100      LDL_SPARSE  thrpt    3    194545.957 ±     246.444  ops/s
BenchmarkSolveSPD.solve      200        QR_DENSE  thrpt    3     21928.961 ±     542.792  ops/s
BenchmarkSolveSPD.solve      200          QR_RAW  thrpt    3     66761.665 ±   23948.580  ops/s
BenchmarkSolveSPD.solve      200        LU_DENSE  thrpt    3     30051.282 ±     504.356  ops/s
BenchmarkSolveSPD.solve      200          LU_RAW  thrpt    3     44648.622 ±     698.933  ops/s
BenchmarkSolveSPD.solve      200       LU_SPARSE  thrpt    3     44595.655 ±    2721.136  ops/s
BenchmarkSolveSPD.solve      200  CHOLESKY_DENSE  thrpt    3     32609.364 ±    2464.262  ops/s
BenchmarkSolveSPD.solve      200    CHOLESKY_RAW  thrpt    3     39784.651 ±    3829.762  ops/s
BenchmarkSolveSPD.solve      200       LDL_DENSE  thrpt    3     31485.834 ±    1871.555  ops/s
BenchmarkSolveSPD.solve      200      LDL_SPARSE  thrpt    3     50562.877 ±    1795.307  ops/s
BenchmarkSolveSPD.solve      500        QR_DENSE  thrpt    3      3304.818 ±     129.971  ops/s
BenchmarkSolveSPD.solve      500          QR_RAW  thrpt    3     10193.736 ±     563.953  ops/s
BenchmarkSolveSPD.solve      500        LU_DENSE  thrpt    3      4255.276 ±       6.795  ops/s
BenchmarkSolveSPD.solve      500          LU_RAW  thrpt    3      6313.562 ±      41.105  ops/s
BenchmarkSolveSPD.solve      500       LU_SPARSE  thrpt    3      6824.551 ±     348.952  ops/s
BenchmarkSolveSPD.solve      500  CHOLESKY_DENSE  thrpt    3      4731.394 ±    1557.976  ops/s
BenchmarkSolveSPD.solve      500    CHOLESKY_RAW  thrpt    3      5280.463 ±    1010.445  ops/s
BenchmarkSolveSPD.solve      500       LDL_DENSE  thrpt    3      4750.383 ±     100.575  ops/s
BenchmarkSolveSPD.solve      500      LDL_SPARSE  thrpt    3      8234.608 ±     866.655  ops/s
BenchmarkSolveSPD.solve     1000        QR_DENSE  thrpt    3       792.036 ±      43.042  ops/s
BenchmarkSolveSPD.solve     1000          QR_RAW  thrpt    3      2711.904 ±     371.029  ops/s
BenchmarkSolveSPD.solve     1000        LU_DENSE  thrpt    3      1015.605 ±       9.945  ops/s
BenchmarkSolveSPD.solve     1000          LU_RAW  thrpt    3      1488.554 ±      15.125  ops/s
BenchmarkSolveSPD.solve     1000       LU_SPARSE  thrpt    3      1628.078 ±      38.245  ops/s
BenchmarkSolveSPD.solve     1000  CHOLESKY_DENSE  thrpt    3      1158.019 ±      11.072  ops/s
BenchmarkSolveSPD.solve     1000    CHOLESKY_RAW  thrpt    3      1152.727 ±      57.617  ops/s
BenchmarkSolveSPD.solve     1000       LDL_DENSE  thrpt    3      1148.822 ±       9.554  ops/s
BenchmarkSolveSPD.solve     1000      LDL_SPARSE  thrpt    3      2101.024 ±       3.515  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BenchmarkSolveSPD {

    public enum SolverKind {

        CHOLESKY_DENSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new DenseCholesky.R064();
            }

        },

        CHOLESKY_RAW {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new RawCholesky();
            }

        },

        LDL_DENSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new DenseLDL.R064();
            }

        },

        LDL_SPARSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new SparseQDLDL();
            }

        },

        LU_DENSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new DenseLU.R064();
            }

        },

        LU_RAW {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new RawLU();
            }

        },

        LU_SPARSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new SparseLU();
            }

        },

        QR_DENSE {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new DenseQR.R064();
            }

        },

        QR_RAW {

            @Override
            MatrixDecomposition.Solver<Double> newSolver() {
                return new RawQR();
            }

        };

        abstract MatrixDecomposition.Solver<Double> newSolver();

    }

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkSolveSPD.class);
    }

    @Param({ "10", "20", "50", "100", "200", "500", "1000" })
    public int dim;

    @Param({ "QR_DENSE", "QR_RAW", "LU_DENSE", "LU_RAW", "LU_SPARSE", "CHOLESKY_DENSE", "CHOLESKY_RAW", "LDL_DENSE", "LDL_SPARSE" })
    public String solver;

    private MatrixDecomposition.Solver<Double> instance;
    private R064Store matrix;
    private R064Store rhs;

    /**
     * Benchmark the cost of constructing and factorising the solver for Q.
     */
    @Benchmark
    public boolean compute() {
        instance = SolverKind.valueOf(solver).newSolver();
        return instance.compute(matrix);
    }

    @Setup(Level.Trial)
    public void setup() {

        matrix = R064Store.FACTORY.makeSPD(dim);

        rhs = R064Store.FACTORY.makeFilled(dim, 1, new Uniform());

        instance = SolverKind.valueOf(solver).newSolver();
        instance.compute(matrix);
    }

    /**
     * Benchmark the cost of solving Q x = b using an already factorised solver.
     */
    @Benchmark
    public MatrixStore<Double> solve() {
        return instance.getSolution(rhs);
    }

}
