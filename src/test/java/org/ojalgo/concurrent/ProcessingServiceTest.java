/*
 * Copyright 1997-2021 Optimatika
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ProcessingServiceTest {

    private static final int _100 = 100;

    @Test
    public void testCompute() {

        ProcessingService executor = ProcessingService.newInstance("Test-compute");

        List<BigDecimal> inputs = new ArrayList<>();
        for (int i = 0; i < _100; i++) {
            inputs.add(BigDecimal.valueOf(i));
        }

        Map<BigDecimal, BigDecimal> results = executor.compute(inputs, Parallelism.THREADS, item -> item.movePointRight(1));

        TestUtils.assertEquals(_100, results.size());
        for (Entry<BigDecimal, BigDecimal> entry : results.entrySet()) {
            TestUtils.assertEquals(entry.getKey(), entry.getValue().divide(BigDecimal.TEN));
        }
    }

    @Test
    public void testProcess() {

        ProcessingService executor = ProcessingService.newInstance("Test-process");

        List<BigDecimal> inputs = new ArrayList<>();
        for (int i = 0; i < _100; i++) {
            inputs.add(BigDecimal.valueOf(i));
        }

        LongAdder counter = new LongAdder();
        Set<BigDecimal> seen = ConcurrentHashMap.newKeySet(_100);

        executor.process(inputs, Parallelism.THREADS, item -> {
            seen.add(item);
            counter.increment();
        });

        TestUtils.assertEquals(_100, counter.sum());
        TestUtils.assertEquals(_100, seen.size());

        for (BigDecimal input : inputs) {
            TestUtils.assertTrue(seen.contains(input));
        }
    }

    @Test
    public void testRun() {

        ProcessingService executor = ProcessingService.newInstance("Test-run");

        List<BigDecimal> inputs = new ArrayList<>();
        for (int i = 0; i < _100; i++) {
            inputs.add(BigDecimal.valueOf(i));
        }

        LongAdder counter = new LongAdder();

        executor.run(_100, () -> {
            counter.increment();
            if (counter.sum() >= _100) {
                return;
            }
        });

        TestUtils.assertEquals(_100, counter.sum());
    }

}
