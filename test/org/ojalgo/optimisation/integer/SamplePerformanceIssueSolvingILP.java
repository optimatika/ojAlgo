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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * https://github.com/optimatika/ojAlgo/issues/36
 */
public class SamplePerformanceIssueSolvingILP {

    static class Container {

        private final String type;
        private final double size;

        public Container(final String type, final long size) {
            this.type = type;
            this.size = size;
        }

        public double getSize() {
            return size;
        }

        public String getType() {
            return type;
        }
    }

    private static final Random RANDOM = new Random();

    // Weighting so we don't have preference to smaller or bigger container
    private static final int MAX_OBJECTIVE_VALUE = 1000000000;

    private static final double DEVIATION = 0.02D;
    private static final double FIRST_CONTAINER_SIZE_PERC = 0.8;
    private static final double SECOND_CONTAINER_SIZE_PERC = 0.1;

    private static final double THIRD_CONTAINER_SIZE_PERC = 0.1;

    public static void main(final String[] args) {
        final List<Container> containers = Arrays.asList(new Container("first", 75280), new Container("second", 57600), new Container("third", 75280),
                new Container("first", 104000), new Container("first", 156000), new Container("first", 140040), new Container("third", 68000),
                new Container("first", 106560), new Container("first", 158560), new Container("third", 70240), new Container("first", 62080),
                new Container("first", 70240), new Container("first", 105000), new Container("second", 115440), new Container("first", 68000),
                new Container("first", 134880), new Container("first", 171360), new Container("first", 122400), new Container("second", 57600),
                new Container("first", 162600), new Container("first", 112800), new Container("first", 130080), new Container("first", 102000),
                new Container("first", 104160), new Container("first", 105840), new Container("first", 89800), new Container("first", 104160),
                new Container("first", 107200), new Container("first", 175520), new Container("first", 119040), new Container("first", 75360),
                new Container("third", 84000), new Container("first", 60000), new Container("second", 184960), new Container("first", 113600),
                new Container("first", 84000), new Container("first", 196800), new Container("third", 74080), new Container("first", 110520),
                new Container("first", 86000), new Container("first", 125280), new Container("first", 158880), new Container("first", 108960),
                new Container("third", 72432), new Container("second", 26400), new Container("first", 149040), new Container("first", 129920),
                new Container("third", 72800), new Container("first", 74080), new Container("second", 100800), new Container("first", 107200),
                new Container("first", 70080), new Container("first", 75040), new Container("first", 148320), new Container("first", 66240),
                new Container("first", 89840), new Container("first", 167200), new Container("first", 122000), new Container("first", 233280),
                new Container("second", 63000), new Container("first", 81600), new Container("first", 74080), new Container("first", 71520),
                new Container("first", 205760), new Container("first", 132960), new Container("first", 71520), new Container("first", 136200),
                new Container("first", 145440), new Container("first", 164200), new Container("third", 65920), new Container("first", 77600),
                new Container("first", 73280), new Container("first", 112800), new Container("first", 132480), new Container("first", 126240),
                new Container("first", 166560), new Container("first", 100640), new Container("first", 70080), new Container("first", 116800),
                new Container("first", 121600), new Container("first", 95360), new Container("first", 113760), new Container("first", 184800),
                new Container("first", 105840), new Container("third", 86560), new Container("first", 146400), new Container("first", 72800),
                new Container("first", 98400), new Container("second", 62400), new Container("first", 101600), new Container("first", 72432),
                new Container("third", 75360), new Container("second", 208800), new Container("second", 120600), new Container("first", 154880),
                new Container("second", 184800), new Container("first", 64800), new Container("first", 102500), new Container("first", 121600),
                new Container("first", 68640), new Container("first", 200640), new Container("first", 129120), new Container("second", 37200),
                new Container("first", 132960), new Container("first", 95200), new Container("first", 105120), new Container("second", 26400),
                new Container("first", 268880), new Container("first", 126720), new Container("first", 120320), new Container("first", 108000),
                new Container("first", 115680), new Container("second", 238800), new Container("first", 98400), new Container("third", 72320),
                new Container("first", 72320), new Container("first", 96480), new Container("first", 72800), new Container("first", 67200),
                new Container("second", 75040), new Container("first", 113760), new Container("third", 76160), new Container("first", 70400),
                new Container("first", 133920), new Container("first", 107040), new Container("first", 136800), new Container("first", 76160),
                new Container("second", 115500), new Container("first", 100800), new Container("first", 72240), new Container("first", 72640),
                new Container("first", 82640), new Container("first", 97600), new Container("third", 82640), new Container("first", 129120),
                new Container("first", 124800), new Container("first", 112800), new Container("first", 76000), new Container("second", 43200),
                new Container("first", 83200), new Container("first", 101600), new Container("second", 25500), new Container("second", 31200),
                new Container("first", 219200), new Container("first", 97440), new Container("first", 144960), new Container("first", 105120),
                new Container("third", 97680), new Container("first", 114400), new Container("first", 93120), new Container("first", 162720),
                new Container("second", 36000), new Container("first", 105840), new Container("first", 68800), new Container("first", 108000),
                new Container("first", 130560), new Container("third", 74080), new Container("first", 126400), new Container("first", 120960),
                new Container("first", 75040), new Container("third", 68800), new Container("second", 107200), new Container("first", 130560),
                new Container("first", 106800), new Container("first", 153120), new Container("third", 75040), new Container("second", 117240),
                new Container("third", 166800), new Container("first", 130880), new Container("first", 86560), new Container("second", 72640),
                new Container("first", 94080), new Container("first", 132000), new Container("first", 132960), new Container("first", 73200),
                new Container("first", 122880), new Container("first", 103200), new Container("first", 162720), new Container("first", 73920),
                new Container("first", 120000), new Container("second", 126400), new Container("third", 76000), new Container("first", 160800),
                new Container("third", 66240), new Container("second", 48000), new Container("second", 70800), new Container("first", 73920),
                new Container("first", 122080), new Container("first", 125280), new Container("first", 88480), new Container("first", 141600),
                new Container("first", 129000), new Container("first", 65920), new Container("first", 137000), new Container("first", 173280),
                new Container("first", 124640), new Container("second", 33540));

        new SamplePerformanceIssueSolvingILP().solve(2700000L, containers);
    }

