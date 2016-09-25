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

import org.ojalgo.TestUtils;

public class TestCSV extends NetioTests {

    public TestCSV() {
        super();
    }

    public TestCSV(final String name) {
        super(name);
    }

    public void testRFC4180() {

        final File tmpFile = new File("./test/org/ojalgo/netio/example.csv");

        final String[][] tmpExpected = new String[][] { { "Year", "Make", "Model", "Description", "Price" },
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" }, { "1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
                { "1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00" }, { "", "", "Venture \"Extended Edition\"", "", "4900.00" } };

        final EnumeratedColumnsParser tmpParser = EnumeratedColumnsParser.make(5).delimiter(',').strategy(EnumeratedColumnsParser.ParseStrategy.RFC4180).get();

        final AtomicInteger tmpRow = new AtomicInteger(0);

        tmpParser.parse(tmpFile, t -> {
            final int tmpAndIncrement = tmpRow.getAndIncrement();
            for (int i = 0; i < 5; i++) {
                TestUtils.assertEquals(tmpExpected[tmpAndIncrement][i], t.get(i));
            }
        });

        TestUtils.assertEquals(6, tmpRow.get());
    }

}
