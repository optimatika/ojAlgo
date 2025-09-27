/*
 * Copyright 1997-2025 Optimatika
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
 * translation from SciPy. Implemented here in an unpreconditioned form and using ojAlgo's dense MatrixStore
 * operations for matrix–vector products with A and A^T.
 * <p>
 * When to use:
 * <ul>
 * <li>Nonsymmetric or indefinite systems where ConjugateGradient is not applicable.</li>
 * <li>When you can provide both A·x and A^T·x products and need a robust Krylov method.</li>
 * <li>Prefer over Jacobi/Gauss–Seidel for difficult nonsymmetric problems requiring better convergence.</li>
 * <li>If A^T is unavailable or too costly, consider BiCGSTAB or GMRES (not provided here).</li>
 * </ul>
 * Characteristics:
 * <ul>
 * <li>Requires both A·x and A^T·x products (transpose appears in the recurrences).
 * <li>No preconditioning (M1=M2=I).
 * <li>This version does not work with complex numbers.
 * <li>The stopping criterion follows ojAlgo style: terminate when NumberContext.isSmall(||b||, ||r||).
 * <li>Designed for square systems; Throw IllegalArgumentException for non-square inputs.
 * </ul>
 * References:
 * <ul>
 * <li>SciPy 1.16.1 implementation: scipy.sparse.linalg._isolve.iterative.qmr
 * <li>https://www.netlib.org/templates/templates.pdf, Figure 2.8.
 * (https://github.com/scipy/scipy/blob/0cf8e9541b1a2457992bf4ec2c0c669da373e497/scipy/sparse/linalg/_isolve/iterative.py#L849-L1051)
 * <li>Freund, Roland W., and Noël M. Nachtigal. "QMR: a quasi-minimal residual method for non-Hermitian
 * linear systems." Numerische Mathematik 60 (1991): 315–339. https://doi.org/10.1007/BF01385726
 * </ul>
 */
public final class QMRSolver extends IterativeSolverTask {

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

    private R064Store d;

    private R064Store p;

    private R064Store q;

    private R064Store y;

    private R064Store z;

    public QMRSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> x) {

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, x);
        }

        int m = equations.size();
        int n = x.size();

        int nbIterations = 0;
        int iterationsLimit = this.getIterationsLimit();

        NumberContext accuracy = this.getAccuracyContext();

        // Scratch vectors
        y = IterativeSolverTask.worker(y, n);
        z = IterativeSolverTask.worker(z, n);
        p = IterativeSolverTask.worker(p, n);
        q = IterativeSolverTask.worker(q, n);
        d = IterativeSolverTask.worker(d, n);

        // r = b - A*x and norms
        double normRHS = ZERO;
        double normErr = ZERO;
        for (int i = 0; i < m; i++) {
            Equation row = equations.get(i);
            double bi = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, bi);
            double ri = bi - row.dot(x);
            y.set(row.index, ri);
            z.set(row.index, ri);
            normErr = HYPOT.invoke(normErr, ri);
        }

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, x);
        }

        if (normErr == ZERO) {
            return ZERO;
        }

        // Initialisations (no preconditioning: M1=M2=I)
        double rho = QMRSolver.norm2(y);
        double xi = rho;
        double gamma = ONE;
        double eta = NEG;
        double theta = ZERO;
        double epsilon = ZERO; // dummy init

        do {

            // Convergence check on residual relative to RHS
            if (accuracy.isSmall(normRHS, normErr)) {
                break; // convergence
            }

            if (Math.abs(rho) == ZERO) {
                break; // rho breakdown
            }
            if (Math.abs(xi) == ZERO) {
                break; // xi breakdown
            }

            // v = vtilde / rho ; y = y / rho
            QMRSolver.scaleInPlace(y, ONE / rho);

            // w = wtilde / xi ; z = z / xi
            QMRSolver.scaleInPlace(z, ONE / xi);

            double delta = z.dot(y);
            if (Math.abs(delta) == ZERO) {
                break; // delta breakdown
            }

            // Unpreconditioned: update p and q in-place
            if (nbIterations > 0) {
                // p = y - (xi * delta / epsilon) * p  (in-place)
                double factor = (xi * delta / epsilon);
                QMRSolver.scaleInPlace(p, -factor);
                QMRSolver.axpy(ONE, y, p);
                // q = z - (rho * (delta / epsilon)) * q  (in-place)
                double factor2 = (rho * (delta / epsilon));
                QMRSolver.scaleInPlace(q, -factor2);
                QMRSolver.axpy(ONE, z, q);
            } else {
                p.fillMatching(y);
                q.fillMatching(z);
            }

            // Compute epsilon = q dot (A * p) without allocating ptilde
            double eps = ZERO;
            for (int i = 0; i < m; i++) {
                Equation row = equations.get(i);
                double aip_dot_p = row.dot(p);
                eps += q.doubleValue(row.index) * aip_dot_p;
            }
            epsilon = eps;
            if (Math.abs(epsilon) == ZERO) {
                break; // epsilon breakdown
            }

            double beta = epsilon / delta;
            if (Math.abs(beta) == ZERO) {
                break; // beta breakdown
            }

            // y = (A * p) - beta * y  (in-place, since M1=I); accumulate A*p directly into y
            QMRSolver.scaleInPlace(y, -beta);
            for (int i = 0; i < m; i++) {
                Equation row = equations.get(i);
                double aip_dot_p = row.dot(p);
                y.add(row.index, aip_dot_p);
            }

            double rho_prev = rho;
            rho = QMRSolver.norm2(y);

            // z = -beta * z + A^T * q  (in-place, since M2^T=I)
            QMRSolver.scaleInPlace(z, -beta);
            // accumulate A^T * q directly into z (dense accumulation from rows)
            for (int i = 0; i < m; i++) {
                Equation row = equations.get(i);
                double qi = q.doubleValue(row.index);
                if (qi != ZERO) {
                    for (int j = 0; j < row.size(); j++) {
                        double aij = row.doubleValue(j);
                        if (aij != ZERO) {
                            z.add(j, qi * aij);
                        }
                    }
                }
            }
            xi = QMRSolver.norm2(z);

            double gamma_prev = gamma;
            double theta_prev = theta;
            theta = rho / (gamma_prev * Math.abs(beta));
            gamma = ONE / Math.sqrt(ONE + theta * theta);
            if (Math.abs(gamma) == ZERO) {
                break; // gamma breakdown
            }

            eta *= -(rho_prev / beta) * (gamma / gamma_prev) * (gamma / gamma_prev);

            if (nbIterations > 0) {
                // d = (theta_prev * gamma)^2 * d + eta * p
                QMRSolver.scaleInPlace(d, (theta_prev * gamma) * (theta_prev * gamma));
                QMRSolver.axpy(eta, p, d);
            } else {
                QMRSolver.scaleCopy(p, eta, d);
            }

            // x += d
            QMRSolver.axpy(ONE, d, x);

            // True residual using rows
            normErr = ZERO;
            for (int i = 0; i < m; i++) {
                Equation row = equations.get(i);
                double ri = row.getRHS() - row.dot(x);
                normErr = HYPOT.invoke(normErr, ri);
            }

            nbIterations++;

            if (this.isDebugPrinterSet()) {
                this.debug(nbIterations, normErr / normRHS, x);
            }

        } while (nbIterations < iterationsLimit && !Double.isNaN(normErr) && !accuracy.isSmall(normRHS, normErr));

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }

}