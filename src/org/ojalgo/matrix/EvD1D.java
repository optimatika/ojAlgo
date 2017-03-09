package org.ojalgo.matrix;

import static org.ojalgo.constant.PrimitiveMath.ZERO;
import static org.ojalgo.function.PrimitiveFunction.*;

public abstract class EvD1D {

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[] mtrxH, final double[] trspV, double[] vctrWork) {

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
                        g += vctrWork[i] * trspV[i + (size * j)];
                    }
                    // Double division avoids possible underflow
                    g = (g / vctrWork[ij]) / mtrxH[tmpIndex];
                    for (int i = ij; i <= sizeM1; i++) {
                        trspV[i + (size * j)] += g * vctrWork[i];
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

    private EvD1D() {
        super();
    }

}
