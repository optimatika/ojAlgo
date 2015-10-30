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
package org.ojalgo.random.process;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.Normal1D;
import org.ojalgo.type.keyvalue.ComparableToDouble;

/**
 * A Gaussian process is a stochastic process whose realizations consist of random values associated with
 * every point in a range of times (or of space) such that each such random variable has a normal
 * distribution. Moreover, every finite collection of those random variables has a multivariate normal
 * distribution. A random field is a generalization of a stochastic process such that the underlying parameter
 * need no longer be a simple real or integer valued "time", but can instead take values that are
 * multidimensional vectors, or points on some manifold. This GaussianField class is a generalization, as well
 * as the underlying implementation, of {@linkplain GaussianProcess}. Prior to calling
 * {@linkplain #getDistribution(Comparable...)} you must call {@linkplain #addObservation(Comparable, double)}
 * one or more times.
 *
 * @author apete
 */
public final class GaussianField<K extends Comparable<K>> {

    public static interface Covariance<K extends Comparable<K>> {

        void calibrate(Collection<ComparableToDouble<K>> observations, Mean<K> mean);

        double invoke(K key1, K key2);

    }

    public static interface Mean<K extends Comparable<K>> {

        void calibrate(Collection<ComparableToDouble<K>> observations);

        double invoke(K key);

    }

    private static final Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    private static <K extends Comparable<K>> Mean<K> mean() {
        return new Mean<K>() {

            public void calibrate(final Collection<ComparableToDouble<K>> observations) {

            }

            public double invoke(final K anArg) {
                return ZERO;
            };
        };
    }

    private final Covariance<K> myCovarianceFunction;
    private final Mean<K> myMeanFunction;

    private final TreeSet<ComparableToDouble<K>> myObservations;

    @SuppressWarnings("unchecked")
    public GaussianField(final Covariance<K> covarFunc) {
        this((Mean<K>) GaussianField.mean(), covarFunc, new TreeSet<ComparableToDouble<K>>());
    }

    public GaussianField(final Mean<K> meanFunc, final Covariance<K> covarFunc) {
        this(meanFunc, covarFunc, new TreeSet<ComparableToDouble<K>>());
    }

    @SuppressWarnings("unused")
    private GaussianField() {
        this(null, null, null);
    }

    @SuppressWarnings("unchecked")
    GaussianField(final Covariance<K> covarFunc, final TreeSet<ComparableToDouble<K>> observations) {
        this((Mean<K>) GaussianField.mean(), covarFunc, observations);
    }

    GaussianField(final Mean<K> meanFunc, final Covariance<K> covarFunc, final TreeSet<ComparableToDouble<K>> observations) {

        super();

        myMeanFunction = meanFunc;
        myCovarianceFunction = covarFunc;
        myObservations = observations;
    }

    public void addObservation(final K key, final double value) {
        myObservations.add(new ComparableToDouble<K>(key, value));
    }

    public void calibrate() {
        myMeanFunction.calibrate(myObservations);
        myCovarianceFunction.calibrate(myObservations, myMeanFunction);
    }

    public Normal1D getDistribution(final boolean cleanCovariances, final K... evaluationPoint) {

        final MatrixStore<Double> tmpRegCoef = this.getRegressionCoefficients(evaluationPoint);

        final MatrixStore<Double> tmpM1 = this.getM1(evaluationPoint);
        final MatrixStore<Double> tmpM2differenses = this.getM2differenses();

        final PrimitiveDenseStore tmpLocations = FACTORY.makeZero(tmpM1.countRows(), tmpM1.countColumns());
        tmpLocations.fillMatching(tmpM1, ADD, tmpRegCoef.multiply(tmpM2differenses));

        final MatrixStore<Double> tmpC11 = this.getC11(evaluationPoint);
        final MatrixStore<Double> tmpC21 = this.getC21(evaluationPoint);

        final PrimitiveDenseStore tmpCovariances = FACTORY.makeZero(tmpC11.countRows(), tmpC11.countColumns());
        tmpCovariances.fillMatching(tmpC11, SUBTRACT, tmpRegCoef.multiply(tmpC21));

        if (cleanCovariances) {

            final Eigenvalue<Double> tmpEvD = Eigenvalue.makePrimitive(true);
            tmpEvD.decompose(tmpCovariances);

            final MatrixStore<Double> tmpV = tmpEvD.getV();
            final PhysicalStore<Double> tmpD = tmpEvD.getD().copy();

            final double tmpLargest = tmpD.doubleValue(0, 0);
            final double tmpLimit = Math.max(PrimitiveMath.MACHINE_EPSILON * tmpLargest, 1E-12);

            final int tmpLength = (int) Math.min(tmpD.countRows(), tmpD.countColumns());
            for (int ij = 0; ij < tmpLength; ij++) {
                if (tmpD.doubleValue(ij, ij) < tmpLimit) {
                    tmpD.set(ij, ij, tmpLimit);
                }
            }

            tmpCovariances.fillMatching(tmpV.multiply(tmpD).multiply(tmpV.builder().transpose().build()));
        }

        return new Normal1D(tmpLocations, tmpCovariances);
    }

