package org.ojalgo.matrix.decomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * Tests {@link MatrixDecomposition.Resizable} implementations – resizing a decomposition one row/column at
 * the time should give the same result as decomposing the (sub)matrix.
 */
public class DecompositionResizableTest extends MatrixDecompositionTests {

    private static final NumberContext ACCURACY = NumberContext.of(10);

    private static <N extends Comparable<N>> boolean append(final Cholesky<N> cholesky, final MatrixStore<N> full, final List<Integer> rows, final int row) {

        List<Integer> extended = new ArrayList<>(rows);
        extended.add(row);

        boolean added = cholesky.appendColumn(full.rows(DecompositionResizableTest.toArray(extended)).columns(row));

        if (added) {
            rows.add(row);
        }

        return added;
    }

    private static <N extends Comparable<N>> void assertDecomposes(final Cholesky<N> cholesky, final MatrixStore<N> full, final List<Integer> rows) {

        int[] indices = DecompositionResizableTest.toArray(rows);

        TestUtils.assertEquals(indices.length, cholesky.getRowDim());
        TestUtils.assertEquals(indices.length, cholesky.getColDim());

        if (indices.length > 0) {
            TestUtils.assertEquals(full.rows(indices).columns(indices), cholesky.reconstruct(), ACCURACY);
            TestUtils.assertTrue(cholesky.isSolvable());
        }
    }

    /**
     * Symmetric positive definite: [G]<sup>T</sup>[G] + I
     */
    private static MatrixStore<Double> newSPD(final int dim, final Random random) {

        R064Store gram = R064Store.FACTORY.make(dim + 3, dim);
        for (int i = 0; i < gram.getRowDim(); i++) {
            for (int j = 0; j < dim; j++) {
                gram.set(i, j, random.nextGaussian());
            }
        }

        return gram.transpose().multiply(gram).add(R064Store.FACTORY.makeIdentity(dim)).collect(R064Store.FACTORY);
    }

    private static <N extends Comparable<N>> void remove(final Cholesky<N> cholesky, final List<Integer> rows, final int position) {
        TestUtils.assertTrue(cholesky.removeColumn(position));
        rows.remove(position);
    }

    /**
     * Grow from empty, shrink, alternate between growing and shrinking, grow after a normal decomposition,
     * and grow again after a reset.
     */
    private static <N extends Comparable<N>> void testGrowAndShrink(final Cholesky<N> cholesky, final MatrixStore<N> full) {

        List<Integer> rows = new ArrayList<>();

        for (int row : new int[] { 3, 7, 1, 19, 0, 12, 5, 8, 15, 2 }) {
            TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, row));
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }

        for (int position : new int[] { 0, 4, 7, 2 }) {
            DecompositionResizableTest.remove(cholesky, rows, position);
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }

        for (int row : new int[] { 4, 6, 9, 10, 11 }) {
            TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, row));
            DecompositionResizableTest.remove(cholesky, rows, 1);
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }

        // Start from a normal decomposition
        rows.clear();
        for (int row = 0; row < 6; row++) {
            rows.add(row);
        }
        int[] indices = DecompositionResizableTest.toArray(rows);
        TestUtils.assertTrue(cholesky.decompose(full.rows(indices).columns(indices)));
        DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        for (int row : new int[] { 13, 17, 6 }) {
            TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, row));
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }
        DecompositionResizableTest.remove(cholesky, rows, 3);
        DecompositionResizableTest.assertDecomposes(cholesky, full, rows);

        // Remove all
        while (!rows.isEmpty()) {
            DecompositionResizableTest.remove(cholesky, rows, rows.size() - 1);
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }

        // Start over after a reset
        cholesky.reset();
        for (int row : new int[] { 9, 4 }) {
            TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, row));
            DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
        }
    }

    /**
     * Growing with a row/column that is linearly dependent on the existing ones (making the matrix singular)
     * is rejected, and leaves the decomposition unchanged.
     */
    private static void testRejectDependent(final Cholesky<Double> cholesky) {

        Random random = new Random(456L);
        int dim = 6;
        MatrixStore<Double> basis = DecompositionResizableTest.newSPD(dim, random);

        // A Gram matrix with a 7:th vector that is the sum of the 1:st and 2:nd
        R064Store vectors = R064Store.FACTORY.make(dim, dim + 1);
        vectors.regionByLimits(dim, dim).fillMatching(basis);
        for (int i = 0; i < dim; i++) {
            vectors.set(i, dim, basis.doubleValue(i, 0) + basis.doubleValue(i, 1));
        }
        MatrixStore<Double> full = vectors.transpose().multiply(vectors).collect(R064Store.FACTORY);

        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < dim; row++) {
            TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, row));
        }

        TestUtils.assertFalse(DecompositionResizableTest.append(cholesky, full, rows, dim));
        DecompositionResizableTest.assertDecomposes(cholesky, full, rows);

        // Without one of the vectors it depends on, it can be added
        DecompositionResizableTest.remove(cholesky, rows, 1);
        TestUtils.assertTrue(DecompositionResizableTest.append(cholesky, full, rows, dim));
        DecompositionResizableTest.assertDecomposes(cholesky, full, rows);
    }

    private static int[] toArray(final List<Integer> rows) {
        return rows.stream().mapToInt(Integer::intValue).toArray();
    }

    @Test
    public void testComplexDenseCholesky() {

        int dim = 20;
        PhysicalStore<ComplexNumber> gram = TestUtils.makeRandomComplexStore(dim + 3, dim);
        MatrixStore<ComplexNumber> full = gram.conjugate().multiply(gram).add(GenericStore.C128.makeIdentity(dim)).collect(GenericStore.C128);

        DecompositionResizableTest.testGrowAndShrink(new DenseCholesky.C128(), full);
    }

    @Test
    public void testPrimitiveDenseCholesky() {
        DecompositionResizableTest.testGrowAndShrink(new DenseCholesky.R064(), DecompositionResizableTest.newSPD(20, new Random(123L)));
        DecompositionResizableTest.testRejectDependent(new DenseCholesky.R064());
    }

    @Test
    public void testQuadrupleDenseCholesky() {
        MatrixStore<Double> full = DecompositionResizableTest.newSPD(20, new Random(123L));
        DecompositionResizableTest.testGrowAndShrink(new DenseCholesky.R128(), GenericStore.R128.copy(full));
    }

    @Test
    public void testRawCholesky() {
        DecompositionResizableTest.testGrowAndShrink(new RawCholesky(), DecompositionResizableTest.newSPD(20, new Random(123L)));
        DecompositionResizableTest.testRejectDependent(new RawCholesky());
    }

}
