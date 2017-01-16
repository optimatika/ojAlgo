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

import org.ojalgo.function.FunctionUtils;

/**
 * @deprecated v42 Use {@link Structure1D.Callback} instead.
 */
@Deprecated
@FunctionalInterface
public interface Callback1D<N extends Number> {

    /**
     * @deprecated v42 Use {@link Structure1D.Callback} instead.
     */
    @Deprecated
    static <N extends Number> void onMatching(final Access1D<N> from, final Callback1D<N> through, final Mutate1D to) {
        final long tmpLimit = FunctionUtils.min(from.count(), to.count());
        for (long i = 0L; i < tmpLimit; i++) {
            through.call(from, i, to);
        }
    }

    /**
     * @param r Reader/Accessor/Getter
     * @param i Index
     * @param w Writer/Mutator/Setter
     * @deprecated v42 Use {@link Structure1D.Callback} instead.
     */
    @Deprecated
    void call(Access1D<N> r, long i, Mutate1D w);

}
