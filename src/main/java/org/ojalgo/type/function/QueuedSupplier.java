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

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

final class QueuedSupplier<T> implements AutoSupplier<T> {

    static final class Worker<T> implements Runnable {

        private final BlockingQueue<T> myQueue;
        private final Supplier<T> mySupplier;

        Worker(final BlockingQueue<T> queue, final Supplier<T> reader) {
            super();
            myQueue = queue;
            mySupplier = reader;
        }

        public void run() {
            try {
                T item = null;
                while ((item = mySupplier.get()) != null) {
                    myQueue.put(item);
                }
            } catch (InterruptedException cause) {
                throw new RuntimeException(cause);
            }
        }

    }

    private final Future<?>[] myFutures;
    private final BlockingQueue<T> myQueue;
    private final Supplier<T>[] mySuppliers;

    QueuedSupplier(final ExecutorService executor, final BlockingQueue<T> queue, final Supplier<T>... suppliers) {

        super();

        myQueue = queue;
        mySuppliers = suppliers;

        myFutures = new Future<?>[suppliers.length];
        for (int i = 0; i < suppliers.length; i++) {
            myFutures[i] = executor.submit(new Worker<>(queue, suppliers[i]));
        }
    }

    @Override
    public void close() throws Exception {
        try {
            for (int i = 0; i < myFutures.length; i++) {
                myFutures[i].get();
                if (mySuppliers[i] instanceof AutoCloseable) {
                    ((AutoCloseable) mySuppliers[i]).close();
                }
            }
        } catch (InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }

    public int drainTo(final Collection<? super T> container, final int maxElements) {
        return myQueue.drainTo(container, maxElements);
    }

    public T read() {

        T retVal = myQueue.poll();

        if (retVal != null) {
            return retVal;
        }

        while (!this.isDone() || myQueue.size() > 0) {
            if ((retVal = myQueue.poll()) != null) {
                return retVal;
            }
            try {
                Thread.sleep(1L);
            } catch (InterruptedException cause) {
                throw new RuntimeException(cause);
            }
        }

        return null;
    }

    private boolean isDone() {
        for (Future<?> worker : myFutures) {
            if (!worker.isDone()) {
                return false;
            }
        }
        return true;
    }

}
