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
package org.ojalgo.data.domain.finance;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.ojalgo.array.Array1D;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.ErrorFunction;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Deterministic;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.process.GeometricBrownianMotion;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.series.CalendarDateSeries;
import org.ojalgo.series.CoordinationSet;
import org.ojalgo.series.primitive.PrimitiveSeries;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.CalendarDate;
import org.ojalgo.type.CalendarDateUnit;

public abstract class FinanceUtils {

    public static double calculateValueAtRisk(final double expRet, final double stdDev, final double confidence, final double time) {

        double tmpConfidenceScale = SQRT_TWO * ErrorFunction.erfi(ONE - TWO * (ONE - confidence));

        return PrimitiveMath.MAX.invoke(PrimitiveMath.SQRT.invoke(time) * stdDev * tmpConfidenceScale - time * expRet, ZERO);
    }

    public static GeometricBrownianMotion estimateExcessDiffusionProcess(final CalendarDateSeries<?> priceSeries,
            final CalendarDateSeries<?> riskFreeInterestRateSeries, final CalendarDateUnit timeUnit) {

        SampleSet tmpSampleSet = FinanceUtils.makeExcessGrowthRateSampleSet(priceSeries, riskFreeInterestRateSeries);

        // The average number of millis between to subsequent keys in the series.
        double tmpStepSize = priceSeries.getResolution().toDurationInMillis();
        // The time between to keys expressed in terms of the specified time meassure and unit.
        tmpStepSize /= timeUnit.toDurationInMillis();

        double tmpExp = tmpSampleSet.getMean();
        double tmpVar = tmpSampleSet.getVariance();

        double tmpDiff = PrimitiveMath.SQRT.invoke(tmpVar / tmpStepSize);
        double tmpDrift = tmpExp / tmpStepSize + tmpDiff * tmpDiff / TWO;

        return new GeometricBrownianMotion(tmpDrift, tmpDiff);
    }

    public static CalendarDateSeries<RandomNumber> forecast(final CalendarDateSeries<? extends Comparable<?>> series, final int pointCount,
            final CalendarDateUnit timeUnit, final boolean includeOriginalSeries) {

        CalendarDateSeries<RandomNumber> retVal = new CalendarDateSeries<>(timeUnit);
        retVal.name(series.getName()).colour(series.getColour());

        double tmpSamplePeriod = (double) series.getAverageStepSize() / (double) timeUnit.toDurationInMillis();
        GeometricBrownianMotion tmpProcess = GeometricBrownianMotion.estimate(series.asPrimitive(), tmpSamplePeriod);

        if (includeOriginalSeries) {
            for (Entry<CalendarDate, ? extends Comparable<?>> tmpEntry : series.entrySet()) {
                retVal.put(tmpEntry.getKey(), new Deterministic(tmpEntry.getValue()));
            }
        }

        CalendarDate tmpLastKey = series.lastKey();
        double tmpLastValue = Scalar.doubleValue(series.lastValue());

        tmpProcess.setValue(tmpLastValue);

        for (int i = 1; i <= pointCount; i++) {
            retVal.put(tmpLastKey.step(i, timeUnit), tmpProcess.getDistribution(i));
        }

        return retVal;
    }

    public static CalendarDateSeries<BigDecimal> makeCalendarPriceSeries(final double[] prices, final Calendar startCalendar,
            final CalendarDateUnit resolution) {

        CalendarDateSeries<BigDecimal> retVal = new CalendarDateSeries<>(resolution);

        FinanceUtils.copyValues(retVal, new CalendarDate(startCalendar), prices);

        return retVal;
    }

