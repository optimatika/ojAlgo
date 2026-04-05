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
    private ProductFormInverse productForm;
    private SparseDecomposition sparseDecomp;

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