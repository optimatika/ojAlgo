/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.netio;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.SegmentedFile.Segment;

public class SegmentedFileTest extends NetioTests {

    @Test
    public void testReading1BRC() {

        String[] LINES = { "Mek'ele;22.2", "Da Nang;24.0", "Hat Yai;32.1", "Marseille;7.9", "Yakutsk;-3.0", "Rangpur;30.8", "San Juan;32.6", "Mango;23.8",
                "Benghazi;34.7", "Houston;24.7", "Denpasar;32.5", "Managua;25.0", "Dubai;20.7", "Baghdad;33.4", "Tamale;27.5", "Kuwait City;9.6", "Bissau;28.5",
                "Austin;21.6", "Baltimore;33.8", "Omaha;-1.7" };

        File file = new File("./src/test/resources/org/ojalgo/netio/1brc.txt");

        try (SegmentedFile segmentedFile = SegmentedFile.newBuilder(file).parallelism(7).segmentBytes(18).build()) {

            List<Segment> segments = segmentedFile.segments();

            TestUtils.assertEquals(14, segments.size());

            AtomicInteger counter = new AtomicInteger();

            for (SegmentedFile.Segment segment : segments) {

                try (TextLineReader reader = segmentedFile.newTextLineReader(segment)) {

                    reader.processAll(line -> {
                        TestUtils.assertEquals(LINES[counter.getAndIncrement()], line);
                    });
                }
            }

            TestUtils.assertEquals(20, counter.intValue());

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

}
