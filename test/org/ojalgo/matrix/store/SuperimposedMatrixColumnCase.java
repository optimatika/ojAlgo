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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.random.Uniform;

public class SuperimposedMatrixColumnCase extends NonPhysicalTest {

    @BeforeEach
    public void setUp() {

        final int tmpRowDim = Uniform.randomInteger(1, 9);
        final int tmpColDim = Uniform.randomInteger(1, 9);

        final BasicMatrix tmpBase = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);
        final BasicMatrix tmpColumn = NonPhysicalTest.makeRandomMatrix(tmpRowDim, 1);
        final int tmpIndex = Uniform.randomInteger(tmpColDim);

        rationalStore = new SuperimposedStore<>(GenericDenseStore.RATIONAL.copy(tmpBase), 0, tmpIndex, GenericDenseStore.RATIONAL.copy(tmpColumn));
        complexStore = new SuperimposedStore<>(GenericDenseStore.COMPLEX.copy(tmpBase), 0, tmpIndex, ComplexDenseStore.FACTORY.copy(tmpColumn));
        primitiveStore = new SuperimposedStore<>(PrimitiveDenseStore.FACTORY.copy(tmpBase), 0, tmpIndex, PrimitiveDenseStore.FACTORY.copy(tmpColumn));
    }

}
