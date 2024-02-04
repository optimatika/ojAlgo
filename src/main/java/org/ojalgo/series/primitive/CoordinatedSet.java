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
package org.ojalgo.series.primitive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.series.BasicSeries;

public class CoordinatedSet<K extends Comparable<? super K>> extends SeriesSet {

    public static final class Builder<K extends Comparable<? super K>> {

        private final List<Supplier<BasicSeries<K, ?>>> mySuppliers = new ArrayList<>();

        public CoordinatedSet.Builder<K> add(final Supplier<BasicSeries<K, ?>> supplier) {
            mySuppliers.add(supplier);
            return this;
        }

        public CoordinatedSet<K> build() {

            List<BasicSeries<K, ?>> uncoordinated = new ArrayList<>();

            for (Supplier<BasicSeries<K, ?>> supplier : mySuppliers) {
                uncoordinated.add(supplier.get());
            }

            return CoordinatedSet.from(uncoordinated);
        }

        public CoordinatedSet<K> build(final UnaryOperator<K> keyMapper) {

            List<BasicSeries<K, ?>> uncoordinated = new ArrayList<>();

            for (Supplier<BasicSeries<K, ?>> supplier : mySuppliers) {
                uncoordinated.add(supplier.get().resample(keyMapper));
            }

            return CoordinatedSet.from(uncoordinated);
        }
    }

    public static <K extends Comparable<? super K>> CoordinatedSet.Builder<K> builder() {
        return new CoordinatedSet.Builder<>();
    }

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(final BasicSeries<K, ?>... uncoordinated) {
        return CoordinatedSet.from(Arrays.asList(uncoordinated));
    }

    public static <K extends Comparable<? super K>> CoordinatedSet<K> from(final List<? extends BasicSeries<K, ?>> uncoordinated) {

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

    private CoordinatedSet(final PrimitiveSeries[] coordinated, final K first, final K last) {

        super(coordinated);

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

    public MatrixStore<Double> getSamples() {
        return this.getData(Primitive64Store.FACTORY);
    }

    public MatrixStore<Double> getSamples(final UnaryOperator<PrimitiveSeries> operator) {
        PrimitiveSeries[] operated = new PrimitiveSeries[myCoordinated.length];
        for (int i = 0; i < operated.length; i++) {
            operated[i] = operator.apply(myCoordinated[i]);
        }
        return Primitive64Store.FACTORY.columns(operated);
    }

    public PrimitiveSeries getSeries(final int index) {
        return myCoordinated[index];
    }

    public int size() {
        return myCoordinated.length;
    }

    @Override
    public String toString() {
        return "FirstKey=" + this.getFirstKey() + ", LastKey=" + this.getLastKey() + ", NumberOfSeries=" + myCoordinated.length + ", NumberOfSeriesEntries="
                + myCoordinated[0].size();
    }

}
