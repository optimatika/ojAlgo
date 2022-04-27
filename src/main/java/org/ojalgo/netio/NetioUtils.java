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

import java.io.File;

abstract class NetioUtils {

    /**
     * @param file Path to a file or directory (does not need to be empty) to be deleted
     */
    static void delete(final File file) {

        if (file == null || !file.exists()) {
            return;
        }

        File[] nested = file.listFiles();

        if (nested != null && nested.length > 0) {
            for (File subfile : nested) {
                NetioUtils.delete(subfile);
            }
        }

        if (!file.delete()) {
            throw new RuntimeException("Failed to delete " + file.getAbsolutePath());
        }
    }

    /**
     * Make sure this directory exists, create if necessary
     */
    static void mkdirs(final File dir) {
        if (!dir.exists() && (!dir.mkdirs() && !dir.exists())) {
            throw new RuntimeException("Failed to create " + dir.getAbsolutePath());
        }
    }

}
