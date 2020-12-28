package org.ojalgo.optimisation.integer;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Hi everyone,
 * <p>
 * the attached Java program solves a relatively simple ILP with ojAlgo. However, the solution calculates is
 * invalid. Can somehow shed some light into why this is?
 * </p>
 * <p>
 * Hopefully the program explains itself, but here is some more text:
 * </p>
 * <p>
 * The function getCoefficients() returns the coefficients for 202 inequalities of the form coef[0]*x +
 * coef[1]*y < 0. The main function creates an ExpressionsBasedModel from this, transformimg the "< 0" into
 * "<= -1" (which is valid since this is about integer values). It prints the solution calculated and then
 * checks if all the inequalities really are satisfied.
 * </p>
 * <p>
 * However, this fails since the solution calculated doesn't solve all inequalities:
 *
 * <pre>
 * x = 19.999999999999993 ~ 20
 * y = -0.9950000000000006 ~ -1
 * Exception in thread "main" java.lang.Exception: -10*x + -200*y = 0 must be negative (exact: -0.9999999999998100)
 *     at Solve.main(Solve.java:60)
 * </pre>
 *
 * The program also prints the exact solutions calculated from the BigDecimals that ojAlgo produced. This is
 * the "exact" value you see in the error message above and, as you see, this value (almost) satisfies the
 * condition "<= -1".
 * </p>
 * <p>
 * Valid solutions to this inequality systems would be x=21, y=-1 or x=201, y=-10.
 * </p>
 * <p>
 * Obviously, this seems to be a rounding issue. Is there anything that can be done about it?
 * </p>
 * Cheers, Uli * http://bugzilla.optimatika.se/show_bug.cgi?id=224
 */
public class P20150127a {

    public static ExpressionsBasedModel getModel() {

        final ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        final Variable x = Variable.make("x").integer(true);
        final Variable y = Variable.make("y").integer(true);
        retVal.addVariable(x);
        retVal.addVariable(y);

        int counter = 0;
        for (final int[] coeff : P20150127a.getCoefficients()) {
            final Expression tmpExpression = retVal.addExpression("inequality_" + ++counter);
            // We want coeff[0] * x + coeff[1] * y < 0. Since our
            // solutions are integer, we can do "<= -1".
            tmpExpression.upper(BigDecimal.ONE.negate());
            tmpExpression.set(x, coeff[0]);
            tmpExpression.set(y, coeff[1]);
        }

        return retVal;
    }

    static public void main(final String[] args) throws Exception {

        final ExpressionsBasedModel model = P20150127a.getModel();

        // final Optimisation.Result result = model.solve(null);
        final Optimisation.Result result = model.minimise();
        if (!result.getState().isSuccess()) {
            throw new Exception("Model should be solvable (e.g. x=201, y=-10)?!");
        }

        final BigDecimal valX = result.get(0);
        final BigDecimal valY = result.get(1);
        final int intX = valX.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        final int intY = valY.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

        // model.options.debug(LinearSolver.class);

        System.out.println("x = " + valX + " ~ " + intX);
        System.out.println("y = " + valY + " ~ " + intY);

        model.validate();
        model.validate(result);

        final List<int[]> coefficients = P20150127a.getCoefficients();

        // Verify solution
        for (final int[] coeff : coefficients) {
            final int value = (coeff[0] * intX) + (coeff[1] * intY);
            final BigDecimal exact = valX.multiply(BigDecimal.valueOf(coeff[0])).add(valY.multiply(BigDecimal.valueOf(coeff[1])));
            if (value >= 0) {
                throw new Exception(coeff[0] + "*x + " + coeff[1] + "*y = " + value + " must be negative (exact: " + exact + ")");
            }
        }
    }

    // Return the coefficients for some inequalities of the form 0 > a*x + b*y
    static List<int[]> getCoefficients() {
        final List<int[]> result = new LinkedList<>();
        result.add(new int[] { 0, 1 });
        for (int i = 0; i < 200; i++) {
            result.add(new int[] { -1 - (i / 20), -i });
        }
        result.add(new int[] { -10, -200 });
        return result;
    }
}
