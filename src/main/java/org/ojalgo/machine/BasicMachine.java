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

    /**
     * Rounded to one decimal, with the decimal omitted when it is zero: 1.5, 2, 1024.
     */
    private static String toOneDecimal(final double value) {

        long tenths = Math.round(value * 10.0);

        if (tenths % 10L == 0L) {
            return Long.toString(tenths / 10L);
        } else {
            return (tenths < 0L ? "-" : "") + Math.abs(tenths / 10L) + "." + Math.abs(tenths % 10L);
        }
    }

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
        result = prime * result + (int) (memory ^ (memory >>> 32));
        result = prime * result + threads;
        return result;
    }

    @Override
    public String toString() {

        int prefix = 1;
        double measure = memory / (double) CommonMachine.K;

        // Step up a unit when the value, rounded to one decimal, reaches 1024
        while (Math.round(measure * 10.0) >= CommonMachine.K * 10L) {
            prefix++;
            measure /= CommonMachine.K;
        }

        String number = BasicMachine.toOneDecimal(measure);

        switch (prefix) {

            case 1:

                return number + KILO + threads + ((threads == 1) ? THREAD : THREADS);

            case 2:

                return number + MEGA + threads + ((threads == 1) ? THREAD : THREADS);

            case 3:

                return number + GIGA + threads + ((threads == 1) ? THREAD : THREADS);

            default:

                return memory + BYTES + threads + ((threads == 1) ? THREAD : THREADS);
        }

    }

}
