package org.ojalgo.array.operation;

import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.structure.*;

/**
 * https://se.mathworks.com/help/matlab/matlab_prog/compatible-array-sizes-for-basic-operations.html
 */
public class FillCompatible {

    public static void invoke(final double[] target, final int structure, final Access2D<?> left, final BinaryFunction<?> operator, final Access2D<?> right) {

        int nbRows = structure;
        int nbCols = target.length / structure;

        int modRowsL = left.getRowDim();
        int modColsL = left.getColDim();

        int modRowsR = right.getRowDim();
        int modColsR = right.getColDim();

        for (int j = 0; j < nbCols; j++) {

            int colL = j % modColsL;
            int colR = j % modColsR;

            for (int i = 0; i < nbRows; i++) {

                int rowL = i % modRowsL;
                int rowR = i % modRowsR;

                double argL = left.doubleValue(rowL, colL);
                double argR = right.doubleValue(rowR, colR);

                double newVal = operator.invoke(argL, argR);

                target[i + j * structure] = newVal;
            }
        }
    }

    public static void invoke(final double[][] target, final Access2D<?> left, final BinaryFunction<?> operator, final Access2D<?> right) {

        int nbRows = target.length;
        int nbCols = nbRows > 0 ? target[0].length : 0;

        int modRowsL = left.getRowDim();
        int modColsL = left.getColDim();

        int modRowsR = right.getRowDim();
        int modColsR = right.getColDim();

        for (int j = 0; j < nbCols; j++) {

            int colL = j % modColsL;
            int colR = j % modColsR;

            for (int i = 0; i < nbRows; i++) {

                int rowL = i % modRowsL;
                int rowR = i % modRowsR;

                double argL = left.doubleValue(rowL, colL);
                double argR = right.doubleValue(rowR, colR);

                double newVal = operator.invoke(argL, argR);

                target[i][j] = newVal;
            }
        }
    }

    public static void invoke(final float[] target, final int structure, final Access2D<?> left, final BinaryFunction<?> operator, final Access2D<?> right) {

        int nbRows = structure;
        int nbCols = target.length / structure;

        int modRowsL = left.getRowDim();
        int modColsL = left.getColDim();

        int modRowsR = right.getRowDim();
        int modColsR = right.getColDim();

        for (int j = 0; j < nbCols; j++) {

            int colL = j % modColsL;
            int colR = j % modColsR;

            for (int i = 0; i < nbRows; i++) {

                int rowL = i % modRowsL;
                int rowR = i % modRowsR;

                float argL = left.floatValue(rowL, colL);
                float argR = right.floatValue(rowR, colR);

                float newVal = operator.invoke(argL, argR);

                target[i + j * structure] = newVal;
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final Mutate1D target, final Access1D<N> left, final BinaryFunction<N> operator,
            final Access1D<N> right) {

        int size = target.size();

        int modL = left.size();
        int modR = right.size();

        for (int i = 0; i < size; i++) {

            int indexL = i % modL;
            int indexR = i % modR;

            N argL = left.get(indexL);
            N argR = right.get(indexR);

            N newVal = operator.invoke(argL, argR);

            target.set(i, newVal);
        }
    }

    public static <N extends Comparable<N>> void invoke(final Mutate2D target, final Access2D<N> left, final BinaryFunction<N> operator,
            final Access2D<N> right) {

        int nbRows = target.getRowDim();
        int nbCols = target.getColDim();

        int modRowsL = left.getRowDim();
        int modColsL = left.getColDim();

        int modRowsR = right.getRowDim();
        int modColsR = right.getColDim();

        for (int j = 0; j < nbCols; j++) {

            int colL = j % modColsL;
            int colR = j % modColsR;

            for (int i = 0; i < nbRows; i++) {

                int rowL = i % modRowsL;
                int rowR = i % modRowsR;

                N argL = left.get(rowL, colL);
                N argR = right.get(rowR, colR);

                N newVal = operator.invoke(argL, argR);

                target.set(i, j, newVal);
            }
        }
    }

    public static <N extends Comparable<N>> void invoke(final MutateAnyD target, final AccessAnyD<N> left, final BinaryFunction<N> operator,
            final AccessAnyD<N> right) {

        int rank = MissingMath.max(target.rank(), left.rank(), right.rank());

        long[] refT = new long[target.rank()];
        long[] refL = new long[target.rank()];
        long[] refR = new long[target.rank()];

        FillCompatible.doOneOfAnyD(target, refT, rank - 1, left, refL, operator, right, refR);
    }

    private static <N extends Comparable<N>> void doOneOfAnyD(final MutateAnyD target, final long[] targRef, final int dim, final AccessAnyD<N> left,
            final long[] leftRef, final BinaryFunction<N> operator, final AccessAnyD<N> right, final long[] righRef) {

        long modL = left.count(dim);
        long modR = right.count(dim);

        for (long i = 0L, limit = target.count(dim); i < limit; i++) {

            targRef[dim] = i;
            leftRef[dim] = i % modL;
            righRef[dim] = i % modR;

            if (dim == 0) {
                target.set(targRef, operator.invoke(left.get(leftRef), right.get(righRef)));
            } else {
                FillCompatible.doOneOfAnyD(target, targRef, dim - 1, left, leftRef, operator, right, righRef);
            }
        }
    }

    static <N extends Comparable<N>, T extends Mutate1D> T expand(final Factory1D<T> factory, final Access1D<N> left, final BinaryFunction<N> operator,
            final Access1D<N> right) {

        T target = factory.make(left, right);

        FillCompatible.invoke(target, left, operator, right);

        return target;
    }

    static <N extends Comparable<N>, T extends Mutate2D> T expand(final Factory2D<T> factory, final Access2D<N> left, final BinaryFunction<N> operator,
            final Access2D<N> right) {

        T target = factory.make(left, right);

        FillCompatible.invoke(target, left, operator, right);

        return target;
    }

    static <N extends Comparable<N>, T extends MutateAnyD> T expand(final FactoryAnyD<T> factory, final AccessAnyD<N> left, final BinaryFunction<N> operator,
            final AccessAnyD<N> right) {

        T target = factory.make(left, right);

        FillCompatible.invoke(target, left, operator, right);

        return target;
    }

}
