package org.ojalgo.structure;

import java.util.AbstractList;

final class WrapperList<N extends Comparable<N>> extends AbstractList<N> {

    private final Access1D<N> myWrappee;

    WrapperList(final Access1D<N> wrappee) {
        super();
        myWrappee = wrappee;
    }

    @Override
    public int size() {
        return myWrappee.size();
    }

    @Override
    public N get(final int index) {
        return myWrappee.get(index);
    }

}
