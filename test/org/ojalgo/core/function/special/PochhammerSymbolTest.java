/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.core.function.special;

import java.util.Random;
import java.util.function.DoubleConsumer;

import org.junit.jupiter.api.Test;
import org.ojalgo.core.TestUtils;

public class PochhammerSymbolTest {

    private static final Random RANDOM = new Random();

    private static void compareToGammaImplementation(final double x, final int n) {
        TestUtils.assertEquals("x=" + x + ", n=" + n, GammaFunction.gamma(x + n) / GammaFunction.gamma(x), PochhammerSymbol.pochhammer(x, n));
    }

    static void doTestDefinition(final double x) {
        PochhammerSymbolTest.compareToGammaImplementation(x, 0);
        PochhammerSymbolTest.compareToGammaImplementation(x, 1);
        PochhammerSymbolTest.compareToGammaImplementation(x, 2);
        PochhammerSymbolTest.compareToGammaImplementation(x, 5);
        PochhammerSymbolTest.compareToGammaImplementation(x, 10);
        PochhammerSymbolTest.compareToGammaImplementation(x, 20);
        PochhammerSymbolTest.compareToGammaImplementation(x, 50);
    }

    static void doTestImplementation(final double x) {
        TestUtils.assertEquals(1.0, PochhammerSymbol.pochhammer(x, 0));
        TestUtils.assertEquals(x, PochhammerSymbol.pochhammer(x, 1));
        TestUtils.assertEquals((x * x) + x, PochhammerSymbol.pochhammer(x, 2));
        TestUtils.assertEquals((x * x * x) + (3.0 * x * x) + (2 * x), PochhammerSymbol.pochhammer(x, 3));
        TestUtils.assertEquals((x * x * x * x) + (6.0 * x * x * x) + (11.0 * x * x) + (6 * x), PochhammerSymbol.pochhammer(x, 4));
    }

    static void performTest(final DoubleConsumer testFunction) {
        RANDOM.doubles(10, -1.0, 1.0).forEach(testFunction);
        RANDOM.doubles(10, -2.0, 2.0).forEach(testFunction);
        RANDOM.doubles(10, -5.0, 5.0).forEach(testFunction);
        RANDOM.doubles(10, -10.0, 10.0).forEach(testFunction);
        RANDOM.doubles(10, -20.0, 20.0).forEach(testFunction);
        RANDOM.doubles(10, -50.0, 50.0).forEach(testFunction);
    }

    /**
     * Test against alternative definition using the gamma function
     */
    @Test
    public void testDefinition() {
        PochhammerSymbolTest.performTest(PochhammerSymbolTest::doTestDefinition);
    }

    /**
     * Test against first few known explicit values
     */
    @Test
    public void testImplementation() {
        PochhammerSymbolTest.performTest(PochhammerSymbolTest::doTestImplementation);
    }

}
