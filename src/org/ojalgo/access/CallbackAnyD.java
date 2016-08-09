/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import java.util.Arrays;

@FunctionalInterface
public interface CallbackAnyD<N extends Number> {

    static <N extends Number> void onMatching(final AccessAnyD<N> from, final CallbackAnyD<N> through, final MutateAnyD to) {
    
        final long[] tmpShape = from.shape();
    
        if (Arrays.equals(tmpShape, to.shape())) {
            throw new IllegalArgumentException("Must have the same shape!");
        }
    
        final long[] tmpRef = new long[tmpShape.length];
    
        final long tmpLimit = from.count();
        for (long i = 0L; i < tmpLimit; i++) {
    
            through.call(from, tmpRef, to);
    
            for (int j = tmpShape.length - 1; (j > 0) && ((++tmpRef[j] % tmpShape[j]) == 0); j++) {
    
            }
        }
    }

    /**
     * @param r Reader/Accessor/Getter
     * @param ref Element reference (indices)
     * @param w Writer/Mutator/Setter
     */
    void call(AccessAnyD<N> r, long[] ref, MutateAnyD w);

}
