/*
 * Copyright 1997-2022 Optimatika
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

    private Optimisation.Sense myOptimisationSense = Optimisation.Sense.MIN;

    protected AbstractModel(final Optimisation.Options someOptions) {

        super();

        options = someOptions;
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    public final boolean isMaximisation() {
        return this.getOptimisationSense() == Optimisation.Sense.MAX;
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    public final boolean isMinimisation() {
        return this.getOptimisationSense() == Optimisation.Sense.MIN;
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    public final void setMaximisation() {
        this.setOptimisationSense(Optimisation.Sense.MAX);
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    public final void setMinimisation() {
        this.setOptimisationSense(Optimisation.Sense.MIN);
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    protected final Optimisation.Sense getOptimisationSense() {
        return myOptimisationSense;
    }

    /**
     * @deprecated v49 Will be removed or at least made private
     */
    @Deprecated
    protected final void setOptimisationSense(final Optimisation.Sense optimisationSense) {
        myOptimisationSense = optimisationSense;
    }

}
