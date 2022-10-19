package org.ojalgo.optimisation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;

public class OptimisationServiceTest {

    private static final String PATH_ENVIRONMENT = "/optimisation/v01/environment";
    private static final String PATH_TEST = "/optimisation/v01/test";
    private static final String HOST = "http://test-service.optimatika.se";
    // private static final String HOST = "http://localhost:8080";

    static final boolean DEBUG = true;

    @AfterEach
    public void clearIntegrations() {
        ExpressionsBasedModel.clearIntegrations();
    }

    @Test
    public void testVeryBasicModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        model.addVariable("A").weight(10).lower(0);
        model.addVariable("B").weight(-10).lower(0);

        model.addExpression("UM2").set(0, 1).set(1, 1).level(2);

        Result expMax = model.maximise();
        Result expMin = model.minimise();

        ExpressionsBasedModel.addIntegration(OptimisationService.newIntegration(HOST));

        Result actMax = model.maximise();
        Result actMin = model.minimise();

        TestUtils.assertStateAndSolution(expMax, actMax);
        TestUtils.assertStateAndSolution(expMin, actMin);

        ExpressionsBasedModel.clearIntegrations();
    }

    @Test
    public void testTest() {

        Response<String> response = ServiceClient.get(HOST + PATH_TEST);

        TestUtils.assertTrue(response.isResponseOK());

        if (DEBUG) {
            BasicLogger.debug(response.getBody());
        }
    }

    @Test
    public void testEnvironment() {

        Response<String> response = ServiceClient.get(HOST + PATH_ENVIRONMENT);

        TestUtils.assertTrue(response.isResponseOK());

        if (DEBUG) {
            BasicLogger.debug(response.getBody());
        }
    }

}
