/*
 * Copyright 1997-2025 Optimatika
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
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;
import org.ojalgo.TestUtils;
import org.ojalgo.optimisation.ExpressionsBasedModel.FileFormat;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.type.context.NumberContext;

/**
 * Round-trip tests for MPS and LP file formats.
 */
class FileFormatTest extends OptimisationTests {

    private static final NumberContext ACCURACY = NumberContext.of(7, 8);

    private static void assertRoundTrip(final ExpressionsBasedModel original, final FileFormat format) {

        Result minResult = original.minimise();
        Result maxResult = original.maximise();

        byte[] bytes = FileFormatTest.writeToBytes(original, format);
        ExpressionsBasedModel parsed = ExpressionsBasedModel.parse(new ByteArrayInputStream(bytes), format);

        TestUtils.assertTrue(format + " round-trip: model should validate", parsed.validate());

        Result parsedMin = parsed.minimise();
        if (minResult.getState().isOptimal()) {
            TestUtils.assertStateNotLessThanOptimal(parsedMin);
            TestUtils.assertEquals(format + " round-trip: min value", minResult.getValue(), parsedMin.getValue(), ACCURACY);
        }

        Result parsedMax = parsed.maximise();
        if (maxResult.getState().isOptimal()) {
            TestUtils.assertStateNotLessThanOptimal(parsedMax);
            TestUtils.assertEquals(format + " round-trip: max value", maxResult.getValue(), parsedMax.getValue(), ACCURACY);
        }
    }

    private static void assertRoundTripMin(final ExpressionsBasedModel original, final FileFormat format) {

        Result minResult = original.minimise();

        byte[] bytes = FileFormatTest.writeToBytes(original, format);
        ExpressionsBasedModel parsed = ExpressionsBasedModel.parse(new ByteArrayInputStream(bytes), format);

        TestUtils.assertTrue(format + " round-trip: model should validate", parsed.validate());

        Result parsedMin = parsed.minimise();
        if (minResult.getState().isOptimal()) {
            TestUtils.assertStateNotLessThanOptimal(parsedMin);
            TestUtils.assertEquals(format + " round-trip: min value", minResult.getValue(), parsedMin.getValue(), ACCURACY);
        }
    }

    private static byte[] writeToBytes(final ExpressionsBasedModel model, final FileFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        switch (format) {
            case MPS:
                FileFormatMPS.write(model, baos);
                break;
            case LP:
                FileFormatLP.write(model, baos);
                break;
            default:
                FileFormatEBM.write(model, baos);
                break;
        }
        return baos.toByteArray();
    }

    @Test
    void testBinaryVariables() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").binary();
        Variable y = model.newVariable("y").binary();
        Variable z = model.newVariable("z").binary();

        model.newExpression("c1").set(x, 1).set(y, 1).set(z, 1).upper(2);

        x.weight(3);
        y.weight(2);
        z.weight(1);

