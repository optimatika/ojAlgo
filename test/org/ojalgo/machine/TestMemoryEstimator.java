package org.ojalgo.machine;

import org.ojalgo.array.Primitive64Array;
import org.ojalgo.matrix.RationalMatrix;

public class TestMemoryEstimator extends MachineTests {

    public TestMemoryEstimator() {
        super();
    }

    public TestMemoryEstimator(final String someName) {
        super(someName);
    }

    public void testRationalMatrix() {

        final long tmpEstimate = MemoryEstimator.estimateObject(RationalMatrix.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }

    public void testPrimitiveArray() {

        final long tmpEstimate = MemoryEstimator.estimateObject(Primitive64Array.class);

        final long tmpManually = 0L;

        if (tmpEstimate == tmpManually) {

        }

    }
}
