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
package org.ojalgo.optimisation.linear;

import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Optimisation.Result;

public class SpecialSituations {

    public SpecialSituations() {
        super();
    }

    @Test
    public void testDegeneracy() {

        PrimitiveDenseStore c = PrimitiveDenseStore.FACTORY.columns(new double[] { -2, -1, 0, 0, 0 });
        PrimitiveDenseStore A = PrimitiveDenseStore.FACTORY.rows(new double[][] { { 4, 3, 1, 0, 0 }, { 4, 1, 0, 1, 0 }, { 4, 2, 0, 0, 1 } });
        PrimitiveDenseStore b = PrimitiveDenseStore.FACTORY.columns(new double[] { 12, 8, 8 });

        LinearSolver lp = LinearSolver.getBuilder(c).equalities(A, b).build();

        Result result = lp.solve();

        BasicLogger.debug(result);

    }

}
