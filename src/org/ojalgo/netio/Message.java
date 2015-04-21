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

import java.util.List;

import org.ojalgo.array.ArrayUtils;

public class Message {

    private static String EMPTY = "";

    public static String toString(final List<Message> aCollection) {

        final StringBuilder retVal = new StringBuilder();

        if (aCollection.size() >= 1) {
            retVal.append(aCollection.get(0));
            for (int i = 1; i < aCollection.size(); i++) {
                retVal.append(ASCII.LF);
                retVal.append(aCollection.get(i));
            }
        }

        return retVal.toString();
    }

    private final String[] myCommand;

    public Message(final String aCommand) {

        super();

        myCommand = new String[] { aCommand };
    }

    public Message(final String aCommand, final String anArgument) {

        super();

        myCommand = new String[] { aCommand, anArgument };
    }

    public Message(final String aCommand, final String anArgument, final String aParameter) {

        super();

        myCommand = new String[] { aCommand, anArgument, aParameter };
    }

    public Message(final String[] aCommand) {

        super();

        myCommand = ArrayUtils.copyOf(aCommand);
    }

    @SuppressWarnings("unused")
    private Message() {
        this(EMPTY);
    }

    public String getArgument() {

        final StringBuilder retVal = new StringBuilder();

        if (myCommand.length >= 2) {
            retVal.append(myCommand[1]);
            for (int i = 2; i < myCommand.length; i++) {
                retVal.append(ASCII.SP);
                retVal.append(myCommand[i]);
            }
        }

        return retVal.toString();
    }

    public String getCommand() {
        return myCommand[0];
    }

    @Override
    public String toString() {

        final StringBuilder retVal = new StringBuilder();

        retVal.append(myCommand[0]);
        for (int i = 1; i < myCommand.length; i++) {
            retVal.append(ASCII.SP);
            retVal.append(myCommand[i]);
        }
        retVal.append(ASCII.SEMICOLON);

        return retVal.toString();
    }

}
