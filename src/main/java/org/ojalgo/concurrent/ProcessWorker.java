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
package org.ojalgo.concurrent;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.concurrent.Callable;

import org.ojalgo.concurrent.ExternalProcessExecutor.IPC;
import org.ojalgo.concurrent.ExternalProcessExecutor.ProcessRequest;
import org.ojalgo.concurrent.ExternalProcessExecutor.ProcessResponse;

/**
 * Child JVM entrypoint. Reads {@link ProcessRequest}s from stdin, invokes the specified method via
 * reflection, and writes a {@link ProcessResponse} to stdout for each request. Stdout is reserved for IPC.
 * Any normal System.out printing is redirected to stderr to avoid corrupting the binary protocol.
 */
public abstract class ProcessWorker {

    public static void main(final String[] args) {

        InputStream inputIPC = System.in;
        PrintStream outputIPC = System.out;

        try {
            System.setOut(System.err);
        } catch (Throwable ignore) {
        }

        try {
            while (true) {
                ProcessRequest request;
                try {
                    request = IPC.readFrame(inputIPC, ProcessRequest.class);
                } catch (java.io.EOFException eof) {
                    break;
                }
                Object result = null;
                Throwable error = null;
                try {
                    result = request.invoke();
                } catch (Throwable problem) {
                    error = problem;
                }
                IPC.writeFrame(outputIPC, error == null ? ProcessResponse.ok(result) : ProcessResponse.fail(error));
                outputIPC.flush();
            }
        } catch (Throwable problem) {
            try {
                IPC.writeFrame(outputIPC, ProcessResponse.fail(problem));
                outputIPC.flush();
            } catch (Throwable ignore) {
            }
            System.exit(2);
            return;
        }

        System.exit(0);
    }

    static <T, C extends Callable<T> & Serializable> T call(final C callable) throws Exception {
        return callable.call();
    }

    static <R extends Runnable & Serializable> void run(final R runnable) {
        runnable.run();
    }

}