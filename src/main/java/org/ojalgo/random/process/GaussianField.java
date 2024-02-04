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
package org.ojalgo.random.process;

import static org.ojalgo.function.constant.PrimitiveMath.ZERO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.decomposition.MatrixDecomposition;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Normal1D;
import org.ojalgo.type.keyvalue.EntryPair;
import org.ojalgo.type.keyvalue.EntryPair.KeyedPrimitive;

/**
 * This GaussianField class is a generalization, as well as the underlying implementation, of
 * {@linkplain GaussianProcess}. Prior to calling {@linkplain #getDistribution(Comparable...)} you must call
 * {@linkplain #addObservation(Comparable, double)} one or more times.
 *
 * @author apete
 */
public final class GaussianField<K extends Comparable<? super K>> extends RandomField<K> {

    public interface Covariance<K extends Comparable<? super K>> {

        void calibrate(Collection<KeyedPrimitive<K>> observations, Mean<K> mean);

        double invoke(K key1, K key2);

    }

    public interface Mean<K extends Comparable<? super K>> {

        void calibrate(Collection<KeyedPrimitive<K>> observations);

        double invoke(K key);

    }

    private static final Factory<Double, Primitive64Store> FACTORY = Primitive64Store.FACTORY;

    private static <K extends Comparable<? super K>> Mean<K> mean() {
        return new Mean<K>() {

            public void calibrate(final Collection<KeyedPrimitive<K>> observations) {

            }

            public double invoke(final K anArg) {
                return ZERO;
            }
        };
    }

    private Covariance<K> myCovarianceFunction;
    private Mean<K> myMeanFunction;

    private TreeSet<KeyedPrimitive<K>> myObservations;

    public GaussianField(final Covariance<K> covarFunc) {
        this((Mean<K>) GaussianField.mean(), covarFunc, new TreeSet<KeyedPrimitive<K>>());
    }

    public GaussianField(final Mean<K> meanFunc, final Covariance<K> covarFunc) {
        this(meanFunc, covarFunc, new TreeSet<KeyedPrimitive<K>>());
    }

    GaussianField(final Covariance<K> covarFunc, final TreeSet<KeyedPrimitive<K>> observations) {
        this((Mean<K>) GaussianField.mean(), covarFunc, observations);
    }

    GaussianField(final Mean<K> meanFunc, final Covariance<K> covarFunc, final TreeSet<KeyedPrimitive<K>> observations) {

        super();

        myMeanFunction = meanFunc;
        myCovarianceFunction = covarFunc;
        myObservations = observations;
    }

    public void addObservation(final K key, final double value) {
        myObservations.add(EntryPair.of(key, value));
    }

    public void calibrate() {
        myMeanFunction.calibrate(myObservations);
        myCovarianceFunction.calibrate(myObservations, myMeanFunction);
    }

    public Normal1D getDistribution(final boolean cleanCovariances, final K... evaluationPoint) {

        MatrixStore<Double> tmpRegCoef = this.getRegressionCoefficients(evaluationPoint);

        MatrixStore<Double> tmpM1 = this.getM1(evaluationPoint);
        MatrixStore<Double> tmpM2differenses = this.getM2differenses();

        Primitive64Store tmpLocations = FACTORY.make(tmpM1.countRows(), tmpM1.countColumns());
        tmpLocations.fillMatching(tmpM1, PrimitiveMath.ADD, tmpRegCoef.multiply(tmpM2differenses));

        MatrixStore<Double> tmpC11 = this.getC11(evaluationPoint);
        MatrixStore<Double> tmpC21 = this.getC21(evaluationPoint);

        Primitive64Store tmpCovariances = FACTORY.make(tmpC11.countRows(), tmpC11.countColumns());
        tmpCovariances.fillMatching(tmpC11, PrimitiveMath.SUBTRACT, tmpRegCoef.multiply(tmpC21));

        if (cleanCovariances) {

            Eigenvalue<Double> tmpEvD = Eigenvalue.PRIMITIVE.make(true);
            tmpEvD.decompose(tmpCovariances);

            MatrixStore<Double> tmpV = tmpEvD.getV();
            PhysicalStore<Double> tmpD = tmpEvD.getD().copy();

            double tmpLargest = tmpD.doubleValue(0, 0);
            double tmpLimit = PrimitiveMath.MAX.invoke(PrimitiveMath.MACHINE_EPSILON * tmpLargest, 1E-12);

            int tmpLength = (int) Math.min(tmpD.countRows(), tmpD.countColumns());
            for (int ij = 0; ij < tmpLength; ij++) {
                if (tmpD.doubleValue(ij, ij) < tmpLimit) {
                    tmpD.set(ij, ij, tmpLimit);
                }
            }

            tmpCovariances.fillMatching(tmpV.multiply(tmpD).multiply(tmpV.transpose()));
        }

        return new Normal1D(tmpLocations, tmpCovariances);
    }

