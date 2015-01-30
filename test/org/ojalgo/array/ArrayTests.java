/*
 * Copyright 1997-2014 Optimatika (www.optimatika.se)
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
package org.ojalgo.array;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ojalgo.FunctionalityTest;

/**
 * ArrayPackageTests
 *
 * @author apete
 */
public abstract class ArrayTests extends FunctionalityTest {

    static final boolean DEBUG = false;

    public static Test suite() {
        final TestSuite suite = new TestSuite(ArrayTests.class.getPackage().getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AggregatorCardinality.class);
        suite.addTestSuite(AggregatorProduct.class);
        suite.addTestSuite(AggregatorSum.class);
        suite.addTestSuite(ArrayStructureTest.class);
        suite.addTestSuite(BufferArrayTest.class);
        suite.addTestSuite(SegmentedArrayTest.class);
        suite.addTestSuite(SetGetTest.class);
        //$JUnit-END$
        return suite;
    }

    protected ArrayTests() {
        super();
    }

    protected ArrayTests(final String name) {
        super(name);
    }
}
