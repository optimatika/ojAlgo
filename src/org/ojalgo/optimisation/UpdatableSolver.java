package org.ojalgo.optimisation;

import java.math.BigDecimal;

public interface UpdatableSolver extends Optimisation.Solver {

    boolean update(BigDecimal lower, Variable variable);

    boolean update(Variable variable, BigDecimal upper);

}
