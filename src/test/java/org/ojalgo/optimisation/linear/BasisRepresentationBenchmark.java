package org.ojalgo.optimisation.linear;

import java.util.concurrent.TimeUnit;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.matrix.decomposition.AbstractBenchmarkSparseLU;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * Phase B benchmark: measures ftran + btran solve cost after K incremental updates for ProductFormInverse and
 * SparseDecomposition. The setup phase performs reset + K updates (untimed); the benchmark method times a
 * single ftran + btran pair. This isolates the per-solve cost at various eta-chain lengths and reveals how
 * fast PFI/Sparse solve cost grows with the number of accumulated updates.
 * <p>
 * Parameters: updates K ∈ {0, 10, 50, 100, 200}, density ∈ {0.01, 0.1, 0.5}, dim ∈ {500, 1000, 2000}. K=0
 * gives the baseline solve cost (root factorisation only, no eta).
 * <p>
 * MacBook Air M4: 2026-04-17
 *
 * <pre>
Benchmark                                  (density)  (dim)  (updates)   Mode  Cnt      Score       Error  Units
BasisRepresentationBenchmark.solveMarkow        0.01    500          0  thrpt    3  27551.959 ±  1501.970  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01    500         10  thrpt    3  23585.183 ±  1201.790  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01    500         50  thrpt    3  16278.279 ±  1608.047  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01    500        100  thrpt    3  13444.871 ±  1734.962  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01    500        200  thrpt    3  16258.500 ±   668.014  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   1000          0  thrpt    3   2186.645 ±    95.958  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   1000         10  thrpt    3   2144.251 ±   124.155  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   1000         50  thrpt    3   1984.086 ±   234.087  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   1000        100  thrpt    3   1678.351 ±   204.938  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   1000        200  thrpt    3   1415.869 ±    30.223  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   2000          0  thrpt    3    277.652 ±    91.745  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   2000         10  thrpt    3    268.791 ±    32.712  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   2000         50  thrpt    3    267.145 ±    36.798  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   2000        100  thrpt    3    247.001 ±    46.290  ops/s
BasisRepresentationBenchmark.solveMarkow        0.01   2000        200  thrpt    3    230.870 ±    14.389  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1    500          0  thrpt    3   2607.084 ±    40.347  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1    500         10  thrpt    3   2579.432 ±   223.838  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1    500         50  thrpt    3   2366.464 ±   147.946  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1    500        100  thrpt    3   2194.725 ±    71.823  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1    500        200  thrpt    3   1936.429 ±    27.187  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   1000          0  thrpt    3    574.627 ±    38.495  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   1000         10  thrpt    3    531.423 ±    63.379  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   1000         50  thrpt    3    538.702 ±   112.959  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   1000        100  thrpt    3    518.063 ±    73.833  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   1000        200  thrpt    3    479.405 ±    48.971  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   2000          0  thrpt    3    124.194 ±     8.587  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   2000         10  thrpt    3    129.321 ±    20.390  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   2000         50  thrpt    3    128.918 ±     8.197  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   2000        100  thrpt    3    125.252 ±     7.255  ops/s
BasisRepresentationBenchmark.solveMarkow         0.1   2000        200  thrpt    3    118.136 ±    13.453  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5    500          0  thrpt    3   2030.337 ±    63.521  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5    500         10  thrpt    3   1877.603 ±  1019.867  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5    500         50  thrpt    3   1854.611 ±   297.221  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5    500        100  thrpt    3   1725.174 ±    81.686  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5    500        200  thrpt    3   1418.689 ±   684.524  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   1000          0  thrpt    3    485.192 ±   113.381  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   1000         10  thrpt    3    474.323 ±    52.579  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   1000         50  thrpt    3    481.715 ±     7.597  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   1000        100  thrpt    3    443.031 ±    39.568  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   1000        200  thrpt    3    418.575 ±    36.851  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   2000          0  thrpt    3    119.814 ±    19.157  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   2000         10  thrpt    3    121.143 ±     6.166  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   2000         50  thrpt    3    113.818 ±    51.389  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   2000        100  thrpt    3    111.751 ±    38.477  ops/s
BasisRepresentationBenchmark.solveMarkow         0.5   2000        200  thrpt    3    104.641 ±    22.092  ops/s
BasisRepresentationBenchmark.solveProduct       0.01    500          0  thrpt    3   5035.800 ±   185.915  ops/s
BasisRepresentationBenchmark.solveProduct       0.01    500         10  thrpt    3   4856.755 ±   657.318  ops/s
BasisRepresentationBenchmark.solveProduct       0.01    500         50  thrpt    3   4146.071 ±  1344.176  ops/s
BasisRepresentationBenchmark.solveProduct       0.01    500        100  thrpt    3   3608.655 ±   269.159  ops/s
BasisRepresentationBenchmark.solveProduct       0.01    500        200  thrpt    3   3610.419 ±   440.501  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   1000          0  thrpt    3    448.884 ±     7.601  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   1000         10  thrpt    3    439.699 ±   176.669  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   1000         50  thrpt    3    428.559 ±    18.166  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   1000        100  thrpt    3    415.169 ±     8.332  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   1000        200  thrpt    3    414.361 ±    27.132  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   2000          0  thrpt    3     94.169 ±     2.537  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   2000         10  thrpt    3     94.352 ±     2.687  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   2000         50  thrpt    3     84.292 ±    56.133  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   2000        100  thrpt    3     83.689 ±   111.460  ops/s
BasisRepresentationBenchmark.solveProduct       0.01   2000        200  thrpt    3     87.327 ±    15.463  ops/s
BasisRepresentationBenchmark.solveProduct        0.1    500          0  thrpt    3   5037.636 ±   877.342  ops/s
BasisRepresentationBenchmark.solveProduct        0.1    500         10  thrpt    3   4888.886 ±   409.858  ops/s
BasisRepresentationBenchmark.solveProduct        0.1    500         50  thrpt    3   4229.666 ±   487.525  ops/s
BasisRepresentationBenchmark.solveProduct        0.1    500        100  thrpt    3   3574.136 ±  1098.068  ops/s
BasisRepresentationBenchmark.solveProduct        0.1    500        200  thrpt    3   3603.484 ±  1181.290  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   1000          0  thrpt    3    436.072 ±   138.488  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   1000         10  thrpt    3    442.768 ±    66.305  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   1000         50  thrpt    3    430.506 ±    55.385  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   1000        100  thrpt    3    414.036 ±     6.650  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   1000        200  thrpt    3    412.721 ±    57.881  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   2000          0  thrpt    3     92.263 ±     2.296  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   2000         10  thrpt    3     94.614 ±     2.330  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   2000         50  thrpt    3     93.318 ±     2.203  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   2000        100  thrpt    3     90.014 ±    22.277  ops/s
BasisRepresentationBenchmark.solveProduct        0.1   2000        200  thrpt    3     90.551 ±     6.301  ops/s
BasisRepresentationBenchmark.solveProduct        0.5    500          0  thrpt    3   4879.502 ±  1228.374  ops/s
BasisRepresentationBenchmark.solveProduct        0.5    500         10  thrpt    3   4828.047 ±  1210.786  ops/s
BasisRepresentationBenchmark.solveProduct        0.5    500         50  thrpt    3   4176.183 ±   631.101  ops/s
BasisRepresentationBenchmark.solveProduct        0.5    500        100  thrpt    3   3562.501 ±   872.766  ops/s
BasisRepresentationBenchmark.solveProduct        0.5    500        200  thrpt    3   3510.580 ±   240.560  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   1000          0  thrpt    3    449.581 ±    37.205  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   1000         10  thrpt    3    448.660 ±     5.063  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   1000         50  thrpt    3    428.512 ±    66.371  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   1000        100  thrpt    3    420.307 ±    22.607  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   1000        200  thrpt    3    423.285 ±    15.807  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   2000          0  thrpt    3     94.687 ±     3.007  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   2000         10  thrpt    3     94.700 ±     3.888  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   2000         50  thrpt    3     88.910 ±    41.231  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   2000        100  thrpt    3     88.965 ±    25.077  ops/s
BasisRepresentationBenchmark.solveProduct        0.5   2000        200  thrpt    3     90.394 ±     0.974  ops/s
BasisRepresentationBenchmark.solveSparse        0.01    500          0  thrpt    3   6118.217 ±   522.861  ops/s
BasisRepresentationBenchmark.solveSparse        0.01    500         10  thrpt    3   6660.845 ±   486.904  ops/s
BasisRepresentationBenchmark.solveSparse        0.01    500         50  thrpt    3   6320.284 ± 12552.961  ops/s
BasisRepresentationBenchmark.solveSparse        0.01    500        100  thrpt    3   6357.969 ±   496.916  ops/s
BasisRepresentationBenchmark.solveSparse        0.01    500        200  thrpt    3   6715.551 ±   990.581  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   1000          0  thrpt    3    797.422 ±     8.962  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   1000         10  thrpt    3    827.849 ±   100.461  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   1000         50  thrpt    3    785.278 ±    50.318  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   1000        100  thrpt    3    828.151 ±   107.756  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   1000        200  thrpt    3    803.981 ±   189.614  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   2000          0  thrpt    3    128.055 ±    53.703  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   2000         10  thrpt    3    133.666 ±     7.528  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   2000         50  thrpt    3    134.086 ±    39.322  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   2000        100  thrpt    3    142.164 ±    55.906  ops/s
BasisRepresentationBenchmark.solveSparse        0.01   2000        200  thrpt    3    143.213 ±    15.939  ops/s
BasisRepresentationBenchmark.solveSparse         0.1    500          0  thrpt    3   2309.363 ±   220.333  ops/s
BasisRepresentationBenchmark.solveSparse         0.1    500         10  thrpt    3   2293.754 ±   165.809  ops/s
BasisRepresentationBenchmark.solveSparse         0.1    500         50  thrpt    3   2273.094 ±   229.100  ops/s
BasisRepresentationBenchmark.solveSparse         0.1    500        100  thrpt    3   2269.430 ±   120.985  ops/s
BasisRepresentationBenchmark.solveSparse         0.1    500        200  thrpt    3   2263.904 ±   293.605  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   1000          0  thrpt    3    511.315 ±    62.078  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   1000         10  thrpt    3    510.796 ±   107.394  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   1000         50  thrpt    3    513.573 ±   111.962  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   1000        100  thrpt    3    529.172 ±   110.850  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   1000        200  thrpt    3    513.617 ±    80.874  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   2000          0  thrpt    3     98.653 ±    50.219  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   2000         10  thrpt    3    100.345 ±    19.317  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   2000         50  thrpt    3    105.996 ±     8.546  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   2000        100  thrpt    3    107.767 ±    17.525  ops/s
BasisRepresentationBenchmark.solveSparse         0.1   2000        200  thrpt    3    109.489 ±     7.637  ops/s
BasisRepresentationBenchmark.solveSparse         0.5    500          0  thrpt    3   2150.607 ±   268.377  ops/s
BasisRepresentationBenchmark.solveSparse         0.5    500         10  thrpt    3   2157.442 ±    92.481  ops/s
BasisRepresentationBenchmark.solveSparse         0.5    500         50  thrpt    3   2150.750 ±   259.014  ops/s
BasisRepresentationBenchmark.solveSparse         0.5    500        100  thrpt    3   2144.825 ±    49.260  ops/s
BasisRepresentationBenchmark.solveSparse         0.5    500        200  thrpt    3   2163.099 ±   128.196  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   1000          0  thrpt    3    526.965 ±    52.612  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   1000         10  thrpt    3    528.255 ±    85.017  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   1000         50  thrpt    3    524.316 ±    85.724  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   1000        100  thrpt    3    521.533 ±    62.295  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   1000        200  thrpt    3    528.428 ±    30.483  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   2000          0  thrpt    3    104.857 ±     1.073  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   2000         10  thrpt    3    106.327 ±    40.952  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   2000         50  thrpt    3    106.339 ±     2.036  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   2000        100  thrpt    3    106.964 ±     9.357  ops/s
BasisRepresentationBenchmark.solveSparse         0.5   2000        200  thrpt    3    106.764 ±     4.217  ops/s
 * </pre>
 */
