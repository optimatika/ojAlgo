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

/**
 * <table>
 * <caption>ASCII codes</caption>
 * <tr>
 * <th></th>
 * <th>0</th>
 * <th>1</th>
 * <th>2</th>
 * <th>3</th>
 * <th>4</th>
 * <th>5</th>
 * <th>6</th>
 * <th>7</th>
 * </tr>
 * <tr>
 * <th>0</th>
 * <td>NUL</td>
 * <td>SOH</td>
 * <td>STX</td>
 * <td>ETX</td>
 * <td>EOT</td>
 * <td>ENQ</td>
 * <td>ACK</td>
 * <td>BEL</td>
 * </tr>
 * <tr>
 * <th>8</th>
 * <td>BS</td>
 * <td>HT</td>
 * <td>LF</td>
 * <td>VT</td>
 * <td>FF</td>
 * <td>CR</td>
 * <td>SO</td>
 * <td>SI</td>
 * </tr>
 * <tr>
 * <th>16</th>
 * <td>DLE</td>
 * <td>DC1</td>
 * <td>DC2</td>
 * <td>DC3</td>
 * <td>DC4</td>
 * <td>NAK</td>
 * <td>SYN</td>
 * <td>ETB</td>
 * </tr>
 * <tr>
 * <th>24</th>
 * <td>CAN</td>
 * <td>EM</td>
 * <td>SUB</td>
 * <td>ESC</td>
 * <td>FS</td>
 * <td>GS</td>
 * <td>RS</td>
 * <td>US</td>
 * </tr>
 * <tr>
 * <th>32</th>
 * <td>SP</td>
 * <td>!</td>
 * <td>"</td>
 * <td>#</td>
 * <td>$</td>
 * <td>%</td>
 * <td>&amp;</td>
 * <td>'</td>
 * </tr>
 * <tr>
 * <th>40</th>
 * <td>(</td>
 * <td>)</td>
 * <td>*</td>
 * <td>+</td>
 * <td>,</td>
 * <td>-</td>
 * <td>.</td>
 * <td>/</td>
 * </tr>
 * <tr>
 * <th>48</th>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * </tr>
 * <tr>
 * <th>56</th>
 * <td>8</td>
 * <td>9</td>
 * <td>:</td>
 * <td>;</td>
 * <td>&lt;</td>
 * <td>=</td>
 * <td>&gt;</td>
 * <td>?</td>
 * </tr>
 * <tr>
 * <th>64</th>
 * <td>@</td>
 * <td>A</td>
 * <td>B</td>
 * <td>C</td>
 * <td>D</td>
 * <td>E</td>
 * <td>F</td>
 * <td>G</td>
 * </tr>
 * <tr>
 * <th>72</th>
 * <td>H</td>
 * <td>I</td>
 * <td>J</td>
 * <td>K</td>
 * <td>L</td>
 * <td>M</td>
 * <td>N</td>
 * <td>O</td>
 * </tr>
 * <tr>
 * <th>80</th>
 * <td>P</td>
 * <td>Q</td>
 * <td>R</td>
 * <td>S</td>
 * <td>T</td>
 * <td>U</td>
 * <td>V</td>
 * <td>W</td>
 * </tr>
 * <tr>
 * <th>88</th>
 * <td>X</td>
 * <td>Y</td>
 * <td>Z</td>
 * <td>[</td>
 * <td>\</td>
 * <td>]</td>
 * <td>^</td>
 * <td>_</td>
 * </tr>
 * <tr>
 * <th>96</th>
 * <td>`</td>
 * <td>a</td>
 * <td>b</td>
 * <td>c</td>
 * <td>d</td>
 * <td>e</td>
 * <td>f</td>
 * <td>g</td>
 * </tr>
 * <tr>
 * <th>104</th>
 * <td>h</td>
 * <td>i</td>
 * <td>j</td>
 * <td>k</td>
 * <td>l</td>
 * <td>m</td>
 * <td>n</td>
 * <td>o</td>
 * </tr>
 * <tr>
 * <th>112</th>
 * <td>p</td>
 * <td>q</td>
 * <td>r</td>
 * <td>s</td>
 * <td>t</td>
 * <td>u</td>
 * <td>v</td>
 * <td>w</td>
 * </tr>
 * <tr>
 * <th>120</th>
 * <td>x</td>
 * <td>y</td>
 * <td>z</td>
 * <td>{</td>
 * <td>|</td>
 * <td></td>
 * <td>~</td>
 * <td>DEL</td>
 * </tr>
 * </table>
 * http://www.lammertbies.nl/comm/info/ascii-characters.html
 *
 * @author apete
 */
