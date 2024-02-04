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
package org.ojalgo.matrix.operation;

import org.ojalgo.array.DenseArray;
import org.ojalgo.array.operation.*;

/**
 * <p>
 * Contents in this package loosely corresponds to BLAS. The exact selection of operations and their API:s are
 * entirely dictated by the requirements of the various {@linkplain org.ojalgo.matrix.store.MatrixStore}
 * implementations.
 * </p>
 * <ul>
 * <li>http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms</li>
 * <li>http://www.netlib.org/blas/</li>
 * <li>http://www.netlib.org/blas/faq.html</li>
 * <li>http://www.netlib.org/lapack/lug/node145.html</li>
 * </ul>
 * Basic Linear Algebra Subprograms (BLAS) Level 1 contains vector operations.
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms#Level_1">BLAS Level 1 @
 * WikipediA</a></li>
 * <li><a href="http://www.netlib.org/blas/#_level_1">BLAS Level 1 @ Netlib</a></li>
 * <li><a href="https://software.intel.com/en-us/node/520730">BLAS Level 1 @ Intel</a></li>
 * </ul>
 * For each operation there should be 2 sets of implementations:
 * <ol>
 * <li>Optimised to be the implementations for the {@linkplain DenseArray} instances.</li>
 * <li>Optimised to be building blocks for higher level algorithms</li>
 * </ol>
 * Basic Linear Algebra Subprograms (BLAS) Level 2 contains matrix-vector operations.
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms#Level_2">BLAS Level 2 @
 * WikipediA</a></li>
 * <li><a href="http://www.netlib.org/blas/#_level_2">BLAS Level 2 @ Netlib</a></li>
 * <li><a href="https://software.intel.com/en-us/node/520748">BLAS Level 2 @ Intel</a></li>
 * </ul>
 * Basic Linear Algebra Subprograms (BLAS) Level 3 contains matrix-matrix operations.
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Basic_Linear_Algebra_Subprograms#Level_3">BLAS Level 3 @
 * WikipediA</a></li>
 * <li><a href="http://www.netlib.org/blas/#_level_3">BLAS Level 3 @ Netlib</a></li>
 * <li><a href="https://software.intel.com/en-us/node/520774">BLAS Level 3 @ Intel</a></li>
 * </ul>
 *
 * @author apete
 */
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
     * @param max The max allowed value
     */
    static void setThresholdsMaxValue(final int max) {
        AggregateAll.THRESHOLD = Math.min(max, AggregateAll.THRESHOLD);
        AMAX.THRESHOLD = Math.min(max, AMAX.THRESHOLD);
        AMIN.THRESHOLD = Math.min(max, AMIN.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.min(max, ApplyCholesky.THRESHOLD);
        ApplyLDL.THRESHOLD = Math.min(max, ApplyLDL.THRESHOLD);
        ApplyLU.THRESHOLD = Math.min(max, ApplyLU.THRESHOLD);
        ASUM.THRESHOLD = Math.min(max, ASUM.THRESHOLD);
        AXPY.THRESHOLD = Math.min(max, AXPY.THRESHOLD);
        CorePrimitiveOperation.THRESHOLD = Math.min(max, CorePrimitiveOperation.THRESHOLD);
        CABS1.THRESHOLD = Math.min(max, CABS1.THRESHOLD);
        COPY.THRESHOLD = Math.min(max, COPY.THRESHOLD);
        DOT.THRESHOLD = Math.min(max, DOT.THRESHOLD);
        DOTC.THRESHOLD = Math.min(max, DOTC.THRESHOLD);
        DOTU.THRESHOLD = Math.min(max, DOTU.THRESHOLD);
        FillAll.THRESHOLD = Math.min(max, FillAll.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.min(max, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.min(max, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.min(max, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.min(max, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.min(max, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.min(max, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.min(max, HouseholderRight.THRESHOLD);
        IndexOf.THRESHOLD = Math.min(max, IndexOf.THRESHOLD);
        ModifyAll.THRESHOLD = Math.min(max, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.min(max, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.min(max, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.min(max, MultiplyLeft.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.min(max, MultiplyNeither.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.min(max, MultiplyRight.THRESHOLD);
        OperationBinary.THRESHOLD = Math.min(max, OperationBinary.THRESHOLD);
        OperationParameter.THRESHOLD = Math.min(max, OperationParameter.THRESHOLD);
        OperationUnary.THRESHOLD = Math.min(max, OperationUnary.THRESHOLD);
        OperationVoid.THRESHOLD = Math.min(max, OperationVoid.THRESHOLD);
        ROT.THRESHOLD = Math.min(max, ROT.THRESHOLD);
        RotateLeft.THRESHOLD = Math.min(max, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.min(max, RotateRight.THRESHOLD);
        ROTG.THRESHOLD = Math.min(max, ROTG.THRESHOLD);
        ROTM.THRESHOLD = Math.min(max, ROTM.THRESHOLD);
        ROTMG.THRESHOLD = Math.min(max, ROTMG.THRESHOLD);
        SCAL.THRESHOLD = Math.min(max, SCAL.THRESHOLD);
        SDOT.THRESHOLD = Math.min(max, SDOT.THRESHOLD);
        SortAll.THRESHOLD = Math.min(max, SortAll.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.min(max, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.min(max, SubstituteForwards.THRESHOLD);
        SWAP.THRESHOLD = Math.min(max, SWAP.THRESHOLD);
        VisitAll.THRESHOLD = Math.min(max, VisitAll.THRESHOLD);
    }

    /**
     * Will make sure all matrix size operation thresholds are at least as large as the supplied value.
     * Existing larger values are unchanged.
     *
     * @param min The min allowed value
     */
    static void setThresholdsMinValue(final int min) {
        AggregateAll.THRESHOLD = Math.max(min, AggregateAll.THRESHOLD);
        AMAX.THRESHOLD = Math.max(min, AMAX.THRESHOLD);
        AMIN.THRESHOLD = Math.max(min, AMIN.THRESHOLD);
        ApplyCholesky.THRESHOLD = Math.max(min, ApplyCholesky.THRESHOLD);
        ApplyLDL.THRESHOLD = Math.max(min, ApplyLDL.THRESHOLD);
        ApplyLU.THRESHOLD = Math.max(min, ApplyLU.THRESHOLD);
        ASUM.THRESHOLD = Math.max(min, ASUM.THRESHOLD);
        AXPY.THRESHOLD = Math.max(min, AXPY.THRESHOLD);
        CorePrimitiveOperation.THRESHOLD = Math.max(min, CorePrimitiveOperation.THRESHOLD);
        CABS1.THRESHOLD = Math.max(min, CABS1.THRESHOLD);
        COPY.THRESHOLD = Math.max(min, COPY.THRESHOLD);
        DOT.THRESHOLD = Math.max(min, DOT.THRESHOLD);
        DOTC.THRESHOLD = Math.max(min, DOTC.THRESHOLD);
        DOTU.THRESHOLD = Math.max(min, DOTU.THRESHOLD);
        FillAll.THRESHOLD = Math.max(min, FillAll.THRESHOLD);
        FillMatchingDual.THRESHOLD = Math.max(min, FillMatchingDual.THRESHOLD);
        FillMatchingSingle.THRESHOLD = Math.max(min, FillMatchingSingle.THRESHOLD);
        GenerateApplyAndCopyHouseholderColumn.THRESHOLD = Math.max(min, GenerateApplyAndCopyHouseholderColumn.THRESHOLD);
        GenerateApplyAndCopyHouseholderRow.THRESHOLD = Math.max(min, GenerateApplyAndCopyHouseholderRow.THRESHOLD);
        HermitianRank2Update.THRESHOLD = Math.max(min, HermitianRank2Update.THRESHOLD);
        HouseholderLeft.THRESHOLD = Math.max(min, HouseholderLeft.THRESHOLD);
        HouseholderRight.THRESHOLD = Math.max(min, HouseholderRight.THRESHOLD);
        IndexOf.THRESHOLD = Math.max(min, IndexOf.THRESHOLD);
        ModifyAll.THRESHOLD = Math.max(min, ModifyAll.THRESHOLD);
        MultiplyBoth.THRESHOLD = Math.max(min, MultiplyBoth.THRESHOLD);
        MultiplyHermitianAndVector.THRESHOLD = Math.max(min, MultiplyHermitianAndVector.THRESHOLD);
        MultiplyLeft.THRESHOLD = Math.max(min, MultiplyLeft.THRESHOLD);
        MultiplyNeither.THRESHOLD = Math.max(min, MultiplyNeither.THRESHOLD);
        MultiplyRight.THRESHOLD = Math.max(min, MultiplyRight.THRESHOLD);
        OperationBinary.THRESHOLD = Math.max(min, OperationBinary.THRESHOLD);
        OperationParameter.THRESHOLD = Math.max(min, OperationParameter.THRESHOLD);
        OperationUnary.THRESHOLD = Math.max(min, OperationUnary.THRESHOLD);
        OperationVoid.THRESHOLD = Math.max(min, OperationVoid.THRESHOLD);
        ROT.THRESHOLD = Math.max(min, ROT.THRESHOLD);
        RotateLeft.THRESHOLD = Math.max(min, RotateLeft.THRESHOLD);
        RotateRight.THRESHOLD = Math.max(min, RotateRight.THRESHOLD);
        ROTG.THRESHOLD = Math.max(min, ROTG.THRESHOLD);
        ROTM.THRESHOLD = Math.max(min, ROTM.THRESHOLD);
        ROTMG.THRESHOLD = Math.max(min, ROTMG.THRESHOLD);
        SCAL.THRESHOLD = Math.max(min, SCAL.THRESHOLD);
        SDOT.THRESHOLD = Math.max(min, SDOT.THRESHOLD);
        SortAll.THRESHOLD = Math.max(min, SortAll.THRESHOLD);
        SubstituteBackwards.THRESHOLD = Math.max(min, SubstituteBackwards.THRESHOLD);
        SubstituteForwards.THRESHOLD = Math.max(min, SubstituteForwards.THRESHOLD);
        SWAP.THRESHOLD = Math.max(min, SWAP.THRESHOLD);
        VisitAll.THRESHOLD = Math.max(min, VisitAll.THRESHOLD);
    }

}
