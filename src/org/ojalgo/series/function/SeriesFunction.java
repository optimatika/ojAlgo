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
package org.ojalgo.series.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ojalgo.access.Access1D;
import org.ojalgo.series.BasicSeries;

/**
 * A function that maps from a (collection of) series and one or more keys to a series of numbers. The
 * interpretation of the input series data and the output series is completely free.
 * 
 * @author apete
 * @param <K> The series key type
 */
public abstract class SeriesFunction<K extends Comparable<K>> {

    private final Map<String, ? extends BasicSeries<K, ? extends Number>> myData;

    @SuppressWarnings("unused")
    private SeriesFunction() {

        super();

        myData = null;
    }

    protected SeriesFunction(final BasicSeries<K, ? extends Number> data) {

        super();

        myData = Collections.singletonMap(data.getName(), data);
    }

    protected SeriesFunction(final Map<String, ? extends BasicSeries<K, ? extends Number>> data) {

        super();

        myData = data;
    }

    /**
     * @param key One or more time series keys
     * @return A map with one entry per series. Each entry/series has the same number of elements as there
     *         were input keys.
     */
    public abstract Map<String, Access1D<?>> invoke(K... key);

    protected List<String> getAllSeriesNames() {
        return new ArrayList<String>(myData.keySet());
    }

    protected BasicSeries<K, ? extends Number> getSeries(final String name) {
        return myData.get(name);
    }

}
