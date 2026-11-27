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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.netio.InMemoryFile;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Optimisation.Sense;
import org.ojalgo.type.context.NumberContext;

/**
 * Writing a model in a file format and reading it back, and translating it from one format to another – read
 * in one, written in another – as the Optimisation Service's translate does. LP, MPS and EBM are translated in
 * every direction, and FZN, which is only read, to each of them. The text changes, but the mathematical
 * problem must not: as many variables, as many of them integer, and the same optimal value minimised and
 * maximised.
 * <p>
 * The names in a model can be just about anything, and each format has its own rules for names. What is
 * written must follow them, so that it is read back, whatever the names in the model. Two things may change:
 * <ul>
 * <li>Names. A name a format does not have is replaced by one it has.
 * <li>The optimisation sense. EBM does not record it, and an MPS file need not. Each case states its own
 * sense, and the solves here say minimise or maximise themselves. A sense recorded in a file is checked
 * against the sense of the model that was written, when that model had one. A model read from EBM has none
 * – and the LP format, which always states a sense, then says Minimize.
 * </ul>
 * Solving a model sets its sense, so a model that is written is never solved, and the reverse.
 */
class FileFormatTranslationTest extends OptimisationTests {

    /**
     * A model as text, in its format, and what it is: its sense, if the text says, and its optimal value
     * minimised and maximised – null where that is no test, as maximising a convex quadratic.
     */
    record Case(String text, FileFormat format, Sense sense, Double minimum, Double maximum) {
    }

    private static final NumberContext ACCURACY = NumberContext.of(7, 8);

    /**
     * The model in {@link FileFormatFZNTest#testFileFormat()}: maximised 9, minimised 0.
     */
    private static final Case FLAT_ZINC = new Case("""
            % A comment
            predicate my_predicate(var int: x);
            array [1..3] of int: w = [2, 3, 4];
            var 0..1: x;
            var 0..1: y;
            var 0..1: z;
            var 0..9: value;
            constraint int_lin_le(w, [x, y, z], 6);
            constraint int_lin_eq([5, 4, 3, -1], [x, y, z, value], 0) :: defines_var(value);
            solve :: int_search([x, y, z], input_order, indomain_max, complete) maximize value;
            """, FileFormat.FZN, Sense.MAX, 0.0, 9.0);

    /**
     * The model in {@link FileFormatFZNTest#testDomainWithRanges()}: minimised 7, maximised 9. A domain with
     * holes is read as one binary variable per range of values, with names such as x#1..3 – names the LP
     * format does not have.
     */
    private static final Case FLAT_ZINC_WITH_HOLES = new Case("""
            var {1..3, 7..9}: x :: output_var;
            var -5..5: y :: output_var;
            constraint int_le(4, x);
            constraint set_in(y, {-5, -2..-1, 4});
            constraint int_lin_le([-1], [y], -2);
            solve minimize x;
            """, FileFormat.FZN, Sense.MIN, 7.0, 9.0);

    /**
     * Maximised 46.5, minimised 10. Names longer than MPS has room for, and variables that are free,
     * negative, integer and binary.
     */
    private static final Case MILP = new Case("""
            \\ A comment
            Maximize
             profit: 3 x1 + 5 y_long_name + 2 flow_a_b_long_name_123 - 1 w + 4 b
            Subject To
             cap: x1 + y_long_name + flow_a_b_long_name_123 <= 10
             mix: x1 - w >= -2
             bal: y_long_name + flow_a_b_long_name_123 = 6
             link: b - x1 <= 0
             def_f: f - x1 = -1
            Bounds
             0 <= x1 <= 8
             -5 <= w <= 5
             y_long_name <= 4.5
             f free
            General
             x1
            Binary
             b
            End
            """, FileFormat.LP, Sense.MAX, 10.0, 46.5);

    /**
     * Minimised 8, maximised 56. A long objective name and a long constraint name, a range, a dot in a name
     * and a bound at -inf.
     */
    private static final Case MILP_WITH_RANGE = new Case("""
            Minimize
             total_cost_objective: 2 x.1 + 4 y2 + z
            Subject To
             a_very_long_constraint_name: x.1 + y2 >= 3
             c2: x.1 - z <= 1
             c3: z >= -4
             r: -4 <= x.1 - y2 <= 8
            Bounds
             -inf <= z <= 10
             x.1 <= 5
            General
             y2
            End
            """, FileFormat.LP, Sense.MIN, 8.0, 56.0);

