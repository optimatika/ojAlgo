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

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import org.ojalgo.type.function.OperatorWithException;

/**
 * A {@link DataOutput} based {@link ToFileWriter}. You have to construct and supply a custom
 * {@link DataWriter.Serializer} instance to use this writer.
 */
public final class DataWriter<T> implements ToFileWriter<T> {

    @FunctionalInterface
    public interface Serializer<T> extends BiConsumer<T, DataOutput> {

        default void accept(final T data, final DataOutput output) {
            try {
                this.serialize(data, output);
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        }

        void serialize(T data, DataOutput output) throws IOException;

    }

    public static <T> DataWriter<T> of(final File file, final DataWriter.Serializer<T> serializer) {
        return new DataWriter<>(ToFileWriter.output(file), serializer);
    }

    public static <T> DataWriter<T> of(final File file, final DataWriter.Serializer<T> serializer, final OperatorWithException<OutputStream> filter) {
        return new DataWriter<>(filter.apply(ToFileWriter.output(file)), serializer);
    }

    public static <T> DataWriter<T> of(final InMemoryFile file, final DataWriter.Serializer<T> serializer) {
        return new DataWriter<>(file.newOutputStream(), serializer);
    }

    public static <T> DataWriter<T> of(final InMemoryFile file, final DataWriter.Serializer<T> serializer, final OperatorWithException<OutputStream> filter) {
        return new DataWriter<>(filter.apply(file.newOutputStream()), serializer);
    }

    private final DataOutputStream myOutput;
    private final DataWriter.Serializer<T> mySerializer;

    public DataWriter(final OutputStream outputStream, final DataWriter.Serializer<T> serializer) {

        super();

        myOutput = new DataOutputStream(new BufferedOutputStream(outputStream));
        mySerializer = serializer;
    }

    public void close() throws IOException {
        myOutput.close();
    }

    public void write(final T itemToWrite) {
        mySerializer.accept(itemToWrite, myOutput);
    }

}
