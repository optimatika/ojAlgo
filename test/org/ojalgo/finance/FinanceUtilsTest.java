/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
import org.ojalgo.type.CalendarDateUnit;

/**
 * SymbolDataTest
 *
 * @author apete
 */
public class FinanceUtilsTest extends FinanceTests {

    public FinanceUtilsTest() {
        super();
    }

    public FinanceUtilsTest(final String name) {
        super(name);
    }

    public void testConversions() {

        for (int r = 10; r <= 20; r++) {

            final double tmpExpReturn = r / PrimitiveMath.HUNDRED;
            final double tmpExpFactor = FinanceUtils.toGrowthFactorFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);
            final double tmpExpRate = FinanceUtils.toGrowthRateFromAnnualReturn(tmpExpReturn, CalendarDateUnit.MONTH);

            double tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthFactor(tmpExpFactor, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, PrimitiveMath.IS_ZERO);

            tmpActReturn = FinanceUtils.toAnnualReturnFromGrowthRate(tmpExpRate, CalendarDateUnit.MONTH);
            TestUtils.assertEquals(tmpExpReturn, tmpActReturn, PrimitiveMath.IS_ZERO);

            TestUtils.assertEquals(tmpExpFactor, Math.exp(tmpExpRate), PrimitiveMath.IS_ZERO);
            TestUtils.assertEquals(tmpExpRate, Math.log(tmpExpFactor), PrimitiveMath.IS_ZERO);
        }

    }
}
