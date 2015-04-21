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

public enum SQL {

    /**
     * <ul>
     * <li>BIGINT</li>
     * </ul>
     */
    BIGINT(java.lang.Long.class),

    /**
     * <ul>
     * <li>VARBINARY</li>
     * <li>LONGVARBINARY</li>
     * <li>BINARY</li>
     * <li>LONG BINARY</li>
     * <li>IMAGE</li>
     * <li>UNIQUEIDENTIFIER</li>
     * </ul>
     */
    BINARY(byte[].class),

    /**
     * <ul>
     * <li>BIT</li>
     * </ul>
     */
    BIT(java.lang.Boolean.class),

    /**
     * <ul>
     * <li>VARCHAR</li>
     * <li>LONGVARCHAR</li>
     * <li>CHARACTER</li>
     * <li>CHAR</li>
     * <li>TEXT</li>
     * <li>UNIQUEIDENTIFIERSTR</li>
     * </ul>
     */
    CHARACTER(java.lang.String.class),

    /**
     * <ul>
     * <li>DATE</li>
     * </ul>
     */
    DATE(java.sql.Date.class),

    /**
     * <ul>
     * <li>DECIMAL</li>
     * <li>NUMERIC</li>
     * <li>MONEY</li>
     * <li>SMALLMONEY</li>
     * </ul>
     */
    DECIMAL(java.math.BigDecimal.class),

    /**
     * <ul>
     * <li>DOUBLE</li>
     * <li>FLOAT</li>
     * <li>DOUBLE PRECISION</li>
     * </ul>
     */
    DOUBLE(java.lang.Double.class),

    /**
     * <ul>
     * <li>INTEGER</li>
     * <li>INT</li>
     * </ul>
     */
    INTEGER(java.lang.Integer.class),

    /**
     * <ul>
     * <li>REAL</li>
     * </ul>
     */
    REAL(java.lang.Float.class),

    /**
     * <ul>
     * <li>SMALLINT</li>
     * </ul>
     */
    SMALLINT(java.lang.Short.class),

    /**
     * <ul>
     * <li>TIME</li>
     * </ul>
     */
    TIME(java.sql.Time.class),

    /**
     * <ul>
     * <li>TIMESTAMP</li>
     * <li>DATETIME</li>
     * <li>SMALLDATETIME</li>
     * </ul>
     */
    TIMESTAMP(java.sql.Timestamp.class),

    /**
     * <ul>
     * <li>TINYINT</li>
     * </ul>
     */
    TINYINT(java.lang.Byte.class);

    private final Class<?> myJavaClass;

    SQL(Class<?> aJavaClass) {
        myJavaClass = aJavaClass;
    }

    public final Class<?> getJavaClass() {
        return myJavaClass;
    }

}
