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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser and writer handling the basic JSON types:
 * <ul>
 * <li>object &harr; {@code Map<String, Object>}</li>
 * <li>array &harr; {@code List<Object>} (any {@link Collection} when writing)</li>
 * <li>string &harr; {@link String}</li>
 * <li>number &harr; {@link Number}</li>
 * <li>boolean &harr; {@link Boolean}</li>
 * <li>null &harr; {@code null}</li>
 * </ul>
 */
public final class BasicJson {

    /**
     * Parse a JSON string representing an array of strings.
     */
    public static List<String> toStringList(final String json) {
        Object parsed = parse(json);
        if (parsed instanceof List<?>) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) parsed) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return Collections.emptyList();
    }

    /**
     * Parse a JSON string representing an object whose values are arrays of strings.
     */
    public static Map<String, List<String>> toStringListMap(final String json) {
        Object parsed = parse(json);
        if (parsed instanceof Map<?, ?>) {
            Map<?, ?> raw = (Map<?, ?>) parsed;
            Map<String, List<String>> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                List<String> values = new ArrayList<>();
                if (entry.getValue() instanceof List<?>) {
                    for (Object item : (List<?>) entry.getValue()) {
                        values.add(String.valueOf(item));
                    }
                }
                result.put(String.valueOf(entry.getKey()), values);
            }
            return result;
        }
        return Collections.emptyMap();
    }

    /**
     * Parse a JSON string into the corresponding Java object.
     *
     * @return a {@code Map<String, Object>}, {@code List<Object>}, {@code String}, {@code Number},
     *         {@code Boolean}, or {@code null}
     */
    public static Object parse(final String json) {
        if (json == null) {
            return null;
        }
        Reader reader = new Reader(json.trim());
        return reader.readValue();
    }

    /**
     * Write a Java object as a JSON string.
     * <p>
     * {@code Map<String, ?>} is written as a JSON object, any {@link Collection} as a JSON array, and
     * {@link String}, {@link Number}, {@link Boolean} and {@code null} as their JSON equivalents.
     */
    public static String write(final Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, value);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(final StringBuilder sb, final Object value) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Map<?, ?>) {
            writeObject(sb, (Map<String, ?>) value);
        } else if (value instanceof Collection<?>) {
            writeArray(sb, (Collection<?>) value);
        } else if (value instanceof String) {
            writeString(sb, (String) value);
        } else if (value instanceof Boolean) {
            sb.append(((Boolean) value).booleanValue() ? "true" : "false");
        } else if (value instanceof Number) {
            Number n = (Number) value;
            double d = n.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                sb.append(n.longValue());
            } else {
                sb.append(n);
            }
        } else {
            writeString(sb, value.toString());
        }
    }

    private static void writeObject(final StringBuilder sb, final Map<String, ?> map) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeString(sb, entry.getKey());
            sb.append(':');
            writeValue(sb, entry.getValue());
        }
        sb.append('}');
    }

    private static void writeArray(final StringBuilder sb, final Collection<?> collection) {
        sb.append('[');
        boolean first = true;
        for (Object element : collection) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeValue(sb, element);
        }
        sb.append(']');
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

    private static final class Reader {

        private final String mySource;
        private int myPos;

        Reader(final String source) {
            mySource = source;
            myPos = 0;
        }

        Object readValue() {
            this.skipWhitespace();
            if (myPos >= mySource.length()) {
                return null;
            }
            char c = mySource.charAt(myPos);
            switch (c) {
            case '{':
                return this.readObject();
            case '[':
                return this.readArray();
            case '"':
                return this.readString();
            case 't':
            case 'f':
                return this.readBoolean();
            case 'n':
                return this.readNull();
            default:
                return this.readNumber();
            }
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            myPos++;
            this.skipWhitespace();
            if (myPos < mySource.length() && mySource.charAt(myPos) == '}') {
                myPos++;
                return map;
            }
            while (myPos < mySource.length()) {
                this.skipWhitespace();
                String key = this.readString();
                this.skipWhitespace();
                myPos++;
                Object value = this.readValue();
                map.put(key, value);
                this.skipWhitespace();
                if (myPos < mySource.length() && mySource.charAt(myPos) == ',') {
                    myPos++;
                } else {
                    break;
                }
            }
            this.skipWhitespace();
            if (myPos < mySource.length() && mySource.charAt(myPos) == '}') {
                myPos++;
            }
            return map;
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            myPos++;
            this.skipWhitespace();
            if (myPos < mySource.length() && mySource.charAt(myPos) == ']') {
                myPos++;
                return list;
            }
            while (myPos < mySource.length()) {
                list.add(this.readValue());
                this.skipWhitespace();
                if (myPos < mySource.length() && mySource.charAt(myPos) == ',') {
                    myPos++;
                } else {
                    break;
                }
            }
            this.skipWhitespace();
            if (myPos < mySource.length() && mySource.charAt(myPos) == ']') {
                myPos++;
            }
            return list;
        }

        private String readString() {
            myPos++;
            StringBuilder sb = new StringBuilder();
            while (myPos < mySource.length()) {
                char c = mySource.charAt(myPos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\' && myPos < mySource.length()) {
                    char esc = mySource.charAt(myPos++);
                    switch (esc) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append(esc);
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        String hex = mySource.substring(myPos, myPos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        myPos += 4;
                        break;
                    default:
                        sb.append('\\');
                        sb.append(esc);
                        break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Number readNumber() {
            int start = myPos;
            if (myPos < mySource.length() && mySource.charAt(myPos) == '-') {
                myPos++;
            }
            while (myPos < mySource.length() && Character.isDigit(mySource.charAt(myPos))) {
                myPos++;
            }
            boolean decimal = false;
            if (myPos < mySource.length() && mySource.charAt(myPos) == '.') {
                decimal = true;
                myPos++;
                while (myPos < mySource.length() && Character.isDigit(mySource.charAt(myPos))) {
                    myPos++;
                }
            }
            if (myPos < mySource.length() && (mySource.charAt(myPos) == 'e' || mySource.charAt(myPos) == 'E')) {
                decimal = true;
                myPos++;
                if (myPos < mySource.length() && (mySource.charAt(myPos) == '+' || mySource.charAt(myPos) == '-')) {
                    myPos++;
                }
                while (myPos < mySource.length() && Character.isDigit(mySource.charAt(myPos))) {
                    myPos++;
                }
            }
            String numStr = mySource.substring(start, myPos);
            if (numStr.isEmpty()) {
                return null;
            }
            if (decimal) {
                return Double.valueOf(numStr);
            }
            long l = Long.parseLong(numStr);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return Integer.valueOf((int) l);
            }
            return Long.valueOf(l);
        }

        private Boolean readBoolean() {
            if (mySource.startsWith("true", myPos)) {
                myPos += 4;
                return Boolean.TRUE;
            }
            myPos += 5;
            return Boolean.FALSE;
        }

        private Object readNull() {
            myPos += 4;
            return null;
        }

        private void skipWhitespace() {
            while (myPos < mySource.length() && Character.isWhitespace(mySource.charAt(myPos))) {
                myPos++;
            }
        }
    }

    private BasicJson() {
    }
}
