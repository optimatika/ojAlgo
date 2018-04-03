package org.ojalgo.optimisation;

import java.math.BigDecimal;

public interface UpdatableSolver {

    void update(BigDecimal lower, Variable variable);

    void update(Variable variable, BigDecimal upper);

}
