/*
 * Copyright 1997-2024 Optimatika
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

import java.util.BitSet;

/**
 * Like a {@link BitSet} but using an {@link Enum} as indices. The {@link Enum} cannot have more than 64
 * values as the internal storage is a single {@code long}.
 */
public final class EnumBitSet<E extends Enum<E>> {

    private static final String MASK_MUST_HAVE_EXACTLY_ONE_BIT_SET = "Mask must have exactly one bit set!";

    private static long mask(final Enum<?> index) {
        return 1L << index.ordinal();
    }

    /**
     * Stores the 64 boolean values
     */
    private long myBitSet;

    public boolean get(final E index) {
        return this.get(EnumBitSet.mask(index));
    }

    public void set(final E index, final boolean value) {
        this.set(EnumBitSet.mask(index), value);
    }

    /**
     * Clears all bits, setting all booleans to false
     */
    void clear() {
        myBitSet = 0;
    }

    /**
     * Get the boolean value corresponding to the mask
     */
    boolean get(final long mask) {
        if (mask == 0 || (mask & mask - 1) != 0) {
            throw new IllegalArgumentException(MASK_MUST_HAVE_EXACTLY_ONE_BIT_SET);
        }
        return (myBitSet & mask) != 0;
    }

    /**
     * Returns the internal long value (optional, useful for debugging or serialization)
     */
    long getBitSet() {
        return myBitSet;
    }

    /**
     * Set the boolean value corresponding to the mask
     */
    void set(final long mask, final boolean value) {
        if (mask == 0 || (mask & mask - 1) != 0) {
            throw new IllegalArgumentException(MASK_MUST_HAVE_EXACTLY_ONE_BIT_SET);
        }
        if (value) {
            myBitSet |= mask; // Turn on the bit specified by the mask
        } else {
            myBitSet &= ~mask; // Turn off the bit specified by the mask
        }
    }

    /**
     * Sets the internal long value directly (optional, useful for debugging or deserialization)
     */
    void setBitSet(final long bitSet) {
        myBitSet = bitSet;
    }

}
