/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnumBitSetTest {

    private enum TestEnum {
        VALUE_A, VALUE_B, VALUE_C, VALUE_D
    }

    @Test
    void testClearAllBits() {

        EnumBitSet<TestEnum> bitSet = new EnumBitSet<>();

        // Set some values
        bitSet.set(TestEnum.VALUE_A, true);
        bitSet.set(TestEnum.VALUE_B, true);

        // Verify set
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_B));

        // Clear all
        bitSet.clear();

        // Verify cleared
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_B));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_C));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_D));
    }

    @Test
    void testMaskValidation() {

        EnumBitSet<TestEnum> bitSet = new EnumBitSet<>();

        // Invalid mask with no bits set
        long invalidMaskZero = 0L;
        IllegalArgumentException exceptionZero = Assertions.assertThrows(IllegalArgumentException.class, () -> bitSet.set(invalidMaskZero, true));
        Assertions.assertEquals("Mask must have exactly one bit set!", exceptionZero.getMessage());

        // Invalid mask with multiple bits set
        long invalidMaskMultiple = 1L << TestEnum.VALUE_A.ordinal() | 1L << TestEnum.VALUE_B.ordinal();
        IllegalArgumentException exceptionMultiple = Assertions.assertThrows(IllegalArgumentException.class, () -> bitSet.set(invalidMaskMultiple, true));
        Assertions.assertEquals("Mask must have exactly one bit set!", exceptionMultiple.getMessage());
    }

    @Test
    void testSetAndGetBitSet() {

        EnumBitSet<TestEnum> bitSet = new EnumBitSet<>();

        // Set the internal bitSet directly
        long newBitSetValue = 1L << TestEnum.VALUE_A.ordinal() | 1L << TestEnum.VALUE_D.ordinal();
        bitSet.setBitSet(newBitSetValue);

        // Validate the changes
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_B));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_C));
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_D));

        // Retrieve the internal value and validate
        Assertions.assertEquals(newBitSetValue, bitSet.getBitSet());
    }

    @Test
    void testSetAndGetIndividualBits() {

        EnumBitSet<TestEnum> bitSet = new EnumBitSet<>();

        // Initially, all values should be false
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_B));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_C));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_D));

        // Set some values
        bitSet.set(TestEnum.VALUE_A, true);
        bitSet.set(TestEnum.VALUE_C, true);

        // Check individual values
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_B));
        Assertions.assertTrue(bitSet.get(TestEnum.VALUE_C));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_D));

        // Reset values
        bitSet.set(TestEnum.VALUE_A, false);
        bitSet.set(TestEnum.VALUE_C, false);

        // Verify all are back to false
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_A));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_B));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_C));
        Assertions.assertFalse(bitSet.get(TestEnum.VALUE_D));
    }

    @Test
    void testSetAndGetWithBitmask() {

        EnumBitSet<TestEnum> bitSet = new EnumBitSet<>();

        long maskA = 1L << TestEnum.VALUE_A.ordinal();
        long maskB = 1L << TestEnum.VALUE_B.ordinal();

        // Set using mask
        bitSet.set(maskA, true);
        bitSet.set(maskB, false);

        // Check using mask
        Assertions.assertTrue(bitSet.get(maskA));
        Assertions.assertFalse(bitSet.get(maskB));

        // Invalid mask test
        long invalidMask = 0b11; // More than one bit set
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> bitSet.get(invalidMask));
        Assertions.assertEquals("Mask must have exactly one bit set!", exception.getMessage());
    }
}
