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

import java.util.function.Consumer;
import java.util.function.Predicate;

final class ConfiguredIntegration<S extends Optimisation.Solver> extends ExpressionsBasedModel.Integration<S> {

    private final Predicate<ExpressionsBasedModel> myCapabilityPredicate;
    private final ExpressionsBasedModel.Integration<S> myDelegate;
    private final Consumer<Optimisation.Options> myOptionsModifier;

    ConfiguredIntegration(final ExpressionsBasedModel.Integration<S> delegate, final Predicate<ExpressionsBasedModel> capabilityPredicate,
            final Consumer<Optimisation.Options> optionsModifier) {
        super();
        myDelegate = delegate;
        myCapabilityPredicate = capabilityPredicate;
        myOptionsModifier = optionsModifier;
    }

    public S build(final ExpressionsBasedModel model) {
        if (myOptionsModifier != null) {
            myOptionsModifier.accept(model.options);
        }
        return myDelegate.build(model);
    }

    public boolean isCapable(final ExpressionsBasedModel model) {
        return (myCapabilityPredicate == null || myCapabilityPredicate.test(model)) && myDelegate.isCapable(model);
    }

    @Override
    public Result toModelState(final Result solverState, final ExpressionsBasedModel model) {
        return myDelegate.toModelState(solverState, model);
    }

    @Override
    public Result toSolverState(final Result modelState, final ExpressionsBasedModel model) {
        return myDelegate.toSolverState(modelState, model);
    }

}
