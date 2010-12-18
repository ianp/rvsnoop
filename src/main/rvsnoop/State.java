// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop;

/**
 * A typesafe enumeration representing a simple set of states.
 */
public enum State {

    PAUSED, STARTED, STOPPED;

    /**
     * A key to use when this enumeration is used as a property on a JavaBean.
     */
    public static final String PROP_STATE = "state";

}
