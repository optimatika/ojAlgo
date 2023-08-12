package org.ojalgo.structure;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.ParameterFunction;
import org.ojalgo.function.UnaryFunction;

/**
 * To be implemented by classes that are not directly mutable themselves, but that can operate on the elements
 * of some internal/future 2D data structure – similar to streams in some sense.
 * <ul>
 * <li>Element-wise operations.
 * <li>The methods always return a new instance of the implementing class.
 * <li>The size or shape of the internal/future data does not change with these operations.
 * </ul>
 */
public interface Operate2D<N extends Comparable<N>, P extends Operate2D<N, P>> extends Structure2D {

    default P onAll(final BinaryFunction<N> operator, final double right) {
        return this.onAll(operator.second(right));
    }

    default P onAll(final BinaryFunction<N> operator, final N right) {
        return this.onAll(operator.second(right));
    }

    default P onAll(final double left, final BinaryFunction<N> operator) {
        return this.onAll(operator.first(left));
    }

    default P onAll(final N left, final BinaryFunction<N> operator) {
        return this.onAll(operator.first(left));
    }

    default P onAll(final ParameterFunction<N> operator, final int parameter) {
        return this.onAll(operator.parameter(parameter));
    }

    P onAll(UnaryFunction<N> operator);

    P onAny(Transformation2D<N> operator);

    P onColumns(Access1D<N> left, BinaryFunction<N> operator);

    P onColumns(BinaryFunction<N> operator, Access1D<N> right);

    default P onCompatible(final Access2D<N> left, final BinaryFunction<N> operator) {

        long nbLeftRows = left.countRows();
        long nbLeftCols = left.countColumns();

        long nbRightRows = this.countRows();
        long nbRightCols = this.countColumns();

        if (nbLeftRows != 1L && nbLeftRows != nbRightRows) {
            throw new IllegalArgumentException("Incompatible row dimensions: " + nbLeftRows + " != " + nbRightRows);
        }

        if (nbLeftCols != 1L && nbLeftCols != nbRightCols) {
            throw new IllegalArgumentException("Incompatible column dimensions: " + nbLeftCols + " != " + nbRightCols);
        }

        if (nbLeftRows == 1L) {
            return this.onColumns(left, operator);
        } else if (nbLeftCols == 1L) {
            return this.onRows(left, operator);
        } else {
            return this.onMatching(left, operator);
        }
    }

    default P onCompatible(final BinaryFunction<N> operator, final Access2D<N> right) {

        long nbLeftCols = this.countColumns();
        long nbLeftRows = this.countRows();

        long nbRightRows = right.countRows();
        long nbRightCols = right.countColumns();

        if (nbRightRows != 1L && nbRightRows != nbLeftRows) {
            throw new IllegalArgumentException("Incompatible row dimensions: " + nbLeftRows + " != " + nbRightRows);
        }

        if (nbRightCols != 1L && nbRightCols != nbLeftCols) {
            throw new IllegalArgumentException("Incompatible column dimensions: " + nbLeftCols + " != " + nbRightCols);
        }

        if (nbRightRows == 1L) {
            return this.onColumns(operator, right);
        } else if (nbRightCols == 1L) {
            return this.onRows(operator, right);
        } else {
            return this.onMatching(operator, right);
        }
    }

    P onMatching(Access2D<N> left, BinaryFunction<N> operator);

    P onMatching(BinaryFunction<N> operator, Access2D<N> right);

    P onRows(Access1D<N> left, BinaryFunction<N> operator);

    P onRows(BinaryFunction<N> operator, Access1D<N> right);

}
