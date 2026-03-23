package org.ojalgo.matrix.decomposition;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;

/**
 * https://github.com/optimatika/ojAlgo/issues/661
 */
public class IssueSingularValueR128 extends MatrixDecompositionTests {

    private static final double e = Double.MIN_VALUE;

    private static final long SECONDS = 1L;

    static <N extends Comparable<N>> void doAssert(final SingularValue<N> impl, final RawStore matrix, final PhysicalStore.Factory<N, ?> factory) {
        String name = impl.getClass().getSimpleName();
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(SECONDS), () -> {
            boolean result = impl.decompose(factory.copy(matrix));
            Assertions.assertTrue(result);
        }, (name + ": Decomposition took too long, likely an infinite loop."));
    }

    static void doTest(final RawStore matrix) {

        for (SingularValue<Double> impl : MatrixDecompositionTests.getPrimitiveSingularValue()) {
            IssueSingularValueR128.doAssert(impl, matrix, R064Store.FACTORY);
        }

        IssueSingularValueR128.doAssert(SingularValue.R128.make(matrix), matrix, GenericStore.R128);

        IssueSingularValueR128.doAssert(SingularValue.C128.make(matrix), matrix, GenericStore.C128);
    }

    @Test
    public void testInfiniteLoopR128_0() {
        IssueSingularValueR128.doTest(RawStore.wrap(new double[][] { { 1, 0, 0, 0 }, { 0, 1, 1, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } }));
    }

    @Test
    public void testInfiniteLoopR128_1() {
        IssueSingularValueR128.doTest(RawStore.wrap(new double[][] { { 1, e, e, e }, { e, e, 1, e }, { e, 1, e, e }, { e, e, e, 1 } }));
    }

    @Test
    public void testInfiniteLoopR128_2() {
        IssueSingularValueR128.doTest(RawStore.wrap(new double[][] { { 1, 0, 0, 0 }, { 0, 0, 1, 0 }, { 0, 1, 0, 0 }, { 0, 0, 0, 1 } }));
    }

    @Test
    public void testInfiniteLoopR128_3() {
        IssueSingularValueR128.doTest(RawStore.wrap(new double[][] { { 0, 0, 3, 0 }, { 0, 1, 1, 1 }, { 3, 1, 0, 0 }, { 0, 1, 0, 0 } }));
    }

    @Test
    public void testInfiniteLoopR128_4() {
        IssueSingularValueR128.doTest(RawStore.wrap(new double[][] { { 0, 0, 3, 0 }, { 0, 12433.182577348916, 1, 0.3545802800218381 },
                { 3, 1, 0, -2.8191084267847077E-24 }, { 0, 0.15640108192643198, 0, 3.2230232111154273E-8 } }));
    }

}
