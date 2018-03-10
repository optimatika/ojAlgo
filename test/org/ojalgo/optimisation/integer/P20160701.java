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
package org.ojalgo.optimisation.integer;

import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * <p>
 * I am trying to call Ojalgo 40 from AnyLogic 7.3.2 (http://www.anylogic..com/downloads) on Ubuntu 16.04 in
 * order to solve a Traveling Salesman Problem, but Ojalgo sometimes stops on a feasible solution before the
 * optimum. The following code works well without Anylogic and always finds 917.31 as optimal solution:
 * (Simply copy/paste the following code in a file called "Tsp.java" in order to test it.)
 * </p>
 * <p>
 * Next, I try to run the same code in the "On startup" section of the "Agent actions" of the "Main" agent in
 * an AnyLogic model. (Click on the project name "Ojalgo" to change the location of ojalgo-40.0.0.jar, like in
 * the attached file.) Unfortunately, the obtained result is not always 917.31, but also sometimes 1099.22 and
 * 1161.84. I do not understand why the solution randomly changes. I also call Cplex from AnyLogic to solve
 * this same problem, which always returns the optimal solution, hence the problem seems not to be due to
 * AnyLogic. As shown by the above Java code, the problem is not due to Ojalgo as well, but only related to
 * the coupling of AnyLogic and Ojalgo. Thank you very much for Ojalgo and your help!
 * </p>
 * <p>
 * apete: ExpressionsBasedModel has a feature that automatically rescales model parameters (to maximize
 * numerical accuracy) before invoking the solver. The current implementation of that feature (apparently)
 * doesn?t work very well with extremely large parameters in the model. I have now modified the behavior of
 * that feature to not scale anything when/if there are extremely large or small parameters present. As far as
 * I can see that solves the problem with your model.
 * </p>
 * <p>
 * apete (later): Have also improved the presolve functionality to fix (not-include) uncorrelated and/or
 * unbounded variables. (Doesn't handle every case, but this one a a few more.) This was the real fix for this
 * problem!
 * </p>
 *
 * @author apete
 */
class P20160701 {

    public static void main(final String[] arg) {
        final int n = 6;
        final double[][] c = new double[n][n];
        c[0][0] = 1.7976931348623157E308;
        c[0][1] = 141.4213562373095;
        c[0][2] = 223.60679774997897;
        c[0][3] = 223.60679774997897;
        c[0][4] = 141.4213562373095;
        c[0][5] = 156.63604262201076;
        c[1][0] = 141.4213562373095;
        c[1][1] = 1.7976931348623157E308;
        c[1][2] = 100.0;
        c[1][3] = 223.60679774997897;
        c[1][4] = 200.0;
        c[1][5] = 219.25609608009617;
        c[2][0] = 223.60679774997897;
        c[2][1] = 100.0;
        c[2][2] = 1.7976931348623157E308;
        c[2][3] = 200.0;
        c[2][4] = 223.60679774997897;
        c[2][5] = 319.2543607976003;
        c[3][0] = 223.60679774997897;
        c[3][1] = 223.60679774997897;
        c[3][2] = 200.0;
        c[3][3] = 1.7976931348623157E308;
        c[3][4] = 100.0;
        c[3][5] = 377.5537017276938;
        c[4][0] = 141.4213562373095;
        c[4][1] = 200.0;
        c[4][2] = 223.60679774997897;
        c[4][3] = 100.0;
        c[4][4] = 1.7976931348623157E308;
        c[4][5] = 297.81988930943544;
        c[5][0] = 156.63604262201076;
        c[5][1] = 219.25609608009617;
        c[5][2] = 319.2543607976003;
        c[5][3] = 377.5537017276938;
        c[5][4] = 297.81988930943544;
        c[5][5] = 1.7976931348623157E308;

        final ExpressionsBasedModel model = new ExpressionsBasedModel();

        //DECISION VARIABLES
        final Variable[][] x = new Variable[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = Variable.make("x" + i + "_" + j).binary().weight(c[i][j]);
                model.addVariable(x[i][j]);
            }
        }
        final Variable[] u = new Variable[n];
        for (int i = 1; i < n; i++) {
            u[i] = new Variable("u" + i);
            model.addVariable(u[i]);
        }

        //CONSTRAINTS
        //forall(i in cities)
        //flow_out:
        //sum(j in cities : i!=j) x[i][j]==1;
        for (int i = 0; i < n; i++) {
            final Expression constraint_line = model.addExpression("constraint_line" + i).lower(1).upper(1);
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    constraint_line.set(x[i][j], 1);
                }
            }
        }

        //forall(j in cities)
        //flow_in:
        //sum(i in cities : i!=j) x[i][j]==1;
        for (int j = 0; j < n; j++) {
            final Expression constraint_column = model.addExpression("constraint_column" + j).lower(1).upper(1);
            for (int i = 0; i < n; i++) {
                if (i != j) {
                    constraint_column.set(x[i][j], 1);
                }
            }
        }

        //forall(i in cities: i>=1, j in cities: j>=1)
        //subroute:
        //u[i]-u[j]+n*x[i][j] <= n-1;
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < n; j++) {
                if (i != j) {
                    final Expression constraint_subroute = model.addExpression("constraint_subroute" + i + "_" + j).upper(n - 1);
                    constraint_subroute.set(u[i], 1);
                    constraint_subroute.set(u[j], -1);
                    constraint_subroute.set(x[i][j], n);
                }
            }
        }

        final Optimisation.Result result = model.minimise();

        if (OptimisationIntegerTests.DEBUG) {
            System.out.print("u=\n\t  ");
            for (int i = 1; i < n; i++) {
                System.out.print(u[i].getValue().intValue() + " ");
            }
            System.out.print("\nx=\n\t");
            for (int i = 0; i < n; i++) {
                System.out.print(i + " ");
            }
            System.out.println();
            for (int i = 0; i < n; i++) {
                System.out.print(i + "\t");
                for (int j = 0; j < n; j++) {
                    System.out.print(x[i][j].getValue().intValue() + " ");
                }
                System.out.println();
            }
            System.out.println("\nResult = " + result);
        }

        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(917.3134949394164, result.getValue());
    }
}
