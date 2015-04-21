/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DaemonFactory implements ThreadFactory {

    static final DaemonFactory INSTANCE = new DaemonFactory();

    private static final String OJALGO_DAEMON_GROUP = "ojAlgo-daemon-group";
    private static final String PREFIX = "ojAlgo-daemon-";
    private static final int PRIORITY = Thread.NORM_PRIORITY - 1;

    private final AtomicInteger myNextThreadID = new AtomicInteger(1);
    private final ThreadGroup myThreadGroup;

    private DaemonFactory() {

        super();

        myThreadGroup = new ThreadGroup(OJALGO_DAEMON_GROUP);
    }

    public Thread newThread(final Runnable runnable) {

        final String tmpName = PREFIX + myNextThreadID.getAndIncrement();

        final Thread retVal = new Thread(myThreadGroup, runnable, tmpName);

        retVal.setDaemon(true);
        retVal.setPriority(PRIORITY);

        return retVal;
    }

}