    /**
     * @return Annualised covariances
     */
    public static <V extends Comparable<V>> MatrixR064 makeCovarianceMatrix(final Collection<CalendarDateSeries<V>> timeSeriesCollection) {

        CoordinationSet<V> tmpCoordinator = new CoordinationSet<>(timeSeriesCollection).prune();

        ArrayList<SampleSet> tmpSampleSets = new ArrayList<>();
        for (CalendarDateSeries<V> tmpTimeSeries : timeSeriesCollection) {
            double[] values = tmpCoordinator.get(tmpTimeSeries.getName()).asPrimitive().toRawCopy1D();
            int tmpSize1 = values.length - 1;

            double[] retVal = new double[tmpSize1];

            for (int i = 0; i < tmpSize1; i++) {
                retVal[i] = PrimitiveMath.LOG.invoke(values[i + 1] / values[i]);
            }
            SampleSet tmpMakeUsingLogarithmicChanges = SampleSet.wrap(Access1D.wrap(retVal));
            tmpSampleSets.add(tmpMakeUsingLogarithmicChanges);
        }

        int tmpSize = timeSeriesCollection.size();

        MatrixR064.DenseReceiver retValStore = MatrixR064.FACTORY.makeDense(tmpSize, tmpSize);

        double tmpToYearFactor = (double) CalendarDateUnit.YEAR.toDurationInMillis() / (double) tmpCoordinator.getResolution().toDurationInMillis();

        SampleSet tmpRowSet;
        SampleSet tmpColSet;

        for (int j = 0; j < tmpSize; j++) {

            tmpColSet = tmpSampleSets.get(j);

            for (int i = 0; i < tmpSize; i++) {

                tmpRowSet = tmpSampleSets.get(i);

                retValStore.set(i, j, tmpToYearFactor * tmpRowSet.getCovariance(tmpColSet));
            }
        }

        return retValStore.get();
    }

    /**
     * @param listOfTimeSeries An ordered collection of time series
     * @param mayBeMissingValues Individual series may be missing some values - try to fix this or not
     * @return Annualised covariances
     */
    public static <N extends Comparable<N>> MatrixR064 makeCovarianceMatrix(final List<CalendarDateSeries<N>> listOfTimeSeries,
            final boolean mayBeMissingValues) {

        int tmpSize = listOfTimeSeries.size();

        CoordinationSet<N> tmpUncoordinated = new CoordinationSet<>(listOfTimeSeries);
        CalendarDateUnit tmpDataResolution = tmpUncoordinated.getResolution();
        if (mayBeMissingValues) {
            tmpUncoordinated.complete();
        }

        CoordinationSet<N> tmpCoordinated = tmpUncoordinated.prune(tmpDataResolution);

        MatrixR064.DenseReceiver tmpMatrixBuilder = MatrixR064.FACTORY.makeDense(tmpSize, tmpSize);

        double tmpToYearFactor = (double) CalendarDateUnit.YEAR.toDurationInMillis() / (double) tmpDataResolution.toDurationInMillis();

        SampleSet tmpSampleSet;
        SampleSet[] tmpSampleSets = new SampleSet[tmpSize];

        for (int j = 0; j < tmpSize; j++) {

            PrimitiveSeries tmpPrimitiveSeries = tmpCoordinated.get(listOfTimeSeries.get(j).getName()).asPrimitive();

            tmpSampleSet = SampleSet.wrap(tmpPrimitiveSeries.quotients().log().toDataSeries());

            tmpMatrixBuilder.set(j, j, tmpToYearFactor * tmpSampleSet.getVariance());

            for (int i = 0; i < j; i++) {

                double tmpCovariance = tmpToYearFactor * tmpSampleSets[i].getCovariance(tmpSampleSet);
                tmpMatrixBuilder.set(i, j, tmpCovariance);
                tmpMatrixBuilder.set(j, i, tmpCovariance);
            }

            tmpSampleSets[j] = tmpSampleSet;
        }

        return tmpMatrixBuilder.get();
    }

