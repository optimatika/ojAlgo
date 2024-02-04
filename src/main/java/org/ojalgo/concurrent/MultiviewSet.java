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
package org.ojalgo.concurrent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Multiple prioritised {@link Queue}:s backed by a common {@link Set}. Typical usage:
 * <ol>
 * <li>Create a {@link MultiviewSet} instance
 * <li>Call {@link #newView(Comparator)} (multiple times) to create the necessary views
 * <li>{@link #add(Object)}, or {@link PrioritisedView#offer(Object)} to any of the views, will have the same
 * effect.
 * <li>{@link PrioritisedView#poll()} from each of the views as needed.
 * </ol>
 *
 * @author apete
 */
public final class MultiviewSet<T> {

    public final class PrioritisedView {

        private final PriorityBlockingQueue<T> myQueue;

        PrioritisedView(final Set<T> initial, final Comparator<? super T> comparator) {

            super();

            myQueue = new PriorityBlockingQueue<>(Math.max(9, initial.size()), comparator);
            myQueue.addAll(initial);
        }

        public boolean isEmpty() {
            return myQueue.isEmpty();
        }

        /**
         * The entry is also added to the common {@link Set} and therefore to all views backed by it.
         */
        public void offer(final T entry) {
            MultiviewSet.this.add(entry);
        }

        /**
         * @return The highest priority item (that also existed in the backing {@link Set})
         */
        public T poll() {

            T candidate = null;

            do {
                candidate = myQueue.poll();
            } while (candidate != null && !MultiviewSet.this.remove(candidate));

            return candidate;
        }

        public int size() {
            return myQueue.size();
        }

        boolean add(final T entry) {
            return myQueue.add(entry);
        }

        void clear() {
            myQueue.clear();
        }

        boolean remove(final Object entry) {
            return myQueue.remove(entry);
        }

    }

    private final Set<T> myCommonSet = ConcurrentHashMap.newKeySet();
    private final Collection<PrioritisedView> myViews = new LinkedBlockingDeque<>();
    private final boolean myRemoveFromViews;

    public MultiviewSet() {
        this(false); //TODO Not decided what the default should be

    }

    /**
     * @param removeFromViews Switch if each and every call to {@link #remove(Object)} should also explicitly
     *        call {@link PrioritisedView#remove(Object)} on each of the views. This is (probably)
     *        innefficient, and is unnecessary as a call to {@link PrioritisedView#poll()} will assert that
     *        the returned instance did exist in the main {@link Set}.
     */
    public MultiviewSet(final boolean removeFromViews) {
        super();
        myRemoveFromViews = removeFromViews;
    }

    /**
     * Add an entry to the common {@link Set} and all {@link Queue}:s.
     */
    public boolean add(final T entry) {
        boolean retVal = myCommonSet.add(entry);
        if (retVal) {
            for (MultiviewSet<T>.PrioritisedView view : myViews) {
                view.add(entry);
            }
        }
        return retVal;
    }

    public void clear() {
        myCommonSet.clear();
        for (MultiviewSet<T>.PrioritisedView view : myViews) {
            view.clear();
        }
    }

    /**
     * @return true if the main set or any of the priority queues have any contents
     */
    public boolean isAnyContents() {

        if (!this.isEmpty()) {
            return true;
        }

        for (MultiviewSet<T>.PrioritisedView view : myViews) {
            if (!view.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return myCommonSet.isEmpty();
    }

    public PrioritisedView newView(final Comparator<? super T> comparator) {
        PrioritisedView view = new PrioritisedView(myCommonSet, comparator);
        myViews.add(view);
        return view;
    }

    /**
     * Remove an entry from the common {@link Set} and all {@link Queue}:s.
     */
    public boolean remove(final T entry) {
        boolean retVal = myCommonSet.remove(entry);
        if (myRemoveFromViews && retVal) {
            for (MultiviewSet<T>.PrioritisedView view : myViews) {
                view.remove(entry);
            }
        }
        return retVal;
    }

    public int size() {
        return myCommonSet.size();
    }

}
