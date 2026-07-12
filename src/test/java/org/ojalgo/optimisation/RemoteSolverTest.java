package org.ojalgo.optimisation;

import java.net.URI;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import se.optimatika.optimisation.service.client.OptClientV01;

class RemoteSolverTest extends OptimisationTests {

    private static final String HOST = System.getenv("SERVICE_HOST") != null ? System.getenv("SERVICE_HOST")
            : "https://optimatika-boot-services-969062758986.europe-north1.run.app";

    private static ExpressionsBasedModel buildDietModel(final Optimisation.Environment environment) {

        ExpressionsBasedModel model = environment != null ? environment.newModel() : new ExpressionsBasedModel();

        Variable oatmeal = model.newVariable("oatmeal").lower(0);
        Variable chicken = model.newVariable("chicken").lower(0);

        model.newExpression("protein").set(oatmeal, 1).set(chicken, 2).lower(12);
        model.newExpression("calories").set(oatmeal, 10).set(chicken, 5).lower(60);

        model.newExpression("cost").set(oatmeal, 2).set(chicken, 3).weight(1);

        return model;
    }

    @Test
    void testMinimiseDirect() {

        ExpressionsBasedModel model = RemoteSolverTest.buildDietModel(null);

        Optimisation.Result result = model.minimise();

        Assertions.assertTrue(result.getState().isOptimal());
        Assertions.assertEquals(20.0, result.getValue(), 1E-6);
    }

    @Test
    void testSubmitDefaultLocal() throws Exception {

        ExpressionsBasedModel model = RemoteSolverTest.buildDietModel(null);

        Future<Optimisation.Result> future = model.submit(Optimisation.Sense.MIN);
        Optimisation.Result result = future.get();

        Assertions.assertTrue(result.getState().isOptimal());
        Assertions.assertEquals(20.0, result.getValue(), 1E-6);
    }

    @Tag("network")
    @Test
    void testSubmitRemote() throws Exception {

        OptClientV01 client = new OptClientV01(URI.create(HOST));

        Optimisation.Environment environment = Optimisation.newEnvironment();
        environment.setRemoteSolver(client::putOnQueue, client::pollResult);

        ExpressionsBasedModel model = RemoteSolverTest.buildDietModel(environment);

        Future<Optimisation.Result> future = model.submit(Optimisation.Sense.MIN);
        Optimisation.Result result = future.get();

        Assertions.assertTrue(result.getState().isOptimal());
        Assertions.assertEquals(20.0, result.getValue(), 1E-6);
    }

}
