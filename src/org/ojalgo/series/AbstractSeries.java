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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.netio.ASCII;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.type.Colour;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.keyvalue.KeyValue;

abstract class AbstractSeries<K extends Comparable<K>, V extends Number, I extends AbstractSeries<K, V, I>> extends TreeMap<K, V>implements BasicSeries<K, V> {

    private Colour myColour = null;
    private String myName = null;

    protected AbstractSeries() {
        super();
    }

    protected AbstractSeries(final Comparator<? super K> someC) {
        super(someC);
    }

    protected AbstractSeries(final Map<? extends K, ? extends V> someM) {
        super(someM);
    }

    protected AbstractSeries(final SortedMap<K, ? extends V> someM) {
        super(someM);
    }

    public I colour(final Colour aPaint) {
        myColour = aPaint;
        return (I) this;
    }

    public V firstValue() {
        return this.get(this.firstKey());
    }

    public Colour getColour() {
        return myColour;
    }

    public DataSeries getDataSeries() {
        return DataSeries.wrap(this.getPrimitiveValues());
    }

    public String getName() {
        return myName;
    }

    public double[] getPrimitiveValues() {

        final double[] retVal = new double[this.size()];

        int i = 0;
        for (final V tmpValue : this.values()) {
            retVal[i] = tmpValue.doubleValue();
            i++;
        }

        return retVal;
    }

    public V lastValue() {
        return this.get(this.lastKey());
    }

    public void modify(final BasicSeries<K, V> aLeftArg, final BinaryFunction<V> aFunc) {
        K tmpKey;
        V tmpLeftArg;
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            tmpKey = tmpEntry.getKey();
            tmpLeftArg = aLeftArg.get(tmpKey);
            if (tmpLeftArg != null) {
                this.put(tmpKey, aFunc.invoke(tmpLeftArg, tmpEntry.getValue()));
            } else {
                this.remove(tmpKey);
            }
        }
    }

    public void modify(final BinaryFunction<V> aFunc, final BasicSeries<K, V> aRightArg) {
        K tmpKey;
        V tmpRightArg;
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            tmpKey = tmpEntry.getKey();
            tmpRightArg = aRightArg.get(tmpKey);
            if (tmpRightArg != null) {
                this.put(tmpKey, aFunc.invoke(tmpEntry.getValue(), tmpRightArg));
            } else {
                this.remove(tmpKey);
            }
        }
    }

    public void modify(final BinaryFunction<V> aFunc, final V aRightArg) {
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            this.put(tmpEntry.getKey(), aFunc.invoke(tmpEntry.getValue(), aRightArg));
        }
    }

    public void modify(final ParameterFunction<V> aFunc, final int aParam) {
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            this.put(tmpEntry.getKey(), aFunc.invoke(tmpEntry.getValue(), aParam));
        }
    }

    public void modify(final UnaryFunction<V> aFunc) {
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            this.put(tmpEntry.getKey(), aFunc.invoke(tmpEntry.getValue()));
        }
    }

    public void modify(final V aLeftArg, final BinaryFunction<V> aFunc) {
        for (final Map.Entry<K, V> tmpEntry : this.entrySet()) {
            this.put(tmpEntry.getKey(), aFunc.invoke(aLeftArg, tmpEntry.getValue()));
        }
    }

    public I name(final String aName) {
        myName = aName;
        return (I) this;
    }

    public void putAll(final Collection<? extends KeyValue<? extends K, ? extends V>> data) {
        for (final KeyValue<? extends K, ? extends V> tmpKeyValue : data) {
            this.put(tmpKeyValue.getKey(), tmpKeyValue.getValue());
        }
    }

    @Override
    public String toString() {

        final StringBuilder retVal = this.toStringFirstPart();

        this.appendLastPartToString(retVal);

        return retVal.toString();
    }

    void appendLastPartToString(final StringBuilder retVal) {
        if (this.getColour() != null) {
            retVal.append(TypeUtils.toHexString(this.getColour().getRGB()));
            retVal.append(ASCII.NBSP);
        }

        if (this.size() <= 30) {
            retVal.append(super.toString());
        } else {
            retVal.append("First:");
            retVal.append(this.firstEntry());
            retVal.append(ASCII.NBSP);
            retVal.append("Last:");
            retVal.append(this.lastEntry());
            retVal.append(ASCII.NBSP);
            retVal.append("Size:");
            retVal.append(this.size());
        }
    }

    void setColour(final Colour aPaint) {
        this.colour(aPaint);
    }

    void setName(final String aName) {
        this.name(aName);
    }

    StringBuilder toStringFirstPart() {
        final StringBuilder retVal = new StringBuilder();

        if (this.getName() != null) {
            retVal.append(this.getName());
            retVal.append(ASCII.NBSP);
        }
        return retVal;
    }

}
