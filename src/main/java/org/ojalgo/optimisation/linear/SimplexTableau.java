/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.optimisation.linear;

import java.util.Collection;

import org.ojalgo.equation.Equation;
import org.ojalgo.optimisation.linear.SimplexSolver.EnterInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.ExitInfo;
import org.ojalgo.optimisation.linear.SimplexSolver.IterDescr;
import org.ojalgo.optimisation.linear.SimplexTableauSolver.IterationPoint;
import org.ojalgo.structure.Access1D;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Primitive1D;
import org.ojalgo.structure.Primitive2D;
import org.ojalgo.type.context.NumberContext;

abstract class SimplexTableau extends SimplexStore implements Access2D<Double>, Mutate2D {

    /**
     * For which (type of) solver is this store intended?
     */
    static enum FeatureSet {
        /**
         * Used by the old {@link SimplexTableauSolver}.
         */
        CLASSIC,
        /**
         * Used by the new {@link SimplexSolver}.
         */
        COMPACT;
    }

    private transient Primitive2D myConstraintsBody = null;
    private transient Primitive1D myConstraintsRHS = null;
    private transient Primitive1D myObjective = null;

    final SimplexTableau.FeatureSet features;

    SimplexTableau(final SimplexTableau.FeatureSet featureSet, final LinearStructure linearStructure) {
        super(linearStructure);
        features = featureSet;
    }

    @Override
    final void calculateDualDirection(final ExitInfo exit) {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    final void calculateIteration() {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    final void calculateIteration(final IterDescr iteration) {
        // With a tableau all calculations are continuously done when pivoting
    }

    @Override
    final void calculatePrimalDirection(final EnterInfo enter) {
        // With a tableau all calculations are continuously done when pivoting
    }

    /**
     * The area of the tableau corresponding to the constraints' body.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#constraintsBody()
     */
    @Override
    final Primitive2D constraintsBody() {
        if (myConstraintsBody == null) {
            myConstraintsBody = this.newConstraintsBody();
        }
        return myConstraintsBody;
    }

    /**
     * The area of the tableau corresponding to the constraints' RHS.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#constraintsRHS()
     */
    @Override
    final Primitive1D constraintsRHS() {
        if (myConstraintsRHS == null) {
            myConstraintsRHS = this.newConstraintsRHS();
        }
        return myConstraintsRHS;
    }



    abstract boolean fixVariable(int index, double value);

    abstract Collection<Equation> generateCutCandidates(boolean[] integer, NumberContext integralityTolerance, double fractionality);

    abstract boolean isAbleToExtractDual();

    abstract Primitive2D newConstraintsBody();

    abstract Primitive1D newConstraintsRHS();

    abstract Primitive1D newObjective();

    /**
     * The area of the tableau corresponding to the objective function.
     *
     * @see org.ojalgo.optimisation.linear.SimplexStore#objective()
     */
    @Override
    final Primitive1D objective() {
        if (myObjective == null) {
            myObjective = this.newObjective();
        }
        return myObjective;
    }

    abstract void pivot(IterationPoint pivot);

    abstract Access1D<Double> sliceBodyColumn(int numerCol);

    abstract Access1D<Double> sliceConstraintsRHS();

    abstract Access1D<Double> sliceTableauRow(int row);

    abstract double value(boolean b);

}
