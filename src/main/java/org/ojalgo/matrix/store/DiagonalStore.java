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
package org.ojalgo.matrix.store;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;

public final class DiagonalStore<N extends Comparable<N>, D extends Access1D<?>> extends FactoryStore<N> {

    public static class Builder<N extends Comparable<N>, D extends Access1D<?>> implements Supplier<DiagonalStore<N, D>> {

        private final PhysicalStore.Factory<N, ?> myFactory;
        private final D myMainDiagonal;
        private D mySubdiagonal = null;
        private D mySuperdiagonal = null;

        Builder(final PhysicalStore.Factory<N, ?> factory, final D mainDiagonal) {
            super();
            myFactory = factory;
            myMainDiagonal = mainDiagonal;
        }

        @Override
        public DiagonalStore<N, D> get() {
            long dim = this.dimension();
            return new DiagonalStore<>(myFactory, dim, dim, myMainDiagonal, mySuperdiagonal, mySubdiagonal);
        }

        public Builder<N, D> subdiagonal(final D subdiagonal) {
            mySubdiagonal = subdiagonal;
            return this;
        }

        public Builder<N, D> superdiagonal(final D superdiagonal) {
            mySuperdiagonal = superdiagonal;
            return this;
        }

        private int dimension() {
            if (myMainDiagonal != null) {
                return Math.toIntExact(myMainDiagonal.count());
            } else if (mySuperdiagonal != null) {
                return Math.toIntExact(mySuperdiagonal.count() + 1L);
            } else if (mySubdiagonal != null) {
                return Math.toIntExact(mySubdiagonal.count() + 1L);
            } else {
                return 0;
            }
        }

    }

    public static <N extends Comparable<N>, D extends Access1D<?>> Builder<N, D> builder(final PhysicalStore.Factory<N, ?> factory, final D mainDiagonal) {
        return new Builder<>(factory, mainDiagonal);
    }

    private final D myMainDiagonal;
    private final Scalar.Factory<N> myScalarFactory;
    private final D mySubdiagonal;
    private final D mySuperdiagonal;
    private final N myZero;

    DiagonalStore(final PhysicalStore.Factory<N, ?> factory, final long numberOfRows, final long numberOfColumns, final D mainDiag, final D superdiag,
            final D subdiag) {

        super(factory, numberOfRows, numberOfColumns);

        myMainDiagonal = mainDiag;
        mySuperdiagonal = superdiag;
        mySubdiagonal = subdiag;

        myScalarFactory = factory.scalar();
        myZero = factory.scalar().zero().get();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        if (myMainDiagonal != null && row == col) {
            return myMainDiagonal.doubleValue(row);
        } else if (mySuperdiagonal != null && col - row == 1L) {
            return mySuperdiagonal.doubleValue(row);
        } else if (mySubdiagonal != null && row - col == 1L) {
            return mySubdiagonal.doubleValue(col);
        } else {
            return PrimitiveMath.ZERO;
        }
    }

    @Override
    public int firstInColumn(final int col) {
        if (mySuperdiagonal != null) {
            return col - 1;
        } else if (myMainDiagonal != null) {
            return col;
        } else if (mySubdiagonal != null) {
            return col + 1;
        } else {
            return this.getRowDim();
        }
    }

    @Override
    public int firstInRow(final int row) {
        if (mySubdiagonal != null) {
            return row - 1;
        } else if (myMainDiagonal != null) {
            return row;
        } else if (mySuperdiagonal != null) {
            return row + 1;
        } else {
            return this.getColDim();
        }
    }

    @Override
    public N get(final int row, final int col) {
        if (myMainDiagonal != null && row == col) {
            return myScalarFactory.cast(myMainDiagonal.get(row));
        } else if (mySuperdiagonal != null && col - row == 1L) {
            return myScalarFactory.cast(mySuperdiagonal.get(row));
        } else if (mySubdiagonal != null && row - col == 1L) {
            return myScalarFactory.cast(mySubdiagonal.get(col));
        } else {
            return myZero;
        }
    }

