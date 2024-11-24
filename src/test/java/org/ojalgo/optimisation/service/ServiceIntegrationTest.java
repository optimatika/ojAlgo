package org.ojalgo.optimisation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;

@Tag("network")
public class ServiceIntegrationTest {

    private static final String HOST = "http://16.16.99.66:8080";
    // private static final String HOST = "http://localhost:8080";
    // private static final String HOST = "http://test-service.optimatika.se:8080";

    static final boolean DEBUG = false;

    @AfterEach
    public void clearIntegrations() {
        ExpressionsBasedModel.clearIntegrations();
    }

    @Test
    public void testEnvironment() {

        String environment = ServiceIntegration.newInstance(HOST).getEnvironment();

        if (DEBUG) {
            BasicLogger.debug(environment);
        }

        TestUtils.assertTrue(environment.contains("HW"));
        TestUtils.assertTrue(environment.contains("thread"));
    }

    @Test
    public void testTest() {
        TestUtils.assertTrue(ServiceIntegration.newInstance(HOST).test().booleanValue());
    }

    @Test
    public void testVeryBasicModel() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        model.newVariable("A").weight(10).lower(0);
        model.newVariable("B").weight(-10).lower(0);

        model.newExpression("UM2").set(0, 1).set(1, 1).level(2);

        Result expMax = model.maximise();
        Result expMin = model.minimise();

        ExpressionsBasedModel.addIntegration(ServiceIntegration.newInstance(HOST));

        Result actMax = model.maximise();
        Result actMin = model.minimise();

        TestUtils.assertStateAndSolution(expMax, actMax);
        TestUtils.assertStateAndSolution(expMin, actMin);

        ExpressionsBasedModel.clearIntegrations();
    }

}
