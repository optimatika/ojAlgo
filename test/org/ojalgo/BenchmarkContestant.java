/*
 * Copyright 1997-2015 Optimatika
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
package org.ojalgo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class BenchmarkContestant<T> {

    public abstract class EigenDecomposer implements UnaryOperator<T> {

        public abstract T apply(final T matrix);

        @SuppressWarnings("unchecked")
        public final Object decompose(final Object matrix) {
            return this.apply((T) matrix);
        }

    }

    /**
     * A general (square) equation system solver
     *
     * @author apete
     */
    public abstract class GeneralSolver implements BinaryOperator<T> {

        public abstract T apply(final T body, final T rhs);

        @SuppressWarnings("unchecked")
        public final Object solve(final Object body, final Object rhs) {
            return this.apply((T) body, (T) rhs);
        }

    }

    public abstract class HermitianSolver implements BinaryOperator<T> {

        public abstract T apply(final T body, final T rhs);

        @SuppressWarnings("unchecked")
        public final Object solve(final Object body, final Object rhs) {
            return this.apply((T) body, (T) rhs);
        }

    }

    public abstract class LeastSquaresSolver implements BinaryOperator<T> {

        public abstract T apply(final T body, final T rhs);

        @SuppressWarnings("unchecked")
        public final Object solve(final Object body, final Object rhs) {
            return this.apply((T) body, (T) rhs);
        }

    }

    public abstract class MatrixBuilder implements Supplier<T> {

        public abstract void set(int row, int col, double value);

    }

    public abstract class MatrixMultiplier implements BinaryOperator<T> {

        public abstract T apply(final T left, final T right);

        @SuppressWarnings("unchecked")
        public final Object multiply(final Object left, final Object right) {
            return this.apply((T) left, (T) left);
        }

    }

    public abstract class SingularDecomposer implements UnaryOperator<T> {

        public abstract T apply(final T matrix);

        @SuppressWarnings("unchecked")
        public final Object decompose(final Object matrix) {
            return this.apply((T) matrix);
        }

    }

    public abstract class TransposedMultiplier implements BinaryOperator<T> {

        public abstract T apply(final T left, final T right);

        @SuppressWarnings("unchecked")
        public final Object multiply(final Object left, final Object right) {
            return this.apply((T) left, (T) left);
        }

    }

    public static final Map<String, BenchmarkContestant<?>> CONTESTANTS = new HashMap<>();

    static {
    }

    public BenchmarkContestant() {
        super();
    }

    @SuppressWarnings("unchecked")
    public final Object convert(final Object object) {
        if (object.getClass().isArray()) {
            return this.convertTo((double[][]) object);
        } else {
            return this.convertFrom((T) object);
        }
    }

    public abstract EigenDecomposer getEigenDecomposer();

    public abstract GeneralSolver getGeneralSolver();

    public abstract HermitianSolver getHermitianSolver();

    public abstract LeastSquaresSolver getLeastSquaresSolver();

    public abstract MatrixBuilder getMatrixBuilder(int numberOfRows, int numberOfColumns);

    public abstract MatrixMultiplier getMatrixMultiplier();

    public abstract SingularDecomposer getSingularDecomposer();

    public abstract TransposedMultiplier getTransposedMultiplier();

    protected abstract double[][] convertFrom(T matrix);

    protected abstract T convertTo(double[][] raw);

}
