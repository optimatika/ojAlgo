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
package org.ojalgo.finance;

import static org.ojalgo.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Builder;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.random.Deterministic;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.random.RandomUtils;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.CoordinationSet;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public abstract class FinanceUtils {

    public static double calculateValueAtRisk(final double expRet, final double stdDev, final double confidence, final double time) {

        final double tmpConfidenceScale = SQRT_TWO * RandomUtils.erfi(ONE - (TWO * (ONE - confidence)));

        return PrimitiveFunction.MAX.invoke((PrimitiveFunction.SQRT.invoke(time) * stdDev * tmpConfidenceScale) - (time * expRet), ZERO);
    }

    public static GeometricBrownianMotion estimateExcessDiffusionProcess(final CalendarDateSeries<?> priceSeries,
            final CalendarDateSeries<?> riskFreeInterestRateSeries, final CalendarDateUnit timeUnit) {

        final SampleSet tmpSampleSet = FinanceUtils.makeExcessGrowthRateSampleSet(priceSeries, riskFreeInterestRateSeries);

        // The average number of millis between to subsequent keys in the series.
        double tmpStepSize = priceSeries.getResolution().size();
        // The time between to keys expressed in terms of the specified time meassure and unit.
        tmpStepSize /= timeUnit.size();

        final double tmpExp = tmpSampleSet.getMean();
        final double tmpVar = tmpSampleSet.getVariance();

        final double tmpDiff = PrimitiveFunction.SQRT.invoke(tmpVar / tmpStepSize);
        final double tmpDrift = (tmpExp / tmpStepSize) + ((tmpDiff * tmpDiff) / TWO);

        final GeometricBrownianMotion retVal = new GeometricBrownianMotion(tmpDrift, tmpDiff);

        return retVal;
    }

    public static CalendarDateSeries<RandomNumber> forecast(final CalendarDateSeries<? extends Number> series, final int pointCount,
            final CalendarDateUnit timeUnit, final boolean includeOriginalSeries) {

        final CalendarDateSeries<RandomNumber> retVal = new CalendarDateSeries<>(timeUnit);
        retVal.name(series.getName()).colour(series.getColour());

        final double tmpSamplePeriod = (double) series.getAverageStepSize() / (double) timeUnit.size();
        final GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.estimate(series.getPrimitiveSeries(), tmpSamplePeriod);

        if (includeOriginalSeries) {
            for (final Entry<CalendarDate, ? extends Number> tmpEntry : series.entrySet()) {
                retVal.put(tmpEntry.getKey(), new Deterministic(tmpEntry.getValue()));
            }
        }

        final CalendarDate tmpLastKey = series.lastKey();
        final double tmpLastValue = series.lastValue().doubleValue();

        tmpProcess.setValue(tmpLastValue);

        for (int i = 1; i <= pointCount; i++) {
            retVal.put(tmpLastKey.millis + (i * timeUnit.size()), tmpProcess.getDistribution(i));
        }

        return retVal;
    }

    public static CalendarDateSeries<BigDecimal> makeCalendarPriceSeries(final double[] prices, final Calendar startCalendar,
            final CalendarDateUnit resolution) {

        final CalendarDateSeries<BigDecimal> retVal = new CalendarDateSeries<>(resolution);

        FinanceUtils.copyValues(retVal, new CalendarDate(startCalendar), prices);

        return retVal;
    }

    /**
     * @param timeSeriesCollection
     * @return Annualised covariances
     */
    public static <V extends Number> BasicMatrix makeCovarianceMatrix(final Collection<CalendarDateSeries<V>> timeSeriesCollection) {

        final CoordinationSet<V> tmpCoordinator = new CoordinationSet<>(timeSeriesCollection).prune();

        final ArrayList<SampleSet> tmpSampleSets = new ArrayList<>();
        for (final CalendarDateSeries<V> tmpTimeSeries : timeSeriesCollection) {
            final double[] values = tmpCoordinator.get(tmpTimeSeries.getName()).getPrimitiveValues();
            final int tmpSize1 = values.length - 1;

            final double[] retVal = new double[tmpSize1];

            for (int i = 0; i < tmpSize1; i++) {
                retVal[i] = PrimitiveFunction.LOG.invoke(values[i + 1] / values[i]);
            }
            final SampleSet tmpMakeUsingLogarithmicChanges = SampleSet.wrap(ArrayUtils.wrapAccess1D(retVal));
            tmpSampleSets.add(tmpMakeUsingLogarithmicChanges);
        }

        final int tmpSize = timeSeriesCollection.size();

        final Builder<PrimitiveMatrix> retValStore = PrimitiveMatrix.FACTORY.getBuilder(tmpSize, tmpSize);

        final double tmpToYearFactor = (double) CalendarDateUnit.YEAR.size() / (double) tmpCoordinator.getResolution().size();

        SampleSet tmpRowSet;
        SampleSet tmpColSet;

        for (int j = 0; j < tmpSize; j++) {

            tmpColSet = tmpSampleSets.get(j);

            for (int i = 0; i < tmpSize; i++) {

                tmpRowSet = tmpSampleSets.get(i);

                retValStore.set(i, j, tmpToYearFactor * tmpRowSet.getCovariance(tmpColSet));
            }
        }

        return retValStore.build();
    }

    /**
     * @param listOfTimeSeries An ordered collection of time series
     * @param mayBeMissingValues Individual series may be missing some values - try to fix this or not
     * @return Annualised covariances
     */
    public static <N extends Number> PrimitiveMatrix makeCovarianceMatrix(final List<CalendarDateSeries<N>> listOfTimeSeries,
            final boolean mayBeMissingValues) {

        final int tmpSize = listOfTimeSeries.size();

        final CoordinationSet<N> tmpUncoordinated = new CoordinationSet<>(listOfTimeSeries);
        final CalendarDateUnit tmpDataResolution = tmpUncoordinated.getResolution();
        if (mayBeMissingValues) {
            tmpUncoordinated.complete();
        }

        final CoordinationSet<N> tmpCoordinated = tmpUncoordinated.prune(tmpDataResolution);

        final Builder<PrimitiveMatrix> tmpMatrixBuilder = PrimitiveMatrix.FACTORY.getBuilder(tmpSize, tmpSize);

        final double tmpToYearFactor = (double) CalendarDateUnit.YEAR.size() / (double) tmpDataResolution.size();

        SampleSet tmpSampleSet;
        final SampleSet[] tmpSampleSets = new SampleSet[tmpSize];

        for (int j = 0; j < tmpSize; j++) {

            final PrimitiveSeries tmpPrimitiveSeries = tmpCoordinated.get(listOfTimeSeries.get(j).getName()).getPrimitiveSeries();

            tmpSampleSet = SampleSet.wrap(tmpPrimitiveSeries.quotients().log().toDataSeries());

            tmpMatrixBuilder.set(j, j, tmpToYearFactor * tmpSampleSet.getVariance());

            for (int i = 0; i < j; i++) {

                final double tmpCovariance = tmpToYearFactor * tmpSampleSets[i].getCovariance(tmpSampleSet);
                tmpMatrixBuilder.set(i, j, tmpCovariance);
                tmpMatrixBuilder.set(j, i, tmpCovariance);
            }

            tmpSampleSets[j] = tmpSampleSet;
        }

        return tmpMatrixBuilder.get();
    }

    public static CalendarDateSeries<BigDecimal> makeDatePriceSeries(final double[] prices, final Date startDate, final CalendarDateUnit resolution) {

        final CalendarDateSeries<BigDecimal> retVal = new CalendarDateSeries<>(resolution);

        FinanceUtils.copyValues(retVal, new CalendarDate(startDate), prices);

        return retVal;
    }

    /**
     * @param priceSeries A series of prices
     * @param riskFreeInterestRateSeries A series of interest rates (risk free return expressed in %, 5.0
     *        means 5.0% annualized risk free return)
     * @return A sample set of price growth rates adjusted for risk free return
     */
    public static SampleSet makeExcessGrowthRateSampleSet(final CalendarDateSeries<?> priceSeries, final CalendarDateSeries<?> riskFreeInterestRateSeries) {

        if (priceSeries.size() != riskFreeInterestRateSeries.size()) {
            throw new IllegalArgumentException("The two series must have the same size (number of elements).");
        }

        if (!priceSeries.firstKey().equals(riskFreeInterestRateSeries.firstKey())) {
            throw new IllegalArgumentException("The two series must have the same first key (date or calendar).");
        }

        if (!priceSeries.lastKey().equals(riskFreeInterestRateSeries.lastKey())) {
            throw new IllegalArgumentException("The two series must have the same last key (date or calendar).");
        }

        final double[] tmpPrices = priceSeries.getPrimitiveValues();
        final double[] tmpRiskFreeInterestRates = riskFreeInterestRateSeries.getPrimitiveValues();

        final Array1D<Double> retVal = Array1D.PRIMITIVE64.makeZero(tmpPrices.length - 1);

        final CalendarDateUnit tmpUnit = priceSeries.getResolution();
        double tmpThisRiskFree, tmpNextRiskFree, tmpAvgRiskFree, tmpRiskFreeGrowthRate, tmpThisPrice, tmpNextPrice, tmpPriceGrowthFactor, tmpPriceGrowthRate,
                tmpAdjustedPriceGrowthRate;

        for (int i = 0; i < retVal.size(); i++) {

            tmpThisRiskFree = tmpRiskFreeInterestRates[i] / PrimitiveMath.HUNDRED;
            tmpNextRiskFree = tmpRiskFreeInterestRates[i + 1] / PrimitiveMath.HUNDRED;
            tmpAvgRiskFree = (tmpThisRiskFree + tmpNextRiskFree) / PrimitiveMath.TWO;
            tmpRiskFreeGrowthRate = FinanceUtils.toGrowthRateFromAnnualReturn(tmpAvgRiskFree, tmpUnit);

            tmpThisPrice = tmpPrices[i];
            tmpNextPrice = tmpPrices[i + 1];
            tmpPriceGrowthFactor = tmpNextPrice / tmpThisPrice;
            tmpPriceGrowthRate = PrimitiveFunction.LOG.invoke(tmpPriceGrowthFactor);

            tmpAdjustedPriceGrowthRate = tmpPriceGrowthRate - tmpRiskFreeGrowthRate;

            retVal.set(i, tmpAdjustedPriceGrowthRate);
        }

        return SampleSet.wrap(retVal);
    }

    /**
     * @param priceSeries A series of prices
     * @param riskFreeInterestRateSeries A series of interest rates (risk free return expressed in %, 5.0
     *        means 5.0% annualized risk free return)
     * @return A sample set of price growth rates adjusted for risk free return
     */
    public static CalendarDateSeries<Double> makeNormalisedExcessPrice(final CalendarDateSeries<?> priceSeries,
            final CalendarDateSeries<?> riskFreeInterestRateSeries) {

        if (priceSeries.size() != riskFreeInterestRateSeries.size()) {
            throw new IllegalArgumentException("The two series must have the same size (number of elements).");
        }

        if (!priceSeries.firstKey().equals(riskFreeInterestRateSeries.firstKey())) {
            throw new IllegalArgumentException("The two series must have the same first key (date or calendar).");
        }

        if (!priceSeries.lastKey().equals(riskFreeInterestRateSeries.lastKey())) {
            throw new IllegalArgumentException("The two series must have the same last key (date or calendar).");
        }

        final long[] tmpDates = priceSeries.getPrimitiveKeys();
        final double[] tmpPrices = priceSeries.getPrimitiveValues();
        final double[] tmpRiskFreeInterestRates = riskFreeInterestRateSeries.getPrimitiveValues();

        final CalendarDateUnit tmpResolution = priceSeries.getResolution();

        final CalendarDateSeries<Double> retVal = new CalendarDateSeries<>(tmpResolution);

        double tmpThisRiskFree, tmpLastRiskFree, tmpAvgRiskFree, tmpRiskFreeGrowthFactor, tmpThisPrice, tmpLastPrice, tmpPriceGrowthFactor,
                tmpAdjustedPriceGrowthFactor;

        double tmpAggregatedExcessPrice = PrimitiveMath.ONE;
        retVal.put(new CalendarDate(tmpDates[0]), tmpAggregatedExcessPrice);
        for (int i = 1; i < priceSeries.size(); i++) {

            tmpThisRiskFree = tmpRiskFreeInterestRates[i] / PrimitiveMath.HUNDRED;
            tmpLastRiskFree = tmpRiskFreeInterestRates[i - 1] / PrimitiveMath.HUNDRED;
            tmpAvgRiskFree = (tmpThisRiskFree + tmpLastRiskFree) / PrimitiveMath.TWO;
            tmpRiskFreeGrowthFactor = FinanceUtils.toGrowthFactorFromAnnualReturn(tmpAvgRiskFree, tmpResolution);

            tmpThisPrice = tmpPrices[i];
            tmpLastPrice = tmpPrices[i - 1];
            tmpPriceGrowthFactor = tmpThisPrice / tmpLastPrice;

            tmpAdjustedPriceGrowthFactor = tmpPriceGrowthFactor / tmpRiskFreeGrowthFactor;

            tmpAggregatedExcessPrice *= tmpAdjustedPriceGrowthFactor;

            retVal.put(new CalendarDate(tmpDates[i]), tmpAggregatedExcessPrice);
        }

        return retVal.name(priceSeries.getName()).colour(priceSeries.getColour());
    }

    /**
     * GrowthRate = ln(GrowthFactor)
     *
     * @param growthFactor A growth factor per unit (day, week, month, year...)
     * @param growthFactorUnit A growth factor unit
     * @return Annualised return (percentage per year)
     */
    public static double toAnnualReturnFromGrowthFactor(final double growthFactor, final CalendarDateUnit growthFactorUnit) {
        final double tmpGrowthFactorUnitsPerYear = growthFactorUnit.convert(CalendarDateUnit.YEAR);
        return PrimitiveFunction.POW.invoke(growthFactor, tmpGrowthFactorUnitsPerYear) - PrimitiveMath.ONE;
    }

    /**
     * AnnualReturn = exp(GrowthRate * GrowthRateUnitsPerYear) - 1.0
     *
     * @param growthRate A growth rate per unit (day, week, month, year...)
     * @param growthRateUnit A growth rate unit
     * @return Annualised return (percentage per year)
     */
    public static double toAnnualReturnFromGrowthRate(final double growthRate, final CalendarDateUnit growthRateUnit) {
        final double tmpGrowthRateUnitsPerYear = growthRateUnit.convert(CalendarDateUnit.YEAR);
        return PrimitiveFunction.EXPM1.invoke(growthRate * tmpGrowthRateUnitsPerYear);
    }

    public static PrimitiveMatrix toCorrelations(final Access2D<?> covariances) {
        return FinanceUtils.toCorrelations(covariances, false);
    }

    /**
     * Will extract the correlation coefficients from the input covariance matrix. If "cleaning" is enabled
     * small and negative eigenvalues of the covariance matrix will be replaced with a new minimal value.
     */
    public static PrimitiveMatrix toCorrelations(final Access2D<?> covariances, final boolean clean) {

        final int tmpSize = (int) Math.min(covariances.countRows(), covariances.countColumns());

        MatrixStore<Double> tmpCovariances = MatrixStore.PRIMITIVE.makeWrapper(covariances).get();

        if (clean) {

            final Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(true);
            tmpEvD.decompose(tmpCovariances);

            final MatrixStore<Double> tmpV = tmpEvD.getV();
            final PhysicalStore<Double> tmpD = tmpEvD.getD().copy();

            final double tmpLargest = tmpEvD.getEigenvalues().get(0).norm();
            final double tmpLimit = tmpLargest * tmpSize * PrimitiveFunction.SQRT.invoke(PrimitiveMath.MACHINE_EPSILON);

            for (int ij = 0; ij < tmpSize; ij++) {
                if (tmpD.doubleValue(ij, ij) < tmpLimit) {
                    tmpD.set(ij, ij, tmpLimit);
                }
            }

            final MatrixStore<Double> tmpLeft = tmpV;
            final MatrixStore<Double> tmpMiddle = tmpD;
            final MatrixStore<Double> tmpRight = tmpLeft.transpose();

            tmpCovariances = tmpLeft.multiply(tmpMiddle).multiply(tmpRight);
        }

        final Builder<PrimitiveMatrix> retVal = PrimitiveMatrix.FACTORY.getBuilder(tmpSize, tmpSize);

        final double[] tmpVolatilities = new double[tmpSize];
        for (int ij = 0; ij < tmpSize; ij++) {
            tmpVolatilities[ij] = PrimitiveFunction.SQRT.invoke(tmpCovariances.doubleValue(ij, ij));
        }

        for (int j = 0; j < tmpSize; j++) {
            final double tmpColVol = tmpVolatilities[j];
            retVal.set(j, j, PrimitiveMath.ONE);
            for (int i = j + 1; i < tmpSize; i++) {
                final double tmpCovariance = tmpCovariances.doubleValue(i, j);
                final double tmpCorrelation = tmpCovariance / (tmpVolatilities[i] * tmpColVol);
                retVal.set(i, j, tmpCorrelation);
                retVal.set(j, i, tmpCorrelation);
            }
        }

        return retVal.get();
    }

    /**
     * Vill constract a covariance matrix from the standard deviations (volatilities) and correlation
     * coefficient,
     */
    public static PrimitiveMatrix toCovariances(final Access1D<?> volatilities, final Access2D<?> correlations) {

        final int tmpSize = (int) volatilities.count();

        final Builder<PrimitiveMatrix> retVal = PrimitiveMatrix.FACTORY.getBuilder(tmpSize, tmpSize);

        for (int j = 0; j < tmpSize; j++) {
            final double tmpColumnVolatility = volatilities.doubleValue(j);
            retVal.set(j, j, tmpColumnVolatility * tmpColumnVolatility);
            for (int i = j + 1; i < tmpSize; i++) {
                final double tmpCovariance = volatilities.doubleValue(i) * correlations.doubleValue(i, j) * tmpColumnVolatility;
                retVal.set(i, j, tmpCovariance);
                retVal.set(j, i, tmpCovariance);
            }
        }

        return retVal.get();
    }

    /**
     * GrowthFactor = exp(GrowthRate)
     *
     * @param annualReturn Annualised return (percentage per year)
     * @param growthFactorUnit A growth factor unit
     * @return A growth factor per unit (day, week, month, year...)
     */
    public static double toGrowthFactorFromAnnualReturn(final double annualReturn, final CalendarDateUnit growthFactorUnit) {
        final double tmpAnnualGrowthFactor = PrimitiveMath.ONE + annualReturn;
        final double tmpYearsPerGrowthFactorUnit = CalendarDateUnit.YEAR.convert(growthFactorUnit);
        return PrimitiveFunction.POW.invoke(tmpAnnualGrowthFactor, tmpYearsPerGrowthFactorUnit);
    }

    /**
     * GrowthRate = ln(1.0 + InterestRate) / GrowthRateUnitsPerYear
     *
     * @param annualReturn Annualised return (percentage per year)
     * @param growthRateUnit A growth rate unit
     * @return A growth rate per unit (day, week, month, year...)
     */
    public static double toGrowthRateFromAnnualReturn(final double annualReturn, final CalendarDateUnit growthRateUnit) {
        final double tmpAnnualGrowthRate = PrimitiveFunction.LOG1P.invoke(annualReturn);
        final double tmpYearsPerGrowthRateUnit = CalendarDateUnit.YEAR.convert(growthRateUnit);
        return tmpAnnualGrowthRate * tmpYearsPerGrowthRateUnit;
    }

    public static PrimitiveMatrix toVolatilities(final Access2D<?> covariances) {
        return FinanceUtils.toVolatilities(covariances, false);
    }

    /**
     * Will extract the standard deviations (volatilities) from the input covariance matrix. If "cleaning" is
     * enabled small variances will be replaced with a new minimal value.
     */
    public static PrimitiveMatrix toVolatilities(final Access2D<?> covariances, final boolean clean) {

        final int tmpSize = (int) Math.min(covariances.countRows(), covariances.countColumns());

        final Builder<PrimitiveMatrix> retVal = PrimitiveMatrix.FACTORY.getBuilder(tmpSize);

        if (clean) {

            final AggregatorFunction<Double> tmpLargest = PrimitiveAggregator.LARGEST.get();
            MatrixStore.PRIMITIVE.makeWrapper(covariances).get().visitDiagonal(0, 0, tmpLargest);
            final double tmpLimit = tmpLargest.doubleValue() * tmpSize * PrimitiveFunction.SQRT.invoke(PrimitiveMath.MACHINE_EPSILON);

            for (int ij = 0; ij < tmpSize; ij++) {
                final double tmpVariance = covariances.doubleValue(ij, ij);
                if (tmpVariance < tmpLimit) {
                    retVal.set(ij, PrimitiveFunction.SQRT.invoke(tmpLimit));
                } else {
                    retVal.set(ij, PrimitiveFunction.SQRT.invoke(tmpVariance));
                }
            }

        } else {

            for (int ij = 0; ij < tmpSize; ij++) {
                retVal.set(ij, PrimitiveFunction.SQRT.invoke(covariances.doubleValue(ij, ij)));
            }
        }

        return retVal.get();
    }

    private static <K extends Comparable<? super K>> void copyValues(final CalendarDateSeries<BigDecimal> series, final CalendarDate firstKey,
            final double[] values) {

        CalendarDate tmpKey = firstKey;

        for (int tmpValueIndex = 0; tmpValueIndex < values.length; tmpValueIndex++) {

            series.put(tmpKey, new BigDecimal(values[tmpValueIndex]));

            tmpKey = series.step(tmpKey);
        }
    }

    private FinanceUtils() {
        super();
    }

}
