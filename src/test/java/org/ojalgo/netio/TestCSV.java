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
package org.ojalgo.netio;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class TestCSV extends NetioTests {

    static final String[][] EXPECTED = new String[][] { { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" },
            { "", "B", "C", "D", "E", "F", "G", "H", "I", "J" }, { "A", "", "C", "D", "E", "F", "G", "H", "I", "J" },
            { "A", "B", "", "D", "E", "F", "G", "H", "I", "J" }, { "A", "B", "C", "", "E", "F", "G", "H", "I", "J" },
            { "A", "B", "C", "D", "", "F", "G", "H", "I", "J" }, { "A", "B", "C", "D", "E", "", "G", "H", "I", "J" },
            { "A", "B", "C", "D", "E", "F", "", "H", "I", "J" }, { "A", "B", "C", "D", "E", "F", "G", "", "I", "J" },
            { "A", "B", "C", "D", "E", "F", "G", "H", "", "J" }, { "A", "B", "C", "D", "E", "F", "G", "H", "I", "" },
            { "", "", "", "", "", "", "", "", "", "" }, { "A", "", "", "", "", "", "", "", "", "" }, { "", "B", "", "", "", "", "", "", "", "" },
            { "", "", "C", "", "", "", "", "", "", "" }, { "", "", "", "D", "", "", "", "", "", "" }, { "", "", "", "", "E", "", "", "", "", "" },
            { "", "", "", "", "", "F", "", "", "", "" }, { "", "", "", "", "", "", "G", "", "", "" }, { "", "", "", "", "", "", "", "H", "", "" },
            { "", "", "", "", "", "", "", "", "I", "" }, { "", "", "", "", "", "", "", "", "", "J" } };

    public TestCSV() {
        super();
    }

    @Test
    public void testFast() {

        final File tmpFile = new File("./src/test/resources/csv/fast.csv");
        final EnumeratedColumnsParser tmpParser = EnumeratedColumnsParser.make(10).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.FAST).get();

        final AtomicInteger tmpCounter = new AtomicInteger(0);
        tmpParser.parse(tmpFile, t -> {
            final int tmpRowIndex = tmpCounter.getAndIncrement();
            for (int i = 0; i < 5; i++) {
                TestUtils.assertEquals(EXPECTED[tmpRowIndex][i], t.get(i));
            }
        });
        TestUtils.assertEquals(22, tmpCounter.get());
    }

    @Test
    public void testQuoted() {

        final File tmpFile = new File("./src/test/resources/csv/quoted.csv");
        final EnumeratedColumnsParser tmpParser = EnumeratedColumnsParser.make(10).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.QUOTED).get();

        final AtomicInteger tmpCounter = new AtomicInteger(0);
        tmpParser.parse(tmpFile, t -> {
            final int tmpRowIndex = tmpCounter.getAndIncrement();
            for (int i = 0; i < 5; i++) {
                TestUtils.assertEquals(EXPECTED[tmpRowIndex][i], t.get(i));
            }
        });
        TestUtils.assertEquals(22, tmpCounter.get());
    }

    @Test
    public void testRFC4180() {

        final AtomicInteger tmpCounter = new AtomicInteger(0);

        final EnumeratedColumnsParser tmpParserFast = EnumeratedColumnsParser.make(10).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.RFC4180)
                .get();
        final EnumeratedColumnsParser tmpParserQuoted = EnumeratedColumnsParser.make(10).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.RFC4180)
                .get();
        final EnumeratedColumnsParser tmpParserRFC4180 = EnumeratedColumnsParser.make(5).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.RFC4180)
                .get();

        final File tmpFileFast = new File("./src/test/resources/csv/fast.csv");
        final File tmpFileQuoted = new File("./src/test/resources/csv/quoted.csv");
        final File tmpFileExample = new File("./src/test/resources/csv/example.csv");

        tmpCounter.set(0);
        tmpParserFast.parse(tmpFileFast, t -> {
            final int tmpRowIndex = tmpCounter.getAndIncrement();
            final String[] tmpStrings = EXPECTED[tmpRowIndex];
            for (int i = 0; i < 10; i++) {
                final String tmpExpected = tmpStrings[i];
                final String tmpActual = t.get(i);
                TestUtils.assertEquals(tmpExpected, tmpActual);
            }
        });
        TestUtils.assertEquals(22, tmpCounter.get());

        tmpCounter.set(0);
        tmpParserQuoted.parse(tmpFileQuoted, t -> {
            final int tmpRowIndex = tmpCounter.getAndIncrement();
            for (int i = 0; i < 10; i++) {
                TestUtils.assertEquals(EXPECTED[tmpRowIndex][i], t.get(i));
            }
        });
        TestUtils.assertEquals(22, tmpCounter.get());

        final String[][] tmpExpected = new String[][] { { "Year", "Make", "Model", "Description", "Price" },
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" }, { "1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
                { "1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00" }, { "", "", "Venture \"Extended Edition\"", "", "4900.00" } };

        tmpCounter.set(0);
        tmpParserRFC4180.parse(tmpFileExample, t -> {
            final int tmpRowIndex = tmpCounter.getAndIncrement();
            for (int i = 0; i < 5; i++) {
                TestUtils.assertEquals(tmpExpected[tmpRowIndex][i], t.get(i));
            }
        });
        TestUtils.assertEquals(6, tmpCounter.get());
    }

}
