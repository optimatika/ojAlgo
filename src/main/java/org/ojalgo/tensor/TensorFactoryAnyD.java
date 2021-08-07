/*
 * Copyright 1997-2021 Optimatika
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

import org.ojalgo.function.FunctionSet;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.scalar.Scalar.Factory;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.AccessAnyD;
import org.ojalgo.structure.FactoryAnyD;
import org.ojalgo.structure.MutateAnyD;

public final class TensorFactoryAnyD<N extends Comparable<N>, T extends MutateAnyD> implements FactoryAnyD<T> {

    public static <N extends Comparable<N>, T extends MutateAnyD> TensorFactoryAnyD<N, T> of(final FactoryAnyD<T> factory) {
        return new TensorFactoryAnyD<>(factory);
    }

    private final FactoryAnyD<T> myFactory;

    TensorFactoryAnyD(final FactoryAnyD<T> factory) {

        super();

        myFactory = factory;
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
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TensorFactoryAnyD)) {
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
        result = (prime * result) + ((myFactory == null) ? 0 : myFactory.hashCode());
        return result;
    }

    public T make(final long... structure) {
        return myFactory.make(structure);
    }

    public T product(final Access1D<?>... vectors) {

        int rank = vectors.length;

        long[] shape = new long[rank];
        for (int i = 0; i < rank; i++) {
            shape[i] = vectors[i].count();
        }

        T retVal = myFactory.make(shape);

        retVal.loopAll((final long[] ref) -> {
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

}
