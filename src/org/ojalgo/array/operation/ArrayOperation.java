/*
 * Copyright 1997-2019 Optimatika
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
package org.ojalgo.array.operation;

/**
 * @author apete
 */
public interface ArrayOperation {

    /**
     * Sets all matrix size operation thresholds to precisly this value.
     *
     * @param value The threshold
     */
    static void setAllOperationThresholds(final int value) {
        ArrayOperation.setThresholdsMaxValue(value);
        ArrayOperation.setThresholdsMinValue(value);
    }

    /**
     * Will make sure no matrix size operation thresholds are larger than the supplied value. Existing smaller
     * values are unchanged.
     *
     * @param value The max allowed value
     */
    static void setThresholdsMaxValue(final int value) {
        AggregateAll.THRESHOLD = Math.min(value, AggregateAll.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.min(value, ApplyCholesky.THRESHOLD);
        ApplyLU.THRESHOLD = Math.min(value, ApplyLU.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.min(value, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.min(value, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.min(value, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.min(value, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.min(value, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.min(value, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.min(value, HouseholderRight.THRESHOLD);
        AXPY.THRESHOLD = Math.min(value, AXPY.THRESHOLD);
        ModifyAll.THRESHOLD = Math.min(value, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.min(value, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.min(value, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.min(value, MultiplyNeither.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.min(value, MultiplyLeft.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.min(value, MultiplyRight.THRESHOLD);
        RotateLeft.THRESHOLD = Math.min(value, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.min(value, RotateRight.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.min(value, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.min(value, SubstituteForwards.THRESHOLD);
    }

    /**
     * Will make sure all matrix size operation thresholds are at least as large as the supplied value.
     * Existing larger values are unchanged.
     *
     * @param value The min allowed value
     */
    static void setThresholdsMinValue(final int value) {
        AggregateAll.THRESHOLD = Math.max(value, AggregateAll.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.max(value, ApplyCholesky.THRESHOLD);
        ApplyLU.THRESHOLD = Math.max(value, ApplyLU.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.max(value, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.max(value, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.max(value, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.max(value, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.max(value, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.max(value, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.max(value, HouseholderRight.THRESHOLD);
        AXPY.THRESHOLD = Math.max(value, AXPY.THRESHOLD);
        ModifyAll.THRESHOLD = Math.max(value, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.max(value, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.max(value, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.max(value, MultiplyNeither.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.max(value, MultiplyLeft.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.max(value, MultiplyRight.THRESHOLD);
        RotateLeft.THRESHOLD = Math.max(value, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.max(value, RotateRight.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.max(value, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.max(value, SubstituteForwards.THRESHOLD);
    }

    int threshold();

}
