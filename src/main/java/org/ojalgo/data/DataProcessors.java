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
package org.ojalgo.data;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.function.Function;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.Array1D;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.random.SampleSet;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Access2D.ColumnView;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Transformation2D;

/**
 * Various data processors that could be useful when doing data science or similar. With ojAlgo it is highly
 * advantageous to store data in columns (rather than rows). All the {@link Transformation2D} instances in
 * this class assume columns represent variables, and rows samples.
 *
 * @author apete
 */
public class DataProcessors {

    /**
     * Variables centered so that their average will be 0.0
     */
    public static final Transformation2D<Double> CENTER = DataProcessors.newColumnsTransformer(ss -> SUBTRACT.by(ss.getMean()));

    /**
     * Variables will be centered around 0.0 AND scaled to be [-1.0,1.0]. The minimum value will be
     * transformed to -1.0 and the maximum to +1.0.
     */
    public static final Transformation2D<Double> CENTER_AND_SCALE = DataProcessors
            .newColumnsTransformer(ss -> SUBTRACT.by(ss.getMean()).andThen(DIVIDE.by((ss.getMaximum() - ss.getMinimum()) / TWO)));

    /**
     * Variables scaled to be within [-1.0,1.0] (divide by largest magnitude regardless of sign). If all
     * values are positive the range will within [0.0,1.0]. If all are negative the range will be within
     * [-1.0,0.0]
     */
    public static final Transformation2D<Double> SCALE = DataProcessors.newColumnsTransformer(ss -> DIVIDE.by(ss.getLargest()));

    /**
     * Will normalise each variable - replace each value with its standard score.
     */
    public static final Transformation2D<Double> STANDARD_SCORE = DataProcessors
            .newColumnsTransformer(ss -> SUBTRACT.by(ss.getMean()).andThen(DIVIDE.by(ss.getStandardDeviation())));

    /**
     * Calculate the correlation matrix from a set of variables' samples. Each {@link Access1D} instance
     * represents one variable, and contains an ordered sequence of samples.
     */
    public static <M extends Mutate2D> M correlations(final Factory2D<M> factory, final Access1D<?>... data) {

        int nbVariables = data.length;
        M retVal = factory.make(nbVariables, nbVariables);

        SampleSet rowSet = SampleSet.make();
        SampleSet colSet = SampleSet.make();

        double[] stdDev = new double[nbVariables];
        double stdDevJ = ZERO;

        for (int j = 0; j < nbVariables; j++) {
            colSet.swap(data[j]);

            stdDevJ = stdDev[j] = colSet.getStandardDeviation();

            for (int i = 0; i < j; i++) {
                rowSet.swap(data[i]);

                double correlation = rowSet.getCovariance(colSet);
                correlation /= stdDev[i];
                correlation /= stdDevJ;

                retVal.set(i, j, correlation);
                retVal.set(j, i, correlation);
            }

            retVal.set(j, j, ONE);
        }

        return retVal;
    }

    /**
     * Calculate the covariance matrix from a set of variables' samples. Each {@link Access1D} instance
     * represents one variable, and contains an ordered sequence of samples.
     */
    public static <M extends Mutate2D> M covariances(final Factory2D<M> factory, final Access1D<?>... data) {

        int nbVariables = data.length;
        M retVal = factory.make(nbVariables, nbVariables);

        SampleSet rowSet = SampleSet.make();
        SampleSet colSet = SampleSet.make();

        for (int j = 0; j < nbVariables; j++) {
            colSet.swap(data[j]);

            retVal.set(j, j, colSet.getVariance());

            for (int i = 0; i < j; i++) {
                rowSet.swap(data[i]);

                double covariance = rowSet.getCovariance(colSet);
                retVal.set(i, j, covariance);
                retVal.set(j, i, covariance);
            }
        }

        return retVal;
    }

