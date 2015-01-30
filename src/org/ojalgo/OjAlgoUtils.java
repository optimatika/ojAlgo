/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
     * @see Package#getSpecificationVersion()
     */
    public static String getDate() {

        final String tmpManifestValue = OjAlgoUtils.class.getPackage().getSpecificationVersion();

        return tmpManifestValue != null ? tmpManifestValue : StandardType.SQL_DATE.format(new Date());
    }

    /**
     * @see Package#getImplementationTitle()
     */
    public static String getTitle() {

        final String tmpManifestValue = OjAlgoUtils.class.getPackage().getImplementationTitle();

        return tmpManifestValue != null ? tmpManifestValue : "ojAlgo";
    }

    /**
     * @see Package#getImplementationVendor()
     */
    public static String getVendor() {

        final String tmpManifestValue = OjAlgoUtils.class.getPackage().getImplementationVendor();

        return tmpManifestValue != null ? tmpManifestValue : "Optimatika";
    }

    /**
     * @see Package#getImplementationVersion()
     */
    public static String getVersion() {

        final String tmpManifestValue = OjAlgoUtils.class.getPackage().getImplementationVersion();

        return tmpManifestValue != null ? tmpManifestValue : "X.X";
    }

    /**
     * This is set for you, but you may want to set it to something different/better. Create a {@linkplain Hardware}
     * instance and then call {@linkplain Hardware#virtualise()}.
     */
    public static VirtualMachine ENVIRONMENT = null;

    static {

        final String tmpArchitecture = VirtualMachine.getArchitecture();
        final long tmpMemory = VirtualMachine.getMemory();
        final int tmpThreads = VirtualMachine.getThreads();

        for (final Hardware tmpHardware : Hardware.PREDEFINED) {
            if (tmpHardware.architecture.equals(tmpArchitecture) && (tmpHardware.threads == tmpThreads) && (tmpHardware.memory >= tmpMemory)) {
                ENVIRONMENT = tmpHardware.virtualise();
            }
        }

        if (ENVIRONMENT == null) {
            BasicLogger.debug("ojAlgo includes a small set of predefined hardware profiles,");
            BasicLogger.debug("none of which were deemed suitable for the hardware you're currently using.");
            BasicLogger.debug("You should set org.ojalgo.OjAlgoUtils.ENVIRONMENT to something that matches the hardware/OS/JVM you're running on.");
            BasicLogger.debug("Additionally it would be appreciated if you contribute your hardware profile to ojAlgo.");
            BasicLogger.debug("https://lists.sourceforge.net/lists/listinfo/ojalgo-user");
            ENVIRONMENT = Hardware.makeSimple(tmpArchitecture, tmpMemory, tmpThreads).virtualise();
        }

    }

    private OjAlgoUtils() {
        super();
    }

}
