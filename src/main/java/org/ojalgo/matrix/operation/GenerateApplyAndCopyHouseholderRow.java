/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.operation;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.scalar.Scalar;

/**
 * Generates the Householder reflector for the part of a row from {@code col} on. The row is overwritten with
 * its first element after the reflection, and the reflector's vector (normalised to 1 at {@code col}) after
 * it, and the reflector is copied to the destination. There is a reflector unless the part of the row after
 * {@code col} is exactly 0; then false is returned and the row is left as it is. The first element after the
 * reflection is -norm when the first element is positive or +0, and +norm when it is negative or -0 (for
 * other number types: -norm times the first element's phase).
 */
public abstract class GenerateApplyAndCopyHouseholderRow implements MatrixOperation {

    public static int THRESHOLD = 128;

    public static boolean invoke(final double[] data, final int structure, final int row, final int col, final Householder.Primitive64 destination) {

        int tmpColDim = data.length / structure;

        double[] tmpVector = destination.vector;
        destination.first = col;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy row and calculate its infinity-norm.
        for (int j = col; j < tmpColDim; j++) {
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[j] = data[row + j * structure]));
        }

        double tmpTailInf = PrimitiveMath.ZERO;
        for (int j = col + 1; j < tmpColDim; j++) {
            tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, PrimitiveMath.ABS.invoke(tmpVector[j]));
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
        }

        if (retVal) {

            double tmpScale = tmpVector[col] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (Math.copySign(PrimitiveMath.ONE, tmpScale) < PrimitiveMath.ZERO) {
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

        double tmpTailInf = PrimitiveMath.ZERO;
        for (int j = col + 1; j < tmpColDim; j++) {
            tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, PrimitiveMath.ABS.invoke(tmpVector[j]));
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
        }

        if (retVal) {

            double tmpScale = tmpVector[col] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (Math.copySign(PrimitiveMath.ONE, tmpScale) < PrimitiveMath.ZERO) {
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

        double tmpTailInf = PrimitiveMath.ZERO;
        for (int j = col + 1; j < tmpColDim; j++) {
            tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, tmpVector[j].norm());
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        N tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int j = col + 1; j < tmpColDim; j++) {
                tmpVal = tmpVector[j].divide(tmpNormInf).get();
                tmpNorm2 += tmpVal.norm() * tmpVal.norm();
                tmpVector[j] = tmpVal;
            }
        }

        if (retVal) {

            N tmpScale = tmpVector[col].divide(tmpNormInf).get();
            tmpNorm2 += tmpScale.norm() * tmpScale.norm();
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2);

            N tmpSignum = tmpScale.normalised().get();
            data[row + col * structure] = tmpSignum.negate().multiply(tmpNorm2 * tmpNormInf).get();
            tmpScale = tmpScale.add(tmpSignum.multiply(tmpNorm2)).get();

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

    public static <N extends Scalar<N>> boolean invokeGeneric(final GenericStore<N> store, final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(store.data, store.getRowDim(), row, column, (Householder.Generic<N>) destination,
                store.physical().scalar());
    }

    public static boolean invokeR064(final R064Store store, final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(store.data, store.getRowDim(), row, column, (Householder.Primitive64) destination);
    }

}
