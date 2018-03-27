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
}