    /**
     * Minimised 0, maximised 8. Names that MPS has but LP does not: one starting with a digit, and
     * parentheses. No OBJSENSE, so no sense.
     */
    private static final Case MPS_WITH_NAMES_LP_DOES_NOT_HAVE = new Case("""
            NAME
            ROWS
             N  obj
             L  c(1)
            COLUMNS
                1x        obj       1
                1x        c(1)      1
                x(2)      obj       2
                x(2)      c(1)      1
            RHS
                rhs       c(1)      4
            ENDATA
            """, FileFormat.MPS, null, 0.0, 8.0);

    /**
     * Names no format has as they are: empty and blank, with spaces, tabs and line breaks, with operators,
     * brackets, separators and comment characters, starting with a digit or a period, words the formats keep
     * for themselves, the names the formats give the objective function and their own sections, letters
     * outside ASCII, longer than the formats take – and the same name twice.
     */
    private static final String[] NAMES = { "", " ", "x", "x", "1x", ".x", "_x", "x.1", "x y", "x\ty", "x\ny", "x\r\ny", "x y", "a+b", "a-b", "-x",
            "x*y", "x^2", "x/2", "[x]", "x:y", "x<=y", "=", ">=", "x(2)", "x#1..3", "y#-5..-5", "a,b", "a;b", "\\x", "*x", "$x", "'x'", "\"x\"", "élan", "日本",
            "𝑥", "end", "End", "Subject", "st", "s.t.", "bin", "Bounds", "General", "free", "inf", "Infinity", "obj", "rhs", "bnd", "rng", "NAME",
            "ROWS", "MARKER", "'MARKER'", "INTORG", "V0000000", "E0000000", "x".repeat(300) };

    /**
     * Minimised -0.78125 at x = 0.375, y = 0.625. A convex quadratic, so maximising it is no test.
     */
    private static final Case QP = new Case("""
            Minimize
             risk: - x - 2 y + [ x ^2 + 0.5 x * y + 1.5 y ^2 ]
            Subject To
             budget: x + y = 1
            End
            """, FileFormat.LP, Sense.MIN, -0.78125, null);

    /**
     * The formats that are written, as well as read.
     */
    private static final Set<FileFormat> WRITTEN = EnumSet.of(FileFormat.EBM, FileFormat.LP, FileFormat.MPS);

    private static void assertOptima(final String path, final Double minimum, final Double maximum, final ExpressionsBasedModel model) {

        if (minimum != null) {
            Result result = model.minimise();
            TestUtils.assertTrue(path + ": minimised " + result, result.getState().isOptimal());
            TestUtils.assertEquals(path + ": the minimum", minimum.doubleValue(), result.getValue(), ACCURACY);
        }

        if (maximum != null) {
            Result result = model.maximise();
            TestUtils.assertTrue(path + ": maximised " + result, result.getState().isOptimal());
            TestUtils.assertEquals(path + ": the maximum", maximum.doubleValue(), result.getValue(), ACCURACY);
        }
    }

    /**
     * The case's model written in each format that is written – and its own text, in its own format.
     */
    private static void assertTranslations(final Case original) {

        ExpressionsBasedModel solved = FileFormatTranslationTest.read(original.format() + " as given", original.text(), original.format());
        if (original.sense() != null) {
            TestUtils.assertEquals(original.format() + ": the sense read", original.sense(), solved.getOptimisationSense());
        }
        FileFormatTranslationTest.assertOptima(original.format() + " as given", original.minimum(), original.maximum(), solved);

        ExpressionsBasedModel model = FileFormatTranslationTest.read(original.format() + " as given", original.text(), original.format());

        Map<FileFormat, String> sources = new EnumMap<>(FileFormat.class);
        sources.put(original.format(), original.text());
        for (FileFormat format : WRITTEN) {
            sources.computeIfAbsent(format, f -> FileFormatTranslationTest.write(model, f));
        }

        FileFormatTranslationTest.assertTranslations(sources, original.minimum(), original.maximum());
    }