    public static CalendarDateSeries<BigDecimal> makeDatePriceSeries(final double[] prices, final Date startDate, final CalendarDateUnit resolution) {

        CalendarDateSeries<BigDecimal> retVal = new CalendarDateSeries<>(resolution);

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

        double[] tmpPrices = priceSeries.asPrimitive().toRawCopy1D();
        double[] tmpRiskFreeInterestRates = riskFreeInterestRateSeries.asPrimitive().toRawCopy1D();

        Array1D<Double> retVal = Array1D.R064.make(tmpPrices.length - 1);

        CalendarDateUnit tmpUnit = priceSeries.getResolution();
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
            tmpPriceGrowthRate = PrimitiveMath.LOG.invoke(tmpPriceGrowthFactor);

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

        long[] tmpDates = priceSeries.getPrimitiveKeys();
        double[] tmpPrices = priceSeries.asPrimitive().toRawCopy1D();
        double[] tmpRiskFreeInterestRates = riskFreeInterestRateSeries.asPrimitive().toRawCopy1D();

        CalendarDateUnit tmpResolution = priceSeries.getResolution();

        CalendarDateSeries<Double> retVal = new CalendarDateSeries<>(tmpResolution);

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
        double tmpGrowthFactorUnitsPerYear = growthFactorUnit.convert(CalendarDateUnit.YEAR);
        return PrimitiveMath.POW.invoke(growthFactor, tmpGrowthFactorUnitsPerYear) - PrimitiveMath.ONE;
    }

    /**
     * AnnualReturn = exp(GrowthRate * GrowthRateUnitsPerYear) - 1.0
     *
     * @param growthRate A growth rate per unit (day, week, month, year...)
     * @param growthRateUnit A growth rate unit
     * @return Annualised return (percentage per year)
     */
    public static double toAnnualReturnFromGrowthRate(final double growthRate, final CalendarDateUnit growthRateUnit) {
        double tmpGrowthRateUnitsPerYear = growthRateUnit.convert(CalendarDateUnit.YEAR);
        return PrimitiveMath.EXPM1.invoke(growthRate * tmpGrowthRateUnitsPerYear);
    }

    public static MatrixR064 toCorrelations(final Access2D<?> covariances) {
        return FinanceUtils.toCorrelations(covariances, false);
    }

    /**
     * Will extract the correlation coefficients from the input covariance matrix. If "cleaning" is enabled
     * small and negative eigenvalues of the covariance matrix will be replaced with a new minimal value.
     */
    public static MatrixR064 toCorrelations(final Access2D<?> covariances, final boolean clean) {

        int size = Math.toIntExact(Math.min(covariances.countRows(), covariances.countColumns()));

        MatrixStore<Double> covarianceMtrx = Primitive64Store.FACTORY.makeWrapper(covariances);

        if (clean) {

            Eigenvalue<Double> evd = Eigenvalue.PRIMITIVE.make(covarianceMtrx, true);
            evd.decompose(covarianceMtrx);

            MatrixStore<Double> mtrxV = evd.getV();
            PhysicalStore<Double> mtrxD = evd.getD().copy();

            double largest = evd.getEigenvalues().get(0).norm();
            double limit = largest * size * PrimitiveMath.RELATIVELY_SMALL;

            for (int ij = 0; ij < size; ij++) {
                if (mtrxD.doubleValue(ij, ij) < limit) {
                    mtrxD.set(ij, ij, limit);
                }
            }

            covarianceMtrx = mtrxV.multiply(mtrxD).multiply(mtrxV.transpose());
        }

        MatrixR064.DenseReceiver retVal = MatrixR064.FACTORY.makeDense(size, size);

        double[] volatilities = new double[size];
        for (int ij = 0; ij < size; ij++) {
            volatilities[ij] = PrimitiveMath.SQRT.invoke(covarianceMtrx.doubleValue(ij, ij));
        }

        for (int j = 0; j < size; j++) {
            double colVol = volatilities[j];

            retVal.set(j, j, PrimitiveMath.ONE);

            for (int i = j + 1; i < size; i++) {
                double rowVol = volatilities[i];

                if (rowVol <= PrimitiveMath.ZERO || colVol <= PrimitiveMath.ZERO) {

                    retVal.set(i, j, PrimitiveMath.ZERO);
                    retVal.set(j, i, PrimitiveMath.ZERO);

                } else {

                    double covariance = covarianceMtrx.doubleValue(i, j);
                    double correlation = covariance / (rowVol * colVol);

                    retVal.set(i, j, correlation);
                    retVal.set(j, i, correlation);
                }
            }
        }

        return retVal.get();
    }

