/*
 * Copyright 1997-2021 Optimatika
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
package org.ojalgo.matrix.operation;

import org.ojalgo.array.operation.*;

public interface MatrixOperation extends ArrayOperation {

    /**
     * Sets all matrix size operation thresholds to precisly this value.
     *
     * @param value The threshold
     */
    static void setAllOperationThresholds(final int value) {
        MatrixOperation.setThresholdsMaxValue(value);
        MatrixOperation.setThresholdsMinValue(value);
    }

    /**
     * Will make sure no matrix size operation thresholds are larger than the supplied value. Existing smaller
     * values are unchanged.
     *
     * @param value The max allowed value
     */
    static void setThresholdsMaxValue(final int value) {
        AggregateAll.THRESHOLD = Math.min(value, AggregateAll.THRESHOLD);
        AMAX.THRESHOLD = Math.min(value, AMAX.THRESHOLD);
        AMIN.THRESHOLD = Math.min(value, AMIN.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.min(value, ApplyCholesky.THRESHOLD);
        ApplyLDL.THRESHOLD = Math.min(value, ApplyLDL.THRESHOLD);
        ApplyLU.THRESHOLD = Math.min(value, ApplyLU.THRESHOLD);
        ASUM.THRESHOLD = Math.min(value, ASUM.THRESHOLD);
        AXPY.THRESHOLD = Math.min(value, AXPY.THRESHOLD);
        CorePrimitiveOperation.THRESHOLD = Math.min(value, CorePrimitiveOperation.THRESHOLD);
        CABS1.THRESHOLD = Math.min(value, CABS1.THRESHOLD);
        COPY.THRESHOLD = Math.min(value, COPY.THRESHOLD);
        DOT.THRESHOLD = Math.min(value, DOT.THRESHOLD);
        DOTC.THRESHOLD = Math.min(value, DOTC.THRESHOLD);
        DOTU.THRESHOLD = Math.min(value, DOTU.THRESHOLD);
        FillAll.THRESHOLD = Math.min(value, FillAll.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.min(value, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.min(value, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.min(value, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.min(value, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.min(value, HermitianRank2Update.THRESHOLD);
        // HouseholderHermitian
        HouseholderLeft.THRESHOLD = Math.min(value, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.min(value, HouseholderRight.THRESHOLD);
        IndexOf.THRESHOLD = Math.min(value, IndexOf.THRESHOLD);
        ModifyAll.THRESHOLD = Math.min(value, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.min(value, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.min(value, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.min(value, MultiplyLeft.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.min(value, MultiplyNeither.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.min(value, MultiplyRight.THRESHOLD);
        NRM2.THRESHOLD = Math.min(value, NRM2.THRESHOLD);
        OperationBinary.THRESHOLD = Math.min(value, OperationBinary.THRESHOLD);
        OperationParameter.THRESHOLD = Math.min(value, OperationParameter.THRESHOLD);
        OperationUnary.THRESHOLD = Math.min(value, OperationUnary.THRESHOLD);
        OperationVoid.THRESHOLD = Math.min(value, OperationVoid.THRESHOLD);
        ROT.THRESHOLD = Math.min(value, ROT.THRESHOLD);
        RotateLeft.THRESHOLD = Math.min(value, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.min(value, RotateRight.THRESHOLD);
        ROTG.THRESHOLD = Math.min(value, ROTG.THRESHOLD);
        ROTM.THRESHOLD = Math.min(value, ROTM.THRESHOLD);
        ROTMG.THRESHOLD = Math.min(value, ROTMG.THRESHOLD);
        SCAL.THRESHOLD = Math.min(value, SCAL.THRESHOLD);
        SDOT.THRESHOLD = Math.min(value, SDOT.THRESHOLD);
        SortAll.THRESHOLD = Math.min(value, SortAll.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.min(value, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.min(value, SubstituteForwards.THRESHOLD);
        SWAP.THRESHOLD = Math.min(value, SWAP.THRESHOLD);
        VisitAll.THRESHOLD = Math.min(value, VisitAll.THRESHOLD);
    }

    /**
     * Will make sure all matrix size operation thresholds are at least as large as the supplied value.
     * Existing larger values are unchanged.
     *
     * @param value The min allowed value
     */
    static void setThresholdsMinValue(final int value) {
        AggregateAll.THRESHOLD = Math.max(value, AggregateAll.THRESHOLD);
        AMAX.THRESHOLD = Math.max(value, AMAX.THRESHOLD);
        AMIN.THRESHOLD = Math.max(value, AMIN.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.max(value, ApplyCholesky.THRESHOLD);
        ApplyLDL.THRESHOLD = Math.max(value, ApplyLDL.THRESHOLD);
        ApplyLU.THRESHOLD = Math.max(value, ApplyLU.THRESHOLD);
        ASUM.THRESHOLD = Math.max(value, ASUM.THRESHOLD);
        AXPY.THRESHOLD = Math.max(value, AXPY.THRESHOLD);
        CorePrimitiveOperation.THRESHOLD = Math.max(value, CorePrimitiveOperation.THRESHOLD);
        CABS1.THRESHOLD = Math.max(value, CABS1.THRESHOLD);
        COPY.THRESHOLD = Math.max(value, COPY.THRESHOLD);
        DOT.THRESHOLD = Math.max(value, DOT.THRESHOLD);
        DOTC.THRESHOLD = Math.max(value, DOTC.THRESHOLD);
        DOTU.THRESHOLD = Math.max(value, DOTU.THRESHOLD);
        FillAll.THRESHOLD = Math.max(value, FillAll.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.max(value, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.max(value, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.max(value, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.max(value, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.max(value, HermitianRank2Update.THRESHOLD);
        // HouseholderHermitian
        HouseholderLeft.THRESHOLD = Math.max(value, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.max(value, HouseholderRight.THRESHOLD);
        IndexOf.THRESHOLD = Math.max(value, IndexOf.THRESHOLD);
        ModifyAll.THRESHOLD = Math.max(value, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.max(value, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.max(value, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.max(value, MultiplyLeft.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.max(value, MultiplyNeither.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.max(value, MultiplyRight.THRESHOLD);
        NRM2.THRESHOLD = Math.max(value, NRM2.THRESHOLD);
        OperationBinary.THRESHOLD = Math.max(value, OperationBinary.THRESHOLD);
        OperationParameter.THRESHOLD = Math.max(value, OperationParameter.THRESHOLD);
        OperationUnary.THRESHOLD = Math.max(value, OperationUnary.THRESHOLD);
        OperationVoid.THRESHOLD = Math.max(value, OperationVoid.THRESHOLD);
        ROT.THRESHOLD = Math.max(value, ROT.THRESHOLD);
        RotateLeft.THRESHOLD = Math.max(value, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.max(value, RotateRight.THRESHOLD);
        ROTG.THRESHOLD = Math.max(value, ROTG.THRESHOLD);
        ROTM.THRESHOLD = Math.max(value, ROTM.THRESHOLD);
        ROTMG.THRESHOLD = Math.max(value, ROTMG.THRESHOLD);
        SCAL.THRESHOLD = Math.max(value, SCAL.THRESHOLD);
        SDOT.THRESHOLD = Math.max(value, SDOT.THRESHOLD);
        SortAll.THRESHOLD = Math.max(value, SortAll.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.max(value, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.max(value, SubstituteForwards.THRESHOLD);
        SWAP.THRESHOLD = Math.max(value, SWAP.THRESHOLD);
        VisitAll.THRESHOLD = Math.max(value, VisitAll.THRESHOLD);
    }

}