    /**
     * @return The main diagonal length
     */
    public int getDimension() {
        return MissingMath.toMinIntExact(this.countRows(), this.countColumns());
    }

    public Optional<D> getMainDiagonal() {
        return Optional.ofNullable(myMainDiagonal);
    }

    public Optional<D> getSubdiagonal() {
        return Optional.ofNullable(mySubdiagonal);
    }

    public Optional<D> getSuperdiagonal() {
        return Optional.ofNullable(mySuperdiagonal);
    }

    @Override
    public int limitOfColumn(final int col) {
        if (mySubdiagonal != null) {
            return Math.min(col + 2, this.getRowDim());
        } else if (myMainDiagonal != null) {
            return Math.min(col + 1, this.getRowDim());
        } else if (mySuperdiagonal != null) {
            return Math.min(col, this.getRowDim());
        } else {
            return 0;
        }
    }

    @Override
    public int limitOfRow(final int row) {
        if (mySuperdiagonal != null) {
            return Math.min(row + 2, this.getColDim());
        } else if (myMainDiagonal != null) {
            return Math.min(row + 1, this.getColDim());
        } else if (mySubdiagonal != null) {
            return Math.min(row, this.getColDim());
        } else {
            return 0;
        }
    }

    public void supplyMainDiagonalTo(final double[] receiver) {
        if (myMainDiagonal != null) {
            myMainDiagonal.supplyTo(receiver);
        } else {
            Arrays.fill(receiver, PrimitiveMath.ZERO);
        }
    }

    public void supplySubdiagonalTo(final double[] receiver) {
        if (mySubdiagonal != null) {
            mySubdiagonal.supplyTo(receiver);
        } else {
            Arrays.fill(receiver, PrimitiveMath.ZERO);
        }
    }

    public void supplySuperdiagonalTo(final double[] receiver) {
        if (mySuperdiagonal != null) {
            mySuperdiagonal.supplyTo(receiver);
        } else {
            Arrays.fill(receiver, PrimitiveMath.ZERO);
        }
    }

    @Override
    public void supplyTo(final TransformableRegion<N> consumer) {
        consumer.reset();
        if (this.isPrimitive()) {
            if (myMainDiagonal != null) {
                for (long i = 0L; i < myMainDiagonal.count(); i++) {
                    consumer.set(i, i, myMainDiagonal.doubleValue(i));
                }
            }
            if (mySubdiagonal != null) {
                for (long i = 0L; i < mySubdiagonal.count(); i++) {
                    consumer.set(i + 1, i, mySubdiagonal.doubleValue(i));
                }
            }
            if (mySuperdiagonal != null) {
                for (long i = 0L; i < mySuperdiagonal.count(); i++) {
                    consumer.set(i, i + 1, mySuperdiagonal.doubleValue(i));
                }
            }
        } else {
            if (myMainDiagonal != null) {
                for (long i = 0L; i < myMainDiagonal.count(); i++) {
                    consumer.set(i, i, myMainDiagonal.get(i));
                }
            }
            if (mySubdiagonal != null) {
                for (long i = 0L; i < mySubdiagonal.count(); i++) {
                    consumer.set(i + 1, i, mySubdiagonal.get(i));
                }
            }
            if (mySuperdiagonal != null) {
                for (long i = 0L; i < mySuperdiagonal.count(); i++) {
                    consumer.set(i, i + 1, mySuperdiagonal.get(i));
                }
            }
        }
    }

    @Override
    public Scalar<N> toScalar(final long row, final long col) {
        if (myMainDiagonal != null && row == col) {
            return myScalarFactory.convert(myMainDiagonal.get(row));
        } else if (mySuperdiagonal != null && col - row == 1L) {
            return myScalarFactory.convert(mySuperdiagonal.get(row));
        } else if (mySubdiagonal != null && row - col == 1L) {
            return myScalarFactory.convert(mySubdiagonal.get(col));
        } else {
            return myScalarFactory.zero();
        }
    }

}
