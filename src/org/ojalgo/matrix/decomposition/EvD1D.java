package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.constant.PrimitiveMath;
import org.ojalgo.function.PrimitiveFunction;

public abstract class EvD1D {

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[] mtrxH, final double[] trnspV, double[] vctrWork) {

        final int size = vctrWork.length;

        final int sizeM1 = size - 1;
        final int sizeM2 = size - 2;
        for (int ij = 0; ij < sizeM2; ij++) {
            final int m = ij + 1;

            // Scale column.
            double tmpColNorm1 = ZERO;

            for (int i = m; i < size; i++) {
                tmpColNorm1 += ABS.invoke(mtrxH[i + (size * ij)]);
            }

            // if (tmpColNorm1 !=  ZERO) {
            if (Double.compare(tmpColNorm1, ZERO) != 0) {

                // Compute Householder transformation.
                double tmpInvBeta = ZERO;
                for (int i = sizeM1; i >= m; i--) {
                    vctrWork[i] = mtrxH[i + (size * ij)] / tmpColNorm1;
                    tmpInvBeta += vctrWork[i] * vctrWork[i];
                }
                double g = SQRT.invoke(tmpInvBeta);
                if (vctrWork[m] > 0) {
                    g = -g;
                }
                tmpInvBeta = tmpInvBeta - (vctrWork[m] * g);
                vctrWork[m] = vctrWork[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)
                for (int j = m; j < size; j++) {
                    double f = ZERO;
                    for (int i = sizeM1; i >= m; i--) {
                        f += vctrWork[i] * mtrxH[i + (size * j)];
                    }
                    f = f / tmpInvBeta;
                    for (int i = m; i <= sizeM1; i++) {
                        mtrxH[i + (size * j)] -= f * vctrWork[i];
                    }
                }

                for (int i = 0; i < size; i++) {
                    double f = ZERO;
                    for (int j = sizeM1; j >= m; j--) {
                        f += vctrWork[j] * mtrxH[i + (size * j)];
                    }
                    f = f / tmpInvBeta;
                    for (int j = m; j < size; j++) {
                        mtrxH[i + (size * j)] -= f * vctrWork[j];
                    }
                }

                vctrWork[m] = tmpColNorm1 * vctrWork[m];
                mtrxH[m + (size * ij)] = tmpColNorm1 * g;
            }
        }

        // BasicLogger.logDebug("Jama H", new PrimitiveDenseStore(tmpDiagDim,
        // tmpDiagDim, aMtrxH));

        // Här borde Hessenberg vara klar
        // Nedan börjar uträkningen av Q

        // Accumulate transformations (Algol's ortran).
        for (int ij = sizeM2; ij >= 1; ij--) {
            final int tmpIndex = ij + (size * (ij - 1));
            if (mtrxH[tmpIndex] != ZERO) {
                for (int i = ij + 1; i <= sizeM1; i++) {
                    vctrWork[i] = mtrxH[i + (size * (ij - 1))];
                }
                for (int j = ij; j <= sizeM1; j++) {
                    double g = ZERO;
                    for (int i = ij; i <= sizeM1; i++) {
                        g += vctrWork[i] * trnspV[i + (size * j)];
                    }
                    // Double division avoids possible underflow
                    g = (g / vctrWork[ij]) / mtrxH[tmpIndex];
                    for (int i = ij; i <= sizeM1; i++) {
                        trnspV[i + (size * j)] += g * vctrWork[i];
                    }
                }
            } else {
                // BasicLogger.logDebug("Iter V", new
                // PrimitiveDenseStore(tmpDiagDim, tmpDiagDim, aMtrxV));
            }
        }

        // BasicLogger.logDebug("Jama V", new PrimitiveDenseStore(tmpDiagDim,
        // tmpDiagDim, aMtrxV));

    }

    public static Array1D<Double> tql2(final DiagonalAccess<?> tridiagonal, final DecompositionStore<?> mtrxV) {

        //   BasicLogger.logDebug("Tridiagonal={}", aTridiagonal.toString());

        final Array1D<?> tmpMainDiagonal = tridiagonal.mainDiagonal;
        final Array1D<?> tmpSubdiagonal = tridiagonal.subdiagonal;

        final int size = tmpMainDiagonal.size();

        final double[] tmpMainDiagData = tmpMainDiagonal.toRawCopy1D(); // Actually unnecessary to copy
        final double[] tmpOffDiagData = new double[size]; // The algorith needs the array to be the same length as the main diagonal
        final int tmpLength = tmpSubdiagonal.size();
        for (int i = 0; i < tmpLength; i++) {
            tmpOffDiagData[i] = tmpSubdiagonal.doubleValue(i);
        }

        //        BasicLogger.logDebug("BEGIN diagonalize");
        //        BasicLogger.logDebug("Main D: {}", Arrays.toString(tmpMainDiagonal));
        //        BasicLogger.logDebug("Seco D: {}", Arrays.toString(tmpOffDiagonal));
        //        BasicLogger.logDebug("V", aV);
        //        BasicLogger.logDebug();

        double tmpShift = PrimitiveMath.ZERO;
        double tmpShiftIncr;

        double tmpMagnitude = PrimitiveMath.ZERO;
        double tmpLocalEpsilon;

        int m;
        // Main loop
        for (int l = 0; l < size; l++) {

            //BasicLogger.logDebug("Loop l=" + l, tmpMainDiagonal, tmpOffDiagonal);

            // Find small subdiagonal element
            tmpMagnitude = PrimitiveFunction.MAX.invoke(tmpMagnitude,
                    PrimitiveFunction.ABS.invoke(tmpMainDiagData[l]) + PrimitiveFunction.ABS.invoke(tmpOffDiagData[l]));
            tmpLocalEpsilon = MACHINE_EPSILON * tmpMagnitude;

            m = l;
            while (m < size) {
                if (PrimitiveFunction.ABS.invoke(tmpOffDiagData[m]) <= tmpLocalEpsilon) {
                    break;
                }
                m++;
            }

            // If m == l, aMainDiagonal[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {

                do {

                    final double tmp1Ml0 = tmpMainDiagData[l]; // (l,l)
                    final double tmp1Ml1 = tmpMainDiagData[l + 1]; // (l+1,l+1)
                    final double tmp1Sl0 = tmpOffDiagData[l]; // (l+1,l) and (l,l+1)

                    // Compute implicit shift

                    double p = (tmp1Ml1 - tmp1Ml0) / (tmp1Sl0 + tmp1Sl0);
                    double r = PrimitiveFunction.HYPOT.invoke(p, PrimitiveMath.ONE);
                    if (p < 0) {
                        r = -r;
                    }

                    final double tmp2Ml0 = tmpMainDiagData[l] = tmp1Sl0 / (p + r); // (l,l)
                    final double tmp2Ml1 = tmpMainDiagData[l + 1] = tmp1Sl0 * (p + r); // (l+1,l+1)
                    final double tmp2Sl1 = tmpOffDiagData[l + 1]; // (l+1,l) and (l,l+1)

                    tmpShiftIncr = tmp1Ml0 - tmp2Ml0;
                    for (int i = l + 2; i < size; i++) {
                        tmpMainDiagData[i] -= tmpShiftIncr;
                    }
                    tmpShift += tmpShiftIncr;

                    //BasicLogger.logDebug("New shift =" + tmpShift, tmpMainDiagonal, tmpOffDiagonal);

                    // Implicit QL transformation

                    double tmpRotCos = PrimitiveMath.ONE;
                    double tmpRotSin = PrimitiveMath.ZERO;

                    double tmpRotCos2 = tmpRotCos;
                    double tmpRotSin2 = PrimitiveMath.ZERO;

                    double tmpRotCos3 = tmpRotCos;

                    p = tmpMainDiagData[m]; // Initiate p
                    //      BasicLogger.logDebug("m={} l={}", m, l);
                    for (int i = m - 1; i >= l; i--) {

                        final double tmp1Mi0 = tmpMainDiagData[i];
                        final double tmp1Si0 = tmpOffDiagData[i];

                        r = PrimitiveFunction.HYPOT.invoke(p, tmp1Si0);

                        tmpRotCos3 = tmpRotCos2;

                        tmpRotCos2 = tmpRotCos;
                        tmpRotSin2 = tmpRotSin;

                        tmpRotCos = p / r;
                        tmpRotSin = tmp1Si0 / r;

                        tmpMainDiagData[i + 1] = (tmpRotCos2 * p) + (tmpRotSin * ((tmpRotCos * tmpRotCos2 * tmp1Si0) + (tmpRotSin * tmp1Mi0)));
                        tmpOffDiagData[i + 1] = tmpRotSin2 * r;

                        p = (tmpRotCos * tmp1Mi0) - (tmpRotSin * tmpRotCos2 * tmp1Si0); // Next p

                        // Accumulate transformation - rotate the eigenvector matrix
                        //aV.transformRight(new Rotation.Primitive(i, i + 1, tmpRotCos, tmpRotSin));

                        //BasicLogger.logDebug("low={} high={} cos={} sin={}", i, i + 1, tmpRotCos, tmpRotSin);
                        if (mtrxV != null) {
                            mtrxV.rotateRight(i, i + 1, tmpRotCos, tmpRotSin);
                        }

                        //          EigenvalueDecomposition.log("QL step done i=" + i, tmpMainDiagonal, tmpOffDiagonal);

                    }

                    p = (-tmpRotSin * tmpRotSin2 * tmpRotCos3) * ((tmp2Sl1 / tmp2Ml1) * tmpOffDiagData[l]); // Final p

                    tmpMainDiagData[l] = tmpRotCos * p;
                    tmpOffDiagData[l] = tmpRotSin * p;

                } while (PrimitiveFunction.ABS.invoke(tmpOffDiagData[l]) > tmpLocalEpsilon); // Check for convergence
            } // End if (m > l)

            tmpMainDiagData[l] = tmpMainDiagData[l] + tmpShift;
            tmpOffDiagData[l] = PrimitiveMath.ZERO;
        } // End main loop - l

        //        BasicLogger.logDebug("END diagonalize");
        //        BasicLogger.logDebug("Main D: {}", Arrays.toString(tmpMainDiagonal));
        //        BasicLogger.logDebug("Seco D: {}", Arrays.toString(tmpOffDiagonal));
        //        BasicLogger.logDebug("V", aV);
        //        BasicLogger.logDebug();

        //        for (int i = 0; i < tmpMainDiagData.length; i++) {
        //            tmpMainDiagonal.set(i, tmpMainDiagData[i]);
        //        }

        //return new PrimitiveArray(tmpMainDiagonal).asArray1D();
        return Array1D.PRIMITIVE64.wrap(Primitive64Array.wrap(tmpMainDiagData));
    }

    private EvD1D() {
        super();
    }

}
