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
package org.ojalgo.netio;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ojalgo.array.ArrayAnyD;
import org.ojalgo.array.DenseArray;
import org.ojalgo.type.function.OperatorWithException;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.KeyValue;

public interface DataInterpreter<T> extends DataReader.Deserializer<T>, DataWriter.Serializer<T> {

    DataInterpreter<byte[]> BYTES = new DataInterpreter<>() {

        public byte[] deserialize(final DataInput input) throws IOException {
            int length = input.readInt();
            byte[] retVal = new byte[length];
            for (int i = 0; i < length; i++) {
                retVal[i] = input.readByte();
            }
            return retVal;
        }

        public void serialize(final byte[] data, final DataOutput output) throws IOException {
            int length = data.length;
            output.writeInt(length);
            for (int i = 0; i < length; i++) {
                output.writeByte(data[i]);
            }
        }

    };

    DataInterpreter<String> STRING_BYTES = new DataInterpreter<>() {

        public String deserialize(final DataInput input) throws IOException {
            return new String(BYTES.deserialize(input));
        }

        public void serialize(final String data, final DataOutput output) throws IOException {
            int length = data.length();
            output.writeInt(length);
            output.writeBytes(data);
        }

    };

    DataInterpreter<String> STRING_CHARS = new DataInterpreter<>() {

        public String deserialize(final DataInput input) throws IOException {
            int length = input.readInt();
            StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                builder.append(input.readChar());
            }
            return builder.toString();
        }

        public void serialize(final String data, final DataOutput output) throws IOException {
            int length = data.length();
            output.writeInt(length);
            output.writeChars(data);
        }

    };

    DataInterpreter<String> STRING_UTF = new DataInterpreter<>() {

        public String deserialize(final DataInput input) throws IOException {
            return input.readUTF();
        }

        public void serialize(final String data, final DataOutput output) throws IOException {
            output.writeUTF(data);
        }

    };

    /**
     * @deprecated v56 Use one of the other alternatives
     */
    @Deprecated
    DataInterpreter<String> STRING = STRING_UTF;

    static <N extends Comparable<N>> DataInterpreter<ArrayAnyD<N>> newIDX(final DenseArray.Factory<N, ?> denseArray) {

        ArrayAnyD.Factory<N> factory = ArrayAnyD.factory(denseArray);

        return new DataInterpreter<>() {

            public ArrayAnyD<N> deserialize(final DataInput input) throws IOException {

                input.readByte();
                input.readByte();
                int type = input.readByte();
                int rank = input.readByte();

                long[] structure = new long[rank];
                for (int i = 0; i < rank; i++) {
                    structure[rank - 1 - i] = input.readInt();
                }

                ArrayAnyD<N> data = factory.make(structure);

                for (long i = 0, limit = data.count(); i < limit; i++) {
                    switch (type) {
                        case 0x08:
                            data.set(i, input.readUnsignedByte());
                            break;
                        case 0x09:
                            data.set(i, input.readByte());
                            break;
                        case 0x0B:
                            data.set(i, input.readShort());
                            break;
                        case 0x0C:
                            data.set(i, input.readInt());
                            break;
                        case 0x0D:
                            data.set(i, input.readFloat());
                            break;
                        case 0x0E:
                            data.set(i, input.readDouble());
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown element type!");
                    }
                }

                return data;
            }

            public void serialize(final ArrayAnyD<N> data, final DataOutput output) throws IOException {

                int rank = data.rank();

                output.writeByte(0);
                output.writeByte(0);

                output.writeByte(0x0D); // Hard code to float for now
                output.writeByte(rank);

                for (int i = 0; i < rank; i++) {
                    output.writeInt(data.size(i));
                }

                for (long i = 0, limit = data.count(); i < limit; i++) {
                    output.writeFloat(data.floatValue(i));
                }
            }

        };
    }

    static <T> DataInterpreter<EntryPair.KeyedPrimitive<KeyValue.Dual<T>>> newScoredDual(final DataInterpreter<T> keyInterpreter) {

        return new DataInterpreter<>() {

            public EntryPair.KeyedPrimitive<KeyValue.Dual<T>> deserialize(final DataInput input) throws IOException {
                return EntryPair.of(keyInterpreter.deserialize(input), keyInterpreter.deserialize(input), input.readFloat());
            }

            public void serialize(final EntryPair.KeyedPrimitive<KeyValue.Dual<T>> data, final DataOutput output) throws IOException {
                KeyValue.Dual<T> key = data.getKey();
                keyInterpreter.serialize(key.first, output);
                keyInterpreter.serialize(key.second, output);
                output.writeFloat(data.floatValue());
            }

        };
    }

    static byte toByte(final byte[] bytes) {
        return bytes[0];
    }

    static byte[] toBytes(final byte value) {
        return new byte[] { value };
    }

    static byte[] toBytes(final char value) {
        return new byte[] { (byte) (value >>> 8 & 0xFF), (byte) (value >>> 0 & 0xFF) };
    }

    static byte[] toBytes(final double value) {
        return DataInterpreter.toBytes(Double.doubleToLongBits(value));
    }

    static byte[] toBytes(final float value) {
        return DataInterpreter.toBytes(Float.floatToIntBits(value));
    }

    static byte[] toBytes(final int value) {
        return new byte[] { (byte) (value >>> 24 & 0xFF), (byte) (value >>> 16 & 0xFF), (byte) (value >>> 8 & 0xFF), (byte) (value >>> 0 & 0xFF) };
    }

    static byte[] toBytes(final long value) {
        return new byte[] { (byte) (value >>> 56 & 0xFF), (byte) (value >>> 48 & 0xFF), (byte) (value >>> 40 & 0xFF), (byte) (value >>> 32 & 0xFF),
                (byte) (value >>> 24 & 0xFF), (byte) (value >>> 16 & 0xFF), (byte) (value >>> 8 & 0xFF), (byte) (value >>> 0 & 0xFF) };
    }

    static byte[] toBytes(final short value) {
        return new byte[] { (byte) (value >>> 8 & 0xFF), (byte) (value >>> 0 & 0xFF) };
    }

    static char toChar(final byte[] bytes) {
        return (char) ((bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF);
    }

    static double toDouble(final byte[] bytes) {
        return Double.longBitsToDouble(DataInterpreter.toLong(bytes));
    }

    static float toFloat(final byte[] bytes) {
        return Float.intBitsToFloat(DataInterpreter.toInt(bytes));
    }

    static int toInt(final byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | bytes[3] & 0xFF;
    }

    static long toLong(final byte[] bytes) {
        return (long) (bytes[0] & 0xFF) << 56 | (long) (bytes[1] & 0xFF) << 48 | (long) (bytes[2] & 0xFF) << 40 | (long) (bytes[3] & 0xFF) << 32
                | (long) (bytes[4] & 0xFF) << 24 | (long) (bytes[5] & 0xFF) << 16 | (long) (bytes[6] & 0xFF) << 8 | bytes[7] & 0xFF;
    }

    static short toShort(final byte[] bytes) {
        return (short) ((bytes[0] & 0xFF) << 8 | bytes[1] & 0xFF);
    }

    default DataReader<T> newReader(final File file) {
        return DataReader.of(file, this);
    }

    default DataReader<T> newReader(final File file, final OperatorWithException<InputStream> filter) {
        return DataReader.of(file, this, filter);
    }

    default DataWriter<T> newWriter(final File file) {
        return DataWriter.of(file, this);
    }

    default DataWriter<T> newWriter(final File file, final OperatorWithException<OutputStream> filter) {
        return DataWriter.of(file, this, filter);
    }

}
