/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.array;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class DensityTrackingArrayTest extends ArrayTests {

    /**
     * The index lists every nonzero value, and no position twice.
     */
    public static void assertIndexComplete(final DensityTrackingArray array) {

        double[] listed = new double[array.size()];
        boolean[] seen = new boolean[array.size()];
        int[] indices = array.indices();
        for (int k = 0, limit = array.countNonzeros(); k < limit; k++) {
            int i = indices[k];
            TestUtils.assertFalse(seen[i]);
            seen[i] = true;
            listed[i] = array.values[i];
        }

        TestUtils.assertEquals(array.values, listed);
    }

    private static int[] sortedIndices(final DensityTrackingArray array) {
        int[] retVal = Arrays.copyOf(array.indices(), array.countNonzeros());
        Arrays.sort(retVal);
        return retVal;
    }

    /**
     * Adding values that cancel to exactly zero, and then adding again, must not list the same position
     * twice.
     */
    @Test
    public void testAddAfterCancellationDoesNotDuplicate() {

        DensityTrackingArray array = new DensityTrackingArray(10);

        array.add(3, 1.5);
        array.add(3, -1.5);
        array.add(3, 2.0);
        array.add(7, 1.0);

        TestUtils.assertEquals(new int[] { 3, 7 }, DensityTrackingArrayTest.sortedIndices(array));
        TestUtils.assertEquals(2.0, array.doubleValue(3));
    }

    /**
     * axpy and dot only visit the listed positions, and give the same results as the dense operations.
     */
    @Test
    public void testAxpyAndDot() {

        DensityTrackingArray array = new DensityTrackingArray(6);

        array.add(1, 2.0);
        array.add(4, -3.0);
        array.add(2, 1.0);
        array.add(2, -1.0);

        double[] y = { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 };
        array.axpy(0.5, y);
        TestUtils.assertEquals(new double[] { 1.0, 2.0, 1.0, 1.0, -0.5, 1.0 }, y);

        double[] vector = { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        TestUtils.assertEquals(2.0 * 2.0 - 3.0 * 5.0, array.dot(vector));
    }

    @Test
    public void testDirectWriteAndDeclare() {

        DensityTrackingArray array = new DensityTrackingArray(8);

        int[] indices = array.indices();
        array.values[2] = 4.0;
        array.values[5] = -1.0;
        indices[0] = 5;
        indices[1] = 2;
        array.setNonzeroCount(2);

        TestUtils.assertEquals(2, array.countNonzeros());
        TestUtils.assertEquals(0.25, array.density());
        TestUtils.assertEquals(new int[] { 2, 5 }, DensityTrackingArrayTest.sortedIndices(array));

        array.reset();

        TestUtils.assertEquals(0, array.countNonzeros());
        for (int i = 0; i < array.size(); i++) {
            TestUtils.assertEquals(0.0, array.doubleValue(i));
        }
    }

    /**
     * A seeded random sequence of operations, checked against a plain array: the values always agree, and
     * (checked now and then, since that validates the index) the index lists every nonzero, and no position
     * twice. The values are multiples of 0.5, so sums cancel to exactly zero.
     */
    @Test
    public void testRandomOperations() {

        Random random = new Random(123L);
        int dim = 20;
        double[] expected = new double[dim];
        DensityTrackingArray array = new DensityTrackingArray(dim);

        for (int step = 0; step < 5_000; step++) {

            int i = random.nextInt(dim);
            double value = random.nextInt(3) - 1 + 0.5 * random.nextInt(2);

            switch (random.nextInt(9)) {
            case 0:
            case 1:
                array.add(i, value);
                expected[i] += value;
                break;
            case 2:
                array.set(i, value);
                expected[i] = value;
                break;
            case 3:
                array.tighten(0.75);
                for (int k = 0; k < dim; k++) {
                    if (Math.abs(expected[k]) <= 0.75) {
                        expected[k] = 0.0;
                    }
                }
                break;
            case 4:
                if (random.nextInt(10) == 0) {
                    array.reset();
                    Arrays.fill(expected, 0.0);
                }
                break;
            case 5:
                array.values[i] = value;
                expected[i] = value;
                array.invalidateIndex();
                break;
            case 6:
                array.reindex();
                break;
            case 7:
                int[] indices = array.indices();
                int count = array.countNonzeros();
                boolean listed = false;
                for (int k = 0; k < count; k++) {
                    listed |= indices[k] == i;
                }
                if (!listed && value != 0.0) {
                    array.values[i] = value;
                    indices[count] = i;
                    array.setNonzeroCount(count + 1);
                    expected[i] = value;
                }
                break;
            default:
                DensityTrackingArray copy = new DensityTrackingArray(dim);
                array.supplyTo(copy);
                TestUtils.assertEquals(expected, copy.values);
                DensityTrackingArrayTest.assertIndexComplete(copy);
                break;
            }

            TestUtils.assertEquals(expected, array.values);
            if (random.nextInt(4) == 0) {
                DensityTrackingArrayTest.assertIndexComplete(array);
            }
        }
    }

    @Test
    public void testReindexAfterDirectWrite() {

        DensityTrackingArray array = new DensityTrackingArray(6);

        array.values[1] = 1.0;
        array.values[4] = 2.0;
        array.reindex();

        TestUtils.assertEquals(new int[] { 1, 4 }, DensityTrackingArrayTest.sortedIndices(array));
    }

    /**
     * A reset with more than half of the positions listed clears everything (not only the listed positions),
     * after which the array is tracked as usual.
     */
    @Test
    public void testResetWhenMostlyListedThenAdd() {

        DensityTrackingArray array = new DensityTrackingArray(6);
        for (int i = 0; i < 5; i++) {
            array.set(i, 1.0 + i);
        }

        array.reset();
        array.add(2, 3.0);
        array.add(2, 1.0);

        TestUtils.assertEquals(new double[] { 0.0, 0.0, 4.0, 0.0, 0.0, 0.0 }, array.values);
        TestUtils.assertEquals(new int[] { 2 }, DensityTrackingArrayTest.sortedIndices(array));
    }

    @Test
    public void testSetToZeroAndBackDoesNotDuplicate() {

        DensityTrackingArray array = new DensityTrackingArray(10);

        array.set(2, 1.0);
        array.set(2, 0.0);
        array.set(2, 3.0);
        array.set(5, 0.0);

        TestUtils.assertEquals(new int[] { 2 }, DensityTrackingArrayTest.sortedIndices(array));
        TestUtils.assertEquals(3.0, array.doubleValue(2));
    }

    /**
     * supplyTo resets the receiver and copies the listed nonzeros, but not positions whose values have
     * cancelled to zero.
     */
    @Test
    public void testSupplyToDropsCancelledZeros() {

        DensityTrackingArray from = new DensityTrackingArray(6);

        from.add(1, 2.0);
        from.add(3, 1.0);
        from.add(3, -1.0);
        from.add(5, -4.0);

        DensityTrackingArray to = new DensityTrackingArray(6);
        to.set(0, 7.0);

        from.supplyTo(to);

        TestUtils.assertEquals(new int[] { 1, 5 }, DensityTrackingArrayTest.sortedIndices(to));
        TestUtils.assertEquals(new double[] { 0.0, 2.0, 0.0, 0.0, 0.0, -4.0 }, to.values);
    }

    @Test
    public void testTightenDropsTinyValues() {

        DensityTrackingArray array = new DensityTrackingArray(5);

        array.set(0, 1.0);
        array.set(1, 1E-16);
        array.set(2, -3.0);
        array.set(3, -1E-15);

        array.tighten(1E-14);

        TestUtils.assertEquals(new int[] { 0, 2 }, DensityTrackingArrayTest.sortedIndices(array));
        TestUtils.assertEquals(0.0, array.doubleValue(1));
        TestUtils.assertEquals(0.0, array.doubleValue(3));
    }

    /**
     * A NaN is not a small value: tighten keeps it (and a numerical breakdown stays visible).
     */
    @Test
    public void testTightenKeepsNaN() {

        DensityTrackingArray array = new DensityTrackingArray(3);

        array.set(0, Double.NaN);
        array.set(2, 1E-16);

        array.tighten(1E-14);

        TestUtils.assertEquals(new int[] { 0 }, DensityTrackingArrayTest.sortedIndices(array));
        TestUtils.assertTrue(Double.isNaN(array.doubleValue(0)));
    }

    /**
     * A position dropped by tighten is listed again, once, when a value is added to it.
     */
    @Test
    public void testTightenThenAdd() {

        DensityTrackingArray array = new DensityTrackingArray(5);

        array.set(1, 1E-16);
        array.set(3, 2.0);
        array.tighten(1E-14);
        array.add(1, 1.0);
        array.add(1, 1.0);

        TestUtils.assertEquals(new int[] { 1, 3 }, DensityTrackingArrayTest.sortedIndices(array));
        DensityTrackingArrayTest.assertIndexComplete(array);
        TestUtils.assertEquals(2.0, array.doubleValue(1));
    }

    /**
     * A wrapped array builds its index (with a scan) when first needed, and adding to a position that is
     * already nonzero does not list it twice.
     */
    @Test
    public void testWrapThenAdd() {

        double[] data = { 0.0, 1.0, 0.0, -2.0 };
        DensityTrackingArray array = DensityTrackingArray.wrap(data);

        array.add(1, 1.0);
        array.add(2, 3.0);

        TestUtils.assertEquals(new double[] { 0.0, 2.0, 3.0, -2.0 }, data);
        TestUtils.assertEquals(new int[] { 1, 2, 3 }, DensityTrackingArrayTest.sortedIndices(array));
        DensityTrackingArrayTest.assertIndexComplete(array);
    }

}
