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
package org.ojalgo.random.scedasticity;

import static org.ojalgo.function.constant.PrimitiveMath.SQRT;

import org.ojalgo.random.SampleSet;
import org.ojalgo.series.primitive.DataSeries;
import org.ojalgo.series.primitive.PrimitiveSeries;

public interface ScedasticityModel {

    double getMean();

    default double getStandardDeviation() {
        return SQRT.invoke(this.getVariance());
    }

    double getVariance();

    void initialise(double mean, double variance);

    void update(double value);

    /**
     * Takes a series of realised values and outputs a series of (locally fluctuating) variances.
     */
    default PrimitiveSeries variances(final PrimitiveSeries values) {

        SampleSet statistics = SampleSet.wrap(values);

        double mean = statistics.getMean();
        double variance = statistics.getVariance();

        this.initialise(mean, variance);

        double[] data = new double[values.size()];

        for (int i = 0; i < values.size(); i++) {
            data[i] = this.getVariance();
            this.update(values.value(i));
        }

        return DataSeries.wrap(data);
    }

}
