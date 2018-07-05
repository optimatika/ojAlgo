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
package org.ojalgo.ann;

import java.util.Collections;
import java.util.List;

import org.ojalgo.type.context.NumberContext;

/**
 * NeuralNetworksDemystified
 *
 * @author apete
 */
public class NeuralNetworksDemystified extends BackPropagationExample {

    private static final NumberContext PRECISION = new NumberContext(8, 8);

    public NeuralNetworksDemystified() {
        super();
    }

    @Override
    protected NetworkBuilder getInitialNetwork() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<TrainingTriplet> getTriplets() {

        TrainingTriplet retVal = new TrainingTriplet();

        return Collections.emptyList();
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
