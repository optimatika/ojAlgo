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
package org.ojalgo.matrix;

import java.util.List;

import org.ojalgo.matrix.decomposition.Eigenvalue.Eigenpair;
import org.ojalgo.structure.Access2D;

public interface Provider2D {

    @FunctionalInterface
    interface Condition extends Provider2D {

        double getCondition();

    }

    @FunctionalInterface
    interface Determinant<N extends Comparable<N>> extends Provider2D {

        N getDeterminant();

    }

    @FunctionalInterface
    interface Eigenpairs extends Provider2D {

        List<Eigenpair> getEigenpairs();

    }

    @FunctionalInterface
    interface Hermitian extends Provider2D {

        boolean isHermitian();

    }

    @FunctionalInterface
    interface Inverse<M> extends Provider2D {

        M invert();

    }

    @FunctionalInterface
    interface Rank extends Provider2D {

        int getRank();

    }

    @FunctionalInterface
    interface Solution<M> extends Provider2D {

        M solve(Access2D<?> rhs);

    }

    @FunctionalInterface
    interface Symmetric extends Provider2D {

        boolean isSymmetric();

    }

    @FunctionalInterface
    interface Trace<N extends Comparable<N>> extends Provider2D {

        N getTrace();

    }

}
