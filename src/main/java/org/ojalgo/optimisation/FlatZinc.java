/*
 * Copyright 1997-2026 Optimatika
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
package org.ojalgo.optimisation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.ojalgo.type.context.NumberContext;

/**
 * ojAlgo as a FlatZinc solver – the program MiniZinc runs with the FlatZinc file it compiled a model to:
 *
 * <pre>
 * java -cp ojalgo.jar org.ojalgo.optimisation.FlatZinc [options] model.fzn
 * </pre>
 * <p>
 * The model should be compiled with MiniZinc's linear library ({@code "mznlib": "-Glinear"} in the solver
 * configuration file), see {@link FileFormatFZN}. Solutions are printed as a FlatZinc solver prints them:
 * the output variables, then {@code ----------}, and {@code ==========} when the solution is proven optimal.
 * {@code =====UNSATISFIABLE=====} when there is no solution, and {@code =====UNKNOWN=====} when none was
 * found in the time there was.
 * <p>
 * Options:
 * <dl>
 * <dt>-a, -i</dt>
 * <dd>All/intermediate solutions: each new best solution is printed as it is found. (For a satisfaction
 * problem only one solution is found.)</dd>
 * <dt>-f</dt>
 * <dd>Free search. Always – search annotations are ignored.</dd>
 * <dt>-p &lt;n&gt;</dt>
 * <dd>Number of threads (default 1).</dd>
 * <dt>-t &lt;ms&gt;</dt>
 * <dd>Time limit in milliseconds. A solution found by then is printed before then.</dd>
 * <dt>-s</dt>
 * <dd>Statistics, as {@code %%%mzn-stat:} lines.</dd>
 * <dt>-v</dt>
 * <dd>Verbose, to standard error.</dd>
 * <dt>-n &lt;n&gt;, -r &lt;seed&gt;</dt>
 * <dd>Accepted and ignored.</dd>
 * <dt>--all-values</dt>
 * <dd>Also print every FlatZinc variable's value, after the output variables – to check a solution against
 * the FlatZinc file.</dd>
 * <dt>--parse-only</dt>
 * <dd>Only parse the file, and print the size of the model.</dd>
 * </dl>
 *
 * @author apete
 */
public abstract class FlatZinc {

    /**
     * The MIP gap tolerance used: requiring the gap to close completely – the objective is integral, and then
     * the gap closes at the next attainable value. The default tolerance would stop at a solution "close
     * enough" and print it as optimal.
     */
    public static final NumberContext GAP = NumberContext.of(12, 9);

