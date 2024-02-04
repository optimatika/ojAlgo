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
package org.ojalgo.matrix.task;

import java.util.Optional;
import java.util.function.Supplier;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.Provider2D;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.decomposition.SingularValue;
import org.ojalgo.matrix.store.ElementsSupplier;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Structure2D;

public interface InverterTask<N extends Comparable<N>> extends MatrixTask<N> {

    public static abstract class Factory<N extends Comparable<N>> {

        public MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {
            return this.make(original, false, false).invert(original);
        }

        public InverterTask<N> make(final int dim, final boolean spd) {

            Structure2D template = new Structure2D() {

                public long countColumns() {
                    return dim;
                }

                public long countRows() {
                    return dim;
                }
            };

            return this.make(template, spd, spd);
        }

        public InverterTask<N> make(final MatrixStore<N> template) {
            return this.make(template, template.isHermitian(), false);
        }

        public abstract InverterTask<N> make(Structure2D template, boolean symmetric, boolean positiveDefinite);
    }

    Factory<ComplexNumber> C128 = new Factory<>() {

        @Override
        public InverterTask<ComplexNumber> make(final Structure2D template, final boolean symmetric, final boolean positiveDefinite) {
            if (symmetric && positiveDefinite) {
                return Cholesky.C128.make(template);
            } else if (template.isSquare()) {
                return LU.C128.make(template);
            } else if (template.isTall()) {
                return QR.C128.make(template);
            } else {
                return SingularValue.C128.make(template);
            }
        }

    };

    /**
     * @deprecated Use {@link #C128} instead.
     */
    @Deprecated
    Factory<ComplexNumber> COMPLEX = C128;

    Factory<Double> R064 = new Factory<>() {

        @Override
        public InverterTask<Double> make(final Structure2D template, final boolean symmetric, final boolean positiveDefinite) {

            long nbRows = template.countRows();

            if (symmetric) {
                if (nbRows == 1L) {
                    return AbstractInverter.FULL_1X1;
                } else if (nbRows == 2L) {
                    return AbstractInverter.SYMMETRIC_2X2;
                } else if (nbRows == 3L) {
                    return AbstractInverter.SYMMETRIC_3X3;
                } else if (nbRows == 4L) {
                    return AbstractInverter.SYMMETRIC_4X4;
                } else if (nbRows == 5L) {
                    return AbstractInverter.SYMMETRIC_5X5;
                } else {
                    return positiveDefinite ? Cholesky.R064.make(template) : LU.R064.make(template);
                }
            } else if (template.isSquare()) {
                if (nbRows == 1L) {
                    return AbstractInverter.FULL_1X1;
                } else if (nbRows == 2L) {
                    return AbstractInverter.FULL_2X2;
                } else if (nbRows == 3L) {
                    return AbstractInverter.FULL_3X3;
                } else if (nbRows == 4L) {
                    return AbstractInverter.FULL_4X4;
                } else if (nbRows == 5L) {
                    return AbstractInverter.FULL_5X5;
                } else {
                    return LU.R064.make(template);
                }
            } else if (template.isTall()) {
                return QR.R064.make(template);
            } else {
                return SingularValue.R064.make(template);
            }
        }

    };

    /**
     * @deprecated Use {@link #R064} instead.
     */
    @Deprecated
    Factory<Double> PRIMITIVE = R064;

    Factory<Quadruple> R128 = new Factory<>() {

        @Override
        public InverterTask<Quadruple> make(final Structure2D template, final boolean symmetric, final boolean positiveDefinite) {
            if (template.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.R128.make(template);
                } else {
                    return LU.R128.make(template);
                }
            } else if (template.isTall()) {
                return QR.R128.make(template);
            } else {
                return SingularValue.R128.make(template);
            }
        }

    };

    /**
     * @deprecated Use {@link #R128} instead.
     */
    @Deprecated
    Factory<Quadruple> QUADRUPLE = R128;

    Factory<Quaternion> H256 = new Factory<>() {

        @Override
        public InverterTask<Quaternion> make(final Structure2D template, final boolean symmetric, final boolean positiveDefinite) {
            if (template.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.H256.make(template);
                } else {
                    return LU.H256.make(template);
                }
            } else if (template.isTall()) {
                return QR.H256.make(template);
            } else {
                return SingularValue.H256.make(template);
            }
        }

    };

    /**
     * @deprecated Use {@link #H256} instead.
     */
    @Deprecated
    Factory<Quaternion> QUATERNION = H256;

    Factory<RationalNumber> Q128 = new Factory<>() {

        @Override
        public InverterTask<RationalNumber> make(final Structure2D template, final boolean symmetric, final boolean positiveDefinite) {
            if (template.isSquare()) {
                if (symmetric && positiveDefinite) {
                    return Cholesky.Q128.make(template);
                } else {
                    return LU.Q128.make(template);
                }
            } else if (template.isTall()) {
                return QR.Q128.make(template);
            } else {
                return SingularValue.Q128.make(template);
            }
        }

    };

    /**
     * @deprecated Use {@link #Q128} instead.
     */
    @Deprecated
    Factory<RationalNumber> RATIONAL = Q128;

    /**
     * The output must be a "right inverse" and a "generalised inverse".
     */
    default MatrixStore<N> invert(final Access2D<?> original) throws RecoverableCondition {
        return this.invert(original, this.preallocate(original));
    }

    /**
     * <p>
     * Exactly how (if at all) a specific implementation makes use of <code>preallocated</code> is not
     * specified by this interface. It must be documented for each implementation.
     * </p>
     * <p>
     * Should produce the same results as calling {@link #invert(Access2D)}.
     * </p>
     * <p>
     * Use {@link #preallocate(Structure2D)} to obtain a suitbale <code>preallocated</code>.
     * </p>
     *
     * @param preallocated Preallocated memory for the results, possibly some intermediate results. You must
     *        assume this is modified, but you cannot assume it will contain the full/ /correct solution.
     * @return The inverse
     * @throws RecoverableCondition TODO
     */
    MatrixStore<N> invert(Access2D<?> original, PhysicalStore<N> preallocated) throws RecoverableCondition;

    default PhysicalStore<N> preallocate(final int numberOfRows, final int numberOfColumns) {
        return this.preallocate(new Structure2D() {

            public long countColumns() {
                return numberOfColumns;
            }

            public long countRows() {
                return numberOfRows;
            }

        });
    }

    /**
     * <p>
     * Will create a {@linkplain PhysicalStore} instance suitable for use with
     * {@link #invert(Access2D, PhysicalStore)}.
     * </p>
     * <p>
     * When inverting a matrix (mxn) the preallocated memory/matrix will typically be nxm (and of course most
     * of the time A is square).
     * </p>
     */
    PhysicalStore<N> preallocate(Structure2D template);

    default Provider2D.Inverse<Optional<MatrixStore<N>>> toInverseProvider(final ElementsSupplier<N> original,
            final Supplier<MatrixStore<N>> alternativeOriginalSupplier) {
        try {
            MatrixStore<N> invert = this.invert(alternativeOriginalSupplier.get());
            return () -> Optional.of(invert);
        } catch (RecoverableCondition cause) {
            return Optional::empty;
        }
    }

}
