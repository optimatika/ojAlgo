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

import org.ojalgo.netio.InMemoryFile;
import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;

final class ServiceSolver implements Optimisation.Solver {

    private static final String PATH_MAXIMISE = "/optimisation/v01/maximise";
    private static final String PATH_MINIMISE = "/optimisation/v01/minimise";

    private final String myHost;
    private final ExpressionsBasedModel myModel;
    private final Optimisation.Sense myOptimisationSense;

    ServiceSolver(final ExpressionsBasedModel model, final String host) {
        super();
        myModel = model;
        myOptimisationSense = model.getOptimisationSense();
        myHost = host;
    }

    @Override
    public Result solve(final Result kickStarter) {

        InMemoryFile file = new InMemoryFile();

        myModel.simplify().writeTo(file);

        //  String modelAsString = file.getContentsAsString();

        Response<String> response = myOptimisationSense == Optimisation.Sense.MAX ? ServiceClient.post(myHost + PATH_MAXIMISE, file.getContentsAsByteArray())
                : ServiceClient.post(myHost + PATH_MINIMISE, file.getContentsAsByteArray());

        String responseBody = response.getBody();
        return Optimisation.Result.parse(responseBody);
    }

}
