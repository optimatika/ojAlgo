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

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ojalgo.series.BasicSeries;

public class CoordinatedSet<K extends Comparable<? super K>> {

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(BasicSeries<K, ?>... uncoordinated) {
        return CoordinatedSet.from(Arrays.asList(uncoordinated));
    }

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(List<? extends BasicSeries<K, ?>> uncoordinated) {

        K first = BasicSeries.findLatestFirstKey(uncoordinated);
        K last = BasicSeries.findEarliestLastKey(uncoordinated);

        SortedSet<K> relevantKeys = new TreeSet<>();
        for (BasicSeries<K, ?> individual : uncoordinated) {
            relevantKeys.addAll(individual.subMap(first, last).keySet());
        }
        relevantKeys.add(last);

        int numberOfSeries = uncoordinated.size();
        int numberOfKeys = relevantKeys.size();

        PrimitiveSeries[] coordinated = new PrimitiveSeries[numberOfSeries];

        for (int s = 0; s < numberOfSeries; s++) {
            BasicSeries<K, ?> inputSeries = uncoordinated.get(s);
            double[] outputSeries = new double[numberOfKeys];

            double tmpVal = Double.NaN;
            double curVal = Double.NaN;

            int k = 0;
            for (K key : relevantKeys) {
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

    private CoordinatedSet(PrimitiveSeries[] coordinated, K first, K last) {

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

    public PrimitiveSeries getSeries(int index) {
        return myCoordinated[index];
    }

    public int size() {
        return myCoordinated.length;
    }

}
