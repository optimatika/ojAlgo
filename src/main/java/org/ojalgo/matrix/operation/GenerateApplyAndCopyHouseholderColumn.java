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
 * Generates the Householder reflector for the part of a column from {@code row} down. The column is
 * overwritten with its first element after the reflection, and the reflector's vector (normalised to 1 at
 * {@code row}) below it, and the reflector is copied to the destination. There is a reflector unless the part
 * of the column below {@code row} is exactly 0; then false is returned and the column is left as it is. The
 * first element after the reflection is -norm when the first element is positive or +0, and +norm when it is
 * negative or -0 (for other number types: -norm times the first element's phase).
 */
public abstract class GenerateApplyAndCopyHouseholderColumn implements MatrixOperation {

    public static int THRESHOLD = 128;

    public static boolean invoke(final double[] data, final int structure, final int row, final int col, final Householder.Primitive64 destination) {

        int tmpColBase = col * structure;

        double[] tmpVector = destination.vector;
        destination.first = row;

        double tmpNormInf = PrimitiveMath.ZERO; // Copy column and calculate its infinity-norm.
        for (int i = row; i < structure; i++) {
            tmpNormInf = Math.max(tmpNormInf, PrimitiveMath.ABS.invoke(tmpVector[i] = data[i + tmpColBase]));
        }

        double tmpTailInf = PrimitiveMath.ZERO;
        for (int i = row + 1; i < structure; i++) {
            tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, PrimitiveMath.ABS.invoke(tmpVector[i]));
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
        }

        if (retVal) {

            double tmpScale = tmpVector[row] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (Math.copySign(PrimitiveMath.ONE, tmpScale) < PrimitiveMath.ZERO) {
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

        double tmpTailInf = PrimitiveMath.ZERO;
        for (int i = row + 1; i < structure; i++) {
            tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, PrimitiveMath.ABS.invoke(tmpVector[i]));
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        double tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i] /= tmpNormInf;
                tmpNorm2 += tmpVal * tmpVal;
            }
        }

        if (retVal) {

            double tmpScale = tmpVector[row] / tmpNormInf;
            tmpNorm2 += tmpScale * tmpScale;
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2); // 2-norm of the vector to transform (scaled by inf-norm)

            if (Math.copySign(PrimitiveMath.ONE, tmpScale) < PrimitiveMath.ZERO) {
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
        double tmpTailInf = PrimitiveMath.ZERO;
        for (int i = row; i < structure; i++) {
            double norm = (tmpVector[i] = data[i + tmpColBase]).norm();
            tmpNormInf = PrimitiveMath.MAX.invoke(tmpNormInf, norm);
            if (i > row) {
                tmpTailInf = PrimitiveMath.MAX.invoke(tmpTailInf, norm);
            }
        }

        boolean retVal = tmpTailInf != PrimitiveMath.ZERO;
        Scalar<N> tmpVal;
        double tmpNorm2 = PrimitiveMath.ZERO;

        if (retVal) {
            for (int i = row + 1; i < structure; i++) {
                tmpVal = tmpVector[i].divide(tmpNormInf);
                tmpNorm2 += tmpVal.norm() * tmpVal.norm();
                tmpVector[i] = tmpVal.get();
            }
        }

        if (retVal) {

            Scalar<N> tmpScale = tmpVector[row].divide(tmpNormInf);
            tmpNorm2 += tmpScale.norm() * tmpScale.norm();
            tmpNorm2 = PrimitiveMath.SQRT.invoke(tmpNorm2);

            Scalar<N> tmpSignum = tmpScale.normalised();
            data[row + col * structure] = tmpSignum.negate().multiply(tmpNorm2 * tmpNormInf).get();
            tmpScale = tmpScale.add(tmpSignum.multiply(tmpNorm2)).get();

            tmpVector[row] = scalar.one().get();

            for (int i = row + 1; i < structure; i++) {
                data[i + tmpColBase] = tmpVector[i] = tmpVector[i].divide(tmpScale).get();
            }

            destination.beta = scalar.cast(tmpScale.norm() / tmpNorm2);
        }

        return retVal;
    }

    public static <N extends Scalar<N>> boolean invokeGeneric(final GenericStore<N> store, final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(store.data, store.getRowDim(), row, column, (Householder.Generic<N>) destination,
                store.physical().scalar());
    }

    public static boolean invokeR064(final R064Store store, final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(store.data, store.getRowDim(), row, column, (Householder.Primitive64) destination);
    }

}
