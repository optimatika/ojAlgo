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
package org.ojalgo;

import java.time.LocalDate;

import org.ojalgo.machine.Hardware;
import org.ojalgo.machine.VirtualMachine;
import org.ojalgo.matrix.operation.MatrixOperation;
import org.ojalgo.netio.BasicLogger;

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

        Hardware.Architecture architecture = Hardware.getArchitecture();
        long memory = Hardware.getMemory();
        int threads = Hardware.getThreads();

        try {

            ENVIRONMENT = Hardware.make(architecture, memory, threads).virtualise();

        } catch (Exception cause) {

            BasicLogger.error(cause, "Hardware detection failed: {}, {} bytes, {} threads", architecture, memory, threads);

            // Fallback to safe, common current parameters if hardware detection fails
            ENVIRONMENT = Hardware.make(Hardware.Architecture.X86_64, 16L * 1024L * 1024L * 1024L, 4).virtualise();
        }
    }

    /**
     * @see Package#getSpecificationVersion()
     */
    public static String getDate() {

        String manifestValue = OjAlgoUtils.class.getPackage().getSpecificationVersion();

        return manifestValue != null ? manifestValue : LocalDate.now().toString();
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

        return manifestValue != null ? manifestValue : "X.Y.Z";
    }

    /**
     * With several CPU cores present you can limit the number of threads used by ojAlgo by defining how many
     * of the cores ojAlgo should "see".
     *
     * @param maxCores The number of CPU cores available to ojAlgo
     */
    public static void limitCoresTo(final int maxCores) {
        double newCores = Math.max(1, Math.min(ENVIRONMENT.cores, maxCores));
        ENVIRONMENT = ENVIRONMENT.limitBy(newCores / ENVIRONMENT.cores);
    }

    public static void limitEnvironmentBy(final double fraction) {
        ENVIRONMENT = ENVIRONMENT.limitBy(fraction);
    }

    /**
     * @param maxThreads The number of CPU threads available to ojAlgo
     */
    public static void limitThreadsTo(final int maxThreads) {
        double newThreads = Math.max(1, Math.min(ENVIRONMENT.threads, maxThreads));
        ENVIRONMENT = ENVIRONMENT.limitBy(newThreads / ENVIRONMENT.threads);
    }

    /**
     * With several CPU:s present you can limit the number of threads used by ojAlgo by defining how many of
     * the CPU:s ojAlgo should "see".
     *
     * @param maxUnits The number of CPU:s available to ojAlgo
     */
    public static void limitUnitsTo(final int maxUnits) {
        double newUnits = Math.max(1, Math.min(ENVIRONMENT.units, maxUnits));
        ENVIRONMENT = ENVIRONMENT.limitBy(newUnits / ENVIRONMENT.units);
    }

    public static void main(final String[] args) {

        BasicLogger.debug();
        BasicLogger.debug("####################################################################");
        BasicLogger.debug("#################### Welcome to oj! Algorithms #####################");
        BasicLogger.debug("####################################################################");
        BasicLogger.debug("{} version {} built by {}.", OjAlgoUtils.getTitle(), OjAlgoUtils.getVersion(), OjAlgoUtils.getVendor());
        BasicLogger.debug("####################################################################");
        BasicLogger.debug();
        BasicLogger.debug("Machine Architecture: {}", Hardware.getArchitecture());
        BasicLogger.debug("Machine Memory: {} bytes", Hardware.getMemory());
        BasicLogger.debug("Machine Threads: {}", Hardware.getThreads());
        BasicLogger.debug();
        BasicLogger.debug("ojAlgo Environment: {}", ENVIRONMENT);
        BasicLogger.debug();
        BasicLogger.debug("System properties: {}", System.getProperties());
        BasicLogger.debug();
    }

    public static void pushUpConcurrencyThresholds(final int minValue) {
        MatrixOperation.setThresholdsMinValue(minValue);
    }

    private OjAlgoUtils() {
        super();
    }

}
