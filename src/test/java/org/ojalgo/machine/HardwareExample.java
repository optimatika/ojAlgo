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

/**
 * Please provide more different examples – you can get the details of the system you're currently using.
 */
class HardwareExample extends MachineTests {

    /**
     * Apple M1 Pro
     * <ul>
     * <li>L1 Cache the high-perf cores have a large 192 KB of L1 instruction cache and 128 KB of L1 data
     * cache The energy-efficient cores have a 128 KB L1 instruction cache, 64 KB L1 data cache.
     * <li>L2 Cache (28MB all together) The 6 high-perf cores are split in two clusters, each cluster has 12MB
     * of shared L2 cache (so 24MB total) The 2 high-efficiency cores have 4MB of shared L2 cache
     * <li>L3 / SLC (24MB all together) The SLC is 12MB per memory controller, so 24MB total.
     * <li>16 GB unified memory
     * </ul>
     */
    public static final Hardware AARCH64__08_M1_PRO = new Hardware(Hardware.Architecture.AARCH64, new BasicMachine[] { new BasicMachine(16L * K * K * K, 8),
            new BasicMachine(24L * K * K, 8), new BasicMachine(28L * K * K, 8), new BasicMachine(128L * K, 1) });

    /**
     * squid / 15" MacBook Air 2023, Apple M2
     * <ul>
     * <li>8 cores (4 performance and 4 efficiency)
     * <li>L1: Performance cores 192+128 KB per core / Efficiency cores 128+64 KB per core
     * <li>L2: Performance cores 16 MB / Efficiency cores 4 MB
     * <li>L3: 8 MB
     * <li>24 GB unified memory
     * </ul>
     */
    public static final Hardware AARCH64__08_M2 = new Hardware(Hardware.Architecture.AARCH64, new BasicMachine[] { new BasicMachine(24L * K * K * K, 8),
            new BasicMachine(8L * K * K, 8), new BasicMachine(4L * K * K, 4), new BasicMachine(64L * K, 1) });

