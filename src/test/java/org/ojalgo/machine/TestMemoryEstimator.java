package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;

/**
 * TestMemoryEstimator
 *
 * @author apete
 */
public class TestMemoryEstimator extends MachineTests {

    /**
     * https://lemire.me/blog/2022/11/22/what-is-the-size-of-a-byte-array-in-java/
     */
    @Test
    public void testByteArray() {

        long[] expected = { 16, 24, 24, 24, 24, 24, 24, 24, 24, 32 };

        for (int i = 0; i < expected.length; i++) {

            long estimate = MemoryEstimator.estimateArray(byte.class, i);

            if (DEBUG) {
                BasicLogger.debug("byte[{}] == {} bytes", i, estimate);
            }

            TestUtils.assertEquals(expected[i], estimate);
        }

    }

}
