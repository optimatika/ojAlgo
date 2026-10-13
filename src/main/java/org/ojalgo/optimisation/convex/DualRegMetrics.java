package org.ojalgo.optimisation.convex;

/**
 * Optional diagnostics for dual regularisation usage. Disabled unless system property
 * "ojalgo.dual.reg.metrics" is true.
 */
final class DualRegMetrics {

    private static final String PROP_METRICS = "ojalgo.dual.reg.metrics";

    static final class Data {
        int schurRegCount;
        int kktRegCount;
        double maxRho;
        void reset() { schurRegCount = 0; kktRegCount = 0; maxRho = 0.0; }
    }

    private static final ThreadLocal<Data> TL = ThreadLocal.withInitial(Data::new);

    static boolean enabled() {
        String prop = System.getProperty(PROP_METRICS);
        return prop != null && Boolean.parseBoolean(prop);
    }

    static Data data() {
        return TL.get();
    }

    static void reset() {
        TL.get().reset();
    }

    static void recordSchur(final double rho) {
        if (!enabled() || rho == 0.0) return;
        Data d = TL.get();
        d.schurRegCount++;
        if (rho > d.maxRho) d.maxRho = rho;
    }

    static void recordKKT(final double rho) {
        if (!enabled() || rho == 0.0) return;
        Data d = TL.get();
        d.kktRegCount++;
        if (rho > d.maxRho) d.maxRho = rho;
    }
}
