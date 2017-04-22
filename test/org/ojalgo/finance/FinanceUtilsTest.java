/*
 * Copyright 1997-2017 Optimatika (www.optimatika.se)
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
package org.ojalgo.finance;

import org.ojalgo.TestUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * SymbolDataTest
 *
 * @author apete
 */
public class FinanceUtilsTest extends FinanceTests {

    private static void doTestCleaning(final double[][] original) {

        final NumberContext tmpEvalCntx = NumberContext.getGeneral(6, 12);

        final PrimitiveDenseStore tmpOriginal = PrimitiveDenseStore.FACTORY.rows(original);

        final SingularValue<Double> tmpSVD = SingularValue.make(tmpOriginal);

        tmpSVD.decompose(tmpOriginal);
        final double tmpRefCond = tmpSVD.getCondition();
        final int tmpRefRank = tmpSVD.getRank();
        final double tmpRefNorm = tmpSVD.getFrobeniusNorm();

        final PrimitiveMatrix tmpCorrelations = FinanceUtils.toCorrelations(tmpOriginal, true);
        final PrimitiveMatrix tmpVolatilities = FinanceUtils.toVolatilities(tmpOriginal, true);
        final PrimitiveMatrix tmpCovariances = FinanceUtils.toCovariances(tmpVolatilities, tmpCorrelations);

        tmpSVD.decompose(PrimitiveDenseStore.FACTORY.copy(tmpCovariances));
        final double tmpNewCond = tmpSVD.getCondition();
        final int tmpNewRank = tmpSVD.getRank();
        final double tmpNewNorm = tmpSVD.getFrobeniusNorm();

        TestUtils.assertTrue("Improved the condition", tmpNewCond <= tmpRefCond);
        TestUtils.assertTrue("Improved the rank", tmpNewRank >= tmpRefRank);
        TestUtils.assertEquals("Full rank", original.length, tmpNewRank);
        TestUtils.assertEquals("Roughly the same frob norm", tmpRefNorm, tmpNewNorm, tmpEvalCntx);

        if (DEBUG) {
            BasicLogger.debug("Original", tmpOriginal);
            BasicLogger.debug("Cleaned", tmpCovariances);
            BasicLogger.debug("Difference", tmpOriginal.subtract(PrimitiveDenseStore.FACTORY.copy(tmpCovariances)), tmpEvalCntx);
        }

    }

    public FinanceUtilsTest() {
        super();
    }

    public FinanceUtilsTest(final String name) {
        super(name);
    }

    public void testCleaningNextGen50() {
        FinanceUtilsTest.doTestCleaning(DirtyCovarianceMatrices.NextGen50);
    }

    public void testConversions() {

        for (int r = 10; r <= 20; r++) {

            final double tmpExpReturn = r / PrimitiveMath.HUNDRED;
            final double tmpExpFactor = FinanceUtils.toGrowthFactorFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);
            final double tmpExpRate = FinanceUtils.toGrowthRateFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);

            double tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthFactor(tmpExpFactor, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, 1E-14 / PrimitiveMath.THREE);

            tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthRate(tmpExpRate, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, 1E-14 / PrimitiveMath.THREE);

            TestUtils.assertEquals(tmpExpFactor, PrimitiveFunction.EXP.invoke(tmpExpRate), 1E-14 / PrimitiveMath.THREE);
            TestUtils.assertEquals(tmpExpRate, PrimitiveFunction.LOG.invoke(tmpExpFactor), 1E-14 / PrimitiveMath.THREE);
        }

    }
}
