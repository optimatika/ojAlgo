package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.RawStore;

/**
 * LU Decomposition.
 * <P>
 * For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n unit lower triangular matrix L, an n-by-n upper
 * triangular matrix U, and a permutation vector piv of length m so that A(piv,:) = L*U. If m < n, then L is m-by-m and
 * U is m-by-n.
 * <P>
 * The LU decompostion with pivoting always exists, even if the matrix is singular, so the constructor will never fail.
 * The primary use of the LU decomposition is in the solution of square systems of simultaneous linear equations. This
 * will fail if isNonsingular() returns false.
 */
class JamaLU implements java.io.Serializable {

    /**
     * Array for internal storage of decomposition.
     *
     * @serial internal array storage.
     */
    private final double[][] LU;

    /**
     * Row and column dimensions, and pivot sign.
     *
     * @serial column dimension.
     * @serial row dimension.
     * @serial pivot sign.
     */
    private final int m, n, d;

    /**
     * Internal storage of pivot vector.
     *
     * @serial pivot vector.
     */
    private final int[] piv;

    private int pivsign;

    /*
     * ------------------------ Temporary, experimental code. ------------------------ *\ \** LU Decomposition, computed
     * by Gaussian elimination. <P> This constructor computes L and U with the "daxpy"-based elimination algorithm used
     * in LINPACK and MATLAB. In Java, we suspect the dot-product, Crout algorithm will be faster. We have temporarily
     * included this constructor until timing experiments confirm this suspicion. <P>
     * @param A Rectangular matrix
     * @param linpackflag Use Gaussian elimination. Actual value ignored.
     * @return Structure to access L, U and piv.\ LUDecomposition (RawStore A, int linpackflag) { // Initialize. LU =
     * A.getArrayCopy(); m = A.getRowDimension(); n = A.getColumnDimension(); piv = new int[m]; for (int i = 0; i < m;
     * i++) { piv[i] = i; } pivsign = 1; // Main loop. for (int k = 0; k < n; k++) { // Find pivot. int p = k; for (int
     * i = k+1; i < m; i++) { if (Math.abs(LU[i][k]) > Math.abs(LU[p][k])) { p = i; } } // Exchange if necessary. if (p
     * != k) { for (int j = 0; j < n; j++) { double t = LU[p][j]; LU[p][j] = LU[k][j]; LU[k][j] = t; } int t = piv[p];
     * piv[p] = piv[k]; piv[k] = t; pivsign = -pivsign; } // Compute multipliers and eliminate k-th column. if (LU[k][k]
     * != 0.0) { for (int i = k+1; i < m; i++) { LU[i][k] /= LU[k][k]; for (int j = k+1; j < n; j++) { LU[i][j] -=
     * LU[i][k]*LU[k][j]; } } } } } \* ------------------------ End of temporary code. ------------------------
     */

    /*
     * ------------------------ Public Methods ------------------------
     */

    /**
     * LU Decomposition Structure to access L, U and piv.
     *
     * @param A Rectangular matrix
     */
    JamaLU(final RawStore A) {

        // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

        LU = A.copyOfData();
        m = (int) A.countRows();
        n = (int) A.countColumns();
        d = Math.min(m, n);
        piv = new int[m];
        for (int i = 0; i < m; i++) {
            piv[i] = i;
        }
        pivsign = 1;
        double[] LUrowi;
        final double[] LUcolj = new double[m];

        // Outer loop.

        for (int j = 0; j < n; j++) {

            // Make a copy of the j-th column to localize references.

            for (int i = 0; i < m; i++) {
                LUcolj[i] = LU[i][j];
            }

            // Apply previous transformations.

            for (int i = 0; i < m; i++) {
                LUrowi = LU[i];

                // Most of the time is spent in the following dot product.

                final int kmax = Math.min(i, j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LUrowi[k] * LUcolj[k];
                }

                LUrowi[j] = LUcolj[i] -= s;
            }

            // Find pivot and exchange if necessary.

            int p = j;
            for (int i = j + 1; i < m; i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                    p = i;
                }
            }
            if (p != j) {
                for (int k = 0; k < n; k++) {
                    final double t = LU[p][k];
                    LU[p][k] = LU[j][k];
                    LU[j][k] = t;
                }
                final int k = piv[p];
                piv[p] = piv[j];
                piv[j] = k;
                pivsign = -pivsign;
            }

            // Compute multipliers.

            if ((j < m) && (LU[j][j] != 0.0)) {
                for (int i = j + 1; i < m; i++) {
                    LU[i][j] /= LU[j][j];
                }
            }
        }
    }

    /**
     * Determinant
     *
     * @return det(A)
     * @exception IllegalArgumentException RawStore must be square
     */
    double det() {
        if (m != n) {
            throw new IllegalArgumentException("RawStore must be square.");
        }
        double d = pivsign;
        for (int j = 0; j < n; j++) {
            d *= LU[j][j];
        }
        return d;
    }

    /**
     * Return pivot permutation vector as a one-dimensional double array
     *
     * @return (double) piv
     */
    double[] getDoublePivot() {
        final double[] vals = new double[m];
        for (int i = 0; i < m; i++) {
            vals[i] = piv[i];
        }
        return vals;
    }

    /**
     * Return lower triangular factor
     *
     * @return L
     */
    RawStore getL() {
        final RawStore X = new RawStore(m, d);
        final double[][] L = X.data;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < d; j++) {
                if (i > j) {
                    L[i][j] = LU[i][j];
                } else if (i == j) {
                    L[i][j] = 1.0;
                } else {
                    L[i][j] = 0.0;
                }
            }
        }
        return X;
    }

    /**
     * Return pivot permutation vector
     *
     * @return piv
     */
    int[] getPivot() {
        final int[] p = new int[m];
        for (int i = 0; i < m; i++) {
            p[i] = piv[i];
        }
        return p;
    }

    /**
     * Return upper triangular factor
     *
     * @return U
     */
    RawStore getU() {
        final RawStore X = new RawStore(d, n);
        final double[][] U = X.data;
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < n; j++) {
                if (i <= j) {
                    U[i][j] = LU[i][j];
                } else {
                    U[i][j] = 0.0;
                }
            }
        }
        return X;
    }

    /**
     * Is the matrix nonsingular?
     *
     * @return true if U, and hence A, is nonsingular.
     */
    boolean isNonsingular() {
        for (int j = 0; j < n; j++) {
            if (LU[j][j] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solve A*X = B
     *
     * @param B A RawStore with as many rows as A and any number of columns.
     * @return X so that L*U*X = B(piv,:)
     * @exception IllegalArgumentException RawStore row dimensions must agree.
     * @exception RuntimeException RawStore is singular.
     */
    RawStore solve(final RawStore B) {
        if ((int) B.countRows() != m) {
            throw new IllegalArgumentException("RawStore row dimensions must agree.");
        }
        if (!this.isNonsingular()) {
            throw new RuntimeException("RawStore is singular.");
        }

        // Copy right hand side with pivoting
        final int nx = (int) B.countColumns();
        final RawStore Xmat = B.getMatrix(piv, 0, nx - 1);
        final double[][] X = Xmat.data;

        // Solve L*Y = B(piv,:)
        for (int k = 0; k < n; k++) {
            for (int i = k + 1; i < n; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * LU[i][k];
                }
            }
        }
        // Solve U*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= LU[k][k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * LU[i][k];
                }
            }
        }
        return Xmat;
    }
}
