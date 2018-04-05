package org.ojalgo.optimisation;

public interface UpdatableSolver extends Optimisation.Solver {

    boolean update(Variable variable);

}
