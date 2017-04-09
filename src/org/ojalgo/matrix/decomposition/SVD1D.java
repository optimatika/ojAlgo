package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.matrix.decomposition.function.ExchangeColumns;
import org.ojalgo.matrix.decomposition.function.NegateColumn;
import org.ojalgo.matrix.decomposition.function.RotateRight;

public abstract class SVD1D {

    static void doCase1(final double[] s, final double[] e, final int p, final int k, final RotateRight q2RotR) {

        double f = e[p - 2];
        e[p - 2] = ZERO;

        double tmp, cos, sin;

        for (int j = p - 2; j >= k; j--) {

            tmp = HYPOT.invoke(s[j], f);
            cos = s[j] / tmp;
            sin = f / tmp;
            s[j] = tmp;

            if (j != k) {
                tmp = e[j - 1];
                f = -sin * tmp;
                e[j - 1] = cos * tmp;
            }

            q2RotR.rotateRight(p - 1, j, cos, sin);
        }
    }

    static void doCase2(final double[] s, final double[] e, final int p, final int k, final RotateRight q1RotR) {

        double f = e[k - 1];
        e[k - 1] = ZERO;

        double tmp, cos, sin;

        for (int j = k; j < p; j++) {

            tmp = HYPOT.invoke(s[j], f);
            cos = s[j] / tmp;
            sin = f / tmp;

            s[j] = tmp;
            tmp = e[j];
            f = -sin * tmp;
            e[j] = cos * tmp;

            q1RotR.rotateRight(k - 1, j, cos, sin);
        }
    }

    static void doCase3(final double[] s, final double[] e, final int p, final int k, final RotateRight q1RotR, final RotateRight q2RotR) {

        // Calculate the shift.
        final double scale = MAX.invoke(MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(s[p - 1]), ABS.invoke(s[p - 2])), ABS.invoke(e[p - 2])), ABS.invoke(s[k])),
                ABS.invoke(e[k]));

        final double s_p1 = s[p - 1] / scale;
        final double s_p2 = s[p - 2] / scale;
        final double e_p2 = e[p - 2] / scale;

        final double s_k = s[k] / scale;
        final double e_k = e[k] / scale;

        final double b = (((s_p2 + s_p1) * (s_p2 - s_p1)) + (e_p2 * e_p2)) / TWO;
        final double c = (s_p1 * e_p2) * (s_p1 * e_p2);

        double shift = ZERO;
        if ((Double.compare(c, ZERO) != 0) || (Double.compare(b, ZERO) != 0)) {
            shift = SQRT.invoke((b * b) + c);
            if (b < ZERO) {
                shift = -shift;
            }
            shift = c / (b + shift);
        }

        double f = ((s_k + s_p1) * (s_k - s_p1)) + shift;
        double g = s_k * e_k;

        double tmp, cos, sin;

        // Chase zeros.
        for (int j = k; j < (p - 1); j++) {

            tmp = HYPOT.invoke(f, g);
            cos = f / tmp;
            sin = g / tmp;
            if (j != k) {
                e[j - 1] = tmp;
            }

            f = (cos * s[j]) + (sin * e[j]);
            e[j] = (cos * e[j]) - (sin * s[j]);
            g = sin * s[j + 1];
            s[j + 1] = cos * s[j + 1];

            q2RotR.rotateRight(j + 1, j, cos, sin);

            tmp = HYPOT.invoke(f, g);
            cos = f / tmp;
            sin = g / tmp;
            s[j] = tmp;

            f = (cos * e[j]) + (sin * s[j + 1]);
            s[j + 1] = (-sin * e[j]) + (cos * s[j + 1]);
            g = sin * e[j + 1];
            e[j + 1] = cos * e[j + 1];

            q1RotR.rotateRight(j + 1, j, cos, sin);
        }

        e[p - 2] = f;
    }

    static void doCase4(final double[] s, final int k, final NegateColumn q2NegCol, final ExchangeColumns q1XchgCols, final ExchangeColumns q2XchgCols) {

        // Make the singular values positive.
        if (s[k] <= ZERO) {
            s[k] = s[k] < ZERO ? -s[k] : ZERO;
            q2NegCol.negateColumn(k);
        }

        final int size = s.length;
        double tmp;

        // Order the singular values.
        for (int iter = k, next = iter + 1; (next < size) && (s[iter] < s[next]); iter++, next++) {

            tmp = s[iter];
            s[iter] = s[next];
            s[next] = tmp;

            q1XchgCols.exchangeColumns(iter, next);
            q2XchgCols.exchangeColumns(iter, next);
        }
    }

    static void toDiagonal(final double[] s, final double[] e, final RotateRight q1RotR, final RotateRight q2RotR, final ExchangeColumns q1XchgCols,
            final ExchangeColumns q2XchgCols, final NegateColumn q2NegCol) {

        int p = s.length;
        while (p > 0) {
            int k, kase;

            // This section of the program inspects for negligible elements in the s and e arrays.
            // On completion the variables kase and k are set as follows:
            //
            // kase = 1     if s[p] and e[k-1] are negligible and k<p                           => deflate negligible s[p]
            // kase = 2     if s[k] is negligible and k<p                                       => split at negligible s[k]
            // kase = 3     if e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible    => perform QR-step
            // kase = 4     if e[p-1] is negligible                                             => convergence

            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (ABS.invoke(e[k]) <= (TINY + (MACHINE_EPSILON * (ABS.invoke(s[k]) + ABS.invoke(s[k + 1]))))) {
                    e[k] = ZERO;
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
                    final double t = (ks != p ? ABS.invoke(e[ks]) : ZERO) + (ks != (k + 1) ? ABS.invoke(e[ks - 1]) : ZERO);
                    if (ABS.invoke(s[ks]) <= (TINY + (MACHINE_EPSILON * t))) {
                        s[ks] = ZERO;
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

            switch (kase) { // Perform the task indicated by kase.

            // s[p] and e[k-1] are negligible and k<p
            case 1: // Deflate negligible s[p]

                SVD1D.doCase1(s, e, p, k, q2RotR);
                break;

            // s[k] is negligible and k<p
            case 2: // Split at negligible s[k]

                SVD1D.doCase2(s, e, p, k, q1RotR);
                break;

            // e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible
            case 3: // Perform QR-step.

                SVD1D.doCase3(s, e, p, k, q1RotR, q2RotR);
                break;

            // e[p-1] is negligible
            case 4: // Convergence

                SVD1D.doCase4(s, k, q2NegCol, q1XchgCols, q2XchgCols);
                p--;
                break;

            // Should never happen
            default:

                throw new IllegalStateException();

            } // switch
        } // while
    }

}
