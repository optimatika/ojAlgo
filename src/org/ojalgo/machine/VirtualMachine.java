/*
 * Copyright 1997-2018 Optimatika
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

import org.ojalgo.ProgrammingError;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.netio.ASCII;
import org.ojalgo.netio.BasicLogger;

public final class VirtualMachine extends AbstractMachine {

    private static final String AMD64 = "amd64";
    private static final String I386 = "i386";
    private static final String X86 = "x86";
    private static final String X86_64 = "x86_64";

    public static String getArchitecture() {

        // http://fantom.org/sidewalk/topic/756

        final String tmpProperty = System.getProperty("os.arch").toLowerCase();

        if (tmpProperty.equals(I386)) {
            return X86;
        } else if (tmpProperty.equals(AMD64)) {
            return X86_64;
        } else {
            return tmpProperty;
        }
    }

    public static long getMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static int getThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    private final Hardware myHardware;
    private final Runtime myRuntime;

    private VirtualMachine(final String architecture, final BasicMachine[] levels) {

        super(architecture, levels);

        myHardware = null;
        myRuntime = null;

        ProgrammingError.throwForIllegalInvocation();
    }

    VirtualMachine(final Hardware hardware, final Runtime runtime) {

        super(hardware, runtime);

        myHardware = hardware;
        myRuntime = runtime;
    }

    public void collectGarbage() {

        myRuntime.runFinalization();

        long tmpIsFree = myRuntime.freeMemory();
        long tmpWasFree;

        do {
            tmpWasFree = tmpIsFree;
            myRuntime.gc();
            try {
                Thread.sleep(8L);
            } catch (final InterruptedException exception) {
                BasicLogger.error(exception.getMessage());
            }
            tmpIsFree = myRuntime.freeMemory();
        } while (tmpIsFree > tmpWasFree);

        myRuntime.runFinalization();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VirtualMachine)) {
            return false;
        }
        final VirtualMachine other = (VirtualMachine) obj;
        if (myHardware == null) {
            if (other.myHardware != null) {
                return false;
            }
        } else if (!myHardware.equals(other.myHardware)) {
            return false;
        }
        return true;
    }

    public int getAvailableDim1D(final long elementSize) {
        return (int) AbstractMachine.elements(this.getAvailableMemory(), elementSize);
    }

    public int getAvailableDim2D(final long elementSize) {
        return (int) PrimitiveFunction.SQRT.invoke(AbstractMachine.elements(this.getAvailableMemory(), elementSize));
    }

    public long getAvailableMemory() {

        final long tmpMax = myRuntime.maxMemory();
        final long tmpTotal = myRuntime.totalMemory();
        final long tmpFree = myRuntime.freeMemory();

        final long tmpAvailable = (tmpMax - tmpTotal) + tmpFree;

        return tmpAvailable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((myHardware == null) ? 0 : myHardware.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + ASCII.SP + myHardware.toString();
    }

}
