/*
 * Copyright 1997-2022 Optimatika
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
package org.ojalgo.data.domain.finance.series;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.ResourceLocator;
import org.ojalgo.netio.ResourceLocator.Request;
import org.ojalgo.netio.ResourceLocator.Response;
import org.ojalgo.netio.ResourceLocator.Session;
import org.ojalgo.type.CalendarDateUnit;

/**
 * https://blog.alwold.com/2011/06/30/how-to-trust-a-certificate-in-java-on-mac-os-x/
 *
 * @author apete
 */
@Tag("unstable")
@Disabled
public class YahooFetcherTest {

    @Test
    public void testSequence() {

        Session session = ResourceLocator.session();
        String symbol = "AAPL";
        CalendarDateUnit resolution = CalendarDateUnit.DAY;

        // A request that requires consent, but not the crumb
        Request challengeRequest = YahooSession.buildChallengeRequest(session, symbol);
        Response challengeResponse = challengeRequest.response();

        String challengeResponseBody = challengeResponse.toString();
        TestUtils.assertNotNullOrEmpty(challengeResponseBody);
        // Must get this after the http body has been read
        if (challengeRequest.equals(challengeResponse.getRequest())) {
            TestUtils.fail("Not redirect - supposed to redirect to the consent page - something changed!");
        }

        YahooSession.scrapeChallengeResponse(session, challengeResponse);

        TestUtils.assertNotNullOrEmpty(session.getParameterValue(YahooSession.SESSION_ID));
        TestUtils.assertNotNullOrEmpty(session.getParameterValue(YahooSession.CSRF_TOKEN));
        TestUtils.assertNotNullOrEmpty(session.getParameterValue(YahooSession.BRAND_BID));

        Request consentRequest = YahooSession.buildConsentRequest(session, challengeRequest);
        Response consentResponse = consentRequest.response();

        TestUtils.assertInRange(200, 300, consentResponse.getStatusCode());

        Request crumbRequest = YahooSession.buildCrumbRequest(session);
        Response crumbResponse = crumbRequest.response();

        YahooSession.scrapeCrumbResponse(session, crumbResponse);

        TestUtils.assertNotNullOrEmpty(session.getParameterValue(YahooSession.CRUMB));
        TestUtils.assertEquals(crumbResponse.toString(), session.getParameterValue(YahooSession.CRUMB));

        Request dataRequest = YahooSession.buildDataRequest(session, symbol, resolution);
        Response dataResponse = dataRequest.response();

        YahooParser parser = new YahooParser();
        List<YahooParser.Data> data = new ArrayList<>();
        parser.parse(dataResponse.getStreamReader(), dp -> data.add(dp));

        TestUtils.assertTrue(data.size() >= 7557);
    }

}
