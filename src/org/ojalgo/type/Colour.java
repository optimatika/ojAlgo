/* 
 * Copyright 1997-2015 Optimatika (www.optimatika.se)
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

import java.awt.Color;
import java.awt.color.ColorSpace;

/**
 * @author apete
 */
public class Colour extends Color {

    public static final Colour WHITE = new Colour(Color.WHITE);
    public static final Colour BLACK = new Colour(Color.BLACK);

    private static final int LIMIT = 256;

    public static Colour random() {

        final int tmpR = (int) Math.floor(LIMIT * Math.random());
        final int tmpG = (int) Math.floor(LIMIT * Math.random());
        final int tmpB = (int) Math.floor(LIMIT * Math.random());

        return new Colour(tmpR, tmpG, tmpB);
    }

    public static Colour valueOf(final String aColourAsHexString) {
        return new Colour(Color.decode(aColourAsHexString));
    }

    public Colour(final Color aColor) {
        super(aColor.getRGB());
    }

    public Colour(final Color aColor, final float a) {
        super(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), a);
    }

    public Colour(final ColorSpace cspace, final float components[], final float alpha) {
        super(cspace, components, alpha);
    }

    public Colour(final float r, final float g, final float b) {
        super(r, g, b);
    }

    public Colour(final float r, final float g, final float b, final float a) {
        super(r, g, b, a);
    }

    public Colour(final int rgb) {
        super(rgb);
    }

    public Colour(final int rgba, final boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public Colour(final int r, final int g, final int b) {
        super(r, g, b);
    }

    public Colour(final int r, final int g, final int b, final int a) {
        super(r, g, b, a);
    }

    public String toHexString() {
        return TypeUtils.toHexString(this);
    }

}
