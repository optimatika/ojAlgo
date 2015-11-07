/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import org.ojalgo.function.multiary.MultiaryFunction;

final class FunctionsBasedModel extends AbstractModel<GenericSolver> {

    public static abstract class Integration<S extends Optimisation.Solver> implements Optimisation.Integration<FunctionsBasedModel, S> {

    }

    static final class Function extends ModelEntity<Function> {

        private final MultiaryFunction<Double> myFunction = null;

        Function(final Function entityToCopy) {
            super(entityToCopy);
        }

        Function(final String name) {
            super(name);
        }

    }

    private final int myNumberOfVariables;

    public FunctionsBasedModel(final int numberOfVariables) {

        super();

        myNumberOfVariables = numberOfVariables;
    }

    private FunctionsBasedModel() {

        super();

        myNumberOfVariables = 0;
    }

    private FunctionsBasedModel(final Options someOptions) {

        super(someOptions);

        myNumberOfVariables = 0;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    public Result maximise() {
        // TODO Auto-generated method stub
        return null;
    }

    public Result minimise() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean validate() {
        // TODO Auto-generated method stub
        return false;
    }

}
