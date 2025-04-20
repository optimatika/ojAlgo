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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * <pre>
# Run complete. Total time: 00:04:29

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                        Mode  Cnt    Score    Error    Units
BasisRepresentationBenchmark.decomposedUpdate   thrpt    3  163.033 ±  2.593  ops/min
BasisRepresentationBenchmark.productFormUpdate  thrpt    3   56.545 ± 97.432  ops/min
 * </pre>
 */
@State(Scope.Benchmark)
public class BasisRepresentationBenchmark {

    private static final int LOOP = 100;
    static final double DENSITY = 0.01;
    static final int DIM = 1_000;

    public static void main(final String[] args) throws RunnerException {
        BenchmarkUtils.run(BenchmarkUtils.options().timeUnit(TimeUnit.MINUTES).measurementTime(new TimeValue(20, TimeUnit.SECONDS)),
                BasisRepresentationBenchmark.class);
    }

    private final MatrixStore<Double> basis;
    private final DecomposedInverse decomposed = new DecomposedInverse(false, DIM);
    private final SparseArray<Double>[] newColumn = new SparseArray[LOOP];
    private final ProductFormInverse productForm = new ProductFormInverse(DIM, 1E-4);
    private final int[] columnIndex = new int[LOOP];
    private PhysicalStore<Double> rhs;

    public BasisRepresentationBenchmark() {

        super();

        basis = AbstractBenchmarkSparseLU.newSparseMatrix(DIM, DENSITY);

        productForm.reset(basis);
        decomposed.reset(basis);
    }

    @Benchmark
    public void decomposedUpdate() {
        for (int i = 0; i < newColumn.length; i++) {
            decomposed.update(basis, columnIndex[i], newColumn[i]);
            decomposed.ftran(rhs);
            decomposed.btran(rhs);
        }
    }

    @Benchmark
    public void productFormUpdate() {
        for (int i = 0; i < newColumn.length; i++) {
            productForm.update(basis, columnIndex[i], newColumn[i]);
            productForm.ftran(rhs);
            productForm.btran(rhs);
        }
    }

    @Setup(Level.Trial)
    public void setup() {

        for (int i = 0; i < newColumn.length; i++) {
            newColumn[i] = AbstractBenchmarkSparseLU.newSparseVector(DIM, DENSITY);
            columnIndex[i] = Uniform.randomInteger(DIM);
        }

        rhs = AbstractBenchmarkSparseLU.newDenseVector(DIM);
    }
}