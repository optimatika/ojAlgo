package org.ojalgo.type;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;

public class ForgetfulMapTest {

    @Test
    public void testExpiration() throws InterruptedException {

        ForgetfulMap<String, String> cache = ForgetfulMap.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS).build();

        cache.put("key1", "value1");

        TestUtils.assertEquals("value1", cache.get("key1"));

        Thread.sleep(4000);

        TestUtils.assertNull(cache.get("key1"));
    }

}
