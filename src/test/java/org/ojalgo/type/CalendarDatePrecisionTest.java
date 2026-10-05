/*
 * Copyright 1997-2026 Optimatika
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

class CalendarDatePrecisionTest {

    @Test
    void testOffsetDateTimePreservesMilliseconds() {
        for (long millis : new long[] {1234567890123L, -1L, 0L}) {
            Instant instant = Instant.ofEpochMilli(millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.atOffset(ZoneOffset.ofHours(9))).millis);
        }
    }

    @Test
    void testZonedDateTimePreservesMilliseconds() {
        for (long millis : new long[] {1234567890123L, -1L, 0L}) {
            Instant instant = Instant.ofEpochMilli(millis);
            TestUtils.assertEquals(millis, CalendarDate.valueOf(instant.atZone(ZoneId.of("Europe/Paris"))).millis);
        }
    }
}
