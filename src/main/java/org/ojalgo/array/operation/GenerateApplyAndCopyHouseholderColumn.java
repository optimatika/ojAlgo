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

public abstract class GenerateApplyAndCopyHouseholderColumn implements ArrayOperation {

    public static int THRESHOLD = 128;

    public static boolean invoke(final double[] data, final int structure, final int row, final int col, final Householder.Primitive64 destination) {

        int tmpColBase = col * structure;

        double[] tmpVector = destination.vector;
        destination.first = row;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy column and calculate its infinity-norm.
        for (int i = row; i < structure; i++) {
            tmpNormInf = Math.max(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[i] = data[i + tmpColBase]));
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpNorm2);
        }

        if (retVal) {

            double tmpScale = tmpVector[row] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (tmpScale <= PrimitiveMath.ZERO) {
                data[row + tmpColBase] = tmpNorm2 * tmpNormInf;
                tmpScale -= tmpNorm2;
            } else {
                data[row + tmpColBase] = -tmpNorm2 * tmpNormInf;
                tmpScale += tmpNorm2;
            }

            tmpVector[row] = PrimitiveMath.ONE;

            for (int i = row + 1; i < structure; i++) {
                data[i + tmpColBase] = tmpVector[i] /= tmpScale;
            }

            destination.beta = PrimitiveMath.ABS.invoke(tmpScale) / tmpNorm2;
        }

        return retVal;
    }

    public static boolean invoke(final float[] data, final int structure, final int row, final int col, final Householder.Primitive32 destination) {

        int tmpColBase = col * structure;

        float[] tmpVector = destination.vector;
        destination.first = row;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy column and calculate its infinity-norm.
        for (int i = row; i < structure; i++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[i] = data[i + tmpColBase]));
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpNorm2);
        }

        if (retVal) {

            double tmpScale = tmpVector[row] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (tmpScale <= PrimitiveMath.ZERO) {
                data[row + tmpColBase] = (float) (tmpNorm2 * tmpNormInf);
                tmpScale -= tmpNorm2;
            } else {
                data[row + tmpColBase] = (float) (-tmpNorm2 * tmpNormInf);
                tmpScale += tmpNorm2;
            }

            tmpVector[row] = (float) PrimitiveMath.ONE;

            for (int i = row + 1; i < structure; i++) {
                data[i + tmpColBase] = tmpVector[i] /= tmpScale;
            }

            destination.beta = (float) (PrimitiveMath.ABS.invoke(tmpScale) / tmpNorm2);
        }

        return retVal;
    }

    public static <N extends Scalar<N>> boolean invoke(final N[] data, final int structure, final int row, final int col,
            final Householder.Generic<N> destination, final Scalar.Factory<N> scalar) {

        int tmpColBase = col * structure;

        N[] tmpVector = destination.vector;
        destination.first = row;

        double tmpNormInf = PrimitiveMath.ZERO;
        for (int i = row; i < structure; i++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, (tmpVector[i] = data[i + tmpColBase]).norm());
        }

        boolean retVal = tmpNormInf != PrimitiveMath.ZERO;
        Scalar<N> tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i].divide(tmpNormInf);
                tmpNorm2 += tmpVal.norm() * tmpVal.norm();
                tmpVector[i] = tmpVal.get();
            }
            retVal = !PrimitiveScalar.isSmall(PrimitiveMath.ONE, tmpNorm2);
        }

        if (retVal) {

            Scalar<N> tmpScale = tmpVector[row].divide(tmpNormInf);
            tmpNorm2 += tmpScale.norm() * tmpScale.norm();
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2);

            // data[row + tmpColBase] = ComplexNumber.makePolar(tmpNorm2 * tmpNormInf, tmpScale.phase());
            data[row + col * structure] = tmpScale.signum().multiply(tmpNorm2 * tmpNormInf).get();
            // tmpScale = tmpScale.subtract(ComplexNumber.makePolar(tmpNorm2, tmpScale.phase()));
            tmpScale = tmpScale.subtract(tmpScale.signum().multiply(tmpNorm2)).get();

            tmpVector[row] = scalar.one().get();

            for (int i = row + 1; i < structure; i++) {
                data[i + tmpColBase] = tmpVector[i] = tmpVector[i].divide(tmpScale).get();
            }

            destination.beta = scalar.cast(tmpScale.norm() / tmpNorm2);
        }

        return retVal;
    }

}
