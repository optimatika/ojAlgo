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
package org.ojalgo.machine;

public enum JavaType {

    BYTE(byte.class, 8, 1L), SHORT(short.class, 16, 2L), INT(int.class, 32, 4L), LONG(long.class, 64, 8L), FLOAT(float.class, 32, 4L), DOUBLE(double.class, 64,
            8L), BOOLEAN(boolean.class, 1, 1L), CHAR(char.class, 16, 2L),
    /**
     * 4 bytes with 32-bit JVM or 64-bit JVM with compressed pointers (All JVM:s assigned less than 32GB)
     */
    REFERENCE(Object.class, 64, 4L);

    public static final JavaType match(final Class<?> aClass) {
        for (final JavaType tmpType : JavaType.values()) {
            if (tmpType.getJavaClass().isAssignableFrom(aClass)) {
                return tmpType;
            }
        }
        return null;
    }

    private final int myInformationBits;
    private final long myMemoryBytes;
    private final Class<?> myJavaClass;

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

    Class<?> getJavaClass() {
        return myJavaClass;
    }

}
