package org.ojalgo.matrix.decomposition.function;

import org.ojalgo.matrix.store.PhysicalStore;

@FunctionalInterface
public interface TriangularSolveForwards<N extends Comparable<N>> {

    void substituteForwards(boolean conjugated, boolean unitDiagonal, double[] arg);

    default void substituteForwards(final boolean conjugated, final boolean unitDiagonal, final PhysicalStore<N> arg) {
        double[] argCopy = arg.toRawCopy1D();
        this.substituteForwards(conjugated, unitDiagonal, argCopy);
        arg.fillMatching(argCopy);
    }

}
