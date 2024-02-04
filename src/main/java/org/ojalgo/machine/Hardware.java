/*
 * Copyright 1997-2024 Optimatika
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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.ojalgo.array.operation.COPY;
import org.ojalgo.netio.ASCII;

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
     * Should contain all available hardware in ascending "power" order.
     */
    public static final Set<Hardware> PREDEFINED = new TreeSet<>();

    /**
     * <p>
     * M1 Pro Mainly modelled after the performance cores since there are more of those. Also did not separate
     * between L2 and L3/SLC cache since there are 2 of each and they are the same size per thread.
     * <p>
     * Notes: M2, M2 Pro, M2 Max, M2 Ultra -> 1, 2, 4, 8 memory controllers resulting in 100GB/s, 200GB/s,
     * 400GB/s and 800GB/s Memory Bandwidth
     * <ul>
     * <li>Apple M1 Pro
     * <ul>
     * <li>L1 Cache the high-perf cores have a large 192 KB of L1 instruction cache and 128 KB of L1 data
     * cache The energy-efficient cores have a 128 KB L1 instruction cache, 64 KB L1 data cache.
     * <li>L2 Cache (28MB all together) The 6 high-perf cores are split in two clusters, each cluster has 12MB
     * of shared L2 cache (so 24MB total) The 2 high-efficiency cores have 4MB of shared L2 cache
     * <li>L3 / SLC (24MB all together) The SLC is 12MB per memory controller, so 24MB total.
     * <li>16 GB unified memory
     * </ul>
     * <li>squid / 15" MacBook Air 2023, Apple M2
     * <ul>
     * <li>8 cores (4 performance and 4 efficiency)
     * <li>L1: Performance cores 192+128 KB per core / Efficiency cores 128+64 KB per core
     * <li>L2: Performance cores 16 MB / Efficiency cores 4 MB
     * <li>L3: 8 MB
     * <li>24 GB unified memory
     * </ul>
     * </ul>
     */
    static final Hardware AARCH64__08 = new Hardware("aarch64", new BasicMachine[] { new BasicMachine(24L * K * K * K, 8), new BasicMachine(8L * K * K, 8),
            new BasicMachine(4L * K * K, 4), new BasicMachine(64L * K, 1) });

    /**
     * <ul>
     * <li>CLAM / PowerBook6,5
     * <ul>
     * <li>1 processor
     * <li>1 core per processor
     * <li>1 thread per core
     * <li>===
     * <li>1.25GB system RAM
     * <li>512kB L2 cache per processor
     * <li>64kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware PPC__01 = new Hardware("ppc",
            new BasicMachine[] { new BasicMachine(5L * 256L * K * K, 1), new BasicMachine(512L * K, 1), new BasicMachine(64L * K, 1) });

    /**
     * <ul>
     * <li>INTEL1
     * <ul>
     * <li>1 processor
     * <li>1 core per processor
     * <li>1 thread per core
     * <li>===
     * <li>1GB system RAM
     * <li>1MB L2 cache per processor
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86__01 = new Hardware("x86",
            new BasicMachine[] { new BasicMachine(1L * K * K * K, 1), new BasicMachine(1L * K * K, 1), new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>B5950053
     * <ul>
     * <li>1 processor
     * <li>2 cores per processor
     * <li>1 thread per core
     * <li>===
     * <li>3.5GB system RAM
     * <li>6MB L2 cache per processor (2 cores)
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86__02 = new Hardware("x86",
            new BasicMachine[] { new BasicMachine(7L * 512L * K * K, 2), new BasicMachine(6L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>MANTA / iMac7,1
     * <ul>
     * <li>1 processor
     * <li>2 cores per processor
     * <li>1 thread per core
     * <li>===
     * <li>3GB system RAM
     * <li>4MB L2 cache per processor (2 cores)
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__02 = new Hardware("x86_64",
            new BasicMachine[] { new BasicMachine(3L * K * K * K, 2), new BasicMachine(4L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * Combination of {@link #X86_64__04_1_L2}, {@link #X86_64__04_1_L3} and {@link #X86_64__04_2}
     */
    static final Hardware X86_64__04 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(32L * K * K * K, 4), new BasicMachine(3L * K * K, 4),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>PA's Q9400
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>3GB system RAM
     * <li>3MB L2 cache per 2 cores
     * <li>32kB L1 cache per core
     * </ul>
     * <li>PA's Q6600
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>4MB L2 cache per 2 cores
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__04_1_L2 = new Hardware("x86_64",
            new BasicMachine[] { new BasicMachine(8L * K * K * K, 4), new BasicMachine(3L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>Intel i5-4670K with 16GB of RAM
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>16GB system RAM
     * <li>6MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>Intel Core i5-3570K with 32GB of RAM (from Java Matrix Benchmark)
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>32GB system RAM
     * <li>6MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__04_1_L3 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(32L * K * K * K, 4), new BasicMachine(6L * K * K, 4),
            new BasicMachine(256L * K, 1), new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>BUBBLE / MacBookAir4,2
     * <ul>
     * <li>1 processors
     * <li>2 cores per processor
     * <li>2 threads per core (4 threads in total)
     * <li>===
     * <li>4GB system RAM
     * <li>3MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>PA's Intel Core i7-620M laptop
     * <ul>
     * <li>1 processors
     * <li>2 cores per processor
     * <li>2 threads per core (4 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>4MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>MacBookPro14,2 (oyster)
     * <ul>
     * <li>1 processors
     * <li>2 cores per processor
     * <li>2 threads per core (4 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>4MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__04_2 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(8L * K * K * K, 4), new BasicMachine(3L * K * K, 4),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>HA's Intel Core i7-920 server
     * <ul>
     * <li>1 processor
     * <li>4 cores per processor
     * <li>2 threads per core (8 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>8MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>Core i7-2600 3.4 GHz - 4 cores - 8 threads from Java Matrix Benchmark
     * <ul>
     * <li>1 processor
     * <li>4 cores per processor
     * <li>2 threads per core (8 threads in total)
     * <li>===
     * <li>11GB system RAM
     * <li>8MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>Core i7-3770 3.4 GHz - 4 cores - 8 threads (whale @ MSC/MSB)
     * <ul>
     * <li>1 processor
     * <li>4 cores per processor
     * <li>2 threads per core (8 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>8MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>Core i7-2600 3.4 GHz - 4 cores - 8 threads (Vostro-460 @ Scila)
     * <ul>
     * <li>1 processor
     * <li>4 cores per processor
     * <li>2 threads per core (8 threads in total)
     * <li>===
     * <li>32GB system RAM
     * <li>8MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>Google Cloud Platform Compute Engine n1-standard-8 (8 vCPUs, 30 GB memory, Skylake)
     * <ul>
     * <li>1 processor
     * <li>4 cores per processor
     * <li>2 threads per core (8 threads in total)
     * <li>===
     * <li>30GB system RAM
     * <li>8.25MB L3 cache per processor
     * <li>1MB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__08 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(32L * K * K * K, 8), new BasicMachine(8L * K * K, 8),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <pre>
     * "Gulftown" (32 nm) Model: SLBUZ (B1)
     * Intel Core i7-980 3.33GHz
     * 8/25/2010
     * ref: http://ark.intel.com/products/47932
     *      https://en.wikipedia.org/wiki/List_of_Intel_Core_i7_microprocessors
     *      Device Manager
     * </pre>
     * <ul>
     * <li>Intel Core i7-980
     * <ul>
     * <li>1 processor
     * <li>6 cores per processor
     * <li>2 threads per core (12 threads in total)
     * <li>===
     * <li>12GB system RAM
     * <li>12MB L3 cache per processor
     * <li>256kB L2 cache per core (x6)
     * <li>32kB L1 cache per core (x6)
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__12 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(12L * K * K * K, 12), new BasicMachine(12L * K * K, 12),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>SAILFISH / MacPro4,1
     * <ul>
     * <li>2 processors
     * <li>4 cores per processor (8 cores in total)
     * <li>2 threads per core (16 threads in total)
     * <li>===
     * <li>12GB system RAM
     * <li>8MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>OCTOPUS / MacBookPro16,1
     * <ul>
     * <li>1 processors
     * <li>8 cores per processor (8 cores in total)
     * <li>2 threads per core (16 threads in total)
     * <li>===
     * <li>64GB system RAM
     * <li>16MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__16 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(64L * K * K * K, 16), new BasicMachine(8L * K * K, 16),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>CBL (prod & test) 2 x Intel(R) Xeon(R) CPU E5-2697A v4 @ 2.60GHz
     * <ul>
     * <li>2 processors
     * <li>16 cores per processor (32 cores in total)
     * <li>2 threads per core (64 threads in total)
     * <li>===
     * <li>512GB system RAM
     * <li>40MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * <li>CBF (simu) 4 x Intel(R) Xeon(R) CPU E7-4809 v3 @ 2.00GHz
     * <ul>
     * <li>4 processors
     * <li>8 cores per processor (32 cores in total)
     * <li>2 threads per core (64 threads in total)
     * <li>===
     * <li>512GB system RAM
     * <li>20MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__64 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(512L * K * K * K, 64), new BasicMachine(20L * K * K, 32),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>CBF (prod) 4 x Intel(R) Xeon(R) CPU E7-4830 v3 @ 2.10GHz
     * <ul>
     * <li>4 processors
     * <li>12 cores per processor (48 cores in total)
     * <li>2 threads per core (96 threads in total)
     * <li>===
     * <li>512GB system RAM
     * <li>30MB L3 cache per processor
     * <li>256kB L2 cache per core
     * <li>32kB L1 cache per core
     * </ul>
     * </ul>
     */
    static final Hardware X86_64__96 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(512L * K * K * K, 96), new BasicMachine(30L * K * K, 24),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    static {
        PREDEFINED.add(AARCH64__08);
        PREDEFINED.add(PPC__01);
        PREDEFINED.add(X86__01);
        PREDEFINED.add(X86__02);
        PREDEFINED.add(X86_64__02);
        PREDEFINED.add(X86_64__04);
        //        PREDEFINED.add(X86_64.X86_64__04_2);
        //        PREDEFINED.add(X86_64.X86_64__04_1_L2);
        //        PREDEFINED.add(X86_64.X86_64__04_1_L3);
        PREDEFINED.add(X86_64__08);
        PREDEFINED.add(X86_64__12);
        PREDEFINED.add(X86_64__16);
        PREDEFINED.add(X86_64__64);
        PREDEFINED.add(X86_64__96);
    }

    public static Hardware makeSimple() {
        return Hardware.makeSimple(VirtualMachine.getArchitecture(), VirtualMachine.getMemory(), VirtualMachine.getThreads());
    }

    public static Hardware makeSimple(final String systemArchitecture, final long systemMemory, final int systemThreads) {

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

    private final BasicMachine[] myLevels;

    /**
     * <code>new BasicMachine[] { SYSTEM, L3, L2, L1 }</code> or
     * <code>new BasicMachine[] { SYSTEM, L2, L1 }</code> or in worst case
     * <code>new BasicMachine[] { SYSTEM, L1 }</code>
     */
    public Hardware(final String arch, final BasicMachine[] levels) {

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
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof Hardware)) {
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
        return prime * result + Arrays.hashCode(myLevels);
    }

    public boolean isL2Specified() {
        return myLevels.length > 2;
    }

    public boolean isL3Specified() {
        return myLevels.length > 3;
    }

    @Override
    public String toString() {

        StringBuilder retVal = new StringBuilder("HW=");

        retVal.append(myLevels[0].toString());
        if (this.isL3Specified()) {
            retVal.append(ASCII.COMMA).append(units).append("xL3:").append(myLevels[myLevels.length - 3]);
        } else if (this.isL2Specified()) {
            retVal.append(ASCII.COMMA).append(units).append("xL2:").append(myLevels[myLevels.length - 2]);
        }
        retVal.append(ASCII.COMMA).append(cores).append("cores:").append(myLevels[myLevels.length - 1]);

        return retVal.toString();
    }

    public VirtualMachine virtualise() {
        return new VirtualMachine(this, Runtime.getRuntime());
    }

}
