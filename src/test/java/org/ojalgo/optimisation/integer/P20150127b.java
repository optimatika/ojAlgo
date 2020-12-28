package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * A smaller version of P20150127a the number constraints have been greatly reduced and a problematic
 * branch-and-bound node identified.
 *
 * @author apete
 */
public class P20150127b {

    public static ExpressionsBasedModel getModel(final boolean branch, final boolean old) {

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        final Variable x = Variable.make("x").integer(true);
        final Variable y = Variable.make("y").integer(true);
        retVal.addVariable(x);
        retVal.addVariable(y);

        final List<int[]> coefficients = P20150127b.getCoefficients();
        int counter = 0;
        for (final int[] coeff : coefficients) {
            final Expression c = retVal.addExpression("inequality_" + ++counter);
            // We want coeff[0] * x + coeff[1] * y < 0. Since our
            // solutions are integer, we can do "<= -1".
            c.upper(BigDecimal.ONE.negate());
            c.set(x, coeff[0]);
            c.set(y, coeff[1]);
        }

        // DEBUG: "Speed things up a bit": This are the parameters that
        // the Branch&Bounde node which produces a wrong result uses.
        if (branch) {
            x.lower(1).upper(20);
            y.upper(-1);
            if (old) {
                // The integer solver used to set this lower limit in place of "no limit"
                y.lower(Integer.MIN_VALUE);
                retVal.relax(true);
            }
        }

        return retVal;
    }

    static public void main(final String[] args) throws Exception {
        final ExpressionsBasedModel model = P20150127b.getModel(true, true);

        // model.options.debug(LinearSolver.class);

        // Solve the model
        final Optimisation.Result result = model.minimise();

        // The rest of this code no longer applies, the above model is
        // unsolvable with the new constraints 1<=x<=20 and y<=-1.
        // However, ojAlgo still notices that "something is wrong"
        // (model.options.validate == true).
        if (result.getState().isSuccess()) {
            throw new Exception("Model should NOT be solvable due to constraints on x/y");
            /*
             * if (!result.getState().isSuccess()) throw new Exception(
             * "Model should be solvable (e.g. x=20, y=-1)?!"); // Get the solution BigDecimal valX =
             * result.get(0); BigDecimal valY = result.get(1); int intX = valX.setScale(0,
             * BigDecimal.ROUND_HALF_UP).intValue(); int intY = valY.setScale(0,
             * BigDecimal.ROUND_HALF_UP).intValue(); System.out.println("x = " + valX + " ~ " + intX);
             * System.out.println("y = " + valY + " ~ " + intY); // Verify solution for (int[] coeff :
             * coefficients) { int value = coeff[0] * intX + coeff[1] * intY; BigDecimal exact =
             * valX.multiply(BigDecimal.valueOf(coeff[0])).add(valY.multiply(BigDecimal.valueOf(coeff[1])));
             * if (value >= 0) throw new Exception(coeff[0] + "*x + " + coeff[1] + "*y = " + value +
             * " must be negative (exact: " + exact + ")"); } /*
             */
        }
    }

    // Return the coefficients for some inequalities of the form 0 > a*x + b*y
    static List<int[]> getCoefficients() {
        final List<int[]> result = new LinkedList<>();
        // Apparently these are unneeded for reproducing the problem,
        // just need i=199
        /*
         * result.add(new int[] { 0, 1 }); for (int i = 0; i < 200; i++) { result.add(new int[] { -1 - (i/20),
         * -i }); }
         */
        // This is what the above loop would add with i=199
        result.add(new int[] { -10, -199 });

        result.add(new int[] { -10, -200 });
        return result;
    }
}
