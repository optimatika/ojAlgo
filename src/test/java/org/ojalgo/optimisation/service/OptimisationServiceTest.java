package org.ojalgo.optimisation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;

@Disabled
public class OptimisationServiceTest {

    private static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";

    static final boolean DEBUG = false;

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

        ExpressionsBasedModel.addIntegration(OptimisationService.newIntegration(HTTP_LOCALHOST_8080));

        Result actMax = model.maximise();
        Result actMin = model.minimise();

        TestUtils.assertStateAndSolution(expMax, actMax);
        TestUtils.assertStateAndSolution(expMin, actMin);

        ExpressionsBasedModel.clearIntegrations();
    }

}
