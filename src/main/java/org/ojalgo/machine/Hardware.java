/*
 * Copyright 1997-2025 Optimatika
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
package org.ojalgo.machine;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.ojalgo.array.operation.COPY;

import com.sun.management.OperatingSystemMXBean;

/**
 * <ul>
 * <li>The first element in the array should correspond to total system resources; the total amount of RAM and
 * the total number of threads (Typically the same as what is returned by
 * {@linkplain Runtime#availableProcessors()}).
 * <li>The last element in the array should describe the L1 cache. Typically Intel processors have 32k L1
 * cache and AMD 64k. 1 or maybe 2 threads use/share this cache.
 * <li>Caches, all levels except L1, are described between the first and last elements in descending order (L3
 * cache comes before L2 cache). Specify the size of the cache and the number of threads using/sharing the
 * cache. (Do not worry about how many cache units there are - describe one unit.)
 * <li>The array must have at least 2 elements. You must describe the total system resources and the L1 cache.
 * It is strongly recommended to also describe the L2 cache. The L3 cache, if it exists, is less important to
 * describe. The derived attributes <code>processors</code>, <code>cores</code> and <code>units</code> may be
 * incorrectly calculated if you fail to specify the caches. Known issue: If you have more than one processor,
 * nut no L3 cache; the <code>processors</code> attribute will be incorrectly set 1. A workaround that
 * currently works is to define an L3 cache anyway and set the memory/size of that cache to 0bytes. This
 * Workaround may stop working in the future.
 * <li><code>new MemoryThreads[] { SYSTEM, L3, L2, L1 }</code> or
 * <code>new MemoryThreads[] { SYSTEM, L2, L1 }</code> or <code>new MemoryThreads[] { SYSTEM, L1 }</code>
 * </ul>
 *
 * @author apete
 */
public final class Hardware extends CommonMachine implements Comparable<Hardware> {

    public static enum Architecture {

        AARCH64, ARM, OTHER, PPC, X86, X86_64;

        public static Architecture from(final String arch) {

            if (arch == null) {
                return OTHER;
            }

            switch (arch.toLowerCase()) {
            case "x86":
            case "i386":
                return X86;
            case "x86_64":
            case "amd64":
                return X86_64;
            case "aarch64":
                return AARCH64;
            case "arm":
                return ARM;
            case "ppc":
            case "ppc64":
                return PPC;
            default:
                return OTHER;
            }
        }

        @Override
        public String toString() {
            switch (this) {
            case X86:
                return "x86";
            case X86_64:
                return "x86_64";
            case AARCH64:
                return "aarch64";
            case ARM:
                return "arm";
            case PPC:
                return "ppc";
            default:
                return "other";
            }
        }
    }

    /**
     * Cache-line size is (typically) 64 bytes
     */
    public static final long CPU_CACHE_LINE_SIZE = 64L;

    /**
     * Page size is usually determined by the processor architecture. Traditionally, pages in a system had
     * uniform size, such as 4,096 bytes. However, processor designs often allow two or more, sometimes
     * simultaneous, page sizes due to its benefits. There are several points that can factor into choosing
     * the best page size.
     * <p>
     * Practically all architectures/OS:s have a page size of 4k (one notable exception is Solaris/SPARC that
     * have 8k)
     * <p>
     * AArch64 supports three different granule sizes: 4KB, 16KB, and 64KB.
     */
    public static final long OS_MEMORY_PAGE_SIZE = 4L * K;

    /**
     * Returns the normalized processor architecture of the current system. This is used by ojAlgo's hardware
     * detection system to determine appropriate cache configurations, threading models, and optimization
     * strategies for different processor families.
     * <p>
     * The architecture string is normalized to standard values for consistency across different JVM
     * implementations and operating systems.
     * <p>
     *
     * @return The normalized processor architecture string, never null. Defaults to "other" if the
     *         architecture cannot be determined.
     */
    public static Architecture getArchitecture() {
        return Architecture.from(System.getProperty("os.arch", "unknown"));
    }

