/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.optimisation;

import java.util.Collection;
import java.util.Collections;

import org.ojalgo.equation.Equation;
import org.ojalgo.type.keyvalue.EntryPair;

public interface UpdatableSolver extends Optimisation.Solver {

    interface EntityMap {

        /**
         * The number of slack variables - relates to {@link #getSlack(int)}
         */
        int countSlackVariables();

        /**
         * The number of variables, in the solver, that directly correspond to a model variable. (Not slack or
         * artificial variables.) This defines the range of the indices that can be used with the indexOf
         * method.
         */
        int countModelVariables();

        /**
         * Returns which model entity, and constraint type, that corresponsf to the slack variable at the
         * supplied index.
         *
         * @param idx Index of solver slack variable
         */
        EntryPair<ModelEntity<?>, ConstraintType> getSlack(int idx);

        /**
         * Converts from a solver specific variable index to the corresponding index of the variable in the
         * model. Note that not all model variables are necessarily represented in the solver, and a model
         * variable may result in multiple solver variables. Further, slack variables, artificial variables
         * and such are typically not represented in the model.
         *
         * @param idx Index of solver variable
         * @return Index of model variable (negative if no map)
         */
        int indexOf(int idx);

        /**
         * Is this solver variable negated relative to the corresponding model variable?
         *
         * @param idx Index of solver variable
         * @return true if this solver variable represents a negated model variable
         */
        boolean isNegated(int idx);

    }

    /**
     * @param index The, solver specific, variable index
     * @param value The value to fix that variable to
     * @return true if fixing is supported and was successful, otherwise false
     */
    default boolean fixVariable(final int index, final double value) {
        return false;
    }

    default Collection<Equation> generateCutCandidates(final double fractionality, final boolean... integer) {
        return Collections.emptySet();
    }

    UpdatableSolver.EntityMap getEntityMap();

    /**
     * A generalisation of {@link #fixVariable(int, double)} where the new/updated lower and upper bounds do
     * not need to be equal.
     *
     * @param index The, solver specific, variable index
     * @param lower New lower bound
     * @param upper New upper bound
     * @return true if updating the range is supported and was successful, otherwise false
     */
    default boolean updateRange(final int index, final double lower, double upper) {
        return false;
    }

}
