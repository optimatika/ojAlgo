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
package org.ojalgo.type;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ojalgo.ProgrammingError;

public abstract class ObjectPool<T> {

    private final boolean myLimited;
    private final BlockingQueue<T> myObjects;

    public ObjectPool() {
        super();
        myObjects = new LinkedBlockingQueue<>();
        myLimited = false;
    }

    public ObjectPool(final int capacity) {
        super();
        myObjects = new LinkedBlockingQueue<>(capacity);
        myLimited = true;
        for (int i = 0; i < capacity; i++) {
            myObjects.add(this.newObject());
        }
    }

    public final T borrow() {
        T retVal;
        if (myLimited) {
            try {
                retVal = myObjects.take();
            } catch (InterruptedException cause) {
                throw new RuntimeException(cause);
            }
        } else if ((retVal = myObjects.poll()) == null) {
            retVal = this.newObject();
        }
        return retVal;
    }

    public final void giveBack(final T object) {
        ProgrammingError.throwIfNull(object);
        this.reset(object);
        if (myLimited) {
            try {
                myObjects.put(object);
            } catch (InterruptedException cause) {
                throw new RuntimeException(cause);
            }
        } else {
            myObjects.offer(object);
        }

    }

    protected abstract T newObject();

    protected abstract void reset(T object);

}
