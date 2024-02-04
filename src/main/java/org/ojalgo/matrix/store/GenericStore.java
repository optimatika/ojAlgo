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
import java.util.List;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.*;
import org.ojalgo.array.operation.*;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.AggregatorSet;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.operation.HouseholderLeft;
import org.ojalgo.matrix.operation.HouseholderRight;
import org.ojalgo.matrix.operation.MultiplyBoth;
import org.ojalgo.matrix.operation.MultiplyLeft;
import org.ojalgo.matrix.operation.MultiplyNeither;
import org.ojalgo.matrix.operation.MultiplyRight;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.HouseholderReference;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.Quadruple;
import org.ojalgo.scalar.Quaternion;
import org.ojalgo.scalar.RationalNumber;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.type.math.MathType;

/**
 * A generic implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class GenericStore<N extends Scalar<N>> extends ScalarArray<N> implements PhysicalStore<N>, DecompositionStore<N> {

    static final class Factory<N extends Scalar<N>> implements PhysicalStore.Factory<N, GenericStore<N>> {

        private final DenseArray.Factory<N> myDenseArrayFactory;

        Factory(final DenseArray.Factory<N> denseArrayFactory) {
            super();
            myDenseArrayFactory = denseArrayFactory;
        }

        @Override
        public AggregatorSet<N> aggregator() {
            return myDenseArrayFactory.function().aggregator();
        }

        @Override
        public DenseArray.Factory<N> array() {
            return myDenseArrayFactory;
        }

        @Override
        public GenericStore<N> columns(final Access1D<?>... source) {

            int tmpRowDim = source[0].size();
            int tmpColDim = source.length;

            N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpColumn.get(i));
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> columns(final Comparable<?>[]... source) {

            int tmpRowDim = source[0].length;
            int tmpColDim = source.length;

            N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            Comparable<?>[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpColumn[i]);
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> columns(final double[]... source) {

            int tmpRowDim = source[0].length;
            int tmpColDim = source.length;

            N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpColumn[i]);
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> columns(final List<? extends Comparable<?>>... source) {

            int tmpRowDim = source[0].size();
            int tmpColDim = source.length;

            N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            List<? extends Comparable<?>> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpColumn.get(i));
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> conjugate(final Access2D<?> source) {

            GenericStore<N> retVal = new GenericStore<>(this, (int) source.countColumns(), (int) source.countRows());

            int tmpRowDim = retVal.getRowDim();
            int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.conjugate(retVal.data, tmpRowDim, aFirst, aLimit, source, Factory.this.scalar());
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.conjugate(retVal.data, tmpRowDim, 0, tmpColDim, source, Factory.this.scalar());
            }

            return retVal;
        }

        @Override
        public GenericStore<N> copy(final Access2D<?> source) {

            int tmpRowDim = source.getRowDim();
            int tmpColDim = source.getColDim();

            final GenericStore<N> retVal = new GenericStore<>(this, tmpRowDim, tmpColDim);

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.copy(retVal.data, tmpRowDim, aFirst, aLimit, source, Factory.this.scalar());
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.copy(retVal.data, tmpRowDim, 0, tmpColDim, source, Factory.this.scalar());
            }

            return retVal;
        }

        @Override
        public FunctionSet<N> function() {
            return myDenseArrayFactory.function();
        }

        @Override
        public MathType getMathType() {
            return myDenseArrayFactory.getMathType();
        }

        @Override
        public GenericStore<N> make(final long rows, final long columns) {
            return new GenericStore<>(this, (int) rows, (int) columns);
        }

        @Override
        public Householder.Generic<N> makeHouseholder(final int length) {
            return new Householder.Generic<>(myDenseArrayFactory.scalar(), length);
        }

        @Override
        public Rotation.Generic<N> makeRotation(final int low, final int high, final double cos, final double sin) {
            return this.makeRotation(low, high, myDenseArrayFactory.scalar().cast(cos), myDenseArrayFactory.scalar().cast(sin));
        }

        @Override
        public Rotation.Generic<N> makeRotation(final int low, final int high, final N cos, final N sin) {
            return new Rotation.Generic<>(low, high, cos, sin);
        }

        @Override
        public GenericStore<N> rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpRow.get(j));
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> rows(final Comparable<?>[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            Comparable<?>[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpRow[j]);
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpRow[j]);
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public GenericStore<N> rows(final List<? extends Comparable<?>>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final N[] tmpData = myDenseArrayFactory.scalar().newArrayInstance(tmpRowDim * tmpColDim);

            List<? extends Comparable<?>> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + tmpRowDim * j] = myDenseArrayFactory.scalar().cast(tmpRow.get(j));
                }
            }

            return new GenericStore<>(this, tmpRowDim, tmpColDim, tmpData);
        }

        @Override
        public Scalar.Factory<N> scalar() {
            return myDenseArrayFactory.scalar();
        }

        @Override
        public GenericStore<N> transpose(final Access2D<?> source) {

            GenericStore<N> retVal = new GenericStore<>(this, (int) source.countColumns(), (int) source.countRows());

            int tmpRowDim = retVal.getRowDim();
            int tmpColDim = retVal.getColDim();

            if (tmpColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int aFirst, final int aLimit) {
                        FillMatchingSingle.transpose(retVal.data, tmpRowDim, aFirst, aLimit, source, Factory.this.scalar());
                    }

                };

                tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(retVal.data, tmpRowDim, 0, tmpColDim, source, Factory.this.scalar());
            }

            return retVal;
        }

    }

    public static final PhysicalStore.Factory<ComplexNumber, GenericStore<ComplexNumber>> C128 = new GenericStore.Factory<>(ArrayC128.FACTORY);
    /**
     * @deprecated Use {@link #C128} instead
     */
    @Deprecated
    public static final PhysicalStore.Factory<ComplexNumber, GenericStore<ComplexNumber>> COMPLEX = C128;
    public static final PhysicalStore.Factory<Quaternion, GenericStore<Quaternion>> H256 = new GenericStore.Factory<>(ArrayH256.FACTORY);
    public static final PhysicalStore.Factory<RationalNumber, GenericStore<RationalNumber>> Q128 = new GenericStore.Factory<>(ArrayQ128.FACTORY);
    public static final PhysicalStore.Factory<Quadruple, GenericStore<Quadruple>> R128 = new GenericStore.Factory<>(ArrayR128.FACTORY);
    /**
     * @deprecated Use {@link #R128} instead
     */
    @Deprecated
    public static final PhysicalStore.Factory<Quadruple, GenericStore<Quadruple>> QUADRUPLE = R128;
    /**
     * @deprecated Use {@link #H256} instead
     */
    @Deprecated
    public static final PhysicalStore.Factory<Quaternion, GenericStore<Quaternion>> QUATERNION = H256;
    /**
     * @deprecated Use {@link #Q128} instead
     */
    @Deprecated
    public static final PhysicalStore.Factory<RationalNumber, GenericStore<RationalNumber>> RATIONAL = Q128;

    public static <N extends Scalar<N>> GenericStore<N> wrap(final GenericStore.Factory<N> factory, final N... data) {
        return new GenericStore<>(factory, data.length, 1, data);
    }

    public static <N extends Scalar<N>> GenericStore<N> wrap(final GenericStore.Factory<N> factory, final N[] data, final int structure) {
        return new GenericStore<>(factory, structure, data.length / structure, data);
    }

    private final MultiplyBoth.Generic<N> multiplyBoth;
    private final MultiplyLeft.Generic<N> multiplyLeft;
    private final MultiplyNeither.Generic<N> multiplyNeither;
    private final MultiplyRight.Generic<N> multiplyRight;
    private final int myColDim;
    private final GenericStore.Factory<N> myFactory;
    private final int myRowDim;
    private final Array2D<N> myUtility;
    private transient N[] myWorkerColumn;

    @SuppressWarnings("unused")
    private GenericStore(final GenericStore.Factory<N> factory, final int numbRows) {
        this(factory, numbRows, 1);
    }

    @SuppressWarnings("unused")
    private GenericStore(final GenericStore.Factory<N> factory, final N[] dataArray) {
        this(factory, dataArray.length, 1, dataArray);
    }

    GenericStore(final GenericStore.Factory<N> factory, final int numbRows, final int numbCols) {

        super(factory.array(), numbRows * numbCols);

        myFactory = factory;

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newGeneric(myRowDim, myColDim);
    }

    GenericStore(final GenericStore.Factory<N> factory, final int numbRows, final int numbCols, final N[] dataArray) {

        super(factory.array(), dataArray);

        myFactory = factory;

        myRowDim = numbRows;
        myColDim = numbCols;

        myUtility = this.wrapInArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.newGeneric(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.newGeneric(myRowDim, myColDim);
        multiplyRight = MultiplyRight.newGeneric(myRowDim, myColDim);
        multiplyNeither = MultiplyNeither.newGeneric(myRowDim, myColDim);
    }

    @Override
    public void accept(final Access2D<?> supplied) {
        for (long j = 0L; j < supplied.countColumns(); j++) {
            for (long i = 0L; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.get(i, j));
            }
        }
    }

    @Override
    public void add(final long row, final long col, final Comparable<?> addend) {
        myUtility.add(row, col, addend);
    }

    @Override
    public void add(final long row, final long col, final double addend) {
        myUtility.add(row, col, addend);
    }

    @Override
    public void applyCholesky(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ScalarArray<N>) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyCholesky.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ApplyCholesky.invoke(tmpData, myRowDim, aFirst, aLimit, tmpColumn);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyCholesky.THRESHOLD);

        } else {

            ApplyCholesky.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn);
        }
    }

    @Override
    public void applyLDL(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ScalarArray<N>) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyLDL.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    ApplyLDL.invoke(tmpData, myRowDim, first, limit, tmpColumn, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLDL.THRESHOLD);

        } else {

            ApplyLDL.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn, iterationPoint);
        }
    }

    @Override
    public void applyLU(final int iterationPoint, final BasicArray<N> multipliers) {

        final N[] tmpData = data;
        final N[] tmpColumn = ((ScalarArray<N>) multipliers).data;

        if (myColDim - iterationPoint - 1 > ApplyLU.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    ApplyLU.invoke(tmpData, myRowDim, aFirst, aLimit, tmpColumn, iterationPoint);
                }
            };

            tmpConquerer.invoke(iterationPoint + 1, myColDim, ApplyLU.THRESHOLD);

        } else {

            ApplyLU.invoke(tmpData, myRowDim, iterationPoint + 1, myColDim, tmpColumn, iterationPoint);
        }
    }

    @Override
    public Array1D<N> asList() {
        return myUtility.flatten();
    }

    @Override
    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<N> transformationCollector, final boolean eigenvalue) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
        return null;
    }

    @Override
    public MatrixStore<N> conjugate() {
        return new ConjugatedStore<>(this);
    }

    @Override
    public GenericStore<N> copy() {
        return new GenericStore<>(myFactory, myRowDim, myColDim, this.copyOfData());
    }

    @Override
    public long countColumns() {
        return myColDim;
    }

    @Override
    public long countRows() {
        return myRowDim;
    }

    @Override
    public void divideAndCopyColumn(final int row, final int column, final BasicArray<N> destination) {

        N[] destinationData = ((ScalarArray<N>) destination).data;

        int index = row + column * myRowDim;
        N denominator = data[index];

        for (int i = row + 1; i < myRowDim; i++) {
            index++;
            destinationData[i] = data[index] = data[index].divide(denominator).get();
        }
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return this.doubleValue(row + col * myRowDim);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof GenericStore)) {
            return false;
        }
        GenericStore other = (GenericStore) obj;
        if (myColDim != other.myColDim) {
            return false;
        }
        if (myFactory == null) {
            if (other.myFactory != null) {
                return false;
            }
        } else if (!myFactory.equals(other.myFactory)) {
            return false;
        }
        if (myRowDim != other.myRowDim) {
            return false;
        }
        return true;
    }

    @Override
    public void exchangeColumns(final long colA, final long colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    @Override
    public void exchangeHermitian(final int indexA, final int indexB) {

        final int tmpMin = Math.min(indexA, indexB);
        final int tmpMax = Math.max(indexA, indexB);

        N tmpVal;
        for (int j = 0; j < tmpMin; j++) {
            tmpVal = this.get(tmpMin, j);
            this.set(tmpMin, j, this.get(tmpMax, j));
            this.set(tmpMax, j, tmpVal);
        }

        tmpVal = this.get(tmpMin, tmpMin);
        this.set(tmpMin, tmpMin, this.get(tmpMax, tmpMax));
        this.set(tmpMax, tmpMax, tmpVal);

        for (int ij = tmpMin + 1; ij < tmpMax; ij++) {
            tmpVal = this.get(ij, tmpMin);
            this.set(ij, tmpMin, this.get(tmpMax, ij).conjugate().get());
            this.set(tmpMax, ij, tmpVal.conjugate().get());
        }

        for (int i = tmpMax + 1; i < myRowDim; i++) {
            tmpVal = this.get(i, tmpMin);
            this.set(i, tmpMin, this.get(i, tmpMax));
            this.set(i, tmpMax, tmpVal);
        }
    }

    @Override
    public void exchangeRows(final long rowA, final long rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    @Override
    public void fillByMultiplying(final Access1D<N> left, final Access1D<N> right) {

        final int complexity = Math.toIntExact(left.count() / this.countRows());
        if (complexity != Math.toIntExact(right.count() / this.countColumns())) {
            ProgrammingError.throwForMultiplicationNotPossible();
        }

        if (left instanceof GenericStore) {
            if (right instanceof GenericStore) {
                multiplyNeither.invoke(data, this.cast(left).data, complexity, this.cast(right).data, myFactory.scalar());
            } else {
                multiplyRight.invoke(data, this.cast(left).data, complexity, right, myFactory.scalar());
            }
        } else if (right instanceof GenericStore) {
            multiplyLeft.invoke(data, left, complexity, this.cast(right).data, myFactory.scalar());
        } else {
            multiplyBoth.invoke(this, left, complexity, right);
        }
    }

    @Override
    public void fillColumn(final long row, final long col, final Access1D<N> values) {
        myUtility.fillColumn(row, col, values);
    }

    @Override
    public void fillColumn(final long row, final long col, final N value) {
        myUtility.fillColumn(row, col, value);
    }

    @Override
    public void fillColumn(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillColumn(row, col, supplier);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final N value) {
        myUtility.fillDiagonal(row, col, value);
    }

    @Override
    public void fillDiagonal(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillDiagonal(row, col, supplier);
    }

    @Override
    public void fillMatching(final Access1D<?> values) {

        if (values instanceof ConjugatedStore) {
            final TransjugatedStore<?> conjugated = (ConjugatedStore<?>) values;

            if (myColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int first, final int limit) {
                        FillMatchingSingle.conjugate(data, myRowDim, first, limit, conjugated.getOriginal(), myFactory.scalar());
                    }

                };

                tmpConquerer.invoke(0, myColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.conjugate(data, myRowDim, 0, myColDim, conjugated.getOriginal(), myFactory.scalar());
            }

        } else if (values instanceof TransposedStore) {
            final TransjugatedStore<?> transposed = (TransposedStore<?>) values;

            if (myColDim > FillMatchingSingle.THRESHOLD) {

                final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                    @Override
                    public void conquer(final int first, final int limit) {
                        FillMatchingSingle.transpose(data, myRowDim, first, limit, transposed.getOriginal(), myFactory.scalar());
                    }

                };

                tmpConquerer.invoke(0, myColDim, FillMatchingSingle.THRESHOLD);

            } else {

                FillMatchingSingle.transpose(data, myRowDim, 0, myColDim, transposed.getOriginal(), myFactory.scalar());
            }

        } else {

            super.fillMatching(values);
        }
    }

    @Override
    public void fillMatching(final Access1D<N> left, final BinaryFunction<N> function, final Access1D<N> right) {

        int matchingCount = MissingMath.toMinIntExact(this.count(), left.count(), right.count());

        if (myColDim > FillMatchingDual.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    OperationBinary.invoke(data, first, limit, 1, left, function, right);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingDual.THRESHOLD * FillMatchingDual.THRESHOLD);

        } else {

            OperationBinary.invoke(data, 0, matchingCount, 1, left, function, right);
        }
    }

    @Override
    public void fillMatching(final UnaryFunction<N> function, final Access1D<N> arguments) {

        int matchingCount = MissingMath.toMinIntExact(this.count(), arguments.count());

        if (myColDim > FillMatchingSingle.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int first, final int limit) {
                    OperationUnary.invoke(data, first, limit, 1, arguments, function);
                }

            };

            tmpConquerer.invoke(0, matchingCount, FillMatchingSingle.THRESHOLD * FillMatchingSingle.THRESHOLD);

        } else {

            OperationUnary.invoke(data, 0, matchingCount, 1, arguments, function);
        }
    }

    @Override
    public void fillOne(final long row, final long col, final Access1D<?> values, final long valueIndex) {
        this.set(row, col, values.get(valueIndex));
    }

    @Override
    public void fillOne(final long row, final long col, final N value) {
        myUtility.fillOne(row, col, value);
    }

    @Override
    public void fillOne(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillOne(row, col, supplier);
    }

    @Override
    public void fillRow(final long row, final long col, final Access1D<N> values) {
        myUtility.fillRow(row, col, values);
    }

    @Override
    public void fillRow(final long row, final long col, final N value) {
        myUtility.fillRow(row, col, value);
    }

    @Override
    public void fillRow(final long row, final long col, final NullaryFunction<?> supplier) {
        myUtility.fillRow(row, col, supplier);
    }

    @Override
    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Generic<N>) destination, myFactory.scalar());
    }

    @Override
    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<N> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Generic<N>) destination, myFactory.scalar());
    }

    @Override
    public MatrixStore<N> get() {
        return this;
    }

    @Override
    public N get(final int row, final int col) {
        return myUtility.get(row, col);
    }

    @Override
    public int getColDim() {
        return myColDim;
    }

    @Override
    public int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    @Override
    public int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    @Override
    public int getRowDim() {
        return myRowDim;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + myColDim;
        result = prime * result + (myFactory == null ? 0 : myFactory.hashCode());
        return prime * result + myRowDim;
    }

    @Override
    public void modifyAll(final UnaryFunction<N> modifier) {

        final int numberOfRows = myRowDim;
        final int numberOfCols = myColDim;

        if (numberOfCols > ModifyAll.THRESHOLD) {

            final DivideAndConquer conquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    GenericStore.this.modify(numberOfRows * aFirst, numberOfRows * aLimit, 1, modifier);
                }

            };

            conquerer.invoke(0, numberOfCols, ModifyAll.THRESHOLD);

        } else {

            this.modify(0, numberOfRows * numberOfCols, 1, modifier);
        }
    }

    @Override
    public void modifyColumn(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyColumn(row, col, modifier);
    }

    @Override
    public void modifyDiagonal(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyDiagonal(row, col, modifier);
    }

    @Override
    public void modifyOne(final long row, final long col, final UnaryFunction<N> modifier) {

        N tmpValue = this.get(row, col);

        tmpValue = modifier.invoke(tmpValue);

        this.set(row, col, tmpValue);
    }

    @Override
    public void modifyRow(final long row, final long col, final UnaryFunction<N> modifier) {
        myUtility.modifyRow(row, col, modifier);
    }

    @Override
    public MatrixStore<N> multiply(final MatrixStore<N> right) {

        final GenericStore<N> retVal = this.physical().make(myRowDim, right.count() / myColDim);

        if (right instanceof GenericStore) {
            retVal.multiplyNeither.invoke(retVal.data, data, myColDim, this.cast(right).data, myFactory.scalar());
        } else {
            retVal.multiplyRight.invoke(retVal.data, data, myColDim, right, myFactory.scalar());
        }

        return retVal;
    }

    @Override
    public N multiplyBoth(final Access1D<N> leftAndRight) {

        final PhysicalStore<N> tmpStep1 = myFactory.make(1L, leftAndRight.count());
        final PhysicalStore<N> tmpStep2 = myFactory.make(1L, 1L);

        final PhysicalStore<N> tmpLeft = myFactory.rows(leftAndRight);
        tmpLeft.modifyAll(myFactory.function().conjugate());
        tmpStep1.fillByMultiplying(tmpLeft, this);

        tmpStep2.fillByMultiplying(tmpStep1, leftAndRight);

        return tmpStep2.get(0L);
    }

    @Override
    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, myFactory.function().negate());
    }

    @Override
    public PhysicalStore.Factory<N, GenericStore<N>> physical() {
        return myFactory;
    }

    @Override
    public TransformableRegion<N> regionByColumns(final int... columns) {
        return new Subregion2D.ColumnsRegion<>(this, multiplyBoth, columns);
    }

    @Override
    public TransformableRegion<N> regionByLimits(final int rowLimit, final int columnLimit) {
        return new Subregion2D.LimitRegion<>(this, multiplyBoth, rowLimit, columnLimit);
    }

    @Override
    public TransformableRegion<N> regionByOffsets(final int rowOffset, final int columnOffset) {
        return new Subregion2D.OffsetRegion<>(this, multiplyBoth, rowOffset, columnOffset);
    }

    @Override
    public TransformableRegion<N> regionByRows(final int... rows) {
        return new Subregion2D.RowsRegion<>(this, multiplyBoth, rows);
    }

    @Override
    public TransformableRegion<N> regionByTransposing() {
        return new Subregion2D.TransposedRegion<>(this, multiplyBoth);
    }

    @Override
    public void rotateRight(final int low, final int high, final double cos, final double sin) {
        RotateRight.invoke(data, myRowDim, low, high, myFactory.scalar().cast(cos), myFactory.scalar().cast(sin));
    }

    @Override
    public void set(final int row, final int col, final double value) {
        myUtility.set(row, col, value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        myUtility.set(row, col, value);
    }

    @Override
    public void setToIdentity(final int col) {
        myUtility.set(col, col, myFactory.scalar().one().get());
        myUtility.fillColumn(col + 1, col, myFactory.scalar().zero().get());
    }

    @Override
    public Array1D<N> sliceColumn(final long row, final long col) {
        return myUtility.sliceColumn(row, col);
    }

    @Override
    public Array1D<N> sliceDiagonal(final long row, final long col) {
        return myUtility.sliceDiagonal(row, col);
    }

    @Override
    public Array1D<N> sliceRange(final long first, final long limit) {
        return myUtility.sliceRange(first, limit);
    }

    @Override
    public Array1D<N> sliceRow(final long row, final long col) {
        return myUtility.sliceRow(row, col);
    }

    @Override
    public void substituteBackwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean hermitian) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteBackwards.invoke(GenericStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, hermitian,
                            myFactory.scalar());
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, hermitian, myFactory.scalar());
        }
    }

    @Override
    public void substituteForwards(final Access2D<N> body, final boolean unitDiagonal, final boolean conjugated, final boolean identity) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteForwards.invoke(GenericStore.this.data, tmpRowDim, aFirst, aLimit, body, unitDiagonal, conjugated, identity, myFactory.scalar());
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, unitDiagonal, conjugated, identity, myFactory.scalar());
        }
    }

    @Override
    public Scalar<N> toScalar(final long row, final long column) {
        return myUtility.get(row, column);
    }

    @Override
    public String toString() {
        return Access2D.toString(this);
    }

    @Override
    public void transformLeft(final Householder<N> transformation, final int firstColumn) {
        HouseholderLeft.call(data, myRowDim, firstColumn, this.cast(transformation), myFactory.scalar());
    }

    @Override
    public void transformLeft(final Rotation<N> transformation) {

        final Rotation.Generic<N> tmpTransf = this.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (tmpTransf.cos != null && tmpTransf.sin != null) {
                RotateLeft.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeRows(tmpLow, tmpHigh);
            }
        } else if (tmpTransf.cos != null) {
            myUtility.modifyRow(tmpLow, 0, myFactory.function().multiply().second(tmpTransf.cos));
        } else if (tmpTransf.sin != null) {
            myUtility.modifyRow(tmpLow, 0, myFactory.function().divide().second(tmpTransf.sin));
        } else {
            myUtility.modifyRow(tmpLow, 0, myFactory.function().negate());
        }
    }

    @Override
    public void transformRight(final Householder<N> transformation, final int firstRow) {
        HouseholderRight.call(data, myRowDim, firstRow, this.cast(transformation), myFactory.scalar());
    }

    @Override
    public void transformRight(final Rotation<N> transformation) {

        final Rotation.Generic<N> tmpTransf = this.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (tmpTransf.cos != null && tmpTransf.sin != null) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeColumns(tmpLow, tmpHigh);
            }
        } else if (tmpTransf.cos != null) {
            myUtility.modifyColumn(0, tmpHigh, myFactory.function().multiply().second(tmpTransf.cos));
        } else if (tmpTransf.sin != null) {
            myUtility.modifyColumn(0, tmpHigh, myFactory.function().divide().second(tmpTransf.sin));
        } else {
            myUtility.modifyColumn(0, tmpHigh, myFactory.function().negate());
        }
    }

    @Override
    public void transformSymmetric(final Householder<N> transformation) {
        HouseholderHermitian.invoke(data, this.cast(transformation), this.getWorkerColumn(), myFactory.scalar());
    }

    @Override
    public MatrixStore<N> transpose() {
        return new TransposedStore<>(this);
    }

    @Override
    public void tred2(final BasicArray<N> mainDiagonal, final BasicArray<N> offDiagonal, final boolean yesvecs) {
        ProgrammingError.throwForUnsupportedOptionalOperation();
    }

    @Override
    public void visitColumn(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitColumn(row, col, visitor);
    }

    @Override
    public void visitDiagonal(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitDiagonal(row, col, visitor);
    }

    @Override
    public void visitRow(final long row, final long col, final VoidFunction<N> visitor) {
        myUtility.visitRow(row, col, visitor);
    }

    private GenericStore<N> cast(final Access1D<?> matrix) {
        if (matrix instanceof GenericStore) {
            return (GenericStore<N>) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return myFactory.copy((Access2D<?>) matrix);
        } else {
            return myFactory.columns(matrix);
        }
    }

    private Householder.Generic<N> cast(final Householder<N> transformation) {
        if (transformation instanceof Householder.Generic) {
            return (Householder.Generic<N>) transformation;
        }
        if (transformation instanceof HouseholderReference<?>) {
            return ((Householder.Generic<N>) ((HouseholderReference<N>) transformation).getWorker(myFactory)).copy(transformation);
        }
        return new Householder.Generic<>(myFactory.scalar(), transformation);
    }

    private Rotation.Generic<N> cast(final Rotation<N> transformation) {
        if (transformation instanceof Rotation.Generic) {
            return (Rotation.Generic<N>) transformation;
        }
        return new Rotation.Generic<>(transformation);
    }

    private N[] getWorkerColumn() {

        if (myWorkerColumn == null) {
            myWorkerColumn = myFactory.scalar().newArrayInstance(myRowDim);
        }

        Arrays.fill(myWorkerColumn, myFactory.scalar().zero().get());

        return myWorkerColumn;
    }

}
