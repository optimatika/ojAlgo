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
package org.ojalgo.netio;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.ojalgo.random.Uniform;

/**
 * Password
 *
 * @author apete
 */
public class Password {

    private static MessageDigest INSTANCE;

    /**
     * @param plainTextPassword An unencrypted (plain text) password
     * @return An encrypted password
     */
    public static String encrypt(final String plainTextPassword) {

        String retVal = null;
        final MessageDigest digest = Password.getInstance();

        if (plainTextPassword != null) {

            final byte[] tmpBytes = digest.digest(plainTextPassword.getBytes());

            for (int i = 0; i < tmpBytes.length; i++) {

                if (tmpBytes[i] < 0) {
                    tmpBytes[i] = (byte) (tmpBytes[i] + 128);
                }
                if (tmpBytes[i] < 32) {
                    tmpBytes[i] = (byte) (tmpBytes[i] + 32);
                }
                // REMOVE!!!
                // 34 is "
                // 38 is &
                // 39 is '
                // 47 is /
                // 60 is <
                // 62 is >
                // 92 is \
                // REMOVE!!!
                if ((tmpBytes[i] == 34) || (tmpBytes[i] == 38) || (tmpBytes[i] == 39) || (tmpBytes[i] == 47) || (tmpBytes[i] == 60) || (tmpBytes[i] == 62)
                        || (tmpBytes[i] == 92)) {
                    tmpBytes[i] = 32;
                }
            }

            retVal = new String(tmpBytes).trim();
        }

        return retVal;
    }

    /**
     * @param plainTextPassword An unencrypted (plain text) password
     * @return An encrypted password
     */
    public static String encrypt(final String plainTextPassword, final String toBytesEncoding, final String fromBytesEncoding) {

        String retVal = null;
        final MessageDigest digest = Password.getInstance();

        if (plainTextPassword != null) {

            try {

                final byte[] tmpBytes = digest.digest(plainTextPassword.getBytes(toBytesEncoding));

                retVal = new String(tmpBytes, fromBytesEncoding).trim();

            } catch (final UnsupportedEncodingException cause) {
                BasicLogger.error(cause.toString());
            }
        }

        return retVal;
    }

    public static String makePlainText(final int length) {

        final char[] retVal = new char[length];

        final Uniform random = new Uniform(0, 128);

        for (int c = 0; c < length; c++) {
            int tmpChar = ASCII.NBSP;
            do {
                tmpChar = random.intValue();
            } while (!ASCII.isAlphanumeric(tmpChar));
            retVal[c] = (char) tmpChar;
        }

        return String.valueOf(retVal);
    }

    private static MessageDigest getInstance() {

        if (INSTANCE == null) {
            try {
                INSTANCE = MessageDigest.getInstance("SHA-512");
            } catch (final NoSuchAlgorithmException cause) {
                BasicLogger.error(cause.toString());
            }
        }

        return INSTANCE;
    }

    protected Password() {
        super();
    }
}
