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
package org.ojalgo.data.batch;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.function.IntSupplier;

import org.ojalgo.data.batch.BatchNode.Builder;
import org.ojalgo.netio.DataInterpreter;
import org.ojalgo.netio.FromFileReader;

/**
 * When you're going to instantiate multiple {@link BatchNode}:s and you want them to share some properties,
 * this is a convenient alternative. Just create a {@link BatchManager}, configure it, and use it to create
 * the {@link BatchNode.Builder}:s using {@link #newNodeBuilder(String, DataInterpreter)}.
 *
 * @author apete
 */
public final class BatchManager {

    private final File myBatchRootDirectory;
    private ExecutorService myExecutor = null;
    private Integer myFragmentation = null;
    private IntSupplier myParallelism = null;
    private Integer myQueueCapacity = null;

    public BatchManager(final File batchRootDirectory) {
        super();
        myBatchRootDirectory = batchRootDirectory;
    }

    /**
     * Dispose of all files associated with nodes instantiated from this manager. It's recommended that you
     * call {@link BatchNode#dispose()} on the individual nodes as you're done with them. This is just a
     * convenient alternative to dispose of multiple (all) nodes simultaneously.
     *
     * @see BatchNode#dispose()
     */
    public void dispose() {
        FromFileReader.delete(myBatchRootDirectory);
    }

    /**
     * @see BatchNode.Builder#executor(ExecutorService)
     */
    public BatchManager executor(final ExecutorService executor) {
        myExecutor = executor;
        return this;
    }

    /**
     * @see BatchNode.Builder#fragmentation(int)
     */
    public BatchManager fragmentation(final int fragmentation) {
        myFragmentation = Integer.valueOf(fragmentation);
        return this;
    }

    public <T> BatchNode.Builder<T> newNodeBuilder(final String nodeName, final DataInterpreter<T> dataInterpreter) {

        Builder<T> retVal = BatchNode.newBuilder(new File(myBatchRootDirectory, nodeName), dataInterpreter);

        if (myFragmentation != null) {
            retVal.fragmentation(myFragmentation.intValue());
        }

        if (myExecutor != null) {
            retVal.executor(myExecutor);
        }

        if (myParallelism != null) {
            retVal.parallelism(myParallelism);
        }

        if (myQueueCapacity != null) {
            retVal.queue(myQueueCapacity.intValue());
        }

        return retVal;
    }

    /**
     * @see BatchNode.Builder#parallelism(int)
     */
    public BatchManager parallelism(final int parallelism) {
        return this.parallelism(() -> parallelism);
    }

    /**
     * @see BatchNode.Builder#parallelism(IntSupplier)
     */
    public BatchManager parallelism(final IntSupplier parallelism) {
        myParallelism = parallelism;
        return this;
    }

    /**
     * @see BatchNode.Builder#queue(int)
     */
    public BatchManager queue(final int capacity) {
        myQueueCapacity = Integer.valueOf(capacity);
        return this;
    }

}
