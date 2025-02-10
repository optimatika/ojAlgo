package org.ojalgo.netio;

import java.io.InputStream;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.ServiceClient.Request;
import org.ojalgo.netio.ServiceClient.Response;

public class ServiceClientTest extends NetioTests {

    @Test
    public void testDownload() {

        ServiceClient.Session session = ServiceClient.newSession();

        Request request1 = session.newRequest("https://be.chregister.ch/cr-portal/auszug/auszug.xhtml?uid=CHE-336.475.508");

        Response<InputStream> response1 = request1.send(BodyHandlers.ofInputStream());

        Request request9 = session.newRequest("https://be.chregister.ch/cr-portal/auszug/auszug.xhtml");

        Response<String> response9 = request9.send(BodyHandlers.ofString());

        String body = response9.getBody();

        TestUtils.assertTrue(body.startsWith("%PDF-1.4"));
    }

}
