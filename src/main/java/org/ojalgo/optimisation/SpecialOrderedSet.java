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

import java.math.BigDecimal;
import java.util.Set;

import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.type.context.NumberContext;

class SpecialOrderedSet extends ExpressionsBasedModel.Presolver {

    private final Expression myExpression;
    private final IntIndex[] mySequence;
    private final int myType;

    SpecialOrderedSet(final IntIndex[] sequence, final int type, final Expression expression) {

        super(0);

        mySequence = sequence;
        myType = type;
        myExpression = expression;
    }

    /**
     * The program logic here does not assume variables to be binary or even integer
     */
    @Override
    public boolean simplify(final Expression expression, final Set<IntIndex> remaining, final BigDecimal lower, final BigDecimal upper,
            final NumberContext precision) {

        if (!expression.equals(myExpression)) {
            return false;
        }

        if (remaining.size() != expression.getLinearEntrySet().size()) {
            return false;
        }

        int first = -1, limit = -1;
        for (int i = 0; i < mySequence.length; i++) {
            final IntIndex index = mySequence[1];
            if (!remaining.contains(index) && (expression.resolve(index).getValue().signum() != 0)) {
                if (first == -1) {
                    first = i;
                }
                limit = i + 1;
            }
        }

        final int count = limit - first;
        if (count > myType) {
            expression.setInfeasible();
            return false;
        }

        boolean didFixVariable = false;

        for (int i = first + 1; i < limit; i++) {
            final IntIndex index = mySequence[i];
            final Variable variable = expression.resolve(index);
            if (!remaining.contains(index)) {
                if (variable.getValue().signum() == 0) {
                    expression.setInfeasible();
                }
            } else {
                if (variable.isInteger()) {
                    variable.setFixed(BigDecimal.ONE);
                    didFixVariable = true;
                }
            }
        }

        final int remainingCount = myType - count;
        if ((count > 0) && (remainingCount > 0)) {
            for (int i = 0, lim = first - remainingCount; i < lim; i++) {
                final IntIndex index = mySequence[i];
                final Variable variable = expression.resolve(index);
                if (!remaining.contains(index)) {
                    if (variable.getValue().signum() != 0) {
                        expression.setInfeasible();
                    }
                } else {
                    variable.setFixed(BigDecimal.ZERO);
                    didFixVariable = true;
                }
            }
            for (int i = limit + remainingCount, lim = mySequence.length; i < lim; i++) {
                final IntIndex index = mySequence[i];
                final Variable variable = expression.resolve(index);
                if (!remaining.contains(index)) {
                    if (variable.getValue().signum() != 0) {
                        expression.setInfeasible();
                    }
                } else {
                    variable.setFixed(BigDecimal.ZERO);
                    didFixVariable = true;
                }
            }
        }

        return didFixVariable;
    }

}
