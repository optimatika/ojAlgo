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
package org.ojalgo.data.ann;

import org.ojalgo.data.ann.ArtificialNeuralNetwork.Activator;

final class LayerTemplate {

    final ArtificialNeuralNetwork.Activator activator;
    final int inputs;
    final int outputs;

    LayerTemplate(final int inputs, final int outputs, final Activator activator) {
        super();
        this.inputs = inputs;
        this.outputs = outputs;
        this.activator = activator;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LayerTemplate)) {
            return false;
        }
        LayerTemplate other = (LayerTemplate) obj;
        if (activator != other.activator) {
            return false;
        }
        if (inputs != other.inputs) {
            return false;
        }
        if (outputs != other.outputs) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((activator == null) ? 0 : activator.hashCode());
        result = (prime * result) + inputs;
        result = (prime * result) + outputs;
        return result;
    }

}
