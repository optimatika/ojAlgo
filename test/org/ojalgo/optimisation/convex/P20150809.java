/*
 * Copyright 1997-2018 Optimatika
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
package org.ojalgo.optimisation.convex;

import org.ojalgo.matrix.store.RawStore;
import org.ojalgo.optimisation.Optimisation;

class P20150809 {

    public static void main(final String[] args) {
        System.out.println();
        P20150809.attempt5(true, false);
        System.out.println();
        P20150809.attempt5(true, true);
        System.out.println();
        P20150809.attempt5(false, true);
        System.out.println();
        P20150809.attempt5(false, false);
    }

    static void attempt5(final boolean identity, final boolean addDummyConstraints) {

        final ConvexSolver cs = P20150809.buildModel(identity, addDummyConstraints);

        try {
            final Optimisation.Result solution = cs.solve();
            if ((solution.getState() == Optimisation.State.DISTINCT) || (solution.getState() == Optimisation.State.APPROXIMATE)
                    || (solution.getState() == Optimisation.State.OPTIMAL)) {
                final double[] pt = new double[4];
                for (int i = 0; i < pt.length; i++) {
                    pt[i] = solution.doubleValue(i);
                }

                System.out.println("Objective " + solution.getValue());
                for (int ii = 0; ii < 4; ii++) {
                    System.out.println("x[" + ii + "] = " + solution.doubleValue(ii));
                }
            } else {
                System.out.println("Failure State = " + solution.getState().name());

            }

        } catch (final Exception e) {
            System.out.println(e);
        }
    }

    static ConvexSolver buildModel(final boolean identity, final boolean addDummyConstraints) {

        if (OptimisationConvexTests.DEBUG) {
            if (!identity && !addDummyConstraints) {
                System.out.println("Zero Q matrix and no constraints -------------------------!");
            } else if (!identity) {
                System.out.println("Zero Q matrix and constraints -------------------------!");
            } else if (!addDummyConstraints) {
                System.out.println("Identity Q matrix and no constraints -------------------------!");
            } else {
                System.out.println("Identity Q matrix and  constraints -------------------------!");
            }
        }

        final double[] C = new double[] { 0.12, -0.05, 0.08, 0.07 };
        final RawStore cov = new RawStore(4, 4);
        if (identity) {
            for (int i = 0; i < 4; i++) {
                cov.set(i, i, 1.0);
            }
        }
        final RawStore linPart = new RawStore(C, 4);
        ConvexSolver.Builder builder = ConvexSolver.getBuilder(cov, linPart);

        if (addDummyConstraints) {
            final RawStore ineq = RawStore.FACTORY.rows(new double[][] { { -1.0, 0.0, 0.0, 0.0 }, { 0.0, -1.0, 0.0, 0.0 }, { 0.0, 0.0, -1.0, 0.0 },
                    { 0.0, 0.0, 0.0, -1.0 }, { 1.0, 0.0, 0.0, 0.0 }, { 0.0, 1.0, 0.0, 0.0 }, { 0.0, 0.0, 1.0, 0.0 }, { 0.0, 0.0, 0.0, 1.0 } });

            final RawStore coeff = RawStore.FACTORY.columns(new double[][] { { 99999, 99999, 99999, 99999, 99999, 99999, 99999, 99999 } });

            builder = builder.inequalities(ineq, coeff);
        }
        final Optimisation.Options opts = new Optimisation.Options();
        opts.iterations_abort = 10000;
        opts.iterations_suffice = 100;
        if (OptimisationConvexTests.DEBUG) {
            opts.debug(ConvexSolver.class);
        }

        return builder.build(opts);
    }

    P20150809() {
        super();
    }

}
