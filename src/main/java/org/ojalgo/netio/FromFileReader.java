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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.ojalgo.type.function.AutoSupplier;
import org.ojalgo.type.function.OperatorWithException;

public interface FromFileReader<T> extends AutoSupplier<T>, Closeable {

    public static final class Builder extends ReaderWriterBuilder<FromFileReader.Builder> {

        Builder(final File[] files) {
            super(files);
        }

        public <T> AutoSupplier<T> build(final Function<File, ? extends FromFileReader<T>> factory) {

            File[] files = this.getFiles();

            LinkedBlockingDeque<T> queue = new LinkedBlockingDeque<>(this.getQueueCapacity());

            AutoSupplier<T> single;

            if (files.length == 1) {

                single = AutoSupplier.queued(this.getExecutor(), queue, factory.apply(files[0]));

            } else {

                LinkedBlockingDeque<File> containers = new LinkedBlockingDeque<>(files.length);
                Collections.addAll(containers, files);

                AutoSupplier<T>[] readers = (AutoSupplier<T>[]) new AutoSupplier<?>[this.getParallelism()];
                for (int i = 0; i < readers.length; i++) {
                    readers[i] = AutoSupplier.sequenced(containers, factory);
                }

                single = AutoSupplier.queued(this.getExecutor(), queue, readers);
            }

            if (this.isStatisticsCollector()) {
                return AutoSupplier.managed(this.getStatisticsCollector(), single);
            } else {
                return single;
            }
        }

    }

    /**
     * Read the properties file and copy the entries to the supplied destination {@link Properties} instance.
     *
     * @param sourceFile Source properties file
     * @param destinationMap Destination properties map
     */
    static void copy(final File sourceFile, final Properties destinationMap) {

        BasicLogger.debug("Path to properties file: {}", sourceFile);

        try (FileInputStream stream = new java.io.FileInputStream(sourceFile)) {
            destinationMap.load(stream);
        } catch (IOException cause) {
            BasicLogger.error(cause, "Failed to load properties file!");
        }
    }

    /**
     * Delete this file or directory (does not need to be empty).
     *
     * @param file Path to a file or directory to be deleted
     */
    static void delete(final File file) {

        if (file == null || !file.exists()) {
            return;
        }

        File[] nested = file.listFiles();

        if (nested != null && nested.length > 0) {
            for (File subfile : nested) {
                FromFileReader.delete(subfile);
            }
        }

        if (!file.delete()) {
            throw new RuntimeException("Failed to delete " + file.getAbsolutePath());
        }
    }

    static <T extends Serializable> T deserializeObjectFromFile(final File file) {
        try (ObjectInputStream ois = new ObjectInputStream(FromFileReader.input(file))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException cause) {
            throw new RuntimeException(cause);
        }
    }

    static InputStream input(final File file) {

        try {

            ToFileWriter.mkdirs(file.getParentFile());
            String name = file.getName();
            InputStream retVal = new FileInputStream(file);

            if (name.endsWith(".gz")) {
                retVal = new GZIPInputStream(retVal);
            } else if (name.endsWith(".zip")) {
                retVal = new ZipInputStream(retVal);
            }

            return retVal;

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    static InputStream input(final File file, final OperatorWithException<InputStream> filter) {
        return filter.apply(FromFileReader.input(file));
    }

    static Builder newBuilder(final File... file) {
        return new Builder(file);
    }

    static Builder newBuilder(final ShardedFile shards) {
        return new Builder(shards.shards());
    }

    /**
     * A factory that produce readers that read items from the supplied sources. (You have a collection of
     * files and want to read through them all using 1 or more readers.)
     */
    static <S, T> Supplier<AutoSupplier<T>> newFactory(final Function<S, FromFileReader<T>> factory, final Collection<? extends S> sources) {

        BlockingQueue<S> work = new LinkedBlockingDeque<>(sources);

        return () -> AutoSupplier.sequenced(work, factory);
    }

    static <S, T> Supplier<AutoSupplier<T>> newFactory(final Function<S, FromFileReader<T>> factory, final S... sources) {

        BlockingQueue<S> work = new LinkedBlockingDeque<>();
        Collections.addAll(work, sources);

        return () -> AutoSupplier.sequenced(work, factory);
    }

    default void close() throws IOException {
        try {
            AutoSupplier.super.close();
        } catch (Exception cause) {
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

}
