package org.ojalgo.machine;

/**
 * How much memory, and how many threads share that memory. Used to describe either total system resources
 * (system RAM and total number of threads handled by the processors) or a cache (processor's L1, L2 or L3
 * cache).
 *
 * @author apete
 */
public class BasicMachine {

    private static final String BYTES = "bytes/";
    private static final String GIGA = "GB/";
    private static final String KILO = "kB/";
    private static final String MEGA = "MB/";
    private static final String THREAD = "thread";
    private static final String THREADS = "threads";

    public final long memory;
    public final int threads;

    public BasicMachine(final long memory, final int threads) {

        super();

        this.memory = memory;
        this.threads = threads;
    }

    @SuppressWarnings("unused")
    private BasicMachine() {
        this(0L, 0);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicMachine)) {
            return false;
        }
        final BasicMachine other = (BasicMachine) obj;
        if (memory != other.memory) {
            return false;
        }
        if (threads != other.threads) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (memory ^ (memory >>> 32));
        result = (prime * result) + threads;
        return result;
    }

    @Override
    public String toString() {

        int tmpPrefix = 1;
        int tmpMeasure = (int) (memory / AbstractMachine.K);

        while ((tmpMeasure / 1024) > 0) {
            tmpPrefix++;
            tmpMeasure /= 1024;
        }

        switch (tmpPrefix) {

        case 1:

            return tmpMeasure + KILO + threads + ((threads == 1) ? THREAD : THREADS);

        case 2:

            return tmpMeasure + MEGA + threads + ((threads == 1) ? THREAD : THREADS);

        case 3:

            return tmpMeasure + GIGA + threads + ((threads == 1) ? THREAD : THREADS);

        default:

            return memory + BYTES + threads + ((threads == 1) ? THREAD : THREADS);
        }

    }
}
