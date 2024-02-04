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
package org.ojalgo.matrix.decomposition;

import org.ojalgo.structure.Structure1D;

final class Pivot {

    private boolean myModified;
    private int[] myOrder;
    private int mySign;

    Pivot() {
        super();
    }

    void change(final int ind1, final int ind2) {

        if (ind1 != ind2) {

            int tmpRow = myOrder[ind1];
            myOrder[ind1] = myOrder[ind2];
            myOrder[ind2] = tmpRow;

            mySign = -mySign;

            myModified = true;

        } else {
            // Why?!
        }
    }

    int[] reverseOrder() {
        int[] inverse = new int[myOrder.length];
        for (int i = 0; i < myOrder.length; i++) {
            inverse[myOrder[i]] = i;
        }
        return inverse;
    }

    int[] getOrder() {
        return myOrder;
    }

    boolean isModified() {
        return myModified;
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
    }

    int signum() {
        return mySign;
    }

}
