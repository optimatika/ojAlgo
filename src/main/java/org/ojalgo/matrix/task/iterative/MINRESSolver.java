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
 * Minimal Residual (MINRES) solver for symmetric (possibly indefinite) square systems.
 * <p>
 * This class mirrors the public API and internal scratch structure of {@link QMRSolver},
 * but implements the classic MINRES iteration specialised for symmetric matrices.
 * Right-preconditioning is supported using the {@link Preconditioner} API provided by
 * {@link IterativeSolverTask}. A shift parameter is present but fixed to zero in this
 * implementation; it is retained to enable straightforward future extensions.
 * <p>
 * Characteristics
 * <ul>
 * <li>Operates on matrix–vector products with A only (symmetry assumed).
 * <li>Right-preconditioning is applied via the {@link Preconditioner} interface.
 * <li>Stops when the residual norm is small relative to the RHS norm (or absolutely small when RHS is zero),
 * or when the iteration limit is reached.
 * </ul>
 * When to use
 * <ul>
 * <li>For symmetric problems, including indefinite ones, where CG is not applicable.
 * <li>When a robust Krylov method without A^T products is desired.
 * <li>When right-preconditioning is convenient and a symmetric preconditioner is available.
 * </ul>
 * References
 * <ul>
 * <li>Paige, C. C., and M. A. Saunders (1975), Solutions of sparse indefinite systems of linear equations.
 * <li>Numerical Analysis. 12 (4): 617–629. doi:10.1137/0712047.
 * </ul>
 */
public final class MINRESSolver extends IterativeSolverTask {

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

    private R064Store r1; // Lanczos vector storage (previous)
    private R064Store r2; // Lanczos vector storage (current)
    private R064Store v; // Lanczos vector v
    private R064Store y; // Preconditioned vector
    private R064Store w; // Solution update direction (current)
    private R064Store w1; // Solution update direction (previous 1)
    private R064Store w2; // Solution update direction (previous 2)

    public MINRESSolver() {
        super();
    }

