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
package org.ojalgo.netio;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.ojalgo.array.ArrayAnyD;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive32Array;
import org.ojalgo.structure.Access2D;

/**
 * Reads IDX-files as described at <a href="http://yann.lecun.com/exdb/mnist/">THE MNIST DATABASE</a> page.
 * The elements/pixels/bytes are written to the ArrayAnyD instance in order as they're read from the file. The
 * indexing order then needs to be reversed, and that causes the images to be transposed.
 */
public abstract class IDX {

    public static ArrayAnyD<Double> parse(String filePath) {
        return IDX.parse(filePath, Primitive32Array.FACTORY);
    }

    public static ArrayAnyD<Double> parse(String filePath, DenseArray.Factory<Double> arrayFactory) {

        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filePath))))) {

            input.read();
            input.read();
            int type = input.read();
            int rank = input.read();

            long[] structure = new long[rank];
            for (int i = 0; i < rank; i++) {
                structure[rank - 1 - i] = input.readInt();
            }

            ArrayAnyD<Double> data = ArrayAnyD.factory(arrayFactory).makeZero(structure);

            for (long i = 0, limit = data.count(); i < limit; i++) {
                switch (type) {
                case 0x08:
                    data.set(i, input.readUnsignedByte());
                    break;
                case 0x09:
                    data.set(i, input.readByte());
                    break;
                case 0x0B:
                    data.set(i, input.readShort());
                    break;
                case 0x0C8:
                    data.set(i, input.readInt());
                    break;
                case 0x0D:
                    data.set(i, input.readFloat());
                    break;
                case 0x0E:
                    data.set(i, input.readDouble());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown element type!");
                }
            }

            return data;

        } catch (IOException exception) {

            return null;
        }
    }

    /**
     * Taking into account that the images are transposed, and assumes the values are between 0 and 255.
     */
    public static void print(Access2D<?> image, BasicLogger.Printer printer) {
        IDX.print(image, printer, true, 255D);
    }

    public static void print(Access2D<?> image, BasicLogger.Printer printer, boolean transpose, double maxValue) {

        double oneThird = maxValue / 3D;
        double twoThirds = (2D * maxValue) / 3D;

        long numbRows = image.countRows();
        long numbCols = image.countColumns();

        if (transpose) {
            for (long j = 0L; j < numbCols; j++) {
                for (long i = 0L; i < numbRows; i++) {
                    IDX.printPixel(image.doubleValue(i, j), printer, oneThird, twoThirds);
                }
                printer.println();
            }
        } else {
            for (long i = 0L; i < numbRows; i++) {
                for (long j = 0L; j < numbCols; j++) {
                    IDX.printPixel(image.doubleValue(i, j), printer, oneThird, twoThirds);
                }
                printer.println();
            }
        }
    }

    private static void printPixel(double gray, BasicLogger.Printer printer, double oneThird, double twoThirds) {
        if (gray < oneThird) {
            printer.print(" ");
        } else if (gray < twoThirds) {
            printer.print("+");
        } else {
            printer.print("X");
        }
    }

}
