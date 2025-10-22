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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ProcessExecutorServiceTest {

    @Test
    public void testCancel() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(Thread.class, "sleep", long.class);
        Future<Void> f = exec.execute(spec, new Object[] { 2000L });
        boolean cancelled = f.cancel(true);
        TestUtils.assertTrue(cancelled);
        Assertions.assertThrows(CancellationException.class, f::get);
    }

    @Test
    public void testExceptionPropagation() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(Integer.class, "parseInt", String.class);
        Future<Integer> f = exec.execute(spec, new Object[] { "x" });
        ExecutionException ex = Assertions.assertThrows(ExecutionException.class, f::get);
        TestUtils.assertTrue(ex.getCause() instanceof NumberFormatException);
    }

    @Test
    public void testExecutesInChildProcess() throws Exception {

        long parentPid = java.lang.ProcessHandle.current().pid();

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        Future<Long> f = exec.call((java.io.Serializable & java.util.concurrent.Callable<Long>) () -> java.lang.ProcessHandle.current().pid());
        long childPid = f.get();
        TestUtils.assertTrue(childPid != parentPid);

        // Also invoke a target class method to correlate with child execution proof
        MethodDescriptor spec = MethodDescriptor.of(ProcessExecutorTestTargets.class, "add", int.class, int.class);
        Future<Integer> sumF = exec.execute(spec, new Object[] { 1, 2 });
        TestUtils.assertEquals(3, sumF.get().intValue());
    }

    @Test
    public void testStaticMethodHappyPath() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(Math.class, "addExact", int.class, int.class);
        Future<Integer> f = exec.execute(spec, new Object[] { 2, 3 });
        Integer sum = f.get();
        TestUtils.assertEquals(5L, sum.longValue());
    }

    @Test
    public void testStaticTargetAdd() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(ProcessExecutorTestTargets.class, "add", int.class, int.class);
        Future<Integer> f = exec.execute(spec, new Object[] { 4, 6 });
        Integer res = f.get();
        TestUtils.assertEquals(10, res.intValue());
    }

    @Test
    public void testStaticTargetExceptionPropagation() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(ProcessExecutorTestTargets.class, "throwIAE");
        Future<Void> f = exec.execute(spec);
        ExecutionException ex = Assertions.assertThrows(ExecutionException.class, f::get);
        TestUtils.assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void testStaticTargetVoidSleep() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(ProcessExecutorTestTargets.class, "sleepMs", long.class);
        Future<Void> f = exec.execute(spec, new Object[] { 25L });
        Void res = f.get();
        TestUtils.assertTrue(res == null);
    }

    @Test
    public void testTimeoutKillsChild() throws Exception {

        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance();

        MethodDescriptor spec = MethodDescriptor.of(Thread.class, "sleep", long.class);
        // Specific short timeout is essential here to validate that the watchdog kills the child process
        ProcessOptions opts = new ProcessOptions.Builder().timeout(Duration.ofMillis(100)).build();
        Future<Void> f = exec.execute(spec, opts, new Object[] { 2000L });
        // We expect get() to fail with ExecutionException due to EOF/timeout; but must not hang
        Assertions.assertThrows(ExecutionException.class, f::get);
    }

}