    /**
     * Latitude 5520 with a 11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz, 1805 Mhz, 4 Core(s), 8 Logical
     * Processor(s)
     */
    public static final Hardware DELL_LATITUDE_5520 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(32L * K * K * K, 8),
            new BasicMachine(12L * K * K, 8), new BasicMachine(5L * 256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * CLAM / PowerBook6,5
     * <ul>
     * <li>1 processor
     * <li>1 core per processor
     * <li>1 thread per core
     * <li>===
     * <li>1.25GB system RAM
     * <li>512kB L2 cache per processor
     * <li>64kB L1 cache per core
     * </ul>
     */
    public static final Hardware PPC__01 = new Hardware(Hardware.Architecture.PPC,
            new BasicMachine[] { new BasicMachine(5L * 256L * K * K, 1), new BasicMachine(512L * K, 1), new BasicMachine(64L * K, 1) });

    /**
     * INTEL1
     * <ul>
     * <li>1 processor
     * <li>1 core per processor
     * <li>1 thread per core
     * <li>===
     * <li>1GB system RAM
     * <li>1MB L2 cache per processor
     * <li>32kB L1 cache per core
     * </ul>
     */
    public static final Hardware X86__01 = new Hardware(Hardware.Architecture.X86,
            new BasicMachine[] { new BasicMachine(1L * K * K * K, 1), new BasicMachine(1L * K * K, 1), new BasicMachine(32L * K, 1) });
    /**
     * B5950053
     * <ul>
     * <li>1 processor
     * <li>2 cores per processor
     * <li>1 thread per core
     * <li>===
     * <li>3.5GB system RAM
     * <li>6MB L2 cache per processor (2 cores)
     * <li>32kB L1 cache per core
     * </ul>
     */
    public static final Hardware X86__02 = new Hardware(Hardware.Architecture.X86,
            new BasicMachine[] { new BasicMachine(7L * 512L * K * K, 2), new BasicMachine(6L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * MANTA / iMac7,1
     * <ul>
     * <li>1 processor
     * <li>2 cores per processor
     * <li>1 thread per core
     * <li>===
     * <li>3GB system RAM
     * <li>4MB L2 cache per processor (2 cores)
     * <li>32kB L1 cache per core
     * </ul>
     */
    public static final Hardware X86_64__02 = new Hardware(Hardware.Architecture.X86_64,
            new BasicMachine[] { new BasicMachine(3L * K * K * K, 2), new BasicMachine(4L * K * K, 2), new BasicMachine(32L * K, 1) });
    /**
     * PA's Q6600
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>8GB system RAM
     * <li>4MB L2 cache per 2 cores
     * <li>32kB L1 cache per core
     * </ul>
     */
    public static final Hardware X86_64__04_1_L2_Q6600 = new Hardware(Hardware.Architecture.X86_64,
            new BasicMachine[] { new BasicMachine(8L * K * K * K, 4), new BasicMachine(4L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * PA's Q9400
     * <ul>
     * <li>1 processors
     * <li>4 cores per processor
     * <li>1 thread per core (4 threads in total)
     * <li>===
     * <li>3GB system RAM
     * <li>3MB L2 cache per 2 cores
     * <li>32kB L1 cache per core
     * </ul>
     */
    public static final Hardware X86_64__04_1_L2_Q9400 = new Hardware(Hardware.Architecture.X86_64,
            new BasicMachine[] { new BasicMachine(3L * K * K * K, 4), new BasicMachine(3L * K * K, 2), new BasicMachine(32L * K, 1) });

    /**
     * Intel Core i5-3570K with 32GB of RAM (from Java Matrix Benchmark)
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
     */
    public static final Hardware X86_64__04_1_L3_I5_3570K = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] {
            new BasicMachine(32L * K * K * K, 4), new BasicMachine(6L * K * K, 4), new BasicMachine(256L * K, 1), new BasicMachine(32L * K, 1) });

    /**
     * Intel i5-4670K with 16GB of RAM
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
     */
    public static final Hardware X86_64__04_1_L3_I5_4670K = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] {
            new BasicMachine(16L * K * K * K, 4), new BasicMachine(6L * K * K, 4), new BasicMachine(256L * K, 1), new BasicMachine(32L * K, 1) });

    /**
     * BUBBLE / MacBookAir4,2
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
     */
    public static final Hardware X86_64__04_2_BUBBLE = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(4L * K * K * K, 4),
            new BasicMachine(3L * K * K, 4), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * PA's Intel Core i7-620M laptop
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
     */
    public static final Hardware X86_64__04_2_I7_620M = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(8L * K * K * K, 4),
            new BasicMachine(4L * K * K, 4), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * MacBookPro14,2 (oyster)
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
     */
    public static final Hardware X86_64__04_2_OYSTER = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(8L * K * K * K, 4),
            new BasicMachine(4L * K * K, 4), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * Google Cloud Platform Compute Engine n1-standard-8 (8 vCPUs, 30 GB memory, Skylake)
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
     */
    public static final Hardware X86_64__08_GCP_N1_STANDARD_8 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] {
            new BasicMachine(30L * K * K * K, 8), new BasicMachine(8250L * K, 8), new BasicMachine(1L * K * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * Core i7-2600 3.4 GHz - 4 cores - 8 threads from Java Matrix Benchmark
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
     */
    public static final Hardware X86_64__08_I7_2600_JMB = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(11L * K * K * K, 8),
            new BasicMachine(8L * K * K, 8), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * Core i7-2600 3.4 GHz - 4 cores - 8 threads (Vostro-460 @ Scila)
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
     */
    public static final Hardware X86_64__08_I7_2600_VOSTRO = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] {
            new BasicMachine(32L * K * K * K, 8), new BasicMachine(8L * K * K, 8), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * Core i7-3770 3.4 GHz - 4 cores - 8 threads (whale @ MSC/MSB)
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
     */
    public static final Hardware X86_64__08_I7_3770 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(8L * K * K * K, 8),
            new BasicMachine(8L * K * K, 8), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * HA's Intel Core i7-920 server
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
     */
    public static final Hardware X86_64__08_I7_920 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(8L * K * K * K, 8),
            new BasicMachine(8L * K * K, 8), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <pre>
     * "Gulftown" (32 nm) Model: SLBUZ (B1)
     * Intel Core i7-980 3.33GHz
     * 8/25/2010
     * ref: http://ark.intel.com/products/47932
     *      https://en.wikipedia.org/wiki/List_of_Intel_Core_i7_microprocessors
     *      Device Manager
     * </pre>
     *
     * Intel Core i7-980
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
     */
    public static final Hardware X86_64__12 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(12L * K * K * K, 12),
            new BasicMachine(12L * K * K, 12), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * OCTOPUS / MacBookPro16,1
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
     */
    public static final Hardware X86_64__16_OCTOPUS = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(64L * K * K * K, 16),
            new BasicMachine(16L * K * K, 16), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * SAILFISH / MacPro4,1
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
     */
    public static final Hardware X86_64__16_SAILFISH = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(12L * K * K * K, 16),
            new BasicMachine(8L * K * K, 8), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * CBF (simu) 4 x Intel(R) Xeon(R) CPU E7-4809 v3 @ 2.00GHz
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
     */
    public static final Hardware X86_64__64_CBF_SIMU = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(512L * K * K * K, 64),
            new BasicMachine(20L * K * K, 32), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * CBL (prod & test) 2 x Intel(R) Xeon(R) CPU E5-2697A v4 @ 2.60GHz
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
     */
    public static final Hardware X86_64__64_CBL = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(512L * K * K * K, 64),
            new BasicMachine(40L * K * K, 32), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * CBF (prod) 4 x Intel(R) Xeon(R) CPU E7-4830 v3 @ 2.10GHz
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
     */
    public static final Hardware X86_64__96 = new Hardware(Hardware.Architecture.X86_64, new BasicMachine[] { new BasicMachine(512L * K * K * K, 96),
            new BasicMachine(30L * K * K, 24), new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

}
