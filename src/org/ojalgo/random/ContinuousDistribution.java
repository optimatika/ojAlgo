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
package org.ojalgo.random;

public interface ContinuousDistribution extends Distribution {

    /**
     * In probability theory and statistics, the cumulative distribution function (CDF), or just distribution
     * function, describes the probability that a real-valued random variable X with a given probability
     * distribution will be found at a value less than or equal to x. Intuitively, it is the "area so far"
     * function of the probability distribution. Cumulative distribution functions are also used to specify
     * the distribution of multivariate random variables. <a
     * href="http://en.wikipedia.org/wiki/Cumulative_distribution_function">WikipediA</a>
     *
     * @param aValue x
     * @return P(&lt;=x)
     */
    double getDistribution(double aValue);

    /**
     * In probability theory, a probability density function (pdf), or density of a continuous random variable
     * is a function that describes the relative likelihood for this random variable to occur at a given
     * point. The probability for the random variable to fall within a particular region is given by the
     * integral of this variable's density over the region. The probability density function is nonnegative
     * everywhere, and its integral over the entire space is equal to one. <a
     * href="http://en.wikipedia.org/wiki/Probability_density_function">WikipediA</a>
     *
     * @param aValue x
     * @return P(x)
     */
    double getProbability(double aValue);

    /**
     * The quantile function, for any distribution, is defined for real variables between zero and one and is
     * mathematically the inverse of the cumulative distribution function. <a
     * href="http://en.wikipedia.org/wiki/Quantile_function">WikipediA</a> The input probability absolutely
     * has to be [0.0, 1.0], but values close to 0.0 and 1.0 may be problematic
     *
     * @param aProbality P(&lt;=x)
     * @return x
     */
    double getQuantile(double aProbality);

}
