/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.data.transform;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.array.ArrayR064;
import org.ojalgo.function.special.PowerOf2;
import org.ojalgo.matrix.MatrixC128;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.context.NumberContext;

public class ZtransformTest extends DataTransformTests {

    @Test
    public void testCompareDFTUsingRandomInput() {

        NumberContext accuracy = NumberContext.of(8);

        for (int exp = 1; exp <= 8; exp++) {

            int dim = PowerOf2.powerOfInt2(exp);

            PhysicalStore<Double> input = R064Store.FACTORY.makeFilled(dim, 1, Uniform.of(-2, 4));

            DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(input.size());
            MatrixStore<ComplexNumber> expected = transformer.transform(input);

            MatrixStore<ComplexNumber> actual = ZTransform.doDFT(input);

            if (DEBUG) {
                BasicLogger.debugMatrix("Expected", expected);
                BasicLogger.debugMatrix("Actual", actual);
            }
            TestUtils.assertComplexEquals(expected, actual, accuracy);
        }
    }

    /**
     * https://www.youtube.com/watch?v=B4IyRw1zvvA&list=PLJ8LTUMGG9U6FGNmnLscY1fhSA7FiFrfO
     */
    @Test
    public void testDavidDorran() {

        //

        ArrayR064 sequence = ArrayR064.wrap(2.0, -3.0, -1.0, 4.0);

        ComplexNumber z = ComplexNumber.valueOf(1.0);

        ComplexNumber expected = ComplexNumber.valueOf(2.0);

        TestUtils.assertEquals(expected, ZTransform.doTransform(sequence, z));

        //

        sequence = ArrayR064.wrap(3.0, -3.0, 3.0, -3.0);

        z = ComplexNumber.valueOf(1.0);

        expected = ComplexNumber.valueOf(0.0);

        TestUtils.assertEquals(expected, ZTransform.doTransform(sequence, z));

        //

        sequence = ArrayR064.wrap(0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        z = ComplexNumber.of(0.3, -0.3);

        expected = ComplexNumber.of(1.333333333333333, 0.8333333333333333);

        TestUtils.assertEquals(expected, ZTransform.doTransform(sequence, z));

        //

        sequence = ArrayR064.wrap(0.5, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        z = ComplexNumber.valueOf(-1.0);

        expected = ComplexNumber.of(0.0, 0.0);

        TestUtils.assertEquals(expected, ZTransform.doTransform(sequence, z));
    }

    /**
     * https://www.youtube.com/watch?v=FaWSGmkboOs
     * 
     * @see DiscreteFourierTransformTest#testSmartEngineer()
     */
    @Test
    public void testSmartEngineer() {

        MatrixC128 input = MatrixC128.FACTORY.column(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);

        DiscreteFourierTransform transformer = DiscreteFourierTransform.newInstance(input.size());
        MatrixStore<ComplexNumber> expected = transformer.transform(input);

        MatrixStore<ComplexNumber> actual = ZTransform.doDFT(input);

        if (DEBUG) {
            BasicLogger.debugMatrix("Expected", expected);
            BasicLogger.debugMatrix("Actual", actual);
        }
        TestUtils.assertComplexEquals(expected, actual);
    }

}
