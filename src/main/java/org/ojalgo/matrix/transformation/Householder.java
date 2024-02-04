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
package org.ojalgo.matrix.transformation;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

public interface Householder<N extends Comparable<N>> extends Access1D<N> {

    public static final class Generic<N extends Scalar<N>> implements Householder<N> {

        public N beta;
        public int first;
        public final N[] vector;

        private final Scalar.Factory<N> myFactory;

        public Generic(final Scalar.Factory<N> factory, final Householder<N> aTransf) {

            this(factory, (int) aTransf.count());

            this.copy(aTransf);
        }

        public Generic(final Scalar.Factory<N> factory, final int dim) {

            super();

            vector = factory.newArrayInstance(dim);
            beta = factory.zero().get();
            first = 0;

            myFactory = factory;
        }

        public Householder.Generic<N> copy(final Householder<N> source) {

            first = source.first();

            final N[] tmpVector = vector;
            N tmpNmbr;
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpNmbr = source.get(i);
                tmpVal = tmpNmbr.norm();
                tmpVal2 += tmpVal * tmpVal;
                tmpVector[i] = tmpNmbr;
            }

            beta = myFactory.cast(PrimitiveMath.TWO / tmpVal2);

            return this;
        }

        public Householder.Generic<N> copy(final Householder<N> source, final N precalculatedBeta) {

            first = source.first();

            final N[] tmpVector = vector;

            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpVector[i] = source.get(i);
            }

            beta = precalculatedBeta;

            return this;
        }

        @Override
        public long count() {
            return vector.length;
        }

        @Override
        public double doubleValue(final int anInd) {
            return vector[anInd].doubleValue();
        }

        @Override
        public int first() {
            return first;
        }

        @Override
        public N get(final long index) {
            return vector[Math.toIntExact(index)];
        }

        @Override
        public String toString() {

            final StringBuilder retVal = new StringBuilder("{");

            final int tmpFirst = first;
            final int tmpLength = vector.length;
            for (int i = 0; i < tmpFirst; i++) {
                retVal.append(ComplexNumber.ZERO);
                retVal.append(", ");
            }
            for (int i = first; i < tmpLength; i++) {
                retVal.append(vector[i]);
                if (i + 1 < tmpLength) {
                    retVal.append(", ");
                }
            }
            retVal.append("}");

            return retVal.toString();
        }

    }

    public static final class Primitive32 implements Householder<Double> {

        public float beta;
        public int first;
        public final float[] vector;

        public Primitive32(final Householder<Double> aTransf) {

            this((int) aTransf.count());

            this.copy(aTransf);
        }

        public Primitive32(final int aDim) {

            super();

            vector = new float[aDim];
            beta = (float) PrimitiveMath.ZERO;
            first = 0;
        }

        public Householder.Primitive32 copy(final Householder<Double> source) {

            first = source.first();

            final float[] tmpVector = vector;
            float tmpVal, tmpVal2 = (float) PrimitiveMath.ZERO;
            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpVal = source.floatValue(i);
                tmpVal2 += tmpVal * tmpVal;
                tmpVector[i] = tmpVal;
            }

            beta = (float) PrimitiveMath.TWO / tmpVal2;

            return this;
        }

        public Householder.Primitive32 copy(final Householder<Double> source, final float precalculatedBeta) {

            first = source.first();

            final float[] tmpVector = vector;

            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpVector[i] = source.floatValue(i);
            }

            beta = precalculatedBeta;

            return this;
        }

        @Override
        public long count() {
            return vector.length;
        }

        @Override
        public double doubleValue(final int anInd) {
            return vector[anInd];
        }

        @Override
        public int first() {
            return first;
        }

        @Override
        public Double get(final long index) {
            return Double.valueOf(vector[Math.toIntExact(index)]);
        }

        @Override
        public String toString() {

            final StringBuilder retVal = new StringBuilder("{ ");

            final int tmpLastIndex = vector.length - 1;
            for (int i = 0; i < tmpLastIndex; i++) {
                retVal.append(this.get(i));
                retVal.append(", ");
            }
            retVal.append(this.get(tmpLastIndex));

            retVal.append(" }");

            return retVal.toString();
        }

    }

    public static final class Primitive64 implements Householder<Double> {

        public double beta;
        public int first;
        public final double[] vector;

        public Primitive64(final Householder<Double> aTransf) {

            this((int) aTransf.count());

            this.copy(aTransf);
        }

        public Primitive64(final int aDim) {

            super();

            vector = new double[aDim];
            beta = PrimitiveMath.ZERO;
            first = 0;
        }

        public Householder.Primitive64 copy(final Householder<Double> source) {

            first = source.first();

            final double[] tmpVector = vector;
            double tmpVal, tmpVal2 = PrimitiveMath.ZERO;
            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpVal = source.doubleValue(i);
                tmpVal2 += tmpVal * tmpVal;
                tmpVector[i] = tmpVal;
            }

            beta = PrimitiveMath.TWO / tmpVal2;

            return this;
        }

        public Householder.Primitive64 copy(final Householder<Double> source, final double precalculatedBeta) {

            first = source.first();

            final double[] tmpVector = vector;

            final int tmpSize = (int) source.count();
            for (int i = source.first(); i < tmpSize; i++) {
                tmpVector[i] = source.doubleValue(i);
            }

            beta = precalculatedBeta;

            return this;
        }

        @Override
        public long count() {
            return vector.length;
        }

        @Override
        public double doubleValue(final int anInd) {
            return vector[anInd];
        }

        @Override
        public int first() {
            return first;
        }

        @Override
        public Double get(final long index) {
            return vector[Math.toIntExact(index)];
        }

        @Override
        public String toString() {

            final StringBuilder retVal = new StringBuilder("{ ");

            final int tmpLastIndex = vector.length - 1;
            for (int i = 0; i < tmpLastIndex; i++) {
                retVal.append(this.get(i));
                retVal.append(", ");
            }
            retVal.append(this.get(tmpLastIndex));

            retVal.append(" }");

            return retVal.toString();
        }

    }

    /**
     * Regardless of what is actually returned by {@linkplain #doubleValue(long)} and/or
     * {@linkplain #get(long)} vector elements with indeces less than 'first' should be assumed to be, and
     * treated as if they are, zero.
     */
    int first();

    default void transform(final PhysicalStore<N> matrix) {
        matrix.transformLeft(this, 0);
    }

}
