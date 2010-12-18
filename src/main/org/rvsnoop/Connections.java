// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;

import javax.swing.ListModel;

import org.bushe.swing.event.EventBus;

import rvsnoop.RvConnection;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * A list of connections.
 * <p>
 * The list of connections is responsible for ensuring that no duplicate
 * connections are added to the list and then whenever a connection is removed
 * from the list it is stopped first.
 * <p>
 * Also, whenever a connection is added to the list it is 'activated' by calling
 * the protected method {@link RvConnection#setParentList(Connections)}.
 * <p>
 * This class wraps a sorted event list to which calls are delegated, the
 * wrapper methods handle ensuring no duplicates are added to the list and also
 * all synchronization using the Glazed Lists locking idiom.
 */
public final class Connections {

    private static final Logger logger = Logger.getLogger();

    public static void toXML(RvConnection[] connections, OutputStream stream) throws IOException {
        final XMLBuilder builder = new XMLBuilder(stream, XMLBuilder.NS_CONNECTIONS)
            .namespace(XMLBuilder.PREFIX_RENDEZVOUS, XMLBuilder.NS_RENDEZVOUS)
            .startTag("connections", XMLBuilder.NS_CONNECTIONS);
        for (int i = 0, imax = connections.length; i < imax; ++i) {
            connections[i].toXML(builder);
        }
        builder.endTag().close();
    }

    private final ObservableElementList<RvConnection> list;

    public Connections() {
        this.list = new ObservableElementList<RvConnection>(
                new SortedList<RvConnection>(
                        GlazedLists.eventList(new ArrayList<RvConnection>()),
                        new DescriptionComparator()),
                new Observer());
    }

    /**
     * As allowed by the contract for {@link java.util.Collection#add(Object)}
     * this method will not add duplicates to the list.
     *
     * @param connection The connection to add.
     * @return <code>true</code> if the argument was added, <code>false</code>
     *         otherwise.
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(RvConnection connection) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            if (list.contains(connection)) {
                logger.info("Ignoring attempt to add duplicate connection: %s", connection);
                return false;
            }
            logger.info("Adding connection: %s", connection);
            connection.setParentList(this);
            list.add(connection);
            EventBus.publish(new AddedEvent(connection));
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            while (list.size() > 0) {
                final RvConnection connection = list.get(0);
                logger.info("Removing connection: %s", connection);
                connection.stop();
                connection.setParentList(null);
                list.remove(0);
            }
        } finally {
            lock.unlock();
        }
    }

    public ListModel createListModel() {
        return new EventListModel<RvConnection>(list);
    }

    /**
     * Get an existing connection.
     *
     * @param service The Rendezvous service parameter.
     * @param network The Rendezvous network parameter.
     * @param daemon The Rendezvous daemon parameter.
     * @return The existing connection, or <code>null</code> if it does not exist.
     */
    public RvConnection get(String service, String network, String daemon) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
        	for (RvConnection c : list) {
                if (c.getService().equals(service) && c.getDaemon().equals(daemon) && c.getNetwork().equals(network))
                    return c;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return An iterator over all of the connections.
     * @see java.util.List#iterator()
     */
    public Iterator<RvConnection> iterator() {
        return list.iterator();
    }

    /**
     * @param connection The connection to remove.
     * @return <code>true</code> if a connection was removed,
     *         <code>false</code> otherwise.
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(RvConnection connection) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            if (!list.contains(connection)) { return false; }
            logger.info("Removing connection: %s", connection);
            connection.stop();
            connection.setParentList(null);
            list.remove(connection);
            EventBus.publish(new RemovedEvent(connection));
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return The number of connections.
     * @see java.util.List#size()
     */
    public int size() {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return An array containing all of the connections. The contents may be
     *         safely cast to <code>RvConnection[]</code>.
     * @see java.util.List#toArray()
     */
    public RvConnection[] toArray() {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            return list.toArray(new RvConnection[list.size()]);
        } finally {
            lock.unlock();
        }
    }

    public final class AddedEvent extends EventObject {

        static final long serialVersionUID = -1027536693959206731L;
        
        private final RvConnection connection;

        public AddedEvent(RvConnection connection) {
            super(Connections.this);
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
    
    private static class DescriptionComparator implements Comparator<RvConnection> {
        private final Collator collator = Collator.getInstance();

        public DescriptionComparator() {
            super();
        }

        public int compare(RvConnection o1, RvConnection o2) {
            return collator.compare(o1.getDescription(), o2.getDescription());
        }
    }

    private static class Observer implements ObservableElementList.Connector<RvConnection>,
            PropertyChangeListener {
        private ObservableElementList<RvConnection> list;

        Observer() {
            super();
        }

        public EventListener installListener(RvConnection element) {
            element.addPropertyChangeListener(this);
            return this;
        }

        public void propertyChange(PropertyChangeEvent event) {
            list.elementChanged((RvConnection) event.getSource());
        }

        public void setObservableElementList(ObservableElementList<RvConnection> list) {
            this.list = list;
        }

        public void uninstallListener(RvConnection element, EventListener listener) {
            element.removePropertyChangeListener(this);
        }
    }

    public final class RemovedEvent extends EventObject {

        private static final long serialVersionUID = -5382677785401677911L;

        private final RvConnection connection;

        public RemovedEvent(RvConnection connection) {
            super(Connections.this);
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

}
