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

import java.util.Map;

import org.ojalgo.access.Access1D;
import org.ojalgo.series.BasicSeries;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.CoordinationSet;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;

/**
 * A forecaster is restricted to {@linkplain CalendarDate} keys and is intended to predict something related
 * to future keys/dates.
 * 
 * @author apete
 */
public abstract class SeriesForecaster extends SeriesFunction<CalendarDate> {

    private final CalendarDate myLastKey;
    private final CalendarDateUnit myResolution;

    private SeriesForecaster(final BasicSeries<CalendarDate, ? extends Number> data) {

        super(data);

        myLastKey = null;
        myResolution = null;
    }

    private SeriesForecaster(final Map<String, ? extends BasicSeries<CalendarDate, ? extends Number>> data) {

        super(data);

        myLastKey = null;
        myResolution = null;
    }

    protected SeriesForecaster(final CalendarDateSeries<? extends Number> data) {

        super(data);

        myLastKey = data.lastKey();
        myResolution = data.getResolution();
    }

    protected SeriesForecaster(final CoordinationSet<? extends Number> coordinatedHistoricalData) {

        super(coordinatedHistoricalData);

        myLastKey = coordinatedHistoricalData.getEarliestLastKey();
        myResolution = coordinatedHistoricalData.getResolution();
    }

    @Override
    public Map<String, Access1D<?>> invoke(final CalendarDate... key) {

        final CalendarDate tmpLastKey = this.getLastKey();
        final CalendarDateUnit tmpResolution = this.getResolution();

        final CalendarDateDuration[] tmpHorizon = new CalendarDateDuration[key.length];
        for (int h = 0; h < tmpHorizon.length; h++) {
            final double tmpMeassure = tmpResolution.count(tmpLastKey.millis, key[h].millis);
            tmpHorizon[h] = new CalendarDateDuration(tmpMeassure, tmpResolution);
        }

        return this.invoke(tmpHorizon);
    }

    public abstract Map<String, Access1D<?>> invoke(CalendarDateDuration... horizon);

    protected final CalendarDate getLastKey() {
        return myLastKey;
    }

    protected final CalendarDateUnit getResolution() {
        return myResolution;
    }

}