    @Override
    public double resolve(final List<Equation> equations, final PhysicalStore<Double> x) {

        final int m = equations.size();
        final int n = x.size();

        final int iterationsLimit = this.getIterationsLimit();
        final NumberContext accuracy = this.getAccuracyContext();

        // Scratch vectors
        r1 = IterativeSolverTask.worker(r1, n);
        r2 = IterativeSolverTask.worker(r2, n);
        v = IterativeSolverTask.worker(v, n);
        y = IterativeSolverTask.worker(y, n);
        w = IterativeSolverTask.worker(w, n);
        w1 = IterativeSolverTask.worker(w1, n);
        w2 = IterativeSolverTask.worker(w2, n);

        // Prepare (right) preconditioner, symmetric recommended for MINRES
        Preconditioner rightPreconditioner = this.getPreconditioner();
        rightPreconditioner.prepare(equations, n);

        // r1 = b - A*x and compute norms
        double normRHS = ZERO;
        double normErr = ZERO;
        for (int i = 0; i < m; i++) {
            final Equation row = equations.get(i);
            final double bi = row.getRHS();
            normRHS = HYPOT.invoke(normRHS, bi);
            final double ri = bi - row.dot(x);
            r1.set(row.index, ri);
            normErr = HYPOT.invoke(normErr, ri);
        }

        if (normRHS == ZERO) {
            x.fillAll(ZERO);
            return ZERO;
        }

        if (this.isDebugPrinterSet()) {
            this.debug(0, (accuracy.isZero(normRHS) ? normErr : normErr / normRHS), x);
        }

        if (normErr == ZERO) {
            return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
        }

        // y = M^{-1} r1
        rightPreconditioner.apply(r1, y);

        double beta1 = r1.dot(y);
        if (beta1 < ZERO) throw new IllegalArgumentException("indefinite preconditioner");
        if (beta1 == ZERO) {
            return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
        }

        beta1 = Math.sqrt(beta1);

        double oldb = ZERO;
        double beta = beta1;
        double dbar = ZERO;
        double epsln = ZERO;
        double phibar = beta1;
        double rhs1 = beta1;
        double rhs2 = ZERO;
        double tnorm2 = ZERO;
        double gmax = ZERO;
        double gmin = Double.MAX_VALUE;
        double cs = NEG;
        double sn = ZERO;

        // initialise recurrence vectors
        w.fillAll(ZERO);
        w1.fillAll(ZERO);
        w2.fillAll(ZERO);
        // r2 = r1
        r2.fillMatching(r1);

        int itn = 0;
        final double shift = ZERO; // keep shift available, for future implementation, but zero
        int istop = 0;
        final double rtol = accuracy.epsilon();

        while (itn < (iterationsLimit > 0 ? iterationsLimit : 5 * n)) {
            itn++;

            // v = y / beta
            scaleCopy(y, ONE / beta, v);

            // y = A*v - shift*v
            for (int i = 0; i < m; i++) {
                final Equation row = equations.get(i);
                final double Avi = row.dot(v);
                y.set(row.index, Avi);
            }
            if (shift != ZERO) axpy(-shift, v, y);

            if (itn >= 2) {
                // y -= (beta/oldb) * r1
                axpy(-(beta / oldb), r1, y);
            }

            final double alfa = v.dot(y);

            // y -= (alfa/beta) * r2
            axpy(-(alfa / beta), r2, y);

            // r1 <- r2 ; r2 <- y switch references to avoid a vector copy
            final R064Store tmpRef = r1; r1 = r2; r2 = tmpRef;
            r2.fillMatching(y);

            // y = M^{-1} r2 (precondition for next step)
            rightPreconditioner.apply(r2, y);

            oldb = beta;
            beta = r2.dot(y);
            if (beta < 0) throw new IllegalArgumentException("non-symmetric matrix / breakdown");
            beta = Math.sqrt(beta);
            tnorm2 += alfa * alfa + oldb * oldb + beta * beta;

            // Previous rotation
            final double oldeps = epsln;
            final double delta = cs * dbar + sn * alfa;
            final double gbar = sn * dbar - cs * alfa;
            epsln = sn * beta;
            dbar = -cs * beta;
            final double root = Math.hypot(gbar, dbar);

            // Next rotation
            double gamma = Math.hypot(gbar, beta);
            gamma = Math.max(gamma, MACHINE_EPSILON);
            cs = gbar / gamma;
            sn = beta / gamma;
            final double phi = cs * phibar;
            phibar = sn * phibar;

            // Update w and x: w = (v - oldeps*w1 - delta*w2) / gamma ; x += phi * w
            w1.fillMatching(w2); // shift: w1 <= previous w2
            w2.fillMatching(w);  // shift: w2 <= previous w
            w.fillMatching(v);   // w = v
            axpy(-oldeps, w1, w);
            axpy(-delta, w2, w);
            scaleInPlace(w, ONE / gamma);

            axpy(phi, w, x);

            // prepare next
            gmax = Math.max(gmax, gamma);
            gmin = Math.min(gmin, gamma);
            final double z = rhs1 / gamma;
            rhs1 = rhs2 - delta * z;
            rhs2 = -epsln * z;

            // Stopping tests – following standard MINRES stopping criteria
            final double Anorm = Math.sqrt(tnorm2);
            final double ynorm = norm2((R064Store) x);
            final double epsx = Anorm * ynorm * MACHINE_EPSILON;
            final double qrnormNow = phibar;
            final double rnorm = qrnormNow;
            final double test1 = (ynorm == 0 || Anorm == 0) ? POSITIVE_INFINITY : rnorm / (Anorm * ynorm);
            final double rootOverAnorm = (Anorm == 0) ? POSITIVE_INFINITY : root / Anorm;
            final double Acond = gmax / gmin;

            if (istop == 0) {
                if (1 + rootOverAnorm <= 1) istop = 2;
                if (1 + test1 <= 1) istop = 1;
                if (itn >= (iterationsLimit > 0 ? iterationsLimit : 5 * n)) istop = 6;
                if (Acond >= 0.1 / MACHINE_EPSILON) istop = 4;
                if (epsx >= beta1) istop = 3;
                if (rootOverAnorm <= rtol) istop = 2;
                if (test1 <= rtol) istop = 1;
            }

            if (istop != 0) break;
        }

        // Compute residual ratio ||b - A x|| / ||b|| using equations (no dense A)
        normErr = ZERO;
        for (int i = 0; i < m; i++) {
            final Equation row = equations.get(i);
            final double bi = row.getRHS();
            final double axi = row.dot(x);
            final double ri = bi - axi;
            normErr = HYPOT.invoke(normErr, ri);
        }

        if (this.isDebugPrinterSet()) {
            this.debug(itn, (accuracy.isZero(normRHS) ? normErr : normErr / normRHS), x);
        }

        return accuracy.isZero(normRHS) ? normErr : normErr / normRHS;
    }
}
