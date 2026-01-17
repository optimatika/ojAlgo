package org.ojalgo.type;

import java.util.Arrays;

import org.ojalgo.array.operation.INV;

/**
 * A simple container for a pair of double (R064) arrays, one holding values and the other their reciprocal.
 */
public final class ReciprocalPair {

    public final double[] inverse;
    public final double[] values;

    public ReciprocalPair(final int length) {
        super();
        values = new double[length];
        inverse = new double[length];
    }

    public void fill(final double value) {
        Arrays.fill(values, value);
    }

    public void invert() {
        INV.invoke(inverse, values);
    }

}
