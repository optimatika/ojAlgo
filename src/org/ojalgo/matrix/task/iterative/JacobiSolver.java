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
package org.ojalgo.matrix.task.iterative;

import static org.ojalgo.constant.PrimitiveMath.*;
import static org.ojalgo.function.PrimitiveFunction.*;

import org.ojalgo.access.Access2D;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.type.context.NumberContext;

public class JacobiSolver extends StationaryIterativeSolver {

    private MatrixStore<Double> myBody;
    private MatrixStore<Double> myBodyDiagonal;
    private PhysicalStore<Double> myIncrement;
    private MatrixStore<Double> myRHS;

    public JacobiSolver() {
        super();
    }

    public JacobiSolver(final int iterationsLimit) {
        super(iterationsLimit);
    }

    public JacobiSolver(final NumberContext terminationContext) {
        super(terminationContext);
    }

    public JacobiSolver(final NumberContext terminationContext, final int iterationsLimit) {
        super(terminationContext, iterationsLimit);
    }

    @Override
    public MatrixStore<Double> iterate(final PhysicalStore<Double> current, final double relaxation) {

        current.multiplyLeft(myBody).operateOnMatching(myRHS, SUBTRACT).operateOnMatching(DIVIDE, myBodyDiagonal).supplyTo(myIncrement);

        if (this.getTerminationContext().isDifferent(ONE, relaxation)) {
            myIncrement.multiply(relaxation);
        }

        current.modifyMatching(ADD, myIncrement);

        return current;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setup(final Access2D<?> body, final Access2D<?> rhs) {

        if ((body instanceof MatrixStore<?>) && (body.get(0L) instanceof Double)) {
            myBody = (MatrixStore<Double>) body;
        } else {
            myBody = MatrixStore.PRIMITIVE.makeWrapper(body).get();
        }
        myBodyDiagonal = PrimitiveDenseStore.FACTORY.columns(myBody.sliceDiagonal(0L, 0L));

        if ((rhs instanceof MatrixStore<?>) && (rhs.get(0L) instanceof Double)) {
            myRHS = (MatrixStore<Double>) rhs;
        } else {
            myRHS = MatrixStore.PRIMITIVE.makeWrapper(rhs).get();
        }

        myIncrement = this.preallocate(body, rhs);
    }

}
