package org.ojalgo.matrix.decomposition;

import org.ojalgo.array.ArrayR064;
import org.ojalgo.array.SparseArray;
import org.ojalgo.matrix.store.ColumnsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
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

        int dim = 1_000;

        MatrixStore<Double> matrix = AbstractBenchmarkSparseLU.newSparseMatrix(dim, 0.01);
        SparseArray<Double> vecor = AbstractBenchmarkSparseLU.newSparseVector(dim, 0.01);
        PhysicalStore<Double> rhs = AbstractBenchmarkSparseLU.newDenseVector(dim);

        for (int d = 0; d < 1_000_000; d++) {

            sparse.decompose(matrix);

            vecor = AbstractBenchmarkSparseLU.newSparseVector(dim, 0.01);
            rhs = AbstractBenchmarkSparseLU.newDenseVector(dim);

            while (sparse.updateColumn(Uniform.randomInteger(dim), vecor)) {

                for (int t = 0; t < 5; t++) {
                    sparse.ftran(rhs);
                    sparse.btran(rhs);
                }
            }
        }
    }

    public static PhysicalStore<Double> newDenseVector(final int dim) {

        // Create random vector
        R064Store vector = R064Store.FACTORY.make(dim, 1);
        for (int i = 0; i < dim; i++) {
            vector.set(i, RANDOM.doubleValue());
        }

        return vector;
    }

    public static MatrixStore<Double> newSparseMatrix(final int dim, final double density) {

        // Create sparse matrix with specified density
        ColumnsSupplier<Double> builder = R064Store.FACTORY.makeColumnsSupplier(dim);
        builder.addColumns(dim);

        int nonZeroCount = ((int) (dim * dim * density));

        int offDiag = nonZeroCount - dim;
        for (int i = 0; i < offDiag; i++) {
            builder.set(Uniform.randomInteger(dim), Uniform.randomInteger(dim), RANDOM.doubleValue());
        }

        // Ensure diagonal elements are non-zero
        for (int i = 0; i < dim; i++) {
            builder.set(i, i, RANDOM.doubleValue());
        }

        return builder;
    }

    public static SparseArray<Double> newSparseVector(final int dim, final double density) {

        // Create sparse matrix with specified density
        SparseArray<Double> builder = SparseArray.factory(ArrayR064.FACTORY).make(dim);
        int nonZeroCount = ((int) (dim * density));

        for (int i = 0; i < nonZeroCount; i++) {
            builder.set(Uniform.randomInteger(dim), RANDOM.doubleValue());
        }

        return builder;
    }

    final LU<Double> dense = new DenseLU.R064();
    final LU<Double> raw = new RawLU();
    final LU<Double> sparse = new SparseLU();

}
