/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.function.UnaryFunction;
import org.ojalgo.random.SampleSet;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.structure.Factory2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Mutate2D.ModifiableReceiver;
import org.ojalgo.structure.Transformation2D;

/**
 * Various data preprocessors that could be useful when doing data science or similar. With ojAlgo it is
 * highly advantageous to store data in columns (rather than rows). All the {@link Transformation2D} instances
 * in this class assume columns represent variables, and rows samples.
 *
 * @author apete
 */
public class DataPreprocessors {

    /**
     * Variables centered so that their average will be 0.0
     */
    public static final Transformation2D<Double> CENTER = DataPreprocessors.newTransformation2D(ss -> SUBTRACT.by(ss.getMean()));

    /**
     * Variables will be centered around 0.0 AND scaled to be [-1.0,1.0]. The minimum value will be
     * transformed to -1.0 and the maximum to +1.0.
     */
    public static final Transformation2D<Double> CENTER_AND_SCALE = DataPreprocessors
            .newTransformation2D(ss -> SUBTRACT.by(ss.getMean()).andThen(DIVIDE.by((ss.getMaximum() - ss.getMinimum()) / TWO)));

    /**
     * Variables scaled to be within [-1.0,1.0] (divide by largest magnitude regardless of sign). If all
     * values are positive the range will within [0.0,1.0]. If all are negative the range will be within
     * [-1.0,0.0]
     */
    public static final Transformation2D<Double> SCALE = DataPreprocessors.newTransformation2D(ss -> DIVIDE.by(ss.getLargest()));

    /**
     * Will normalise each variable - replace each value with its standard score.
     */
    public static final Transformation2D<Double> STANDARD_SCORE = DataPreprocessors
            .newTransformation2D(ss -> SUBTRACT.by(ss.getMean()).andThen(DIVIDE.by(ss.getStandardDeviation())));

    public static <D extends Access2D<?> & Access2D.Sliceable<?>, M extends Mutate2D> M covariances(Factory2D<M> factory, D data) {

        long numberOfVariables = data.countColumns();
        M retVal = factory.makeZero(numberOfVariables, numberOfVariables);

        SampleSet rowSet = SampleSet.make();
        SampleSet colSet = SampleSet.make();

        for (int j = 0; j < numberOfVariables; j++) {
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

    public static Transformation2D<Double> newTransformation2D(Function<SampleSet, UnaryFunction<Double>> definition) {
        return new Transformation2D<Double>() {

            public <T extends ModifiableReceiver<Double> & Access2D<Double>> void transform(T transformable) {
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