    /**
     * Each source translated to each format that is written, read back, and compared.
     */
    private static void assertTranslations(final Map<FileFormat, String> sources, final Double minimum, final Double maximum) {

        for (Map.Entry<FileFormat, String> source : sources.entrySet()) {

            for (FileFormat to : WRITTEN) {

                String path = source.getKey() + " to " + to;

                ExpressionsBasedModel from = FileFormatTranslationTest.read(path + ", reading " + source.getKey(), source.getValue(), source.getKey());
                String text = FileFormatTranslationTest.write(from, to);
                ExpressionsBasedModel translated = FileFormatTranslationTest.read(path, text, to);

                TestUtils.assertTrue(path + ": validates", translated.validate());
                TestUtils.assertEquals(path + ": variables", from.countVariables(), translated.countVariables());
                TestUtils.assertEquals(path + ": integer variables", FileFormatTranslationTest.countIntegers(from),
                        FileFormatTranslationTest.countIntegers(translated));

                Sense written = from.getOptimisationSense();
                Sense recorded = translated.getOptimisationSense();
                if (written != null && recorded != null) {
                    TestUtils.assertEquals(path + ": the sense recorded", written, recorded);
                }

                FileFormatTranslationTest.assertOptima(path, minimum, maximum, translated);
            }
        }
    }

    /**
     * A model built here – each time the same – written in each format that is written, then translated, read
     * back and compared with the model itself.
     */
    private static void assertWritten(final Supplier<ExpressionsBasedModel> factory, final boolean maximise) {

        Result minimum = factory.get().minimise();
        TestUtils.assertTrue("minimised " + minimum, minimum.getState().isOptimal());

        Double maximum = null;
        if (maximise) {
            Result result = factory.get().maximise();
            TestUtils.assertTrue("maximised " + result, result.getState().isOptimal());
            maximum = result.getValue();
        }

        Map<FileFormat, String> sources = new EnumMap<>(FileFormat.class);
        for (FileFormat format : WRITTEN) {
            sources.put(format, FileFormatTranslationTest.write(factory.get(), format));
        }

        FileFormatTranslationTest.assertTranslations(sources, minimum.getValue(), maximum);
    }

    private static long countIntegers(final ExpressionsBasedModel model) {
        return model.getVariables().stream().filter(Variable::isInteger).count();
    }

    /**
     * Each of {@link #NAMES} as the name of a variable, and of a constraint. Each variable between 0 and 10 –
     * every fifth one integer, unless quadratic – and in 3 constraints, of each kind there is, so that the
     * model has an optimum both minimised and maximised. With the objective function its own expression, its
     * name is one no format has either; otherwise it has none of its own, while a constraint is named obj.
     */
    private static ExpressionsBasedModel newModelWithNames(final boolean objectiveExpression, final boolean quadratic) {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        int n = NAMES.length;

        for (int i = 0; i < n; i++) {
            model.newVariable(NAMES[i]).lower(0).upper(10).integer(!quadratic && i % 5 == 1);
        }

        Set<String> named = new HashSet<>();
        for (int i = 0; i < n; i++) {

            String name = named.add(NAMES[i]) ? NAMES[i] : NAMES[i] + " again";

            Expression constraint = model.newExpression(name).set(i, 1).set((i + 1) % n, 2).set((i + 2) % n, 3);

            switch (i % 4) {
                case 0 -> constraint.upper(15);
                case 1 -> constraint.lower(2);
                case 2 -> constraint.lower(1).upper(20);
                default -> constraint.level(6 + i % 3);
            }
        }

        Expression objective = objectiveExpression ? model.newExpression("the objective: [ x * y ] <= End").weight(1) : null;
        Expression squares = quadratic && !objectiveExpression ? model.newExpression("squares").weight(1) : objective;

        for (int i = 0; i < n; i++) {
            int coefficient = i % 5 - 2;
            if (objectiveExpression) {
                objective.set(i, coefficient);
            } else {
                model.getVariable(i).weight(coefficient);
            }
            if (quadratic) {
                squares.set(i, i, 1);
            }
        }

        return model;
    }

