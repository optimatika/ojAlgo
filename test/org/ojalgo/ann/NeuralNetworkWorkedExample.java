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

import static org.ojalgo.ann.ArtificialNeuralNetwork.Activator.*;
import static org.ojalgo.ann.ArtificialNeuralNetwork.Error.*;
import static org.ojalgo.constant.PrimitiveMath.*;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.ojalgo.access.Access1D;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.netio.BasicLogger;
import org.ojalgo.netio.LineSplittingParser;
import org.ojalgo.type.context.NumberContext;

/**
 * https://gormanalysis.com/neural-networks-a-worked-example/
 * https://github.com/ben519/MLPB/tree/master/Problems/Classify%20Images%20of%20Stairs
 *
 * @author apete
 */
public class NeuralNetworkWorkedExample extends BackPropagationExample {

    private static final NumberContext PRECISION = new NumberContext(5, 5);

    public NeuralNetworkWorkedExample() {
        super();
    }

    @Test
    public void testNeuralNetworkWorkedExample() {

        NumberContext precision = this.precision();
        Factory<Double, PrimitiveDenseStore> factory = PrimitiveDenseStore.FACTORY;

        NetworkBuilder builder = this.getInitialNetwork();

        ArtificialNeuralNetwork network = builder.get();

        PrimitiveDenseStore input_1 = factory.rows(new double[] { 252, 4, 155, 175 });
        PrimitiveDenseStore input_2 = factory.rows(new double[] { 175, 10, 186, 200 });
        PrimitiveDenseStore input_3 = factory.rows(new double[] { 82, 131, 230, 100 });
        PrimitiveDenseStore input_4 = factory.rows(new double[] { 115, 138, 80, 88 });

        PrimitiveDenseStore layer0_1 = factory.rows(new double[] { 0.39558, 0.75548 });
        PrimitiveDenseStore layer0_2 = factory.rows(new double[] { 0.47145, 0.58025 });
        PrimitiveDenseStore layer0_3 = factory.rows(new double[] { 0.77841, 0.70603 });
        PrimitiveDenseStore layer0_4 = factory.rows(new double[] { 0.50746, 0.71304 });

        PrimitiveDenseStore layer1_1 = factory.rows(new double[] { 0.49865, 0.50135 });
        PrimitiveDenseStore layer1_2 = factory.rows(new double[] { 0.49826, 0.50174 });
        PrimitiveDenseStore layer1_3 = factory.rows(new double[] { 0.49747, 0.50253 });
        PrimitiveDenseStore layer1_4 = factory.rows(new double[] { 0.49828, 0.50172 });

        this.compare(input_1, network, precision, layer0_1, layer1_1);
        this.compare(input_2, network, precision, layer0_2, layer1_2);
        this.compare(input_3, network, precision, layer0_3, layer1_3);
        this.compare(input_4, network, precision, layer0_4, layer1_4);

        PrimitiveDenseStore target_1 = factory.rows(new double[] { 1, 0 });
        PrimitiveDenseStore target_2 = factory.rows(new double[] { 1, 0 });
        PrimitiveDenseStore target_3 = factory.rows(new double[] { 0, 1 });
        PrimitiveDenseStore target_4 = factory.rows(new double[] { 0, 1 });

        PrimitiveDenseStore input = factory.rows(input_1, input_2, input_3, input_4);
        PrimitiveDenseStore target = factory.rows(target_1, target_2, target_3, target_4);
        PrimitiveDenseStore expected_output = factory.rows(layer1_1, layer1_2, layer1_3, layer1_4);
        // Access2D<Double> actual_output = network.applyBatch(input);

        // TestUtils.assertEquals(expected_output, actual_output, precision);

        builder.train(input_1, target_1, 0.1);
        builder.train(input_2, target_2, 0.1);
        builder.train(input_3, target_3, 0.1);
        builder.train(input_4, target_4, 0.1);

        builder.trainRows(input, target, 0.1);

        LineSplittingParser parser = new LineSplittingParser(",", true);

        parser.parse(new File("./test/org/ojalgo/ann/train.csv"), true, columns -> {

            // R1C1,R1C2,R2C1,R2C2,IsStairs
            int R1C1 = Integer.parseInt(columns[1]);
            int R1C2 = Integer.parseInt(columns[2]);
            int R2C1 = Integer.parseInt(columns[3]);
            int R2C2 = Integer.parseInt(columns[4]);
            int IsStairs = Integer.parseInt(columns[5]);

            PrimitiveDenseStore input_csv = PrimitiveDenseStore.FACTORY.rows(new double[] { R1C1, R1C2, R2C1, R2C2 });
            PrimitiveDenseStore output_csv = PrimitiveDenseStore.FACTORY.rows(new double[] { IsStairs, ONE - IsStairs });

            builder.train(input_csv, output_csv, 1);
        });

        parser.parse(new File("./test/org/ojalgo/ann/test.csv"), true, columns -> {

            // R1C1,R1C2,R2C1,R2C2,IsStairs
            int R1C1 = Integer.parseInt(columns[1]);
            int R1C2 = Integer.parseInt(columns[2]);
            int R2C1 = Integer.parseInt(columns[3]);
            int R2C2 = Integer.parseInt(columns[4]);
            int IsStairs = Integer.parseInt(columns[5]);

            PrimitiveDenseStore input_csv = PrimitiveDenseStore.FACTORY.rows(new double[] { R1C1, R1C2, R2C1, R2C2 });

            Access1D<Double> output_net = network.apply(input_csv);

            BasicLogger.debug("{}, but was {}", IsStairs, output_net.doubleValue(0));
            // TestUtils.assertEquals(columns[0], IsStairs, Math.round(output_net.doubleValue(0)));
        });

    }

    @Override
    protected NumberContext precision() {
        return PRECISION;
    }

    @Override
    protected NetworkBuilder getInitialNetwork() {

        NetworkBuilder builder = new NetworkBuilder(4, 2, 2);

        builder.activator(0, SIGMOID).activator(1, SOFTMAX).error(CROSS_ENTROPY);

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
    protected TrainingTriplet getTrainingTriplet() {

        TrainingTriplet retVal = new TrainingTriplet(0.01);

        //        retVal.input();
        //        retVal.target();
        //        retVal.expected();

        return retVal;
    }

}
