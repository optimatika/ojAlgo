import org.ojalgo.TestUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.Eigenvalue;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.random.SampleSet;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

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

public class CodeSnippets {

    public static void main(final String[] args) {

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
            final int first = Math.max(0, i - 2);
            final int limit = i + 1;
            final double mean = doubles.aggregateRange(first, limit, Aggregator.AVERAGE);
            rollingMedian.set(i, mean);
        }

        System.out.println(rollingMedian.toString());

        final SampleSet samples = SampleSet.make();
        for (int i = 0; i < doubles.length; i++) {
            final int first = Math.max(0, i - 2);
            final int limit = i + 1;
            samples.swap(doubles.sliceRange(first, limit));
            final double mean = samples.getMean();
            rollingMedian.set(i, mean);
        }

        System.out.println(rollingMedian.toString());

        final PrimitiveDenseStore org = MatrixUtils.makeSPD(10);

        final Eigenvalue<Double> evd = Eigenvalue.PRIMITIVE.make(true);
        evd.decompose(org);

        final MatrixStore<Double> expSquared = org.multiply(org);

        final MatrixStore<Double> v = evd.getV();
        final MatrixStore<Double> d = evd.getD().logical().diagonal(false).get();
        final Array1D<ComplexNumber> values = evd.getEigenvalues();

        final MatrixStore<Double> ident = v.multiply(v.conjugate());

        final MatrixStore<Double> actSquared = v.multiply(d).multiply(d).multiply(v.conjugate());

        TestUtils.assertEquals(expSquared, actSquared);

        final PhysicalStore<Double> copied = v.copy();
        copied.loopRow(0, (r, c) -> copied.modifyColumn(c, PrimitiveFunction.MULTIPLY.second(values.doubleValue(c) * values.doubleValue(c))));
        final MatrixStore<Double> actSquared2 = copied.multiply(v.conjugate());

        TestUtils.assertEquals(expSquared, actSquared2);

        final MatrixStore<Double> matrix = org;

        matrix.logical().limits(3, 3).offsets(1, 1).get();

        matrix.logical().offsets(1, 1).get();

    }

}
