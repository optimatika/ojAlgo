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
package org.ojalgo.type.context;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

/**
 * NumberContextTest
 *
 * @author apete
 */
public class NumberContextTest {

    @Test
    public void testPercentContext() {

        final NumberContext tmpNC = NumberContext.getPercent(Locale.US);

        TestUtils.assertEquals(7, tmpNC.getPrecision());
        TestUtils.assertEquals(4, tmpNC.getScale());
        TestUtils.assertEquals(RoundingMode.HALF_EVEN, tmpNC.getRoundingMode());

        final String tmpOriginalString = "123.4567";
        final BigDecimal tmpBD = new BigDecimal(tmpOriginalString);
        TestUtils.assertEquals(tmpOriginalString, tmpBD.toPlainString());

        TestUtils.assertEquals("12,345.67%", tmpNC.format(tmpBD));
    }

    @Test
    public void testPrecisionAndScaleMutations() {

        NumberContext nc = NumberContext.of(12, 14);

        NumberContext incrementedPrecision = nc.withIncrementedPrecision();
        TestUtils.assertEquals(13, incrementedPrecision.getPrecision());
        TestUtils.assertEquals(14, incrementedPrecision.getScale());

        NumberContext incrementedScale = nc.withIncrementedScale();
        TestUtils.assertEquals(12, incrementedScale.getPrecision());
        TestUtils.assertEquals(15, incrementedScale.getScale());

        NumberContext decrementedPrecision = nc.withDecrementedPrecision();
        TestUtils.assertEquals(11, decrementedPrecision.getPrecision());
        TestUtils.assertEquals(14, decrementedPrecision.getScale());

        NumberContext decrementedScale = nc.withDecrementedScale();
        TestUtils.assertEquals(12, decrementedScale.getPrecision());
        TestUtils.assertEquals(13, decrementedScale.getScale());

        NumberContext doubledPrecision = nc.withDoubledPrecision();
        TestUtils.assertEquals(24, doubledPrecision.getPrecision());
        TestUtils.assertEquals(14, doubledPrecision.getScale());

        NumberContext doubledScale = nc.withDoubledScale();
        TestUtils.assertEquals(12, doubledScale.getPrecision());
        TestUtils.assertEquals(28, doubledScale.getScale());

        NumberContext halvedPrecision = nc.withHalvedPrecision();
        TestUtils.assertEquals(6, halvedPrecision.getPrecision());
        TestUtils.assertEquals(14, halvedPrecision.getScale());

        NumberContext halvedScale = nc.withHalvedScale();
        TestUtils.assertEquals(12, halvedScale.getPrecision());
        TestUtils.assertEquals(7, halvedScale.getScale());
    }

}
