package org.ojalgo.optimisation.strategy;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Expression;

/**
 * Interface to be implemented by different presolvers for {@link ExpressionsBasedModel}s.
 */
public interface ExpressionBasedModelPresolveStrategy {

    /**
     * Modifies the passed in model by setting the {@link Expression#myRedundant} of expressions
     * that it find to be redundant.
     *
     * @param model This expression will be modified in place.
     * @return The number of expressions that the presolver found to be redundant. The return type is actually
     *          added to avoid returning void.
     */
    int presolve(final ExpressionsBasedModel model);
}