    public Normal1D getDistribution(final K... evaluationPoint) {
        return this.getDistribution(false, evaluationPoint);
    }

    MatrixStore<Double> getC11(final K[] args) {

        int tmpLength = args.length;

        Primitive64Store retVal = FACTORY.make(tmpLength, tmpLength);

        for (int j = 0; j < tmpLength; j++) {
            for (int i = 0; i < tmpLength; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(args[i], args[j]));
            }
        }

        return retVal;
    }

    MatrixStore<Double> getC12(final K[] args) {

        List<KeyedPrimitive<K>> tmpObservations = this.getObservations();

        int tmpRowDim = args.length;
        int tmpColDim = tmpObservations.size();

        Primitive64Store retVal = FACTORY.make(tmpRowDim, tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(args[i], tmpObservations.get(j).getKey()));
            }
        }

        return retVal;
    }

    MatrixStore<Double> getC21(final K[] args) {

        List<KeyedPrimitive<K>> tmpObservations = this.getObservations();

        int tmpRowDim = tmpObservations.size();
        int tmpColDim = args.length;

        Primitive64Store retVal = FACTORY.make(tmpRowDim, tmpColDim);

        for (int j = 0; j < tmpColDim; j++) {
            for (int i = 0; i < tmpRowDim; i++) {
                retVal.set(i, j, myCovarianceFunction.invoke(tmpObservations.get(i).getKey(), args[j]));
            }
        }

        return retVal;
    }

    MatrixDecomposition.Solver<Double> getC22() {

        List<KeyedPrimitive<K>> tmpObservations = this.getObservations();

        int tmpSize = tmpObservations.size();

        Primitive64Store tmpMatrix = FACTORY.make(tmpSize, tmpSize);

        for (int j = 0; j < tmpSize; j++) {
            K tmpColumnKey = tmpObservations.get(j).getKey();
            for (int i = 0; i < tmpSize; i++) {
                tmpMatrix.set(i, j, myCovarianceFunction.invoke(tmpObservations.get(i).getKey(), tmpColumnKey));
            }
        }

        SingularValue<Double> retVal = SingularValue.PRIMITIVE.make();

        retVal.decompose(tmpMatrix);

        return retVal;
    }

    MatrixStore<Double> getM1(final K[] args) {

        int tmpLength = args.length;

        Primitive64Store retVal = FACTORY.make(tmpLength, 1);

        for (int i = 0; i < tmpLength; i++) {
            retVal.set(i, 0, myMeanFunction.invoke(args[i]));
        }

        return retVal;
    }

    MatrixStore<Double> getM2() {

        List<KeyedPrimitive<K>> tmpObservations = this.getObservations();

        int tmpSize = tmpObservations.size();

        Primitive64Store retVal = FACTORY.make(tmpSize, 1);

        for (int i = 0; i < tmpSize; i++) {
            retVal.set(i, 0, myMeanFunction.invoke(tmpObservations.get(i).getKey()));
        }

        return retVal;
    }

    MatrixStore<Double> getM2differenses() {

        List<KeyedPrimitive<K>> tmpObservations = this.getObservations();

        int tmpSize = tmpObservations.size();

        Primitive64Store retVal = FACTORY.make(tmpSize, 1);

        KeyedPrimitive<K> tmpObservation;
        double tmpDiff;
        for (int i = 0; i < tmpSize; i++) {
            tmpObservation = tmpObservations.get(i);
            tmpDiff = tmpObservation.doubleValue() - myMeanFunction.invoke(tmpObservation.getKey());
            retVal.set(i, 0, tmpDiff);
        }

        return retVal;
    }

    List<KeyedPrimitive<K>> getObservations() {
        return new ArrayList<>(myObservations);
    }

    MatrixStore<Double> getRegressionCoefficients(final K[] args) {
        return this.getC22().getSolution(this.getC21(args)).transpose();
    }

}
