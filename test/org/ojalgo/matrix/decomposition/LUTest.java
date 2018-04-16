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
package org.ojalgo.matrix.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.P20061119Case;
import org.ojalgo.matrix.RationalMatrix;
import org.ojalgo.matrix.store.ComplexDenseStore;
import org.ojalgo.matrix.store.GenericDenseStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.type.context.NumberContext;

/**
 * @author apete
 */
public class LUTest {

    @BeforeEach
    public void minimiseAllBranchLimits() {
        TestUtils.minimiseAllBranchLimits();
    }

    @Test
    public void testP20061119Case() {

        final RationalMatrix tmpProblematic = P20061119Case.getProblematic();

        final LU<RationalNumber> tmpBig = LU.RATIONAL.make();
        tmpBig.decompose(GenericDenseStore.RATIONAL.copy(tmpProblematic));

        final LU<ComplexNumber> tmpComplex = LU.COMPLEX.make();
        tmpComplex.decompose(ComplexDenseStore.FACTORY.copy(tmpProblematic));

        final LU<Double> tmpPrimitive = LU.PRIMITIVE.make();
        tmpPrimitive.decompose(PrimitiveDenseStore.FACTORY.copy(tmpProblematic));

        final LU<Double> tmpJama = new RawLU();
        tmpJama.decompose(PrimitiveDenseStore.FACTORY.copy(tmpProblematic));

        final NumberContext tmpPrintContext = NumberContext.getGeneral(20);

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big L", tmpBig.getL(), tmpPrintContext);
            BasicLogger.debug("Complex L", tmpComplex.getL(), tmpPrintContext);
            BasicLogger.debug("Primitive L", tmpPrimitive.getL(), tmpPrintContext);
            BasicLogger.debug("Jama L", tmpJama.getL(), tmpPrintContext);
        }

        if (MatrixDecompositionTests.DEBUG) {
            BasicLogger.debug("Big U", tmpBig.getU(), tmpPrintContext);
            BasicLogger.debug("Complex U", tmpComplex.getU(), tmpPrintContext);
            BasicLogger.debug("Primitive U", tmpPrimitive.getU(), tmpPrintContext);
            BasicLogger.debug("Jama U", tmpJama.getU(), tmpPrintContext);
        }

        final SingularValue<Double> tmpSVD = new RawSingularValue();
        tmpSVD.decompose(PrimitiveDenseStore.FACTORY.copy(tmpProblematic));

        TestUtils.assertEquals("LU.rank SVD vs Big", tmpSVD.getRank(), tmpBig.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Complex", tmpSVD.getRank(), tmpComplex.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Primitive", tmpSVD.getRank(), tmpPrimitive.getRank());
        TestUtils.assertEquals("LU.rank SVD vs Jama", tmpSVD.getRank(), tmpJama.getRank());

    }

}
