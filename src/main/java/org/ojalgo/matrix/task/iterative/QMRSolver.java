/*
 * Copyright 1997-2025 Optimatika
 */
package org.ojalgo.matrix.task.iterative;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate1D;
import org.ojalgo.type.context.NumberContext;

import java.util.List;

import static org.ojalgo.function.constant.PrimitiveMath.*;

/**
 * Quasi-Minimal Residual (QMR) solver for general nonsymmetric square systems.
 *
 * This is a Java port of SciPy's qmr() reference implementation.
 * It is almost a straight line-by-line translation from SciPy.
 * Implemented here in an unpreconditioned form and using ojAlgo's dense MatrixStore operations
 * for matrix–vector products with A and A^T.
 *
 * Characteristics:
 * - Requires both A·x and A^T·x products (transpose appears in the recurrences).
 * - No preconditioning (M1=M2=I).
 * - This version does not work with complex numbers.
 * - The stopping criterion follows ojAlgo style: terminate when NumberContext.isSmall(||b||, ||r||).
 * - Designed for square systems; Throw IllegalArgumentException for non-square inputs.
 *
 * References:
 * - SciPy 1.16.1 implementation: scipy.sparse.linalg._isolve.iterative.qmr
 * (https://github.com/scipy/scipy/blob/0cf8e9541b1a2457992bf4ec2c0c669da373e497/scipy/sparse/linalg/_isolve/iterative.py#L849-L1051)
 * - Freund, Roland W., and Noël M. Nachtigal. "QMR: a quasi-minimal residual method for non-Hermitian linear systems."
 *   Numerische Mathematik 60 (1991): 315–339. https://doi.org/10.1007/BF01385726
 */
public final class QMRSolver extends KrylovSubspaceSolver implements IterativeSolverTask.SparseDelegate {

    public QMRSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> x) {

        final int n = equations.size();

        // Scratch vectors
        R064Store r = R064Store.FACTORY.make(n, 1);
        R064Store vtilde = R064Store.FACTORY.make(n, 1);
        R064Store wtilde = R064Store.FACTORY.make(n, 1);
        R064Store v = R064Store.FACTORY.make(n, 1);
        R064Store w = R064Store.FACTORY.make(n, 1);
        R064Store y = R064Store.FACTORY.make(n, 1);
        R064Store z = R064Store.FACTORY.make(n, 1);
        R064Store p = R064Store.FACTORY.make(n, 1);
        R064Store q = R064Store.FACTORY.make(n, 1);
        R064Store d = R064Store.FACTORY.make(n, 1);
        R064Store s = R064Store.FACTORY.make(n, 1);
        R064Store ytilde = R064Store.FACTORY.make(n, 1);
        R064Store ztilde = R064Store.FACTORY.make(n, 1);
        R064Store ptilde = R064Store.FACTORY.make(n, 1);
        R064Store ATq = R064Store.FACTORY.make(n, 1);

        final NumberContext accuracy = this.getAccuracyContext();
        final int limit = this.getIterationsLimit();

        // r = b - A*x and norms
        double normRHS = ZERO;
        double normErr = ZERO;
        for (int i = 0; i < n; i++) {
            Equation row = equations.get(i);
            double bi = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, bi);
            double ri = bi - row.dot(x);
            r.set(i, ri);
            normErr = HYPOT.invoke(normErr, ri);
        }
