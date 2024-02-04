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

import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.type.IntCount;

/**
 * Stuff common to {@link Hardware} and {@link VirtualMachine}.
 *
 * @author apete
 */
public abstract class CommonMachine extends BasicMachine {

    static final long K = 1024L;

    static long elements(final long availableMemory, final long elementSize) {
        return availableMemory / elementSize;
    }

    public final String architecture;//x86_64

    /**
     * The size of one top level (L3 or L2) cache unit in bytes.
     */
    public final long cache;
    /**
     * The total number of processor cores.
     */
    public final int cores;
    /**
     * The number of top level (L3 or L2) cache units. If there is a L3 cache this usually corresponds to the
     * number of CPU:s.
     */
    public final int units;

    protected CommonMachine(final Hardware hardware, final Runtime runtime) {

        super(Math.min(hardware.memory, runtime.maxMemory()), Math.min(hardware.threads, runtime.availableProcessors()));

        architecture = hardware.architecture;

        cache = hardware.cache;

        cores = hardware.cores;
        units = hardware.units;
    }

    /**
     * <code>new MemoryThreads[] { SYSTEM, L3, L2, L1 }</code> or
     * <code>new MemoryThreads[] { SYSTEM, L2, L1 }</code> or in worst case
     * <code>new MemoryThreads[] { SYSTEM, L1 }</code>
     */
    protected CommonMachine(final String arch, final BasicMachine[] levels) {

        super(levels[0].memory, levels[0].threads);

        architecture = arch;

        cores = threads / levels[levels.length - 1].threads;
        cache = levels[1].memory;
        units = threads / levels[1].threads;
    }

    CommonMachine(final VirtualMachine base, final int modUnits, final int modCores, final int modThreads) {
        super(base.memory, modThreads);
        architecture = base.architecture;
        cache = base.cache;
        cores = modCores;
        units = modUnits;
    }

    /**
     * The total amount of top level (L3 or L2) cache memory in bytes.
     */
    public final long cache() {
        return cache * units;
    }

    /**
     * @deprecated v53 Use {@link Parallelism#CORES} instead.
     */
    @Deprecated
    public final IntCount countCores() {
        return new IntCount(cores);
    }

    /**
     * @deprecated v53 Use {@link Parallelism#THREADS} instead.
     */
    @Deprecated
    public final IntCount countThreads() {
        return new IntCount(threads);
    }

    /**
     * @deprecated v53 Use {@link Parallelism#UNITS} instead.
     */
    @Deprecated
    public final IntCount countUnits() {
        return new IntCount(units);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || !(obj instanceof CommonMachine)) {
            return false;
        }
        CommonMachine other = (CommonMachine) obj;
        if (architecture == null) {
            if (other.architecture != null) {
                return false;
            }
        } else if (!architecture.equals(other.architecture)) {
            return false;
        }
        if (cache != other.cache || cores != other.cores || units != other.units) {
            return false;
        }
        return true;
    }

    /**
     * @deprecated v53
     */
    @Deprecated
    public final long getCacheElements(final long elementSize) {
        return CommonMachine.elements(cache, elementSize);
    }

    /**
     * @deprecated v53
     */
    @Deprecated
    public final long getMemoryElements(final long elementSize) {
        return CommonMachine.elements(memory, elementSize);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (architecture == null ? 0 : architecture.hashCode());
        result = prime * result + (int) (cache ^ cache >>> 32);
        result = prime * result + cores;
        return prime * result + units;
    }

    public final boolean isMultiCore() {
        return cores > 1;
    }

    public final boolean isMultiThread() {
        return threads > 1;
    }

    public final boolean isMultiUnit() {
        return units > 1;
    }

}
