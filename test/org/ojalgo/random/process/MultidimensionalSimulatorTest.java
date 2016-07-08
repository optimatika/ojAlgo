/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se)
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
package org.ojalgo.random.process;

import java.util.ArrayList;
import java.util.List;

import org.ojalgo.TestUtils;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.finance.portfolio.SimpleAsset;
import org.ojalgo.finance.portfolio.SimplePortfolio;
import org.ojalgo.finance.portfolio.simulator.PortfolioSimulator;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.process.RandomProcess.SimulationResults;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

/**
 * @author apete
 */
public class MultidimensionalSimulatorTest extends RandomProcessTests {

    public MultidimensionalSimulatorTest() {
        super();
    }

    public MultidimensionalSimulatorTest(final String aName) {
        super(aName);
    }

    public void _testFirstSebCase() {

        final double[][] tmpCorrelations = new double[][] {
                { 1.0, 4.818910644591628E-4, -0.2073732878173141, -0.2570196357409301, -0.2288355302753617, -0.06622162366629052, 0.9995107173168294,
                        0.046413002772051466, -0.12638463146425086, -0.2648581058359268, 4.818910644591628E-4, 4.818910644591628E-4, -0.29110060025606405 },
                { 4.818910644591628E-4, 1.0, 0.459704102653577, 0.2329848867647044, 0.23359911839446348, 0.11034865213357695, -0.002148308071200791,
                        0.8083204130337523, 0.6830967821186095, 0.17092481883773306, 0.9999999999999998, 0.9999999999999998, 0.2925123800699666 },
                { -0.2073732878173141, 0.459704102653577, 1.0, 0.7585942105977437, 0.14228262696178093, 0.5130036221887934, -0.2048824527435123,
                        0.47369514919913946, 0.630006642104428, 0.7794685675266949, 0.459704102653577, 0.459704102653577, 0.7797230267924377 },
                { -0.2570196357409301, 0.2329848867647044, 0.7585942105977437, 1.0, 0.2294679179627081, 0.5813548671560165, -0.251176272641125,
                        0.2475578563281484, 0.479933038682869, 0.7547291944708587, 0.2329848867647044, 0.2329848867647044, 0.7207853187826451 },
                { -0.2288355302753617, 0.23359911839446348, 0.14228262696178093, 0.2294679179627081, 1.0, 0.12516536864601804, -0.22826962916119573,
                        0.09519884606443976, 0.16127241385122823, 0.0491830417531641, 0.23359911839446348, 0.23359911839446348, 0.2435934037819271 },
                { -0.06622162366629052, 0.11034865213357695, 0.5130036221887934, 0.5813548671560165, 0.12516536864601804, 1.0, -0.06058984018995384,
                        0.25378533722183977, 0.4263094459271189, 0.4452448648949314, 0.11034865213357695, 0.11034865213357695, 0.446149304907719 },
                { 0.9995107173168294, -0.002148308071200791, -0.2048824527435123, -0.251176272641125, -0.22826962916119573, -0.06058984018995384, 1.0,
                        0.047186248486568036, -0.12334626779914706, -0.25844839422106985, -0.002148308071200791, -0.002148308071200791, -0.28615035938713 },
                { 0.046413002772051466, 0.8083204130337523, 0.47369514919913946, 0.2475578563281484, 0.09519884606443976, 0.25378533722183977,
                        0.047186248486568036, 1.0, 0.912889061689689, 0.18323667580229164, 0.8083204130337523, 0.8083204130337523, 0.3003301609236652 },
                { -0.12638463146425086, 0.6830967821186095, 0.630006642104428, 0.479933038682869, 0.16127241385122823, 0.4263094459271189, -0.12334626779914706,
                        0.912889061689689, 1.0, 0.40349072682174353, 0.6830967821186095, 0.6830967821186095, 0.5463500182343711 },
                { -0.2648581058359268, 0.17092481883773306, 0.7794685675266949, 0.7547291944708587, 0.0491830417531641, 0.4452448648949314,
                        -0.25844839422106985, 0.18323667580229164, 0.40349072682174353, 1.0, 0.17092481883773306, 0.17092481883773306, 0.7175346866777632 },
                { 4.818910644591628E-4, 0.9999999999999998, 0.459704102653577, 0.2329848867647044, 0.23359911839446348, 0.11034865213357695,
                        -0.002148308071200791, 0.8083204130337523, 0.6830967821186095, 0.17092481883773306, 1.0, 0.9999999999999998, 0.2925123800699666 },
                { 4.818910644591628E-4, 0.9999999999999998, 0.459704102653577, 0.2329848867647044, 0.23359911839446348, 0.11034865213357695,
                        -0.002148308071200791, 0.8083204130337523, 0.6830967821186095, 0.17092481883773306, 0.9999999999999998, 1.0, 0.2925123800699666 },
                { -0.29110060025606405, 0.2925123800699666, 0.7797230267924377, 0.7207853187826451, 0.2435934037819271, 0.446149304907719, -0.28615035938713,
                        0.3003301609236652, 0.5463500182343711, 0.7175346866777632, 0.2925123800699666, 0.2925123800699666, 1.0 } };
        final Access2D<Double> tmpCorrMtrx = ArrayUtils.wrapAccess2D(tmpCorrelations);

        final double[] tmpReturns = new double[] { 0.04582240030511955, 0.05556513020194605, 0.0075608287398083035, 0.026401783542103284, 0.019107275479030267,
                0.026634284056767113, 0.049130046880785455, 0.05093391949085686, 0.06147942284537679, 0.04847598428157901, 0.05556513020194605,
                0.05556513020194605, 0.08771127352045523 };
        final double[] tmpRisks = new double[] { 0.023052170765191896, 0.10976390274674515, 0.1491910905975412, 0.2591842059403274, 0.22991874940262647,
                0.1673146496232647, 0.02771164170051353, 0.1088216213067869, 0.12277592098780352, 0.22070588630919719, 0.10976390274674515, 0.10976390274674515,
                0.24176577517424258 };

        final ArrayList<SimpleAsset> tmpAssets = new ArrayList<>(tmpReturns.length);

        for (int i = 0; i < tmpReturns.length; i++) {

            final double tmpMeanReturn = tmpReturns[i];
            final double tmpVolatility = tmpRisks[i];
            final double tmpWeight = 1.0 / tmpReturns.length;

            final SimpleAsset tmpSimpleAsset = new SimpleAsset(tmpMeanReturn, tmpVolatility, tmpWeight);

            tmpAssets.add(tmpSimpleAsset);
        }

        final SimplePortfolio tmpPortfolio = new SimplePortfolio(tmpCorrMtrx, tmpAssets);

        final GeometricBrownianMotion tmpProcess = tmpPortfolio.forecast();
        final PortfolioSimulator tmpSimulator = tmpPortfolio.getSimulator();

        final int tmpNumberOfRealisations = 9999;
        final int tmpNumberOfSteps = 12 * 5;
        final double tmpStepSize = 1.0 / 12.0;
        final SimulationResults tmpProcResults = tmpProcess.simulate(tmpNumberOfRealisations, tmpNumberOfSteps, tmpStepSize);
        final SimulationResults tmpSimResults1 = tmpSimulator.simulate(tmpNumberOfRealisations, tmpNumberOfSteps, tmpStepSize);
        final SimulationResults tmpSimResults2 = tmpSimulator.simulate(tmpNumberOfRealisations, tmpNumberOfSteps, tmpStepSize, 1);

        if (RandomProcessTests.DEBUG) {
            for (int t = 0; t < tmpNumberOfSteps; t++) {
                BasicLogger.debug("t={}\n\tproc={}\n\tsim1={}\n\tsim2={}", (t + 1), tmpProcResults.getSampleSet(t), tmpSimResults1.getSampleSet(t),
                        tmpSimResults2.getSampleSet(t));
            }
        }

    }

