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
package org.ojalgo.series;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

import org.ojalgo.ProgrammingError;
import org.ojalgo.access.Access1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;

public class NumberSeries<N extends Number & Comparable<N>> extends TreeSeries<N, N, NumberSeries<N>> {

    public NumberSeries() {
        super();
    }

    public NumberSeries(final Map<? extends N, ? extends N> map) {
        super(map);
    }

    public NumberSeries(final SortedMap<N, ? extends N> sortedMap) {
        super(sortedMap);
    }

    public Access1D<N> accessKeys() {
        return ArrayUtils.wrapAccess1D(new ArrayList<>(this.keySet()));
    }

    public Access1D<N> accessValues() {
        return ArrayUtils.wrapAccess1D(new ArrayList<>(this.values()));
    }

    public N get(long key) {
        return this.get(MappedIndexSeries.toKey(key));
    }

    public N get(N key) {
        return this.get((Object) key);
    }

    public double invoke(final double arg) {
        ProgrammingError.throwForIllegalInvocation();
        return PrimitiveMath.NaN;
    }

    public N invoke(final N arg) {
        return this.get(arg);
    }

    public double put(final N key, final double value) {
        final Double tmpValue = value;
        final N tmpOldValue = super.put(key, (N) tmpValue);
        if (tmpOldValue != null) {
            return tmpOldValue.doubleValue();
        } else {
            return Double.NaN;
        }
    }

}
