/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.optimisation.system;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.optimisation.Optimisation;

public final class ConjugateGradientSolver extends OptimisationSystem {

    public ConjugateGradientSolver() {
        super();
    }

    @Override
    public Optimisation.Result solve(final MatrixStore<Double> body, final MatrixStore<Double> rhs) {

        final PhysicalStore<Double> retVal = PrimitiveDenseStore.FACTORY.makeZero(rhs.countRows(), rhs.countColumns());

        final PhysicalStore<Double> tmpResidual = PrimitiveDenseStore.FACTORY.copy(rhs); // Since initial estimate is 0
        final MatrixStore<Double> tmpTranspRes = tmpResidual.builder().transpose().build();

        final PhysicalStore<Double> tmpDirection = PrimitiveDenseStore.FACTORY.copy(tmpResidual);
        final MatrixStore<Double> tmpTranspDir = tmpDirection.builder().transpose().build();

        double tmpAlpha;
        final double tmpBeta;

        //  while (false) {

        tmpAlpha = tmpResidual.multiplyLeft(tmpTranspRes).doubleValue(0L) / body.multiplyLeft(tmpTranspDir).multiplyRight(tmpDirection).doubleValue(0L);

        retVal.maxpy(tmpAlpha, tmpDirection);
        tmpResidual.maxpy(-tmpAlpha, body.multiplyRight(tmpDirection));

        //  }

        return null;
    }
}