    /**
     * Vill constract a covariance matrix from the standard deviations (volatilities) and correlation
     * coefficient,
     */
    public static MatrixR064 toCovariances(final Access1D<?> volatilities, final Access2D<?> correlations) {

        int tmpSize = (int) volatilities.count();

        MatrixR064.DenseReceiver retVal = MatrixR064.FACTORY.makeDense(tmpSize, tmpSize);

        for (int j = 0; j < tmpSize; j++) {
            double tmpColumnVolatility = volatilities.doubleValue(j);
            retVal.set(j, j, tmpColumnVolatility * tmpColumnVolatility);
            for (int i = j + 1; i < tmpSize; i++) {
                double tmpCovariance = volatilities.doubleValue(i) * correlations.doubleValue(i, j) * tmpColumnVolatility;
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
        double tmpAnnualGrowthFactor = PrimitiveMath.ONE + annualReturn;
        double tmpYearsPerGrowthFactorUnit = CalendarDateUnit.YEAR.convert(growthFactorUnit);
        return PrimitiveMath.POW.invoke(tmpAnnualGrowthFactor, tmpYearsPerGrowthFactorUnit);
    }

    /**
     * GrowthRate = ln(1.0 + InterestRate) / GrowthRateUnitsPerYear
     *
     * @param annualReturn Annualised return (percentage per year)
     * @param growthRateUnit A growth rate unit
     * @return A growth rate per unit (day, week, month, year...)
     */
    public static double toGrowthRateFromAnnualReturn(final double annualReturn, final CalendarDateUnit growthRateUnit) {
        double tmpAnnualGrowthRate = PrimitiveMath.LOG1P.invoke(annualReturn);
        double tmpYearsPerGrowthRateUnit = CalendarDateUnit.YEAR.convert(growthRateUnit);
        return tmpAnnualGrowthRate * tmpYearsPerGrowthRateUnit;
    }

    public static MatrixR064 toVolatilities(final Access2D<?> covariances) {
        return FinanceUtils.toVolatilities(covariances, false);
    }

    /**
     * Will extract the standard deviations (volatilities) from the input covariance matrix. If "cleaning" is
     * enabled small variances will be replaced with a new minimal value.
     */
    public static MatrixR064 toVolatilities(final Access2D<?> covariances, final boolean clean) {

        int size = Math.toIntExact(Math.min(covariances.countRows(), covariances.countColumns()));

        MatrixR064.DenseReceiver retVal = MatrixR064.FACTORY.makeDense(size);

        if (clean) {

            MatrixStore<Double> covarianceMtrx = Primitive64Store.FACTORY.makeWrapper(covariances);

            double largest = covarianceMtrx.aggregateDiagonal(Aggregator.LARGEST).doubleValue();
            double limit = largest * size * PrimitiveMath.RELATIVELY_SMALL;
            double smallest = PrimitiveMath.SQRT.invoke(limit);

            for (int ij = 0; ij < size; ij++) {
                double variance = covariances.doubleValue(ij, ij);

                if (variance < limit) {
                    retVal.set(ij, smallest);
                } else {
                    retVal.set(ij, PrimitiveMath.SQRT.invoke(variance));
                }
            }

        } else {

            for (int ij = 0; ij < size; ij++) {
                double variance = covariances.doubleValue(ij, ij);

                if (variance <= PrimitiveMath.ZERO) {
                    retVal.set(ij, PrimitiveMath.ZERO);
                } else {
                    retVal.set(ij, PrimitiveMath.SQRT.invoke(variance));
                }
            }
        }

        return retVal.get();
    }

    private static <K extends Comparable<? super K>> void copyValues(final CalendarDateSeries<BigDecimal> series, final CalendarDate firstKey,
            final double[] values) {

        CalendarDate tmpKey = firstKey;

        for (int i = 0; i < values.length; i++) {

            series.put(tmpKey, BigDecimal.valueOf(values[i]));

            tmpKey = series.step(tmpKey);
        }
    }

    private FinanceUtils() {
        super();
    }

}
