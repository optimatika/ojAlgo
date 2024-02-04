/*
 * Copyright 1997-2024 Optimatika
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
package org.ojalgo.optimisation.service;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.InMemoryFile;
import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

/**
 * {@link Solver} and {@link Integration} implementations that make use of Optimatika's
 * Optimisation-as-a-Service (OaaS).
 * <p>
 * There is a test/demo version of that service available at: http://test-service.optimatika.se
 * <p>
 * That particular instance is NOT for production use, and may be restricted or removed without warning.
 * <p>
 * If you'd like access to a service instance for (private) production use, you should contact Optimatika
 * using: https://www.optimatika.se/products-services-inquiry/
 *
 * @author apete
 * @see https://www.optimatika.se/products-services-inquiry/
 */
public abstract class OptimisationService {

    public static final class Integration extends ExpressionsBasedModel.Integration<OptimisationService.Solver> {

        private static final String PATH_TEST = "/optimisation/v01/test";

        private Boolean myCapable = null;
        private final String myHost;

        Integration(final String host) {
            super();
            myHost = host;
        }

        public OptimisationService.Solver build(final ExpressionsBasedModel model) {
            return new OptimisationService.Solver(model, myHost);
        }

        public boolean isCapable(final ExpressionsBasedModel model) {

            if (myCapable == null) {
                Response<String> response = ServiceClient.get(myHost + PATH_TEST);
                if (response.isResponseOK() && response.getBody().contains("VALID")) {
                    myCapable = Boolean.TRUE;
                } else {
                    BasicLogger.error("Calling {} failed!", myHost + PATH_TEST);
                    myCapable = Boolean.FALSE;
                }
            }

            return myCapable.booleanValue();
        }

    }

    public static final class Solver implements Optimisation.Solver {

        private static final String PATH_MAXIMISE = "/optimisation/v01/maximise";
        private static final String PATH_MINIMISE = "/optimisation/v01/minimise";

        private final String myHost;
        private final ExpressionsBasedModel myModel;
        private final Optimisation.Sense myOptimisationSense;

        Solver(final ExpressionsBasedModel model, final String host) {
            super();
            myModel = model;
            myOptimisationSense = model.getOptimisationSense();
            myHost = host;
        }

        public Result solve(final Result kickStarter) {

            InMemoryFile file = new InMemoryFile();

            myModel.simplify().writeTo(file);

            //  String modelAsString = file.getContentsAsString();

            Response<String> response = myOptimisationSense == Optimisation.Sense.MAX
                    ? ServiceClient.post(myHost + PATH_MAXIMISE, file.getContentsAsByteArray())
                    : ServiceClient.post(myHost + PATH_MINIMISE, file.getContentsAsByteArray());

            String responseBody = response.getBody();
            return Optimisation.Result.parse(responseBody);
        }

    }

    public static OptimisationService.Integration newIntegration(final String host) {
        return new Integration(host);
    }

}
