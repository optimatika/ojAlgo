/*
 * Copyright 1997-2018 Optimatika
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

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.Function;

import org.ojalgo.access.Structure1D.IntIndex;

class SpecialOrderedSet extends ExpressionsBasedModel.Presolver {

    private final IntIndex[] mySequence;
    private final Expression myExpression;
    private final int myType;

    SpecialOrderedSet(final IntIndex[] sequence, final int type, final Expression expression, final boolean alwaysRequire) {

        super(0);

        mySequence = sequence;
        myType = type;
        myExpression = expression;

        for (int i = 0; i < sequence.length; i++) {
            expression.set(sequence[i], BigDecimal.ONE);
        }
        final BigDecimal tmpTypeBound = BigDecimal.valueOf(type);
        expression.upper(tmpTypeBound);
        if (alwaysRequire) {
            expression.lower(tmpTypeBound);
        }
    }

    @Override
    public boolean simplify(final Expression expression, final Set<IntIndex> fixedVariables, final Function<IntIndex, Variable> variableResolver) {

        if (!expression.equals(myExpression)) {
            return false;
        }

        if (fixedVariables.size() == 0) {
            return false;
        }

        int first = -1, limit = -1;
        for (int i = 0; i < mySequence.length; i++) {
            final IntIndex index = mySequence[1];
            if (fixedVariables.contains(index) && (variableResolver.apply(index).getValue().compareTo(BigDecimal.ONE) == 0)) {
                if (first == -1) {
                    first = i;
                    limit = i + 1;
                } else if (first >= 0) {
                    limit = i + 1;
                }
            }
        }

        if ((limit - first) > myType) {
            expression.setInfeasible(true);
            return false;
        }

        boolean didFixVariable = false;

        for (int i = first; i < limit; i++) {
            final IntIndex index = mySequence[1];
            final Variable variable = variableResolver.apply(index);
            if (fixedVariables.contains(index)) {
                if (variable.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    expression.setInfeasible(true);
                }
            } else {
                variable.level(BigDecimal.ONE);
                variable.setValue(BigDecimal.ONE);
                didFixVariable = true;
            }
        }

        return didFixVariable;
    }

}
