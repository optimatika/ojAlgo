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
package org.ojalgo.matrix.decomposition;

import java.util.Arrays;

/**
 * A bucket queue: items 0 to n-1, each with a count (a small non-negative integer), kept in one doubly linked
 * list per count. Inserting an item, removing it (and so moving it to another count), getting the first item
 * with a given count, and the next item with the same count, are all constant time operations. That makes it
 * possible to repeatedly visit the items with the smallest counts while the counts change.
 * <p>
 * Used by the Markowitz pivot search of {@link SparseLU}, with the rows and the columns of the active
 * submatrix by their number of nonzeros. It is also what the degree lists of a proper approximate minimum
 * degree (AMD) ordering would need, to improve on the static degree ordering of {@link MinimumDegree}.
 * <p>
 * An item must not be inserted twice, and only an inserted item can be removed. While an item is inserted,
 * its {@link #count} must only change by removing and inserting it again. Otherwise the counts are free to be
 * used as plain counters.
 */
final class BucketQueue {

    /**
     * The first item with each count, or -1.
     */
    private final int[] myHead;
    private final int[] myNext;
    private final int[] myPrevious;
    /**
     * The count of each item.
     */
    final int[] count;

    BucketQueue(final int nbItems, final int maxCount) {

        super();

        count = new int[nbItems];
        myHead = new int[maxCount + 1];
        myNext = new int[nbItems];
        myPrevious = new int[nbItems];

        Arrays.fill(myHead, -1);
    }

    /**
     * Empty all buckets.
     */
    void clear() {
        Arrays.fill(myHead, -1);
    }

    /**
     * @return The first item with this count, or -1
     */
    int first(final int itemCount) {
        return myHead[itemCount];
    }

    /**
     * Add an item, with this count.
     */
    void insert(final int item, final int itemCount) {
        int head = myHead[itemCount];
        myNext[item] = head;
        myPrevious[item] = -1;
        if (head >= 0) {
            myPrevious[head] = item;
        }
        myHead[itemCount] = item;
        count[item] = itemCount;
    }

    /**
     * @return The next item with the same count, or -1
     */
    int next(final int item) {
        return myNext[item];
    }

    /**
     * Remove an item (it keeps its count).
     */
    void remove(final int item) {
        int previous = myPrevious[item];
        int next = myNext[item];
        if (previous >= 0) {
            myNext[previous] = next;
        } else {
            myHead[count[item]] = next;
        }
        if (next >= 0) {
            myPrevious[next] = previous;
        }
    }

}
