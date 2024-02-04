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

import java.util.Arrays;

/**
 * Keeps track of n (ordered) {@link Enum} values – any {@link Enum} and any number of values. This is a
 * generalised version of {@link IndexSelector}.
 *
 * @see IndexSelector
 * @author apete
 */
public final class EnumPartition<E extends Enum<E>> {

    private final E[] myConstants;
    private final int[] myCounts;
    private final byte[] myValues;

    public EnumPartition(final int size, final E initialValue) {

        super();

        myValues = new byte[size];
        myConstants = initialValue.getDeclaringClass().getEnumConstants();
        myCounts = new int[myConstants.length];

        this.fill(initialValue);
    }

    /**
     * Count the number of times a specific value appears.
     */
    public int count(final E value) {
        return this.count(value, false);
    }

    /**
     * Generalised version of {@link #count(Enum)} that allows to instead count the number of times something
     * else is set.
     */
    public int count(final E value, final boolean negate) {
        int count = myCounts[value.ordinal()];
        if (negate) {
            return myValues.length - count;
        } else {
            return count;
        }
    }

    /**
     * Get the indices where this value is set.
     */
    public int[] extract(final E value) {
        return this.extract(value, false);
    }

    /**
     * Generalised version of {@link #extract(Enum)} that allows to instead get the negated (complement) set
     * of indices.
     */
    public int[] extract(final E value, final boolean negate) {

        int count = this.count(value, negate);
        int[] retVal = new int[count];

        this.extract(value, negate, retVal);

        return retVal;
    }

    /**
     * Generalised version of {@link #extract(Enum, boolean)} that allows to supply the array the resulting
     * indices should be written to.
     */
    public void extract(final E value, final boolean negate, final int[] receiver) {

        byte key = (byte) value.ordinal();

        for (int i = 0, j = 0; i < myValues.length && j < receiver.length; i++) {
            if ((myValues[i] == key) != negate) {
                receiver[j] = i;
                j++;
            }
        }
    }

    public void fill(final E value) {

        int index = value.ordinal();
        byte key = (byte) index;

        Arrays.fill(myValues, key);
        Arrays.fill(myCounts, 0);
        myCounts[index] = myValues.length;
    }

    public E get(final int index) {
        return myConstants[myValues[index]];
    }

    public boolean is(final int index, final E value) {
        return myValues[index] == value.ordinal();
    }

    public int size() {
        return myValues.length;
    }

    public void update(final int index, final E value) {

        int oldKey = myValues[index];
        int newKey = value.ordinal();

        if (oldKey != newKey) {

            myValues[index] = (byte) newKey;

            myCounts[oldKey]--;
            myCounts[newKey]++;
        }
    }

}
