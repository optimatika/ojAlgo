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
package org.ojalgo.data.image;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;

public class ImageDataTest extends DataImageTests {

    @Test
    public void testChannelSeparation() {

        ImageData image = ImageData.newColour(10, 10);

        ImageData alpha = image.sliceAlphaChannel();
        ImageData red = image.sliceRedChannel();
        ImageData green = image.sliceGreenChannel();
        ImageData blue = image.sliceBlueChannel();

        red.fillAll(123D);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", alpha);
            BasicLogger.debugMatrix("R", red);
            BasicLogger.debugMatrix("G", green);
            BasicLogger.debugMatrix("B", blue);
        }

        TestUtils.assertEquals(0, alpha.intValue(2, 3));
        TestUtils.assertEquals(123, red.intValue(2, 3));
        TestUtils.assertEquals(0, green.intValue(2, 3));
        TestUtils.assertEquals(0, blue.intValue(2, 3));

        green.fillColumn(4, 56D);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", alpha);
            BasicLogger.debugMatrix("R", red);
            BasicLogger.debugMatrix("G", green);
            BasicLogger.debugMatrix("B", blue);
        }

        TestUtils.assertEquals(0, alpha.intValue(2, 4));
        TestUtils.assertEquals(123, red.intValue(2, 4));
        TestUtils.assertEquals(56, green.intValue(2, 4));
        TestUtils.assertEquals(0, blue.intValue(2, 4));

        blue.fillRow(5, 255D);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", alpha);
            BasicLogger.debugMatrix("R", red);
            BasicLogger.debugMatrix("G", green);
            BasicLogger.debugMatrix("B", blue);
        }

        TestUtils.assertEquals(0, alpha.intValue(5, 4));
        TestUtils.assertEquals(123, red.intValue(5, 4));
        TestUtils.assertEquals(56, green.intValue(5, 4));
        TestUtils.assertEquals(255, blue.intValue(5, 4));

        alpha.set(7, 8, 9);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", alpha);
            BasicLogger.debugMatrix("R", red);
            BasicLogger.debugMatrix("G", green);
            BasicLogger.debugMatrix("B", blue);
        }

        TestUtils.assertEquals(9, alpha.intValue(7, 8));
        TestUtils.assertEquals(123, red.intValue(7, 8));
        TestUtils.assertEquals(0, green.intValue(7, 8));
        TestUtils.assertEquals(0, blue.intValue(7, 8));

        if (DEBUG) {
            BasicLogger.debugMatrix("Image", image);
        }

        image.set(9, 6, 1);

        if (DEBUG) {
            BasicLogger.debugMatrix("A", alpha);
            BasicLogger.debugMatrix("R", red);
            BasicLogger.debugMatrix("G", green);
            BasicLogger.debugMatrix("B", blue);
            BasicLogger.debugMatrix("Image", image);
        }

        TestUtils.assertEquals(255, alpha.intValue(9, 6));
        TestUtils.assertEquals(1, red.intValue(9, 6));
        TestUtils.assertEquals(1, green.intValue(9, 6));
        TestUtils.assertEquals(1, blue.intValue(9, 6));
        TestUtils.assertEquals(1, image.intValue(9, 6));
    }

}
