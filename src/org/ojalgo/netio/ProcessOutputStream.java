/*
 * Copyright 1997-2019 Optimatika
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
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class ProcessOutputStream extends PrintStream {

    private static String EMPTY = "";
    private String myLastArgument = EMPTY;
    private String myLastCommand = EMPTY;

    public ProcessOutputStream(final Process aProcess) {
        super(aProcess.getOutputStream(), true);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final File someFile) throws FileNotFoundException {
        super(someFile);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final File someFile, final String someCsn) throws FileNotFoundException, UnsupportedEncodingException {
        super(someFile, someCsn);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final OutputStream anOutputStream) {
        super(anOutputStream);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final OutputStream someOut, final boolean autoFlush) {
        super(someOut, autoFlush);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final OutputStream someOut, final boolean autoFlush, final String someEncoding) throws UnsupportedEncodingException {
        super(someOut, autoFlush, someEncoding);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final String someFileName) throws FileNotFoundException {
        super(someFileName);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(final String someFileName, final String someCsn) throws FileNotFoundException, UnsupportedEncodingException {
        super(someFileName, someCsn);
    }

    public String getLastArgument() {
        return myLastArgument;
    }

    public String getLastCommand() {
        return myLastCommand;
    }

    public void sendBatch(final List<Message> aBatch) {

        final StringBuilder retVal = new StringBuilder();

        Message tmpMessage = aBatch.get(0);
        retVal.append(tmpMessage);
        for (int i = 1; i < aBatch.size(); i++) {

            tmpMessage = aBatch.get(i);

            retVal.append(ASCII.LF);
            retVal.append(tmpMessage.toString());
        }

        myLastCommand = tmpMessage.getCommand();
        myLastArgument = tmpMessage.getArgument();

        this.writeString(retVal.toString());
    }

    public void sendMessage(final Message aMessage) {

        myLastCommand = aMessage.getCommand();
        myLastArgument = aMessage.getArgument();

        this.writeString(aMessage.toString());
    }

    private void writeString(final String aString) {
        this.print(aString.length() + String.valueOf(ASCII.SP) + aString);
    }

}
