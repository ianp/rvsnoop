// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.event;

import org.rvsnoop.Connections;
import rvsnoop.RvConnection;

import java.util.EventObject;

/**
 * Event fired when a new connection has been created.
 */
public final class ConnectionCreatedEvent extends EventObject {

    private final RvConnection connection;

    public ConnectionCreatedEvent(Connections connections, RvConnection connection) {
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
