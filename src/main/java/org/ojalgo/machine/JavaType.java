/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.machine;

/**
 * <pre>
  B   byte       signed byte
  C   char       Unicode character code point in the Basic Multilingual Plane,
                 encoded with UTF-16
  D   double     double-precision floating-point value
  F   float      single-precision floating-point value
  I   int        integer
  J   long       long integer
  L ClassName ;  reference             an instance of class ClassName
  S   short      signed short
  Z   boolean    true or false
  [   reference  one array dimension
 * </pre>
 *
 * https://stackoverflow.com/questions/32768036/why-is-l-the-prefix-for-reference-types-instead-of-some-other-letter
 *
 * @author apete
 */
public enum JavaType {

    BOOLEAN(boolean.class, 1, 1L),
    BYTE(byte.class, 8, 1L),
    CHAR(char.class, 16, 2L),
    DOUBLE(double.class, 64, 8L),
    FLOAT(float.class, 32, 4L),
    INT(int.class, 32, 4L),
    LONG(long.class, 64, 8L),
    /**
     * 4 bytes with 32-bit JVM or 64-bit JVM with compressed pointers (All JVM:s assigned less than 32GB)
     */
    REFERENCE(Object.class, 64, 4L),
    SHORT(short.class, 16, 2L);

    /**
     * Return the boxed wrapper type for a primitive {@code Class}. If the input is {@code null} or not a
     * primitive, it is returned unchanged. Examples: {@code int.class -> Integer.class},
     * {@code void.class -> Void.class}.
     */
    public static Class<?> box(final Class<?> type) {
        if (type == null || !type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == void.class) return Void.class;
        return type; // fallback
    }

    /**
     * Return the primitive type for a boxed wrapper {@code Class}. If the input is {@code null} or not a
     * wrapper, it is returned unchanged. Examples: {@code Integer.class -> int.class}.
     */
    public static Class<?> unbox(final Class<?> type) {
        if (type == null) return null;
        if (type == Boolean.class) return boolean.class;
        if (type == Byte.class) return byte.class;
        if (type == Character.class) return char.class;
        if (type == Short.class) return short.class;
        if (type == Integer.class) return int.class;
        if (type == Long.class) return long.class;
        if (type == Float.class) return float.class;
        if (type == Double.class) return double.class;
        if (type == Void.class) return void.class;
        return type; // not a wrapper
    }

    public static final JavaType match(final Class<?> aClass) {
        for (final JavaType tmpType : JavaType.values()) {
            if (tmpType.getJavaClass().isAssignableFrom(aClass)) {
                return tmpType;
            }
        }
        return null;
    }

    private final int myInformationBits;
    private final Class<?> myJavaClass;
    private final long myMemoryBytes;

    JavaType(final Class<?> aClass, final int informationBits, final long memoryBytes) {
        myJavaClass = aClass;
        myInformationBits = informationBits;
        myMemoryBytes = memoryBytes;
    }

    public final long estimateSizeOfWrapperClass() {
        return MemoryEstimator.makeForClassExtendingObject().add(this).estimate();
    }

    public Class<?> getJavaClass() {
        return myJavaClass;
    }

    public final long memory() {
        return myMemoryBytes;
    }

    final int getInformationBits() {
        return myInformationBits;
    }

    public static Class<?> resolveType(final String name) throws ClassNotFoundException {
        switch (name) {
        case "boolean":
            return boolean.class;
        case "byte":
            return byte.class;
        case "char":
            return char.class;
        case "short":
            return short.class;
        case "int":
            return int.class;
        case "long":
            return long.class;
        case "float":
            return float.class;
        case "double":
            return double.class;
        case "void":
            return void.class;
        default:
            // Handle array types like "int[]" or "java.lang.String[]" via Class.forName syntax
            if (name.endsWith("[]")) {
                // Use reflection array name: for non-primitive arrays, prefix "[L" + nameWithoutBrackets + ";"
                String elem = name.substring(0, name.length() - 2);
                String jvmName;
                switch (elem) {
                case "boolean":
                    jvmName = "[Z";
                    break;
                case "byte":
                    jvmName = "[B";
                    break;
                case "char":
                    jvmName = "[C";
                    break;
                case "short":
                    jvmName = "[S";
                    break;
                case "int":
                    jvmName = "[I";
                    break;
                case "long":
                    jvmName = "[J";
                    break;
                case "float":
                    jvmName = "[F";
                    break;
                case "double":
                    jvmName = "[D";
                    break;
                default:
                    jvmName = "[L" + elem + ";";
                    break;
                }
                return Class.forName(jvmName);
            } else {
                return Class.forName(name);
            }
        }
    }

}