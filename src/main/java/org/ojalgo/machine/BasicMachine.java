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

    public BasicMachine(final long memoryBytes, final int nbThreads) {

        super();

        memory = memoryBytes;
        threads = nbThreads;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BasicMachine)) {
            return false;
        }
        BasicMachine other = (BasicMachine) obj;
        if ((memory != other.memory) || (threads != other.threads)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (memory ^ (memory >>> 32));
        return prime * result + threads;
    }

    @Override
    public String toString() {

        int prefix = 1;
        long measure = memory / CommonMachine.K;

        while ((measure / CommonMachine.K) > 0) {
            prefix++;
            measure /= CommonMachine.K;
        }

        switch (prefix) {

        case 1:

            return measure + KILO + threads + ((threads == 1) ? THREAD : THREADS);

        case 2:

            return measure + MEGA + threads + ((threads == 1) ? THREAD : THREADS);

        case 3:

            return measure + GIGA + threads + ((threads == 1) ? THREAD : THREADS);

        default:

            return memory + BYTES + threads + ((threads == 1) ? THREAD : THREADS);
        }

    }
}
