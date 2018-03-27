package org.ojalgo.machine;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class TestHardware {

    @Test
    public void testCompare() {
        TestUtils.assertTrue(Hardware.X86_64__04_2.compareTo(Hardware.X86_64__04_1_L2) < 0);
        TestUtils.assertTrue(Hardware.X86_64__04_1_L3.compareTo(Hardware.X86_64__04_1_L2) > 0);
    }

    @Test
    public void testPPC__01() {

        final Hardware tmpHardware = Hardware.PPC__01;

        final int tmpThreads = 1;
        final int tmpCores = 1;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86__01() {

        final Hardware tmpHardware = Hardware.X86__01;

        final int tmpThreads = 1;
        final int tmpCores = 1;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86__02() {

        final Hardware tmpHardware = Hardware.X86__02;

        final int tmpThreads = 2;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__02() {

        final Hardware tmpHardware = Hardware.X86_64__02;

        final int tmpThreads = 2;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__04_1_L2() {

        final Hardware tmpHardware = Hardware.X86_64__04_1_L2;

        final int tmpThreads = 4;
        final int tmpCores = 4;
        final int tmpUnits = 2;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__04_1_L3() {

        final Hardware tmpHardware = Hardware.X86_64__04_1_L3;

        final int tmpThreads = 4;
        final int tmpCores = 4;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__04_2() {

        final Hardware tmpHardware = Hardware.X86_64__04_2;

        final int tmpThreads = 4;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__08() {

        final Hardware tmpHardware = Hardware.X86_64__08;

        final int tmpThreads = 8;
        final int tmpCores = 4;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__12() {

        final Hardware tmpHardware = Hardware.X86_64__12;

        final int tmpThreads = 12;
        final int tmpCores = 6;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    @Test
    public void testX86_64__16() {

        final Hardware tmpHardware = Hardware.X86_64__16;

        final int tmpThreads = 16;
        final int tmpCores = 8;
        final int tmpUnits = 2;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    private void doTest(final Hardware hardware, final int threads, final int cores, final int units) {
        TestUtils.assertEquals("threads", threads, hardware.threads);
        TestUtils.assertEquals("cores", cores, hardware.cores);
        TestUtils.assertEquals("units", units, hardware.units);
    }
}
