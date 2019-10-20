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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class ColumnsCase extends NonPhysicalTest {

    @Override
    @BeforeEach
    public void setUp() {

        int tmpRowDim = Uniform.randomInteger(1, 9);
        int tmpColDim = Uniform.randomInteger(1, 9);

        MatrixStore<ComplexNumber> tmpBase = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);

        int[] tmpCols = new int[Uniform.randomInteger(1, tmpColDim)];
        for (int i = 0; i < tmpCols.length; i++) {
            tmpCols[i] = Uniform.randomInteger(tmpColDim);
        }

        rationalStore = new ColumnsStore<>(GenericStore.RATIONAL.copy(tmpBase), tmpCols);
        complexStore = new ColumnsStore<>(GenericStore.COMPLEX.copy(tmpBase), tmpCols);
        primitiveStore = new ColumnsStore<>(Primitive64Store.FACTORY.copy(tmpBase), tmpCols);

        numberOfRows = tmpRowDim;
        numberOfColumns = tmpCols.length;
    }

}
