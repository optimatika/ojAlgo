package org.ojalgo.optimisation;

import static org.ojalgo.function.constant.PrimitiveMath.ONE;

import org.ojalgo.array.operation.MULTIPLY;
import org.ojalgo.type.ReciprocalPair;

public abstract class Equilibrator<P> {

    /**
     * Maximum admissible scaling factor.
     */
    public static final double MAX = 1E+4;
    /**
     * Minimum admissible scaling factor.
     */
    public static final double MIN = 1E-4;

    /**
     * Clamps a single scaling factor to the admissible {@link Equilibrator#MIN}–{@link Equilibrator#MAX}
     * interval, replacing very small values by 1 to avoid excessive rescaling.
     */
    public static double clamp(final double value) {
        if (value < Equilibrator.MIN) {
            return ONE;
        } else if (value > Equilibrator.MAX) {
            return Equilibrator.MAX;
        } else {
            return value;
        }
    }

    /**
     * Clamps each entry of a scaling vector to the admissible
     * {@link Equilibrator#MIN}–{@link Equilibrator#MAX} interval, replacing very small values by 1 to avoid
     * excessive rescaling.
     */
    public static void clamp(final double[] values) {
        for (int i = 0, lim = values.length; i < lim; i++) {
            values[i] = Equilibrator.clamp(values[i]);
        }
    }

    /**
     * Scalar cost-function scaling.
     */
    public double cost = ONE;

    /**
     * Diagonal scaling and its reciprocal for the constraints.
     */
    public final ReciprocalPair dual;
    /**
     * Diagonal scaling and its reciprocal for the primal variables.
     */
    public final ReciprocalPair primal;

    private final int myNumberOfIterations;
    private final double[] myWorkDual;
    private final double[] myWorkPrimal;

    protected Equilibrator(final int nbIterations, final int nbPrimals, final int nbDuals) {

        super();

        primal = new ReciprocalPair(nbPrimals);
        dual = new ReciprocalPair(nbDuals);

        myNumberOfIterations = nbIterations;

        myWorkPrimal = new double[nbPrimals];
        myWorkDual = new double[nbDuals];
    }

    public final void update(final P data) {

        primal.fill(ONE);
        dual.fill(ONE);
        cost = ONE;

        for (int i = 0; i < myNumberOfIterations; i++) {
            this.doUpdateIteration(data, myWorkPrimal, myWorkDual);
        }

        primal.invert();
        dual.invert();

        if (myNumberOfIterations > 0) {
            this.doAfterUpdate(data);
        }
    }

    protected abstract void doAfterUpdate(P data);

    protected abstract void doUpdateIteration(P data, double[] wPrimal, double[] wDual);

    protected final void unscaleDual(final double[] solution) {

        if (myNumberOfIterations > 0) {
            MULTIPLY.invoke(solution, dual.values);
            MULTIPLY.invoke(solution, ONE / cost);
        }
    }

    protected final void unscalePrimal(final double[] solution) {

        if (myNumberOfIterations > 0) {
            MULTIPLY.invoke(solution, primal.values);
        }
    }

}
