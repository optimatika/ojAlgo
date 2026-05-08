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
import org.ojalgo.optimisation.Equilibrator;
import org.ojalgo.structure.Structure2D;

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
final class RuizScaling extends Equilibrator<AlternatingDirectionSolver.Problem> {

    RuizScaling(final int nbIterations, final int n, final int m) {
        super(nbIterations, n, m);
    }

    RuizScaling(final int nbIterations, final Structure2D dimensions) {
        this(nbIterations, dimensions.getColDim(), dimensions.getRowDim());
    }

    @Override
    protected void doAfterUpdate(final AlternatingDirectionSolver.Problem data) {
        MULTIPLY.invoke(data.l, dual.values);
        MULTIPLY.invoke(data.u, dual.values);
    }

    @Override
    protected void doUpdateIteration(final AlternatingDirectionSolver.Problem data, final double[] wPrimal, final double[] wDual) {

        double temp;
        double norm;

        R064CSC.calculateInfinityColumnNorms(data.A, wPrimal);
        R064CSC.calculateInfinitySymmetricNorms(data.P, wPrimal);

        R064CSC.calculateInfinityRowNorms(data.A, wDual);

        Equilibrator.clamp(wPrimal);
        Equilibrator.clamp(wDual);

        SQRT.invoke(wPrimal);
        SQRT.invoke(wDual);

        INV.invoke(wPrimal, wPrimal);
        INV.invoke(wDual, wDual);

        R064CSC.scaleRows(data.P, wPrimal);
        R064CSC.scaleColumns(data.P, wPrimal);

        R064CSC.scaleRows(data.A, wDual);
        R064CSC.scaleColumns(data.A, wPrimal);

        MULTIPLY.invoke(data.q, wPrimal);

        MULTIPLY.invoke(primal.values, wPrimal, primal.values);
        MULTIPLY.invoke(dual.values, wDual, dual.values);

        Arrays.fill(wPrimal, PrimitiveMath.ZERO);
        R064CSC.calculateInfinitySymmetricNorms(data.P, wPrimal);
        temp = MEAN.invoke(wPrimal);
        temp = Equilibrator.clamp(temp);

        norm = NRMINF.invoke(data.q);
        norm = Equilibrator.clamp(norm);

        temp = Math.max(temp, norm);
        temp = PrimitiveMath.ONE / temp;

        R064CSC.scale(data.P, temp);
        MULTIPLY.invoke(data.q, temp);
        cost *= temp;
    }

    /**
     * Undo the effect of the most recent
     * {@link #update(AlternatingDirectionSolver.Problem, AlternatingDirectionSolver.Work)} call on the
     * quadratic program data, restoring the original scale. If the configured number of iterations is zero,
     * no modifications are applied.
     */
    void unscale(final AlternatingDirectionSolver.Problem data) {

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

    /**
     * Map a scaled solution back to the original variable and constraint space using the stored scaling
     * factors. If the configured number of iterations is zero, this method is a no-op.
     */
    void unscale(final AlternatingDirectionSolver.Solution solution) {

        this.unscalePrimal(solution.x);

        this.unscaleDual(solution.y);
    }

}