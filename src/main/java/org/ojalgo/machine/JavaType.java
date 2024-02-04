/*
 * Copyright 1997-2024 Optimatika
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

    BOOLEAN(boolean.class, 1, 1L), BYTE(byte.class, 8, 1L), CHAR(char.class, 16, 2L), DOUBLE(double.class, 64, 8L), FLOAT(float.class, 32, 4L), INT(int.class,
            32, 4L), LONG(long.class, 64, 8L),
    /**
     * 4 bytes with 32-bit JVM or 64-bit JVM with compressed pointers (All JVM:s assigned less than 32GB)
     */
    REFERENCE(Object.class, 64, 4L), SHORT(short.class, 16, 2L);

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

    public final long memory() {
        return myMemoryBytes;
    }

    final int getInformationBits() {
        return myInformationBits;
    }

    public Class<?> getJavaClass() {
        return myJavaClass;
    }

}
