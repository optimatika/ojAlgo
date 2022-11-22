/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.data.domain.finance;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.integer.NextGenSysModTest;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.context.NumberContext;

/**
 * SymbolDataTest
 *
 * @author apete
 */
public class FinanceUtilsTest extends FinanceTests {

    private static final NumberContext ACCEPTABLE_ACCURACY = NumberContext.of(14, 14);
    private static final NumberContext IDENTICAL_ACCURACY = NumberContext.of(16, 16);

    private static void doTestCleaning(final double[][] rawOriginal) {

        MatrixR064 original = MatrixR064.FACTORY.rows(rawOriginal);

        SingularValue<Double> svd = SingularValue.PRIMITIVE.make(original);
        svd.decompose(original);
        double refCondition = svd.getCondition();
        int refRank = svd.getRank();

        MatrixR064 plainCorrelations = FinanceUtils.toCorrelations(original, false);
        MatrixR064 plainVolatilities = FinanceUtils.toVolatilities(original, false);
        MatrixR064 reconstructedPlain = FinanceUtils.toCovariances(plainVolatilities, plainCorrelations);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", original);
            BasicLogger.debugMatrix("Plain Correlations", plainCorrelations);
            BasicLogger.debugMatrix("Plain Volatilities", plainVolatilities);
            BasicLogger.debugMatrix("Reconstructed Plain", reconstructedPlain);
            BasicLogger.debugMatrix("Difference", original.subtract(reconstructedPlain), IDENTICAL_ACCURACY);
        }

        TestUtils.assertEquals(original, reconstructedPlain, IDENTICAL_ACCURACY);

        MatrixR064 cleanedCorrelations = FinanceUtils.toCorrelations(original, true);
        MatrixR064 cleanedVolatilities = FinanceUtils.toVolatilities(original, true);
        MatrixR064 reconstructedCleaned = FinanceUtils.toCovariances(cleanedVolatilities, cleanedCorrelations);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", original);
            BasicLogger.debugMatrix("Plain Correlations", cleanedCorrelations);
            BasicLogger.debugMatrix("Plain Volatilities", cleanedVolatilities);
            BasicLogger.debugMatrix("Reconstructed Plain", reconstructedCleaned);
            BasicLogger.debugMatrix("Difference", original.subtract(reconstructedCleaned), ACCEPTABLE_ACCURACY);
        }

        svd.decompose(reconstructedCleaned);
        double newCondition = svd.getCondition();
        int newRank = svd.getRank();

        TestUtils.assertTrue("Made the condition worse! " + refCondition + " => " + newCondition,
                newCondition <= refCondition || !ACCEPTABLE_ACCURACY.isDifferent(refCondition, newCondition));
        TestUtils.assertTrue("Made the rank worse!", newRank >= refRank);
        TestUtils.assertEquals("Not full rank!", rawOriginal.length, newRank);

        TestUtils.assertEquals(original, reconstructedPlain, ACCEPTABLE_ACCURACY);

        if (DEBUG) {
            BasicLogger.debugMatrix("Original", original);
            BasicLogger.debugMatrix("Cleaned", reconstructedCleaned);
            BasicLogger.debugMatrix("Difference", original.subtract(reconstructedCleaned), ACCEPTABLE_ACCURACY);
        }
    }

    public FinanceUtilsTest() {
        super();
    }

    @Test
    public void testCleaningCase010A() {
        FinanceUtilsTest.doTestCleaning(NextGenSysModTest.CASE_010A.getCovarianceMtrx());
    }

    @Test
    public void testCleaningCase020A() {
        FinanceUtilsTest.doTestCleaning(NextGenSysModTest.CASE_020A.getCovarianceMtrx());
    }

    @Test
    public void testCleaningCase030B() {
        FinanceUtilsTest.doTestCleaning(NextGenSysModTest.CASE_030B.getCovarianceMtrx());
    }

    @Test
    public void testCleaningCase040B() {
        FinanceUtilsTest.doTestCleaning(NextGenSysModTest.CASE_040B.getCovarianceMtrx());
    }

    @Test
    public void testCleaningCase050B() {
        FinanceUtilsTest.doTestCleaning(NextGenSysModTest.CASE_050B.getCovarianceMtrx());
    }

    @Test
    public void testCleaningNextGen50() {
        FinanceUtilsTest.doTestCleaning(DirtyCovarianceMatrices.NextGen50);
    }

    @Test
    public void testConversions() {

        for (int r = 10; r <= 20; r++) {

            final double tmpExpReturn = r / PrimitiveMath.HUNDRED;
            final double tmpExpFactor = FinanceUtils.toGrowthFactorFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);
            final double tmpExpRate = FinanceUtils.toGrowthRateFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);

            double tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthFactor(tmpExpFactor, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, 1E-14 / PrimitiveMath.THREE);

            tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthRate(tmpExpRate, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, 1E-14 / PrimitiveMath.THREE);

            TestUtils.assertEquals(tmpExpFactor, PrimitiveMath.EXP.invoke(tmpExpRate), 1E-14 / PrimitiveMath.THREE);
            TestUtils.assertEquals(tmpExpRate, PrimitiveMath.LOG.invoke(tmpExpFactor), 1E-14 / PrimitiveMath.THREE);
        }

    }
}
