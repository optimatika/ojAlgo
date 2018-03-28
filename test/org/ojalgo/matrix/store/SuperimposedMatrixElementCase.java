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

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.constant.BigMath;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;

public class SuperimposedMatrixElementCase extends NonPhysicalTest {

    @BeforeEach
    public void setUp() {

        final int tmpRowDim = Uniform.randomInteger(1, 9);
        final int tmpColDim = Uniform.randomInteger(1, 9);

        final BasicMatrix tmpBase = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);
        final int tmpRowIndex = Uniform.randomInteger(tmpRowDim);
        final int tmpColumnIndex = Uniform.randomInteger(tmpColDim);
        final BigDecimal tmpElement = BigMath.PI;
        final MatrixStore<BigDecimal> aBase = BigDenseStore.FACTORY.copy(tmpBase);

        //        myBigStore = new SuperimposedMatrixStore<BigDecimal>(BigDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, tmpElement);
        //        myComplexStore = new SuperimposedMatrixStore<ComplexNumber>(ComplexDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, ComplexNumber.makeReal(tmpElement.doubleValue()));
        //        myPrimitiveStore = new SuperimposedMatrixStore<Double>(PrimitiveDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, tmpElement.doubleValue());

        myBigStore = new SuperimposedStore<>(aBase, tmpRowIndex, tmpColumnIndex, new SingleStore<>(aBase.physical(), tmpElement));
        final MatrixStore<ComplexNumber> aBase1 = ComplexDenseStore.FACTORY.copy(tmpBase);
        myComplexStore = new SuperimposedStore<>(aBase1, tmpRowIndex, tmpColumnIndex,
                new SingleStore<>(aBase1.physical(), ComplexNumber.valueOf(tmpElement.doubleValue())));
        final MatrixStore<Double> aBase2 = PrimitiveDenseStore.FACTORY.copy(tmpBase);
        myPrimitiveStore = new SuperimposedStore<>(aBase2, tmpRowIndex, tmpColumnIndex, new SingleStore<>(aBase2.physical(), tmpElement.doubleValue()));
    }

}
