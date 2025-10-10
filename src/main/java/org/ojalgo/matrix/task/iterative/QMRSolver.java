/*
 * Copyright 1997-2025 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.List;

import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.context.NumberContext;

/**
 * Quasi-Minimal Residual (QMR) solver for general nonsymmetric square systems.
 * <p>
 * This is a Java port of SciPy's qmr() reference implementation. It is almost a straight line-by-line
 * translation from SciPy. Implemented here with right-preconditioning only (M1 = I, M2 != I), and using
 * ojAlgo's dense MatrixStore operations for matrix–vector products with A and A^T.
 * <p>
 * Characteristics
 * <ul>
 * <li>Operates on matrix–vector products with both A and A^T.
 * <li>Right-preconditioning is used; both forward and transpose applications of the preconditioner may be
 * invoked.
 * <li>Stops when the residual norm is small relative to the RHS norm (or absolutely small when RHS is zero),
 * or when the iteration limit is reached.
 * </ul>
 * When to use
 * <ul>
 * <li>For nonsymmetric or indefinite problems where SPD-specific methods are inapplicable.
 * <li>When a robust Krylov method is preferred over simple stationary iterations.
 * <li>If A^T is unavailable or too costly, consider alternatives that avoid explicit transpose products.
 * </ul>
 * References
 * <ul>
 * <li>Templates for the Solution of Linear Systems, Barrett et al., Figure 2.8.
 * <li>Freund & Nachtigal (1991), QMR: a quasi-minimal residual method for non-Hermitian linear systems.
 * </ul>
 */
public final class QMRSolver extends IterativeSolverTask {

    private static void axpby(final double alpha, final R064Store src, final double beta, final R064Store dst) {
        for (int i = 0; i < src.getRowDim(); i++) {
            dst.set(i, alpha * src.doubleValue(i) + beta * dst.doubleValue(i));
        }
    }

    private static void axpy(final double alpha, final R064Store x, final Mutate1D.Modifiable<?> y) {
        x.axpy(alpha, y);
    }

    private static double norm2(final R064Store a) {
        double n = ZERO;
        for (int i = 0; i < a.getRowDim(); i++) {
            n = HYPOT.invoke(n, a.doubleValue(i));
        }
        return n;
    }

    private static void scaleCopy(final R064Store src, final double alpha, final R064Store dst) {
        for (int i = 0; i < src.getRowDim(); i++) {
            dst.set(i, alpha * src.doubleValue(i));
        }
    }

    private static void scaleInPlace(final R064Store x, final double alpha) {
        x.modifyAll(MULTIPLY.by(alpha));
    }

    private R064Store r; // residual r
    private R064Store vtilde; // v~
    private R064Store wtilde; // w~
    private R064Store v; // v
    private R064Store z; // z
    private R064Store p; // p
    private R064Store q; // q
    private R064Store ptilde; // A p
    private R064Store d; // d (solution direction accumulation)
    private R064Store s; // s (residual update accumulation)

