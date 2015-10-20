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
package org.ojalgo.matrix.task;

import java.math.BigDecimal;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.scalar.ComplexNumber;

public interface DeterminantTask<N extends Number> extends MatrixTask<N> {

    public static abstract class Factory<N extends Number> {

        public final DeterminantTask<N> make(final MatrixStore<N> template) {
            return this.make(template, MatrixUtils.isHermitian(template));
        }

        public abstract DeterminantTask<N> make(MatrixStore<N> template, boolean symmetric);

    }

    public static final Factory<BigDecimal> BIG = new Factory<BigDecimal>() {

        @Override
        public DeterminantTask<BigDecimal> make(final MatrixStore<BigDecimal> template, final boolean symmetric) {
            if (symmetric) {
                return Cholesky.make(template);
            } else {
                return LU.make(template);
            }
        }

    };

    public static final Factory<ComplexNumber> COMPLEX = new Factory<ComplexNumber>() {

        @Override
        public DeterminantTask<ComplexNumber> make(final MatrixStore<ComplexNumber> template, final boolean symmetric) {
            if (symmetric) {
                return Cholesky.make(template);
            } else {
                return LU.make(template);
            }
        }

    };

    public static final Factory<Double> PRIMITIVE = new Factory<Double>() {

        @Override
        public DeterminantTask<Double> make(final MatrixStore<Double> template, final boolean symmetric) {
            final long tmpDim = template.countRows();
            if (tmpDim == 1L) {
                return AbstractDeterminator.FULL_1X1;
            } else if (symmetric) {
                if (tmpDim == 2L) {
                    return AbstractDeterminator.SYMMETRIC_2X2;
                } else if (tmpDim == 3L) {
                    return AbstractDeterminator.SYMMETRIC_3X3;
                } else if (tmpDim == 4L) {
                    return AbstractDeterminator.SYMMETRIC_4X4;
                } else if (tmpDim == 5L) {
                    return AbstractDeterminator.SYMMETRIC_5X5;
                } else {
                    return Cholesky.make(template);
                }
            } else {
                if (tmpDim == 2L) {
                    return AbstractDeterminator.FULL_2X2;
                } else if (tmpDim == 3L) {
                    return AbstractDeterminator.FULL_3X3;
                } else if (tmpDim == 4L) {
                    return AbstractDeterminator.FULL_4X4;
                } else if (tmpDim == 5L) {
                    return AbstractDeterminator.FULL_5X5;
                } else {
                    return LU.make(template);
                }
            }
        }

    };

    N calculateDeterminant(Access2D<?> matrix);

}
