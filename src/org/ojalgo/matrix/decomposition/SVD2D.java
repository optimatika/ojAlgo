package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

public abstract class SVD2D {

    static void doCase1(final double[] s, final double[] e, final boolean factors, final int p, final double[][] myVt, final int n, final int k) {

        double f = e[p - 2];
        e[p - 2] = ZERO;
        for (int j = p - 2; j >= k; j--) {
            final double b = f;
            double t = HYPOT.invoke(s[j], b);
            final double cs = s[j] / t;
            final double sn = f / t;
            s[j] = t;
            if (j != k) {
                f = -sn * e[j - 1];
                e[j - 1] = cs * e[j - 1];
            }
            if (factors) {
                for (int i = 0; i < n; i++) {
                    t = (cs * myVt[j][i]) + (sn * myVt[p - 1][i]);
                    myVt[p - 1][i] = (-sn * myVt[j][i]) + (cs * myVt[p - 1][i]);
                    myVt[j][i] = t;
                }
            }
        }
    }

    static void doCase2(final double[] s, final double[] e, final boolean factors, final int p, final double[][] myUt, final int m, final int k) {
        double f = e[k - 1];
        e[k - 1] = ZERO;
        for (int j = k; j < p; j++) {
            final double b = f;
            double t = HYPOT.invoke(s[j], b);
            final double cs = s[j] / t;
            final double sn = f / t;
            s[j] = t;
            f = -sn * e[j];
            e[j] = cs * e[j];
            if (factors) {
                for (int i = 0; i < m; i++) {
                    // t = (cs * myU[i][j]) + (sn * myU[i][k - 1]);
                    t = (cs * myUt[j][i]) + (sn * myUt[k - 1][i]);
                    // myU[i][k - 1] = (-sn * myU[i][j]) + (cs * myU[i][k - 1]);
                    myUt[k - 1][i] = (-sn * myUt[j][i]) + (cs * myUt[k - 1][i]);
                    // myU[i][j] = t;
                    myUt[j][i] = t;
                }
            }
        }
    }

