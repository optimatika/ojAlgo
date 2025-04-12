package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.matrix.store.SparseStore.Builder;
import org.ojalgo.random.Uniform;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Assuming LP bases typically have density in the range of 1-5% (0.01-0.05)
 * <p>
 * Usually benchmark the dense LP solver up to a size of 1k constraints/variables. Here primarily interested
 * to learn what happens around (and above) that size.
 */
public abstract class AbstractBenchmarkSparseLU {

    static final Uniform RANDOM = Uniform.of(-10, 20);

    public static void main(final String[] args) throws RunnerException {

        LU<Double> sparse = LU.newSparseR064();

        MatrixStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(2_000, 0.02);

        for (int i = 0; i < 10_000; i++) {
            sparse.decompose(matrix);
        }
    }

    static PhysicalStore<Double> newDenseVector(final int dim) {

        // Create random vector
        R064Store vector = R064Store.FACTORY.make(dim, 1);
        for (int i = 0; i < dim; i++) {
            vector.set(i, RANDOM.doubleValue());
        }

        return vector;
    }

    static SparseStore<Double> newSparseMatrix(final int dim, final double density) {

        // Create sparse matrix with specified density
        Builder<Double> builder = SparseStore.R064.newBuilder(dim, dim);
        int nonZeroCount = ((int) (dim * dim * density));

        int offDiag = nonZeroCount - dim;
        for (int i = 0; i < offDiag; i++) {
            builder.set(Uniform.randomInteger(dim), Uniform.randomInteger(dim), RANDOM.doubleValue());
        }

        // Ensure diagonal elements are non-zero
        for (int i = 0; i < dim; i++) {
            builder.set(i, i, RANDOM.doubleValue());
        }

        return builder.build();
    }

    final LU<Double> dense = new DenseLU.R064();
    final LU<Double> raw = new RawLU();
    final LU<Double> sparse = new SparseLU();

}
