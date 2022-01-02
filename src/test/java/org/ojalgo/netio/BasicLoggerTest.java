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
package org.ojalgo.netio;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger.AppendablePrinter;
import org.ojalgo.netio.BasicLogger.PrintStreamPrinter;
import org.ojalgo.netio.BasicLogger.PrintWriterPrinter;

public class BasicLoggerTest extends NetioTests {

    @Test
    public void testGettingPrinters() {

        Optional<Appendable> appendable = Optional.empty();
        Optional<PrintStream> printStream = Optional.empty();
        Optional<PrintWriter> printWriter = Optional.empty();

        BasicLogger.AppendablePrinter appendablePrinter = new AppendablePrinter(new StringBuilder());
        BasicLogger.PrintStreamPrinter printStreamPrinter = new PrintStreamPrinter(System.out);
        BasicLogger.PrintWriterPrinter printWriterPrinter = new PrintWriterPrinter(new PrintWriter(System.out));

        appendable = appendablePrinter.getAppendable();
        printStream = appendablePrinter.getPrintStream();
        printWriter = appendablePrinter.getPrintWriter();

        TestUtils.assertEquals(true, appendable.isPresent());
        TestUtils.assertEquals(false, printStream.isPresent());
        TestUtils.assertEquals(false, printWriter.isPresent());

        appendable = printStreamPrinter.getAppendable();
        printStream = printStreamPrinter.getPrintStream();
        printWriter = printStreamPrinter.getPrintWriter();

        TestUtils.assertEquals(false, appendable.isPresent());
        TestUtils.assertEquals(true, printStream.isPresent());
        TestUtils.assertEquals(false, printWriter.isPresent());

        appendable = printWriterPrinter.getAppendable();
        printStream = printWriterPrinter.getPrintStream();
        printWriter = printWriterPrinter.getPrintWriter();

        TestUtils.assertEquals(false, appendable.isPresent());
        TestUtils.assertEquals(false, printStream.isPresent());
        TestUtils.assertEquals(true, printWriter.isPresent());
    }

    @Test
    public void testColumns() {

        StringBuilder appendable = new StringBuilder();

        BasicLogger.AppendablePrinter appendablePrinter = new AppendablePrinter(appendable);

        appendablePrinter.columns(7, "123", "1234", "12345", "123456", "1234567");

        String string = appendable.toString();

        TestUtils.assertEquals(7 * 5 + 1, string.length()); // +1 for the newline at the end
    }

}
