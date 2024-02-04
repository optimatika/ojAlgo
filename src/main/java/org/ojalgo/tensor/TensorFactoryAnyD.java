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
package org.ojalgo.tensor;

import java.util.Arrays;

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.FactoryAnyD;
import org.ojalgo.structure.MutateAnyD;
import org.ojalgo.type.math.MathType;

public final class TensorFactoryAnyD<N extends Comparable<N>, T extends MutateAnyD> implements FactoryAnyD<T> {

    public static <N extends Comparable<N>, T extends MutateAnyD> TensorFactoryAnyD<N, T> of(final FactoryAnyD<T> factory) {
        return new TensorFactoryAnyD<>(factory);
    }

    private final FactoryAnyD<T> myFactory;

    TensorFactoryAnyD(final FactoryAnyD<T> factory) {

        super();

        myFactory = factory;
    }

    /**
     * Same as {@link TensorFactory2D#blocks(Access2D...)} but for higher/aribitrary rank tensors.
     */
    public T blocks(final AccessAnyD<N>... tensors) {

        int rank = 1;
        for (AccessAnyD<N> tensor : tensors) {
            rank = Math.max(rank, tensor.shape().length);
        }

        long[] structure = new long[rank];
        for (int r = 0; r < structure.length; r++) {
            long count = 0L;
            for (int t = 0; t < tensors.length; t++) {
                count += tensors[t].count(r);
            }
            structure[r] = count;
        }

        T retVal = myFactory.make(structure);

        long[] offset = new long[rank];
        long[] outRef = new long[rank];

        for (AccessAnyD<N> tensor : tensors) {

            tensor.loopAllReferences(inRef -> {

                double value = tensor.doubleValue(inRef);

                System.arraycopy(offset, 0, outRef, 0, offset.length);
                for (int i = 0; i < inRef.length; i++) {
                    outRef[i] += inRef[i];
                }

                retVal.set(outRef, value);
            });

            for (int i = 0; i < offset.length; i++) {
                offset[i] += tensor.count(i);
            }
        }

        return retVal;
    }

    public T copy(final Access1D<N> elements) {

        T retVal = myFactory.make(elements.count());

        for (long i = 0; i < elements.count(); i++) {
            retVal.set(i, elements.get(i));
        }

        return retVal;
    }

    public T copy(final Access2D<N> elements) {

        T retVal = myFactory.make(elements.countRows(), elements.countColumns());

        for (long i = 0; i < elements.count(); i++) {
            retVal.set(i, elements.get(i));
        }

        return retVal;
    }

    public T copy(final AccessAnyD<N> elements) {

        T retVal = myFactory.make(elements);

        for (long i = 0; i < elements.count(); i++) {
            retVal.set(i, elements.get(i));
        }

        return retVal;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof TensorFactoryAnyD)) {
            return false;
        }
        TensorFactoryAnyD other = (TensorFactoryAnyD) obj;
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        return true;
    }

    public FunctionSet<N> function() {
        return (FunctionSet<N>) myFactory.function();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + (myFactory == null ? 0 : myFactory.hashCode());
    }

    public T make(final long... structure) {
        return myFactory.make(structure);
    }

    public T power(final Access1D<N> vector, final int exponent) {
        Access1D<N>[] vectors = (Access1D<N>[]) new Access1D<?>[exponent];
        Arrays.fill(vectors, vector);
        return this.product(vectors);
    }

    public T product(final Access1D<?>... vectors) {

        int rank = vectors.length;

        long[] shape = new long[rank];
        for (int i = 0; i < rank; i++) {
            shape[i] = vectors[i].count();
        }

        T retVal = myFactory.make(shape);

        retVal.loopAllReferences(ref -> {
            double val = 1.0;
            for (int d = 0; d < ref.length; d++) {
                val *= vectors[d].doubleValue(ref[d]);
            }
            retVal.set(ref, val);
        });

        return retVal;
    }

    public Scalar.Factory<N> scalar() {
        return (Factory<N>) myFactory.scalar();
    }

    /**
     * Direct sum of vectors. The rank of the returned object will be 1.
     *
     * @see TensorFactory1D#sum(Access1D...)
     */
    public T sum(final Access1D<N>... vectors) {

        long dimensions = 0;
        for (Access1D<N> vector : vectors) {
            dimensions += vector.count();
        }

        T retVal = myFactory.make(dimensions);

        long offset = 0L;
        for (Access1D<N> vector : vectors) {
            long limit = vector.count();
            for (int i = 0; i < limit; i++) {
                retVal.set(offset + i, vector.doubleValue(i));
            }
            offset += limit;
        }

        return retVal;
    }

    public MathType getMathType() {
        return myFactory.getMathType();
    }

}
