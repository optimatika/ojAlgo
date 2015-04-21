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

import java.math.BigDecimal;
import java.util.TreeMap;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.TypeUtils;
import org.ojalgo.type.context.NumberContext;

public class SeriesInterpolator {

    private final NumberContext myContext;
    private CoordinationSet<Double> myCoordinatedSet = null;
    private final TreeMap<BigDecimal, String> myKeys = new TreeMap<BigDecimal, String>();
    private final CoordinationSet<Double> myOriginalSet = new CoordinationSet<Double>();

    public SeriesInterpolator() {
        this(NumberContext.getGeneral(15));
    }

    public SeriesInterpolator(final NumberContext context) {

        super();

        myContext = context;
    }

    public void addSeries(final Number key, final CalendarDateSeries<Double> series) {

        final BigDecimal tmpKey = TypeUtils.toBigDecimal(key, myContext);

        myKeys.put(tmpKey, series.getName());
        myOriginalSet.put(series);
        myCoordinatedSet = null;
    }

    public CalendarDateSeries<Double> getCombination(final Number inputKey) {

        final BigDecimal tmpInputKey = TypeUtils.toBigDecimal(inputKey, myContext);

        if (myCoordinatedSet == null) {
            myCoordinatedSet = myOriginalSet.prune();
            myCoordinatedSet.complete();
        }

        final CalendarDateSeries<Double> retVal = new CalendarDateSeries<Double>(myCoordinatedSet.getResolution());

        BigDecimal tmpLowerKey = null;
        BigDecimal tmpUpperKey = null;
        for (final BigDecimal tmpIterKey : myKeys.keySet()) {

            if (tmpIterKey.compareTo(tmpInputKey) != 1) {
                if ((tmpLowerKey == null) || (tmpIterKey.compareTo(tmpLowerKey) == 1)) {
                    tmpLowerKey = tmpIterKey;
                }
            }

            if (tmpIterKey.compareTo(tmpInputKey) != -1) {
                if ((tmpUpperKey == null) || (tmpIterKey.compareTo(tmpInputKey) != -1)) {
                    tmpUpperKey = tmpIterKey;
                }

            }
        }

        @SuppressWarnings("unchecked")
        final long[] tmpSeriesKeys = ((CalendarDateSeries<Double>) myCoordinatedSet.values().toArray()[0]).getPrimitiveKeys();
        double tmpFactor;
        double[] tmpSeriesValues;

        if ((tmpLowerKey == null) && (tmpUpperKey != null)) {

            tmpFactor = tmpInputKey.doubleValue() / tmpUpperKey.doubleValue();

            tmpSeriesValues = myCoordinatedSet.get(myKeys.get(tmpUpperKey)).getPrimitiveValues();
            for (int i = 0; i < tmpSeriesValues.length; i++) {
                tmpSeriesValues[i] *= tmpFactor;
            }

        } else if ((tmpLowerKey != null) && (tmpUpperKey == null)) {

            tmpFactor = tmpInputKey.doubleValue() / tmpLowerKey.doubleValue();

            tmpSeriesValues = myCoordinatedSet.get(myKeys.get(tmpLowerKey)).getPrimitiveValues();
            for (int i = 0; i < tmpSeriesValues.length; i++) {
                tmpSeriesValues[i] *= tmpFactor;
            }

        } else if ((tmpLowerKey != null) && (tmpUpperKey != null)) {

            if (tmpLowerKey.equals(tmpUpperKey)) {

                tmpSeriesValues = myCoordinatedSet.get(myKeys.get(tmpLowerKey)).getPrimitiveValues();

            } else {

                final double[] tmpLowerValues = myCoordinatedSet.get(myKeys.get(tmpLowerKey)).getPrimitiveValues();
                final double[] tmpUpperValues = myCoordinatedSet.get(myKeys.get(tmpUpperKey)).getPrimitiveValues();

                tmpFactor = (tmpInputKey.doubleValue() - tmpLowerKey.doubleValue()) / (tmpUpperKey.doubleValue() - tmpLowerKey.doubleValue());

                tmpSeriesValues = new double[tmpSeriesKeys.length];

                for (int i = 0; i < tmpSeriesValues.length; i++) {
                    tmpSeriesValues[i] = (tmpFactor * tmpUpperValues[i]) + ((PrimitiveMath.ONE - tmpFactor) * tmpLowerValues[i]);
                }
            }

        } else {

            tmpSeriesValues = new double[tmpSeriesKeys.length];
        }

        for (int i = 0; i < tmpSeriesKeys.length; i++) {
            retVal.put(new CalendarDate(tmpSeriesKeys[i]), tmpSeriesValues[i]);
        }

        return retVal;
    }
}
