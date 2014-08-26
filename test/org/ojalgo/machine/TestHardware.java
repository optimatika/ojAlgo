package org.ojalgo.machine;

import org.ojalgo.TestUtils;

public class TestHardware extends MachineTests {

    public TestHardware() {
        super();
    }

    public TestHardware(final String someName) {
        super(someName);
    }

    public void testPPC__01() {

        final Hardware tmpHardware = Hardware.PPC__01;

        final int tmpThreads = 1;
        final int tmpCores = 1;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86__01() {

        final Hardware tmpHardware = Hardware.X86__01;

        final int tmpThreads = 1;
        final int tmpCores = 1;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86__02() {

        final Hardware tmpHardware = Hardware.X86__02;

        final int tmpThreads = 2;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86_64__02() {

        final Hardware tmpHardware = Hardware.X86_64__02;

        final int tmpThreads = 2;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86_64__04() {

        final Hardware tmpHardware = Hardware.X86_64__04;

        final int tmpThreads = 4;
        final int tmpCores = 2;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86_64__08() {

        final Hardware tmpHardware = Hardware.X86_64__08;

        final int tmpThreads = 8;
        final int tmpCores = 4;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

    public void testX86_64__12() {

        final Hardware tmpHardware = Hardware.X86_64__12;

        final int tmpThreads = 12;
        final int tmpCores = 6;
        final int tmpUnits = 1;

        this.doTest(tmpHardware, tmpThreads, tmpCores, tmpUnits);
    }

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
