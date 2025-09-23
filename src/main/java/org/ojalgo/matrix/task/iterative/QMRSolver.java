/*
 * Copyright 1997-2025 Optimatika
 */
package org.ojalgo.matrix.task.iterative;

import org.ojalgo.equation.Equation;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
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
 * - https://www.netlib.org/templates/templates.pdf, Figure 2.8.
 * (https://github.com/scipy/scipy/blob/0cf8e9541b1a2457992bf4ec2c0c669da373e497/scipy/sparse/linalg/_isolve/iterative.py#L849-L1051)
 * - Freund, Roland W., and Noël M. Nachtigal. "QMR: a quasi-minimal residual method for non-Hermitian linear systems."
 *   Numerische Mathematik 60 (1991): 315–339. https://doi.org/10.1007/BF01385726
 */
public final class QMRSolver extends KrylovSubspaceSolver implements IterativeSolverTask.SparseDelegate {

    private R064Store y;
    private R064Store z;
    private R064Store p;
    private R064Store q;
    private R064Store d;

    public QMRSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> x) {

        int n = equations.size();
        int xSize = x.getRowDim();

        // Scratch vectors
        if (y == null || y.getRowDim() != xSize) {
            y = R064Store.FACTORY.make(xSize, 1);
            z = R064Store.FACTORY.make(xSize, 1);
            p = R064Store.FACTORY.make(xSize, 1);
            q = R064Store.FACTORY.make(xSize, 1);
            d = R064Store.FACTORY.make(xSize, 1);
        }

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
            y.set(row.index, ri);
            z.set(row.index, ri);
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
        double rho = norm2(y);
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
            scaleInPlace(y, 1.0 / rho);

            // w = wtilde / xi ; z = z / xi
            scaleInPlace(z, 1.0 / xi);

            double delta = z.dot(y);
            if (Math.abs(delta) == 0.0) {
                break; // delta breakdown
            }

            // Unpreconditioned: update p and q in-place
            if (iterations > 0) {
                // p = y - (xi * delta / epsilon) * p  (in-place)
                double factor = (xi * delta / epsilon);
                scaleInPlace(p, -factor);
                axpy(1.0, y, p);
                // q = z - (rho * (delta / epsilon)) * q  (in-place)
                double factor2 = (rho * (delta / epsilon));
                scaleInPlace(q, -factor2);
                axpy(1.0, z, q);
            } else {
                p.fillMatching(y);
                q.fillMatching(z);
            }

            // Compute epsilon = q dot (A * p) without allocating ptilde
            double eps = 0.0;
            for (int i = 0; i < n; i++) {
                Equation row = equations.get(i);
                double aip_dot_p = row.dot(p);
                eps += q.doubleValue(row.index) * aip_dot_p;
            }
            epsilon = eps;
            if (Math.abs(epsilon) == 0.0) {
                break; // epsilon breakdown
            }

            double beta = epsilon / delta;
            if (Math.abs(beta) == 0.0) {
                break; // beta breakdown
            }

            // y = (A * p) - beta * y  (in-place, since M1=I); accumulate A*p directly into y
            scaleInPlace(y, -beta);
            for (int i = 0; i < n; i++) {
                Equation row = equations.get(i);
                double aip_dot_p = row.dot(p);
                y.add(row.index, aip_dot_p);
            }

            double rho_prev = rho;
            rho = norm2(y);

            // z = -beta * z + A^T * q  (in-place, since M2^T=I)
            scaleInPlace(z, -beta);
            // accumulate A^T * q directly into z (dense accumulation from rows)
            for (int i = 0; i < n; i++) {
                Equation row = equations.get(i);
                double qi = q.doubleValue(row.index);
                if (qi != 0.0) {
                    for (int j = 0; j < row.size(); j++) {
                        double aij = row.doubleValue(j);
                        if (aij != 0.0) {
                            z.add(j, qi * aij);
                        }
                    }
                }
            }
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
            } else {
                scaleCopy(p, eta, d);
            }

            // x += d
            axpy(1.0, d, x);

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
