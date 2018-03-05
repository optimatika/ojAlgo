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
package org.ojalgo.series.primitive;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ojalgo.series.BasicSeries;

public class CoordinatedSet<K extends Comparable<? super K>> {

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(final List<? extends BasicSeries<K, ?>> uncoordinated) {

        final K first = BasicSeries.findLatestFirstKey(uncoordinated);
        final K last = BasicSeries.findEarliestLastKey(uncoordinated);

        final SortedSet<K> tmpAllKeys = new TreeSet<>();

        for (final BasicSeries<K, ?> individual : uncoordinated) {
            tmpAllKeys.addAll(individual.subMap(first, last).keySet());
        }
        tmpAllKeys.add(last);

        final int numberOfSeries = uncoordinated.size();
        final int numberOfKeys = tmpAllKeys.size();

        final PrimitiveSeries[] coordinated = new PrimitiveSeries[numberOfSeries];

        for (int s = 0; s < numberOfSeries; s++) {
            final BasicSeries<K, ?> inputSeries = uncoordinated.get(s);
            final double[] outputSeries = new double[numberOfKeys];

            double tmpVal = Double.NaN;
            double curVal = Double.NaN;

            int k = 0;
            for (final K key : tmpAllKeys) {
                tmpVal = inputSeries.doubleValue(key);
                if (Double.isNaN(tmpVal)) {
                    tmpVal = curVal;
                }
                outputSeries[k] = tmpVal;
                curVal = tmpVal;
                k++;
            }
            coordinated[s] = DataSeries.wrap(outputSeries);
        }

        return new CoordinatedSet<>(coordinated, first, last);
    }

    private final PrimitiveSeries[] myCoordinated;
    private final K myFirstKey;
    private final K myLastKey;

    private CoordinatedSet(final PrimitiveSeries[] coordinated, final K first, final K last) {

        super();

        myCoordinated = coordinated;
        myFirstKey = first;
        myLastKey = last;
    }

    public K getFirstKey() {
        return myFirstKey;
    }

    public K getLastKey() {
        return myLastKey;
    }

    public PrimitiveSeries getSeries(final int index) {
        return myCoordinated[index];
    }

    public int size() {
        return myCoordinated.length;
    }

}
