package org.ojalgo.optimisation;

import org.ojalgo.optimisation.strategy.ExpressionBasedModelPresolveStrategy;

import java.util.Set;

/**
 * A tail recursive implementation of the presolve interface, deligating the work of
 * determining if an Expression is redundant to {@link Expression#simplify(Set)}.
 */
public class ExpressionBasedModelPresolveRecursive implements ExpressionBasedModelPresolveStrategy {

    public int presolve(final ExpressionsBasedModel model) {
        final Set<Expression.Index> tmpFixedVariables = model.getFixedVariables();

        for (final Expression tmpExpression : model.getExpressions()) {

            final boolean tmpConstraint = tmpExpression.isConstraint();
            final boolean tmpInfeasible = tmpExpression.isInfeasible();
            final boolean tmpRedundant = tmpExpression.isRedundant();

            if (tmpConstraint && !tmpInfeasible && !tmpRedundant && tmpExpression.simplify(tmpFixedVariables)) {
                return 1 + this.presolve(model);
            }
        }

        return 0;
    }
}
