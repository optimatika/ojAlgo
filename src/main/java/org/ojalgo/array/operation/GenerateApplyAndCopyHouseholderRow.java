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
package org.ojalgo.array.operation;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

public abstract class GenerateApplyAndCopyHouseholderRow implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static boolean invoke(final double[] data, final int structure, final int row, final int col, final Householder.Primitive64 destination) {

        int tmpColDim = data.length / structure;

        double[] tmpVector = destination.vector;
        destination.first = col;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy row and calculate its infinity-norm.
        for (int j = col; j < tmpColDim; j++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[j] = data[row + j * structure]));
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
            double value = tmpNorm2;
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, value);
        }

        if (retVal) {

            double tmpScale = tmpVector[col] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (tmpScale <= PrimitiveMath.ZERO) {
                data[row + col * structure] = tmpNorm2 * tmpNormInf;
                tmpScale -= tmpNorm2;
            } else {
                data[row + col * structure] = -tmpNorm2 * tmpNormInf;
                tmpScale += tmpNorm2;
            }

            tmpVector[col] = PrimitiveMath.ONE;

            for (int j = col + 1; j < tmpColDim; j++) {
                data[row + j * structure] = tmpVector[j] /= tmpScale;
            }

            destination.beta = PrimitiveMath.ABS.invoke(tmpScale) / tmpNorm2;
        }

        return retVal;
    }

    public static boolean invoke(final float[] data, final int structure, final int row, final int col, final Householder.Primitive32 destination) {

        int tmpColDim = data.length / structure;

        float[] tmpVector = destination.vector;
        destination.first = col;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy row and calculate its infinity-norm.
        for (int j = col; j < tmpColDim; j++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[j] = data[row + j * structure]));
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
            double value = tmpNorm2;
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, value);
        }

        if (retVal) {

            double tmpScale = tmpVector[col] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (tmpScale <= PrimitiveMath.ZERO) {
                data[row + col * structure] = (float) (tmpNorm2 * tmpNormInf);
                tmpScale -= tmpNorm2;
            } else {
                data[row + col * structure] = (float) (-tmpNorm2 * tmpNormInf);
                tmpScale += tmpNorm2;
            }

            tmpVector[col] = (float) PrimitiveMath.ONE;

            for (int j = col + 1; j < tmpColDim; j++) {
                data[row + j * structure] = tmpVector[j] /= tmpScale;
            }

            destination.beta = (float) (PrimitiveMath.ABS.invoke(tmpScale) / tmpNorm2);
        }

        return retVal;
    }

    public static <N extends Scalar<N>> boolean invoke(final N[] data, final int structure, final int row, final int col,
            final Householder.Generic<N> destination, final Scalar.Factory<N> scalar) {

        int tmpColDim = data.length / structure;

        N[] tmpVector = destination.vector;
        destination.first = col;

        double tmpNormInf = PrimitiveMath.ZERO;
        for (int j = col; j < tmpColDim; j++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, (tmpVector[j] = data[row + j * structure]).norm());
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        N tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j].divide(tmpNormInf).get();
                tmpNorm2 += tmpVal.norm() * tmpVal.norm();
                tmpVector[j] = tmpVal;
            }
            double value = tmpNorm2;
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, value);
        }

        if (retVal) {

            N tmpScale = tmpVector[col].divide(tmpNormInf).get();
            tmpNorm2 += tmpScale.norm() * tmpScale.norm();
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2);

            // data[(row + (col * structure))] = ComplexNumber.makePolar(tmpNorm2 * tmpNormInf, tmpScale.phase());
            data[row + col * structure] = tmpScale.signum().multiply(tmpNorm2 * tmpNormInf).get();
            // tmpScale = tmpScale.subtract(ComplexNumber.makePolar(tmpNorm2, tmpScale.phase()));
            tmpScale = tmpScale.subtract(tmpScale.signum().multiply(tmpNorm2)).get();

            tmpVector[col] = scalar.one().get();

            for (int j = col + 1; j < tmpColDim; j++) {
                // data[row + (j * structure)] = tmpVector[j] = ComplexFunction.DIVIDE.invoke(tmpVector[j], tmpScale).conjugate();
                data[row + j * structure] = tmpVector[j] = tmpVector[j].divide(tmpScale).conjugate().get();
            }

            // destination.beta = ComplexNumber.valueOf(tmpScale.norm() / tmpNorm2);
            destination.beta = scalar.cast(tmpScale.norm() / tmpNorm2);
        }

        return retVal;
    }

}
