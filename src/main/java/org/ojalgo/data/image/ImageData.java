/*
 * Copyright 1997-2023 Optimatika
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
package org.ojalgo.data.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.ojalgo.function.constant.PrimitiveMath;
import org.ojalgo.function.special.MissingMath;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore.Factory;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.structure.Access2D;
import org.ojalgo.structure.Mutate2D;
import org.ojalgo.structure.Structure2D;
import org.ojalgo.type.NumberDefinition;

/**
 * Treats an image as a matrix. (Wraps a {@link BufferedImage} and implements {@link MatrixStore}.)
 * <p>
 * By default this wrapper treats the underlying image as a grey scale image (with no alpha).
 * <ol>
 * <li>If the underlying image actually is a colour image, then {@link #convertToGreyScale()} usually improves
 * the grey scale image quality (visual appearance).
 * <li>By using {@link #sliceRedChannel()}, {@link #sliceGreenChannel()}, {@link #sliceBlueChannel()} and
 * {@link #sliceAlphaChannel()} you access the individual colour channels separately. Then, instead of 1 grey
 * scale image, you have 3 (or 4) colour channel images. Even if they're all backed by the same image,
 * manipulating one channel does not alter the others.
 * <li>The numbers you get/set are always (enforced to be) in the range [0,255]. If it's a grey scale image,
 * it has 256 shades of grey. Each of the sliced colour channels have 256 shades of their respective colour.
 * </ol>
 */
public class ImageData implements MatrixStore<Double>, Mutate2D.Fillable<Double> {

    static final class SingleChannel extends ImageData {

        private final int myMask;
        private final int myShift;

        SingleChannel(final BufferedImage image, final int mask, final int shift) {
            super(image);
            myShift = shift;
            myMask = mask;
        }

        @Override
        public int intValue(final int row, final int col) {
            int argb = this.getARGB(row, col);
            return (argb & myMask) >>> myShift;
        }

        @Override
        public void set(final int row, final int col, final int value) {

            int ranged = ImageData.toRanged(value);

            int oldOtherChannels = this.getARGB(row, col) & ~myMask;
            int newThisChannel = ranged << myShift;

            int argb = oldOtherChannels ^ newThisChannel;

            this.setARGB(row, col, argb);
        }

    }

    static final int MASK_ALPHA = 0xFF000000;
    static final int MASK_BLUE = 0xFF;
    static final int MASK_GREEN = 0xFF00;
    static final int MASK_RED = 0xFF0000;
    static final int SHIFT_ALPHA = 24;
    static final int SHIFT_BLUE = 0;
    static final int SHIFT_GREEN = 8;
    static final int SHIFT_RED = 16;

    public static ImageData copy(final Access2D<?> values) {
        ImageData retVal = ImageData.newGreyScale(values);
        retVal.fillMatching(values);
        return retVal;
    }

    public static ImageData newColour(final int nbRows, final int nbCols) {
        return new ImageData(new BufferedImage(nbCols, nbRows, BufferedImage.TYPE_INT_ARGB));
    }

    public static ImageData newColour(final Structure2D shape) {
        return ImageData.newColour(shape.getRowDim(), shape.getColDim());
    }

    public static ImageData newGreyScale(final int nbRows, final int nbCols) {
        return new ImageData(new BufferedImage(nbCols, nbRows, BufferedImage.TYPE_BYTE_GRAY));
    }

    public static ImageData newGreyScale(final Structure2D shape) {
        return ImageData.newGreyScale(shape.getRowDim(), shape.getColDim());
    }

