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
package org.ojalgo.array.operation;

/**
 * @author apete
 */
public interface ArrayOperation {

    /**
     * Argument check for the BLAS/LAPACK operations, where the reference implementation calls XERBLA.
     *
     * @param routine   The routine's name, such as {@code "GEMM"}
     * @param parameter The parameter's name, such as {@code "lda"}
     * @param valid     Whether the argument is valid
     * @throws IllegalArgumentException if the argument is not valid
     */
    static void checkArgument(final String routine, final String parameter, final boolean valid) {
        if (!valid) {
            throw new IllegalArgumentException("On entry to " + routine + " parameter " + parameter + " had an illegal value");
        }
    }

}
