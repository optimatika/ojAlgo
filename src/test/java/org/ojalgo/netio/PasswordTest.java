/*
 * Copyright 1997-2022 Optimatika
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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class PasswordTest extends NetioTests {

    private static void doTestPassword(final String original) {

        String encrypted = Password.encrypt(original);

        TestUtils.assertNotNullOrEmpty(encrypted);

        TestUtils.assertNotEquals(original, encrypted);

        TestUtils.assertEquals(encrypted, Password.encrypt(original));
    }

    @Test
    public void testEncryption() {
        PasswordTest.doTestPassword("secret");
    }

    @Test
    public void testGeneration() {

        for (int i = 1; i < 100; i++) {

            String original = Password.makePlainText(i);

            TestUtils.assertEquals(i, original.length());

            TestUtils.assertNotEquals(original, Password.makePlainText(i));

            PasswordTest.doTestPassword(original);
        }
    }

}