@State(Scope.Benchmark)
public class BasisRepresentationBenchmark {

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().timeUnit(TimeUnit.SECONDS).measurementTime(new TimeValue(5, TimeUnit.SECONDS)).warmupIterations(1)
                .measurementIterations(3), BasisRepresentationBenchmark.class);
    }

    @Param({ "0.01", "0.1", "0.5" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;
    @Param({ "0", "10", "50", "100", "200" })
    public int updates;

    private R064CSC csc;
    private int[] included;
    private double[] rhsProduct;
    private double[] rhsSparse;
    private BasisRepresentation productForm;
    private BasisRepresentation sparseDecomp;

    public BasisRepresentationBenchmark() {
        super();
    }

    @Setup(Level.Trial)
    public void setup() {

        csc = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density).toCSC();

        included = new int[dim];
        for (int i = 0; i < dim; i++) {
            included[i] = i;
        }

        int[] columnIndex = new int[updates];
        for (int i = 0; i < updates; i++) {
            columnIndex[i] = Uniform.randomInteger(dim);
        }

        sparseDecomp = new SparseDecomposition(dim);
        productForm = new ProductFormInverse(dim);

        sparseDecomp.reset(csc, included);
        productForm.reset(csc, included);

        for (int u = 0; u < updates; u++) {
            sparseDecomp.update(csc, included, columnIndex[u], columnIndex[u]);
            productForm.update(csc, included, columnIndex[u], columnIndex[u]);
        }
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        double[] base = AbstractBenchmarkSparseLU.newDenseVector(dim).toRawCopy1D();
        rhsProduct = base.clone();
        rhsSparse = base.clone();
    }

    @Benchmark
    public void solveProduct() {
        productForm.ftran(rhsProduct);
        productForm.btran(rhsProduct);
    }

    @Benchmark
    public void solveSparse() {
        sparseDecomp.ftran(rhsSparse);
        sparseDecomp.btran(rhsSparse);
    }
}