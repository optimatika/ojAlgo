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

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;

class FileFormatFZNTest extends OptimisationTests {

    private static FileFormatFZN.Parsed parse(final String flatZinc) {
        return FileFormatFZN.parse(new ByteArrayInputStream(flatZinc.getBytes(StandardCharsets.UTF_8)), ExpressionsBasedModel::new);
    }

    private static String solution(final FileFormatFZN.Parsed parsed, final Optimisation.Result result) {
        StringBuilder retVal = new StringBuilder();
        parsed.printSolution(result, retVal);
        return retVal.toString();
    }

    private static Optimisation.Result solve(final FileFormatFZN.Parsed parsed) {
        ExpressionsBasedModel model = parsed.getModel();
        return parsed.getSense() == Optimisation.Sense.MAX ? model.maximise() : model.minimise();
    }

    /**
     * A variable, an array of variables and constants, and a variable declared again under another name.
     */
    @Test
    void testAliasesAndConstants() {

        String flatZinc = """
                var 0..10: y;
                var int: x :: output_var = y;
                array [1..3] of var int: a :: output_array([1..3]) = [x, 3, y];
                constraint int_lin_eq([1, 1], [x, 3], 7);
                solve satisfy;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isFeasible());
        TestUtils.assertEquals("x = 4;\na = array1d(1..3,[4,3,4]);\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * At least one of a, b true, and c false or d true – minimise the number of true ones.
     */
    @Test
    void testBoolClause() {

        String flatZinc = """
                var bool: a;
                var bool: b;
                var bool: c;
                var bool: d;
                var 0..4: total :: output_var;
                constraint bool_clause([a, b], []);
                constraint bool_clause([d], [c]);
                constraint bool_eq(c, true);
                constraint int_lin_eq([1, 1, 1, 1, -1], [a, b, c, d, total], 0);
                solve minimize total;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals("total = 3;\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * The bool predicates are exactly linear over binary variables – each forces the values here.
     */
    @Test
    void testBoolPredicates() {

        String flatZinc = """
                var bool: a :: output_var;
                var bool: b :: output_var;
                var bool: c :: output_var;
                var bool: d :: output_var;
                var bool: r;
                var 0..4: total;
                constraint array_bool_or([a, b], r);
                constraint bool_eq(r, false);
                constraint array_bool_and([c, d], true);
                constraint int_lin_eq([1, 1, 1, 1, -1], [a, b, c, d, total], 0);
                solve maximize total;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(2.0, result.getValue(), 1E-9);
        TestUtils.assertEquals("a = false;\nb = false;\nc = true;\nd = true;\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * A constraint that is only constants, and not satisfied, makes the model infeasible.
     */
    @Test
    void testConstantConstraintNotSatisfied() {

        String flatZinc = """
                var 0..5: x :: output_var;
                constraint int_le(5, 3);
                solve minimize x;
                """;

        Optimisation.Result result = FileFormatFZNTest.solve(FileFormatFZNTest.parse(flatZinc));

        TestUtils.assertFalse(result.getState().isFeasible());
    }

    /**
     * An empty domain, a constant outside its domain, or a domain disjoint from what the variable already
     * has, makes the model infeasible – it is not an error, and not ignored.
     */
    @Test
    void testDomainNotSatisfied() {

        String[] flatZincs = { """
                var 1..0: x;
                solve satisfy;
                """, """
                var 1..5: y :: output_var = 7;
                solve satisfy;
                """, """
                var 0.0..1.0: f :: output_var = 1.5;
                solve satisfy;
                """, """
                var int: x;
                array [1..2] of var 0..3: a :: output_array([1..2]) = [x, 7];
                solve satisfy;
                """, """
                var 0..5: x;
                constraint set_in(x, {});
                solve satisfy;
                """, """
                var 1..3: x;
                constraint set_in(x, 5..7);
                solve satisfy;
                """ };

        for (String flatZinc : flatZincs) {
            Optimisation.Result result = FileFormatFZNTest.solve(FileFormatFZNTest.parse(flatZinc));
            TestUtils.assertEquals(flatZinc, Optimisation.State.INFEASIBLE, result.getState());
        }
    }

    /**
     * The domain of an array of variables applies to its elements
     */
    @Test
    void testDomainOfArrayElements() {

        String flatZinc = """
                var int: x :: output_var;
                var int: y;
                var int: z :: output_var;
                array [1..3] of var {0..3, 8}: a = [x, y, z];
                constraint int_lin_le([1, 1], [x, y], 5);
                constraint int_le(5, z);
                solve maximize x;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals("x = 3;\nz = 8;\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * {1,4,9} is not the same as 1..9
     */
    @Test
    void testDomainWithHoles() {

        String flatZinc = """
                var {1, 4, 9}: x :: output_var;
                constraint int_le(5, x);
                solve minimize x;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals("x = 9;\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * {1..3, 7..9} is not the same as 1..9
     */
    @Test
    void testDomainWithRanges() {

        String flatZinc = """
                var {1..3, 7..9}: x :: output_var;
                var -5..5: y :: output_var;
                constraint int_le(4, x);
                constraint set_in(y, {-5, -2..-1, 4});
                constraint int_lin_le([-1], [y], -2);
                solve minimize x;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals("x = 7;\ny = 4;\n", FileFormatFZNTest.solution(parsed, result));
    }

    /**
     * Parsed as any other file format, the model is all there is
     */
    @Test
    void testFileFormat() {

        String flatZinc = """
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
                """;

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(flatZinc.getBytes(StandardCharsets.UTF_8)), FileFormat.FZN);

        TestUtils.assertEquals(Optimisation.Sense.MAX, model.getOptimisationSense());

        Optimisation.Result result = model.maximise();

        TestUtils.assertTrue(result.getState().isOptimal());
        TestUtils.assertEquals(9.0, result.getValue(), 1E-9);
    }

    @Test
    void testFileFormatDetected() {
        TestUtils.assertEquals(FileFormat.FZN, FileFormat.detect("model.fzn"));
    }

    /**
     * output_array with two dimensions, bools as true/false and floats as floats
     */
    @Test
    void testOutputFormat() {

        String flatZinc = """
                var bool: b1;
                var bool: b2;
                var 0.0..10.0: f :: output_var;
                array [1..4] of var bool: grid :: output_array([1..2, 0..1]) = [b1, true, b2, false];
                constraint bool_not(b1, b2);
                constraint float_lin_eq([2.0], [f], 5.0);
                constraint bool_eq(b1, true);
                solve satisfy;
                """;

        FileFormatFZN.Parsed parsed = FileFormatFZNTest.parse(flatZinc);
        Optimisation.Result result = FileFormatFZNTest.solve(parsed);

        TestUtils.assertTrue(result.getState().isFeasible());
        TestUtils.assertEquals("f = 2.5;\ngrid = array2d(1..2,0..1,[true,true,false,false]);\n", FileFormatFZNTest.solution(parsed, result));
    }

    @Test
    void testSetVariableNotSupported() {

        String flatZinc = """
                var set of 1..3: s;
                solve satisfy;
                """;

        IllegalArgumentException thrown = TestUtils.assertThrows(IllegalArgumentException.class, () -> FileFormatFZNTest.parse(flatZinc));

        TestUtils.assertTrue(thrown.getMessage(), thrown.getMessage().startsWith("Line 1:"));
    }

    /**
     * A constraint that is not linear is an error with the number of its line – never skipped.
     */
    @Test
    void testUnsupportedConstraint() {

        String flatZinc = """
                var 0..5: x;
                var 0..5: y;
                var 0..25: z;

                constraint int_times(x, y, z);
                solve satisfy;
                """;

        IllegalArgumentException thrown = TestUtils.assertThrows(IllegalArgumentException.class, () -> FileFormatFZNTest.parse(flatZinc));

        TestUtils.assertTrue(thrown.getMessage(), thrown.getMessage().startsWith("Line 5: 'int_times'"));
    }

}
