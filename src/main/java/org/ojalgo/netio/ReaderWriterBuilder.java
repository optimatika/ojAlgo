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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.function.IntSupplier;

import org.ojalgo.concurrent.DaemonPoolExecutor;
import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.type.management.MBeanUtils;
import org.ojalgo.type.management.Throughput;

abstract class ReaderWriterBuilder<B extends ReaderWriterBuilder<B>> {

    private static volatile ExecutorService EXECUTOR = null;

    static ExecutorService executor() {
        if (EXECUTOR == null) {
            synchronized (ReaderWriterBuilder.class) {
                if (EXECUTOR == null) {
                    EXECUTOR = DaemonPoolExecutor.newCachedThreadPool("ojAlgo IO");
                }
            }
        }
        return EXECUTOR;
    }

    private ExecutorService myExecutor = null;
    private final File[] myFiles;
    private String myManagerName = null;
    private IntSupplier myParallelism = Parallelism.CORES.limit(32);
    private int myQueueCapacity = 1024;
    private Throughput myStatisticsCollector = null;

    ReaderWriterBuilder(final File[] files) {
        super();
        myFiles = files;
    }

    public B executor(final ExecutorService executor) {
        myExecutor = executor;
        return (B) this;
    }

    public B manager(final String name) {
        myManagerName = name;
        return (B) this;
    }

    public B parallelism(final int parallelism) {
        return this.parallelism(() -> parallelism);
    }

    public B parallelism(final IntSupplier parallelism) {
        myParallelism = parallelism;
        return (B) this;
    }

    public B queue(final int capacity) {
        myQueueCapacity = capacity;
        return (B) this;
    }

    public B statistics(final Throughput collector) {
        myStatisticsCollector = collector;
        return (B) this;
    }

    ExecutorService getExecutor() {
        if (myExecutor == null) {
            myExecutor = ReaderWriterBuilder.executor();
        }
        return myExecutor;
    }

    File[] getFiles() {
        return myFiles;
    }

    int getParallelism() {
        return myParallelism.getAsInt();
    }

    int getQueueCapacity() {
        return myQueueCapacity;
    }

    Throughput getStatisticsCollector() {
        if (myStatisticsCollector == null && myManagerName != null) {
            myStatisticsCollector = new Throughput();
            MBeanUtils.register(myStatisticsCollector, myManagerName);
        }
        return myStatisticsCollector;
    }

    boolean isStatisticsCollector() {
        return (myStatisticsCollector != null || myManagerName != null);
    }
}
