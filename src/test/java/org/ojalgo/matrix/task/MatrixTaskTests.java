/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.matrix.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.random.Uniform;

/**
 * @author apete
 */
public abstract class MatrixTaskTests {

    static final boolean DEBUG = false;

    public static List<DeterminantTask<Double>> getPrimitiveFull() {

        final ArrayList<DeterminantTask<Double>> retVal = new ArrayList<>();

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveLU());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveEigenvalueGeneral());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveQR());

        return retVal;
    }

    public static List<DeterminantTask<Double>> getPrimitiveSymmetric() {

        final ArrayList<DeterminantTask<Double>> retVal = new ArrayList<>();

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveCholesky());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveEigenvalueSymmetric());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveLU());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveEigenvalueGeneral());

        Collections.addAll(retVal, MatrixDecompositionTests.getPrimitiveQR());

        return retVal;
    }

    /**
     * A random matrix with rank one less than its smallest dimension. Rounding makes the determinant (of the
     * square case) tiny rather than exactly 0, which is the case that used to produce huge but finite values.
     */
    static MatrixStore<Double> makeRankDeficient(final int nbRows, final int nbCols) {

        int rank = Math.min(nbRows, nbCols) - 1;

        if (rank == 0) {
            return R064Store.FACTORY.make(nbRows, nbCols);
        }

        R064Store left = R064Store.FACTORY.makeFilled(nbRows, rank, new Uniform(-1, 2));
        R064Store right = R064Store.FACTORY.makeFilled(rank, nbCols, new Uniform(-1, 2));

        return left.multiply(right);
    }

    /**
     * A random symmetric matrix with rank dim - 1
     */
    static MatrixStore<Double> makeSingularSymmetric(final int dim) {

        if (dim == 1) {
            return R064Store.FACTORY.make(1, 1);
        }

        R064Store factor = R064Store.FACTORY.makeFilled(dim, dim - 1, new Uniform(-1, 2));

        return factor.multiply(factor.transpose());
    }

    @BeforeEach
    public void setUp() {
        TestUtils.minimiseAllBranchLimits();
    }
}
