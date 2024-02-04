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
package org.ojalgo.type;

import org.ojalgo.function.constant.PrimitiveMath;

/**
 * @author apete
 */
public class ColourData {

    public static final ColourData BLACK = new ColourData(0, 0, 0);
    public static final ColourData WHITE = new ColourData(255, 255, 255);

    private static final int LIMIT = 256;

    public static ColourData random() {

        final int tmpR = (int) PrimitiveMath.FLOOR.invoke(LIMIT * Math.random());
        final int tmpG = (int) PrimitiveMath.FLOOR.invoke(LIMIT * Math.random());
        final int tmpB = (int) PrimitiveMath.FLOOR.invoke(LIMIT * Math.random());

        return new ColourData(tmpR, tmpG, tmpB);
    }

    public static ColourData valueOf(final String colourAsHexString) {
        final int i = Integer.decode(colourAsHexString).intValue();
        return new ColourData((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    private final int myValue;

    public ColourData(final float r, final float g, final float b) {
        this((int) ((r * 255F) + 0.5F), (int) ((g * 255F) + 0.5F), (int) ((b * 255F) + 0.5F));
    }

    public ColourData(final float r, final float g, final float b, final float a) {
        this((int) ((r * 255F) + 0.5F), (int) ((g * 255F) + 0.5F), (int) ((b * 255F) + 0.5F), (int) ((a * 255F) + 0.5F));
    }

    public ColourData(final int rgb) {
        myValue = 0xff000000 | rgb;
    }

    public ColourData(final int rgba, final boolean alpha) {
        if (alpha) {
            myValue = rgba;
        } else {
            myValue = 0xff000000 | rgba;
        }
    }

    public ColourData(final int r, final int g, final int b) {
        this(r, g, b, 255);
    }

    public ColourData(final int r, final int g, final int b, final int a) {
        myValue = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
    }

    public int getAlpha() {
        return (myValue >> 24) & 0xff;
    }

    public int getBlue() {
        return (myValue >> 0) & 0xFF;
    }

    public int getGreen() {
        return (myValue >> 8) & 0xFF;
    }

    public int getRed() {
        return (myValue >> 16) & 0xFF;
    }

    public int getRGB() {
        return myValue;
    }

    public String toHexString() {
        return TypeUtils.toHexString(myValue);
    }

}
