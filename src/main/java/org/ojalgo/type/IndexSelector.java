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

import org.ojalgo.random.Uniform;

/**
 * An array of int:s (indices) that are partitioned to be either "included" or "excluded". If you need more
 * than 2 different states then {@link EnumPartition} is an alternative.
 *
 * @see EnumPartition
 * @author apete
 */
public final class IndexSelector {

    private transient int[] myExcluded = null;
    private int myExcludedLength;
    private transient int[] myIncluded = null;
    private int myIncludedLength;
    private int myLastExcluded;
    private int myLastIncluded;
    private final boolean[] mySelector;

    public IndexSelector(final int count) {

        super();

        mySelector = new boolean[count];

        myExcludedLength = count;
        myIncludedLength = 0;

        myLastExcluded = -1;
        myLastIncluded = -1;
    }

    public IndexSelector(final int count, final int[] initiallyIncludedIndeces) {

        this(count);

        this.include(initiallyIncludedIndeces);
    }

    @SuppressWarnings("unused")
    private IndexSelector() {
        this(0);
    }

    public int countExcluded() {
        return myExcludedLength;
    }

    public int countIncluded() {
        return myIncludedLength;
    }

    public void exclude(final int indexToExclude) {
        if (mySelector[indexToExclude]) {
            mySelector[indexToExclude] = false;
            myLastExcluded = indexToExclude;
            myExcludedLength++;
            myIncludedLength--;
        }
    }

    public void exclude(final int... indecesToExclude) {
        int tmpIndex;
        for (int i = 0; i < indecesToExclude.length; i++) {
            tmpIndex = indecesToExclude[i];
            if (0 <= tmpIndex && tmpIndex < mySelector.length) {
                this.exclude(tmpIndex);
            }
        }
    }

    public void excludeAll() {
        Arrays.fill(mySelector, false);
        myExcludedLength = mySelector.length;
        myIncludedLength = 0;
    }

    public int[] getExcluded() {

        if (myExcluded == null || myExcluded.length != myExcludedLength) {
            myExcluded = new int[myExcludedLength];
        }

        int j = 0;
        for (int i = 0; i < mySelector.length; i++) {
            if (!mySelector[i]) {
                myExcluded[j] = i;
                j++;
            }
        }

        return myExcluded;
    }

    public int[] getIncluded() {

        if (myIncluded == null || myIncluded.length != myIncludedLength) {
            myIncluded = new int[myIncludedLength];
        }

        int j = 0;
        for (int i = 0; i < mySelector.length; i++) {
            if (mySelector[i]) {
                myIncluded[j] = i;
                j++;
            }
        }

        return myIncluded;
    }

    public int getLastExcluded() {
        return myLastExcluded;
    }

    public int getLastIncluded() {
        return myLastIncluded;
    }

    /**
     * Randomly include 1 of the currently excluded
     */
    public void grow() {

        if (myExcludedLength > 0) {

            int tmpInclRef = Uniform.randomInteger(myExcludedLength);
            int tmpExclCount = -1;

            for (int i = 0; i < mySelector.length && tmpExclCount < tmpInclRef; i++) {
                if (!mySelector[i]) {
                    tmpExclCount++;
                }
                if (tmpExclCount == tmpInclRef) {
                    this.include(i);
                }
            }
        }
    }

    public void include(final int indexToInclude) {
        if (!mySelector[indexToInclude]) {
            mySelector[indexToInclude] = true;
            myLastIncluded = indexToInclude;
            myIncludedLength++;
            myExcludedLength--;
        }
    }

    public void include(final int... indecesToInclude) {
        int tmpIndex;
        for (int i = 0; i < indecesToInclude.length; i++) {
            tmpIndex = indecesToInclude[i];
            if (0 <= tmpIndex && tmpIndex < mySelector.length) {
                this.include(tmpIndex);
            }
        }
    }

    public void includeAll() {
        Arrays.fill(mySelector, true);
        myIncludedLength = mySelector.length;
        myExcludedLength = 0;
    }

    public boolean isExcluded(final int index) {
        return !mySelector[index];
    }

    public boolean isIncluded(final int index) {
        return mySelector[index];
    }

    /**
     * Is the last excluded index still excluded, or has it been included by a later operation?
     */
    public boolean isLastExcluded() {
        return !mySelector[myLastExcluded];
    }

    /**
     * Is the last included index still included, or has it been excluded by a later operation?
     */
    public boolean isLastIncluded() {
        return mySelector[myLastIncluded];
    }

    public void pivot(final int indexToExclude, final int indexToInclude) {
        this.exclude(indexToExclude);
        this.include(indexToInclude);
    }

    public void revertLastExclusion() {
        this.include(myLastExcluded);
    }

    public void revertLastInclusion() {
        this.exclude(myLastIncluded);
    }

    /**
     * Randomly exclude 1 of the currently included
     */
    public void shrink() {

        if (myIncludedLength > 0) {

            int tmpExclRef = Uniform.randomInteger(myIncludedLength);
            int tmpInclCount = -1;

            for (int i = 0; i < mySelector.length && tmpInclCount < tmpExclRef; i++) {
                if (mySelector[i]) {
                    tmpInclCount++;
                }
                if (tmpInclCount == tmpExclRef) {
                    this.exclude(i);
                }
            }
        }
    }

    /**
     * Randomly exclude 1 of the currently included and include 1 of the excluded
     */
    public void shuffle() {

        if (myIncludedLength > 0 && myExcludedLength > 0) {

            int tmpExclRef = Uniform.randomInteger(myIncludedLength);
            int tmpInclCount = -1;

            int tmpInclRef = Uniform.randomInteger(myExcludedLength);
            int tmpExclCount = -1;

            for (int i = 0; i < mySelector.length && (tmpInclCount < tmpExclRef || tmpExclCount < tmpInclRef); i++) {
                if (mySelector[i]) {
                    tmpInclCount++;
                } else {
                    tmpExclCount++;
                }
                if (tmpInclCount == tmpExclRef) {
                    this.exclude(i);
                } else if (tmpExclCount == tmpInclRef) {
                    this.include(i);
                }
            }
        }
    }

    public int size() {
        return mySelector.length;
    }

    @Override
    public String toString() {
        return "Last Incl/Excl: " + myLastIncluded + "/" + myLastExcluded + " => " + Arrays.toString(this.getIncluded()) + " / "
                + Arrays.toString(this.getExcluded());
    }
}
