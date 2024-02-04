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
package org.ojalgo.series;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import org.ojalgo.structure.Access1D;
import org.ojalgo.type.NumberDefinition;

public class NumberSeries<N extends Comparable<N>> extends TreeSeries<N, N, NumberSeries<N>> {

    public NumberSeries() {
        super(new TreeMap<>());
    }

    public NumberSeries(final Map<? extends N, ? extends N> map) {
        super(new TreeMap<>(map));
    }

    public NumberSeries(final SortedMap<N, ? extends N> sortedMap) {
        super(new TreeMap<>(sortedMap));
    }

    public Access1D<N> accessKeys() {
        return Access1D.wrap(new ArrayList<>(this.keySet()));
    }

    public Access1D<N> accessValues() {
        return Access1D.wrap(new ArrayList<>(this.values()));
    }

    public double doubleValue(final N key) {
        return NumberDefinition.doubleValue(this.get(key));
    }

    @Override
    public N get(final N key) {
        return this.get((Object) key);
    }

    public BasicSeries<N, N> resample(final UnaryOperator<N> keyTranslator) {
        NumberSeries<N> retVal = new NumberSeries<>();
        this.resample(keyTranslator, retVal);
        return retVal;
    }

}
