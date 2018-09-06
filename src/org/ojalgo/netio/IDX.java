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

    private static final double ZERO = 0D;
    private static final double ONE_THIRD = 256D / 3D;
    private static final double TWO_THIRDS = 512D / 3D;

    public static ArrayAnyD<Double> parse(File file) {
        return IDX.parse(file, Primitive32Array.FACTORY);
    }

    public static ArrayAnyD<Double> parse(File file, DenseArray.Factory<Double> factory) {

        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            input.read();
            input.read();
            int type = input.read();
            int rank = input.read();

            long[] structure = new long[rank];
            for (int i = 0; i < rank; i++) {
                structure[rank - 1 - i] = input.readInt();
            }

            ArrayAnyD<Double> data = ArrayAnyD.factory(factory).makeZero(structure);

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
     * Taking into account that the images are transposed
     */
    public static void print(Access2D<?> image, BasicLogger.Printer printer) {
        for (int col = 0; col < image.countColumns(); col++) {
            for (int row = 0; row < image.countRows(); row++) {
                double gray = image.doubleValue(row, col);
                if (gray == ZERO) {
                    printer.print(" ");
                } else if (gray < ONE_THIRD) {
                    printer.print("~");
                } else if (gray < TWO_THIRDS) {
                    printer.print("+");
                } else {
                    printer.print("X");
                }
            }
            printer.println();
        }
    }

}
