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

import java.io.IOException;
import java.util.List;

import org.ojalgo.ProgrammingError;

public abstract class SystemProcess {

    private static String ERROR_TEXT = "System command not working!";

    private ProcessInputStream myInStream;
    private ProcessOutputStream myOutStream;
    private Process myProcess;

    public SystemProcess(final String[] aCommandArray, final String aLastCommandPrefix) {

        super();

        final Runtime tmpRuntime = Runtime.getRuntime();

        try {

            myProcess = tmpRuntime.exec(aCommandArray);

            myOutStream = new ProcessOutputStream(myProcess);
            myInStream = new ProcessInputStream(myProcess, aLastCommandPrefix);

            myInStream.collectMessages();

        } catch (final IOException anException) {
            throw new ProgrammingError(ERROR_TEXT);
        }
    }

    @SuppressWarnings("unused")
    private SystemProcess() {

        super();

        ProgrammingError.throwForIllegalInvocation();
    }

    public List<Message> communicate(final List<Message> aBatch) {
        myOutStream.sendBatch(aBatch);
        return myInStream.collectMessages();
    }

    public List<Message> communicate(final Message aMessage) {
        myOutStream.sendMessage(aMessage);
        return myInStream.collectMessages();
    }

    public String getLastReturnArgument() {
        return myInStream.getLastArgument();
    }

    public void terminate() {

        try {

            if (myOutStream != null) {
                myOutStream.close();
            }

            if (myInStream != null) {
                myInStream.close();
            }

            if (myProcess != null) {
                myProcess.destroy();
            }

        } catch (final IOException anException) {
            ;
        } finally {

            myOutStream = null;
            myInStream = null;
            myProcess = null;
        }
    }
}
