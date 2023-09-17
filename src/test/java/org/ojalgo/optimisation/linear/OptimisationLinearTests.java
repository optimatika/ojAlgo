/*
 * Copyright 1997-2023 Optimatika
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

import java.util.Map;

import org.ojalgo.optimisation.ExpressionsBasedModel;

abstract class OptimisationLinearTests {

    public static final ExpressionsBasedModel.Integration<LinearSolver> DEFAULT_DENSE = LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
        opt.experimental = false;
        opt.sparse = Boolean.FALSE;
    });
    public static final ExpressionsBasedModel.Integration<LinearSolver> DEFAULT_SPARSE = LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
        opt.experimental = false;
        opt.sparse = Boolean.TRUE;
    });
    public static final ExpressionsBasedModel.Integration<LinearSolver> EXPERIMENTAL_DENSE = LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
        opt.experimental = true;
        opt.sparse = Boolean.FALSE;
    });
    public static final ExpressionsBasedModel.Integration<LinearSolver> EXPERIMENTAL_SPARSE = LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
        opt.experimental = true;
        opt.sparse = Boolean.TRUE;
    });

    public static final Map<String, ExpressionsBasedModel.Integration<LinearSolver>> INTEGRATIONS = Map.of("Def-Dense", DEFAULT_DENSE, "Def-Sparse",
            DEFAULT_SPARSE, "Exp-Dense", EXPERIMENTAL_DENSE, "Exp-Sparse", EXPERIMENTAL_SPARSE);

    static final boolean DEBUG = false;

}