    /**
     * A small model with names drawn at random from characters and words that the formats do not all have.
     */
    private static ExpressionsBasedModel newModelWithRandomNames(final long seed) {

        String[] parts = { "a", "Z", "_", ".", "0", "9", " ", "\t", "\n", "\r", "+", "-", "*", "/", "^", "[", "]", "(", ")", ":", "<", "=", ">", ",", ";",
                "#", "$", "'", "\"", "\\", "!", "@", "%", "&", "{", "}", "|", "~", "é", "日", " ", "end", "inf", "free", "st", "MARKER", "rhs", "obj" };

        Random random = new Random(seed);

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        int n = 6;
        for (int i = 0; i < n; i++) {
            model.newVariable(FileFormatTranslationTest.randomName(random, parts)).lower(-5).upper(5).weight(random.nextInt(7) - 3);
        }

        Set<String> named = new HashSet<>();
        for (int c = 0; c < 4; c++) {
            String name = FileFormatTranslationTest.randomName(random, parts);
            while (!named.add(name)) {
                name += "_";
            }
            Expression constraint = model.newExpression(name);
            for (int i = 0; i < n; i++) {
                constraint.set(i, random.nextInt(5) - 2);
            }
            constraint.lower(-8).upper(8);
        }

        return model;
    }

    private static String randomName(final Random random, final String[] parts) {
        StringBuilder retVal = new StringBuilder();
        for (int length = random.nextInt(5); length > 0; length--) {
            retVal.append(parts[random.nextInt(parts.length)]);
        }
        return retVal.toString();
    }

    private static ExpressionsBasedModel read(final String path, final String text, final FileFormat format) {
        try {
            return ExpressionsBasedModel.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), format);
        } catch (RuntimeException cause) {
            String shown = text.length() > 2_000 ? text.substring(0, 2_000) + "…" : text;
            return TestUtils.fail(path + ": not read as " + format + " – " + cause.getMessage() + "\n" + shown, cause);
        }
    }

    /**
     * As the Optimisation Service writes it: to an in-memory file, the format given by its name.
     */
    private static String write(final ExpressionsBasedModel model, final FileFormat format) {

        InMemoryFile file = new InMemoryFile().name("model." + format.name().toLowerCase(Locale.ROOT));
        model.writeTo(file);

        return new String(file.getContentsAsByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    void testFlatZinc() {
        FileFormatTranslationTest.assertTranslations(FLAT_ZINC);
    }

    /**
     * FlatZinc is read, never written.
     */
    @Test
    void testFlatZincNotWritten() {

        ExpressionsBasedModel model = FileFormatTranslationTest.read("FZN as given", FLAT_ZINC.text(), FileFormat.FZN);

        TestUtils.assertThrows(UnsupportedOperationException.class, () -> FileFormatTranslationTest.write(model, FileFormat.FZN));
    }

    /**
     * Names, as read from FlatZinc, that the LP format does not have.
     */
    @Test
    void testFlatZincWithHoles() {
        FileFormatTranslationTest.assertTranslations(FLAT_ZINC_WITH_HOLES);
    }

    @Test
    void testMixedInteger() {
        FileFormatTranslationTest.assertTranslations(MILP);
    }

    @Test
    void testMixedIntegerWithRange() {
        FileFormatTranslationTest.assertTranslations(MILP_WITH_RANGE);
    }

    /**
     * Names that no format has as they are – for the variables, the constraints and the objective function –
     * and with the objective function its own named expression, or variables' weights and a constraint named
     * obj.
     */
    @Test
    void testNamesAnyFormatDoesNotHave() {
        FileFormatTranslationTest.assertWritten(() -> FileFormatTranslationTest.newModelWithNames(true, false), true);
        FileFormatTranslationTest.assertWritten(() -> FileFormatTranslationTest.newModelWithNames(false, false), true);
    }

    /**
     * As {@link #testNamesAnyFormatDoesNotHave()}, with a quadratic objective function.
     */
    @Test
    void testNamesAnyFormatDoesNotHaveQuadratic() {
        FileFormatTranslationTest.assertWritten(() -> FileFormatTranslationTest.newModelWithNames(true, true), false);
        FileFormatTranslationTest.assertWritten(() -> FileFormatTranslationTest.newModelWithNames(false, true), false);
    }

    /**
     * Names, as read from MPS, that the LP format does not have.
     */
    @Test
    void testNamesLPDoesNotHave() {
        FileFormatTranslationTest.assertTranslations(MPS_WITH_NAMES_LP_DOES_NOT_HAVE);
    }

    /**
     * Small models with names drawn at random – the same each time the test runs.
     */
    @Test
    void testNamesRandom() {
        for (long seed = 1L; seed <= 100L; seed++) {
            long given = seed;
            FileFormatTranslationTest.assertWritten(() -> FileFormatTranslationTest.newModelWithRandomNames(given), true);
        }
    }

    @Test
    void testQuadratic() {
        FileFormatTranslationTest.assertTranslations(QP);
    }

}
