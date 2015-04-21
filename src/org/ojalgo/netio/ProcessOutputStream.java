/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

    public ProcessOutputStream(Process aProcess) {
        super(aProcess.getOutputStream(), true);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(File someFile) throws FileNotFoundException {
        super(someFile);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(File someFile, String someCsn) throws FileNotFoundException, UnsupportedEncodingException {
        super(someFile, someCsn);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(OutputStream anOutputStream) {
        super(anOutputStream);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(OutputStream someOut, boolean autoFlush) {
        super(someOut, autoFlush);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(OutputStream someOut, boolean autoFlush, String someEncoding) throws UnsupportedEncodingException {
        super(someOut, autoFlush, someEncoding);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(String someFileName) throws FileNotFoundException {
        super(someFileName);
    }

    @SuppressWarnings("unused")
    private ProcessOutputStream(String someFileName, String someCsn) throws FileNotFoundException, UnsupportedEncodingException {
        super(someFileName, someCsn);
    }

    public String getLastArgument() {
        return myLastArgument;
    }

    public String getLastCommand() {
        return myLastCommand;
    }

    public void sendBatch(List<Message> aBatch) {

        StringBuilder retVal = new StringBuilder();

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

    public void sendMessage(Message aMessage) {

        myLastCommand = aMessage.getCommand();
        myLastArgument = aMessage.getArgument();

        this.writeString(aMessage.toString());
    }

    private void writeString(String aString) {
        this.print(aString.length() + String.valueOf(ASCII.SP) + aString);
    }

}
