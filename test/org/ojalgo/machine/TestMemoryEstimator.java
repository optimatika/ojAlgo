package org.ojalgo.machine;

import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.matrix.BigMatrix;

public class TestMemoryEstimator extends MachineTests {

    public TestMemoryEstimator() {
        super();
    }

    public TestMemoryEstimator(final String someName) {
        super(someName);
    }

    public void testBigMatrix() {

        final long tmpEstimate = MemoryEstimator.estimateObject(BigMatrix.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }

    public void testPrimitiveArray() {

        final long tmpEstimate = MemoryEstimator.estimateObject(PrimitiveArray.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }
}
