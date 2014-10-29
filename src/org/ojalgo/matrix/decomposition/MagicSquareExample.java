package org.ojalgo.matrix.decomposition;

import java.util.Date;

import org.ojalgo.matrix.store.RawStore;

/** Example of use of RawStore Class, featuring magic squares. **/

class MagicSquareExample {

    /** Format double with Fw.d. **/

    public static String fixedWidthDoubletoString(final double x, final int w, final int d) {
        final java.text.DecimalFormat fmt = new java.text.DecimalFormat();
        fmt.setMaximumFractionDigits(d);
        fmt.setMinimumFractionDigits(d);
        fmt.setGroupingUsed(false);
        String s = fmt.format(x);
        while (s.length() < w) {
            s = " " + s;
        }
        return s;
    }

    /** Format integer with Iw. **/

    public static String fixedWidthIntegertoString(final int n, final int w) {
        String s = Integer.toString(n);
        while (s.length() < w) {
            s = " " + s;
        }
        return s;
    }

    /** Generate magic square test matrix. **/

    public static RawStore magic(final int n) {

        final double[][] M = new double[n][n];

        // Odd order

        if ((n % 2) == 1) {
            final int a = (n + 1) / 2;
            final int b = (n + 1);
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < n; i++) {
                    M[i][j] = (n * ((i + j + a) % n)) + ((i + (2 * j) + b) % n) + 1;
                }
            }

            // Doubly Even Order

        } else if ((n % 4) == 0) {
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < n; i++) {
                    if ((((i + 1) / 2) % 2) == (((j + 1) / 2) % 2)) {
                        M[i][j] = (n * n) - (n * i) - j;
                    } else {
                        M[i][j] = (n * i) + j + 1;
                    }
                }
            }

            // Singly Even Order

        } else {
            final int p = n / 2;
            final int k = (n - 2) / 4;
            final RawStore A = MagicSquareExample.magic(p);
            for (int j = 0; j < p; j++) {
                for (int i = 0; i < p; i++) {
                    final double aij = A.get(i, j);
                    M[i][j] = aij;
                    M[i][j + p] = aij + (2 * p * p);
                    M[i + p][j] = aij + (3 * p * p);
                    M[i + p][j + p] = aij + (p * p);
                }
            }
            for (int i = 0; i < p; i++) {
                for (int j = 0; j < k; j++) {
                    final double t = M[i][j];
                    M[i][j] = M[i + p][j];
                    M[i + p][j] = t;
                }
                for (int j = (n - k) + 1; j < n; j++) {
                    final double t = M[i][j];
                    M[i][j] = M[i + p][j];
                    M[i + p][j] = t;
                }
            }
            double t = M[k][0];
            M[k][0] = M[k + p][0];
            M[k + p][0] = t;
            t = M[k][k];
            M[k][k] = M[k + p][k];
            M[k + p][k] = t;
        }
        return new RawStore(M);
    }

    public static void main(final String argv[]) {

        /*
         * | Tests LU, QR, SVD and symmetric Eig decompositions. | | n = order of magic square. | trace = diagonal sum,
         * should be the magic sum, (n^3 + n)/2. | max_eig = maximum eigenvalue of (A + A')/2, should equal trace. |
         * rank = linear algebraic rank, | should equal n if n is odd, be less than n if n is even. | cond = L_2
         * condition number, ratio of singular values. | lu_res = test of LU factorization, norm1(L*U-A(p,:))/(n*eps). |
         * qr_res = test of QR factorization, norm1(Q*R-A)/(n*eps).
         */

        MagicSquareExample.print("\n    Test of RawStore Class, using magic squares.\n");
        MagicSquareExample.print("    See MagicSquareExample.main() for an explanation.\n");
        MagicSquareExample.print("\n      n     trace       max_eig   rank        cond      lu_res      qr_res\n\n");

        final Date start_time = new Date();
        final double eps = Math.pow(2.0, -52.0);
        for (int n = 3; n <= 32; n++) {
            MagicSquareExample.print(MagicSquareExample.fixedWidthIntegertoString(n, 7));

            final RawStore M = MagicSquareExample.magic(n);

            final int t = (int) M.trace();
            MagicSquareExample.print(MagicSquareExample.fixedWidthIntegertoString(t, 10));

            final JamaEigenvalue E = new JamaEigenvalue(M.plus(M.transpose()).times(0.5));
            final double[] d = E.getRealEigenvalues();
            MagicSquareExample.print(MagicSquareExample.fixedWidthDoubletoString(d[n - 1], 14, 3));

            final int r = M.rank();
            MagicSquareExample.print(MagicSquareExample.fixedWidthIntegertoString(r, 7));

            final double c = M.cond();
            MagicSquareExample.print(c < (1 / eps) ? MagicSquareExample.fixedWidthDoubletoString(c, 12, 3) : "         Inf");

            final JamaLU LU = new JamaLU(M);
            final RawStore L = LU.getL();
            final RawStore U = LU.getU();
            final int[] p = LU.getPivot();
            RawStore R = L.times(U).minus(M.getMatrix(p, 0, n - 1));
            double res = R.norm1() / (n * eps);
            MagicSquareExample.print(MagicSquareExample.fixedWidthDoubletoString(res, 12, 3));

            final JamaQR QR = new JamaQR(M);
            final RawStore Q = QR.getQ();
            R = QR.getR();
            R = Q.times(R).minus(M);
            res = R.norm1() / (n * eps);
            MagicSquareExample.print(MagicSquareExample.fixedWidthDoubletoString(res, 12, 3));

            MagicSquareExample.print("\n");
        }
        final Date stop_time = new Date();
        final double etime = (stop_time.getTime() - start_time.getTime()) / 1000.;
        MagicSquareExample.print("\nElapsed Time = " + MagicSquareExample.fixedWidthDoubletoString(etime, 12, 3) + " seconds\n");
        MagicSquareExample.print("Adios\n");
    }

    /** Shorten spelling of print. **/

    private static void print(final String s) {
        System.out.print(s);
    }
}
