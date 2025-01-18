/*
 * Copyright 1997-2025 Optimatika
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
import org.ojalgo.netio.ServiceClient;
import org.ojalgo.netio.ServiceClient.Response;
import org.ojalgo.optimisation.ExpressionsBasedModel;

public final class ServiceIntegration extends ExpressionsBasedModel.Integration<ServiceSolver> {

    private static final String PATH_ENVIRONMENT = "/optimisation/v01/environment";
    private static final String PATH_TEST = "/optimisation/v01/test";

    public static ServiceIntegration newInstance(final String host) {
        return new ServiceIntegration(host);
    }

    private Boolean myCapable = null;
    private final String myHost;

    ServiceIntegration(final String host) {
        super();
        myHost = host;
    }

    @Override
    public ServiceSolver build(final ExpressionsBasedModel model) {
        return new ServiceSolver(model, myHost);
    }

    public String getEnvironment() {
        return ServiceClient.get(myHost + PATH_ENVIRONMENT).getBody();
    }

    @Override
    public boolean isCapable(final ExpressionsBasedModel model) {

        if (myCapable == null) {
            myCapable = this.test();
        }

        return Boolean.TRUE.equals(myCapable);
    }

    public Boolean test() {
        Response<String> response = ServiceClient.get(myHost + PATH_TEST);
        if (response.isResponseOK() && response.getBody().contains("VALID")) {
            return Boolean.TRUE;
        } else {
            BasicLogger.error("Calling {} failed!", myHost + PATH_TEST);
            return Boolean.FALSE;
        }
    }

}
