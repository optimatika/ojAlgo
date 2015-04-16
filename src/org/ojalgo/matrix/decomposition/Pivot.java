/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.access.AccessUtils;

final class Pivot {

    private boolean myModified = false;
    private final int[] myOrder;
    private int mySign;

    Pivot(final int numberOfRows) {

        super();

        myOrder = AccessUtils.makeIncreasingRange(0, numberOfRows);
        mySign = 1;
    }

    public void change(final int row1, final int row2) {

        if (row1 != row2) {

            final int tmpRow = myOrder[row1];
            myOrder[row1] = myOrder[row2];
            myOrder[row2] = tmpRow;

            mySign = -mySign;

            myModified = true;

        } else {
            // Why?!
        }
    }

    public int[] getOrder() {
        return myOrder;
    }

    public boolean isModified() {
        return myModified;
    }

    public int signum() {
        return mySign;
    }

}
