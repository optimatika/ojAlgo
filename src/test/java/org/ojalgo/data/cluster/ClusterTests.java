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
package org.ojalgo.data.cluster;

import java.util.Collection;

abstract class ClusterTests {

    static final class Point {

        static Point mean(final Collection<Point> points) {
            double sumX = 0, sumY = 0;
            for (Point p : points) {
                sumX += p.x;
                sumY += p.y;
            }
            return new Point(sumX / points.size(), sumY / points.size());
        }

        double x, y;

        Point(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Point)) {
                return false;
            }
            Point other = (Point) obj;
            if ((Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) || (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(x);
            result = prime * result + (int) (temp ^ temp >>> 32);
            temp = Double.doubleToLongBits(y);
            result = prime * result + (int) (temp ^ temp >>> 32);
            return result;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        /**
         * Calculates the Euclidean distance between this point and another point.
         *
         * @param other The other point to which the distance is calculated.
         * @return The Euclidean distance between this point and the other point.
         */
        double distance(final Point other) {
            return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
        }

    }

    static final boolean DEBUG = true;

}