public abstract class ASCII {

    public static final char COMMA = ',';
    public static final char CR = (char) 13; // Carriage Return
    public static final char DECIMAL_NINE = '9';
    public static final char DECIMAL_ZERO = '0';
    public static final char DEL = (char) 127;
    public static final char EQUALS = '=';
    public static final char HT = (char) 9; // TAB (Horizontal Tab)
    public static final char LCB = '{'; // Left Curly Bracket
    public static final char LF = (char) 10; // Line Feed
    public static final char LOWERCASE_A = 'a';
    public static final char LOWERCASE_Z = 'z';
    public static final char NBSP = (char) 160; // Non Breaking SPace
    public static final char NULL = (char) 0;
    public static final char RCB = '}'; // Right Curly Bracket
    public static final char SEMICOLON = (char) 59;
    public static final char SP = (char) 32; // SPace
    public static final char UNDERSCORE = (char) 95;
    public static final char UPPERCASE_A = 'A';
    public static final char UPPERCASE_Z = 'Z';

    public static boolean isAlphabetic(final int aChar) {
        return ASCII.isLowercase(aChar) || ASCII.isUppercase(aChar);
    }

    public static boolean isAlphanumeric(final int aChar) {
        return ASCII.isAlphabetic(aChar) || ASCII.isDigit(aChar);
    }

    /**
     * @return True if aChar is an ASCII character.
     */
    public static boolean isAscii(final int aChar) {
        return (NULL <= aChar) && (aChar <= DEL);
    }

    public static boolean isControl(final int aChar) {
        return ((NULL <= aChar) && (aChar < SP)) || (aChar == DEL);
    }

    public static boolean isDigit(final int aChar) {
        return (DECIMAL_ZERO <= aChar) && (aChar <= DECIMAL_NINE);
    }

    public static boolean isGraph(final int aChar) {
        return (SP < aChar) && (aChar < DEL);
    }

    /**
     * @return true if aChar is an lowercase character
     */
    public static boolean isLowercase(final int aChar) {
        return (LOWERCASE_A <= aChar) && (aChar <= LOWERCASE_Z);
    }

    public static boolean isPrintable(final int aChar) {
        return (SP <= aChar) && (aChar < DEL);
    }

    /**
     * Not sure this is correct
     */
    public static boolean isPunctuation(final int aChar) {
        return ASCII.isGraph(aChar) && !ASCII.isAlphanumeric(aChar);
    }

    public static boolean isSpace(final int aChar) {
        return (aChar == SP) || ((9 <= aChar) && (aChar <= 13));
    }

    /**
     * @return true if aChar is an uppercase character
     */
    public static boolean isUppercase(final int aChar) {
        return (UPPERCASE_A <= aChar) && (aChar <= UPPERCASE_Z);
    }

    /**
     * If aChar is an uppercase character it is converted to the corresponding lowercase character. Otherwise
     * it is returned unaltered.
     */
    public static int toLowercase(final int aChar) {
        return ASCII.isUppercase(aChar) ? aChar + SP : aChar;
    }

    public static int toPrintable(final int aChar) {
        return ASCII.isPrintable(aChar) ? aChar : SP;
    }

    /**
     * If aChar is a lowercase character it is converted to the corresponding uppercase character. Otherwise
     * it is returned unaltered.
     */
    public static int toUppercase(final int aChar) {
        return ASCII.isLowercase(aChar) ? aChar - SP : aChar;
    }

    private ASCII() {
        super();
    }

}
