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
package org.ojalgo.optimisation.convex;

import java.util.Arrays;

import org.ojalgo.array.operation.INV;
import org.ojalgo.array.operation.MEAN;
import org.ojalgo.array.operation.MULTIPLY;
import org.ojalgo.array.operation.NRMINF;
import org.ojalgo.array.operation.SQRT;
import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.matrix.store.R064CSC;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.ReciprocalPair;

/**
 * Ruiz-style diagonal equilibration for quadratic programmes.
 * <p>
 * This helper maintains diagonal scaling factors for primal variables and constraints and applies a fixed
 * number of Ruiz iterations to the quadratic data (P, q, A, l, u). The aim is to approximately equilibrate
 * the associated KKT system and reduce the dynamic range of the numerical values before factorisation. When
 * the configured number of iterations is zero the scaling acts as the identity and all operations are
 * effectively no-ops.
 * <p>
 * A {@link RuizScaling} instance can be reused across solves:
 * {@link #update(AlternatingDirectionSolver.Problem)} recomputes the scaling for the current data, and
 * {@link #unscale(AlternatingDirectionSolver.Problem)} together with
 * {@link #unscale(AlternatingDirectionSolver.Solution)} map matrices, vectors and solutions back to the
 * original scale.
 */
final class RuizScaling {

    /** Maximum admissible scaling factor. */
    static final double MAX = 1E+4;

    /** Minimum admissible scaling factor. */
    static final double MIN = 1E-4;

    /**
     * Clamps a single scaling factor to the admissible {@link #MIN}–{@link #MAX} interval, replacing very
     * small values by 1 to avoid excessive rescaling.
     */
    static double clamp(final double value) {
        if (value < RuizScaling.MIN) {
            return PrimitiveMath.ONE;
        } else if (value > RuizScaling.MAX) {
            return RuizScaling.MAX;
        } else {
            return value;
        }
    }

    /**
     * Clamps each entry of a scaling vector to the admissible {@link #MIN}–{@link #MAX} interval, replacing
     * very small values by 1 to avoid excessive rescaling.
     */
    static void clamp(final double[] values) {
        for (int i = 0, lim = values.length; i < lim; i++) {
            values[i] = RuizScaling.clamp(values[i]);
        }
    }

    private final int myNumberOfIterations;
    private final double[] myWorkM;
    private final double[] myWorkN;

    /** Scalar cost-function scaling. */
    double cost = PrimitiveMath.ONE;

    /** Diagonal scaling and its reciprocal for the constraints. */
    final ReciprocalPair dual;
    /** Diagonal scaling and its reciprocal for the primal variables. */
    final ReciprocalPair primal;

    RuizScaling(final int nbIterations, final int m, final int n) {

        super();

        primal = new ReciprocalPair(n);
        dual = new ReciprocalPair(m);

        myNumberOfIterations = nbIterations;

        myWorkM = new double[m];
        myWorkN = new double[n];
    }

    RuizScaling(final int nbIterations, final Structure2D dimensions) {
        this(nbIterations, dimensions.getRowDim(), dimensions.getColDim());
    }

    /**
     * Undo the effect of the most recent
     * {@link #update(AlternatingDirectionSolver.Problem, AlternatingDirectionSolver.Work)} call on the
     * quadratic program data, restoring the original scale. If the configured number of iterations is zero,
     * no modifications are applied.
     */
    void unscale(final AlternatingDirectionSolver.Problem data) {

        if (myNumberOfIterations > 0) {

            R064CSC.scale(data.P, PrimitiveMath.ONE / cost);
            R064CSC.scaleRows(data.P, primal.inverse);
            R064CSC.scaleColumns(data.P, primal.inverse);
            MULTIPLY.invoke(data.q, PrimitiveMath.ONE / cost);
            MULTIPLY.invoke(data.q, primal.inverse);

            R064CSC.scaleRows(data.A, dual.inverse);
            R064CSC.scaleColumns(data.A, primal.inverse);
            MULTIPLY.invoke(data.l, dual.inverse);
            MULTIPLY.invoke(data.u, dual.inverse);
        }
    }

    /**
     * Map a scaled solution back to the original variable and constraint space using the stored scaling
     * factors. If the configured number of iterations is zero, this method is a no-op.
     */
    void unscale(final AlternatingDirectionSolver.Solution solution) {

        if (myNumberOfIterations > 0) {

            MULTIPLY.invoke(solution.x, primal.values);

            MULTIPLY.invoke(solution.y, dual.values);
            MULTIPLY.invoke(solution.y, PrimitiveMath.ONE / cost);
        }
    }

    /**
     * Recompute Ruiz diagonal scalings for the current problem data and apply them in-place.
     * <p>
     * Executes the configured number of Ruiz iterations on the KKT system derived from {@code data}, updates
     * {@link #primal}, {@link #dual} and {@link #cost}, and scales (P, q, A, l, u) accordingly. If the number
     * of iterations is zero the data and scaling factors are left unchanged.
     */
    void update(final AlternatingDirectionSolver.Problem data) {

        primal.fill(PrimitiveMath.ONE);
        dual.fill(PrimitiveMath.ONE);
        cost = PrimitiveMath.ONE;

        double temp, norm;

        for (int i = 0; i < myNumberOfIterations; i++) {

            R064CSC.calculateInfinityColumnNorms(data.A, myWorkN);
            R064CSC.calculateInfinitySymmetricNorms(data.P, myWorkN);

            R064CSC.calculateInfinityRowNorms(data.A, myWorkM);

            RuizScaling.clamp(myWorkN);
            RuizScaling.clamp(myWorkM);

            SQRT.invoke(myWorkN);
            SQRT.invoke(myWorkM);

            INV.invoke(myWorkN, myWorkN);
            INV.invoke(myWorkM, myWorkM);

            R064CSC.scaleRows(data.P, myWorkN);
            R064CSC.scaleColumns(data.P, myWorkN);

            R064CSC.scaleRows(data.A, myWorkM);
            R064CSC.scaleColumns(data.A, myWorkN);

            MULTIPLY.invoke(data.q, myWorkN);

            MULTIPLY.invoke(primal.values, myWorkN, primal.values);
            MULTIPLY.invoke(dual.values, myWorkM, dual.values);

            Arrays.fill(myWorkN, PrimitiveMath.ZERO);
            R064CSC.calculateInfinitySymmetricNorms(data.P, myWorkN);
            temp = MEAN.invoke(myWorkN);
            temp = RuizScaling.clamp(temp);

            norm = NRMINF.invoke(data.q);
            norm = RuizScaling.clamp(norm);

            temp = Math.max(temp, norm);
            temp = PrimitiveMath.ONE / temp;

            R064CSC.scale(data.P, temp);
            MULTIPLY.invoke(data.q, temp);
            cost *= temp;
        }

        if (myNumberOfIterations > 0) {
            MULTIPLY.invoke(data.l, dual.values);
            MULTIPLY.invoke(data.u, dual.values);
        }

        primal.invert();
        dual.invert();
    }

}