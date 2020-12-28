/*
 * Copyright 1997-2020 Optimatika
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
package org.ojalgo.optimisation;

abstract class AbstractModel implements Optimisation.Model {

    public final Optimisation.Options options;

    private boolean myMinimisation = true;

    protected AbstractModel() {

        super();

        options = new Optimisation.Options();
    }

    protected AbstractModel(final Optimisation.Options someOptions) {

        super();

        options = someOptions;
    }

    /**
     * @deprected v49 Will be removed or at least made private
     */
    public final boolean isMaximisation() {
        return !this.isMinimisation();
    }

    /**
     * @deprected v49 Will be removed or at least made private
     */
    public final boolean isMinimisation() {
        return myMinimisation;
    }

    /**
     * @deprected v49 Will be removed or at least made private
     */
    public final void setMaximisation() {
        this.setMaximisation(true);
    }

    /**
     * @deprected v49 Will be removed or at least made private
     */
    public final void setMinimisation() {
        this.setMinimisation(true);
    }

    protected final void setMaximisation(final boolean maximisation) {
        this.setMinimisation(!maximisation);
    }

    protected final void setMinimisation(final boolean minimisation) {
        myMinimisation = minimisation;
    }

}
