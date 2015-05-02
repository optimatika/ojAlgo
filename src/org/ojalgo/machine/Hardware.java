/*
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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
import java.util.TreeSet;

import org.ojalgo.ProgrammingError;
import org.ojalgo.array.ArrayUtils;
import org.ojalgo.netio.ASCII;

/**
 * <ul>
 * <li>The first element in the array should correspond to total system resources; the total amount of RAM and
 * the total number of threads (Typically the same as what is returned by
 * {@linkplain Runtime#availableProcessors()}).</li>
 * <li>The last element in the array should describe the L1 cache. Typically Intel processors have 32k L1
 * cache and AMD 64k. 1 or maybe 2 threads use/share this cache.</li>
 * <li>Caches, all levels except L1, are described between the first and last elements in descending order (L3
 * cache comes before L2 cache). Specify the size of the cache and the number of threads using/sharing the
 * cache. (Do not worry about how many cache units there are - describe one unit.)</li>
 * <li>The array must have at least 2 elements. You must describe the total system resources and the L1 cache.
 * It is strongly recommended to also describe the L2 cache. The L3 cache, if it exists, is less important to
 * describe. The derived attributes <code>processors</code>, <code>cores</code> and <code>units</code> may be
 * incorrectly calculated if you fail to specify the caches. Known issue: If you have more than one processor,
 * nut no L3 cache; the <code>processors</code> attribute will be incorrectly set 1. A workaround that
 * currently works is to define an L3 cache anyway and set the memory/size of that cache to 0bytes. This
 * workoround may stop working in the future.</li>
 * <li>
 * <code>new MemoryThreads[] { SYSTEM, L3, L2, L1 }</code> or
 * <code>new MemoryThreads[] { SYSTEM, L2, L1 }</code> or <code>new MemoryThreads[] { SYSTEM, L1 }</code></li>
 * </ul>
 *
 * @author apete
 */
public final class Hardware extends AbstractMachine implements Comparable<Hardware> {

    public static Hardware makeSimple() {
        return Hardware.makeSimple(VirtualMachine.getArchitecture(), VirtualMachine.getMemory(), VirtualMachine.getThreads());
    }