    static void doCase3(final double[] s, final double[] e, final boolean factors, final int p, final double[][] myUt, final double[][] myVt, final int n,
            final int m, final int k) {
        // Calculate the shift.
        final double scale = MAX.invoke(MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(s[p - 1]), ABS.invoke(s[p - 2])), ABS.invoke(e[p - 2])), ABS.invoke(s[k])),
                ABS.invoke(e[k]));
        final double sp = s[p - 1] / scale;
        final double spm1 = s[p - 2] / scale;
        final double epm1 = e[p - 2] / scale;
        final double sk = s[k] / scale;
        final double ek = e[k] / scale;
        final double b = (((spm1 + sp) * (spm1 - sp)) + (epm1 * epm1)) / TWO;
        final double c = (sp * epm1) * (sp * epm1);
        double shift = ZERO;
        // if ((b != ZERO) | (c != ZERO)) {
        if ((Double.compare(b, ZERO) != 0) || (Double.compare(c, ZERO) != 0)) {
            shift = SQRT.invoke((b * b) + c);
            if (b < ZERO) {
                shift = -shift;
            }
            shift = c / (b + shift);
        }
        double f = ((sk + sp) * (sk - sp)) + shift;
        double g = sk * ek;

        // Chase zeros.
        for (int j = k; j < (p - 1); j++) {
            final double a = f;
            final double b1 = g;
            double t = HYPOT.invoke(a, b1);
            double cs = f / t;
            double sn = g / t;
            if (j != k) {
                e[j - 1] = t;
            }
            f = (cs * s[j]) + (sn * e[j]);
            e[j] = (cs * e[j]) - (sn * s[j]);
            g = sn * s[j + 1];
            s[j + 1] = cs * s[j + 1];
            if (factors) {
                for (int i = 0; i < n; i++) {
                    // t = (cs * myV[i][j]) + (sn * myV[i][j + 1]);
                    t = (cs * myVt[j][i]) + (sn * myVt[j + 1][i]);
                    // myV[i][j + 1] = (-sn * myV[i][j]) + (cs * myV[i][j + 1]);
                    myVt[j + 1][i] = (-sn * myVt[j][i]) + (cs * myVt[j + 1][i]);
                    // myV[i][j] = t;
                    myVt[j][i] = t;
                }
            }
            final double a1 = f;
            final double b2 = g;
            t = HYPOT.invoke(a1, b2);
            cs = f / t;
            sn = g / t;
            s[j] = t;
            f = (cs * e[j]) + (sn * s[j + 1]);
            s[j + 1] = (-sn * e[j]) + (cs * s[j + 1]);
            g = sn * e[j + 1];
            e[j + 1] = cs * e[j + 1];
            if (factors && (j < (m - 1))) {
                for (int i = 0; i < m; i++) {
                    // t = (cs * myU[i][j]) + (sn * myU[i][j + 1]);
                    t = (cs * myUt[j][i]) + (sn * myUt[j + 1][i]);
                    // myU[i][j + 1] = (-sn * myU[i][j]) + (cs * myU[i][j + 1]);
                    myUt[j + 1][i] = (-sn * myUt[j][i]) + (cs * myUt[j + 1][i]);
                    // myU[i][j] = t;
                    myUt[j][i] = t;
                }
            }
        }
        e[p - 2] = f;
    }

    static int doCase4(final double[] s, final boolean factors, final double[][] myUt, final double[][] myVt, final int n, final int m, final int pp, int k) {

        double[] tmpAt_k;
        // Make the singular values positive.
        if (s[k] <= ZERO) {
            s[k] = (s[k] < ZERO ? -s[k] : ZERO);
            if (factors) {
                for (int i = 0; i <= pp; i++) {
                    // myV[i][k] = -myV[i][k];
                    myVt[k][i] = -myVt[k][i];
                }
            }
        }

        // Order the singular values.
        while (k < pp) {
            if (s[k] >= s[k + 1]) {
                break;
            }
            final double t = s[k];
            s[k] = s[k + 1];
            s[k + 1] = t;
            if (factors && (k < (n - 1))) {
                tmpAt_k = myVt[k + 1]; // Re-use tmpAt_k for a completely different purpose
                myVt[k + 1] = myVt[k];
                myVt[k] = tmpAt_k;
            }
            if (factors && (k < (m - 1))) {
                tmpAt_k = myUt[k + 1]; // Re-use tmpAt_k for a completely different purpose
                myUt[k + 1] = myUt[k];
                myUt[k] = tmpAt_k;
            }
            k++;
        }
        return k;
    }

    static void toDiagonal(final double[] myS, final double[] tmpE, final boolean factors, int p, final double[][] myUt, final double[][] myVt) {

        final int n = myS.length;
        final int m = myUt != null ? myUt[0].length : n;

        // Main iteration loop for the singular values.
        final int pp = p - 1;
        final double eps = POW.invoke(TWO, -52.0);
        final double tiny = POW.invoke(TWO, -966.0);
        while (p > 0) {
            int k, kase;

            // Here is where a test for too many iterations would go.

            // This section of the program inspects for
            // negligible elements in the s and e arrays.  On
            // completion the variables kase and k are set as follows.

            // kase = 1     if s(p) and e[k-1] are negligible and k<p
            // kase = 2     if s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              s(k), ..., s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).

            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (ABS.invoke(tmpE[k]) <= (tiny + (eps * (ABS.invoke(myS[k]) + ABS.invoke(myS[k + 1]))))) {
                    tmpE[k] = ZERO;
                    break;
                }
            }
            if (k == (p - 2)) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    final double t = (ks != p ? ABS.invoke(tmpE[ks]) : 0.) + (ks != (k + 1) ? ABS.invoke(tmpE[ks - 1]) : 0.);
                    if (ABS.invoke(myS[ks]) <= (tiny + (eps * t))) {
                        myS[ks] = ZERO;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == (p - 1)) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;

            // Perform the task indicated by kase.
            switch (kase) {

            // Deflate negligible s(p).
            case 1: {
                SVD2D.doCase1(myS, tmpE, factors, p, myVt, n, k);
            }
                break;

            // Split at negligible s(k).
            case 2: {
                SVD2D.doCase2(myS, tmpE, factors, p, myUt, m, k);
            }
                break;

            // Perform one qr step.
            case 3: {

                SVD2D.doCase3(myS, tmpE, factors, p, myUt, myVt, n, m, k);
            }
                break;

            // Convergence.
            case 4: {

                k = SVD2D.doCase4(myS, factors, myUt, myVt, n, m, pp, k);
                p--;
            }
                break;
            }
        }
    }

}
