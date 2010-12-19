// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.event;

import rvsnoop.Record;

import java.util.EventObject;

/**
 * Event fired when a new message is received.
 */
public class MessageReceivedEvent extends EventObject {

    public MessageReceivedEvent(Record record) {
        super(record);
    }

    @Override
    public Record getSource() {
        return (Record) super.getSource();
    }

}
