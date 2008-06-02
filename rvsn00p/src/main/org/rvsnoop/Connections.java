/*
 * Class:     Connections
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Iterator;

import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rvsnoop.RvConnection;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEventListener;
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
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date: 2007-01-08 08:07:22 +0000 (Mon, 08 Jan
 *          2007) $
 * @since 1.6
 */
public final class Connections {

    private static class DescriptionComparator implements Comparator {
        private final Collator collator = Collator.getInstance();

        public DescriptionComparator() {
            super();
        }

        public int compare(Object o1, Object o2) {
            final String d1 = ((RvConnection) o1).getDescription();
            final String d2 = ((RvConnection) o2).getDescription();
            return collator.compare(d1, d2);
        }
    }

    private static class Observer implements ObservableElementList.Connector,
            PropertyChangeListener {
        private ObservableElementList list;

        Observer() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.ObservableElementList.Connector#installListener(java.lang.Object)
         */
        public EventListener installListener(Object element) {
            ((RvConnection) element).addPropertyChangeListener(this);
            return this;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
            list.elementChanged(evt.getSource());
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.ObservableElementList.Connector#setObservableElementList(ca.odell.glazedlists.ObservableElementList)
         */
        public void setObservableElementList(ObservableElementList list) {
            this.list = list;
        }

        /*
         * (non-Javadoc)
         *
         * @see ca.odell.glazedlists.ObservableElementList.Connector#uninstallListener(java.lang.Object,
         *      java.util.EventListener)
         */
        public void uninstallListener(Object element, EventListener listener) {
            ((RvConnection) element).removePropertyChangeListener(this);
        }
    }

    private static final Log log = LogFactory.getLog(Connections.class);

    private final ObservableElementList list;

    private final boolean ownsConnections;

    public Connections(Collection connections, boolean ownsConnections) {
        if (connections == null) { connections = Collections.EMPTY_LIST; }
        this.list = new ObservableElementList(
                new SortedList(
                        GlazedLists.eventList(connections),
                        new DescriptionComparator()),
                new Observer());
        this.ownsConnections = ownsConnections;
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
                if (log.isInfoEnabled()) {
                    log.info("Ignoring attempt to add duplicate connection: "
                            + connection);
                }
                return false;
            }
            if (log.isInfoEnabled()) {
                log.info("Adding connection: " + connection);
            }
            if (ownsConnections) { connection.setParentList(this); }
            list.add(connection);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param listChangeListener
     * @see ca.odell.glazedlists.EventList#addListEventListener(ca.odell.glazedlists.event.ListEventListener)
     */
    public void addListEventListener(ListEventListener listChangeListener) {
        list.addListEventListener(listChangeListener);
    }

    /**
     * @see java.util.List#clear()
     */
    public void clear() {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            while (list.size() > 0) {
                final RvConnection connection = (RvConnection) list.get(0);
                if (log.isInfoEnabled()) {
                    log.info("Removing connection: " + connection);
                }
                if (ownsConnections) {
                    connection.stop();
                    connection.setParentList(null);
                }
                list.remove(0);
            }
        } finally {
            lock.unlock();
        }
    }

    public ListModel createListModel() {
        return new EventListModel(list);
    }

    /**
     * Get an existing connection.
     *
     * @param service The Rendezvous service parameter.
     * @param network The Rendezvous network parameter.
     * @param daemon The Rendezvous daemon parameter.
     * @return The existing connection, or <code>null</code> if it does not
     *         exist.
     */
    public RvConnection get(String service, String network, String daemon) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            for (final Iterator i = list.iterator(); i.hasNext();) {
                final RvConnection c = (RvConnection) i.next();
                if (c.getService().equals(service)
                        && c.getDaemon().equals(daemon)
                        && c.getNetwork().equals(network))
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
    public Iterator iterator() {
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
            if (log.isInfoEnabled()) {
                log.info("Removing connection: " + connection);
            }
            if (ownsConnections) {
                connection.stop();
                connection.setParentList(null);
            }
            list.remove(connection);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param listChangeListener
     * @see ca.odell.glazedlists.EventList#removeListEventListener(ca.odell.glazedlists.event.ListEventListener)
     */
    public void removeListEventListener(ListEventListener listChangeListener) {
        list.removeListEventListener(listChangeListener);
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
            return (RvConnection[]) list.toArray(new RvConnection[list.size()]);
        } finally {
            lock.unlock();
        }
    }

    public void toXML(OutputStream stream) throws IOException {
        final XMLBuilder builder = new XMLBuilder(stream, XMLBuilder.NS_CONNECTIONS)
            .namespace(XMLBuilder.PREFIX_RENDEZVOUS, XMLBuilder.NS_RENDEZVOUS)
            .startTag("connections", XMLBuilder.NS_CONNECTIONS);
        final RvConnection[] connections = toArray();
        for (int i = 0, imax = connections.length; i < imax; ++i) {
            connections[i].toXML(builder);
        }
        builder.endTag().close();
    }

}
