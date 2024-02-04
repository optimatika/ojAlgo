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
package org.ojalgo.type.function;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

final class QueuedConsumer<T> implements AutoConsumer<T> {

    static final class Worker<T> implements Runnable {

        private final Consumer<T> myConsumer;
        private final QueuedConsumer<T> myParent;

        Worker(final QueuedConsumer<T> parent, final Consumer<T> consumer) {
            super();
            myParent = parent;
            myConsumer = consumer;
        }

        public void run() {

            List<T> batchContainer = myParent.newBatchContainer();

            while (myParent.drainTo(batchContainer) != 0 || myParent.isMoreToCome()) {
                if (batchContainer.size() != 0) {
                    if (myConsumer instanceof AutoConsumer<?>) {
                        ((AutoConsumer<T>) myConsumer).writeBatch(batchContainer);
                    } else {
                        for (T item : batchContainer) {
                            myConsumer.accept(item);
                        }
                    }
                    batchContainer.clear();
                } else {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException cause) {
                        throw new RuntimeException(cause);
                    }
                }
            }
        }

    }

    private volatile boolean myActive;
    private final int myBatchSize;
    private final Consumer<T>[] myConsumers;
    private final Future<?>[] myFutures;
    private final BlockingQueue<T> myQueue;

    QueuedConsumer(final ExecutorService executor, final BlockingQueue<T> queue, final Consumer<T>... consumers) {

        super();

        myQueue = queue;
        myConsumers = consumers;

        myBatchSize = Math.max(3, queue.remainingCapacity() / (2 + consumers.length));

        myActive = true;
        myFutures = new Future<?>[consumers.length];
        for (int i = 0; i < consumers.length; i++) {
            myFutures[i] = executor.submit(new Worker<>(this, consumers[i]));
        }

    }

    @Override
    public void close() throws Exception {

        myActive = false;

        try {
            for (int i = 0; i < myFutures.length; i++) {
                myFutures[i].get();
                if (myConsumers[i] instanceof AutoCloseable) {
                    ((AutoCloseable) myConsumers[i]).close();
                }
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }

    public void write(final T item) {
        try {
            myQueue.put(item);
        } catch (InterruptedException cause) {
            throw new RuntimeException(cause);
        }
    }

    int drainTo(final List<T> batchContainer) {
        return myQueue.drainTo(batchContainer, myBatchSize);
    }

    boolean isMoreToCome() {
        return myActive || myQueue.size() > 0;
    }

    List<T> newBatchContainer() {
        return new ArrayList<>(myBatchSize);
    }

}
