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

import java.util.ArrayList;
import java.util.List;

/**
 * http://www.iana.org/assignments/media-types/ http://www.webmaster-toolkit.com/mime-types.shtml
 *
 * @author apete
 */
public final class DelimitedData extends Object {

    public static DelimitedData makeCommaDelimited() {
        return new DelimitedData(ASCII.COMMA, LineTerminator.WINDOWS);
    }

    public static DelimitedData makeSemicolonDelimited() {
        return new DelimitedData(ASCII.SEMICOLON, LineTerminator.WINDOWS);
    }

    public static DelimitedData makeSpaceDelimited() {
        return new DelimitedData(ASCII.SP, LineTerminator.WINDOWS);
    }

    public static DelimitedData makeTabDelimited() {
        return new DelimitedData(ASCII.HT, LineTerminator.WINDOWS);
    }

    private final char myDelimiter;
    private final List<List<Object>> myLines;
    private final LineTerminator myTerminator;

    public DelimitedData(char aDelimiter, LineTerminator aTerminator) {

        super();

        myDelimiter = aDelimiter;
        myTerminator = aTerminator;
        myLines = new ArrayList<List<Object>>();
    }

    public void addEmptyLines(int aNumberOfLines, int aNumberOfElementsOnEachLine) {
        for (int i = 0; i < aNumberOfLines; i++) {
            myLines.add(new ArrayList<Object>(aNumberOfElementsOnEachLine));
        }
    }

    public void addLine(List<?> aLine) {
        myLines.add((List<Object>) aLine);
    }

    /**
     * The row and column must already exist. One way to create it is to call
     * {@linkplain #addEmptyLines(int, int)}.
     */
    public void set(int aRowIndex, int aColumnIndex, Object anElement) {
        myLines.get(aRowIndex).set(aColumnIndex, anElement);
    }

    public String toString() {

        StringBuilder retVal = new StringBuilder();

        for (List<Object> tmpLine : myLines) {
            for (Object tmpElement : tmpLine) {
                if (tmpElement != null) {
                    retVal.append(tmpElement);
                }
                retVal.append(myDelimiter);
            }
            retVal.append(myTerminator);
        }

        return retVal.toString();
    }

}
