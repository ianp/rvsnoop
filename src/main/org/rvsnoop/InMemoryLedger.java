// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import ca.odell.glazedlists.util.concurrent.Lock;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.jdesktop.application.ApplicationContext;
import org.rvsnoop.event.MessageReceivedEvent;
import org.rvsnoop.event.ProjectClosingEvent;
import rvsnoop.Record;

import ca.odell.glazedlists.BasicEventList;
import rvsnoop.RecordTypes;

/**
 * A ledger that uses a simple in-memory collection to hold the records.
 */
public final class InMemoryLedger extends RecordLedger {

    /**
     * Create a new in memory ledger instance.
     */
    public InMemoryLedger(ApplicationContext context, RecordTypes recordTypes) {
        super(context, new BasicEventList<Record>(), recordTypes);
        AnnotationProcessor.process(this);
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        add(event.getSource());
    }

    @EventSubscriber
    public void onProjectClosing(ProjectClosingEvent event) {
        this.clear();
    }

}
