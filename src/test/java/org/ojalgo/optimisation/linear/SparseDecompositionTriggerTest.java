/*
 * Copyright 1997-2025 Optimatika
 */
package org.ojalgo.optimisation.linear;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Diagnostic driver: runs a handful of NETLIB models through the dual-S (revised simplex with
 * {@link SparseDecomposition}) path and prints the breakdown of which refactorisation trigger fired plus
 * wall-clock time. Sweeps tighter (more-aggressive-refactoring) parameter combinations to look for speedups
 * from fresher numerics — fewer simplex iterations may outweigh the extra refactor cost.
 */
@Tag("diagnostic")
@Disabled
public class SparseDecompositionTriggerTest {

    /** Models to exercise — mix of healthy and pathological cases. */
    private static final String[] MODELS = { "25FV47", "AGG2", "BNL1", "BNL2", "CRE-A", "DEGEN3", "MAROS-R7", "PEROLD", "QAP12", "STOCFOR2", "TRUSS", "WOOD1P",
            "WOODW" };

    /**
     * Parameter combinations to sweep: (etaMultiplier, pivotGatePrecision, maxUpdates). One step looser per
     * axis from the current baseline (3, 4, 250), to verify the baseline isn't a missed local optimum.
     */
    private static final int[][] CONFIGS = {
            // baseline (current)
            { 3, 4, 250 },
            // one step looser per axis
            { 4, 4, 250 }, { 3, 5, 250 }, { 3, 4, 500 },
            // pairs
            { 4, 5, 250 }, { 4, 4, 500 }, { 3, 5, 500 },
            // all three looser
            { 4, 5, 500 } };

    private static long runOne(final String name) {
        ExpressionsBasedModel model = ModelFileTest.makeModel("netlib", name + ".SIF", false);
        model.options.sparse = Boolean.TRUE;
        model.options.linear().dual();
        SparseDecomposition.resetCounters();
        long t0 = System.nanoTime();
        Result result = model.minimise();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
        BasicLogger.debug("    {} [{}]: {}ms refactors={} (max={}, pivot={}, eta={}, fail={}) updates={}", name, result.getState(), elapsedMs,
                SparseDecomposition.COUNT_TRIGGER_MAX.get() + SparseDecomposition.COUNT_TRIGGER_PIVOT.get() + SparseDecomposition.COUNT_TRIGGER_ETA.get()
                        + SparseDecomposition.COUNT_TRIGGER_FAIL.get(),
                SparseDecomposition.COUNT_TRIGGER_MAX.get(), SparseDecomposition.COUNT_TRIGGER_PIVOT.get(), SparseDecomposition.COUNT_TRIGGER_ETA.get(),
                SparseDecomposition.COUNT_TRIGGER_FAIL.get(), SparseDecomposition.COUNT_UPDATES.get());
        return elapsedMs;
    }

    @Test
    public void sweepParameters() {
        // Warm up JIT with one cheap model first
        SparseDecompositionTriggerTest.runOne("AGG2");

        int origEta = SparseDecomposition.ETA_MULTIPLIER;
        int origMax = SparseDecomposition.MAX_UPDATES;
        NumberContext origGate = SparseDecomposition.PIVOT_DEGRADATION_GATE;
        try {
            for (int[] cfg : CONFIGS) {
                SparseDecomposition.ETA_MULTIPLIER = cfg[0];
                SparseDecomposition.PIVOT_DEGRADATION_GATE = NumberContext.of(cfg[1]);
                SparseDecomposition.MAX_UPDATES = cfg[2];
                BasicLogger.debug("");
                BasicLogger.debug("=== ETA_MULTIPLIER={}, PIVOT_GATE=of({}), MAX_UPDATES={} ===", cfg[0], cfg[1], cfg[2]);
                long total = 0;
                for (String name : MODELS) {
                    try {
                        total += SparseDecompositionTriggerTest.runOne(name);
                    } catch (Throwable t) {
                        BasicLogger.debug("    {}: FAILED {}", name, t.getMessage());
                    }
                }
                BasicLogger.debug("    --- total: {}ms ---", total);
            }
        } finally {
            SparseDecomposition.ETA_MULTIPLIER = origEta;
            SparseDecomposition.PIVOT_DEGRADATION_GATE = origGate;
            SparseDecomposition.MAX_UPDATES = origMax;
        }
    }

}