    public static Hardware makeSimple(final String systemArchitecture, final long systemMemory, final int systemThreads) {

        if (systemThreads > 8) {
            // Assume hyperthreading, L3 cache and more than 1 CPU

            final BasicMachine tmpL1Machine = new BasicMachine(32L * K, 2); //Hyperthreading

            final BasicMachine tmpL2Machine = new BasicMachine(256L * K, tmpL1Machine.threads);

            final BasicMachine tmpL3Machine = new BasicMachine(4L * K * K, systemThreads / ((systemThreads + 7) / 8)); //More than 1 CPU

            final BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL3Machine, tmpL2Machine, tmpL1Machine });

        } else if (systemThreads >= 4) {
            // Assume hyperthreading, L3 cache but only 1 CPU

            final BasicMachine tmpL1Machine = new BasicMachine(32L * K, 2); //Hyperthreading

            final BasicMachine tmpL2Machine = new BasicMachine(256L * K, tmpL1Machine.threads);

            final BasicMachine tmpL3Machine = new BasicMachine(3L * K * K, systemThreads);

            final BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL3Machine, tmpL2Machine, tmpL1Machine });

        } else {
            // No hyperthreading, no L3 cache and 1 CPU

            final BasicMachine tmpL1Machine = new BasicMachine(32L * K, 1); //No hyperthreading

            final BasicMachine tmpL2Machine = new BasicMachine(2L * K * K, tmpL1Machine.threads);

            final BasicMachine tmpSystemMachine = new BasicMachine(systemMemory, systemThreads);

            return new Hardware(systemArchitecture, new BasicMachine[] { tmpSystemMachine, tmpL2Machine, tmpL1Machine });
        }
    }

    /**
     * Should contain all available hardware in ascending "power" order.
     */
    public static final TreeSet<Hardware> PREDEFINED = new TreeSet<Hardware>();

    /**
     * <ul>
     * <li>CLAM / PowerBook6,5
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>1 core per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 1 threads</li>
     * <li></li>
     * <li>1.25GB system RAM</li>
     * <li>512kB L2 cache per processor</li>
     * <li>64kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware PPC__01 = new Hardware("ppc", new BasicMachine[] { new BasicMachine(5L * 256L * K * K, 1), new BasicMachine(512L * K, 1),
            new BasicMachine(64L * K, 1) });

    /**
     * <ul>
     * <li>INTEL1
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>1 core per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 1 threads</li>
     * <li></li>
     * <li>1GB system RAM</li>
     * <li>1MB L2 cache per processor</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86__01 = new Hardware("x86", new BasicMachine[] { new BasicMachine(1L * K * K * K, 1), new BasicMachine(1L * K * K, 1),
            new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>B5950053
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>2 cores per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 2 threads</li>
     * <li></li>
     * <li>3.5GB system RAM</li>
     * <li>6MB L2 cache per processor (2 cores)</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86__02 = new Hardware("x86", new BasicMachine[] { new BasicMachine(7L * 512L * K * K, 2), new BasicMachine(6L * K * K, 2),
            new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>MANTA / iMac7,1
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>2 cores per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 2 threads</li>
     * <li></li>
     * <li>3GB system RAM</li>
     * <li>4MB L2 cache per processor (2 cores)</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86_64__02 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(3L * K * K * K, 2), new BasicMachine(4L * K * K, 2),
            new BasicMachine(32L * K, 1) });

    /**
     * <ul>
     * <li>BUBBLE / MacBookAir4,2
     * <ul>
     * <li></li>
     * <li>1 processors</li>
     * <li>2 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 4 threads</li>
     * <li></li>
     * <li>4GB system RAM</li>
     * <li>3MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * <li>Peter Abeles's (EJML) Intel Core i7-620M laptop
     * <ul>
     * <li></li>
     * <li>1 processors</li>
     * <li>2 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 4 threads</li>
     * <li></li>
     * <li>8GB system RAM</li>
     * <li>4MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * <li>Peter Abeles' (EJML) Q9400
     * <ul>
     * <li></li>
     * <li>1 processors</li>
     * <li>4 cores per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 4 threads</li>
     * <li></li>
     * <li>3GB system RAM</li>
     * <li>3MB L2 cache per 2 cores</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * <li>Peter Abeles' (EJML) Q6600
     * <ul>
     * <li></li>
     * <li>1 processors</li>
     * <li>4 cores per processor</li>
     * <li>1 thread per core</li>
     * <li>==>> Total 4 threads</li>
     * <li></li>
     * <li>8GB system RAM</li>
     * <li>4MB L2 cache per 2 cores</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86_64__04 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(8L * K * K * K, 4), new BasicMachine(3L * K * K, 4),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>Holger Arndt's (UJMP) Intel Core i7-920 server
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>4 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 8 threads</li>
     * <li></li>
     * <li>8GB system RAM</li>
     * <li>8MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * <li>Core i7-2600 3.4 GHz - 4 cores - 8 threads from Java Matrix Benchmark
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>4 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 8 threads</li>
     * <li></li>
     * <li>11GB system RAM</li>
     * <li>8MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * <li>Core i7-3770 3.4 GHz - 4 cores - 8 threads (whale @ MSC/MSB)
     * <ul>
     * <li></li>
     * <li>1 processor</li>
     * <li>4 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 8 threads</li>
     * <li></li>
     * <li>8GB system RAM</li>
     * <li>8MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86_64__08 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(11L * K * K * K, 8), new BasicMachine(8L * K * K, 8),
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
     * <li>1 processor</li>
     * <li>6 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 12 threads</li>
     * <li></li>
     * <li>12GB system RAM</li>
     * <li>12MB L3 cache per processor</li>
     * <li>256kB L2 cache per core (x6)</li>
     * <li>32kB L1 cache per core (x6)</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86_64__12 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(12L * K * K * K, 12), new BasicMachine(12L * K * K, 12),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    /**
     * <ul>
     * <li>SAILFISH / MacPro4,1
     * <ul>
     * <li></li>
     * <li>2 processors</li>
     * <li>4 cores per processor</li>
     * <li>2 threads per core</li>
     * <li>==>> Total 16 threads</li>
     * <li></li>
     * <li>12GB system RAM</li>
     * <li>8MB L3 cache per processor</li>
     * <li>256kB L2 cache per core</li>
     * <li>32kB L1 cache per core</li>
     * </ul>
     * </li>
     * </ul>
     */
    static final Hardware X86_64__16 = new Hardware("x86_64", new BasicMachine[] { new BasicMachine(12L * K * K * K, 16), new BasicMachine(8L * K * K, 8),
            new BasicMachine(256L * K, 2), new BasicMachine(32L * K, 2) });

    static {
        PREDEFINED.add(PPC__01);
        PREDEFINED.add(X86__01);
        PREDEFINED.add(X86__02);
        PREDEFINED.add(X86_64__02);
        PREDEFINED.add(X86_64__04);
        PREDEFINED.add(X86_64__08);
        PREDEFINED.add(X86_64__12);
        PREDEFINED.add(X86_64__16);
    }

    private final BasicMachine[] myLevels;

    /**
     * <code>new BasicMachine[] { SYSTEM, L3, L2, L1 }</code> or
     * <code>new BasicMachine[] { SYSTEM, L2, L1 }</code> or in worst case
     * <code>new BasicMachine[] { SYSTEM, L1 }</code>
     */
    public Hardware(final String architecture, final BasicMachine[] levels) {

        super(architecture, levels);

        if (levels.length < 2) {
            throw new IllegalArgumentException();
        }

        myLevels = ArrayUtils.copyOf(levels);
    }

    private Hardware(final Hardware aHardware, final Runtime aRuntime) {

        super(aHardware, aRuntime);

        myLevels = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    public int compareTo(final Hardware aReference) {
        if (units != aReference.units) {
            return units - aReference.units;
        } else if (cache != aReference.cache) {
            return (int) (cache - aReference.cache);
        } else if (cores != aReference.cores) {
            return cores - aReference.cores;
        } else if (threads != aReference.threads) {
            return threads - aReference.threads;
        } else if (memory != aReference.memory) {
            return (int) (memory - aReference.memory);
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
        final Hardware other = (Hardware) obj;
        if (!Arrays.equals(myLevels, other.myLevels)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + Arrays.hashCode(myLevels);
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

        final StringBuilder retVal = new StringBuilder("HW=");

        retVal.append(myLevels[0].toString());
        if (this.isL3Specified()) {
            retVal.append(ASCII.COMMA).append(units).append("xL3:").append(myLevels[myLevels.length - 3]);
        } else if (this.isL2Specified()) {
            retVal.append(ASCII.COMMA).append(units).append("xL2:").append(myLevels[myLevels.length - 2]);
        }
        retVal.append(ASCII.COMMA).append(cores).append("cores:").append(myLevels[myLevels.length - 1]);

        return retVal.toString();
    }

    public final VirtualMachine virtualise() {
        return new VirtualMachine(this, Runtime.getRuntime());
    }

}
