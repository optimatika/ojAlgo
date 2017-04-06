package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
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

    static void doCase3(final double[] s, final double[] e, final int p, final int k, final DecompositionStore<?> mtrxQ1, final DecompositionStore<?> mtrxQ2) {

        final int indPm1 = p - 1;
        final int indPm2 = p - 2;

        // Calculate the shift.
        final double scale = MAX.invoke(
                MAX.invoke(MAX.invoke(MAX.invoke(ABS.invoke(s[indPm1]), ABS.invoke(s[indPm2])), ABS.invoke(e[indPm2])), ABS.invoke(s[k])), ABS.invoke(e[k]));

        final double sPm1 = s[indPm1] / scale;
        final double sPm2 = s[indPm2] / scale;
        final double ePm2 = e[indPm2] / scale;
        final double sK = s[k] / scale;
        final double eK = e[k] / scale;

        final double b = (((sPm2 + sPm1) * (sPm2 - sPm1)) + (ePm2 * ePm2)) / TWO;
        final double c = (sPm1 * ePm2) * (sPm1 * ePm2);
        double shift = ZERO;
        // if ((c != ZERO) || (b != ZERO)) {
        if ((Double.compare(c, ZERO) != 0) || (Double.compare(b, ZERO) != 0)) {
            shift = SQRT.invoke((b * b) + c);
            if (b < ZERO) {
                shift = -shift;
            }
            shift = c / (b + shift);
        }

        double f = ((sK + sPm1) * (sK - sPm1)) + shift;
        double g = sK * eK;

        double t;
        double cs;
        double sn;

        // Chase zeros.
        for (int j = k; j < indPm1; j++) {

            t = HYPOT.invoke(f, g);
            cs = f / t;
            sn = g / t;
            if (j != k) {
                e[j - 1] = t;
            }
            f = (cs * s[j]) + (sn * e[j]);
            e[j] = (cs * e[j]) - (sn * s[j]);
            g = sn * s[j + 1];
            s[j + 1] = cs * s[j + 1];

            if (mtrxQ2 != null) {
                mtrxQ2.rotateRight(j + 1, j, cs, sn);

            }

            t = HYPOT.invoke(f, g);
            cs = f / t;
            sn = g / t;
            s[j] = t;
            f = (cs * e[j]) + (sn * s[j + 1]);
            s[j + 1] = (-sn * e[j]) + (cs * s[j + 1]);
            g = sn * e[j + 1];
            e[j + 1] = cs * e[j + 1];

            if (mtrxQ1 != null) {
                mtrxQ1.rotateRight(j + 1, j, cs, sn);

            }
        }

        e[indPm2] = f;
    }

    static void doCase4(final double[] s, final int k, final NegateColumn q2NegCol, final ExchangeColumns q1XchgCols, final ExchangeColumns q2XchgCols) {

        // Make the singular values positive.
        if (s[k] <= ZERO) {
            s[k] = s[k] < ZERO ? -s[k] : ZERO;
            q2NegCol.negateColumn(k);
        }

        // Order the singular values.
        for (int iter = k, next = iter + 1; (next < s.length) && (s[iter] < s[next]); iter++, next++) {

            final double tmp = s[iter];
            s[iter] = s[next];
            s[next] = tmp;

            q1XchgCols.exchangeColumns(iter, next);
            q2XchgCols.exchangeColumns(iter, next);
        }
    }

    static Array1D<Double> toDiagonal(final DiagonalArray1D<?> bidiagonal, final DecompositionStore<?> mtrxQ1, final DecompositionStore<?> mtrxQ2) {

        final int tmpDiagDim = bidiagonal.mainDiagonal.size();

        final double[] s = bidiagonal.mainDiagonal.toRawCopy1D(); // s
        final double[] e = new double[tmpDiagDim]; // e
        final int tmpOffLength = bidiagonal.superdiagonal.size();
        for (int i = 0; i < tmpOffLength; i++) {
            e[i] = bidiagonal.superdiagonal.doubleValue(i);
        }

        final RotateRight q1RotR = mtrxQ1 != null ? mtrxQ1 : RotateRight.NULL;
        final RotateRight q2RotR = mtrxQ2 != null ? mtrxQ2 : RotateRight.NULL;
        final ExchangeColumns q1XchgCols = mtrxQ1 != null ? mtrxQ1 : ExchangeColumns.NULL;
        final ExchangeColumns q2XchgCols = mtrxQ2 != null ? mtrxQ2 : ExchangeColumns.NULL;
        final NegateColumn q2NegCol = mtrxQ1 != null ? mtrxQ2 : NegateColumn.NULL;

        // Main iteration loop for the singular values.
        int kase;
        int k;
        int p = tmpDiagDim;
        while (p > 0) {

            //
            // This section of the program inspects for negligible elements in the s and e arrays.
            // On completion the variables kase and k are set as follows:
            //
            // kase = 1     if s[p] and e[k-1] are negligible and k<p                           => deflate negligible s[p]
            // kase = 2     if s[k] is negligible and k<p                                       => split at negligible s[k]
            // kase = 3     if e[k-1] is negligible, k<p, and s(k)...s(p) are not negligible    => perform QR-step
            // kase = 4     if e[p-1] is negligible                                             => convergence.
            //

            kase = 0;
            k = 0;

            for (k = p - 2; k >= -1; k--) {
                if (k == -1) {
                    break;
                }
                if (ABS.invoke(e[k]) <= (SVDnew32.TINY + (MACHINE_EPSILON * (ABS.invoke(s[k]) + ABS.invoke(s[k + 1]))))) {
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
                    if (ABS.invoke(s[ks]) <= (SVDnew32.TINY + (MACHINE_EPSILON * t))) {
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

            case 1: // Deflate negligible s[p]

                SVD1D.doCase1(s, e, p, k, q2RotR);
                break;

            case 2: // Split at negligible s[k]

                SVD1D.doCase2(s, e, p, k, q1RotR);
                break;

            case 3: // Perform QR-step.

                SVD1D.doCase3(s, e, p, k, mtrxQ1, mtrxQ2);
                break;

            case 4: // Convergence

                SVD1D.doCase4(s, k, q2NegCol, q1XchgCols, q2XchgCols);
                p--;
                break;

            default:

                throw new IllegalStateException();

            } // switch
        } // while

        //return new PrimitiveArray(s).asArray1D();
        return Array1D.PRIMITIVE64.wrap(Primitive64Array.wrap(s));
    }

}
