package org.sc.themis.shared.utils;

public class BitwiseState {

    private int state = 0x0;

    public boolean isset( int property ) {
        return (this.state & property) == property;
    }

    public void unset( int property ) {
        this.state &= ~property;
    }

    public void set( int property ) {
        this.state |= property;
    }

}
