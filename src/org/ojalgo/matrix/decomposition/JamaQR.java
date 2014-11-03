package org.ojalgo.matrix.decomposition;

import org.ojalgo.matrix.store.RawStore;

/**
 * QR Decomposition.
 * <P>
 * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n orthogonal matrix Q and an n-by-n upper
 * triangular matrix R so that A = Q*R.
 * <P>
 * The QR decompostion always exists, even if the matrix does not have full rank, so the constructor will never fail.
 * The primary use of the QR decomposition is in the least squares solution of nonsquare systems of simultaneous linear
 * equations. This will fail if isFullRank() returns false.
 */
class JamaQR implements java.io.Serializable {

    /**
     * Row and column dimensions.
     *
     * @serial column dimension.
     * @serial row dimension.
     */
    private final int m, n;

    /**
     * Array for internal storage of decomposition.
     *
     * @serial internal array storage.
     */
    private final double[][] QR;

    /**
     * Array for internal storage of diagonal of R.
     *
     * @serial diagonal of R.
     */
    private final double[] Rdiag;

    /**
     * QR Decomposition, computed by Householder reflections. Structure to access R and the Householder vectors and
     * compute Q.
     *
     * @param A Rectangular matrix
     */
    JamaQR(final RawStore A) {
        // Initialize.
        QR = A.copyOfData();
        m = (int) A.countRows();
        n = (int) A.countColumns();
        Rdiag = new double[n];

        // Main loop.
        for (int k = 0; k < n; k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < m; i++) {
                nrm = Maths.hypot(nrm, QR[i][k]);
            }

            if (nrm != 0.0) {
                // Form k-th Householder vector.
                if (QR[k][k] < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    QR[i][k] /= nrm;
                }
                QR[k][k] += 1.0;

                // Apply transformation to remaining columns.
                for (int j = k + 1; j < n; j++) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k] * QR[i][j];
                    }
                    s = -s / QR[k][k];
                    for (int i = k; i < m; i++) {
                        QR[i][j] += s * QR[i][k];
                    }
                }
            }
            Rdiag[k] = -nrm;
        }
    }

    /**
     * Return the Householder vectors
     *
     * @return Lower trapezoidal matrix whose columns define the reflections
     */
    RawStore getH() {
        final RawStore X = new RawStore(m, n);
        final double[][] H = X.data;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i >= j) {
                    H[i][j] = QR[i][j];
                } else {
                    H[i][j] = 0.0;
                }
            }
        }
        return X;
    }

    /**
     * Generate and return the (economy-sized) orthogonal factor
     *
     * @return Q
     */
    RawStore getQ() {
        final RawStore X = new RawStore(m, n);
        final double[][] Q = X.data;
        for (int k = n - 1; k >= 0; k--) {
            for (int i = 0; i < m; i++) {
                Q[i][k] = 0.0;
            }
            Q[k][k] = 1.0;
            for (int j = k; j < n; j++) {
                if (QR[k][k] != 0) {
                    double s = 0.0;
                    for (int i = k; i < m; i++) {
                        s += QR[i][k] * Q[i][j];
                    }
                    s = -s / QR[k][k];
                    for (int i = k; i < m; i++) {
                        Q[i][j] += s * QR[i][k];
                    }
                }
            }
        }
        return X;
    }

    /**
     * Return the upper triangular factor
     *
     * @return R
     */
    RawStore getR() {
        final RawStore X = new RawStore(n, n);
        final double[][] R = X.data;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < j) {
                    R[i][j] = QR[i][j];
                } else if (i == j) {
                    R[i][j] = Rdiag[i];
                } else {
                    R[i][j] = 0.0;
                }
            }
        }
        return X;
    }

    /**
     * Is the matrix full rank?
     *
     * @return true if R, and hence A, has full rank.
     */
    boolean isFullRank() {
        for (int j = 0; j < n; j++) {
            if (Rdiag[j] == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Least squares solution of A*X = B
     *
     * @param B A RawStore with as many rows as A and any number of columns.
     * @return X that minimizes the two norm of Q*R*X-B.
     * @exception IllegalArgumentException RawStore row dimensions must agree.
     * @exception RuntimeException RawStore is rank deficient.
     */
    RawStore solve(final RawStore B) {
        if ((int) B.countRows() != m) {
            throw new IllegalArgumentException("RawStore row dimensions must agree.");
        }
        if (!this.isFullRank()) {
            throw new RuntimeException("RawStore is rank deficient.");
        }

        // Copy right hand side
        final int nx = (int) B.countColumns();
        final double[][] X = B.copyOfData();

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                double s = 0.0;
                for (int i = k; i < m; i++) {
                    s += QR[i][k] * X[i][j];
                }
                s = -s / QR[k][k];
                for (int i = k; i < m; i++) {
                    X[i][j] += s * QR[i][k];
                }
            }
        }
        // Solve R*X = Y;
        for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= Rdiag[k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j] * QR[i][k];
                }
            }
        }
        return (new RawStore(X, n, nx).getMatrix(0, n - 1, 0, nx - 1));
    }
}
