package org.ojalgo.matrix.decomposition;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.array.Array1D;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.matrix.decomposition.function.RotateRight;

public abstract class SVD1D {

    static void doCase1(final double[] s, final double[] e, final int p, final int k, final RotateRight mtrxQ2) {

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

            mtrxQ2.rotateRight(p - 1, j, cos, sin);
        }
    }

    static void doCase2(final double[] s, final double[] e, final int p, final int k, final RotateRight mtrxQ1) {

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

            mtrxQ1.rotateRight(k - 1, j, cos, sin);
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

    static void doCase4(final double[] s, final int k, final DecompositionStore<?> mtrxQ1, final DecompositionStore<?> mtrxQ2) {

        final int tmpDiagDim = s.length;

        // Make the singular values positive.
        final double tmpSk = s[k];
        if (tmpSk < ZERO) {
            s[k] = -tmpSk;

            if (mtrxQ2 != null) {
                //aQ2.modifyColumn(0, k,  NEGATE);
                mtrxQ2.negateColumn(k);

            }
        } else if (tmpSk == ZERO) {
            s[k] = ZERO; // To get rid of negative zeros
        }

        // Order the singular values.
        int tmpK = k;

        while (tmpK < (tmpDiagDim - 1)) {
            if (s[tmpK] >= s[tmpK + 1]) {
                break;
            }
            final double t = s[tmpK];
            s[tmpK] = s[tmpK + 1];
            s[tmpK + 1] = t;

            if (mtrxQ1 != null) {
                mtrxQ1.exchangeColumns(tmpK + 1, tmpK);
            }
            if (mtrxQ2 != null) {
                mtrxQ2.exchangeColumns(tmpK + 1, tmpK);
            }

            tmpK++;
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

                RotateRight q2RotR = mtrxQ2 != null ? mtrxQ2 : RotateRight.NULL;
                SVD1D.doCase1(s, e, p, k, q2RotR);
                break;

            case 2: // Split at negligible s[k]

                SVD1D.doCase2(s, e, p, k, mtrxQ1);
                break;

            case 3: // Perform QR-step.

                SVD1D.doCase3(s, e, p, k, mtrxQ1, mtrxQ2);
                break;

            case 4: // Convergence

                SVD1D.doCase4(s, k, mtrxQ1, mtrxQ2);
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