// When normRHS is small, use absolute error instead of relative error.
        normRHS = Math.max(normRHS, ONE);

        if (this.isDebugPrinterSet()) {
            this.debug(0, NaN, x);
        }

        if (normErr == 0.0) {
            return 0.0;
        }

        // Initialisations (no preconditioning: M1=M2=I)
        vtilde.fillMatching(r);
        y.fillMatching(r);
        double rho = norm2(r);
        wtilde.fillMatching(r);
        z.fillMatching(r);
        double xi = rho;
        double gamma = 1.0;
        double eta = -1.0;
        double theta = 0.0;
        double epsilon = 0.0; // dummy init

        int iterations = 0;

        while (iterations < limit) {

            // Convergence check on residual relative to RHS
            if (accuracy.isSmall(normRHS, normErr)) {
                break; // convergence
            }

            if (Math.abs(rho) == 0.0) {
                break; // rho breakdown
            }
            if (Math.abs(xi) == 0.0) {
                break; // xi breakdown
            }

            // v = vtilde / rho ; y = y / rho
            scaleCopy(vtilde, 1.0 / rho, v);
            scaleInPlace(y, 1.0 / rho);

            // w = wtilde / xi ; z = z / xi
            scaleCopy(wtilde, 1.0 / xi, w);
            scaleInPlace(z, 1.0 / xi);

            double delta = z.dot(y);
            if (Math.abs(delta) == 0.0) {
                break; // delta breakdown
            }

            // Unpreconditioned: ytilde = y ; ztilde = z
            ztilde.fillMatching(z);
            ytilde.fillMatching(y);
            if (iterations > 0) {
                // ytilde -= (xi * delta / epsilon) * p
                double factor = (xi * delta / epsilon);
                axpy(-factor, p, ytilde);
                p.fillMatching(ytilde);
                // ztilde -= (rho * (delta / epsilon)) * q
                double factor2 = (rho * (delta / epsilon));
                axpy(-factor2, q, ztilde);
                q.fillMatching(ztilde);
            } else {
                p.fillMatching(ytilde);
                q.fillMatching(ztilde);
            }

            // ptilde = A * p
            for (int i = 0; i < n; i++) {
                ptilde.set(i, equations.get(i).dot(p));
            }

            epsilon = q.dot(ptilde);
            if (Math.abs(epsilon) == 0.0) {
                break; // epsilon breakdown
            }

            double beta = epsilon / delta;
            if (Math.abs(beta) == 0.0) {
                break; // beta breakdown
            }

            // vtilde = ptilde - beta * v
            vtilde.fillMatching(ptilde);
            axpy(-beta, v, vtilde);
            // y = vtilde (since M1=I)
            y.fillMatching(vtilde);

            double rho_prev = rho;
            rho = norm2(y);

            // wtilde = -beta * w + A^T * q
            scaleCopy(w, -beta, wtilde);
            // ATq = A^T * q (dense accumulation from rows)
            ATq.fillAll(0.0);
            for (int i = 0; i < n; i++) {
                double qi = q.doubleValue(i);
                if (qi == 0.0) continue;
                double[] coeffs = equations.get(i).getCoefficients();
                for (int j = 0; j < coeffs.length; j++) {
                    double aij = coeffs[j];
                    if (aij != 0.0) {
                        ATq.add(j, 0L, qi * aij);
                    }
                }
            }
            axpy(1.0, ATq, wtilde);
            // z = wtilde (since M2^T=I)
            z.fillMatching(wtilde);
            xi = norm2(z);

            double gamma_prev = gamma;
            double theta_prev = theta;
            theta = rho / (gamma_prev * Math.abs(beta));
            gamma = 1.0 / Math.sqrt(1.0 + theta * theta);
            if (Math.abs(gamma) == 0.0) {
                break; // gamma breakdown
            }

            eta *= -(rho_prev / beta) * (gamma / gamma_prev) * (gamma / gamma_prev);

            if (iterations > 0) {
                // d = (theta_prev * gamma)^2 * d + eta * p
                scaleInPlace(d, (theta_prev * gamma) * (theta_prev * gamma));
                axpy(eta, p, d);
                // s = (theta_prev * gamma)^2 * s + eta * ptilde
                scaleInPlace(s, (theta_prev * gamma) * (theta_prev * gamma));
                axpy(eta, ptilde, s);
            } else {
                scaleCopy(p, eta, d);
                scaleCopy(ptilde, eta, s);
            }

            // x += d ; r -= s
            axpy(1.0, d, x);
            axpy(-1.0, s, r);

            // True residual using rows
            normErr = ZERO;
            for (int i = 0; i < n; i++) {
                Equation row = equations.get(i);
                double ri = row.getRHS() - row.dot(x);
                normErr = HYPOT.invoke(normErr, ri);
            }

            iterations++;
            if (this.isDebugPrinterSet()) {
                this.debug(iterations, normErr / normRHS, x);
            }
        }

        return normErr / normRHS;
    }

    @Override
    public MatrixStore<Double> solve(final Access2D<?> body, final Access2D<?> rhs, final PhysicalStore<Double> preallocated) throws RecoverableCondition {

        List<Equation> equations = IterativeSolverTask.toListOfRows(body, rhs);

        this.resolve(equations, preallocated);

        return preallocated;
    }

    // --- small helpers on R064Store vectors ---

    private static double norm2(final R064Store a) {
        double n = 0.0;
        for (int i = 0; i < a.getRowDim(); i++) {
            n = HYPOT.invoke(n, a.doubleValue(i));
        }
        return n;
    }

    private static void axpy(final double alpha, final R064Store x, final Mutate1D.Modifiable<?> y) {
        x.axpy(alpha, y);
    }

    private static void scaleInPlace(final R064Store x, final double alpha) {
        x.modifyAll(PrimitiveMath.MULTIPLY.by(alpha));
    }

    private static void scaleCopy(final R064Store src, final double alpha, final R064Store dst) {
        for (int i = 0; i < src.getRowDim(); i++) {
            dst.set(i, alpha * src.doubleValue(i));
        }
    }
}
