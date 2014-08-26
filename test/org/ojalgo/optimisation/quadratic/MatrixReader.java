package org.ojalgo.optimisation.quadratic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;

class MatrixReader {

    public static MatrixStore<Double> readMatrix(final String fileName) {
        PrimitiveDenseStore matrix = null;
        try {
            //
            // read rowDim, colDim
            //
            int rowDim = 0;
            int colDim = 0;
            final File file = new File(fileName);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            while (line != null) {
                int fileLineColumns = 0;
                final String words[] = line.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    final String valueStr = words[i];
                    if ((valueStr != null) && (valueStr.length() > 0)) {
                        fileLineColumns++;
                    }
                }
                if (fileLineColumns > colDim) {
                    colDim = fileLineColumns;
                }
                if (fileLineColumns > 0) {
                    rowDim++;
                }
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            //
            // read matrix
            //
            bufferedReader = new BufferedReader(new FileReader(file));
            matrix = PrimitiveDenseStore.FACTORY.makeZero(rowDim, colDim);
            int row = 0;
            line = bufferedReader.readLine();
            while (line != null) {
                int col = 0;
                final String words[] = line.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    final String valueStr = words[i];
                    if ((valueStr != null) && (valueStr.length() > 0)) {
                        final Double value = Double.parseDouble(valueStr);
                        matrix.set(row, col, value);
                        col++;
                    }
                }
                if (col > 0) {
                    row++;
                }
                line = bufferedReader.readLine();
            }
        } catch (final NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return matrix;
    }

    public static MatrixStore<Double> readMatrix(final String s, final int rowDim, final int colDim) {
        PrimitiveDenseStore matrix = null;
        try {
            final BufferedReader reader = new BufferedReader(new StringReader(s));
            matrix = PrimitiveDenseStore.FACTORY.makeZero(rowDim, colDim);
            final char[] cbuf = new char[100];
            boolean done = false;
            int numberCount = 0;
            for (int row = 0; row < rowDim; ++row) {
                if (done) {
                    break;
                }
                for (int col = 0; col < colDim; ++col) {
                    reader.mark(100);
                    Arrays.fill(cbuf, ' ');
                    int count = reader.read(cbuf, 0, 100);
                    int len = -1;
                    for (int i = 0; i < count; ++i) {
                        if (!MatrixReader.isNumberChar(cbuf[i])) {
                            len = i;
                            break;
                        } else if (i == (count - 1)) {
                            len = count;
                            break;
                        }
                    }
                    reader.reset();
                    if (len >= 0) {
                        Arrays.fill(cbuf, ' ');
                        reader.read(cbuf, 0, len);
                        final String temp = String.copyValueOf(cbuf, 0, len);
                        final Double d = Double.parseDouble(temp);
                        matrix.set(row, col, d);
                        numberCount++;
                    } else {
                        done = true;
                        break;
                    }
                    reader.mark(100);
                    Arrays.fill(cbuf, ' ');
                    count = reader.read(cbuf, 0, 100);
                    len = -1;
                    for (int i = 0; i < count; ++i) {
                        if (MatrixReader.isNumberChar(cbuf[i])) {
                            len = i;
                            break;
                        }
                    }
                    reader.reset();
                    if (len >= 0) {
                        Arrays.fill(cbuf, ' ');
                        reader.read(cbuf, 0, len);
                    }
                }
            }
            //   System.out.println("Number count: " + numberCount);
        } catch (final NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return matrix;
    }

    private static boolean isNumberChar(final char ch) {
        return Character.isDigit(ch) || (ch == '+') || (ch == '-') || (ch == '.') || (ch == 'E') || (ch == 'e');
    }

}
