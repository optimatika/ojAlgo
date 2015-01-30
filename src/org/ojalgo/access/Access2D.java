/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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

import java.util.List;

import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.Scalar;

public interface Access2D<N extends Number> extends Structure2D, Access1D<N> {

    /**
     * This interface mimics {@linkplain Fillable}, but methods return the builder instance instead, and then adds the
     * {@link #build()} method.
     *
     * @author apete
     */
    public interface Builder<I extends Access2D<?>> extends Structure2D, Access1D.Builder<I> {

        I build();

        Builder<I> fillColumn(long row, long column, Number value);

        Builder<I> fillDiagonal(long row, long column, Number value);

        Builder<I> fillRow(long row, long column, Number value);

        Builder<I> set(long row, long column, double value);

        Builder<I> set(long row, long column, Number value);

    }

    public interface Elements extends Structure2D, Access1D.Elements {

        /**
         * @see Scalar#isAbsolute()
         */
        boolean isAbsolute(long row, long column);

        /**
         * @see Scalar#isSmall(double)
         */
        boolean isSmall(long row, long column, double comparedTo);

        /**
         * @see Scalar#isZero()
         * @deprecated v37
         */
        @Deprecated
        default boolean isZero(final long row, final long column) {
            return this.isSmall(row, column, PrimitiveMath.ONE);
        }

    }

    public interface Factory<I extends Access2D<?>> {

        I columns(Access1D<?>... source);

        I columns(double[]... source);

        @SuppressWarnings("unchecked")
        I columns(List<? extends Number>... source);

        I columns(Number[]... source);

        I copy(Access2D<?> source);

        I makeEye(long rows, long columns);

        I makeRandom(long rows, long columns, RandomNumber distribution);

        I makeZero(long rows, long columns);

        I rows(Access1D<?>... source);

        I rows(double[]... source);

        @SuppressWarnings("unchecked")
        I rows(List<? extends Number>... source);

        I rows(Number[]... source);

    }

    public interface Fillable<N extends Number> extends Structure2D, Access1D.Fillable<N> {

        default void fillColumn(final long row, final long column, final Access1D<N> values) {
            for (long i = 0L; i < values.count(); i++) {
                this.set(row + i, column, values.get(i));
            }
        }

        void fillColumn(long row, long column, N value);

        void fillColumn(long row, long column, NullaryFunction<N> supplier);

        default void fillDiagonal(final long row, final long column, final Access1D<N> values) {
            for (long ij = 0L; ij < values.count(); ij++) {
                this.set(row + ij, column + ij, values.get(ij));
            }
        }

        void fillDiagonal(long row, long column, N value);

        void fillDiagonal(long row, long column, NullaryFunction<N> supplier);

        default void fillRow(final long row, final long column, final Access1D<N> values) {
            for (long j = 0L; j < values.count(); j++) {
                this.set(row, column + j, values.get(j));
            }
        }

        void fillRow(long row, long column, N value);

        void fillRow(long row, long column, NullaryFunction<N> supplier);

        void set(long row, long column, double value);

        void set(long row, long column, Number value);

    }

    public interface Iterable2D<N extends Number> {

        Iterable<Access1D<N>> columns();

        Iterable<Access1D<N>> rows();
    }

    public interface Modifiable<N extends Number> extends Structure2D, Access1D.Modifiable<N> {

        void modifyColumn(long row, long column, UnaryFunction<N> function);

        void modifyDiagonal(long row, long column, UnaryFunction<N> function);

        void modifyOne(long row, long column, UnaryFunction<N> function);

        void modifyRow(long row, long column, UnaryFunction<N> function);

    }

    public interface Visitable<N extends Number> extends Structure2D, Access1D.Visitable<N> {

        void visitColumn(long row, long column, VoidFunction<N> visitor);

        void visitDiagonal(long row, long column, VoidFunction<N> visitor);

        void visitRow(long row, long column, VoidFunction<N> visitor);

    }

    /**
     * Extracts one element of this matrix as a double.
     *
     * @param row A row index.
     * @param column A column index.
     * @return One matrix element
     */
    double doubleValue(long row, long column);

    N get(long row, long column);

}