    public Normal1D getDistribution(final K... evaluationPoint) {
        return this.getDistribution(false, evaluationPoint);
    }

    MatrixStore<Double> getC11(final K[] args) {

        final int tmpLength = args.length;

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpLength, tmpLength);

        for (int j = 0; j < tmpLength; j++) {
            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(args[i], args[j]));
            }
        }

        return retVal;
    }

    MatrixStore<Double> getC12(final K[] args) {

        final List<ComparableToDouble<K>> tmpObservations = this.getObservations();

        final int tmpRowDim = args.length;
        final int tmpColDim = tmpObservations.size();

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpRowDim, tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(args[i], tmpObservations.get(j).key));
            }
        }

        return retVal;
    }

    MatrixStore<Double> getC21(final K[] args) {

        final List<ComparableToDouble<K>> tmpObservations = this.getObservations();

        final int tmpRowDim = tmpObservations.size();
        final int tmpColDim = args.length;

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpRowDim, tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(tmpObservations.get(i).key, args[j]));
            }
        }

        return retVal;
    }

    MatrixDecomposition.Solver<Double> getC22() {

        final List<ComparableToDouble<K>> tmpObservations = this.getObservations();

        final int tmpSize = tmpObservations.size();

        final PrimitiveDenseStore tmpMatrix = FACTORY.makeZero(tmpSize, tmpSize);

        for (int j = 0; j < tmpSize; j++) {
            final K tmpColumnKey = tmpObservations.get(j).key;
            for (int i = 0; i < tmpSize; i++) {
                tmpMatrix.set(i, j, myCovarianceFunction.invoke(tmpObservations.get(i).key, tmpColumnKey));
            }
        }

        final SingularValue<Double> retVal = SingularValue.makePrimitive();

        retVal.decompose(tmpMatrix);

        return retVal;
    }

    MatrixStore<Double> getM1(final K[] args) {

        final int tmpLength = args.length;

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpLength, 1);

        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, 0, myMeanFunction.invoke(args[i]));
        }

        return retVal;
    }

    MatrixStore<Double> getM2() {

        final List<ComparableToDouble<K>> tmpObservations = this.getObservations();

        final int tmpSize = tmpObservations.size();

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpSize, 1);

        for (int i = 0; i < tmpSize; i++) {
            retVal.set(i, 0, myMeanFunction.invoke(tmpObservations.get(i).key));
        }

        return retVal;
    }

    MatrixStore<Double> getM2differenses() {

        final List<ComparableToDouble<K>> tmpObservations = this.getObservations();

        final int tmpSize = tmpObservations.size();

        final PrimitiveDenseStore retVal = FACTORY.makeZero(tmpSize, 1);

        ComparableToDouble<K> tmpObservation;
        double tmpDiff;
        for (int i = 0; i < tmpSize; i++) {
            tmpObservation = tmpObservations.get(i);
            tmpDiff = tmpObservation.value - myMeanFunction.invoke(tmpObservation.key);
            retVal.set(i, 0, tmpDiff);
        }

        return retVal;
    }

    List<ComparableToDouble<K>> getObservations() {
        return new ArrayList<ComparableToDouble<K>>(myObservations);
    }

    MatrixStore<Double> getRegressionCoefficients(final K[] args) {
        return this.getC22().solve(this.getC21(args)).builder().transpose().build();
    }

}
