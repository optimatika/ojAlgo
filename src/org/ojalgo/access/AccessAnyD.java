/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
package org.ojalgo.access;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.scalar.Scalar;

public interface AccessAnyD<N extends Number> extends StructureAnyD, Access1D<N> {

    /**
     * This interface mimics {@linkplain Fillable}, but methods return the builder instance instead, and then
     * adds the {@link #build()} method.
     *
     * @author apete
     */
    public interface Builder<I extends AccessAnyD<?>> extends StructureAnyD, Access1D.Builder<I> {

        I build();

        Builder<I> set(long[] reference, double value);

        Builder<I> set(long[] reference, Number value);

    }

    public interface Elements extends StructureAnyD, Access1D.Elements {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long[] reference);

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long[] reference, double comparedTo);

        default boolean isZero(final long[] reference) {
            return this.isSmall(reference, PrimitiveMath.ONE);
        }

    }

    public interface Factory<I extends AccessAnyD<?>> {

        I copy(AccessAnyD<?> source);

        I makeFilled(long[] structure, NullaryFunction<?> supplier);

        I makeZero(long... structure);

    }

    public interface Fillable<N extends Number> extends Settable<N>, Access1D.Fillable<N> {

        void fillOne(long[] reference, N value);

        void fillOne(long[] reference, NullaryFunction<N> supplier);

        default void fillRange(final long first, final long limit, final N value) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, value);
            }
        }

        default void fillRange(final long first, final long limit, final NullaryFunction<N> supplier) {
            for (long i = first; i < limit; i++) {
                this.fillOne(i, supplier);
            }
        }

    }

    public interface Modifiable<N extends Number> extends Settable<N>, Access1D.Modifiable<N> {

        void modifyOne(long[] reference, UnaryFunction<N> function);

        default void modifyRange(final long first, final long limit, final UnaryFunction<N> function) {
            for (long i = first; i < limit; i++) {
                this.modifyOne(i, function);
            }
        }

    }

    public interface Settable<N extends Number> extends StructureAnyD, Access1D.Settable<N> {

        default void add(final long index, final double addend) {
            this.add(AccessUtils.reference(index, this.shape()), addend);
        }

        default void add(final long index, final Number addend) {
            this.add(AccessUtils.reference(index, this.shape()), addend);
        }

        void add(long[] reference, double addend);

        void add(long[] reference, Number addend);

        default void set(final long index, final double value) {
            this.set(AccessUtils.reference(index, this.shape()), value);
        }

        default void set(final long index, final Number value) {
            this.set(AccessUtils.reference(index, this.shape()), value);
        }

        void set(long[] reference, double value);

        void set(long[] reference, Number value);

    }

    public interface IndexOf extends StructureAnyD, Access1D.IndexOf {

    }

    public interface Visitable<N extends Number> extends StructureAnyD, Access1D.Visitable<N> {

        void visitOne(long[] reference, VoidFunction<N> visitor);

    }

    public interface Sliceable<N extends Number> extends StructureAnyD, Access1D.Sliceable<N> {

        Access1D<N> slice(final long[] first, final int dimension);

    }

    default double doubleValue(final long index) {
        return this.doubleValue(AccessUtils.reference(index, this.shape()));
    }

    double doubleValue(long[] reference);

    default N get(final long index) {
        return this.get(AccessUtils.reference(index, this.shape()));
    }

    N get(long[] reference);

}
