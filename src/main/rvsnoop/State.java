// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

/**
 * A typesafe enumeration representing a simple set of states.
 */
public final class State {

    /**
     * A key to use when this enumeration is used as a property on a JavaBean.
     */
    public static final String PROP_STATE = "state";

    public static final State PAUSED  = new State(1, "PAUSED");

    public static final State STARTED = new State(2, "STARTED");

    public static final State STOPPED = new State(0, "STOPPED");

    private final String name;

    private final int ordinal;

    private State(int ordinal, String name) {
        super();
        this.ordinal = ordinal;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof State && ((State) obj).ordinal == ordinal;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return 17 * ordinal;
    }

    @Override
    public String toString() {
        return name;
    }

}
