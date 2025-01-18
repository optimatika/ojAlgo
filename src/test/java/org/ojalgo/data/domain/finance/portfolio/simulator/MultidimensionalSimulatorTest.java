/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.data.domain.finance.portfolio.simulator;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.data.domain.finance.portfolio.SimpleAsset;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.random.process.Process1D;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author apete
 */
public class MultidimensionalSimulatorTest extends PortfolioSimulatorTests {

    public MultidimensionalSimulatorTest() {
        super();
    }

    @Test
    public void testStepping() {

        R064Store correlation = R064Store.FACTORY.makeEye(3, 3);

        GeometricBrownianMotion orgProc1 = new SimpleAsset(ZERO, 0.01, THIRD).forecast();
        GeometricBrownianMotion orgProc2 = new SimpleAsset(ZERO, 0.02, THIRD).forecast();
        GeometricBrownianMotion orgProc3 = new SimpleAsset(ZERO, 0.03, THIRD).forecast();

        TestUtils.assertEquals(0.01, orgProc1.getStandardDeviation(), 0.005);
        TestUtils.assertEquals(0.02, orgProc2.getStandardDeviation(), 0.005);
        TestUtils.assertEquals(0.03, orgProc3.getStandardDeviation(), 0.005);

        ArrayList<GeometricBrownianMotion> procs = new ArrayList<>();
        procs.add(orgProc1);
        procs.add(orgProc2);
        procs.add(orgProc3);
        Process1D<GeometricBrownianMotion> procGBM1D = Process1D.of(correlation, procs);

        List<CalendarDateSeries<Double>> series = new ArrayList<>();
        series.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));
        series.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));
        series.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));

        CalendarDate key = CalendarDate.make(CalendarDateUnit.MONTH);

        series.get(0).put(key, orgProc1.getValue());
        series.get(1).put(key, orgProc2.getValue());
        series.get(2).put(key, orgProc3.getValue());

        for (int t = 0; t < 1000; t++) {

            procGBM1D.step(TWELFTH);
            key = key.step(CalendarDateUnit.MONTH);

            series.get(0).put(key, procGBM1D.getValue(0));
            series.get(1).put(key, procGBM1D.getValue(1));
            series.get(2).put(key, procGBM1D.getValue(2));
        }

        GeometricBrownianMotion newProc1 = GeometricBrownianMotion.estimate(series.get(0).asPrimitive(), TWELFTH);
        GeometricBrownianMotion newProc2 = GeometricBrownianMotion.estimate(series.get(1).asPrimitive(), TWELFTH);
        GeometricBrownianMotion newProc3 = GeometricBrownianMotion.estimate(series.get(2).asPrimitive(), TWELFTH);

        newProc1.setValue(ONE);
        newProc2.setValue(ONE);
        newProc3.setValue(ONE);

        TestUtils.assertEquals(0.01, newProc1.getStandardDeviation(), 0.005);
        TestUtils.assertEquals(0.02, newProc2.getStandardDeviation(), 0.005);
        TestUtils.assertEquals(0.03, newProc3.getStandardDeviation(), 0.005);
    }

}
