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
package org.ojalgo.matrix.store;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import java.util.List;

import org.ojalgo.access.Access1D;
import org.ojalgo.access.Access2D;
import org.ojalgo.access.AccessUtils;
import org.ojalgo.array.Array1D;
import org.ojalgo.array.Array2D;
import org.ojalgo.array.BasicArray;
import org.ojalgo.array.ComplexArray;
import org.ojalgo.array.PrimitiveArray;
import org.ojalgo.concurrent.DivideAndConquer;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.FunctionSet;
import org.ojalgo.function.NullaryFunction;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.VoidFunction;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.function.aggregator.AggregatorCollection;
import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.function.aggregator.PrimitiveAggregator;
import org.ojalgo.machine.JavaType;
import org.ojalgo.machine.MemoryEstimator;
import org.ojalgo.matrix.MatrixUtils;
import org.ojalgo.matrix.decomposition.DecompositionStore;
import org.ojalgo.matrix.decomposition.LUDecomposition.Pivot;
import org.ojalgo.matrix.store.operation.*;
import org.ojalgo.matrix.transformation.Householder;
import org.ojalgo.matrix.transformation.Rotation;
import org.ojalgo.random.RandomNumber;
import org.ojalgo.scalar.ComplexNumber;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;
import org.ojalgo.type.context.NumberContext;

/**
 * A {@linkplain Double} (actually double) implementation of {@linkplain PhysicalStore}.
 *
 * @author apete
 */
public final class PrimitiveDenseStore extends PrimitiveArray implements PhysicalStore<Double>, DecompositionStore<Double> {

    public static interface PrimitiveMultiplyBoth {

        void invoke(double[] product, Access1D<?> left, int complexity, Access1D<?> right);

    }

    public static interface PrimitiveMultiplyLeft {

        void invoke(double[] product, Access1D<?> left, int complexity, double[] right);

    }

    public static interface PrimitiveMultiplyRight {

        void invoke(double[] product, double[] left, int complexity, Access1D<?> right);

    }