    public static void main(final String[] args) {

        long start = System.nanoTime();

        boolean allValues = false;
        boolean intermediate = false;
        boolean parseOnly = false;
        boolean statistics = false;
        boolean verbose = false;
        int threads = 1;
        long timeLimit = -1L;
        String file = null;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-a", "-i", "--all-solutions", "--intermediate-solutions" -> intermediate = true;
                    case "-f", "--free-search" -> {
                        // always
                    }
                    case "-s", "--statistics" -> statistics = true;
                    case "-v", "--verbose" -> verbose = true;
                    case "-p", "--parallel" -> threads = Integer.parseInt(args[++i]);
                    case "-t", "--time-limit" -> timeLimit = Long.parseLong(args[++i]);
                    case "-n", "-r", "--num-solutions", "--random-seed", "--seed" -> i++;
                    case "--all-values" -> allValues = true;
                    case "--parse-only" -> parseOnly = true;
                    default -> {
                        if (args[i].startsWith("-")) {
                            System.err.println("Ignoring unknown option " + args[i]);
                        } else {
                            file = args[i];
                        }
                    }
                }
            }
        } catch (RuntimeException cause) {
            FlatZinc.error("Bad options: " + cause.getMessage());
            return;
        }

        if (file == null) {
            FlatZinc.error("Usage: java -cp ojalgo.jar org.ojalgo.optimisation.FlatZinc [options] model.fzn");
            return;
        }

        FileFormatFZN.Parsed parsed;
        Map<String, Object> symbols = new LinkedHashMap<>();
        try (InputStream input = new FileInputStream(file)) {
            parsed = FileFormatFZN.parse(input, ExpressionsBasedModel::new, symbols);
        } catch (IOException | RuntimeException cause) {
            FlatZinc.error(cause.getMessage());
            return;
        }

        long parsedAt = System.nanoTime();
        long parseMillis = (parsedAt - start) / 1_000_000L;

        ExpressionsBasedModel model = parsed.getModel();

        if (parseOnly) {
            long integers = model.getVariables().stream().filter(Variable::isInteger).count();
            System.out.println("parsed variables=" + model.countVariables() + " integer=" + integers + " expressions=" + model.countExpressions()
                    + " sense=" + parsed.getSense() + " ms=" + parseMillis);
            System.out.flush();
            System.exit(0);
        }

        model.options.parallelism(threads);
        model.options.integer(IntegerStrategy.DEFAULT.withGapTolerance(GAP));

        if (timeLimit > 0L) {
            // Some margin to stop, and print, before the time is up
            long margin = Math.max(500L, timeLimit / 50L);
            long remaining = Math.max(1L, timeLimit - parseMillis - margin);
            model.options.time_abort = remaining;
            model.options.time_suffice = remaining;
        }

        if (verbose) {
            System.err.println("% ojAlgo: " + model.countVariables() + " variables, " + model.countExpressions() + " expressions, parsed in " + parseMillis
                    + " ms");
        }

        Optimisation.Sense sense = parsed.getSense();

        // The last solution printed
        AtomicReference<String> printed = new AtomicReference<>();
        AtomicInteger incumbents = new AtomicInteger();

        if (intermediate) {
            boolean withAllValues = allValues;
            model.options.incumbent(incumbent -> {
                incumbents.incrementAndGet();
                String solution = FlatZinc.solution(parsed, incumbent, withAllValues, symbols);
                System.out.print(solution);
                System.out.print("----------\n");
                System.out.flush();
                printed.set(solution);
            });
        }

        Optimisation.Result result;
        try {
            result = sense == Optimisation.Sense.MAX ? model.maximise() : model.minimise();
        } catch (RuntimeException cause) {
            FlatZinc.error(cause.toString());
            return;
        }

        long solvedAt = System.nanoTime();

        Optimisation.State state = result.getState();

        StringBuilder text = new StringBuilder();

        if (state == Optimisation.State.UNBOUNDED) {
            text.append("=====UNBOUNDED=====\n");
        } else if (state.isFeasible()) {
            String solution = FlatZinc.solution(parsed, result, allValues, symbols);
            if (!solution.equals(printed.get())) {
                text.append(solution);
                text.append("----------\n");
            }
            if (state.isOptimal() && sense != null) {
                text.append("==========\n");
            }
        } else if (state == Optimisation.State.INFEASIBLE) {
            text.append("=====UNSATISFIABLE=====\n");
        } else {
            text.append("=====UNKNOWN=====\n");
        }

        if (statistics) {
            text.append("%%%mzn-stat: flatTime=").append((parsedAt - start) / 1.0E9).append('\n');
            text.append("%%%mzn-stat: solveTime=").append((solvedAt - parsedAt) / 1.0E9).append('\n');
            if (state.isFeasible() && sense != null) {
                text.append("%%%mzn-stat: objective=").append(result.getValue()).append('\n');
            }
            text.append("%%%mzn-stat-end\n");
        }

        System.out.print(text);
        System.out.flush();

        if (verbose) {
            System.err.println("% ojAlgo: " + state + " " + result.getValue() + ", solved in " + (solvedAt - parsedAt) / 1_000_000L + " ms, "
                    + incumbents.get() + " incumbents printed");
        }

        System.exit(0);
    }

    private static String solution(final FileFormatFZN.Parsed parsed, final Optimisation.Result result, final boolean allValues,
            final Map<String, Object> symbols) {
        StringBuilder retVal = new StringBuilder();
        parsed.printSolution(result, retVal);
        if (allValues) {
            retVal.append("% all values\n");
            parsed.printAll(result, retVal, symbols);
        }
        return retVal.toString();
    }

    private static void error(final String message) {
        System.err.println(message);
        System.out.println("=====ERROR=====");
        System.out.flush();
        System.exit(1);
    }

}
