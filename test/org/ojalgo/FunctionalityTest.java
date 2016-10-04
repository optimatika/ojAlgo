/*
 * Copyright 1997-2016 Optimatika (www.optimatika.se) Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ojalgo;

import org.ojalgo.access.AccessTests;
import org.ojalgo.array.ArrayTests;
import org.ojalgo.constant.ConstantTests;
import org.ojalgo.finance.FinanceTests;
import org.ojalgo.finance.data.FinanceDataTests;
import org.ojalgo.finance.portfolio.FinancePortfolioTests;
import org.ojalgo.function.FunctionTests;
import org.ojalgo.function.multiary.FunctionMultiaryTests;
import org.ojalgo.function.polynomial.FunctionPolynomialTests;
import org.ojalgo.machine.MachineTests;
import org.ojalgo.matrix.MatrixTests;
import org.ojalgo.matrix.decomposition.MatrixDecompositionTests;
import org.ojalgo.matrix.store.MatrixStoreTests;
import org.ojalgo.matrix.task.MatrixTaskTests;
import org.ojalgo.optimisation.OptimisationTests;
import org.ojalgo.optimisation.convex.OptimisationConvexTests;
import org.ojalgo.optimisation.integer.OptimisationIntegerTests;
import org.ojalgo.optimisation.linear.OptimisationLinearTests;
import org.ojalgo.random.RandomTests;
import org.ojalgo.random.process.RandomProcessTests;
import org.ojalgo.scalar.ScalarTests;
import org.ojalgo.series.SeriesTests;
import org.ojalgo.type.context.TypeContextTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author apete
 */
public abstract class FunctionalityTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(FunctionalityTest.suite());
    }

    public static Test suite() {
        final TestSuite suite = new TestSuite("ojAlgo Functionality Tests");
        //$JUnit-BEGIN$
        suite.addTest(AccessTests.suite());
        suite.addTest(ArrayTests.suite());
        suite.addTest(ConstantTests.suite());
        suite.addTest(MachineTests.suite());
        suite.addTest(FinanceTests.suite());
        suite.addTest(FinanceDataTests.suite());
        suite.addTest(FinancePortfolioTests.suite());
        suite.addTest(FunctionTests.suite());
        suite.addTest(FunctionMultiaryTests.suite());
        suite.addTest(FunctionPolynomialTests.suite());
        suite.addTest(MatrixTests.suite());
        suite.addTest(MatrixDecompositionTests.suite());
        suite.addTest(MatrixTaskTests.suite());
        suite.addTest(MatrixStoreTests.suite());
        suite.addTest(OptimisationTests.suite());
        suite.addTest(OptimisationConvexTests.suite());
        suite.addTest(OptimisationIntegerTests.suite());
        suite.addTest(OptimisationLinearTests.suite());
        suite.addTest(RandomTests.suite());
        suite.addTest(RandomProcessTests.suite());
        suite.addTest(ScalarTests.suite());
        suite.addTest(SeriesTests.suite());
        suite.addTest(TypeContextTests.suite());
        //$JUnit-END$
        return suite;
    }

    protected FunctionalityTest() {
        super();
    }

    protected FunctionalityTest(final String name) {
        super(name);
    }

}
