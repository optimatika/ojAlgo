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
package org.ojalgo.matrix.decomposition;

import java.util.Arrays;
import java.util.BitSet;

import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure1D;

final class Pivot {

    private static int[] doReverse(final int[] original, final int[] inverse) {
        for (int i = 0; i < original.length; i++) {
            inverse[original[i]] = i;
        }
        return inverse;
    }

    static int[] reverse(final int[] original) {
        int[] inverse = new int[original.length];
        return Pivot.doReverse(original, inverse);
    }

    private final BitSet myDoneBits = new BitSet();
    private boolean myModified;
    private int[] myOrder;
    private int[] myReverse;
    private boolean myReverseNeedsUpdate;
    private int mySign;

    Pivot() {
        super();
    }

    Pivot(final int... order) {
        super();
        this.reset(order.length);
        System.arraycopy(order, 0, myOrder, 0, order.length);
        myModified = true;
        myReverseNeedsUpdate = true;
        // TODO How to set mySign?
    }

    @Override
    public String toString() {
        return Arrays.toString(myOrder) + "/" + Arrays.toString(this.reverseOrder());
    }

    private <N extends Comparable<N>, M extends Access2D<N> & Mutate2D> void followPermutationCycles(final M elements, final int[] order) {

        myDoneBits.clear();
        int dim = order.length;

        for (int i = 0; i < dim; i++) {
            if (!myDoneBits.get(i)) {
                // Follow the cycle starting at i
                int j = i;
                double temp = elements.doubleValue(i);

                while (!myDoneBits.get(j)) {
                    myDoneBits.set(j);
                    int next = order[j];
                    if (next != j) {
                        double val = elements.doubleValue(next);
                        elements.set(next, temp);
                        temp = val;
                        j = next;
                    }
                }
            }
        }
    }

    /**
     * Equivalent to selecting the rows (or columns) in the pivot order,
     * <code>arg.rows(pivot.getOrder())</code>.
     */
    <N extends Comparable<N>, M extends Access2D<N> & Mutate2D> void applyPivotOrder(final M arg) {
        if (this.isModified()) {
            this.followPermutationCycles(arg, this.reverseOrder());
        }
    }

    /**
     * Equivalent to selecting the rows (or columns) in the reverse order,
     * <code>arg.rows(pivot.reverseOrder())</code>.
     */
    <N extends Comparable<N>, M extends Access2D<N> & Mutate2D> void applyReverseOrder(final M arg) {
        if (this.isModified()) {
            this.followPermutationCycles(arg, myOrder);
        }
    }

    void change(final int ind1, final int ind2) {

        if (ind1 != ind2) {

            int tmpRow = myOrder[ind1];
            myOrder[ind1] = myOrder[ind2];
            myOrder[ind2] = tmpRow;

            mySign = -mySign;

            myModified = true;
            myReverseNeedsUpdate = true;

        } else {
            // Why?!
        }
    }

    /**
     * Performs a cycle permutation on the pivot order.
     * <p>
     * This method applies a cycle permutation that moves the element at position ind1 to position ind2, while
     * shifting all elements in between one position to the left.
     * </p>
     * <p>
     * The method only performs the cycle if ind1 is less than ind2. If ind1 is greater than or equal to ind2,
     * no changes are made to the pivot order.
     * </p>
     *
     * @param ind1 The starting index of the cycle
     * @param ind2 The ending index of the cycle
     */
    void cycle(final int ind1, final int ind2) {

        if (ind1 < ind2) {
            for (int j = ind1; j < ind2; j++) {
                this.change(j, j + 1);
            }
        }
    }

    int[] getOrder() {
        return myOrder;
    }

    boolean isModified() {
        return myModified;
    }

    int locationOf(final int original) {
        int[] reverseOrder = this.reverseOrder();
        return reverseOrder[original];
    }

    void reset(final int numberOf) {

        if ((myOrder == null) || (myOrder.length != numberOf)) {
            myOrder = Structure1D.newIncreasingRange(0, numberOf);
        } else {
            for (int i = 0; i < myOrder.length; i++) {
                myOrder[i] = i;
            }
        }

        myModified = false;
        mySign = 1;
        myReverseNeedsUpdate = true;
    }

    int[] reverseOrder() {
        if (myReverse == null || myReverse.length != myOrder.length) {
            myReverse = new int[myOrder.length];
            myReverseNeedsUpdate = true;
        }
        if (myReverseNeedsUpdate) {
            Pivot.doReverse(myOrder, myReverse);
            myReverseNeedsUpdate = false;
        }
        return myReverse;
    }

    int signum() {
        return mySign;
    }

}
