package org.ojalgo.matrix;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

public abstract class EvD2D {

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[][] mtrxH, double[][] trspV, double[] vctrWork) {

        final int size = vctrWork.length;

        final int sizeM1 = size - 1;
        final int sizeM2 = size - 2;
        for (int m = 0 + 1; m <= sizeM2; m++) {
            final int ij = m - 1;

            // Scale column.
            double scale = ZERO;

            for (int i = m; i <= sizeM1; i++) {
                scale = scale + ABS.invoke(mtrxH[i][ij]);
            }

            // if (scale != ZERO) {
            if (Double.compare(scale, ZERO) != 0) {

                // Compute Householder transformation.

                double h = ZERO;
                for (int i = sizeM1; i >= m; i--) {
                    vctrWork[i] = mtrxH[i][ij] / scale;
                    h += vctrWork[i] * vctrWork[i];
                }
                double g = SQRT.invoke(h);
                if (vctrWork[m] > 0) {
                    g = -g;
                }
                h = h - (vctrWork[m] * g);
                vctrWork[m] = vctrWork[m] - g;

                // Apply Householder similarity transformation
                // H = (I-u*u'/h)*H*(I-u*u')/h)

                for (int j = m; j < size; j++) {
                    double f = ZERO;
                    for (int i = sizeM1; i >= m; i--) {
                        f += vctrWork[i] * mtrxH[i][j];
                    }
                    f = f / h;
                    for (int i = m; i <= sizeM1; i++) {
                        mtrxH[i][j] -= f * vctrWork[i];
                    }
                }

                for (int i = 0; i <= sizeM1; i++) {
                    double f = ZERO;
                    for (int j = sizeM1; j >= m; j--) {
                        f += vctrWork[j] * mtrxH[i][j];
                    }
                    f = f / h;
                    for (int j = m; j <= sizeM1; j++) {
                        mtrxH[i][j] -= f * vctrWork[j];
                    }
                }
                vctrWork[m] = scale * vctrWork[m];
                mtrxH[m][ij] = scale * g;
            }
        }

        // Accumulate transformations (Algol's ortran).
        if (trspV != null) {

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    //V[i][j] = (i == j ? ONE : ZERO);
                    trspV[j][i] = (i == j ? ONE : ZERO);
                }
            }

            for (int m = sizeM2; m >= (0 + 1); m--) {
                if (mtrxH[m][m - 1] != ZERO) {
                    for (int i = m + 1; i <= sizeM1; i++) {
                        vctrWork[i] = mtrxH[i][m - 1];
                    }
                    for (int j = m; j <= sizeM1; j++) {
                        double g = ZERO;
                        for (int i = m; i <= sizeM1; i++) {
                            //g += ort[i] * V[i][j];
                            g += vctrWork[i] * trspV[j][i];
                        }
                        // Double division avoids possible underflow
                        g = (g / vctrWork[m]) / mtrxH[m][m - 1];
                        for (int i = m; i <= sizeM1; i++) {
                            //V[i][j] += g * ort[i];
                            trspV[j][i] += g * vctrWork[i];
                        }
                    }
                }
            }
        }
    }

    private EvD2D() {
        super();
    }

}
