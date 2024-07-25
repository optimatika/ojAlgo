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
package org.ojalgo.optimisation;

import java.util.Collection;
import java.util.Collections;

import org.ojalgo.equation.Equation;
import org.ojalgo.optimisation.ExpressionsBasedModel.EntityMap;
import org.ojalgo.type.keyvalue.EntryPair;

public interface UpdatableSolver extends Optimisation.Solver {

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

    ExpressionsBasedModel.EntityMap getEntityMap();

    default boolean[] integers(final ExpressionsBasedModel model) {

        EntityMap entityMap = this.getEntityMap();

        int nbProblVars = entityMap.countModelVariables();
        int nbSlackVars = entityMap.countSlackVariables();

        boolean[] integers = new boolean[nbProblVars + nbSlackVars];

        for (int i = 0; i < nbProblVars; i++) {
            int indexInModel = entityMap.indexOf(i);
            Variable variable = model.getVariable(indexInModel);
            if (variable.isInteger()) {
                integers[i] = true;
            }
        }

        for (int i = 0; i < nbSlackVars; i++) {
            EntryPair<ModelEntity<?>, ConstraintType> slack = entityMap.getSlack(i);
            ModelEntity<?> entity = slack.getKey();
            if (entity.isInteger()) {
                integers[nbProblVars + i] = true;
            }
        }

        return integers;
    }

    default boolean isMapped() {
        return this.getEntityMap() != null;
    }

    /**
     * A generalisation of {@link #fixVariable(int, double)} where the new/updated lower and upper bounds do
     * not need to be equal.
     *
     * @param index The, solver specific, variable index
     * @param lower New lower bound
     * @param upper New upper bound
     * @return true if updating the range is supported and was successful, otherwise false
     */
    default boolean updateRange(final int index, final double lower, final double upper) {
        return false;
    }

}