    public static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = new DecompositionStore.Factory<Double, PrimitiveDenseStore>() {

        public AggregatorCollection<Double> aggregator() {
            return PrimitiveAggregator.getCollection();
        }

        public PrimitiveDenseStore columns(final Access1D<?>... source) {

            final int tmpRowDim = (int) source[0].count();
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Access1D<?> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn.doubleValue(i);
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final double[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            double[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn[i];
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final List<? extends Number>... source) {

            final int tmpRowDim = source[0].size();
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Number> tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn.get(i).doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore columns(final Number[]... source) {

            final int tmpRowDim = source[0].length;
            final int tmpColDim = source.length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Number[] tmpColumn;
            for (int j = 0; j < tmpColDim; j++) {
                tmpColumn = source[j];
                for (int i = 0; i < tmpRowDim; i++) {
                    tmpData[i + (tmpRowDim * j)] = tmpColumn[i].doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore conjugate(final Access2D<?> source) {
            return this.transpose(source);
        }

        public PrimitiveDenseStore copy(final Access2D<?> source) {

            final PrimitiveDenseStore retVal = new PrimitiveDenseStore((int) source.countRows(), (int) source.countColumns());

            retVal.fillMatching(source);

            return retVal;
        }

        public FunctionSet<Double> function() {
            return PrimitiveFunction.getSet();
        }

        public PrimitiveArray makeArray(final int length) {
            return PrimitiveArray.make(length);
        }

        public PrimitiveDenseStore makeEye(final long rows, final long columns) {

            final PrimitiveDenseStore retVal = this.makeZero(rows, columns);

            retVal.myUtility.fillDiagonal(0, 0, PrimitiveMath.ONE);

            return retVal;
        }

        public Householder.Primitive makeHouseholder(final int length) {
            return new Householder.Primitive(length);
        }

        public PrimitiveDenseStore makeRandom(final long rows, final long columns, final RandomNumber distribution) {

            final int tmpRowDim = (int) rows;
            final int tmpColDim = (int) columns;

            final int tmpLength = tmpRowDim * tmpColDim;

            final double[] tmpData = new double[tmpLength];

            for (int i = 0; i < tmpLength; i++) {
                tmpData[i] = distribution.doubleValue();
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Rotation.Primitive makeRotation(final int low, final int high, final double cos, final double sin) {
            return new Rotation.Primitive(low, high, cos, sin);
        }

        public Rotation.Primitive makeRotation(final int low, final int high, final Double cos, final Double sin) {
            return this.makeRotation(low, high, cos != null ? cos.doubleValue() : Double.NaN, sin != null ? sin.doubleValue() : Double.NaN);
        }

        public PrimitiveDenseStore makeZero(final long rows, final long columns) {
            return new PrimitiveDenseStore((int) rows, (int) columns);
        }

        public PrimitiveDenseStore rows(final Access1D<?>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = (int) source[0].count();

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Access1D<?> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow.doubleValue(j);
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final double[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            double[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow[j];
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final List<? extends Number>... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].size();

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            List<? extends Number> tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow.get(j).doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public PrimitiveDenseStore rows(final Number[]... source) {

            final int tmpRowDim = source.length;
            final int tmpColDim = source[0].length;

            final double[] tmpData = new double[tmpRowDim * tmpColDim];

            Number[] tmpRow;
            for (int i = 0; i < tmpRowDim; i++) {
                tmpRow = source[i];
                for (int j = 0; j < tmpColDim; j++) {
                    tmpData[i + (tmpRowDim * j)] = tmpRow[j].doubleValue();
                }
            }

            return new PrimitiveDenseStore(tmpRowDim, tmpColDim, tmpData);
        }

        public Scalar.Factory<Double> scalar() {
            return PrimitiveScalar.FACTORY;
        }

        public PrimitiveDenseStore transpose(final Access2D<?> source) {

            final PrimitiveDenseStore retVal = new PrimitiveDenseStore((int) source.countColumns(), (int) source.countRows());

            retVal.fillTransposed(source);

            return retVal;
        }
    };

    static final long ELEMENT_SIZE = JavaType.DOUBLE.memory();

    static final long SHALLOW_SIZE = MemoryEstimator.estimateObject(PrimitiveDenseStore.class);

    static PrimitiveDenseStore cast(final Access1D<Double> matrix) {
        if (matrix instanceof PrimitiveDenseStore) {
            return (PrimitiveDenseStore) matrix;
        } else if (matrix instanceof Access2D<?>) {
            return FACTORY.copy((Access2D<?>) matrix);
        } else {
            return FACTORY.columns(matrix);
        }
    }

    static Householder.Primitive cast(final Householder<Double> transformation) {
        if (transformation instanceof Householder.Primitive) {
            return (Householder.Primitive) transformation;
        } else if (transformation instanceof DecompositionStore.HouseholderReference<?>) {
            return ((DecompositionStore.HouseholderReference<Double>) transformation).getPrimitiveWorker().copy(transformation);
        } else {
            return new Householder.Primitive(transformation);
        }
    }

    static Rotation.Primitive cast(final Rotation<Double> transformation) {
        if (transformation instanceof Rotation.Primitive) {
            return (Rotation.Primitive) transformation;
        } else {
            return new Rotation.Primitive(transformation);
        }
    }

    static void doAfter(final double[] aMtrxH, final double[] aMtrxV, final double[] tmpMainDiagonal, final double[] tmpOffDiagonal, double r, double s,
            double z, final double aNorm1) {

        final int tmpDiagDim = (int) Math.sqrt(aMtrxH.length);
        final int tmpDiagDimMinusOne = tmpDiagDim - 1;

        //        BasicLogger.logDebug("r={}, s={}, z={}", r, s, z);

        double p;
        double q;
        double t;
        double w;
        double x;
        double y;

        for (int ij = tmpDiagDimMinusOne; ij >= 0; ij--) {

            p = tmpMainDiagonal[ij];
            q = tmpOffDiagonal[ij];

            // Real vector
            if (q == 0) {
                int l = ij;
                aMtrxH[ij + (tmpDiagDim * ij)] = 1.0;
                for (int i = ij - 1; i >= 0; i--) {
                    w = aMtrxH[i + (tmpDiagDim * i)] - p;
                    r = PrimitiveMath.ZERO;
                    for (int j = l; j <= ij; j++) {
                        r = r + (aMtrxH[i + (tmpDiagDim * j)] * aMtrxH[j + (tmpDiagDim * ij)]);
                    }
                    if (tmpOffDiagonal[i] < PrimitiveMath.ZERO) {
                        z = w;
                        s = r;
                    } else {
                        l = i;
                        if (tmpOffDiagonal[i] == PrimitiveMath.ZERO) {
                            if (w != PrimitiveMath.ZERO) {
                                aMtrxH[i + (tmpDiagDim * ij)] = -r / w;
                            } else {
                                aMtrxH[i + (tmpDiagDim * ij)] = -r / (PrimitiveMath.MACHINE_EPSILON * aNorm1);
                            }

                            // Solve real equations
                        } else {
                            x = aMtrxH[i + (tmpDiagDim * (i + 1))];
                            y = aMtrxH[(i + 1) + (tmpDiagDim * i)];
                            q = ((tmpMainDiagonal[i] - p) * (tmpMainDiagonal[i] - p)) + (tmpOffDiagonal[i] * tmpOffDiagonal[i]);
                            t = ((x * s) - (z * r)) / q;
                            aMtrxH[i + (tmpDiagDim * ij)] = t;
                            if (Math.abs(x) > Math.abs(z)) {
                                aMtrxH[(i + 1) + (tmpDiagDim * ij)] = (-r - (w * t)) / x;
                            } else {
                                aMtrxH[(i + 1) + (tmpDiagDim * ij)] = (-s - (y * t)) / z;
                            }
                        }

                        // Overflow control
                        t = Math.abs(aMtrxH[i + (tmpDiagDim * ij)]);
                        if (((PrimitiveMath.MACHINE_EPSILON * t) * t) > 1) {
                            for (int j = i; j <= ij; j++) {
                                aMtrxH[j + (tmpDiagDim * ij)] = aMtrxH[j + (tmpDiagDim * ij)] / t;
                            }
                        }
                    }
                }

                // Complex vector
            } else if (q < 0) {
                int l = ij - 1;

                // Last vector component imaginary so matrix is triangular
                if (Math.abs(aMtrxH[ij + (tmpDiagDim * (ij - 1))]) > Math.abs(aMtrxH[(ij - 1) + (tmpDiagDim * ij)])) {
                    aMtrxH[(ij - 1) + (tmpDiagDim * (ij - 1))] = q / aMtrxH[ij + (tmpDiagDim * (ij - 1))];
                    aMtrxH[(ij - 1) + (tmpDiagDim * ij)] = -(aMtrxH[ij + (tmpDiagDim * ij)] - p) / aMtrxH[ij + (tmpDiagDim * (ij - 1))];
                } else {

                    final ComplexNumber tmpX = new ComplexNumber(PrimitiveMath.ZERO, (-aMtrxH[(ij - 1) + (tmpDiagDim * ij)]));
                    final double imaginary = q;
                    final ComplexNumber tmpY = new ComplexNumber((aMtrxH[(ij - 1) + (tmpDiagDim * (ij - 1))] - p), imaginary);

                    final ComplexNumber tmpZ = tmpX.divide(tmpY);

                    aMtrxH[(ij - 1) + (tmpDiagDim * (ij - 1))] = tmpZ.doubleValue();
                    aMtrxH[(ij - 1) + (tmpDiagDim * ij)] = tmpZ.i;
                }
                aMtrxH[ij + (tmpDiagDim * (ij - 1))] = PrimitiveMath.ZERO;
                aMtrxH[ij + (tmpDiagDim * ij)] = 1.0;
                for (int i = ij - 2; i >= 0; i--) {
                    double ra, sa, vr, vi;
                    ra = PrimitiveMath.ZERO;
                    sa = PrimitiveMath.ZERO;
                    for (int j = l; j <= ij; j++) {
                        ra = ra + (aMtrxH[i + (tmpDiagDim * j)] * aMtrxH[j + (tmpDiagDim * (ij - 1))]);
                        sa = sa + (aMtrxH[i + (tmpDiagDim * j)] * aMtrxH[j + (tmpDiagDim * ij)]);
                    }
                    w = aMtrxH[i + (tmpDiagDim * i)] - p;

                    if (tmpOffDiagonal[i] < PrimitiveMath.ZERO) {
                        z = w;
                        r = ra;
                        s = sa;
                    } else {
                        l = i;
                        if (tmpOffDiagonal[i] == 0) {
                            final ComplexNumber tmpX = new ComplexNumber((-ra), (-sa));
                            final double real = w;
                            final double imaginary = q;
                            final ComplexNumber tmpY = new ComplexNumber(real, imaginary);

                            final ComplexNumber tmpZ = tmpX.divide(tmpY);

                            aMtrxH[i + (tmpDiagDim * (ij - 1))] = tmpZ.doubleValue();
                            aMtrxH[i + (tmpDiagDim * ij)] = tmpZ.i;
                        } else {

                            // Solve complex equations
                            x = aMtrxH[i + (tmpDiagDim * (i + 1))];
                            y = aMtrxH[(i + 1) + (tmpDiagDim * i)];
                            vr = (((tmpMainDiagonal[i] - p) * (tmpMainDiagonal[i] - p)) + (tmpOffDiagonal[i] * tmpOffDiagonal[i])) - (q * q);
                            vi = (tmpMainDiagonal[i] - p) * 2.0 * q;
                            if ((vr == PrimitiveMath.ZERO) & (vi == PrimitiveMath.ZERO)) {
                                vr = PrimitiveMath.MACHINE_EPSILON * aNorm1 * (Math.abs(w) + Math.abs(q) + Math.abs(x) + Math.abs(y) + Math.abs(z));
                            }

                            final ComplexNumber tmpX = new ComplexNumber((((x * r) - (z * ra)) + (q * sa)), ((x * s) - (z * sa) - (q * ra)));
                            final double real = vr;
                            final double imaginary = vi;
                            final ComplexNumber tmpY = new ComplexNumber(real, imaginary);

                            final ComplexNumber tmpZ = tmpX.divide(tmpY);

                            aMtrxH[i + (tmpDiagDim * (ij - 1))] = tmpZ.doubleValue();
                            aMtrxH[i + (tmpDiagDim * ij)] = tmpZ.i;

                            if (Math.abs(x) > (Math.abs(z) + Math.abs(q))) {
                                aMtrxH[(i + 1) + (tmpDiagDim * (ij - 1))] = ((-ra - (w * aMtrxH[i + (tmpDiagDim * (ij - 1))])) + (q * aMtrxH[i
                                        + (tmpDiagDim * ij)]))
                                        / x;
                                aMtrxH[(i + 1) + (tmpDiagDim * ij)] = (-sa - (w * aMtrxH[i + (tmpDiagDim * ij)]) - (q * aMtrxH[i + (tmpDiagDim * (ij - 1))]))
                                        / x;
                            } else {
                                final ComplexNumber tmpX1 = new ComplexNumber((-r - (y * aMtrxH[i + (tmpDiagDim * (ij - 1))])), (-s - (y * aMtrxH[i
                                        + (tmpDiagDim * ij)])));
                                final double real1 = z;
                                final double imaginary1 = q;
                                final ComplexNumber tmpY1 = new ComplexNumber(real1, imaginary1);

                                final ComplexNumber tmpZ1 = tmpX1.divide(tmpY1);

                                aMtrxH[(i + 1) + (tmpDiagDim * (ij - 1))] = tmpZ1.doubleValue();
                                aMtrxH[(i + 1) + (tmpDiagDim * ij)] = tmpZ1.i;
                            }
                        }

                        // Overflow control
                        t = Math.max(Math.abs(aMtrxH[i + (tmpDiagDim * (ij - 1))]), Math.abs(aMtrxH[i + (tmpDiagDim * ij)]));
                        if (((PrimitiveMath.MACHINE_EPSILON * t) * t) > 1) {
                            for (int j = i; j <= ij; j++) {
                                aMtrxH[j + (tmpDiagDim * (ij - 1))] = aMtrxH[j + (tmpDiagDim * (ij - 1))] / t;
                                aMtrxH[j + (tmpDiagDim * ij)] = aMtrxH[j + (tmpDiagDim * ij)] / t;
                            }
                        }
                    }
                }
            }
        }

        // Back transformation to get eigenvectors of original matrix
        for (int j = tmpDiagDimMinusOne; j >= 0; j--) {
            for (int i = 0; i <= tmpDiagDimMinusOne; i++) {
                z = PrimitiveMath.ZERO;
                for (int k = 0; k <= j; k++) {
                    z += aMtrxV[i + (tmpDiagDim * k)] * aMtrxH[k + (tmpDiagDim * j)];
                }
                aMtrxV[i + (tmpDiagDim * j)] = z;
            }
        }
    }

    static int doHessenberg(final double[] aMtrxH, final double[] aMtrxV) {

        final int tmpDiagDim = (int) Math.sqrt(aMtrxH.length);
        final int tmpDiagDimMinusTwo = tmpDiagDim - 2;

        final double[] tmpWorkCopy = new double[tmpDiagDim];

        for (int ij = 0; ij < tmpDiagDimMinusTwo; ij++) {

            // Scale column.
            double tmpColNorm1 = PrimitiveMath.ZERO;
            for (int i = ij + 1; i < tmpDiagDim; i++) {
                tmpColNorm1 += Math.abs(aMtrxH[i + (tmpDiagDim * ij)]);
            }

            if (tmpColNorm1 != PrimitiveMath.ZERO) {

                // Compute Householder transformation.
                double tmpInvBeta = PrimitiveMath.ZERO;
                for (int i = tmpDiagDim - 1; i >= (ij + 1); i--) {
                    tmpWorkCopy[i] = aMtrxH[i + (tmpDiagDim * ij)] / tmpColNorm1;
                    tmpInvBeta += tmpWorkCopy[i] * tmpWorkCopy[i];
                }
                double g = Math.sqrt(tmpInvBeta);
                if (tmpWorkCopy[ij + 1] > 0) {
                    g = -g;
                }
                tmpInvBeta = tmpInvBeta - (tmpWorkCopy[ij + 1] * g);
                tmpWorkCopy[ij + 1] = tmpWorkCopy[ij + 1] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)
                for (int j = ij + 1; j < tmpDiagDim; j++) {
                    double f = PrimitiveMath.ZERO;
                    for (int i = tmpDiagDim - 1; i >= (ij + 1); i--) {
                        f += tmpWorkCopy[i] * aMtrxH[i + (tmpDiagDim * j)];
                    }
                    f = f / tmpInvBeta;
                    for (int i = ij + 1; i <= (tmpDiagDim - 1); i++) {
                        aMtrxH[i + (tmpDiagDim * j)] -= f * tmpWorkCopy[i];
                    }
                }

                for (int i = 0; i < tmpDiagDim; i++) {
                    double f = PrimitiveMath.ZERO;
                    for (int j = tmpDiagDim - 1; j >= (ij + 1); j--) {
                        f += tmpWorkCopy[j] * aMtrxH[i + (tmpDiagDim * j)];
                    }
                    f = f / tmpInvBeta;
                    for (int j = ij + 1; j < tmpDiagDim; j++) {
                        aMtrxH[i + (tmpDiagDim * j)] -= f * tmpWorkCopy[j];
                    }
                }

                tmpWorkCopy[ij + 1] = tmpColNorm1 * tmpWorkCopy[ij + 1];
                aMtrxH[(ij + 1) + (tmpDiagDim * ij)] = tmpColNorm1 * g;
            }
        }

        //  BasicLogger.logDebug("Jama H", new PrimitiveDenseStore(tmpDiagDim, tmpDiagDim, aMtrxH));

        // Här borde Hessenberg vara klar
        // Nedan börjar uträkningen av Q

        // Accumulate transformations (Algol's ortran).
        for (int ij = tmpDiagDimMinusTwo; ij >= 1; ij--) {
            final int tmpIndex = ij + (tmpDiagDim * (ij - 1));
            if (aMtrxH[tmpIndex] != PrimitiveMath.ZERO) {
                for (int i = ij + 1; i <= (tmpDiagDim - 1); i++) {
                    tmpWorkCopy[i] = aMtrxH[i + (tmpDiagDim * (ij - 1))];
                }
                for (int j = ij; j <= (tmpDiagDim - 1); j++) {
                    double g = PrimitiveMath.ZERO;
                    for (int i = ij; i <= (tmpDiagDim - 1); i++) {
                        g += tmpWorkCopy[i] * aMtrxV[i + (tmpDiagDim * j)];
                    }
                    // Double division avoids possible underflow
                    g = (g / tmpWorkCopy[ij]) / aMtrxH[tmpIndex];
                    for (int i = ij; i <= (tmpDiagDim - 1); i++) {
                        aMtrxV[i + (tmpDiagDim * j)] += g * tmpWorkCopy[i];
                    }
                }
            } else {
                //                BasicLogger.logDebug("Iter V", new PrimitiveDenseStore(tmpDiagDim, tmpDiagDim, aMtrxV));
            }
        }

        //      BasicLogger.logDebug("Jama V", new PrimitiveDenseStore(tmpDiagDim, tmpDiagDim, aMtrxV));

        return tmpDiagDim;
    }

    static double[][] doSchur(final double[] aMtrxH, final double[] aMtrxV, final boolean allTheWay) {

        final int tmpDiagDim = (int) Math.sqrt(aMtrxH.length);
        final int tmpDiagDimMinusOne = tmpDiagDim - 1;

        // Store roots isolated by balanc and compute matrix norm
        double tmpVal = PrimitiveMath.ZERO;
        for (int j = 0; j < tmpDiagDim; j++) {
            for (int i = Math.min(j + 1, tmpDiagDim - 1); i >= 0; i--) {
                tmpVal += Math.abs(aMtrxH[i + (tmpDiagDim * j)]);
            }
        }
        final double tmpNorm1 = tmpVal;

        final double[] tmpMainDiagonal = new double[tmpDiagDim];
        final double[] tmpOffDiagonal = new double[tmpDiagDim];

        double exshift = PrimitiveMath.ZERO;
        double p = 0, q = 0, r = 0, s = 0, z = 0;

        double w, x, y;
        // Outer loop over eigenvalue index
        int tmpIterCount = 0;
        int tmpMainIterIndex = tmpDiagDimMinusOne;
        while (tmpMainIterIndex >= 0) {

            // Look for single small sub-diagonal element
            int l = tmpMainIterIndex;
            while (l > 0) {
                s = Math.abs(aMtrxH[(l - 1) + (tmpDiagDim * (l - 1))]) + Math.abs(aMtrxH[l + (tmpDiagDim * l)]);
                if (s == PrimitiveMath.ZERO) {
                    s = tmpNorm1;
                }
                if (Math.abs(aMtrxH[l + (tmpDiagDim * (l - 1))]) < (PrimitiveMath.MACHINE_EPSILON * s)) {
                    break;
                }
                l--;
            }

            // Check for convergence
            // One root found
            if (l == tmpMainIterIndex) {
                aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)] = aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)] + exshift;
                tmpMainDiagonal[tmpMainIterIndex] = aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)];
                tmpOffDiagonal[tmpMainIterIndex] = PrimitiveMath.ZERO;
                tmpMainIterIndex--;
                tmpIterCount = 0;

                // Two roots found
            } else if (l == (tmpMainIterIndex - 1)) {
                w = aMtrxH[tmpMainIterIndex + (tmpDiagDim * (tmpMainIterIndex - 1))] * aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * tmpMainIterIndex)];
                p = (aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * (tmpMainIterIndex - 1))] - aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)]) / 2.0;
                q = (p * p) + w;
                z = Math.sqrt(Math.abs(q));
                aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)] = aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)] + exshift;
                aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * (tmpMainIterIndex - 1))] = aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * (tmpMainIterIndex - 1))]
                        + exshift;
                x = aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)];

                // Real pair
                if (q >= 0) {
                    if (p >= 0) {
                        z = p + z;
                    } else {
                        z = p - z;
                    }
                    tmpMainDiagonal[tmpMainIterIndex - 1] = x + z;
                    tmpMainDiagonal[tmpMainIterIndex] = tmpMainDiagonal[tmpMainIterIndex - 1];
                    if (z != PrimitiveMath.ZERO) {
                        tmpMainDiagonal[tmpMainIterIndex] = x - (w / z);
                    }
                    tmpOffDiagonal[tmpMainIterIndex - 1] = PrimitiveMath.ZERO;
                    tmpOffDiagonal[tmpMainIterIndex] = PrimitiveMath.ZERO;
                    x = aMtrxH[tmpMainIterIndex + (tmpDiagDim * (tmpMainIterIndex - 1))];
                    s = Math.abs(x) + Math.abs(z);
                    p = x / s;
                    q = z / s;
                    r = Math.sqrt((p * p) + (q * q));
                    p = p / r;
                    q = q / r;

                    // Row modification
                    for (int j = tmpMainIterIndex - 1; j < tmpDiagDim; j++) {
                        z = aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * j)];
                        aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * j)] = (q * z) + (p * aMtrxH[tmpMainIterIndex + (tmpDiagDim * j)]);
                        aMtrxH[tmpMainIterIndex + (tmpDiagDim * j)] = (q * aMtrxH[tmpMainIterIndex + (tmpDiagDim * j)]) - (p * z);
                    }

                    // Column modification
                    for (int i = 0; i <= tmpMainIterIndex; i++) {
                        z = aMtrxH[i + (tmpDiagDim * (tmpMainIterIndex - 1))];
                        aMtrxH[i + (tmpDiagDim * (tmpMainIterIndex - 1))] = (q * z) + (p * aMtrxH[i + (tmpDiagDim * tmpMainIterIndex)]);
                        aMtrxH[i + (tmpDiagDim * tmpMainIterIndex)] = (q * aMtrxH[i + (tmpDiagDim * tmpMainIterIndex)]) - (p * z);
                    }

                    // Accumulate transformations
                    for (int i = 0; i <= tmpDiagDimMinusOne; i++) {
                        z = aMtrxV[i + (tmpDiagDim * (tmpMainIterIndex - 1))];
                        aMtrxV[i + (tmpDiagDim * (tmpMainIterIndex - 1))] = (q * z) + (p * aMtrxV[i + (tmpDiagDim * tmpMainIterIndex)]);
                        aMtrxV[i + (tmpDiagDim * tmpMainIterIndex)] = (q * aMtrxV[i + (tmpDiagDim * tmpMainIterIndex)]) - (p * z);
                    }

                    // Complex pair
                } else {
                    tmpMainDiagonal[tmpMainIterIndex - 1] = x + p;
                    tmpMainDiagonal[tmpMainIterIndex] = x + p;
                    tmpOffDiagonal[tmpMainIterIndex - 1] = z;
                    tmpOffDiagonal[tmpMainIterIndex] = -z;
                }
                tmpMainIterIndex = tmpMainIterIndex - 2;
                tmpIterCount = 0;

                // No convergence yet
            } else {

                // Form shift
                x = aMtrxH[tmpMainIterIndex + (tmpDiagDim * tmpMainIterIndex)];
                y = PrimitiveMath.ZERO;
                w = PrimitiveMath.ZERO;
                if (l < tmpMainIterIndex) {
                    y = aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * (tmpMainIterIndex - 1))];
                    w = aMtrxH[tmpMainIterIndex + (tmpDiagDim * (tmpMainIterIndex - 1))] * aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * tmpMainIterIndex)];
                }

                // Wilkinson's original ad hoc shift
                if (tmpIterCount == 10) {
                    exshift += x;
                    for (int i = 0; i <= tmpMainIterIndex; i++) {
                        aMtrxH[i + (tmpDiagDim * i)] -= x;
                    }
                    s = Math.abs(aMtrxH[tmpMainIterIndex + (tmpDiagDim * (tmpMainIterIndex - 1))])
                            + Math.abs(aMtrxH[(tmpMainIterIndex - 1) + (tmpDiagDim * (tmpMainIterIndex - 2))]);
                    x = y = 0.75 * s;
                    w = -0.4375 * s * s;
                }

                // MATLAB's new ad hoc shift
                if (tmpIterCount == 30) {
                    s = (y - x) / 2.0;
                    s = (s * s) + w;
                    if (s > 0) {
                        s = Math.sqrt(s);
                        if (y < x) {
                            s = -s;
                        }
                        s = x - (w / (((y - x) / 2.0) + s));
                        for (int i = 0; i <= tmpMainIterIndex; i++) {
                            aMtrxH[i + (tmpDiagDim * i)] -= s;
                        }
                        exshift += s;
                        x = y = w = 0.964;
                    }
                }

                tmpIterCount++; // (Could check iteration count here.)

                // Look for two consecutive small sub-diagonal elements
                int m = tmpMainIterIndex - 2;
                while (m >= l) {
                    z = aMtrxH[m + (tmpDiagDim * m)];
                    r = x - z;
                    s = y - z;
                    p = (((r * s) - w) / aMtrxH[(m + 1) + (tmpDiagDim * m)]) + aMtrxH[m + (tmpDiagDim * (m + 1))];
                    q = aMtrxH[(m + 1) + (tmpDiagDim * (m + 1))] - z - r - s;
                    r = aMtrxH[(m + 2) + (tmpDiagDim * (m + 1))];
                    s = Math.abs(p) + Math.abs(q) + Math.abs(r);
                    p = p / s;
                    q = q / s;
                    r = r / s;
                    if (m == l) {
                        break;
                    }
                    if ((Math.abs(aMtrxH[m + (tmpDiagDim * (m - 1))]) * (Math.abs(q) + Math.abs(r))) < (PrimitiveMath.MACHINE_EPSILON * (Math.abs(p) * (Math
                            .abs(aMtrxH[(m - 1) + (tmpDiagDim * (m - 1))]) + Math.abs(z) + Math.abs(aMtrxH[(m + 1) + (tmpDiagDim * (m + 1))]))))) {
                        break;
                    }
                    m--;
                }

                for (int i = m + 2; i <= tmpMainIterIndex; i++) {
                    aMtrxH[i + (tmpDiagDim * (i - 2))] = PrimitiveMath.ZERO;
                    if (i > (m + 2)) {
                        aMtrxH[i + (tmpDiagDim * (i - 3))] = PrimitiveMath.ZERO;
                    }
                }

                // Double QR step involving rows l:n and columns m:n
                for (int k = m; k <= (tmpMainIterIndex - 1); k++) {
                    final boolean notlast = (k != (tmpMainIterIndex - 1));
                    if (k != m) {
                        p = aMtrxH[k + (tmpDiagDim * (k - 1))];
                        q = aMtrxH[(k + 1) + (tmpDiagDim * (k - 1))];
                        r = (notlast ? aMtrxH[(k + 2) + (tmpDiagDim * (k - 1))] : PrimitiveMath.ZERO);
                        x = Math.abs(p) + Math.abs(q) + Math.abs(r);
                        if (x == PrimitiveMath.ZERO) {
                            continue;
                        }
                        p = p / x;
                        q = q / x;
                        r = r / x;
                    }

                    s = Math.sqrt((p * p) + (q * q) + (r * r));
                    if (p < 0) {
                        s = -s;
                    }
                    if (s != 0) {
                        if (k != m) {
                            aMtrxH[k + (tmpDiagDim * (k - 1))] = -s * x;
                        } else if (l != m) {
                            aMtrxH[k + (tmpDiagDim * (k - 1))] = -aMtrxH[k + (tmpDiagDim * (k - 1))];
                        }
                        p = p + s;
                        x = p / s;
                        y = q / s;
                        z = r / s;
                        q = q / p;
                        r = r / p;

                        // Row modification
                        for (int j = k; j < tmpDiagDim; j++) {
                            p = aMtrxH[k + (tmpDiagDim * j)] + (q * aMtrxH[(k + 1) + (tmpDiagDim * j)]);
                            if (notlast) {
                                p = p + (r * aMtrxH[(k + 2) + (tmpDiagDim * j)]);
                                aMtrxH[(k + 2) + (tmpDiagDim * j)] = aMtrxH[(k + 2) + (tmpDiagDim * j)] - (p * z);
                            }
                            aMtrxH[k + (tmpDiagDim * j)] = aMtrxH[k + (tmpDiagDim * j)] - (p * x);
                            aMtrxH[(k + 1) + (tmpDiagDim * j)] = aMtrxH[(k + 1) + (tmpDiagDim * j)] - (p * y);
                        }

                        // Column modification
                        for (int i = 0; i <= Math.min(tmpMainIterIndex, k + 3); i++) {
                            p = (x * aMtrxH[i + (tmpDiagDim * k)]) + (y * aMtrxH[i + (tmpDiagDim * (k + 1))]);
                            if (notlast) {
                                p = p + (z * aMtrxH[i + (tmpDiagDim * (k + 2))]);
                                aMtrxH[i + (tmpDiagDim * (k + 2))] = aMtrxH[i + (tmpDiagDim * (k + 2))] - (p * r);
                            }
                            aMtrxH[i + (tmpDiagDim * k)] = aMtrxH[i + (tmpDiagDim * k)] - p;
                            aMtrxH[i + (tmpDiagDim * (k + 1))] = aMtrxH[i + (tmpDiagDim * (k + 1))] - (p * q);
                        }

                        // Accumulate transformations
                        for (int i = 0; i <= tmpDiagDimMinusOne; i++) {
                            p = (x * aMtrxV[i + (tmpDiagDim * k)]) + (y * aMtrxV[i + (tmpDiagDim * (k + 1))]);
                            if (notlast) {
                                p = p + (z * aMtrxV[i + (tmpDiagDim * (k + 2))]);
                                aMtrxV[i + (tmpDiagDim * (k + 2))] = aMtrxV[i + (tmpDiagDim * (k + 2))] - (p * r);
                            }
                            aMtrxV[i + (tmpDiagDim * k)] = aMtrxV[i + (tmpDiagDim * k)] - p;
                            aMtrxV[i + (tmpDiagDim * (k + 1))] = aMtrxV[i + (tmpDiagDim * (k + 1))] - (p * q);
                        }
                    } // (s != 0)
                } // k loop
            } // check convergence
        } // while (n >= low)

        // Backsubstitute to find vectors of upper triangular form
        if (allTheWay && (tmpNorm1 != PrimitiveMath.ZERO)) {
            PrimitiveDenseStore.doAfter(aMtrxH, aMtrxV, tmpMainDiagonal, tmpOffDiagonal, r, s, z, tmpNorm1);
        }

        return new double[][] { tmpMainDiagonal, tmpOffDiagonal };
    }

    private final PrimitiveMultiplyBoth multiplyBoth;
    private final PrimitiveMultiplyLeft multiplyLeft;
    private final PrimitiveMultiplyRight multiplyRight;
    private final int myColDim;
    private final int myRowDim;
    private final Array2D<Double> myUtility;

    PrimitiveDenseStore(final double[] anArray) {

        super(anArray);

        myRowDim = anArray.length;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
    }

    PrimitiveDenseStore(final int aLength) {

        super(aLength);

        myRowDim = aLength;
        myColDim = 1;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
    }

    PrimitiveDenseStore(final int aRowDim, final int aColDim) {

        super(aRowDim * aColDim);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
    }

    PrimitiveDenseStore(final int aRowDim, final int aColDim, final double[] anArray) {

        super(anArray);

        myRowDim = aRowDim;
        myColDim = aColDim;

        myUtility = this.asArray2D(myRowDim);

        multiplyBoth = MultiplyBoth.getPrimitive(myRowDim, myColDim);
        multiplyLeft = MultiplyLeft.getPrimitive(myRowDim, myColDim);
        multiplyRight = MultiplyRight.getPrimitive(myRowDim, myColDim);
    }

    public void accept(final Access2D<Double> supplied) {
        for (long j = 0; j < supplied.countColumns(); j++) {
            for (long i = 0; i < supplied.countRows(); i++) {
                this.set(i, j, supplied.doubleValue(i, j));
            }
        }
    }

    public MatrixStore<Double> add(final MatrixStore<Double> addend) {
        return new SuperimposedStore<>(this, addend);
    }

    public Double aggregateAll(final Aggregator aggregator) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        final AggregatorFunction<Double> tmpMainAggr = aggregator.getPrimitiveFunction();

        if (tmpColDim > AggregateAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {

                    final AggregatorFunction<Double> tmpPartAggr = aggregator.getPrimitiveFunction();

                    PrimitiveDenseStore.this.visit(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, tmpPartAggr);

                    synchronized (tmpMainAggr) {
                        tmpMainAggr.merge(tmpPartAggr.getNumber());
                    }
                }
            };

            tmpConquerer.invoke(0, tmpColDim, AggregateAll.THRESHOLD);

        } else {

            PrimitiveDenseStore.this.visit(0, this.size(), 1, tmpMainAggr);
        }

        return tmpMainAggr.getNumber();
    }

    public void applyCholesky(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((PrimitiveArray) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyCholesky.THRESHOLD) {

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

    public void applyLU(final int iterationPoint, final BasicArray<Double> multipliers) {

        final double[] tmpData = data;
        final double[] tmpColumn = ((PrimitiveArray) multipliers).data;

        if ((myColDim - iterationPoint - 1) > ApplyLU.THRESHOLD) {

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

    public Array2D<Double> asArray2D() {
        return myUtility;
    }

    public Array1D<Double> asList() {
        return myUtility.asArray1D();
    }

    public final MatrixStore.Builder<Double> builder() {
        return new MatrixStore.Builder<Double>(this);
    }

    public void caxpy(final double aSclrA, final int aColX, final int aColY, final int aFirstRow) {
        CAXPY.invoke(data, aColY * myRowDim, data, aColX * myRowDim, aSclrA, aFirstRow, myRowDim);
    }

    public void caxpy(final Double scalarA, final int columnX, final int columnY, final int firstRow) {
        CAXPY.invoke(data, columnY * myRowDim, data, columnX * myRowDim, scalarA.doubleValue(), firstRow, myRowDim);
    }

    public Pivot computeInPlaceLU() {

        final int tmpRowDim = this.getRowDim();
        final int tmpColDim = this.getColDim();
        final int tmpMinDim = Math.min(tmpRowDim, tmpColDim);

        final Pivot retVal = new Pivot(tmpRowDim);

        final double[] tmpData = data;
        final double[] tmpMultipliers = new double[tmpRowDim];

        // Main loop - along the diagonal
        for (int ij = 0; ij < tmpMinDim; ij++) {

            // Find next pivot row
            final int tmpPivotRow = (int) (this.isZero(ij, ij) ? myUtility.indexOfLargestInColumn(ij, ij) : ij);
            // Pivot?
            if (tmpPivotRow != ij) {
                myUtility.exchangeRows(tmpPivotRow, ij);
                retVal.change(tmpPivotRow, ij);
            }

            // Do the calculations...
            if (!this.isZero(ij, ij)) {

                // Calculate multipliers and copy to local column
                // Current column, below the diagonal

                int tmpIndex = ij + (ij * tmpRowDim);
                final double tmpDenominator = tmpData[tmpIndex];
                for (int i = ij + 1; i < tmpRowDim; i++) {
                    tmpMultipliers[i] = tmpData[++tmpIndex] /= tmpDenominator;
                }

                // Apply transformations to everything below and to the right of the pivot element
                //                ApplyLU.invoke(tmpData, tmpRowDim, ij + 1, tmpColDim, tmpMultipliers, ij);
                for (int j = ij + 1; j < tmpColDim; j++) {
                    SubtractScaledVector.invoke(tmpData, j * tmpRowDim, tmpMultipliers, 0, tmpData[ij + (j * tmpRowDim)], ij + 1, tmpRowDim);
                }

            } else {

                this.set(ij, ij, ZERO);
            }

        }

        return retVal;
    }

    public Array1D<ComplexNumber> computeInPlaceSchur(final PhysicalStore<Double> transformationCollector, final boolean eigenvalue) {

        //        final PrimitiveDenseStore tmpThisCopy = this.copy();
        //        final PrimitiveDenseStore tmpCollCopy = (PrimitiveDenseStore) aTransformationCollector.copy();
        //
        //        tmpThisCopy.computeInPlaceHessenberg(true);

        // Actual

        final double[] tmpData = data;

        final double[] tmpCollectorData = ((PrimitiveDenseStore) transformationCollector).data;

        PrimitiveDenseStore.doHessenberg(tmpData, tmpCollectorData);

        //        BasicLogger.logDebug("Schur Step", this);
        //        BasicLogger.logDebug("Hessenberg", tmpThisCopy);

        final double[][] tmpDiags = PrimitiveDenseStore.doSchur(tmpData, tmpCollectorData, eigenvalue);
        final double[] aRawReal = tmpDiags[0];
        final double[] aRawImag = tmpDiags[1];
        final int tmpLength = Math.min(aRawReal.length, aRawImag.length);

        final ComplexArray retVal = ComplexArray.make(tmpLength);
        final ComplexNumber[] tmpRaw = retVal.data;

        for (int i = 0; i < tmpLength; i++) {
            tmpRaw[i] = new ComplexNumber(aRawReal[i], aRawImag[i]);
        }

        return Array1D.COMPLEX.wrap(retVal);
    }

    public MatrixStore<Double> conjugate() {
        return this.transpose();
    }

    public PrimitiveDenseStore copy() {
        return new PrimitiveDenseStore(myRowDim, myColDim, this.copyOfData());
    }

    public long countColumns() {
        return myColDim;
    }

    public long countRows() {
        return myRowDim;
    }

    public void divideAndCopyColumn(final int row, final int column, final BasicArray<Double> destination) {

        final double[] tmpData = data;
        final int tmpRowDim = myRowDim;

        final double[] tmpDestination = ((PrimitiveArray) destination).data;

        int tmpIndex = row + (column * tmpRowDim);
        final double tmpDenominator = tmpData[tmpIndex];

        for (int i = row + 1; i < tmpRowDim; i++) {
            tmpDestination[i] = tmpData[++tmpIndex] /= tmpDenominator;
        }
    }

    public double doubleValue(final long aRow, final long aCol) {
        return myUtility.doubleValue(aRow, aCol);
    }

    public boolean equals(final MatrixStore<Double> other, final NumberContext context) {
        return AccessUtils.equals(this, other, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object anObj) {
        if (anObj instanceof MatrixStore) {
            return this.equals((MatrixStore<Double>) anObj, NumberContext.getGeneral(6));
        } else {
            return super.equals(anObj);
        }
    }

    public void exchangeColumns(final int colA, final int colB) {
        myUtility.exchangeColumns(colA, colB);
    }

    public void exchangeRows(final int rowA, final int rowB) {
        myUtility.exchangeRows(rowA, rowB);
    }

    public PhysicalStore.Factory<Double, PrimitiveDenseStore> factory() {
        return FACTORY;
    }

    public void fillByMultiplying(final Access1D<Double> left, final Access1D<Double> right) {

        final int tmpComplexity = ((int) left.count()) / myRowDim;

        final double[] tmpProductData = data;

        if (right instanceof PrimitiveDenseStore) {
            multiplyLeft.invoke(tmpProductData, left, tmpComplexity, PrimitiveDenseStore.cast(right).data);
        } else if (left instanceof PrimitiveDenseStore) {
            multiplyRight.invoke(tmpProductData, PrimitiveDenseStore.cast(left).data, tmpComplexity, right);
        } else {
            multiplyBoth.invoke(tmpProductData, left, tmpComplexity, right);
        }
    }

    public void fillColumn(final long row, final long column, final Double value) {
        myUtility.fillColumn(row, column, value);
    }

    public void fillColumn(final long row, final long column, final NullaryFunction<Double> supplier) {
        myUtility.fillColumn(row, column, supplier);
    }

    public void fillConjugated(final Access2D<? extends Number> source) {
        FillConjugated.invoke(data, myRowDim, 0, myColDim, source);
    }

    public void fillDiagonal(final long row, final long column, final Double value) {
        myUtility.fillDiagonal(row, column, value);
    }

    public void fillDiagonal(final long row, final long column, final NullaryFunction<Double> supplier) {
        myUtility.fillDiagonal(row, column, supplier);
    }

    public void fillMatching(final Access1D<? extends Number> source) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingSingle.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    FillMatchingSingle.invoke(PrimitiveDenseStore.this.data, tmpRowDim, aFirst, aLimit, source);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingSingle.THRESHOLD);

        } else {

            FillMatchingSingle.invoke(data, tmpRowDim, 0, tmpColDim, source);
        }
    }

    public void fillMatching(final Access1D<Double> leftArg, final BinaryFunction<Double> func, final Access1D<Double> rightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingBoth.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    PrimitiveDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, leftArg, func, rightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingBoth.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, leftArg, func, rightArg);
        }
    }

    public void fillMatching(final Access1D<Double> aLeftArg, final BinaryFunction<Double> aFunc, final Double aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    PrimitiveDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingLeft.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillMatching(final Double aLeftArg, final BinaryFunction<Double> aFunc, final Access1D<Double> aRightArg) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillMatchingRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                protected void conquer(final int aFirst, final int aLimit) {
                    PrimitiveDenseStore.this.fill(tmpRowDim * aFirst, tmpRowDim * aLimit, aLeftArg, aFunc, aRightArg);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillMatchingRight.THRESHOLD);

        } else {

            this.fill(0, tmpRowDim * tmpColDim, aLeftArg, aFunc, aRightArg);
        }
    }

    public void fillRow(final long row, final long column, final Double value) {
        myUtility.fillRow(row, column, value);
    }

    public void fillRow(final long row, final long column, final NullaryFunction<Double> supplier) {
        myUtility.fillRow(row, column, supplier);
    }

    public void fillTransposed(final Access2D<? extends Number> source) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > FillTransposed.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    FillTransposed.invoke(PrimitiveDenseStore.this.data, tmpRowDim, aFirst, aLimit, source);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, FillTransposed.THRESHOLD);

        } else {

            FillTransposed.invoke(data, tmpRowDim, 0, tmpColDim, source);
        }
    }

    public boolean generateApplyAndCopyHouseholderColumn(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderColumn.invoke(data, myRowDim, row, column, (Householder.Primitive) destination);
    }

    public boolean generateApplyAndCopyHouseholderRow(final int row, final int column, final Householder<Double> destination) {
        return GenerateApplyAndCopyHouseholderRow.invoke(data, myRowDim, row, column, (Householder.Primitive) destination);
    }

    public Double get(final long aRow, final long aCol) {
        return myUtility.get(aRow, aCol);
    }

    public int getColDim() {
        return myColDim;
    }

    public int getIndexOfLargestInColumn(final int row, final int column) {
        return (int) myUtility.indexOfLargestInColumn(row, column);
    }

    public int getMaxDim() {
        return Math.max(myRowDim, myColDim);
    }

    public int getMinDim() {
        return Math.min(myRowDim, myColDim);
    }

    public int getRowDim() {
        return myRowDim;
    }

    @Override
    public int hashCode() {
        return MatrixUtils.hashCode(this);
    }

    public boolean isAbsolute(final long row, final long column) {
        return myUtility.isAbsolute(row, column);
    }

    public boolean isLowerLeftShaded() {
        return false;
    }

    public boolean isSmall(final long row, final long column, final double comparedTo) {
        return myUtility.isSmall(row, column, comparedTo);
    }

    public boolean isUpperRightShaded() {
        return false;
    }

    public boolean isZero(final long row, final long column) {
        return myUtility.isZero(row, column);
    }

    public void maxpy(final Double aSclrA, final MatrixStore<Double> aMtrxX) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > MAXPY.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    MAXPY.invoke(PrimitiveDenseStore.this.data, tmpRowDim, aFirst, aLimit, aSclrA, aMtrxX);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, MAXPY.THRESHOLD);

        } else {

            MAXPY.invoke(data, tmpRowDim, 0, tmpColDim, aSclrA, aMtrxX);
        }
    }

    @Override
    public void modifyAll(final UnaryFunction<Double> aFunc) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > ModifyAll.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    PrimitiveDenseStore.this.modify(tmpRowDim * aFirst, tmpRowDim * aLimit, 1, aFunc);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, ModifyAll.THRESHOLD);

        } else {

            this.modify(tmpRowDim * 0, tmpRowDim * tmpColDim, 1, aFunc);
        }
    }

    public void modifyColumn(final long row, final long column, final UnaryFunction<Double> function) {
        myUtility.modifyColumn(row, column, function);
    }

    public void modifyDiagonal(final long row, final long column, final UnaryFunction<Double> function) {
        myUtility.modifyDiagonal(row, column, function);
    }

    public void modifyOne(final long row, final long column, final UnaryFunction<Double> function) {

        double tmpValue = this.doubleValue(row, column);

        tmpValue = function.invoke(tmpValue);

        this.set(row, column, tmpValue);
    }

    public void modifyRow(final long row, final long column, final UnaryFunction<Double> function) {
        myUtility.modifyRow(row, column, function);
    }

    public MatrixStore<Double> multiply(final Access1D<Double> right) {

        final PrimitiveDenseStore retVal = FACTORY.makeZero(myRowDim, right.count() / myColDim);

        retVal.multiplyRight.invoke(retVal.data, data, myColDim, right);

        return retVal;
    }

    public MatrixStore<Double> multiplyLeft(final Access1D<Double> left) {

        final PrimitiveDenseStore retVal = FACTORY.makeZero(left.count() / myRowDim, myColDim);

        retVal.multiplyLeft.invoke(retVal.data, left, myRowDim, data);

        return retVal;
    }

    public MatrixStore<Double> negate() {
        return new ModificationStore<>(this, FACTORY.function().negate());
    }

    public void negateColumn(final int column) {
        myUtility.modifyColumn(0, column, PrimitiveFunction.NEGATE);
    }

    public void raxpy(final Double scalarA, final int rowX, final int rowY, final int firstColumn) {
        RAXPY.invoke(data, rowY, data, rowX, scalarA, firstColumn, myColDim);
    }

    public MatrixStore.ElementsConsumer<Double> region(final int row, final int column) {
        return new PhysicalStore.ConsumerRegion<Double>(this, row, column);
    }

    public void rotateRight(final int aLow, final int aHigh, final double aCos, final double aSin) {
        RotateRight.invoke(data, myRowDim, aLow, aHigh, aCos, aSin);
    }

    public MatrixStore<Double> scale(final Double scalar) {
        return new ModificationStore<>(this, FACTORY.function().multiply().first(scalar));
    }

    public void set(final long aRow, final long aCol, final double aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void set(final long aRow, final long aCol, final Number aNmbr) {
        myUtility.set(aRow, aCol, aNmbr);
    }

    public void setToIdentity(final int aCol) {
        myUtility.set(aCol, aCol, ONE);
        myUtility.fillColumn(aCol + 1, aCol, ZERO);
    }

    public void substituteBackwards(final Access2D<Double> body, final boolean conjugated) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteBackwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteBackwards.invoke(PrimitiveDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, conjugated);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteBackwards.THRESHOLD);

        } else {

            SubstituteBackwards.invoke(data, tmpRowDim, 0, tmpColDim, body, conjugated);
        }
    }

    public void substituteForwards(final Access2D<Double> body, final boolean onesOnDiagonal, final boolean zerosAboveDiagonal) {

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if (tmpColDim > SubstituteForwards.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    SubstituteForwards.invoke(PrimitiveDenseStore.this.data, tmpRowDim, aFirst, aLimit, body, onesOnDiagonal, zerosAboveDiagonal);
                }

            };

            tmpConquerer.invoke(0, tmpColDim, SubstituteForwards.THRESHOLD);

        } else {

            SubstituteForwards.invoke(data, tmpRowDim, 0, tmpColDim, body, onesOnDiagonal, zerosAboveDiagonal);
        }
    }

    public MatrixStore<Double> subtract(final MatrixStore<Double> subtrahend) {
        return this.add(subtrahend.negate());
    }

    public PrimitiveScalar toScalar(final long row, final long column) {
        return new PrimitiveScalar(this.doubleValue(row + (column * myRowDim)));
    }

    @Override
    public final String toString() {
        return MatrixUtils.toString(this);
    }

    public void transformLeft(final Householder<Double> transformation, final int firstColumn) {

        final Householder.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final double[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if ((tmpColDim - firstColumn) > HouseholderLeft.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    HouseholderLeft.invoke(tmpData, tmpRowDim, aFirst, aLimit, tmpTransf);
                }

            };

            tmpConquerer.invoke(firstColumn, tmpColDim, HouseholderLeft.THRESHOLD);

        } else {

            HouseholderLeft.invoke(tmpData, tmpRowDim, firstColumn, tmpColDim, tmpTransf);
        }
    }

    public void transformLeft(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateLeft.invoke(data, myColDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeRows(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                myUtility.modifyRow(tmpLow, 0L, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyRow(tmpLow, 0L, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyRow(tmpLow, 0, NEGATE);
            }
        }
    }

    public void transformRight(final Householder<Double> transformation, final int firstRow) {

        final Householder.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final double[] tmpData = data;

        final int tmpRowDim = myRowDim;
        final int tmpColDim = myColDim;

        if ((tmpRowDim - firstRow) > HouseholderRight.THRESHOLD) {

            final DivideAndConquer tmpConquerer = new DivideAndConquer() {

                @Override
                public void conquer(final int aFirst, final int aLimit) {
                    HouseholderRight.invoke(tmpData, aFirst, aLimit, tmpColDim, tmpTransf);
                }

            };

            tmpConquerer.invoke(firstRow, tmpRowDim, HouseholderRight.THRESHOLD);

        } else {

            HouseholderRight.invoke(tmpData, firstRow, tmpRowDim, tmpColDim, tmpTransf);
        }
    }

    public void transformRight(final Rotation<Double> transformation) {

        final Rotation.Primitive tmpTransf = PrimitiveDenseStore.cast(transformation);

        final int tmpLow = tmpTransf.low;
        final int tmpHigh = tmpTransf.high;

        if (tmpLow != tmpHigh) {
            if (!Double.isNaN(tmpTransf.cos) && !Double.isNaN(tmpTransf.sin)) {
                RotateRight.invoke(data, myRowDim, tmpLow, tmpHigh, tmpTransf.cos, tmpTransf.sin);
            } else {
                myUtility.exchangeColumns(tmpLow, tmpHigh);
            }
        } else {
            if (!Double.isNaN(tmpTransf.cos)) {
                myUtility.modifyColumn(0L, tmpHigh, MULTIPLY.second(tmpTransf.cos));
            } else if (!Double.isNaN(tmpTransf.sin)) {
                myUtility.modifyColumn(0L, tmpHigh, DIVIDE.second(tmpTransf.sin));
            } else {
                myUtility.modifyColumn(0, tmpHigh, NEGATE);
            }
        }
    }

    public void transformSymmetric(final Householder<Double> transformation) {
        HouseholderHermitian.invoke(data, PrimitiveDenseStore.cast(transformation), new double[(int) transformation.count()]);
    }

    public MatrixStore<Double> transpose() {
        return new TransposedStore<>(this);
    }

    public void tred2(final BasicArray<Double> mainDiagonal, final BasicArray<Double> offDiagonal, final boolean yesvecs) {
        HouseholderHermitian.tred2j(data, ((PrimitiveArray) mainDiagonal).data, ((PrimitiveArray) offDiagonal).data, yesvecs);
    }

    @Override
    public void visitAll(final VoidFunction<Double> visitor) {
        myUtility.visitAll(visitor);
    }

    public void visitColumn(final long row, final long column, final VoidFunction<Double> visitor) {
        myUtility.visitColumn(row, column, visitor);
    }

    public void visitDiagonal(final long row, final long column, final VoidFunction<Double> visitor) {
        myUtility.visitDiagonal(row, column, visitor);
    }

    public void visitRow(final long row, final long column, final VoidFunction<Double> visitor) {
        myUtility.visitRow(row, column, visitor);
    }

}
