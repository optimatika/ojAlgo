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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProcessInputStream extends DataInputStream {

    private static String EMPTY = "";

    private String myLastArgument = EMPTY;
    private String myLastCommand = EMPTY;
    private final String myLastCommandPrefix;
    private long myWaitTime = 1;

    public ProcessInputStream(final Process aProcess, final String aLastCommandPrefix) {

        super(aProcess.getInputStream());

        myLastCommandPrefix = aLastCommandPrefix;
    }

    @SuppressWarnings("unused")
    private ProcessInputStream(final InputStream someIn) {

        super(someIn);

        myLastCommandPrefix = null;
    }

    public List<Message> collectMessages() {

        final List<Message> retVal = new Batch();

        Message tmpMessage;
        myWaitTime = 1;
        boolean tmpReadOneMore = true;
        while (tmpReadOneMore) {

            tmpMessage = this.constructMessage();

            if (tmpMessage != null) {

                retVal.add(tmpMessage);

                tmpReadOneMore &= !tmpMessage.getCommand().startsWith(myLastCommandPrefix);
            }
        }

        return retVal;
    }

    public String getLastArgument() {
        return myLastArgument;
    }

    public String getLastCommand() {
        return myLastCommand;
    }

    private Message constructMessage() {

        Message retVal = null;

        final String tmpString = this.readString();

        if ((tmpString != null) && !tmpString.equals(EMPTY)) {

            int tmpFirst = 0;
            int tmpLast = tmpString.indexOf(ASCII.LF);

            final String tmpCommand = tmpString.substring(tmpFirst, tmpLast);

            tmpFirst = tmpLast + 1;
            tmpLast = tmpString.length();

            final String tmpArgument = tmpString.substring(tmpFirst, tmpLast);

            myLastCommand = tmpCommand;
            myLastArgument = tmpArgument;

            retVal = new Message(tmpCommand, tmpArgument);

        } else {

            try {
                this.wait(myWaitTime);
            } catch (final InterruptedException anException) {
                ;
            }

            myWaitTime *= 2;
        }

        return retVal;
    }

    /**
     * Creates a <code>String</code> from the stream of bytes. Each <code>byte</code> (8 bit) is converted to
     * a <code>char</code>. The chars are meged to form a <code>String</code>. The first bytes, describing the
     * count of bytes to come, as well as any '\r' character are discarded. Everything else is converted and
     * merged.
     */
    private String readString() {

        //     BasicLogger.logDebug("Reading in new characters.");

        final StringBuilder retVal = new StringBuilder();

        try {

            int tmpInt;
            char tmpChar;
            int tmpByteCount = 0;

            while (((tmpInt = this.readByteAsInt()) != ASCII.SP) && (tmpInt >= ASCII.DECIMAL_ZERO) && (tmpInt <= ASCII.DECIMAL_NINE)) {

                //    BasicLogger.logDebug("The byte/int is: {}", tmpInt);

                tmpByteCount *= 10;
                tmpByteCount += tmpInt - ASCII.DECIMAL_ZERO;

                //     BasicLogger.logDebug("The number of bytes/chars to read is: {}", tmpByteCount);

            }

            for (int i = 0; i < tmpByteCount; i++) {
                tmpChar = this.readByteAsChar();

                //  BasicLogger.logDebug("The byte/char is: {}", tmpChar);

                if (tmpChar != ASCII.CR) {
                    retVal.append(tmpChar);
                }
            }

        } catch (final IOException anException) {
            retVal.append(ASCII.SP);
            retVal.append(anException);
        }

        //  BasicLogger.logDebug("String is done: {}", retVal);

        return retVal.toString();
    }

    /**
     * @see java.io.DataInput#readByte()
     */
    protected char readByteAsChar() throws IOException {

        final int retVal = this.read();

        if (retVal < 0) {
            throw new EOFException();
        }

        return (char) retVal;
    }

    /**
     * @see java.io.DataInput#readByte()
     */
    protected int readByteAsInt() throws IOException {

        final int retVal = this.read();

        if (retVal < 0) {
            throw new EOFException();
        }

        return retVal;
    }

}
