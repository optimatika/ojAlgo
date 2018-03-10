/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.matrix.store.operation;

import org.ojalgo.OjAlgoUtils;
import org.ojalgo.array.blas.AXPY;

/**
 * @author apete
 */
public abstract class MatrixOperation {

    /**
     * Sets all matrix size operation thresholds to precisly this value.
     *
     * @param value The threshold
     */
    public static void setAllOperationThresholds(final int value) {
        MatrixOperation.setThresholdsMaxValue(value);
        MatrixOperation.setThresholdsMinValue(value);
    }

    /**
     * Will make sure no matrix size operation thresholds are larger than the supplied value. Existing smaller
     * values are unchanged.
     *
     * @param maxValue The max allowed value
     */
    public static void setThresholdsMaxValue(final int maxValue) {
        AggregateAll.THRESHOLD = Math.min(maxValue, AggregateAll.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.min(maxValue, ApplyCholesky.THRESHOLD);
        ApplyLU.THRESHOLD = Math.min(maxValue, ApplyLU.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.min(maxValue, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.min(maxValue, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.min(maxValue, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.min(maxValue, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.min(maxValue, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.min(maxValue, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.min(maxValue, HouseholderRight.THRESHOLD);
        AXPY.THRESHOLD = Math.min(maxValue, AXPY.THRESHOLD);
        ModifyAll.THRESHOLD = Math.min(maxValue, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.min(maxValue, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.min(maxValue, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.min(maxValue, MultiplyLeft.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.min(maxValue, MultiplyRight.THRESHOLD);
        RotateLeft.THRESHOLD = Math.min(maxValue, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.min(maxValue, RotateRight.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.min(maxValue, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.min(maxValue, SubstituteForwards.THRESHOLD);
    }

    /**
     * Will make sure all matrix size operation thresholds are at least as large as the supplied value.
     * Existing larger values are unchanged.
     *
     * @param minValue The min allowed value
     */
    public static void setThresholdsMinValue(final int minValue) {
        AggregateAll.THRESHOLD = Math.max(minValue, AggregateAll.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.max(minValue, ApplyCholesky.THRESHOLD);
        ApplyLU.THRESHOLD = Math.max(minValue, ApplyLU.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.max(minValue, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.max(minValue, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.max(minValue, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.max(minValue, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.max(minValue, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.max(minValue, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.max(minValue, HouseholderRight.THRESHOLD);
        AXPY.THRESHOLD = Math.max(minValue, AXPY.THRESHOLD);
        ModifyAll.THRESHOLD = Math.max(minValue, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.max(minValue, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.max(minValue, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.max(minValue, MultiplyLeft.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.max(minValue, MultiplyRight.THRESHOLD);
        RotateLeft.THRESHOLD = Math.max(minValue, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.max(minValue, RotateRight.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.max(minValue, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.max(minValue, SubstituteForwards.THRESHOLD);
    }

    protected MatrixOperation() {
        super();
    }

    public abstract int threshold();

    public int workers() {
        return OjAlgoUtils.ENVIRONMENT.threads;
    }

}
