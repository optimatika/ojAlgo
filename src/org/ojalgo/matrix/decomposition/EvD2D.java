package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

public abstract class EvD2D {

    /**
     * @param mtrxH Array for internal storage of nonsymmetric Hessenberg form.
     * @param vctrWork Temporary work storage
     */
    public static void orthes(final double[][] mtrxH, double[][] trnspV, double[] vctrWork) {

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
        if (trnspV != null) {

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    //V[i][j] = (i == j ? ONE : ZERO);
                    trnspV[j][i] = (i == j ? ONE : ZERO);
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
                            g += vctrWork[i] * trnspV[j][i];
                        }
                        // Double division avoids possible underflow
                        g = (g / vctrWork[m]) / mtrxH[m][m - 1];
                        for (int i = m; i <= sizeM1; i++) {
                            //V[i][j] += g * ort[i];
                            trnspV[j][i] += g * vctrWork[i];
                        }
                    }
                }
            }
        }
    }

    public static void tql2(double[] d, double[] e, double[][] trnspV) {

        int size = d.length;

        for (int i = 1; i < size; i++) {
            e[i - 1] = e[i];
        }
        e[size - 1] = ZERO;

        double f = ZERO;
        double tst1 = ZERO;
        for (int l = 0; l < size; l++) {

            // Find small subdiagonal element
            tst1 = MAX.invoke(tst1, ABS.invoke(d[l]) + ABS.invoke(e[l]));
            int m = l;
            while (m < size) {
                if (ABS.invoke(e[m]) <= (MACHINE_EPSILON * tst1)) {
                    break;
                }
                m++;
            }

            // If m == l, d[l] is an eigenvalue, otherwise, iterate.
            if (m > l) {
                int iter = 0;
                do {
                    iter = iter + 1; // (Could check iteration count here.)

                    // Compute implicit shift
                    double g = d[l];
                    double p = (d[l + 1] - g) / (TWO * e[l]);
                    final double a = p;
                    double r = HYPOT.invoke(a, ONE);
                    if (p < 0) {
                        r = -r;
                    }
                    d[l] = e[l] / (p + r);
                    d[l + 1] = e[l] * (p + r);
                    final double dl1 = d[l + 1];
                    double h = g - d[l];
                    for (int i = l + 2; i < size; i++) {
                        d[i] -= h;
                    }
                    f = f + h;

                    // Implicit QL transformation.
                    p = d[m];
                    double c = ONE;
                    double c2 = c;
                    double c3 = c;
                    final double el1 = e[l + 1];
                    double s = ZERO;
                    double s2 = ZERO;
                    for (int i = m - 1; i >= l; i--) {
                        c3 = c2;
                        c2 = c;
                        s2 = s;
                        g = c * e[i];
                        h = c * p;
                        final double a1 = p;
                        r = HYPOT.invoke(a1, e[i]);
                        e[i + 1] = s * r;
                        s = e[i] / r;
                        c = p / r;
                        p = (c * d[i]) - (s * g);
                        d[i + 1] = h + (s * ((c * g) + (s * d[i])));

                        // Accumulate transformation.
                        if (trnspV != null) {
                            double h1;
                            for (int k = 0; k < trnspV[i].length; k++) {
                                //h = V[k][i + 1];
                                h1 = trnspV[i + 1][k];
                                //V[k][i + 1] = (s * V[k][i]) + (c * h);
                                trnspV[i + 1][k] = (s * trnspV[i][k]) + (c * h1);
                                //V[k][i] = (c * V[k][i]) - (s * h);
                                trnspV[i][k] = (c * trnspV[i][k]) - (s * h1);
                            }
                        }

                    }
                    // p = (-s * s2 * c3 * el1 * e[l]) / dl1;
                    p = (-s * s2 * c3) * ((el1 / dl1) * e[l]);
                    e[l] = s * p;
                    d[l] = c * p;

                    // Check for convergence.
                } while (ABS.invoke(e[l]) > (MACHINE_EPSILON * tst1));
            }
            d[l] = d[l] + f;
            e[l] = ZERO;
        }

        // Sort eigenvalues and corresponding vectors.
        for (int i = 0; i < (size - 1); i++) {

            int k = i;
            double p = d[i];
            for (int j = i + 1; j < size; j++) {
                if (d[j] > p) {
                    k = j;
                    p = d[j];
                }
            }
            if (k != i) {
                d[k] = d[i];
                d[i] = p;

                if (trnspV != null) {
                    double[] tmpCol = trnspV[i];
                    trnspV[i] = trnspV[k];
                    trnspV[k] = tmpCol;
                }
            }
        }
    }

    private EvD2D() {
        super();
    }

}
