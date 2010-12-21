// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.event;

import org.rvsnoop.Connections;
import rvsnoop.RvConnection;

import java.util.EventObject;

/**
 * Event fired when a connection has been destroyed/removed.
 */
public final class ConnectionDestroyedEvent extends EventObject {

    private final RvConnection connection;

    public ConnectionDestroyedEvent(Connections connections, RvConnection connection) {
        super(connections);
        this.connection = connection;
    }

    public RvConnection getConnection() {
        return connection;
    }

    @Override
    public Connections getSource() {
        return (Connections) super.getSource();
    }

}
