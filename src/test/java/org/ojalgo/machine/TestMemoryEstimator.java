package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.matrix.MatrixQ128;

public class TestMemoryEstimator {

    @Test
    public void testPrimitiveArray() {

        final long tmpEstimate = MemoryEstimator.estimateObject(ArrayR064.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }

    @Test
    public void testRationalMatrix() {

        final long tmpEstimate = MemoryEstimator.estimateObject(MatrixQ128.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }
}
