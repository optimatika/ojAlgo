/*
 * Copyright 1997-2024 Optimatika
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.Map.Entry;

import org.ojalgo.netio.ASCII;
import org.ojalgo.structure.Structure1D.IntIndex;
import org.ojalgo.structure.Structure2D.IntRowColumn;

abstract class FileFormatEBM {

    private static final String TAB = String.valueOf(ASCII.HT);

    private static Expression readExpression(final ExpressionsBasedModel model, final String[] fields) {

        Expression expression = model.newExpression(fields[1]);

        FileFormatEBM.readModelEntity(expression, fields);

        return expression;
    }

    private static void readLinear(final Expression current, final String[] fields) {

        int index = Integer.parseInt(fields[1]);

        BigDecimal value = new BigDecimal(fields[2]);

        current.set(index, value);

    }

    private static void readModelEntity(final ModelEntity<?> entity, final String[] fields) {

        if (fields.length > 2 && fields[2].length() > 0) {
            entity.lower(new BigDecimal(fields[2]));
        }

        if (fields.length > 3 && fields[3].length() > 0) {
            entity.upper(new BigDecimal(fields[3]));
        }

        if (fields.length > 4 && fields[4].length() > 0) {
            entity.weight(new BigDecimal(fields[4]));
        }
    }

    private static void readQuadratic(final Expression current, final String[] fields) {

        int row = Integer.parseInt(fields[1]);

        int col = Integer.parseInt(fields[2]);

        BigDecimal value = new BigDecimal(fields[3]);

        current.set(row, col, value);

    }

    private static void readVariable(final ExpressionsBasedModel model, final String[] fields) {

        Variable variable = model.newVariable(fields[1]);

        FileFormatEBM.readModelEntity(variable, fields);

        variable.integer(Boolean.parseBoolean(fields[5]));

        if (fields.length > 6 && fields[6].length() > 0) {
            variable.setValue(new BigDecimal(fields[6]));
        }
    }

    private static void writeExpression(final Expression expression, final BufferedWriter writer) throws IOException {
        writer.write("E");
        FileFormatEBM.writeModelEntity(expression, writer);
        writer.newLine();
    }

    private static void writeLinear(final Entry<IntIndex, BigDecimal> entry, final BufferedWriter writer) throws IOException {
        writer.write("L");
        writer.write(ASCII.HT);
        writer.write(Integer.toString(entry.getKey().index));
        writer.write(ASCII.HT);
        writer.write(entry.getValue().toPlainString());
        writer.newLine();
    }

    private static void writeModelEntity(final ModelEntity<?> entity, final BufferedWriter writer) throws IOException {

        String name = entity.getName().replace(ASCII.HT, ASCII.NBSP);
        BigDecimal lower = entity.getLowerLimit();
        BigDecimal upper = entity.getUpperLimit();
        BigDecimal weight = entity.getContributionWeight();

        writer.write(ASCII.HT);
        writer.write(name);

        writer.write(ASCII.HT);
        if (lower != null) {
            writer.write(lower.toPlainString());
        }

        writer.write(ASCII.HT);
        if (upper != null) {
            writer.write(upper.toPlainString());
        }

        writer.write(ASCII.HT);
        if (weight != null) {
            writer.write(weight.toPlainString());
        }
    }

    private static void writeQuadratic(final Entry<IntRowColumn, BigDecimal> entry, final BufferedWriter writer) throws IOException {
        writer.write("Q");
        writer.write(ASCII.HT);
        writer.write(Integer.toString(entry.getKey().row));
        writer.write(ASCII.HT);
        writer.write(Integer.toString(entry.getKey().column));
        writer.write(ASCII.HT);
        writer.write(entry.getValue().toPlainString());
        writer.newLine();
    }

    private static void writeVariable(final Variable variable, final BufferedWriter writer) throws IOException {
        writer.write("V");
        FileFormatEBM.writeModelEntity(variable, writer);
        writer.write(ASCII.HT);
        writer.write(Boolean.toString(variable.isInteger()));
        writer.write(ASCII.HT);
        BigDecimal value = variable.getValue();
        if (value != null) {
            writer.write(value.toPlainString());
        }
        writer.newLine();
    }

    static ExpressionsBasedModel read(final InputStream input) {

        ExpressionsBasedModel retVal = new ExpressionsBasedModel();

        Expression current = null;

        String line;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            while ((line = reader.readLine()) != null) {

                String[] fields = line.split(TAB);

                switch (line.charAt(0)) {
                case 'V':
                    FileFormatEBM.readVariable(retVal, fields);
                    break;
                case 'E':
                    current = FileFormatEBM.readExpression(retVal, fields);
                    break;
                case 'L':
                    if (current == null) {
                        throw new IllegalStateException();
                    }
                    FileFormatEBM.readLinear(current, fields);
                    break;
                case 'Q':
                    if (current == null) {
                        throw new IllegalStateException();
                    }
                    FileFormatEBM.readQuadratic(current, fields);
                    break;
                default:
                    throw new IllegalStateException();
                }
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

        return retVal;
    }

    static void write(final ExpressionsBasedModel model, final OutputStream output) {

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output))) {

            for (Variable variable : model.getVariables()) {
                FileFormatEBM.writeVariable(variable, writer);
            }

            for (Expression expression : model.getExpressions()) {
                FileFormatEBM.writeExpression(expression, writer);
                for (Entry<IntIndex, BigDecimal> entry : expression.getLinearEntrySet()) {
                    FileFormatEBM.writeLinear(entry, writer);
                }
                for (Entry<IntRowColumn, BigDecimal> entry : expression.getQuadraticEntrySet()) {
                    FileFormatEBM.writeQuadratic(entry, writer);
                }
            }

        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

    }

    private FileFormatEBM() {
        super();
    }

}