    public QMRSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> x) {

        final int m = equations.size();
        final int n = x.size();

        int nbIterations = 0;
        final int iterationsLimit = this.getIterationsLimit();

        final NumberContext accuracy = this.getAccuracyContext();

        // Scratch vectors
        r = IterativeSolverTask.worker(r, n);
        vtilde = IterativeSolverTask.worker(vtilde, n);
        wtilde = IterativeSolverTask.worker(wtilde, n);
        v = IterativeSolverTask.worker(v, n);
        z = IterativeSolverTask.worker(z, n);
        p = IterativeSolverTask.worker(p, n);
        q = IterativeSolverTask.worker(q, n);
        ptilde = IterativeSolverTask.worker(ptilde, n);
        d = IterativeSolverTask.worker(d, n);
        s = IterativeSolverTask.worker(s, n);

        // Prepare preconditioner (right-only)
        final Preconditioner rightPreconditioner = this.getPreconditioner();
        rightPreconditioner.prepare(equations, n);

        // r = b - A*x  and accumulate norms
        double normRHS = ZERO; // ||b||
        double normErr = ZERO; // ||r||
        for (int i = 0; i < m; i++) {
            final Equation row = equations.get(i);
            final double bi = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, bi);
            // ri = bi - (A_i * x)
            final double ri = bi - row.dot(x); // matrix–vector product A*x (row dot x)
            r.set(row.index, ri);
            vtilde.set(row.index, ri);
            wtilde.set(row.index, ri);
            // normErr = hypot(normErr, ri)
            normErr = HYPOT.invoke(normErr, ri);
        }

        if (this.isDebugPrinterSet()) {
            this.debug(nbIterations, normErr / normRHS, x);
        }

        if (normErr == ZERO) {
            return ZERO;
        }

        // y = vtilde (M1 = I)  and  z = M2^{-T} wtilde
        rightPreconditioner.applyTranspose(wtilde, z); // z = (M2^T)^{-1} * wtilde

        // rho = ||v~|| ; xi = ||z||
        double rho = QMRSolver.norm2(vtilde);
        double xi = QMRSolver.norm2(z);
        double gamma = ONE;
        double eta = NEG;
        double theta = ZERO;
        double epsilon = ONE; // dummy init

        do {

            // Convergence check on residual relative to RHS
            if (accuracy.isSmall(normRHS, normErr)) {
                break; // converged
            }

            if (Math.abs(rho) == ZERO) {
                break; // rho breakdown
            }
            if (Math.abs(xi) == ZERO) {
                break; // xi breakdown
            }

            // v = vtilde / rho
            QMRSolver.scaleCopy(vtilde, ONE / rho, v);

            // z = z / xi
            QMRSolver.scaleInPlace(z, ONE / xi); // z *= 1/xi

            // delta = z^T v  (since y = v when M1 = I)
            final double delta = z.dot(v);
            if (Math.abs(delta) == ZERO) {
                break; // delta breakdown
            }

            // ytilde = M2^{-1} * v  (since y = v when M1 = I)
            rightPreconditioner.apply(v, vtilde); //reuse vtilde as ytilde

            if (nbIterations > 0) {
                // p = ytilde - (xi * delta / epsilon) * p
                QMRSolver.axpby(ONE, vtilde, -(xi * delta / epsilon), p);
                // q = z - (rho * (delta / epsilon)) * q
                QMRSolver.axpby(ONE, z, -(rho * (delta / epsilon)), q); // q += z
            } else {
                // First iteration initialisation
                p.fillMatching(vtilde); // p = ytilde (reused vtilde)
                q.fillMatching(z); // q = z
            }

            // ptilde = A * p  (matrix–vector product)
            for (int i = 0; i < m; i++) {
                final Equation row = equations.get(i);
                final double aip_dot_p = row.dot(p); // row dot p
                ptilde.set(row.index, aip_dot_p);
            }

            // epsilon = q^T ptilde  (dot product)
            epsilon = q.dot(ptilde);
            if (Math.abs(epsilon) == ZERO) {
                break; // epsilon breakdown
            }

            final double beta = epsilon / delta;
            if (Math.abs(beta) == ZERO) {
                break; // beta breakdown
            }

            // vtilde = ptilde - beta * v  (in-place into vtilde)
            vtilde.fillMatching(ptilde); // vtilde = ptilde
            QMRSolver.axpy(-beta, v, vtilde); // vtilde += (-beta) * v

            // rho update: rho = ||v~||
            final double rho_prev = rho;
            rho = QMRSolver.norm2(vtilde);

            // wtilde = (-beta/xi) * wtilde + A^T * q  (compute into wtilde)
            QMRSolver.scaleInPlace(wtilde, (-beta / xi));
            // A^T * q accumulation using row structure
            for (int i = 0; i < m; i++) {
                final Equation row = equations.get(i);
                final double qi = q.doubleValue(row.index);
                if (qi != ZERO) {
                    // z_j += qi * a_ij  (accumulate into wtilde as A^T*q)
                    for (int j = 0; j < row.size(); j++) {
                        final double aij = row.doubleValue(j);
                        if (aij != ZERO) {
                            wtilde.add(j, qi * aij); // wtilde_j += qi * a_ij
                        }
                    }
                }
            }

            // z = (M2^T)^{-1} * wtilde
            rightPreconditioner.applyTranspose(wtilde, z);

            // xi update: xi = ||z||
            xi = QMRSolver.norm2(z);

            // theta/gamma updates
            final double gamma_prev = gamma;
            final double theta_prev = theta;
            theta = rho / (gamma_prev * Math.abs(beta));
            gamma = ONE / Math.sqrt(ONE + theta * theta);
            if (Math.abs(gamma) == ZERO) {
                break; // gamma breakdown
            }

            eta *= -(rho_prev / beta) * (gamma / gamma_prev) * (gamma / gamma_prev);

            if (nbIterations > 0) {
                // d = (theta_prev * gamma)^2 * d + eta * p
                double factor = (theta_prev * gamma) * (theta_prev * gamma);
                QMRSolver.axpby(eta, p, factor, d);

                // s = (theta_prev * gamma)^2 * s + eta * ptilde
                QMRSolver.axpby(eta, ptilde, factor, s);
            } else {
                // d = eta * p ; s = eta * ptilde
                QMRSolver.scaleCopy(p, eta, d);
                QMRSolver.scaleCopy(ptilde, eta, s);
            }

            // x += d  (solution update)
            QMRSolver.axpy(ONE, d, x);

            // r -= s  (residual update)
            QMRSolver.axpy(NEG, s, r);

            // Recompute residual norm for convergence check: ||r||
            normErr = QMRSolver.norm2(r);

            nbIterations++;
            if (this.isDebugPrinterSet()) {
                this.debug(nbIterations, normErr / normRHS, x);
            }

        } while (nbIterations < iterationsLimit && !Double.isNaN(normErr));
        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }
}