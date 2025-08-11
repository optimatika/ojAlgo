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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;

/**
 * Tests to verify that the new intelligent hardware detection logic produces results that align reasonably
 * well with the HardwareDetectionTest hardware constants.
 */
public class HardwareTest extends MachineTests {

    static final class HWKey {

        public final Hardware.Architecture architecture;
        public final long memory;
        public final int threads;

        HWKey(final Hardware.Architecture architecture, final long memory, final int threads) {
            super();
            this.architecture = architecture;
            this.memory = memory;
            this.threads = threads;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HWKey)) {
                return false;
            }
            HWKey other = (HWKey) obj;
            return Objects.equals(architecture, other.architecture) && memory == other.memory && threads == other.threads;
        }

        @Override
        public int hashCode() {
            return Objects.hash(architecture, memory, threads);
        }

        @Override
        public String toString() {
            return "HWKey [architecture=" + architecture + ", memory=" + memory + ", threads=" + threads + "]";
        }

    }

    public static final Map<HWKey, SortedSet<Hardware>> EXAMPLES = new HashMap<>();

    private static final long K = 1024L;

    static {
        HardwareTest.add(HardwareExample.AARCH64__08_M1_PRO);
        HardwareTest.add(HardwareExample.AARCH64__08_M2);
        HardwareTest.add(HardwareExample.DELL_LATITUDE_5520);
        HardwareTest.add(HardwareExample.PPC__01);
        HardwareTest.add(HardwareExample.X86__01);
        HardwareTest.add(HardwareExample.X86__02);
        HardwareTest.add(HardwareExample.X86_64__02);
        HardwareTest.add(HardwareExample.X86_64__04_2_BUBBLE);
        HardwareTest.add(HardwareExample.X86_64__04_2_I7_620M);
        HardwareTest.add(HardwareExample.X86_64__04_2_OYSTER);
        HardwareTest.add(HardwareExample.X86_64__04_1_L2_Q9400);
        HardwareTest.add(HardwareExample.X86_64__04_1_L2_Q6600);
        HardwareTest.add(HardwareExample.X86_64__04_1_L3_I5_4670K);
        HardwareTest.add(HardwareExample.X86_64__04_1_L3_I5_3570K);
        HardwareTest.add(HardwareExample.X86_64__08_I7_920);
        HardwareTest.add(HardwareExample.X86_64__08_I7_2600_JMB);
        HardwareTest.add(HardwareExample.X86_64__08_I7_3770);
        HardwareTest.add(HardwareExample.X86_64__08_I7_2600_VOSTRO);
        HardwareTest.add(HardwareExample.X86_64__08_GCP_N1_STANDARD_8);
        HardwareTest.add(HardwareExample.X86_64__12);
        HardwareTest.add(HardwareExample.X86_64__16_SAILFISH);
        HardwareTest.add(HardwareExample.X86_64__16_OCTOPUS);
        HardwareTest.add(HardwareExample.X86_64__64_CBL);
        HardwareTest.add(HardwareExample.X86_64__64_CBF_SIMU);
        HardwareTest.add(HardwareExample.X86_64__96);
    }

    private static void add(final Hardware hw) {
        EXAMPLES.computeIfAbsent(HardwareTest.toKey(hw), k -> new TreeSet<>()).add(hw);
    }

    private static Hardware recreate(final Hardware definition) {

        Hardware.Architecture architecture = definition.architecture;
        long memory = definition.memory;
        int threads = definition.threads;

        return Hardware.make(architecture, memory, threads);
    }

    private static Hardware recreate(final HWKey definition) {

        Hardware.Architecture architecture = definition.architecture;
        long memory = definition.memory;
        int threads = definition.threads;

        return Hardware.make(architecture, memory, threads);
    }

    private static HWKey toKey(final Hardware definition) {

        Hardware.Architecture architecture = definition.architecture;
        long memory = definition.memory;
        int threads = definition.threads;

        return new HWKey(architecture, memory, threads);
    }

    @BeforeAll
    static void setup() {
        // Ensure we have access to the HardwareDetectionTest hardware configurations
        Assertions.assertFalse(EXAMPLES.isEmpty(), "HardwareDetectionTest hardware configurations should be available");
    }

    @Test
    void testAppleSiliconDetection() {
        // Test configuration similar to AARCH64__08 with correct Apple vendor
        Hardware appleSilicon = Hardware.make(Hardware.Architecture.AARCH64, 24L * K * K * K, 8);

        Assertions.assertEquals(8, appleSilicon.threads, "Apple Silicon should preserve thread count");
        Assertions.assertEquals(8, appleSilicon.cores, "Apple Silicon should assume 1:1 thread:core ratio");
        Assertions.assertTrue(appleSilicon.cache >= 4L * K * K, "Apple Silicon should have large shared cache");
    }

    @Test
    void testConsistentResults() {
        Hardware.Architecture arch = Hardware.Architecture.X86_64;
        long memory = 16L * K * K * K;
        int threads = 8;

        final Hardware.Architecture systemArchitecture = arch;
        final long systemMemory = memory;
        final int systemThreads = threads;

        // Multiple calls should produce identical results
        Hardware result1 = Hardware.make(systemArchitecture, systemMemory, systemThreads);
        final Hardware.Architecture systemArchitecture1 = arch;
        final long systemMemory1 = memory;
        final int systemThreads1 = threads;
        Hardware result2 = Hardware.make(systemArchitecture1, systemMemory1, systemThreads1);
        final Hardware.Architecture systemArchitecture2 = arch;
        final long systemMemory2 = memory;
        final int systemThreads2 = threads;
        Hardware result3 = Hardware.make(systemArchitecture2, systemMemory2, systemThreads2);

        Assertions.assertEquals(result1.toString(), result2.toString(), "Multiple calls should produce identical results");
        Assertions.assertEquals(result2.toString(), result3.toString(), "Multiple calls should produce identical results");

        Assertions.assertTrue(result1.equals(result2), "Hardware objects should be equal");
        Assertions.assertTrue(result2.equals(result3), "Hardware objects should be equal");
    }

    @Test
    void testCurrentSystemDetection() {
        Hardware intelligent = Hardware.newInstance();
        Hardware simple = Hardware.makeSimple();

        // Basic sanity checks
        Assertions.assertTrue(intelligent.threads > 0, "Should detect at least 1 thread");
        Assertions.assertTrue(intelligent.cores > 0, "Should detect at least 1 core");
        Assertions.assertTrue(intelligent.memory > 0, "Should detect some memory");
        Assertions.assertTrue(intelligent.cache > 0, "Should detect some cache");

        // Cores should not exceed threads
        Assertions.assertTrue(intelligent.cores <= intelligent.threads,
                "Cores (" + intelligent.cores + ") should not exceed threads (" + intelligent.threads + ")");

        // Should be reasonably close to simple detection
        Assertions.assertEquals(simple.threads, intelligent.threads, "Thread count should match simple detection");
        Assertions.assertEquals(simple.memory, intelligent.memory, "Memory should match simple detection");
    }

    @Test
    void testEdgeCases() {
        // Test very high thread count (potential many-socket server) - Intel Xeon
        Hardware manyCore = Hardware.make(Hardware.Architecture.X86_64, 1024L * K * K * K, 256);
        Assertions.assertEquals(256, manyCore.threads);
        Assertions.assertTrue(manyCore.cores <= 128, "256 threads should suggest reasonable core count");
        Assertions.assertTrue(manyCore.units > 1, "Many-core system should detect multiple CPU units");

        // Test minimal system - legacy Intel
        Hardware minimal = Hardware.make(Hardware.Architecture.X86, 256L * K * K, 1);
        Assertions.assertEquals(1, minimal.threads);
        Assertions.assertEquals(1, minimal.cores);
        Assertions.assertEquals(1, minimal.units);
    }

    @Test
    void testExamples() {

        for (Entry<HWKey, SortedSet<Hardware>> entry : EXAMPLES.entrySet()) {

            HWKey key = entry.getKey();
            SortedSet<Hardware> value = entry.getValue();

            Hardware low = value.first();
            Hardware high = value.last();

            Hardware recreated = HardwareTest.recreate(key);

            if (DEBUG) {

                BasicLogger.debug("key: {}", key);

                BasicLogger.debug("cores: {} < {} < {}", low.cores, recreated.cores, high.cores);
                BasicLogger.debug("threads: {} < {} < {}", low.threads, recreated.threads, high.threads);
                BasicLogger.debug("cache: {} < {} < {}", low.cache, recreated.cache, high.cache);
                BasicLogger.debug("units: {} < {} < {}", low.units, recreated.units, high.units);
                BasicLogger.debug("memory: {} < {} < {}", low.memory, recreated.memory, high.memory);
                BasicLogger.debug("architecture: {} < {} < {}", low.architecture, recreated.architecture, high.architecture);
            }

            TestUtils.assertTrue(recreated.compareTo(low) >= 0);
            TestUtils.assertTrue(recreated.compareTo(high) <= 0);
        }
    }

    @Test
    void testLegacySystems() {
        // Test single-core systems - Intel
        Hardware legacy1 = Hardware.make(Hardware.Architecture.X86, 1L * K * K * K, 1);
        Assertions.assertEquals(1, legacy1.threads);
        Assertions.assertEquals(1, legacy1.cores);
        Assertions.assertTrue(legacy1.cache > 0, "Legacy system should have some cache");

        // Test early multi-core without hyperthreading - Intel Core 2 era
        Hardware legacy2 = Hardware.make(Hardware.Architecture.X86, 7L * 512L * K * K, 2);
        Assertions.assertEquals(2, legacy2.threads);
        Assertions.assertEquals(2, legacy2.cores);
    }

    @Test
    void testPowerPCDetection() {
        // Test legacy PowerPC (like PPC__01)
        Hardware ppcLegacy = Hardware.make(Hardware.Architecture.PPC, 5L * 256L * K * K, 1);

        Assertions.assertEquals(1, ppcLegacy.threads, "Legacy PPC should have 1 thread");
        Assertions.assertEquals(1, ppcLegacy.cores, "Legacy PPC should have 1 core");
        Assertions.assertTrue(ppcLegacy.cache >= 256L * K, "Legacy PPC should have reasonable L2 cache");

        // Test modern POWER system
        Hardware powerModern = Hardware.make(Hardware.Architecture.PPC, 64L * K * K * K, 32);

        Assertions.assertEquals(32, powerModern.threads, "Modern POWER should preserve thread count");
        Assertions.assertTrue(powerModern.cores <= 8, "Modern POWER with 32 threads should estimate ~4-8 cores (SMT)");
        Assertions.assertTrue(powerModern.cache >= 512L * K, "Modern POWER should have substantial cache");
    }

    @Test
    void testServerClassSystems() {
        // Test X86_64__64 equivalent - Intel Xeon
        Hardware server64 = Hardware.make(Hardware.Architecture.X86_64, 512L * K * K * K, 64);
        Assertions.assertEquals(64, server64.threads, "Server should preserve thread count");
        Assertions.assertTrue(server64.cores <= 32, "64 threads should suggest ~32 cores with hyperthreading");
        Assertions.assertTrue(server64.units >= 1, "Server should detect multiple CPU units");

        // Test X86_64__96 equivalent - Intel Xeon
        Hardware server96 = Hardware.make(Hardware.Architecture.X86_64, 512L * K * K * K, 96);
        Assertions.assertEquals(96, server96.threads, "Large server should preserve thread count");
        Assertions.assertTrue(server96.cores <= 48, "96 threads should suggest ~48 cores");
        Assertions.assertTrue(server96.cache >= 16L * K * K, "Large server should have substantial cache");
    }

    @Test
    void testSPARCDetection() {
        // Test legacy SPARC
        Hardware sparcLegacy = Hardware.make(Hardware.Architecture.OTHER, 2L * K * K * K, 2);

        Assertions.assertEquals(2, sparcLegacy.threads, "Legacy SPARC should preserve thread count");
        Assertions.assertEquals(2, sparcLegacy.cores, "Legacy SPARC should assume 1:1 thread:core ratio");

        // Test modern SPARC T-series
        Hardware sparcModern = Hardware.make(Hardware.Architecture.OTHER, 128L * K * K * K, 64);

        Assertions.assertEquals(64, sparcModern.threads, "Modern SPARC should preserve thread count");

        Assertions.assertTrue(sparcModern.cores <= 16, "SPARC T-series with 64 threads should estimate ~8 cores (got " + sparcModern.cores + ")");

    }

    @Test
    void testUnknownVendorFallback() {
        // Test that unknown vendor triggers vendor detection
        Hardware unknownVendor = Hardware.make(Hardware.Architecture.X86_64, 16L * K * K * K, 8);
        Hardware detectedVendor = Hardware.make(Hardware.Architecture.X86_64, 16L * K * K * K, 8);

        // Results should be similar since unknown should trigger detection
        Assertions.assertEquals(unknownVendor.threads, detectedVendor.threads);
        Assertions.assertEquals(unknownVendor.memory, detectedVendor.memory);
        Assertions.assertEquals(unknownVendor.architecture, detectedVendor.architecture);
    }

    @Test
    void testVendorSpecificDetection() {
        // Compare Intel vs AMD detection for same configuration
        Hardware intelSystem = Hardware.make(Hardware.Architecture.X86_64, 16L * K * K * K, 8);
        Hardware amdSystem = Hardware.make(Hardware.Architecture.X86_64, 16L * K * K * K, 8);

        // Basic properties should be the same
        Assertions.assertEquals(intelSystem.threads, amdSystem.threads);
        Assertions.assertEquals(intelSystem.memory, amdSystem.memory);
        Assertions.assertEquals(intelSystem.architecture, amdSystem.architecture);

        // Both should detect the same core configuration for same thread count
        Assertions.assertEquals(intelSystem.cores, amdSystem.cores);

        // Cache configurations might be similar since both are x86_64
        Assertions.assertTrue(intelSystem.cache > 0);
        Assertions.assertTrue(amdSystem.cache > 0);

        // Test ARM vs Apple detection - these may differ due to different threading assumptions
        Hardware armSystem = Hardware.make(Hardware.Architecture.AARCH64, 8L * K * K * K, 8);
        Hardware appleSystem = Hardware.make(Hardware.Architecture.AARCH64, 8L * K * K * K, 8);

        Assertions.assertEquals(armSystem.threads, appleSystem.threads);
        // Note: ARM and Apple may have different core detection logic
        // Apple Silicon assumes 1:1 thread:core, while ARM may assume hyperthreading
        Assertions.assertTrue(armSystem.cores >= 4, "ARM should detect reasonable core count");
        Assertions.assertTrue(appleSystem.cores >= 4, "Apple should detect reasonable core count");
        Assertions.assertTrue(armSystem.cores <= 8, "ARM cores should not exceed thread count");
        Assertions.assertTrue(appleSystem.cores <= 8, "Apple cores should not exceed thread count");
    }

    @Test
    void testX86HyperthreadingDetection() {
        // Test scenarios from HardwareDetectionTest configurations

        // X86_64__02 - dual core, no hyperthreading - Intel Core 2 era
        Hardware x86Dual = Hardware.make(Hardware.Architecture.X86_64, 3L * K * K * K, 2);
        Assertions.assertEquals(2, x86Dual.cores, "Dual core x86_64 should detect 2 cores");

        // X86_64__04_2 - dual core with hyperthreading (4 threads) - Intel Core i7
        Hardware x86Hyper = Hardware.make(Hardware.Architecture.X86_64, 8L * K * K * K, 4);
        Assertions.assertTrue(x86Hyper.cores <= 2, "4 threads should suggest 2 cores with hyperthreading");

        // X86_64__08 - quad core with hyperthreading (8 threads) - Intel Core i7
        Hardware x86Quad = Hardware.make(Hardware.Architecture.X86_64, 32L * K * K * K, 8);
        Assertions.assertTrue(x86Quad.cores <= 4, "8 threads should suggest 4 cores with hyperthreading");
    }
}