    /**
     * Variables in columns and matching samples in rows.
     *
     * @see #covariances(Factory2D, Access1D...)
     */
    public static <D extends Access2D<?> & Access2D.Sliceable<?>, M extends Mutate2D> M covariances(final Factory2D<M> factory, final D data) {

        int nbVariables = data.getColDim();
        M retVal = factory.make(nbVariables, nbVariables);

        SampleSet rowSet = SampleSet.make();
        SampleSet colSet = SampleSet.make();

        for (int j = 0; j < nbVariables; j++) {
            colSet.swap(data.sliceColumn(j));

            retVal.set(j, j, colSet.getVariance());

            for (int i = 0; i < j; i++) {
                rowSet.swap(data.sliceColumn(i));

                double covariance = rowSet.getCovariance(colSet);
                retVal.set(i, j, covariance);
                retVal.set(j, i, covariance);
            }
        }

        return retVal;
    }

    /**
     * @see #covariances(Factory2D, Access1D...)
     */
    public static <M extends Mutate2D> M covariances(final Factory2D<M> factory, final double[]... data) {
        return DataProcessors.covariances(factory, RawStore.wrap(data).transpose());
    }

    /**
     * @see #covariances(Factory2D, SingularValue, int)
     */
    public static <M extends PhysicalStore<Double>> M covariances(final Factory2D<M> factory, final SingularValue<Double> svd) {
        return DataProcessors.covariances(factory, svd, Math.toIntExact(svd.countColumns()));
    }

    /**
     * @see #covariances(Factory2D, SingularValue, int)
     */
    public static <M extends PhysicalStore<Double>> M covariances(final Factory2D<M> factory, final SingularValue<Double> svd, final double threshold) {
        return DataProcessors.covariances(factory, svd, svd.countSignificant(threshold));
    }

    /**
     * @param factory A factory that will produce the returned covariance matrix
     * @param svd A pre-decomposed SVD instance. The original matrix is assumed to have centered data in its
     *        columns
     * @param complexity The maximum number of singular values that should be considered
     */
    public static <M extends PhysicalStore<Double>> M covariances(final Factory2D<M> factory, final SingularValue<Double> svd, final int complexity) {

        if (!svd.isComputed()) {
            throw new ProgrammingError("The decomposition must be computed!");
        }

        if (!svd.isOrdered()) {
            throw new ProgrammingError("The singular values must be ordered!");
        }

        long numberOfSamples = svd.countRows();
        long numberOfVariables = svd.countColumns();

        if (numberOfSamples <= 1) {
            throw new ProgrammingError("There must be more than 1 sample!");
        }

        M retVal = factory.make(numberOfVariables, numberOfVariables);

        int limit = Math.min(complexity, svd.getRank());
        if (limit > 0) {

            Array1D<Double> values = svd.getSingularValues();
            MatrixStore<Double> vectors = svd.getV();

            if (limit < numberOfVariables) {
                values = values.sliceRange(0L, limit);
                vectors = vectors.limits(-1, limit);
            }

            MatrixStore<Double> scaledV = vectors.onColumns(MULTIPLY, values).collect(factory);

            retVal.fillByMultiplying(scaledV, scaledV.transpose());

            retVal.modifyAll(DIVIDE.by(numberOfSamples - 1));
        }

        return retVal;
    }

    /**
     * Creates a {@link Transformation2D} that will apply a {@link UnaryFunction} to each column. That unary
     * function will be created by the provided {@link Function} using a {@link SampleSet} (of that column) as
     * input.
     * <p>
     * The constants {@link #CENTER}, {@link #SCALE}, {@link #CENTER_AND_SCALE} and {@link #STANDARD_SCORE}
     * are predefined {@link Transformation2D} instances created by calling this method.
     * 
     * @param definition A {@link Function} that will create a {@link UnaryFunction} from a {@link SampleSet}
     *        to be applied to each column
     * @return A {@link Transformation2D} that will apply a {@link UnaryFunction} to each column
     */
    public static Transformation2D<Double> newColumnsTransformer(final Function<SampleSet, UnaryFunction<Double>> definition) {

        return new Transformation2D<>() {

            public <T extends Mutate2D.ModifiableReceiver<Double>> void transform(final T transformable) {
                SampleSet sampleSet = SampleSet.make();
                for (ColumnView<Double> view : transformable.columns()) {
                    sampleSet.swap(view);
                    UnaryFunction<Double> modifier = definition.apply(sampleSet);
                    transformable.modifyColumn(view.column(), modifier);
                }
            }

        };
    }

}
