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

import static org.ojalgo.ann.ArtificialNeuralNetwork.Activator.*;
import static org.ojalgo.ann.ArtificialNeuralNetwork.Error.*;
import static org.ojalgo.function.constant.PrimitiveMath.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.netio.LineSplittingParser;
import org.ojalgo.structure.Access1D;
import org.ojalgo.type.context.NumberContext;

/**
 * https://gormanalysis.com/neural-networks-a-worked-example/
 * https://github.com/ben519/MLPB/tree/master/Problems/Classify%20Images%20of%20Stairs
 *
 * @author apete
 */
public class NeuralNetworkWorkedExample extends BackPropagationExample {

    private static final double _255_0 = 255.0;
    private static final NumberContext PRECISION = new NumberContext(5, 5);

    public NeuralNetworkWorkedExample() {
        super();
    }

    /**
     * Train on the "training" data with random weights and normalised input, and test against the "test"
     * data.
     */
    @Test
    public void testTraining() {

        NetworkTrainer builder = this.getInitialNetwork(Primitive64Store.FACTORY);

        LineSplittingParser parser = new LineSplittingParser(",", true);

        for (int i = 0; i < 1000; i++) {

            parser.parse("./test/org/ojalgo/ann/train.csv", true, columns -> {

                // R1C1,R1C2,R2C1,R2C2,IsStairs
                double R1C1 = Double.parseDouble(columns[1]);
                double R1C2 = Double.parseDouble(columns[2]);
                double R2C1 = Double.parseDouble(columns[3]);
                double R2C2 = Double.parseDouble(columns[4]);
                int IsStairs = Integer.parseInt(columns[5]);

                Primitive64Store input_csv = Primitive64Store.FACTORY.row(R1C1 / _255_0, R1C2 / _255_0, R2C1 / _255_0, R2C2 / _255_0);
                Primitive64Store output_csv = Primitive64Store.FACTORY.row(IsStairs, ONE - IsStairs);

                builder.rate(0.01).train(input_csv, output_csv);
            });

        }

        ArtificialNeuralNetwork network = builder.get();
        AtomicInteger correct = new AtomicInteger();
        AtomicInteger wrong = new AtomicInteger();

        parser.parse("./test/org/ojalgo/ann/test.csv", true, columns -> {

            // R1C1,R1C2,R2C1,R2C2,IsStairs
            double R1C1 = Double.parseDouble(columns[1]);
            double R1C2 = Double.parseDouble(columns[2]);
            double R2C1 = Double.parseDouble(columns[3]);
            double R2C2 = Double.parseDouble(columns[4]);
            int IsStairs = Integer.parseInt(columns[5]);

            Primitive64Store input_csv = Primitive64Store.FACTORY.row(R1C1 / _255_0, R1C2 / _255_0, R2C1 / _255_0, R2C2 / _255_0);

            Access1D<Double> output_net = network.invoke(input_csv);

            if (IsStairs == Math.round(output_net.doubleValue(0))) {
                correct.incrementAndGet();
            } else {
                wrong.incrementAndGet();
            }
        });

        double quotient = correct.doubleValue() / wrong.doubleValue();
        // Can get quotients of more than 30. 5.0 should be a safe limit
        TestUtils.assertTrue(Double.toString(quotient), quotient > FIVE);
    }

    @Override
    protected NetworkTrainer getInitialNetwork(Factory<Double, ?> factory) {

        NetworkTrainer builder = ArtificialNeuralNetwork.builder(factory, 4, 2, 2);

        builder.activators(SIGMOID, SOFTMAX).error(CROSS_ENTROPY);

        builder.bias(0, 0, -0.00469);
        builder.bias(0, 1, 0.00797);
        builder.weight(0, 0, 0, -0.00256);
        builder.weight(0, 0, 1, 0.00889);
        builder.weight(0, 1, 0, 0.00146);
        builder.weight(0, 1, 1, 0.00322);
        builder.weight(0, 2, 0, 0.00816);
        builder.weight(0, 2, 1, 0.00258);
        builder.weight(0, 3, 0, -0.00597);
        builder.weight(0, 3, 1, -0.00876);

        builder.bias(1, 0, -0.00588);
        builder.bias(1, 1, -0.00232);
        builder.weight(1, 0, 0, -0.00647);
        builder.weight(1, 0, 1, 0.00540);
        builder.weight(1, 1, 0, 0.00347);
        builder.weight(1, 1, 1, -0.00005);

        return builder;
    }

    @Override
    protected List<Data> getTestCases() {

        List<Data> retVal = new ArrayList<>();

        // In the example the input is not normalised (which is very odd)
        retVal.add(new Data().input(252, 4, 155, 175).target(1, 0).expected(0.49865, 0.50135));
        retVal.add(new Data().input(175, 10, 186, 200).target(1, 0).expected(0.49826, 0.50174));
        retVal.add(new Data().input(82, 131, 230, 100).target(0, 1).expected(0.49747, 0.50253));
        retVal.add(new Data().input(115, 138, 80, 88).target(0, 1).expected(0.49828, 0.50172));

        return retVal;
    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

}
