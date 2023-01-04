package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class TestHardware extends MachineTests {

    private static void doTest(final Hardware hardware, final int threads, final int cores, final int units) {
        TestUtils.assertEquals("threads", threads, hardware.threads);
        TestUtils.assertEquals("cores", cores, hardware.cores);
        TestUtils.assertEquals("units", units, hardware.units);
    }

    @Test
    public void testCompare() {
        TestUtils.assertTrue(Hardware.X86_64__04_2.compareTo(Hardware.X86_64__04_1_L2) < 0);
        TestUtils.assertTrue(Hardware.X86_64__04_1_L3.compareTo(Hardware.X86_64__04_1_L2) > 0);
    }

    @Test
    public void testPPC__01() {

        Hardware hardware = Hardware.PPC__01;

        int threads = 1;
        int cores = 1;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86__01() {

        Hardware hardware = Hardware.X86__01;

        int threads = 1;
        int cores = 1;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86__02() {

        Hardware hardware = Hardware.X86__02;

        int threads = 2;
        int cores = 2;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__02() {

        Hardware hardware = Hardware.X86_64__02;

        int threads = 2;
        int cores = 2;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__04_1_L2() {

        Hardware hardware = Hardware.X86_64__04_1_L2;

        int threads = 4;
        int cores = 4;
        int units = 2;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__04_1_L3() {

        Hardware hardware = Hardware.X86_64__04_1_L3;

        int threads = 4;
        int cores = 4;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__04_2() {

        Hardware hardware = Hardware.X86_64__04_2;

        int threads = 4;
        int cores = 2;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__08() {

        Hardware hardware = Hardware.X86_64__08;

        int threads = 8;
        int cores = 4;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__12() {

        Hardware hardware = Hardware.X86_64__12;

        int threads = 12;
        int cores = 6;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }

    @Test
    public void testX86_64__16() {

        Hardware hardware = Hardware.X86_64__16;

        int threads = 16;
        int cores = 8;
        int units = 1;

        TestHardware.doTest(hardware, threads, cores, units);
    }
}
