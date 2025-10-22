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

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ProcessExecutorServiceLambdaTest {

    @Test
    public void capturingCallableLambda() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        int base = 7;
        Future<Integer> f = exec.call(() -> base * 3);
        Integer res = f.get();
        TestUtils.assertEquals(21, res.intValue());
    }

    @Test
    public void nonCapturingCallableLambda() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        Future<Integer> f = exec.call(() -> 2 + 3);
        Integer sum = f.get();
        TestUtils.assertEquals(5, sum.intValue());
    }

    @Test
    public void runnableLambdaReturnsVoid() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        Future<Void> f = exec.run(() -> {
            // simple side-effect-free runnable
            int x = 0; // no-op
            for (int i = 0; i < 10; i++) {
                x += i;
            }
        });
        Void res = f.get();
        TestUtils.assertTrue(res == null);
    }

    @Test
    public void timeoutOnLongRunningLambda() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        // Specific short timeout is essential here to validate that the watchdog kills the child process
        ProcessOptions opts = new ProcessOptions.Builder().timeout(Duration.ofMillis(100)).build();

        Future<Void> f = exec.call(() -> {
            Thread.sleep(2_000L);
            return null;
        }, opts);
        try {
            f.get();
            throw new AssertionError("Expected ExecutionException due to timeout/EOF");
        } catch (ExecutionException expected) {
            // ok
        }
    }

}