    /**
     * System RAM - attempts multiple strategies to determine total physical memory with robust fallback
     * mechanisms.
     */
    public static long getMemory() {

        // 1) Prefer OS-reported total physical memory
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long totalPhysicalMemory = osBean.getTotalPhysicalMemorySize();
            if (totalPhysicalMemory > 0) {
                return totalPhysicalMemory;
            }
        } catch (Throwable ignore) {
            // ignore and fall back
        }

        // 2) Reverse-engineer from default JVM sizing: Xmx is typically ~25% of physical
        long heapMax = Runtime.getRuntime().maxMemory();
        if (heapMax > 0L) {
            long estimated = heapMax * 4L; // assume default MaxRAMPercentage ~ 25%
            if (estimated < heapMax) {
                // overflow guard (highly unlikely)
                estimated = heapMax;
            }
            return estimated;
        }

        // 3) Static minimal assumption
        return 2L * K * K * K; // 2 GiB
    }

    /**
     * Returns the number of processors (logical cores/threads) available to the Java Virtual Machine. This
     * includes both physical cores and logical threads created by technologies like Intel's Hyperthreading or
     * IBM's SMT. This value is used by ojAlgo for parallel algorithm selection, thread pool sizing, and work
     * distribution strategies.
     * <p>
     * The value represents the total number of logical processors that the JVM can utilize for parallel
     * execution. This is used by ojAlgo to:
     * <ul>
     * <li>Size thread pools for parallel matrix operations</li>
     * <li>Determine when to use parallel vs. sequential algorithms</li>
     * <li>Configure parallelism thresholds for different operations</li>
     * <li>Estimate optimal block sizes for parallel decomposition</li>
     * </ul>
     * <p>
     * <b>Typical values:</b>
     * <ul>
     * <li><code>1</code> - Single-core systems or restricted environments</li>
     * <li><code>2</code> - Dual-core systems or dual-core with hyperthreading disabled</li>
     * <li><code>4</code> - Quad-core systems or dual-core with hyperthreading</li>
     * <li><code>8</code> - Octa-core systems or quad-core with hyperthreading</li>
     * <li><code>16-32</code> - High-end consumer or workstation processors</li>
     * <li><code>64-128+</code> - Server-class processors with many cores</li>
     * </ul>
     *
     * @return The number of logical processors available to the JVM, always at least 1.
     */
    public static int getThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Creates a Hardware instance from available system properties and logical deduction. This method
     * supports a wide range of hardware from legacy systems to modern many-core processors.
     *
     * @param architecture The system architecture (x86, x86_64, aarch64, etc.)
     * @param memory       The available system memory in bytes (now machine RAM, not JVM max heap)
     * @param threads      The number of available processor threads
     * @return A Hardware instance that closely matches the actual system hardware
     */
    public static Hardware make(final Architecture architecture, final long memory, final int threads) {

        BasicMachine l1 = Hardware.estimateL1(architecture, memory, threads);

        int nbLevels = Hardware.estimateNumberOfLevels(architecture, memory, threads, l1);
        int length = 1 + nbLevels;

        BasicMachine[] levels = new BasicMachine[length];

        levels[0] = new BasicMachine(memory, threads);
        levels[nbLevels] = l1;
        for (int level = 2; level <= nbLevels; level++) {
            levels[length - level] = Hardware.estimateLevel(architecture, memory, threads, l1, nbLevels, level);
        }

        return new Hardware(architecture, levels);
    }

    /**
     * @deprecated v56 Use {@link #newInstance()} instead
     */
    @Deprecated
    public static Hardware makeSimple() {
        return Hardware.makeSimple(Hardware.getArchitecture(), Hardware.getMemory(), Hardware.getThreads());
    }

    /**
     * @deprecated v56 Use {@link #make(String, long, int, String) instead
     */
    @Deprecated
    public static Hardware makeSimple(final Architecture systemArchitecture, final long systemMemory, final int systemThreads) {

        if (systemThreads > 8) {
            // Assume hyperthreading, L3 cache and more than 1 CPU

            BasicMachine tmpL1Machine = new BasicMachine(32L * K, 2); //Hyperthreading

            BasicMachine tmpL2Machine = new BasicMachine(256L * K, tmpL1Machine.threads);

            BasicMachine tmpL3Machine = new BasicMachine(4L * K * K, systemThreads / ((systemThreads + 7) / 8)); //More than 1 CPU

            BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL3Machine, tmpL2Machine, tmpL1Machine });

        } else if (systemThreads >= 4) {
            // Assume hyperthreading, L3 cache but only 1 CPU

            BasicMachine tmpL1Machine = new BasicMachine(32L * K, 2); //Hyperthreading

            BasicMachine tmpL2Machine = new BasicMachine(256L * K, tmpL1Machine.threads);

            BasicMachine tmpL3Machine = new BasicMachine(3L * K * K, systemThreads);

            BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL3Machine, tmpL2Machine, tmpL1Machine });

        } else {
            // No hyperthreading, no L3 cache and 1 CPU

            BasicMachine tmpL1Machine = new BasicMachine(32L * K, 1); //No hyperthreading

            BasicMachine tmpL2Machine = new BasicMachine(2L * K * K, tmpL1Machine.threads);

            BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL2Machine, tmpL1Machine });
        }
    }

    /**
     * Creates a Hardware instance by intelligently detecting system characteristics from available system
     * properties and logical deduction. This method supports a wide range of hardware from legacy systems to
     * modern many-core processors.
     *
     * @return A Hardware instance that closely matches the actual system hardware
     */
    public static Hardware newInstance() {
        return Hardware.make(Hardware.getArchitecture(), Hardware.getMemory(), Hardware.getThreads());
    }

    /**
     * L1 cache is usually fixed per architecture.
     */
    private static BasicMachine estimateL1(final Architecture architecture, final long memory, final int threads) {

        int threadsPerCore;
        long l1Size;

        switch (architecture) {
        case PPC:
            if (threads >= 16) {
                threadsPerCore = 8; // SMT8
            } else if (threads >= 8) {
                threadsPerCore = 4; // SMT4
            } else if (threads >= 4) {
                threadsPerCore = 2; // SMT2
            } else {
                threadsPerCore = 1;
            }
            l1Size = 64L * K;
            break;

        case X86:
        case X86_64:
            // For 4-thread systems, infer SMT2 vs 1 based on memory bands; otherwise typical SMT2 for larger even counts
            if (threads == 4) {
                long gb = Math.max(1L, memory / (K * K * K));
                if (gb < 4L) {
                    threadsPerCore = 1; // very low-RAM quads (no HT)
                } else if (gb <= 11L) {
                    threadsPerCore = 2; // likely 2C4T with HT
                } else {
                    threadsPerCore = 1; // likely 4C4T without HT
                }
            } else if (threads == 2) {
                threadsPerCore = 1;
            } else {
                threadsPerCore = (threads >= 4 && (threads % 2 == 0)) ? 2 : 1;
            }
            l1Size = 32L * K;
            break;

        case AARCH64:
            threadsPerCore = 1;
            l1Size = 64L * K;
            break;

        case ARM:
            threadsPerCore = (threads >= 8) ? 2 : 1;
            l1Size = 32L * K;
            break;

        default:
            // Generic/OTHER: scale SMT with high thread counts typical for many-thread architectures (e.g., SPARC T-series)
            if (threads >= 64) {
                threadsPerCore = 8;
            } else if (threads >= 32) {
                threadsPerCore = 4;
            } else if (threads >= 4 && (threads % 2 == 0)) {
                threadsPerCore = 2;
            } else {
                threadsPerCore = 1;
            }
            l1Size = 32L * K;
        }

        return new BasicMachine(l1Size, threadsPerCore);
    }

    private static BasicMachine estimateLevel(final Architecture architecture, final long memory, final int threads, final BasicMachine l1, final int nbLevels,
            final int level) {
        // level: 2 => L2, 3 => L3 (when nbLevels == 3)
        final int tpc = Math.max(1, l1.threads); // threads per core
        final int cores = Math.max(1, threads / tpc);

        switch (architecture) {
        case X86:
        case X86_64: {
            if (level == 2) {
                // Modern x86(x64) commonly scales L2 per core; legacy 4-thread low-RAM used larger shared L2
                if (threads == 4) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb <= 3L) {
                        return new BasicMachine(3L * K * K, 2);
                    }
                }
                // Legacy 32-bit x86 single/dual-core often had larger shared L2
                if (architecture == Architecture.X86 && threads <= 2) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb <= 2L) {
                        return new BasicMachine(1L * K * K, 1);
                    } else if (gb <= 4L) {
                        return new BasicMachine(6L * K * K, 2);
                    }
                }
                // Early x86_64 dual-core without HT often had shared L2 of a few MB
                if (architecture == Architecture.X86_64 && threads == 2) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb <= 4L) {
                        return new BasicMachine(4L * K * K, 2);
                    }
                }
                // Default per-core L2 size with a memory-banded bump for mid/high-memory 8-16 thread systems
                long gb = Math.max(1L, memory / (K * K * K));
                if (threads >= 8 && threads <= 16 && gb >= 24L) {
                    return new BasicMachine(1L * K * K, tpc);
                }
                return new BasicMachine(256L * K, tpc);
            } else if (level == 3) {
                // Heuristics for L3 size and sharing
                long l3Size;
                int l3Threads = threads; // default: shared by all threads
                if (threads >= 96) {
                    l3Size = 30L * K * K; // large multi-socket server
                    l3Threads = Math.max(1, threads / 4); // approximate per-socket sharing (assume 4 sockets)
                } else if (threads >= 64) {
                    l3Size = 20L * K * K;
                    l3Threads = Math.max(1, threads / 2); // typical dual-socket sharing
                } else if (cores == 6) {
                    l3Size = 12L * K * K; // Common 6-core desktop/workstation parts
                } else if (cores >= 8) {
                    // Use memory ranges to separate older vs newer 8+ core platforms
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb >= 32L) {
                        // Newer: ~2MB per core, shared by all threads
                        l3Size = Math.min(32L * K * K, cores * 2L * K * K);
                        l3Threads = threads;
                    } else if (gb <= 16L) {
                        // Older/leaner memory configs: smaller LLC, shared by physical cores only
                        l3Size = 8L * K * K;
                        l3Threads = cores;
                    } else {
                        // In-between: scale with cores, shared by all threads
                        l3Size = Math.max(8L * K * K, Math.min(32L * K * K, cores * 2L * K * K));
                        l3Threads = threads;
                    }
                } else if (threads == 8) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb >= 28L && gb <= 31L) {
                        // Mid-memory 30GB configs (e.g., some cloud/desktop platforms)
                        l3Size = 8250L * K; // 8,250 KiB (~8.06 MiB)
                    } else {
                        l3Size = 8L * K * K; // 8 MiB
                    }
                } else if (threads == 4) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    // Memory-banded L3 for 4-thread systems: <=4GB -> 3MB, <=8GB -> 4MB, >8GB -> 6MB
                    if (gb <= 4L) {
                        l3Size = 3L * K * K;
                    } else if (gb <= 8L) {
                        l3Size = 4L * K * K;
                    } else {
                        l3Size = 6L * K * K;
                    }
                } else {
                    // Generic: ~2MB per core, min 3MB, cap 32MB
                    l3Size = Math.max(3L * K * K, Math.min(32L * K * K, cores * 2L * K * K));
                }
                return new BasicMachine(l3Size, l3Threads);
            }
            break;
        }
        case AARCH64: {
            if (level == 2) {
                // Non-Apple default ~256KB per core; Apple can have bigger shared L2
                // Use simple heuristic on threads & memory to mimic common M1/M2 examples
                if (threads == 8) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb <= 18L) {
                        // M1 Pro example: large shared L2 across performance clusters
                        return new BasicMachine(28L * K * K, 8);
                    } else {
                        // M2 Air example: 4MB (perf), simplified as a single unit with 4 threads
                        return new BasicMachine(4L * K * K, 4);
                    }
                }
                return new BasicMachine(256L * K, tpc);
            } else if (level == 3) {
                if (threads == 8) {
                    long gb = Math.max(1L, memory / (K * K * K));
                    if (gb <= 18L) {
                        return new BasicMachine(24L * K * K, 8); // M1 Pro SLC/L3
                    } else {
                        return new BasicMachine(8L * K * K, 8); // M2 example
                    }
                }
                // Generic ARM64: modest LLC
                return new BasicMachine(2L * K * K, threads);
            }
            break;
        }
        case ARM: {
            if (level == 2) {
                return new BasicMachine(256L * K, tpc);
            } else if (level == 3) {
                return new BasicMachine(2L * K * K, threads);
            }
            break;
        }
        case PPC: {
            if (level == 2) {
                // Many PPC parts have ~512KB L2 per core (simplified)
                return new BasicMachine(512L * K, Math.min(8, tpc));
            } else if (level == 3) {
                // Typical small PPC L3
                return new BasicMachine(8L * K * K, threads);
            }
            break;
        }
        default: {
            if (level == 2) {
                return new BasicMachine(256L * K, tpc);
            } else if (level == 3) {
                return new BasicMachine(3L * K * K, threads);
            }
        }
        }

        // Fallback (should not reach)
        return new BasicMachine(256L * K, tpc);
    }

    /**
     * Usually 3, but could be 2. Theoretically could be any non-negative number (even 0).
     */
    private static int estimateNumberOfLevels(final Architecture architecture, final long memory, final int threads, final BasicMachine l1) {
        // Heuristic: return number of cache levels including L1 (i.e., L1=1, L2+L1=2, L3+L2+L1=3)
        switch (architecture) {
        case X86:
        case X86_64: {
            if (threads <= 2) {
                return 2;
            }
            if (threads == 4) {
                long gb = Math.max(1L, memory / (K * K * K));
                return (gb <= 3L) ? 2 : 3;
            }
            return 3;
        }
        case AARCH64:
            return 3;
        case ARM:
            return 2;
        case PPC:
            return (threads >= 16) ? 3 : 2;
        default:
            return 2;
        }
    }

    private final BasicMachine[] myLevels;

    /**
     * <code>new BasicMachine[] { SYSTEM, L3, L2, L1 }</code>,
     * <code>new MemoryThreads[] { SYSTEM, L2, L1 }</code> or <code>new MemoryThreads[] { SYSTEM, L1 }</code>
     */
    public Hardware(final Architecture arch, final BasicMachine[] levels) {

        super(arch, levels);

        if (levels.length < 2) {
            throw new IllegalArgumentException();
        }

        myLevels = COPY.copyOf(levels);
    }

    @Override
    public int compareTo(final Hardware other) {
        if (cores != other.cores) {
            return cores - other.cores;
        } else if (threads != other.threads) {
            return threads - other.threads;
        } else if (cache != other.cache) {
            return (int) (cache - other.cache);
        } else if (units != other.units) {
            return units - other.units;
        } else if (memory != other.memory) {
            return (int) (memory - other.memory);
        } else if (architecture != other.architecture) {
            return architecture.compareTo(other.architecture);
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Hardware)) {
            return false;
        }
        Hardware other = (Hardware) obj;
        if (!Arrays.equals(myLevels, other.myLevels)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(myLevels);
        return result;
    }

    public boolean isL2Specified() {
        return myLevels.length > 2;
    }

    public boolean isL3Specified() {
        return myLevels.length > 3;
    }

    @Override
    public String toString() {
        return architecture + " " + Arrays.toString(myLevels);
    }

    public VirtualMachine virtualise() {
        return new VirtualMachine(this, Runtime.getRuntime());
    }

}