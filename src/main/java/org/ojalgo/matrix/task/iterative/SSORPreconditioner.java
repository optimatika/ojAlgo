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
import org.ojalgo.structure.Access1D;

/**
 * Symmetric Successive Over-Relaxation (SSOR) preconditioner with relaxation factor omega (ω).
 * <p>
 * For ω = 1 this reduces to a symmetric Gauss–Seidel (forward + backward) sweep – often an effective
 * preconditioner for SPD systems with suitable variable ordering.
 * <p>
 * The (left) preconditioning effect applied is an (approximate) application of M^{-1} where (classic form):
 *
 * <pre>
 *   M = (D/ω + L) D^{-1} (D/ω + U) * (2 - ω)/ω
 * </pre>
 *
 * D is the diagonal of A, L (strictly) lower and U (strictly) upper parts. Application performs:
 * <ol>
 * <li>Forward substitution: (D/ω + L) y = r
 * <li>Scale: d = D y
 * <li>Backward substitution: (D/ω + U) z = d
 * <li>z = (ω / (2 - ω)) * z
 * </ol>
 * For ω = 1 the scalings simplify and the implementation elides redundant multiplies.
 * <p>
 * Notes & limitations:
 * <ul>
 * <li>Assumes the system matrix is represented by {@link Equation} rows where {@code row.index} gives the
 * pivot/diagonal position and {@link Equation#getPivot()} returns the diagonal element a_{ii}.
 * <li>Iteration over the full column span (row.size()) – suitable for reasonably sparse (or modest sized)
 * problems; otherwise consider a specialised sparse structure.
 * <li>No reordering or scaling heuristics; those should be performed externally if desired.
 * <li>If a diagonal pivot is zero it is treated as 1.0 (effectively skipping that row in the factor solve).
 * </ul>
 */
public final class SSORPreconditioner implements Preconditioner {

    private double myOmega = ONE; // relaxation factor ω (0 < ω < 2), ω=1 -> symmetric Gauss–Seidel
    private transient R064Store myDiag = null; // diagonal D
    private transient R064Store myWorkY = null; // forward solution y (reused also as d = D y)
    private transient R064Store myWorkZ = null; // backward solution / output z (may alias destination)
    private transient List<Equation> myEquations = null;
    private int myDim = 0;

    public SSORPreconditioner() {
        super();
    }

    @Override
    public void apply(final Access1D<Double> src, final PhysicalStore<Double> dst) {

        if (myEquations == null) { // Not prepared: identity fallback
            dst.fillMatching(src);
            return;
        }

        boolean unitOmega = myOmega == ONE;
        double invOmega = unitOmega ? ONE : (ONE / myOmega);

        myWorkY.fillAll(ZERO);
        // Forward solve: (D/ω + L) y = r  -> y_i = (r_i - sum_{j<i} a_ij y_j) / (d_i/ω)
        for (int k = 0, m = myEquations.size(); k < m; k++) {
            Equation row = myEquations.get(k);
            int i = row.index;
            double di = myDiag.doubleValue(i);
            double sumLower = row.dotLower(myWorkY);
            double rhs = src.doubleValue(i) - sumLower;
            myWorkY.set(i, rhs / (unitOmega ? di : (di * invOmega)));
        }

        // d = D y (in-place in workY)
        for (int i = 0; i < myDim; i++) {
            myWorkY.set(i, myDiag.doubleValue(i) * myWorkY.doubleValue(i));
        }

        myWorkZ.fillAll(ZERO);
        // Backward solve: (D/ω + U) z = d  -> z_i = (d_i - sum_{j>i} a_ij z_j)/(d_i/ω)
        for (int kk = myEquations.size() - 1; kk >= 0; kk--) {
            Equation row = myEquations.get(kk);
            int i = row.index;
            double di = myDiag.doubleValue(i);
            double sumUpper = row.dotUpper(myWorkZ);
            double rhs = myWorkY.doubleValue(i) - sumUpper;
            myWorkZ.set(i, rhs / (unitOmega ? di : (di * invOmega)));
        }

        double scale = unitOmega ? ONE : (myOmega / (TWO - myOmega));
        if (scale != ONE) {
            for (int i = 0; i < myDim; i++) {
                myWorkZ.set(i, scale * myWorkZ.doubleValue(i));
            }
        }

        dst.fillMatching(myWorkZ);
    }

    public SSORPreconditioner omega(final double value) {
        if (value <= ZERO || value >= TWO) {
            throw new IllegalArgumentException("omega must be in (0,2)");
        }
        myOmega = value;
        return this;
    }

    @Override
    public void prepare(final List<Equation> equations, final int dimension) {

        myEquations = equations;
        myDim = dimension;

        myDiag = IterativeSolverTask.worker(myDiag, dimension);
        myDiag.fillAll(ONE);
        for (int r = 0, m = equations.size(); r < m; r++) {
            Equation row = equations.get(r);
            double pivot = row.getPivot();
            if (pivot != ZERO) {
                myDiag.set(row.index, pivot);
            }
        }

        myWorkY = IterativeSolverTask.worker(myWorkY, dimension);
        myWorkZ = IterativeSolverTask.worker(myWorkZ, dimension);
    }
}