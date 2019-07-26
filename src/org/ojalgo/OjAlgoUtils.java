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
package org.ojalgo;

import java.util.Date;

import org.ojalgo.machine.Hardware;
import org.ojalgo.machine.VirtualMachine;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.type.StandardType;

public abstract class OjAlgoUtils {

    /**
     * This is set for you, but you may want to set it to something different/better. Create a
     * {@linkplain Hardware} instance and then call {@linkplain Hardware#virtualise()}. The idea is that the
     * {@linkplain Hardware} instance should match the actual hardware, but the {@linkplain VirtualMachine}
     * can optionally be limited to only let ojAlgo "see" a subset of the cores/threads. Changing this must be
     * the very first thing you do with ojAlgo.
     */
    public static VirtualMachine ENVIRONMENT = null;

    static {

        String architecture = VirtualMachine.getArchitecture();
        long memory = VirtualMachine.getMemory();
        int threads = VirtualMachine.getThreads();

        for (Hardware hw : Hardware.PREDEFINED) {
            if (hw.architecture.equals(architecture) && (hw.threads <= threads) && (hw.memory >= memory)) {
                ENVIRONMENT = hw.virtualise();
            }
        }

        if (ENVIRONMENT == null) {
            if (System.getProperty("shut.up.ojAlgo") == null) {
                BasicLogger.debug("ojAlgo includes a small set of predefined hardware profiles,");
                BasicLogger.debug("none of which were deemed suitable for the hardware you're currently using.");
                BasicLogger.debug("A default hardware profile, that is perfectly usable, has been set for you.");
                BasicLogger.debug("You may want to set org.ojalgo.OjAlgoUtils.ENVIRONMENT to something that");
                BasicLogger.debug("better matches the hardware/OS/JVM you're running on, than the default.");
                BasicLogger.debug("Additionally it would be appreciated if you contribute your hardware profile:");
                BasicLogger.debug("https://github.com/optimatika/ojAlgo/issues");
                BasicLogger.debug("Architecture={} Threads={} Memory={}", architecture, threads, memory);
            }
            ENVIRONMENT = Hardware.makeSimple(architecture, memory, threads).virtualise();
        }
    }

    /**
     * @see Package#getSpecificationVersion()
     */
    public static String getDate() {

        String manifestValue = OjAlgoUtils.class.getPackage().getSpecificationVersion();

        return manifestValue != null ? manifestValue : StandardType.SQL_DATE.format(new Date());
    }

    /**
     * @see Package#getImplementationTitle()
     */
    public static String getTitle() {

        String manifestValue = OjAlgoUtils.class.getPackage().getImplementationTitle();

        return manifestValue != null ? manifestValue : "ojAlgo";
    }

    /**
     * @see Package#getImplementationVendor()
     */
    public static String getVendor() {

        String manifestValue = OjAlgoUtils.class.getPackage().getImplementationVendor();

        return manifestValue != null ? manifestValue : "Optimatika";
    }

    /**
     * @see Package#getImplementationVersion()
     */
    public static String getVersion() {

        String manifestValue = OjAlgoUtils.class.getPackage().getImplementationVersion();

        return manifestValue != null ? manifestValue : "X.X";
    }

    /**
     * With several CPU cores present you can limit the number of threads used by ojAlgo by defining how many
     * of the cores ojAlgo should "see".
     *
     * @param maxCores The number of CPU cores avaibale to ojAlgo
     */
    public static void limitCoresTo(final int maxCores) {
        ENVIRONMENT = ENVIRONMENT.limitCores(maxCores);
    }

    /**
     * @param maxThreads The number of CPU threads avaibale to ojAlgo
     */
    public static void limitThreadsTo(final int maxThreads) {
        ENVIRONMENT = ENVIRONMENT.limitThreads(maxThreads);
    }

    /**
     * With several CPU:s present you can limit the number of threads used by ojAlgo by defining how many of
     * the CPU:s ojAlgo should "see".
     *
     * @param maxUnits The number of CPU:s avaibale to ojAlgo
     */
    public static void limitUnitsTo(final int maxUnits) {
        ENVIRONMENT = ENVIRONMENT.limitUnits(maxUnits);
    }

    public static void main(final String[] args) {
        BasicLogger.debug();
        BasicLogger.debug("####################################################################");
        BasicLogger.debug("#################### Welcome to oj! Algorithms! ####################");
        BasicLogger.debug("####################################################################");
        BasicLogger.debug("{} version {} built by {}.", OjAlgoUtils.getTitle(), OjAlgoUtils.getVersion(), OjAlgoUtils.getVendor());
        BasicLogger.debug("####################################################################");
        BasicLogger.debug();
        BasicLogger.debug("Machine Architecture: {}", VirtualMachine.getArchitecture());
        BasicLogger.debug("Machine Threads: {}", VirtualMachine.getThreads());
        BasicLogger.debug("Machine Memory: {}", VirtualMachine.getMemory());
        BasicLogger.debug();
        BasicLogger.debug("ojAlgo Environment: {}", ENVIRONMENT);
        BasicLogger.debug();
        BasicLogger.debug("System properties: {}", System.getProperties());
        BasicLogger.debug();
    }

    private OjAlgoUtils() {
        super();
    }

}
