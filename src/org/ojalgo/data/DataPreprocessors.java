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

import org.ojalgo.random.SampleSet;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.ColumnView;
import org.ojalgo.structure.Mutate2D.ModifiableReceiver;
import org.ojalgo.structure.RowView;
import org.ojalgo.structure.Transformation2D;

public class DataPreprocessors {

    public static final Transformation2D<Double> COLUMNS_NORMALISER = new Transformation2D<Double>() {

        public <T extends ModifiableReceiver<Double> & Access2D<Double>> void transform(T transformable) {
            SampleSet sampleSet = SampleSet.make();
            for (ColumnView<Double> view : transformable.columns()) {
                sampleSet.swap(view);
                for (long i = 0L, rows = transformable.countRows(); i < rows; i++) {
                    transformable.set(i, view.column(), sampleSet.getStandardScore(i));
                }
            }
        }
    };

    public static final Transformation2D<Double> ROWS_NORMALISER = new Transformation2D<Double>() {

        public <T extends ModifiableReceiver<Double> & Access2D<Double>> void transform(T transformable) {
            SampleSet sampleSet = SampleSet.make();
            for (RowView<Double> view : transformable.rows()) {
                sampleSet.swap(view);
                for (long j = 0L, cols = transformable.countColumns(); j < cols; j++) {
                    transformable.set(view.row(), j, sampleSet.getStandardScore(j));
                }
            }
        }

    };

}
