package org.ojalgo.concurrent;

import static org.ojalgo.TestUtils.assertEquals;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class ExternalProcessExecutorTest {

    public static String readLibPath() {
        return System.getProperty("java.library.path");
    }

    @Test
    public void inheritsJavaLibraryPathByDefault() throws Exception {
        String parent = System.getProperty("java.library.path");
        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance(1);
        Future<String> future = exec.execute(ExternalProcessExecutorTest.class, "readLibPath", new Class<?>[] {});
        String child = future.get(10, TimeUnit.SECONDS);
        assertEquals(parent, child);
    }

    @Test
    public void explicitOverrideBeatsDefaultInheritance() throws Exception {
        String override = "/tmp/ojalgo-libpath-override";
        ExternalProcessExecutor exec = ExternalProcessExecutor.newInstance(1);
        ProcessOptions opts = new ProcessOptions.Builder().systemProperty("java.library.path", override).build();
        Future<String> future = exec.execute(ExternalProcessExecutorTest.class, "readLibPath", new Class<?>[] {}, opts);
        String child = future.get(10, TimeUnit.SECONDS);
        assertEquals(override, child);
    }
}
