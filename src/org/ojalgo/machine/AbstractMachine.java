/*
 * Copyright 1997-2019 Optimatika
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

import org.ojalgo.type.IntCount;

public abstract class AbstractMachine extends BasicMachine {

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
     * The number of top level (L3 or L2) cache units. With L3 cache defined this corresponds to the number of
     * CPU:s.
     */
    public final int units;

    protected AbstractMachine(final Hardware hardware, final Runtime runtime) {

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
    protected AbstractMachine(final String architecture, final BasicMachine[] levels) {

        super(levels[0].memory, levels[0].threads);

        this.architecture = architecture;

        cores = threads / levels[levels.length - 1].threads;
        cache = levels[1].memory;
        units = threads / levels[1].threads;
    }

    AbstractMachine(final VirtualMachine base, final int modUnits, final int modCores, final int modThreads) {
        super(base.memory, modThreads);
        architecture = base.architecture;
        cache = base.cache;
        cores = modCores;
        units = modUnits;
    }

    public IntCount countCores() {
        return new IntCount(cores);
    }

    public IntCount countThreads() {
        return new IntCount(threads);
    }

    public IntCount countUnits() {
        return new IntCount(units);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof AbstractMachine)) {
            return false;
        }
        AbstractMachine other = (AbstractMachine) obj;
        if (architecture == null) {
            if (other.architecture != null) {
                return false;
            }
        } else if (!architecture.equals(other.architecture)) {
            return false;
        }
        if (cache != other.cache) {
            return false;
        }
        if (cores != other.cores) {
            return false;
        }
        if (units != other.units) {
            return false;
        }
        return true;
    }

    public long getCacheElements(final long elementSize) {
        return AbstractMachine.elements(cache, elementSize);
    }

    public long getMemoryElements(final long elementSize) {
        return AbstractMachine.elements(memory, elementSize);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((architecture == null) ? 0 : architecture.hashCode());
        result = (prime * result) + (int) (cache ^ (cache >>> 32));
        result = (prime * result) + cores;
        result = (prime * result) + units;
        return result;
    }

    public boolean isMultiCore() {
        return cores > 1;
    }

    public boolean isMultiThread() {
        return threads > 1;
    }

    public boolean isMultiUnit() {
        return units > 1;
    }

}
