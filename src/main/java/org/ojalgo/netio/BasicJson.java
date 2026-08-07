/*
 * Copyright 1997-2026 Optimatika
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

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser and writer handling the six JSON types:
 * <ul>
 * <li>object as {@code Map<String, Object>} (any {@link Map} when writing)
 * <li>array as {@code List<Object>} (any {@link Collection} when writing)
 * <li>string as {@link String}
 * <li>number as {@link Number} - {@link BigInteger} for an integer literal, {@link BigDecimal} for anything
 * with a fraction or an exponent. JSON numbers are unbounded, so parsing never rounds and never overflows.
 * Call {@link Number#doubleValue()} to get at the value as a primitive.
 * <li>boolean as {@link Boolean}
 * <li>null as {@code null}
 * </ul>
 * Objects and arrays nest freely - the values of a parsed {@link Map}, and the elements of a parsed
 * {@link List}, are any of those six types.
 * <p>
 * Parsing is strict. The full input must be exactly one JSON document, and anything that isn't valid JSON
 * results in an {@link IllegalArgumentException}. Object member order is preserved.
 */
public final class BasicJson {

    /**
     * A single-use recursive descent parser over one JSON document.
     */
    private static final class Parser {

        private static boolean isDigit(final char c) {
            return c >= '0' && c <= '9';
        }

        private int myPos;
        private final String mySource;

        Parser(final String source) {
            super();
            mySource = source;
            myPos = 0;
        }

        private IllegalArgumentException error(final String message) {
            return new IllegalArgumentException(message + " at index " + myPos + " parsing JSON!");
        }

        /**
         * Return the current character without consuming it.
         */
        private char peek() {
            if (myPos >= mySource.length()) {
                throw this.error("Unexpected end of input");
            }
            return mySource.charAt(myPos);
        }

        private List<Object> readArray() {

            List<Object> list = new ArrayList<>();

            myPos++;
            this.skipWhitespace();
            if (this.peek() == ']') {
                myPos++;
                return list;
            }

            for (;;) {
                this.skipWhitespace();
                list.add(this.readValue());
                this.skipWhitespace();
                char c = this.peek();
                if (c == ',') {
                    myPos++;
                } else if (c == ']') {
                    myPos++;
                    return list;
                } else {
                    throw this.error("Expected ',' or ']'");
                }
            }
        }

        /**
         * Consume one of the fixed literals true, false or null.
         */
        private void readLiteral(final String literal) {
            if (!mySource.startsWith(literal, myPos)) {
                throw this.error("Expected '" + literal + "'");
            }
            myPos += literal.length();
        }

        private Number readNumber() {

            int first = myPos;

            if (this.peek() == '-') {
                myPos++;
            }

            char c = this.peek();
            if (c == '0') {
                myPos++;
            } else if (Parser.isDigit(c)) {
                this.skipDigits();
            } else {
                throw this.error("Expected a digit");
            }

            boolean integer = true;

            if (myPos < mySource.length() && mySource.charAt(myPos) == '.') {
                integer = false;
                myPos++;
                if (!Parser.isDigit(this.peek())) {
                    throw this.error("Expected a digit");
                }
                this.skipDigits();
            }

            if (myPos < mySource.length() && (mySource.charAt(myPos) == 'e' || mySource.charAt(myPos) == 'E')) {
                integer = false;
                myPos++;
                if (myPos < mySource.length() && (mySource.charAt(myPos) == '+' || mySource.charAt(myPos) == '-')) {
                    myPos++;
                }
                if (!Parser.isDigit(this.peek())) {
                    throw this.error("Expected a digit");
                }
                this.skipDigits();
            }

            String literal = mySource.substring(first, myPos);

            try {
                return integer ? new BigInteger(literal) : new BigDecimal(literal);
            } catch (NumberFormatException cause) {
                throw this.error("Number out of range");
            }
        }

        private Map<String, Object> readObject() {

            Map<String, Object> map = new LinkedHashMap<>();

            myPos++;
            this.skipWhitespace();
            if (this.peek() == '}') {
                myPos++;
                return map;
            }

            for (;;) {
                this.skipWhitespace();
                if (this.peek() != '"') {
                    throw this.error("Expected a string key");
                }
                String key = this.readString();
                this.skipWhitespace();
                if (this.peek() != ':') {
                    throw this.error("Expected ':'");
                }
                myPos++;
                this.skipWhitespace();
                map.put(key, this.readValue());
                this.skipWhitespace();
                char c = this.peek();
                if (c == ',') {
                    myPos++;
                } else if (c == '}') {
                    myPos++;
                    return map;
                } else {
                    throw this.error("Expected ',' or '}'");
                }
            }
        }

