package org.ojalgo.type.management;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.ojalgo.netio.BasicLogger;

public abstract class MBeanUtils {

    private static final AtomicInteger ID = new AtomicInteger();

    public static void register(final Object mbean) {
        MBeanUtils.register(mbean, mbean.getClass().getSimpleName());
    }

    public static void register(final Object mbean, final String type) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName on = new ObjectName("ojAlgo:type=" + type + "-" + ID.incrementAndGet());
            mbs.registerMBean(mbean, on);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException cause) {
            BasicLogger.error("Error creating MBean", cause);
        }
    }

}
