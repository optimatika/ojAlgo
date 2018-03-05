/*
 * Copyright 1997-2018 Optimatika
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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.ColourData;
import org.ojalgo.type.TypeUtils;

abstract class TreeSeries<K extends Comparable<? super K>, N extends Number, I extends TreeSeries<K, N, I>> extends TreeMap<K, N> implements BasicSeries<K, N> {

    private ColourData myColour = null;
    private String myName = null;

    @SuppressWarnings("unused")
    private TreeSeries(final Comparator<? super K> comparator) {
        super(comparator);
    }

    protected TreeSeries() {
        super();
    }

    protected TreeSeries(final Map<? extends K, ? extends N> map) {
        super(map);
    }

    protected TreeSeries(final SortedMap<K, ? extends N> sortedMap) {
        super(sortedMap);
    }

    public PrimitiveSeries asPrimitive() {

        final double[] retVal = new double[this.size()];

        int i = 0;
        for (final N tmpValue : this.values()) {
            retVal[i] = tmpValue.doubleValue();
            i++;
        }

        return DataSeries.wrap(retVal);
    }

    @SuppressWarnings("unchecked")
    public I colour(final ColourData colour) {
        myColour = colour;
        return (I) this;
    }

    public final double doubleValue(final K key) {
        return this.get(key).doubleValue();
    }

    public N firstValue() {
        return this.get(this.firstKey());
    }

    public ColourData getColour() {
        return myColour;
    }

    public String getName() {
        if (myName == null) {
            myName = UUID.randomUUID().toString();
        }
        return myName;
    }

    public N lastValue() {
        return this.get(this.lastKey());
    }

    @SuppressWarnings("unchecked")
    public I name(final String name) {
        myName = name;
        return (I) this;
    }

    @Override
    public String toString() {

        final StringBuilder retVal = this.toStringFirstPart();

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

    final void appendLastPartToString(final StringBuilder builder) {

        if (myColour != null) {
            builder.append(TypeUtils.toHexString(myColour.getRGB()));
            builder.append(ASCII.NBSP);
        }

        if (this.size() <= 30) {
            builder.append(super.toString());
        } else {
            builder.append("First:");
            builder.append(this.firstEntry());
            builder.append(ASCII.NBSP);
            builder.append("Last:");
            builder.append(this.lastEntry());
            builder.append(ASCII.NBSP);
            builder.append("Size:");
            builder.append(this.size());
        }
    }

    void setColour(final ColourData colour) {
        this.colour(colour);
    }

    void setName(final String name) {
        this.name(name);
    }

    StringBuilder toStringFirstPart() {

        final StringBuilder retVal = new StringBuilder();

        if (myName != null) {
            retVal.append(myName);
            retVal.append(ASCII.NBSP);
        }

        return retVal;
    }

}