        private String readString() {

            myPos++;

            StringBuilder retVal = new StringBuilder();

            for (;;) {
                char c = this.peek();
                myPos++;
                if (c == '"') {
                    return retVal.toString();
                }
                if (c == '\\') {
                    char esc = this.peek();
                    myPos++;
                    switch (esc) {
                        case '"':
                        case '\\':
                        case '/':
                            retVal.append(esc);
                            break;
                        case 'b':
                            retVal.append('\b');
                            break;
                        case 'f':
                            retVal.append('\f');
                            break;
                        case 'n':
                            retVal.append('\n');
                            break;
                        case 'r':
                            retVal.append('\r');
                            break;
                        case 't':
                            retVal.append('\t');
                            break;
                        case 'u':
                            retVal.append(this.readUnicodeEscape());
                            break;
                        default:
                            throw this.error("Invalid escape '\\" + esc + "'");
                    }
                } else if (c < 0x20) {
                    throw this.error("Unescaped control character");
                } else {
                    retVal.append(c);
                }
            }
        }

        /**
         * Read the 4 hexadecimal digits following a \\u escape. A surrogate pair is simply two such escapes,
         * and appending them one at a time reassembles the code point.
         */
        private char readUnicodeEscape() {
            if (myPos + 4 > mySource.length()) {
                throw this.error("Incomplete unicode escape");
            }
            int retVal = 0;
            for (int i = 0; i < 4; i++) {
                int digit = Character.digit(mySource.charAt(myPos++), 16);
                if (digit < 0) {
                    throw this.error("Invalid unicode escape");
                }
                retVal = retVal * 16 + digit;
            }
            return (char) retVal;
        }

        private Object readValue() {
            switch (this.peek()) {
                case '{':
                    return this.readObject();
                case '[':
                    return this.readArray();
                case '"':
                    return this.readString();
                case 't':
                    this.readLiteral("true");
                    return Boolean.TRUE;
                case 'f':
                    this.readLiteral("false");
                    return Boolean.FALSE;
                case 'n':
                    this.readLiteral("null");
                    return null;
                default:
                    char c = this.peek();
                    if (c == '-' || Parser.isDigit(c)) {
                        return this.readNumber();
                    }
                    throw this.error("Unexpected character '" + c + "'");
            }
        }

        private void skipDigits() {
            while (myPos < mySource.length() && Parser.isDigit(mySource.charAt(myPos))) {
                myPos++;
            }
        }

        /**
         * JSON allows space, horizontal tab, line feed and carriage return between tokens - nothing else.
         */
        private void skipWhitespace() {
            while (myPos < mySource.length()) {
                char c = mySource.charAt(myPos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    myPos++;
                } else {
                    return;
                }
            }
        }

        /**
         * Read exactly one value, and verify that nothing but whitespace follows it.
         */
        Object parseDocument() {
            this.skipWhitespace();
            Object value = this.readValue();
            this.skipWhitespace();
            if (myPos < mySource.length()) {
                throw this.error("Trailing content");
            }
            return value;
        }
    }

    /**
     * Parse a JSON document of unknown type.
     *
     * @param json A complete JSON document, or {@code null}
     * @return A {@link Map}, {@link List}, {@link String}, {@link Number}, {@link Boolean} or {@code null}
     */
    public static Object parse(final String json) {
        if (json == null) {
            return null;
        }
        return new Parser(json).parseDocument();
    }

    /**
     * Parse a JSON document that is expected to be an array.
     *
     * @return The elements, each of which is a {@link Map}, {@link List}, {@link String}, {@link Number},
     *         {@link Boolean} or {@code null}. {@code null} if the document is the JSON literal null.
     */
    public static List<?> parseArray(final String json) {
        return BasicJson.parseAs(json, List.class, "array");
    }

    /**
     * Parse a JSON document that is expected to be a boolean.
     *
     * @return {@code null} if the document is the JSON literal null
     */
    public static Boolean parseBoolean(final String json) {
        return BasicJson.parseAs(json, Boolean.class, "boolean");
    }

    /**
     * Parse a JSON document that is expected to be the literal null.
     *
     * @return Always {@code null}
     */
    public static Void parseNull(final String json) {
        return BasicJson.parseAs(json, Void.class, "null");
    }

    /**
     * Parse a JSON document that is expected to be a number.
     *
     * @return A {@link BigInteger} for an integer literal, otherwise a {@link BigDecimal}. {@code null} if
     *         the document is the JSON literal null.
     */
    public static Number parseNumber(final String json) {
        return BasicJson.parseAs(json, Number.class, "number");
    }

