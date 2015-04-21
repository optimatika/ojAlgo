/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.series;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.UnaryFunction;

public class NumberSeries<N extends Number & Comparable<N>> extends AbstractSeries<N, N, NumberSeries<N>> implements UnaryFunction<N> {

    public NumberSeries() {
        super();
    }

    public NumberSeries(final Comparator<? super N> someC) {
        super(someC);
    }

    public NumberSeries(final Map<? extends N, ? extends N> someM) {
        super(someM);
    }

    public NumberSeries(final SortedMap<N, ? extends N> someM) {
        super(someM);
    }

    public Access1D<N> accessKeys() {
        return ArrayUtils.wrapAccess1D(new ArrayList<N>(this.keySet()));
    }

    public Access1D<N> accessValues() {
        return ArrayUtils.wrapAccess1D(new ArrayList<N>(this.values()));
    }

    public double invoke(final double arg) {
        ProgrammingError.throwForIllegalInvocation();
        return PrimitiveMath.NaN;
    }

    public N invoke(final N arg) {
        return this.get(arg);
    }

}