        model.setOptimisationSense(Optimisation.Sense.MAX);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testCrossFormatRoundTrip() {

        ExpressionsBasedModel original = new ExpressionsBasedModel();

        Variable x = original.newVariable("x").lower(0).upper(10);
        Variable y = original.newVariable("y").lower(0).upper(10);

        x.weight(2);
        y.weight(3);

        original.newExpression("c1").set(x, 1).set(y, 1).upper(8);
        original.newExpression("c2").set(x, 1).set(y, -1).level(0);

        original.setOptimisationSense(Optimisation.Sense.MIN);

        Result originalResult = original.minimise();

        // Write to MPS, read back as LP round-trip
        byte[] mpsBytes = FileFormatTest.writeToBytes(original, FileFormat.MPS);
        ExpressionsBasedModel fromMPS = ExpressionsBasedModel.parse(new ByteArrayInputStream(mpsBytes), FileFormat.MPS);

        byte[] lpBytes = FileFormatTest.writeToBytes(fromMPS, FileFormat.LP);
        ExpressionsBasedModel fromLP = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpBytes), FileFormat.LP);

        Result crossResult = fromLP.minimise();
        TestUtils.assertStateNotLessThanOptimal(crossResult);
        TestUtils.assertEquals(originalResult.getValue(), crossResult.getValue(), ACCURACY);
    }

    @Test
    void testEqualityConstraints() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y").lower(0);
        Variable z = model.newVariable("z").lower(0);

        model.newExpression("eq").set(x, 1).set(y, 1).set(z, 1).level(10);
        model.newExpression("c1").set(x, 2).set(y, 1).upper(12);

        x.weight(1);
        y.weight(2);
        z.weight(3);

        model.setOptimisationSense(Optimisation.Sense.MAX);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testFreeAndFixedVariables() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y"); // free: no bounds
        Variable z = model.newVariable("z").level(5); // fixed

        model.newExpression("c1").set(x, 1).set(y, 1).set(z, 1).lower(-10).upper(20);

        x.weight(1);
        y.weight(2);
        z.weight(1);

        model.setOptimisationSense(Optimisation.Sense.MIN);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testIntegerVariables() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10).integer(true);
        Variable y = model.newVariable("y").lower(0).upper(10).integer(true);

        model.newExpression("c1").set(x, 1).set(y, 1).upper(7);

        x.weight(5);
        y.weight(4);

        model.setOptimisationSense(Optimisation.Sense.MAX);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testLPReadBasic() {

        String lpContent = "Minimize\n" + "  obj: 2 x + 3 y\n" + "Subject To\n" + "  c1: x + y <= 10\n" + "Bounds\n" + "  0 <= x\n" + "  0 <= y\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        TestUtils.assertTrue(model.validate());

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);
    }

    @Test
    void testLPReadIntegerBinary() {

        String lpContent = "Maximize\n" + "  obj: 5 x + 4 y + 3 z\n" + "Subject To\n" + "  c1: x + y + z <= 2\n" + "Bounds\n" + "  0 <= x <= 10\n"
                + "  0 <= y <= 10\n" + "Generals\n" + "  x y\n" + "Binary\n" + "  z\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        TestUtils.assertTrue(model.validate());

        Result result = model.maximise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(10.0, result.getValue(), ACCURACY);
    }

    @Test
    void testLPReadMaximise() {

        String lpContent = "Maximize\n" + "  obj: 2 x + 3 y\n" + "Subject To\n" + "  c1: x + y <= 10\n" + "  c2: x <= 6\n" + "Bounds\n" + "  0 <= x\n"
                + "  0 <= y\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        TestUtils.assertTrue(model.validate());

        Result result = model.maximise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(30.0, result.getValue(), ACCURACY);
    }

    @Test
    void testLPReadQuadraticObjective() {

        String lpContent = "Minimize\n" + "  obj: x + y + [ 2 x ^2 + 2 x * y + 2 y ^2 ] / 2\n" + "Subject To\n" + "  c1: x + y >= 2\n" + "Bounds\n"
                + "  0 <= x\n" + "  0 <= y\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        TestUtils.assertTrue(model.validate());

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);
    }

    @Test
    void testLPReadUpperBoundOnly() {

        String lpContent = "Minimize\n" + "  obj: x + y\n" + "Subject To\n" + "  c1: x + y >= 1\n" + "Bounds\n" + "  x <= 10\n" + "  y <= 10\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        for (Variable var : model.getVariables()) {
            TestUtils.assertTrue(var.getName() + " lower should be 0", var.isLowerLimitSet());
            TestUtils.assertEquals(var.getName() + " lower", 0.0, var.getLowerLimit().doubleValue(), ACCURACY);
        }

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(1.0, result.getValue(), ACCURACY);
    }

    @Test
    void testLPReadQuadraticUpperBoundOnly() {

        String lpContent = "Minimize\n" + " obj: 0 x1 + 0 x2 + [ 2 x1 ^2 - 2 x1 * x2 + 2 x2 ^2 ] / 2\n" + "Subject To\n" + " e1: x1 + x2 = 1\n"
                + "Bounds\n" + " x1 <= 1\n" + " x2 <= 1\n" + "End\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(lpContent.getBytes()), FileFormat.LP);

        TestUtils.assertTrue(model.validate());

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.25, result.getValue(), ACCURACY);
    }

    @Test
    void testMinimisation() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y").lower(0);

        model.newExpression("c1").set(x, 2).set(y, 1).lower(4);
        model.newExpression("c2").set(x, 1).set(y, 3).lower(6);

        x.weight(3);
        y.weight(5);

        model.setOptimisationSense(Optimisation.Sense.MIN);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testMixedIntegerAndContinuous() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(100);
        Variable y = model.newVariable("y").lower(0).upper(50).integer(true);
        Variable z = model.newVariable("z").binary();

        model.newExpression("c1").set(x, 1).set(y, 2).set(z, 50).upper(100);

        x.weight(1);
        y.weight(10);
        z.weight(25);

        model.setOptimisationSense(Optimisation.Sense.MAX);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testMPSWriteReadWithExistingFiles() {

        String mpsContent = "NAME          test\n" + "ROWS\n" + " N  obj\n" + " L  c1\n" + "COLUMNS\n" + "    x         obj           2.0\n"
                + "    x         c1            1.0\n" + "    y         obj           3.0\n" + "    y         c1            1.0\n" + "RHS\n"
                + "    rhs       c1            10.0\n" + "BOUNDS\n" + "ENDATA\n";

        ExpressionsBasedModel model = ExpressionsBasedModel.parse(new ByteArrayInputStream(mpsContent.getBytes()), FileFormat.MPS);

        TestUtils.assertTrue(model.validate());

        Result result = model.minimise();
        TestUtils.assertStateNotLessThanOptimal(result);
        TestUtils.assertEquals(0.0, result.getValue(), ACCURACY);
    }

    @Test
    void testQuadraticObjective() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10);
        Variable y = model.newVariable("y").lower(0).upper(10);

        Expression qobj = model.newExpression("qobj");
        qobj.set(x, x, 2);
        qobj.set(y, y, 3);
        qobj.set(x, y, 1);
        qobj.weight(1);

        model.newExpression("c1").set(x, 1).set(y, 1).upper(8);

        model.setOptimisationSense(Optimisation.Sense.MIN);

        FileFormatTest.assertRoundTripMin(model, FileFormat.MPS);
        FileFormatTest.assertRoundTripMin(model, FileFormat.LP);
    }

    @Test
    void testQuadraticObjectiveWithLinear() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0).upper(10);
        Variable y = model.newVariable("y").lower(0).upper(10);

        x.weight(3);
        y.weight(-2);

        Expression qobj = model.newExpression("qobj");
        qobj.set(x, x, 1);
        qobj.set(y, y, 1);
        qobj.weight(1);

        model.newExpression("c1").set(x, 1).set(y, 1).lower(2);

        model.setOptimisationSense(Optimisation.Sense.MIN);

        FileFormatTest.assertRoundTripMin(model, FileFormat.MPS);
        FileFormatTest.assertRoundTripMin(model, FileFormat.LP);
    }

    @Test
    void testRangeConstraints() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y").lower(0);

        model.newExpression("range1").set(x, 1).set(y, 1).lower(2).upper(8);
        model.newExpression("eq1").set(x, 1).set(y, -1).level(0);

        x.weight(1);
        y.weight(1);

        model.setOptimisationSense(Optimisation.Sense.MIN);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

    @Test
    void testSimpleLP() {

        ExpressionsBasedModel model = new ExpressionsBasedModel();

        Variable x = model.newVariable("x").lower(0);
        Variable y = model.newVariable("y").lower(0);

        model.newExpression("c1").set(x, 1).set(y, 1).upper(10);
        model.newExpression("c2").set(x, 1).set(y, -1).upper(4);

        x.weight(2);
        y.weight(3);

        model.setOptimisationSense(Optimisation.Sense.MAX);

        FileFormatTest.assertRoundTrip(model, FileFormat.MPS);
        FileFormatTest.assertRoundTrip(model, FileFormat.LP);
    }

}