    public static ImageData read(final File file) {
        try {
            return new ImageData(ImageIO.read(file));
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    public static ImageData wrap(final BufferedImage image) {
        return new ImageData(image);
    }

    static int toRanged(final int value) {
        return Math.max(0, Math.min(value, 255));
    }

    private final BufferedImage myImage;

    ImageData(final BufferedImage image) {
        super();
        myImage = image;
    }

    @Override
    public byte byteValue(final int row, final int col) {
        return (byte) this.intValue(row, col);
    }

    /**
     * {@link BufferedImage#TYPE_BYTE_GRAY}, {@link BufferedImage#TYPE_INT_ARGB} or any other valid type...
     */
    public ImageData convertTo(final int imageType) {
        BufferedImage image = new BufferedImage(myImage.getWidth(), myImage.getHeight(), imageType);
        Graphics graphics = image.getGraphics();
        graphics.drawImage(myImage, 0, 0, null);
        graphics.dispose();
        return new ImageData(image);
    }

    /**
     * @see #convertTo(int)
     * @see BufferedImage#TYPE_BYTE_GRAY
     */
    public ImageData convertToGreyScale() {
        return this.convertTo(BufferedImage.TYPE_BYTE_GRAY);
    }

    @Override
    public long countColumns() {
        return this.getColDim();
    }

    @Override
    public long countRows() {
        return this.getRowDim();
    }

    @Override
    public double doubleValue(final int row, final int col) {
        return this.intValue(row, col);
    }

    public void fillMatching(final Access2D<?> values) {

        int nbRows = Math.min(this.getRowDim(), values.getRowDim());
        int nbCols = Math.min(this.getColDim(), values.getColDim());

        for (int j = 0; j < nbCols; j++) {
            for (int i = 0; i < nbRows; i++) {
                this.set(i, j, values.intValue(i, j));
            }
        }
    }

    @Override
    public float floatValue(final int row, final int col) {
        return this.intValue(row, col);
    }

    @Override
    public Double get(final int row, final int col) {
        return Double.valueOf(this.intValue(row, col));
    }

    @Override
    public int getColDim() {
        return myImage.getWidth();
    }

    @Override
    public int getRowDim() {
        return myImage.getHeight();
    }

    @Override
    public int intValue(final int row, final int col) {

        int argb = this.getARGB(row, col);

        int b = argb & MASK_BLUE;
        int g = (argb & MASK_GREEN) >>> SHIFT_GREEN;
        int r = (argb & MASK_RED) >>> SHIFT_RED;

        return (r + r + g + g + g + g + b) / 7;
    }

    @Override
    public Factory<Double, ?> physical() {
        return Primitive32Store.FACTORY;
    }

    /**
     * Will create a new image - the largest possible, with the same aspect ratio, that fits within the
     * specified number of rows/columns (pixel height and width). Works well for both down-sampling and
     * moderate up-sampling.
     */
    public ImageData resample(final int nbRows, final int nbCols) {

        double oldRowDim = this.getRowDim();
        double oldColDim = this.getColDim();

        double scale = Math.max(oldRowDim / nbRows, oldColDim / nbCols);

        int newRowDim = MissingMath.roundToInt(oldRowDim / scale);
        int newColDim = MissingMath.roundToInt(oldColDim / scale);

        ImageData retVal = ImageData.newGreyScale(newRowDim, newColDim);

        double half = scale / 2D;
        double third = scale / 3D;
        double diag = third / Math.sqrt(2D);

        double maxBaseRow = oldRowDim - third - 0.5D - oldRowDim * PrimitiveMath.MACHINE_EPSILON;
        double maxBaseCol = oldColDim - third - 0.5D - oldColDim * PrimitiveMath.MACHINE_EPSILON;

        double baseCol = half - 0.5;
        for (int j = 0; j < newColDim; j++) {
            baseCol = Math.min(baseCol, maxBaseCol);

            double baseRow = half - 0.5;
            for (int i = 0; i < newRowDim; i++) {
                baseRow = Math.min(baseRow, maxBaseRow);

                double pixel = 0D;

                pixel += this.doubleValue(baseRow - third, baseCol);
                pixel += this.doubleValue(baseRow + third, baseCol);
                pixel += this.doubleValue(baseRow, baseCol - third);
                pixel += this.doubleValue(baseRow, baseCol + third);

                pixel += this.doubleValue(baseRow - diag, baseCol - diag);
                pixel += this.doubleValue(baseRow + diag, baseCol - diag);
                pixel += this.doubleValue(baseRow - diag, baseCol + diag);
                pixel += this.doubleValue(baseRow + diag, baseCol + diag);

                pixel /= 8D;

                retVal.set(i, j, pixel);

                baseRow += scale;
            }

            baseCol += scale;
        }

        return retVal;
    }

    @Override
    public void set(final int row, final int col, final double value) {
        this.set(row, col, Math.round(value));
    }

    @Override
    public void set(final int row, final int col, final float value) {
        this.set(row, col, Math.round(value));
    }

    @Override
    public void set(final int row, final int col, final int value) {

        int ranged = ImageData.toRanged(value);

        int a = MASK_ALPHA;
        int r = ranged << SHIFT_RED;
        int g = ranged << SHIFT_GREEN;
        int b = ranged;

        int argb = a | r | g | b;

        this.setARGB(row, col, argb);
    }

    @Override
    public void set(final int row, final int col, final long value) {
        this.set(row, col, (int) value);
    }

    @Override
    public void set(final long row, final long col, final Comparable<?> value) {
        this.set(row, col, NumberDefinition.intValue(value));
    }

    public ImageData sliceAlphaChannel() {
        return new SingleChannel(myImage, MASK_ALPHA, SHIFT_ALPHA);
    }

    public ImageData sliceBlueChannel() {
        return new SingleChannel(myImage, MASK_BLUE, SHIFT_BLUE);
    }

    public ImageData sliceGreenChannel() {
        return new SingleChannel(myImage, MASK_GREEN, SHIFT_GREEN);
    }

    public ImageData sliceRedChannel() {
        return new SingleChannel(myImage, MASK_RED, SHIFT_RED);
    }

    /**
     * The file format is derived from the file name ending (png, jpg...)
     */
    public void writeTo(final File file) {
        try {
            String fileName = file.getName();
            String formatName = fileName.substring(1 + fileName.lastIndexOf("."));
            ImageIO.write(myImage, formatName, file);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private double doubleValue(final double row, final double col) {
        return this.doubleValue(MissingMath.roundToInt(row), MissingMath.roundToInt(col));
    }

    protected int getARGB(final int row, final int col) {
        return myImage.getRGB(col, row);
    }

    protected void setARGB(final int row, final int col, final int argb) {
        myImage.setRGB(col, row, argb);
    }

}
