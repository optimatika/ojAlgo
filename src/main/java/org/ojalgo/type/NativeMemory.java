/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.type;

import java.lang.ref.Cleaner;
import java.lang.reflect.Field;

import sun.misc.Unsafe;

public abstract class NativeMemory {

    static final Cleaner CLEANER = Cleaner.create();
    static final long SIZE_BYTE = Unsafe.ARRAY_BYTE_INDEX_SCALE;
    static final long SIZE_DOUBLE = Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
    static final long SIZE_FLOAT = Unsafe.ARRAY_FLOAT_INDEX_SCALE;
    static final long SIZE_INT = Unsafe.ARRAY_INT_INDEX_SCALE;
    static final long SIZE_LONG = Unsafe.ARRAY_LONG_INDEX_SCALE;
    static final long SIZE_SHORT = Unsafe.ARRAY_SHORT_INDEX_SCALE;
    static final Unsafe UNSAFE;
    static final byte ZERO_BYTE = (byte) 0;
    static final double ZERO_DOUBLE = 0.0;
    static final float ZERO_FLOAT = 0F;
    static final int ZERO_INT = 0;
    static final long ZERO_LONG = 0L;
    static final short ZERO_SHORT = (short) 0;

    static {

        Unsafe tmpUnsafe = null;

        try {
            final Field tmpField = Unsafe.class.getDeclaredField("theUnsafe");
            tmpField.setAccessible(true);
            tmpUnsafe = (Unsafe) tmpField.get(null);
        } catch (final Exception cause) {
            throw new RuntimeException(cause);
        } finally {
            UNSAFE = tmpUnsafe;
        }
    }

    public static long allocateByteArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_BYTE);
    }

    public static long allocateDoubleArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_DOUBLE);
    }

    public static long allocateFloatArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_FLOAT);
    }

    public static long allocateIntArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_INT);
    }

    public static long allocateLongArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_LONG);
    }

    public static long allocateShortArray(final Object owner, final long count) {
        return NativeMemory.allocate(owner, count * SIZE_SHORT);
    }

    public static void fillByteArray(final long basePointer, final long count, final byte value) {
        for (long i = basePointer, limit = basePointer + SIZE_BYTE * count; i < limit; i += SIZE_BYTE) {
            UNSAFE.putByte(i, value);
        }
    }

    public static void fillDoubleArray(final long basePointer, final long count, final double value) {
        for (long i = basePointer, limit = basePointer + SIZE_DOUBLE * count; i < limit; i += SIZE_DOUBLE) {
            UNSAFE.putDouble(i, value);
        }
    }

    public static void fillFloatArray(final long basePointer, final long count, final float value) {
        for (long i = basePointer, limit = basePointer + SIZE_FLOAT * count; i < limit; i += SIZE_FLOAT) {
            UNSAFE.putFloat(i, value);
        }
    }

    public static void fillIntArray(final long basePointer, final long count, final int value) {
        for (long i = basePointer, limit = basePointer + SIZE_INT * count; i < limit; i += SIZE_INT) {
            UNSAFE.putInt(i, value);
        }
    }

    public static void fillLongArray(final long basePointer, final long count, final long value) {
        for (long i = basePointer, limit = basePointer + SIZE_LONG * count; i < limit; i += SIZE_LONG) {
            UNSAFE.putLong(i, value);
        }
    }

    public static void fillShortArray(final long basePointer, final long count, final short value) {
        for (long i = basePointer, limit = basePointer + SIZE_SHORT * count; i < limit; i += SIZE_SHORT) {
            UNSAFE.putShort(i, value);
        }
    }

    public static byte getByte(final long basePointer, final long index) {
        return UNSAFE.getByte(basePointer + SIZE_BYTE * index);
    }

    public static double getDouble(final long basePointer, final long index) {
        return UNSAFE.getDouble(basePointer + SIZE_DOUBLE * index);
    }

    public static float getFloat(final long basePointer, final long index) {
        return UNSAFE.getFloat(basePointer + SIZE_FLOAT * index);
    }

    public static int getInt(final long basePointer, final long index) {
        return UNSAFE.getInt(basePointer + SIZE_INT * index);
    }

    public static long getLong(final long basePointer, final long index) {
        return UNSAFE.getLong(basePointer + SIZE_LONG * index);
    }

    public static short getShort(final long basePointer, final long index) {
        return UNSAFE.getShort(basePointer + SIZE_SHORT * index);
    }

    public static void initialiseByteArray(final long basePointer, final long count) {
        NativeMemory.fillByteArray(basePointer, count, ZERO_BYTE);
    }

    public static void initialiseDoubleArray(final long basePointer, final long count) {
        NativeMemory.fillDoubleArray(basePointer, count, ZERO_DOUBLE);
    }

    public static void initialiseFloatArray(final long basePointer, final long count) {
        NativeMemory.fillFloatArray(basePointer, count, ZERO_FLOAT);
    }

    public static void initialiseIntArray(final long basePointer, final long count) {
        NativeMemory.fillIntArray(basePointer, count, ZERO_INT);
    }

    public static void initialiseLongArray(final long basePointer, final long count) {
        NativeMemory.fillLongArray(basePointer, count, ZERO_LONG);
    }

    public static void initialiseShortArray(final long basePointer, final long count) {
        NativeMemory.fillShortArray(basePointer, count, ZERO_SHORT);
    }

    public static void setByte(final long basePointer, final long index, final byte value) {
        UNSAFE.putByte(basePointer + SIZE_BYTE * index, value);
    }

    public static void setDouble(final long basePointer, final long index, final double value) {
        UNSAFE.putDouble(basePointer + SIZE_DOUBLE * index, value);
    }

    public static void setFloat(final long basePointer, final long index, final float value) {
        UNSAFE.putFloat(basePointer + SIZE_FLOAT * index, value);
    }

    public static void setInt(final long basePointer, final long index, final int value) {
        UNSAFE.putInt(basePointer + SIZE_INT * index, value);
    }

    public static void setLong(final long basePointer, final long index, final long value) {
        UNSAFE.putLong(basePointer + SIZE_LONG * index, value);
    }

    public static void setShort(final long basePointer, final long index, final short value) {
        UNSAFE.putShort(basePointer + SIZE_SHORT * index, value);
    }

    static long allocate(final Object owner, final long bytes) {

        final long pointer = UNSAFE.allocateMemory(bytes);

        CLEANER.register(owner, () -> UNSAFE.freeMemory(pointer));

        return pointer;
    }

}
