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
package org.ojalgo.matrix.task.iterative;

import java.util.List;

import org.ojalgo.equation.Equation;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.structure.Access1D;

/**
 * Identity (no-op) preconditioner.
 * <p>
 * Characteristics and usage
 * <ul>
 * <li>Works with any iterative method and serves as a baseline when no preconditioning is desired.
 * <li>Safe choice for methods that require symmetry/positive-definiteness of the preconditioner.
 * <li>Often used to measure the effect of adding a non-trivial preconditioner.
 * </ul>
 */
final class IdentityPreconditioner implements Preconditioner {

    @Override
    public void apply(final Access1D<Double> src, final PhysicalStore<Double> dst) {
        dst.fillMatching(src); // copy src -> dst
    }

    @Override
    public void prepare(final List<Equation> equations, final int dimension) {
        // no-op
    }

}