    private void solve(final long maxContainerSize, final List<Container> containers) {

        final ExpressionsBasedModel expressionsBasedModel = new ExpressionsBasedModel();

        expressionsBasedModel.options.time_suffice = 60 * 1000;
        expressionsBasedModel.options.iterations_suffice = 64;
        expressionsBasedModel.options.mip_gap = 0.01;

        final double firstContainerSize = maxContainerSize * FIRST_CONTAINER_SIZE_PERC;
        final Expression firstContainerExpression = expressionsBasedModel.addExpression("expression:first").lower(firstContainerSize * (1 - DEVIATION))
                .upper(firstContainerSize * (1 + DEVIATION));

        final double secondContainerSize = maxContainerSize * SECOND_CONTAINER_SIZE_PERC;
        final Expression secondContainerExpression = expressionsBasedModel.addExpression("expression:second").lower(secondContainerSize * (1 - DEVIATION))
                .upper(secondContainerSize * (1 + DEVIATION));

        final double thridContainerSize = maxContainerSize * THIRD_CONTAINER_SIZE_PERC;
        final Expression thirdContainerExpression = expressionsBasedModel.addExpression("expression:third").lower(thridContainerSize * (1 - DEVIATION))
                .upper(thridContainerSize * (1 + DEVIATION));

        for (int cIndex = 0; cIndex < containers.size(); cIndex++) {
            final Container container = containers.get(cIndex);
            final Variable variable = Variable.make("v" + cIndex).weight(RANDOM.nextInt(MAX_OBJECTIVE_VALUE) * container.getSize()).binary();

            expressionsBasedModel.addVariable(variable);

            if (container.getType().equals("first")) {
                firstContainerExpression.set(variable, container.getSize());
            }

            if (container.getType().equals("second")) {
                secondContainerExpression.set(variable, container.getSize());
            }

            if (container.getType().equals("third")) {
                thirdContainerExpression.set(variable, container.getSize());
            }
        }

        // expressionsBasedModel.options.debug(IntegerSolver.class);
        // expressionsBasedModel.relax(true);

        final Optimisation.Result result = expressionsBasedModel.maximise();
        System.out.println("RESULT: " + result);
        expressionsBasedModel.validate();
        expressionsBasedModel.validate(result);

        final List<Container> resultContainer = new ArrayList<>();
        for (int cIndex = 0; cIndex < containers.size(); cIndex++) {
            final BigDecimal bigDecimal = result.get(cIndex);
            if (bigDecimal.compareTo(BigDecimal.ZERO) > 0) {
                resultContainer.add(containers.get(cIndex));
            }
        }

        System.out.println("RESULT: " + resultContainer.size());
    }

}
