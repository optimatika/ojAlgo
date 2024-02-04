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
import org.ojalgo.function.constant.BigMath;
import org.ojalgo.random.Uniform;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.RationalNumber;

public class SuperimposedElementCase extends NonPhysicalTest {

    @Override
    @BeforeEach
    public void setUp() {

        int tmpRowDim = Uniform.randomInteger(1, 9);
        int tmpColDim = Uniform.randomInteger(1, 9);

        MatrixStore<ComplexNumber> tmpBase = NonPhysicalTest.makeRandomMatrix(tmpRowDim, tmpColDim);
        int tmpRowIndex = Uniform.randomInteger(tmpRowDim);
        int tmpColumnIndex = Uniform.randomInteger(tmpColDim);
        RationalNumber tmpElement = RationalNumber.valueOf(BigMath.PI);
        MatrixStore<RationalNumber> aBase = GenericStore.Q128.copy(tmpBase);

        //        myBigStore = new SuperimposedMatrixStore<BigDecimal>(BigDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, tmpElement);
        //        myComplexStore = new SuperimposedMatrixStore<ComplexNumber>(ComplexDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, ComplexNumber.makeReal(tmpElement.doubleValue()));
        //        myPrimitiveStore = new SuperimposedMatrixStore<Double>(PrimitiveDenseStore.FACTORY.copyMatrix(tmpBase), tmpRowIndex, tmpColumnIndex, tmpElement.doubleValue());

        rationalStore = new SuperimposedStore<>(aBase, tmpRowIndex, tmpColumnIndex, new SingleStore<>(aBase.physical(), tmpElement));
        MatrixStore<ComplexNumber> aBase1 = GenericStore.C128.copy(tmpBase);
        complexStore = new SuperimposedStore<>(aBase1, tmpRowIndex, tmpColumnIndex,
                new SingleStore<>(aBase1.physical(), ComplexNumber.valueOf(tmpElement.doubleValue())));
        MatrixStore<Double> aBase2 = Primitive64Store.FACTORY.copy(tmpBase);
        primitiveStore = new SuperimposedStore<>(aBase2, tmpRowIndex, tmpColumnIndex, new SingleStore<>(aBase2.physical(), tmpElement.doubleValue()));

        numberOfRows = tmpRowDim;
        numberOfColumns = tmpColDim;
    }

}
