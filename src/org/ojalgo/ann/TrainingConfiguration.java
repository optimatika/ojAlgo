/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.ann;

import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.function.DoubleUnaryOperator;

final class TrainingConfiguration {

    boolean dropouts = false;
    ArtificialNeuralNetwork.Error error = ArtificialNeuralNetwork.Error.HALF_SQUARED_DIFFERENCE;
    double learningRate = ONE;
    boolean regularisationL1 = false;
    double regularisationL1Factor = ZERO;
    boolean regularisationL2 = false;
    double regularisationL2Factor = ZERO;

    TrainingConfiguration() {
        super();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrainingConfiguration)) {
            return false;
        }
        TrainingConfiguration other = (TrainingConfiguration) obj;
        if (dropouts != other.dropouts) {
            return false;
        }
        if (error != other.error) {
            return false;
        }
        if (Double.doubleToLongBits(learningRate) != Double.doubleToLongBits(other.learningRate)) {
            return false;
        }
        if (regularisationL1 != other.regularisationL1) {
            return false;
        }
        if (Double.doubleToLongBits(regularisationL1Factor) != Double.doubleToLongBits(other.regularisationL1Factor)) {
            return false;
        }
        if (regularisationL2 != other.regularisationL2) {
            return false;
        }
        if (Double.doubleToLongBits(regularisationL2Factor) != Double.doubleToLongBits(other.regularisationL2Factor)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (dropouts ? 1231 : 1237);
        result = (prime * result) + ((error == null) ? 0 : error.hashCode());
        long temp;
        temp = Double.doubleToLongBits(learningRate);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + (regularisationL1 ? 1231 : 1237);
        temp = Double.doubleToLongBits(regularisationL1Factor);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        result = (prime * result) + (regularisationL2 ? 1231 : 1237);
        temp = Double.doubleToLongBits(regularisationL2Factor);
        result = (prime * result) + (int) (temp ^ (temp >>> 32));
        return result;
    }

    private double doL1(final double current) {
        if (current < ZERO) {
            return -regularisationL1Factor;
        } else {
            return regularisationL1Factor;
        }
    }

    private double doL2(final double current) {
        return regularisationL2Factor * current;
    }

    /**
     * Used to scale the weights after training with dropouts, and also to adjut the learning rate
     *
     * @param layer
     * @return The probabilityToKeep (as in not drop) the input nodes of this layer
     */
    double probabilityDidKeepInput(final int layer) {
        if (dropouts && (layer != 0)) {
            return HALF;
        } else {
            return ONE;
        }
    }

    /**
     * Used to modify the activation function – with this probabilty it will be used as is, for the other
     * parts the output is 0.
     *
     * @param layer
     * @param depth
     * @return
     */
    double probabilityWillKeepOutput(final int layer, final int depth) {
        if (dropouts && (layer < (depth - 1))) {
            return HALF;
        } else {
            return ONE;
        }
    }

    DoubleUnaryOperator regularisation() {
        if (regularisationL2) {
            if (regularisationL1) {
                return current -> this.doL1(current) + this.doL2(current);
            } else {
                return this::doL2;
            }
        } else {
            if (regularisationL1) {
                return this::doL1;
            } else {
                return null;
            }
        }
    }

}
