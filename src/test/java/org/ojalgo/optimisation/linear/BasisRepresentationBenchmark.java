package org.ojalgo.optimisation.linear;

import java.util.concurrent.TimeUnit;

import org.ojalgo.BenchmarkUtils;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.decomposition.AbstractBenchmarkSparseLU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
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
 * <pre>
# Run complete. Total time: 01:01:47

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                    (density)  (dim)   Mode  Cnt     Score     Error    Units
BasisRepresentationBenchmark.b1SparseDecomp      0.005    500  thrpt    3  6102.713 ± 201.892  ops/min
BasisRepresentationBenchmark.b1SparseDecomp      0.005   1000  thrpt    3   425.985 ±  30.779  ops/min
BasisRepresentationBenchmark.b1SparseDecomp      0.005   2000  thrpt    3    26.642 ±   0.980  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.01    500  thrpt    3  1352.892 ±  77.082  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.01   1000  thrpt    3   142.907 ± 116.309  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.01   2000  thrpt    3    18.519 ±   0.578  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.02    500  thrpt    3   768.167 ±   5.116  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.02   1000  thrpt    3   117.131 ±   6.201  ops/min
BasisRepresentationBenchmark.b1SparseDecomp       0.02   2000  thrpt    3    15.929 ±   0.271  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005    500  thrpt    3  2109.399 ± 190.937  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005   1000  thrpt    3   254.947 ±  41.219  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005   2000  thrpt    3    28.088 ±   0.983  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01    500  thrpt    3   119.026 ± 527.828  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01   1000  thrpt    3   187.175 ± 226.024  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01   2000  thrpt    3    23.673 ±   1.261  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02    500  thrpt    3   647.774 ±   1.505  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02   1000  thrpt    3   108.989 ±  27.972  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02   2000  thrpt    3    15.166 ±   0.509  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005    500  thrpt    3   101.993 ± 255.364  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005   1000  thrpt    3   250.062 ±  20.739  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005   2000  thrpt    3    27.902 ±   0.991  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01    500  thrpt    3   148.834 ± 606.243  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01   1000  thrpt    3   169.352 ±   6.571  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01   2000  thrpt    3    17.072 ±   3.553  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02    500  thrpt    3   718.732 ±  38.992  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02   1000  thrpt    3   132.619 ±  27.145  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02   2000  thrpt    3    21.631 ±   0.392  ops/min
 * </pre>
 *
 * Grouped/Sorted:
 *
 * <pre>
Benchmark                                    (density)  (dim)   Mode  Cnt     Score     Error    Units
BasisRepresentationBenchmark.b1SparseDecomp      0.005    500  thrpt    3  6102.713 ± 201.892  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005    500  thrpt    3  2109.399 ± 190.937  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005    500  thrpt    3   101.993 ± 255.364  ops/min

BasisRepresentationBenchmark.b1SparseDecomp      0.005   1000  thrpt    3   425.985 ±  30.779  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005   1000  thrpt    3   254.947 ±  41.219  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005   1000  thrpt    3   250.062 ±  20.739  ops/min

BasisRepresentationBenchmark.b1SparseDecomp      0.005   2000  thrpt    3    26.642 ±   0.980  ops/min
BasisRepresentationBenchmark.b2DenseDecomp       0.005   2000  thrpt    3    28.088 ±   0.983  ops/min
BasisRepresentationBenchmark.b3ProductForm       0.005   2000  thrpt    3    27.902 ±   0.991  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.01    500  thrpt    3  1352.892 ±  77.082  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01    500  thrpt    3   119.026 ± 527.828  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01    500  thrpt    3   148.834 ± 606.243  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.01   1000  thrpt    3   142.907 ± 116.309  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01   1000  thrpt    3   187.175 ± 226.024  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01   1000  thrpt    3   169.352 ±   6.571  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.01   2000  thrpt    3    18.519 ±   0.578  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.01   2000  thrpt    3    23.673 ±   1.261  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.01   2000  thrpt    3    17.072 ±   3.553  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.02    500  thrpt    3   768.167 ±   5.116  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02    500  thrpt    3   647.774 ±   1.505  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02    500  thrpt    3   718.732 ±  38.992  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.02   1000  thrpt    3   117.131 ±   6.201  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02   1000  thrpt    3   108.989 ±  27.972  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02   1000  thrpt    3   132.619 ±  27.145  ops/min

BasisRepresentationBenchmark.b1SparseDecomp       0.02   2000  thrpt    3    15.929 ±   0.271  ops/min
BasisRepresentationBenchmark.b2DenseDecomp        0.02   2000  thrpt    3    15.166 ±   0.509  ops/min
BasisRepresentationBenchmark.b3ProductForm        0.02   2000  thrpt    3    21.631 ±   0.392  ops/min
 * </pre>
 */
@State(Scope.Benchmark)
public class BasisRepresentationBenchmark {

    private static final int LOOP = 50;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().timeUnit(TimeUnit.MINUTES).measurementTime(new TimeValue(20, TimeUnit.SECONDS)),
                BasisRepresentationBenchmark.class);
    }

    @Param({ "0.005", "0.01", "0.02" })
    public double density;
    @Param({ "500", "1000", "2000" })
    public int dim;
    private MatrixStore<Double> basis;
    private final int[] columnIndex = new int[LOOP];
    private final DecomposedInverse denseDecomp = new DecomposedInverse(false, 1_000);
    private final SparseArray<Double>[] newColumn = new SparseArray[LOOP];
    private final ProductFormInverse productForm = new ProductFormInverse(1_000, 1E-4);

    private PhysicalStore<Double> rhs;
    private final DecomposedInverse sparseDecomp = new DecomposedInverse(true, 1_000);

    public BasisRepresentationBenchmark() {

        super();

    }

    @Benchmark
    public void b1SparseDecomp() {
        this.doBenchmark(sparseDecomp);
    }

    @Benchmark
    public void b2DenseDecomp() {
        this.doBenchmark(denseDecomp);
    }

    @Benchmark
    public void b3ProductForm() {
        this.doBenchmark(productForm);
    }

    @Setup(Level.Trial)
    public void setup() {

        basis = AbstractBenchmarkSparseLU.newSparseMatrix(dim, density);

        for (int i = 0; i < newColumn.length; i++) {
            newColumn[i] = AbstractBenchmarkSparseLU.newSparseVector(dim, density);
            columnIndex[i] = Uniform.randomInteger(dim);
        }

        rhs = AbstractBenchmarkSparseLU.newDenseVector(dim);

        sparseDecomp.reset(basis);
        denseDecomp.reset(basis);
        productForm.reset(basis);
    }

    void doBenchmark(final BasisRepresentation representation) {

        representation.reset(basis);

        for (int u = 0; u < newColumn.length; u++) {

            sparseDecomp.update(basis, columnIndex[u], newColumn[u]);

            sparseDecomp.ftran(rhs);
            sparseDecomp.btran(rhs);
            sparseDecomp.ftran(rhs);
        }

    }
}