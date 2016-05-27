package org.ojalgo.access;

public class ElementView2D<N extends Number> extends ElementView<N> {

    private final Access1D<N> myAccess;

    protected ElementView2D(final Access2D<N> access, final long index) {

        super(access, index);

        myAccess = access;
    }

}
