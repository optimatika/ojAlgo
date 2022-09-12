package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.array.PrimitiveR064;
import org.ojalgo.matrix.RationalMatrix;

public class TestMemoryEstimator {

    @Test
    public void testPrimitiveArray() {

        final long tmpEstimate = MemoryEstimator.estimateObject(PrimitiveR064.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }

    @Test
    public void testRationalMatrix() {

        final long tmpEstimate = MemoryEstimator.estimateObject(RationalMatrix.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }
}
