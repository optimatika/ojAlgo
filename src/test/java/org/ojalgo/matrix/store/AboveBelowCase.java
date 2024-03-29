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
package org.ojalgo.matrix.store;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class AboveBelowCase extends NonPhysicalTest {

    @Override
    @BeforeEach
    public void setUp() {

        int tmpRowDim = Uniform.randomInteger(1, 9);
        int tmpColDim = Uniform.randomInteger(1, 9);

        MatrixStore<ComplexNumber> above = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);
        MatrixStore<ComplexNumber> below = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);

        rationalStore = new AboveBelowStore<>(GenericStore.Q128.copy(above), GenericStore.Q128.copy(below));
        complexStore = new AboveBelowStore<>(GenericStore.C128.copy(above), GenericStore.C128.copy(below));
        primitiveStore = new AboveBelowStore<>(R064Store.FACTORY.copy(above), R064Store.FACTORY.copy(below));

        numberOfRows = tmpRowDim + tmpRowDim;
        numberOfColumns = tmpColDim;
    }

}
