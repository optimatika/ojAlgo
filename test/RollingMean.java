import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.Uniform;

/*
 * Copyright 1997-2018 Optimatika
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

public class RollingMean {

    public static void main(String[] args) {

        final Array1D<Double> doubles = Array1D.factory(Primitive32Array.FACTORY).makeFilled(10, new Uniform());
        System.out.println(doubles.toString());

        final Array1D<Double> rollingMedian = Array1D.PRIMITIVE32.makeZero(doubles.size());

        rollingMedian.set(0, doubles.get(0));
        System.out.printf("%1$s -> %1$s\n", doubles.get(0));

        final Array1D<Double> someSamples2 = doubles.subList(0, 2);
        final double mean2 = SampleSet.wrap(someSamples2).getMean();
        rollingMedian.set(1, mean2);
        System.out.printf("%s -> %s\n", someSamples2.toString(), mean2);

        for (int i = 2; i < doubles.length; i++) {
            final Array1D<Double> someSamples = doubles.subList(i - 2, i + 1);
            final double mean = SampleSet.wrap(someSamples).getMean();
            rollingMedian.set(i, mean);
            System.out.printf("%s -> %s\n", someSamples.toString(), mean2);
        }

        System.out.println(rollingMedian.toString());

        for (int i = 0; i < doubles.length; i++) {
            int first = Math.max(0, i - 2);
            int limit = i + 1;
            double mean = doubles.aggregateRange(first, limit, Aggregator.AVERAGE);
            rollingMedian.set(i, mean);
        }

        System.out.println(rollingMedian.toString());

        SampleSet samples = SampleSet.make();
        for (int i = 0; i < doubles.length; i++) {
            int first = Math.max(0, i - 2);
            int limit = i + 1;
            samples.swap(doubles.sliceRange(first, limit));
            double mean = samples.getMean();
            rollingMedian.set(i, mean);
        }

        System.out.println(rollingMedian.toString());

    }

    public RollingMean() {

        MatrixStore<Double> matrix = null;

        matrix.logical().limits(3, 3).offsets(1, 1).get();

        matrix.logical().offsets(1, 1).get();

    }

}
