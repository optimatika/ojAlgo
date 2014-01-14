/*
 * Copyright 1997-2013 Optimatika (www.optimatika.se)
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
package org.ojalgo.matrix.store.operation;

import java.math.BigDecimal;

import org.ojalgo.constant.BigMath;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BigFunction;
import org.ojalgo.function.ComplexFunction;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.type.TypeUtils;

public final class GenerateApplyAndCopyHouseholderColumn extends MatrixOperation {

    public static int THRESHOLD = 128;

    public static boolean invoke(final BigDecimal[] aData, final int aRowDim, final int aRow, final int aCol, final Householder.Big aDestination) {

        final int tmpColBase = aCol * aRowDim;

        final BigDecimal[] tmpVector = aDestination.vector;
        aDestination.first = aRow;

        BigDecimal tmpNormInf = BigMath.ZERO;
        for (int i = aRow; i < aRowDim; i++) {
            tmpNormInf = tmpNormInf.max((tmpVector[i] = aData[i + tmpColBase]).abs());
        }

        boolean retVal = tmpNormInf.signum() != 0;
        BigDecimal tmpVal;
        BigDecimal tmpNorm2 = BigMath.ZERO;

        if (retVal) {
            for (int i = aRow + 1; i < aRowDim; i++) {
                tmpVal = BigFunction.DIVIDE.invoke(tmpVector[i], tmpNormInf);
                tmpNorm2 = BigFunction.ADD.invoke(tmpNorm2, BigFunction.MULTIPLY.invoke(tmpVal, tmpVal));
                tmpVector[i] = tmpVal;
            }
            retVal = !TypeUtils.isZero(tmpNorm2.doubleValue());
        }

        if (retVal) {

            BigDecimal tmpScale = BigFunction.DIVIDE.invoke(tmpVector[aRow], tmpNormInf);
            tmpNorm2 = BigFunction.ADD.invoke(tmpNorm2, BigFunction.MULTIPLY.invoke(tmpScale, tmpScale));
            tmpNorm2 = BigFunction.SQRT.invoke(tmpNorm2);

            if (tmpScale.signum() != 1) {
                aData[aRow + tmpColBase] = tmpNorm2.multiply(tmpNormInf);
                tmpScale = BigFunction.SUBTRACT.invoke(tmpScale, tmpNorm2);
            } else {
                aData[aRow + tmpColBase] = tmpNorm2.negate().multiply(tmpNormInf);
                tmpScale = BigFunction.ADD.invoke(tmpScale, tmpNorm2);
            }

            tmpVector[aRow] = BigMath.ONE;

            for (int i = aRow + 1; i < aRowDim; i++) {
                aData[i + tmpColBase] = tmpVector[i] = BigFunction.DIVIDE.invoke(tmpVector[i], tmpScale);
            }

            aDestination.beta = BigFunction.DIVIDE.invoke(tmpScale.abs(), tmpNorm2);
        }

        return retVal;
    }

    public static boolean invoke(final ComplexNumber[] aData, final int aRowDim, final int aRow, final int aCol, final Householder.Complex aDestination) {

        final int tmpColBase = aCol * aRowDim;

        final ComplexNumber[] tmpVector = aDestination.vector;
        aDestination.first = aRow;

        double tmpNormInf = PrimitiveMath.ZERO;
        for (int i = aRow; i < aRowDim; i++) {
            tmpNormInf = Math.max(tmpNormInf, (tmpVector[i] = aData[i + tmpColBase]).norm());
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        ComplexNumber tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = aRow + 1; i < aRowDim; i++) {
                tmpVal = tmpVector[i].divide(tmpNormInf);
                tmpNorm2 += tmpVal.norm() * tmpVal.norm();
                tmpVector[i] = tmpVal;
            }
            retVal = !TypeUtils.isZero(tmpNorm2);
        }

        if (retVal) {

            ComplexNumber tmpScale = tmpVector[aRow].divide(tmpNormInf);
            tmpNorm2 += tmpScale.norm() * tmpScale.norm();
            tmpNorm2 = Math.sqrt(tmpNorm2);

            aData[aRow + tmpColBase] = ComplexNumber.makePolar(tmpNorm2 * tmpNormInf, tmpScale.phase());
            tmpScale = tmpScale.subtract(ComplexNumber.makePolar(tmpNorm2, tmpScale.phase()));

            tmpVector[aRow] = ComplexNumber.ONE;

            for (int i = aRow + 1; i < aRowDim; i++) {
                aData[i + tmpColBase] = tmpVector[i] = ComplexFunction.DIVIDE.invoke(tmpVector[i], tmpScale);
            }

            aDestination.beta = ComplexNumber.makeReal(tmpScale.norm() / tmpNorm2);
        }

        return retVal;
    }

    public static boolean invoke(final double[] aData, final int aRowDim, final int aRow, final int aCol, final Householder.Primitive aDestination) {

        final int tmpColBase = aCol * aRowDim;

        final double[] tmpVector = aDestination.vector;
        aDestination.first = aRow;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy column and calculate its infinity-norm.
        for (int i = aRow; i < aRowDim; i++) {
            tmpNormInf = Math.max(tmpNormInf, Math.abs(tmpVector[i] = aData[i + tmpColBase]));
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = aRow + 1; i < aRowDim; i++) {
                tmpVal = tmpVector[i] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
            retVal = !TypeUtils.isZero(tmpNorm2);
        }

        if (retVal) {

            double tmpScale = tmpVector[aRow] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = Math.sqrt(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (tmpScale <= PrimitiveMath.ZERO) {
                aData[(aRow + tmpColBase)] = tmpNorm2 * tmpNormInf;
                tmpScale -= tmpNorm2;
            } else {
                aData[(aRow + tmpColBase)] = -tmpNorm2 * tmpNormInf;
                tmpScale += tmpNorm2;
            }

            tmpVector[aRow] = PrimitiveMath.ONE;

            for (int i = aRow + 1; i < aRowDim; i++) {
                aData[i + tmpColBase] = tmpVector[i] /= tmpScale;
            }

            aDestination.beta = Math.abs(tmpScale) / tmpNorm2;
        }

        return retVal;
    }

    private GenerateApplyAndCopyHouseholderColumn() {
        super();
    }

}