    public void testStepping() {

        final PrimitiveDenseStore tmpCorrelation = PrimitiveDenseStore.FACTORY.makeEye(3, 3);

        final GeometricBrownianMotion tmpOrgProc1 = new SimpleAsset(0.0, 0.01, PrimitiveMath.THIRD).forecast();
        final GeometricBrownianMotion tmpOrgProc2 = new SimpleAsset(0.0, 0.02, PrimitiveMath.THIRD).forecast();
        final GeometricBrownianMotion tmpOrgProc3 = new SimpleAsset(0.0, 0.03, PrimitiveMath.THIRD).forecast();

        TestUtils.assertEquals(0.01, tmpOrgProc1.getStandardDeviation(1.0), 0.005);
        TestUtils.assertEquals(0.02, tmpOrgProc2.getStandardDeviation(1.0), 0.005);
        TestUtils.assertEquals(0.03, tmpOrgProc3.getStandardDeviation(1.0), 0.005);

        final ArrayList<GeometricBrownianMotion> tmpProcs = new ArrayList<>();
        tmpProcs.add(tmpOrgProc1);
        tmpProcs.add(tmpOrgProc2);
        tmpProcs.add(tmpOrgProc3);
        final GeometricBrownian1D tmpGB1D = new GeometricBrownian1D(tmpCorrelation, tmpProcs);
        final List<CalendarDateSeries<Double>> tmpSeries = new ArrayList<>();

        tmpSeries.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));
        tmpSeries.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));
        tmpSeries.add(new CalendarDateSeries<Double>(CalendarDateUnit.MONTH));

        CalendarDate tmpCalendarDateKey = CalendarDate.make(CalendarDateUnit.MONTH);

        tmpSeries.get(0).put(tmpCalendarDateKey, tmpOrgProc1.getValue());
        tmpSeries.get(1).put(tmpCalendarDateKey, tmpOrgProc2.getValue());
        tmpSeries.get(2).put(tmpCalendarDateKey, tmpOrgProc3.getValue());

        for (int t = 0; t < 1000; t++) {

            tmpGB1D.step(1.0 / 12.0);
            tmpCalendarDateKey = tmpCalendarDateKey.step(CalendarDateUnit.MONTH);

            tmpSeries.get(0).put(tmpCalendarDateKey, tmpGB1D.getValue(0));
            tmpSeries.get(1).put(tmpCalendarDateKey, tmpGB1D.getValue(1));
            tmpSeries.get(2).put(tmpCalendarDateKey, tmpGB1D.getValue(2));
        }

        final GeometricBrownianMotion tmpNewProc1 = GeometricBrownianMotion.estimate(tmpSeries.get(0).getDataSeries(), 1.0 / 12.0);
        final GeometricBrownianMotion tmpNewProc2 = GeometricBrownianMotion.estimate(tmpSeries.get(1).getDataSeries(), 1.0 / 12.0);
        final GeometricBrownianMotion tmpNewProc3 = GeometricBrownianMotion.estimate(tmpSeries.get(2).getDataSeries(), 1.0 / 12.0);

        TestUtils.assertEquals(0.01, tmpNewProc1.getStandardDeviation(1.0), 0.005);
        TestUtils.assertEquals(0.02, tmpNewProc2.getStandardDeviation(1.0), 0.005);
        TestUtils.assertEquals(0.03, tmpNewProc3.getStandardDeviation(1.0), 0.005);

    }

}