    /**
     * Parse a JSON document that is expected to be an object.
     *
     * @return The members, each value of which is a {@link Map}, {@link List}, {@link String},
     *         {@link Number}, {@link Boolean} or {@code null}. {@code null} if the document is the JSON
     *         literal null.
     */
    public static Map<String, ?> parseObject(final String json) {
        return BasicJson.parseAs(json, Map.class, "object");
    }

    /**
     * Parse a JSON document that is expected to be a string.
     *
     * @return The unescaped contents, or {@code null} if the document is the JSON literal null
     */
    public static String parseString(final String json) {
        return BasicJson.parseAs(json, String.class, "string");
    }

    /**
     * Write a Java object as a JSON document.
     * <p>
     * Any {@link Map} is written as a JSON object, with each key rendered by {@link String#valueOf(Object)}.
     * Any {@link Collection} and any Java array - of objects or of primitives - is written as a JSON array.
     * {@link String}, {@link Number}, {@link Boolean} and {@code null} are written as their JSON equivalents,
     * each number exactly as it prints itself. Anything else is written as its {@link Object#toString()}
     * quoted as a JSON string.
     * <p>
     * Maps, collections and arrays nest and mix freely.
     *
     * @throws IllegalArgumentException If a {@link Number} is NaN or infinite, neither of which JSON can
     *                                  represent
     */
    public static String write(final Object value) {
        StringBuilder sb = new StringBuilder();
        BasicJson.writeValue(sb, value);
        return sb.toString();
    }

    /**
     * Parse a full document and require the result to be of the indicated type. The JSON literal null maps to
     * {@code null} regardless of the expected type.
     */
    @SuppressWarnings("unchecked")
    private static <T> T parseAs(final String json, final Class<?> type, final String description) {
        Object value = BasicJson.parse(json);
        if (value == null || type.isInstance(value)) {
            return (T) value;
        }
        throw new IllegalArgumentException("Expected a JSON " + description + " but found " + value.getClass().getSimpleName() + "!");
    }

    private static void writeArray(final StringBuilder sb, final Collection<?> collection) {
        sb.append('[');
        boolean first = true;
        for (Object element : collection) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            BasicJson.writeValue(sb, element);
        }
        sb.append(']');
    }

    /**
     * Write any Java array - of primitives, of objects, or nested - as a JSON array. Elements of a primitive
     * array are boxed one at a time, which is negligible next to formatting them as text.
     */
    private static void writeArray(final StringBuilder sb, final Object array) {
        sb.append('[');
        for (int i = 0, length = Array.getLength(array); i < length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            BasicJson.writeValue(sb, Array.get(array, i));
        }
        sb.append(']');
    }

    /**
     * Every {@link Number} writes itself - no value is ever converted to another type first, so nothing is
     * rounded, widened or narrowed on the way out. The {@link Object#toString()} of every JDK number type is
     * a valid JSON number.
     * <p>
     * The one exception is NaN and infinity, which no numeric type can express and which are therefore
     * rejected rather than written as something no parser will read back. {@link BigInteger} and
     * {@link BigDecimal} are finite by construction, and are exempt from that check because converting one of
     * them to {@code double} would report a large but perfectly writable value as infinite.
     */
    private static void writeNumber(final StringBuilder sb, final Number number) {

        if (!(number instanceof BigInteger) && !(number instanceof BigDecimal) && !Double.isFinite(number.doubleValue())) {
            throw new IllegalArgumentException("JSON cannot represent " + number + "!");
        }

        sb.append(number);
    }

    private static void writeObject(final StringBuilder sb, final Map<?, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            BasicJson.writeString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            BasicJson.writeValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeString(final StringBuilder sb, final String s) {
        sb.append('"');
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append('"');
    }

    private static void writeValue(final StringBuilder sb, final Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Map<?, ?>) {
            BasicJson.writeObject(sb, (Map<?, ?>) value);
        } else if (value instanceof Collection<?>) {
            BasicJson.writeArray(sb, (Collection<?>) value);
        } else if (value.getClass().isArray()) {
            BasicJson.writeArray(sb, value);
        } else if (value instanceof String) {
            BasicJson.writeString(sb, (String) value);
        } else if (value instanceof Boolean) {
            sb.append(((Boolean) value).booleanValue() ? "true" : "false");
        } else if (value instanceof Number) {
            BasicJson.writeNumber(sb, (Number) value);
        } else {
            BasicJson.writeString(sb, value.toString());
        }
    }

    private BasicJson() {
        super();
    }
}
