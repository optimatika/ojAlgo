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
package org.ojalgo.netio;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.ojalgo.type.function.OperatorWithException;

/**
 * A {@link DataInput} based {@link FromFileReader}. You have to construct and supply a custom
 * {@link DataReader.Deserializer} instance to use this reader.
 */
public final class DataReader<T> implements FromFileReader<T> {

    @FunctionalInterface
    public interface Deserializer<T> extends Function<DataInput, T> {

        /**
         * Will return null on EOF
         */
        default T apply(final DataInput input) {
            try {
                return this.deserialize(input);
            } catch (IOException cause) {
                return null;
            }
        }

        T deserialize(DataInput input) throws IOException;

    }

    public static <T> DataReader<T> of(final File file, final DataReader.Deserializer<T> deserializer) {
        return new DataReader<>(FromFileReader.input(file), deserializer);
    }

    public static <T> DataReader<T> of(final File file, final DataReader.Deserializer<T> deserializer, final OperatorWithException<InputStream> filter) {
        return new DataReader<>(filter.apply(FromFileReader.input(file)), deserializer);
    }

    public static <T> DataReader<T> of(final InMemoryFile file, final DataReader.Deserializer<T> deserializer) {
        return new DataReader<>(file.newInputStream(), deserializer);
    }

    public static <T> DataReader<T> of(final InMemoryFile file, final DataReader.Deserializer<T> deserializer,
            final OperatorWithException<InputStream> filter) {
        return new DataReader<>(filter.apply(file.newInputStream()), deserializer);
    }

    private final Deserializer<T> myDeserializer;
    private final DataInputStream myInput;

    public DataReader(final InputStream inputStream, final DataReader.Deserializer<T> deserializer) {

        super();

        myInput = new DataInputStream(new BufferedInputStream(inputStream));
        myDeserializer = deserializer;
    }

    public void close() throws IOException {
        myInput.close();
    }

    public T read() {
        return myDeserializer.apply(myInput);
    }

}
