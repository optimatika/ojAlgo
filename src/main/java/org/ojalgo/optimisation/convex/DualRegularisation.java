package org.ojalgo.optimisation.convex;

import static org.ojalgo.function.constant.PrimitiveMath.*;

/**
 * Factory + baseline implementation for dual regularisation.
 * Now always enabled (except extended precision and zero-Q paths).
 */
final class DualRegularisation {

    private static final String PROP_ENABLE = "ojalgo.dual.reg";

    private static final DualRegularisationStrategy DISABLED = (dMax, dMin, m, ext, zeroQ) -> 0.0;

    private static final DualRegularisationStrategy BASELINE = (dMax, dMin, m, ext, zeroQ) -> {
        if (ext || zeroQ) return 0.0;
        double scale = (Double.isFinite(dMax) && dMax > 0.0) ? dMax : 1.0;
        double spread = (Double.isFinite(dMin) && dMin > 0.0) ? (scale / dMin) : Double.POSITIVE_INFINITY;
        if (spread <= 1.0e8) return 0.0;
        double rho = scale * MACHINE_EPSILON;
        double cap = scale * 1.0e-12;
        if (rho > cap) rho = cap;
        if (!Double.isFinite(rho) || rho <= 0.0) return 0.0;
        return rho;
    };

    static DualRegularisationStrategy strategy() {
        String prop = System.getProperty(PROP_ENABLE);
        if (prop == null || !Boolean.parseBoolean(prop)) return DISABLED;
        return BASELINE;
    }
}