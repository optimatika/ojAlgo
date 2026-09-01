/*
 * Copyright 1997-2026 Optimatika
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.ojalgo.optimisation.integer;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.integer.IntegerStrategy.CutType;
import org.ojalgo.optimisation.integer.NodeSolver.Separator;

/**
 * What the cut separators did during one {@link IntegerSolver} solve: per separator type, and separately
 * for the root and for the other nodes, how many candidates were attempted, how many were kept, and how
 * many were rejected by which filter; and per cut round (all separators together, since the LP is only
 * re-solved once per round) by how much the LP bound moved. Updated from all worker threads.
 */
final class CutStatistics {

    static final class Entry {

        final LongAdder accepted = new LongAdder();
        final LongAdder attempted = new LongAdder();
        final LongAdder calls = new LongAdder();
        final LongAdder callsWithCuts = new LongAdder();
        final LongAdder rejectedDensity = new LongAdder();
        final LongAdder rejectedDynamism = new LongAdder();
        final LongAdder rejectedEfficacy = new LongAdder();
        final LongAdder rejectedSimilar = new LongAdder();
        final LongAdder skipped = new LongAdder();
        final LongAdder nanos = new LongAdder();

        void record(final Separator separator, final long elapsedNanos) {
            nanos.add(elapsedNanos);
            calls.increment();
            int nbAccepted = separator.countAccepted();
            if (nbAccepted > 0) {
                callsWithCuts.increment();
            }
            accepted.add(nbAccepted);
            attempted.add(separator.countAttempted());
            rejectedDensity.add(separator.countRejectedDensity());
            rejectedDynamism.add(separator.countRejectedDynamism());
            rejectedEfficacy.add(separator.countRejectedEfficacy());
            rejectedSimilar.add(separator.countRejectedSimilar());
        }

    }

    static final class Rounds {

        final DoubleAdder improvement = new DoubleAdder();
        final LongAdder resolveNanos = new LongAdder();
        final LongAdder rounds = new LongAdder();
        final LongAdder roundsWithImprovement = new LongAdder();

        void record(final double signedImprovement) {
            rounds.increment();
            if (signedImprovement > 0.0) {
                roundsWithImprovement.increment();
                improvement.add(signedImprovement);
            }
        }

    }

    /**
     * Every this many skipped calls an unproductive separator is tried again.
     */
    static final int RETRY_INTERVAL = 10;
    /**
     * Calls (at one level, root or node) without any accepted cut after which a separator is left out.
     */
    static final int UNPRODUCTIVE_CALLS = 10;
    /**
     * One entry per cut type, created up front so that the maps are never modified (and thus safe to read
     * from any thread); the counters themselves are thread-safe adders.
     */
    private final Map<CutType, Entry> myNodeEntries = new EnumMap<>(CutType.class);
    private final Rounds myNodeRounds = new Rounds();
    private final Map<CutType, Entry> myRootEntries = new EnumMap<>(CutType.class);
    private final Rounds myRootRounds = new Rounds();
    private final Optimisation.Sense mySense;

    CutStatistics(final Optimisation.Sense sense) {
        super();
        mySense = sense;
        for (CutType type : CutType.values()) {
            myRootEntries.put(type, new Entry());
            myNodeEntries.put(type, new Entry());
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        this.append(builder, "root", myRootEntries, myRootRounds);
        this.append(builder, "node", myNodeEntries, myNodeRounds);

        return builder.toString();
    }

    /**
     * Whether to call a separator of the given type at this level now. A separator that has had
     * {@link #UNPRODUCTIVE_CALLS} calls at this level without a single accepted cut is left out (what it
     * tried and did not get accepted on this model it would keep trying), but gets another chance every
     * {@link #RETRY_INTERVAL} skipped calls, since the LP points change as the search moves on.
     */
    boolean shouldTry(final CutType type, final boolean root) {

        Entry entry = (root ? myRootEntries : myNodeEntries).get(type);

        if (entry.accepted.sum() > 0L || entry.calls.sum() < UNPRODUCTIVE_CALLS) {
            return true;
        }

        entry.skipped.increment();

        return entry.skipped.sum() % RETRY_INTERVAL == 0L;
    }

    /**
     * Record what a separator did in the round that just ended (read off its per-round counters).
     */
    void record(final Separator separator, final boolean root, final long elapsedNanos) {
        (root ? myRootEntries : myNodeEntries).get(separator.type()).record(separator, elapsedNanos);
    }

    /**
     * Record the LP bound before and after a cut round (all separators of that round together), and the
     * time the re-solve took.
     */
    void recordRound(final boolean root, final double valueBefore, final double valueAfter, final long resolveNanos) {
        double improvement = mySense == Optimisation.Sense.MAX ? valueBefore - valueAfter : valueAfter - valueBefore;
        Rounds rounds = root ? myRootRounds : myNodeRounds;
        rounds.record(improvement);
        rounds.resolveNanos.add(resolveNanos);
    }

    private void append(final StringBuilder builder, final String where, final Map<CutType, Entry> entries, final Rounds rounds) {

        builder.append(where).append(": rounds=").append(rounds.rounds.sum()).append(", improving=").append(rounds.roundsWithImprovement.sum())
                .append(", bound improvement=").append(rounds.improvement.sum()).append(", re-solve ms=").append(rounds.resolveNanos.sum() / 1_000_000L).append('\n');

        for (Map.Entry<CutType, Entry> mapEntry : entries.entrySet()) {
            Entry entry = mapEntry.getValue();
            if (entry.calls.sum() == 0L) {
                continue;
            }
            builder.append("  ").append(mapEntry.getKey().code).append(": ms=").append(entry.nanos.sum() / 1_000_000L).append(", calls=").append(entry.calls.sum())
                    .append(" (with cuts ").append(entry.callsWithCuts.sum())
                    .append("), attempted=").append(entry.attempted.sum()).append(", accepted=").append(entry.accepted.sum()).append(", rejected efficacy=")
                    .append(entry.rejectedEfficacy.sum()).append(" density=").append(entry.rejectedDensity.sum()).append(" dynamism=")
                    .append(entry.rejectedDynamism.sum()).append(" similar=").append(entry.rejectedSimilar.sum()).append(", skipped=").append(entry.skipped.sum())
                    .append('\n');
        }
    }

}
