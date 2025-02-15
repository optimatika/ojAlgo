/*
 * Copyright 1997-2025 Optimatika
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.ojalgo.optimisation.ExpressionsBasedModel;

abstract class OptimisationLinearTests {

    static final boolean DEBUG = false;

    static final Map<String, ExpressionsBasedModel.Integration<LinearSolver>> INTEGRATIONS = Map.of("Dual-Default",
            LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().dual();
                opt.sparse = null;
            }), "Dual-Dense", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().dual();
                opt.sparse = Boolean.FALSE;
            }), "Dual-Sparse", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().dual();
                opt.sparse = Boolean.TRUE;
            }), "Primal-Default", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().primal();
                opt.sparse = null;
            }), "Primal-Dense", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().primal();
                opt.sparse = Boolean.FALSE;
            }), "Primal-Sparse", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.linear().primal();
                opt.sparse = Boolean.TRUE;
            }), "Default-Default", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.sparse = null;
            }), "Default-Dense", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.sparse = Boolean.FALSE;
            }), "Default-Sparse", LinearSolver.INTEGRATION.withOptionsModifier(opt -> {
                opt.sparse = Boolean.TRUE;
            }));

    static final List<Function<LinearStructure, SimplexStore>> STORE_FACTORIES = List.of(RevisedStore::new, DenseTableau::new, SparseTableau::new);

    static final List<Function<LinearStructure, SimplexTableau>> TABLEAU_FACTORIES = List.of(DenseTableau::new, SparseTableau::new);

}
