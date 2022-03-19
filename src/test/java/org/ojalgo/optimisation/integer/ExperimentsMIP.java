/*
 * Copyright 1997-2022 Optimatika
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.ModelFileTest;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.State;
import org.ojalgo.type.CalendarDateDuration;
import org.ojalgo.type.CalendarDateUnit;
import org.ojalgo.type.Stopwatch;

public class ExperimentsMIP extends OptimisationIntegerTests implements ModelFileTest {

    private static final Map<String, Comparator<NodeKey>> COMPARATORS = new HashMap<>();
    private static final Set<String> MODELS = new HashSet<>();

    static {

        MODELS.add("b-ball.mps");
        MODELS.add("gen-ip021.mps");
        MODELS.add("gen-ip036.mps");
        MODELS.add("mas76.mps");
        MODELS.add("modglob.mps");
        MODELS.add("neos5.mps");
        // MODELS.add("noswot.mps");
        MODELS.add("pk1.mps");
        MODELS.add("pp08a.mps");
        MODELS.add("pp08aCUTS.mps");
        MODELS.add("timtab1.mps");
        MODELS.add("vpm2.mps");

        COMPARATORS.put("DISPLACE_DECR", NodeKey.SMALLEST_DISPLACEMENT);
        COMPARATORS.put("DISPLACE_INCR", NodeKey.LARGEST_DISPLACEMENT);
        COMPARATORS.put("OBJECTIV_DECR", NodeKey.MIN_OBJECTIVE);
        COMPARATORS.put("OBJECTIV_INCR", NodeKey.MAX_OBJECTIVE);
        COMPARATORS.put("SEQUENCE_DECR", NodeKey.EARLIEST_SEQUENCE);
        COMPARATORS.put("SEQUENCE_INCR", NodeKey.LATEST_SEQUENCE);
    }

    public static void main(final String... args) {

        Stopwatch clock = new Stopwatch();

        for (String modelName : MODELS) {

            BasicLogger.debug();
            BasicLogger.debug(modelName);
            BasicLogger.debug("=================================================");

            for (Entry<String, Comparator<NodeKey>> entry : COMPARATORS.entrySet()) {
                String comparatorName = entry.getKey();

                ExpressionsBasedModel model = ModelFileTest.makeModel("miplib", modelName, false);

                model.options.time_suffice = 5L;
                model.options.time_abort = 60L * 1_000L;
                model.options.integer(IntegerStrategy.newConfigurable().withPriorityDefinitions(entry.getValue()));

                clock.reset();
                Result minimise = model.minimise();
                State state = minimise.getState();
                double value = minimise.getValue();
                CalendarDateDuration duration = clock.stop(CalendarDateUnit.MILLIS);

                BasicLogger.debug(18, modelName, comparatorName, state, value, duration);
            }
        }
    }
}
