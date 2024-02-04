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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

/**
 * @see https://en.wikipedia.org/wiki/Iterative_method#Stationary_iterative_methods
 * @see https://mathworld.wolfram.com/StationaryIterativeMethod.html
 * @author apete
 */
public abstract class StationaryIterativeSolver extends IterativeSolverTask {

    private double myRelaxationFactor = ONE;

    StationaryIterativeSolver() {
        super();
    }

    public final double getRelaxationFactor() {
        return myRelaxationFactor;
    }

    public final void setRelaxationFactor(final double relaxation) {
        myRelaxationFactor = relaxation;
    }

}
