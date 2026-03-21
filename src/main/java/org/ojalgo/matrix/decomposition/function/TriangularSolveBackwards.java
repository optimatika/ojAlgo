package org.ojalgo.matrix.decomposition.function;

import org.ojalgo.matrix.store.PhysicalStore;

@FunctionalInterface
public interface TriangularSolveBackwards<N extends Comparable<N>> {

    void substituteBackwards(boolean conjugated, boolean unitDiagonal, double[] arg);

    default void substituteBackwards(final boolean conjugated, final boolean unitDiagonal, final PhysicalStore<N> arg) {
        double[] argCopy = arg.toRawCopy1D();
        this.substituteBackwards(conjugated, unitDiagonal, argCopy);
        arg.fillMatching(argCopy);
    }

}
