//:File:    Connections.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

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
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class Connections implements EventList {
    
    private static class DescriptionComparator implements Comparator {
        private final Collator collator = Collator.getInstance();
        public int compare(Object o1, Object o2) {
            final String d1 = ((RvConnection) o1).getDescription();
            final String d2 = ((RvConnection) o2).getDescription();
            return collator.compare(d1, d2);
        }
    }

    private static class Observer implements ObservableElementList.Connector, PropertyChangeListener {
        private ObservableElementList list;

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.ObservableElementList.Connector#installListener(java.lang.Object)
         */
        public EventListener installListener(Object element) {
            ((RvConnection) element).addPropertyChangeListener(this);
            return this;
        }

        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt) {
            list.elementChanged(evt.getSource());
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.ObservableElementList.Connector#setObservableElementList(ca.odell.glazedlists.ObservableElementList)
         */
        public void setObservableElementList(ObservableElementList list) {
            this.list = list;
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.ObservableElementList.Connector#uninstallListener(java.lang.Object, java.util.EventListener)
         */
        public void uninstallListener(Object element, EventListener listener) {
            ((RvConnection) element).removePropertyChangeListener(this);
        }
 
    }
    
    private static Connections instance;
    
    private static final Logger logger = Logger.getLogger(Connections.class);
    
    public static synchronized Connections getInstance() {
        if (instance == null) instance = new Connections();
        return instance;
    }
    
    private final ObservableElementList list = new ObservableElementList(
            new SortedList(new BasicEventList(), new DescriptionComparator()),
            new Observer());
    
    private Connections() {
        super();
    }

    /**
     * As allowed by the contract for {@link java.util.List#add(int, Object)}
     * this method will not add duplicates to the list. Note that unlike a
     * call to the un-indexed add method this will throw an
     * <code>IllegalStateException</code> if the element is not added.
     * 
     * @param index
     * @param element
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        if (!add(element)) throw new IllegalStateException("Duplicate element not added.");
    }
    
    /**
     * As allowed by the contract for {@link java.util.Collection#add(Object)}
     * this method will not add duplicates to the list.
     * 
     * @param o
     * @return <code>true</code> if the argument was added, <code>false</code> otherwise.
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(Object o) {
        list.getReadWriteLock().writeLock().lock();
        try {
            return internalAdd((RvConnection) o);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * @param c
     * @return
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        list.getReadWriteLock().writeLock().lock();
        boolean added = false;
        try {
            for (Iterator i = c.iterator(); i.hasNext();)
                added = internalAdd((RvConnection) i.next()) || added;
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
        return added;
    }
    
    /**
     * @param index
     * @param c
     * @return
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection c) {
        return addAll(c);
    }

    /**
     * @param listChangeListener
     * @see ca.odell.glazedlists.EventList#addListEventListener(ca.odell.glazedlists.event.ListEventListener)
     */
    public void addListEventListener(ListEventListener listChangeListener) {
        list.addListEventListener(listChangeListener);
    }

    /**
     * 
     * @see java.util.List#clear()
     */
    public void clear() {
        list.getReadWriteLock().writeLock().lock();
        try {
            while (list.size() > 0)
                internalRemove(0);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * @param o
     * @return
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.contains(o);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * @param c
     * @return
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.containsAll(c);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * @param o
     * @return
     * @see java.util.List#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.equals(o);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * @param index
     * @return
     * @see java.util.List#get(int)
     */
    public Object get(int index) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.get(index);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
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
        list.getReadWriteLock().readLock().lock();
        try {
            for (Iterator i = list.iterator(); i.hasNext();) {
                RvConnection c = (RvConnection) i.next();
                if (c.getService().equals(service) &&
                        c.getDaemon().equals(daemon) &&
                        c.getNetwork().equals(network))
                    return c;
            }
            return null;
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }
    
    /**
     * @return
     * @see ca.odell.glazedlists.EventList#getPublisher()
     */
    public ListEventPublisher getPublisher() {
        return list.getPublisher();
    }

    /**
     * @return
     * @see ca.odell.glazedlists.EventList#getReadWriteLock()
     */
    public ReadWriteLock getReadWriteLock() {
        return list.getReadWriteLock();
    }

    /**
     * @return
     * @see java.util.List#hashCode()
     */
    public int hashCode() {
        return list.hashCode();
    }

    /**
     * @param o
     * @return
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.indexOf(o);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    private boolean internalAdd(RvConnection connection) {
        if (list.contains(connection)) return false;
        if (Logger.isInfoEnabled())
            logger.info("Adding connection: " + connection);
        connection.setParentList(this);
        list.add(connection);
        return true;
    }

    private RvConnection internalRemove(int index) {
        RvConnection connection = (RvConnection) list.get(index);
        if (Logger.isInfoEnabled())
            logger.info("Removing connection: " + connection);
        connection.stop();
        connection.setParentList(null);
        list.remove(index);
        return connection;
    }

    private boolean internalRemove(RvConnection connection) {
        if (!list.contains(connection)) return false;
        if (Logger.isInfoEnabled())
            logger.info("Removing connection: " + connection);
        connection.stop();
        connection.setParentList(null);
        list.remove(connection);
        return true;
    }

    /**
     * @return
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.isEmpty();
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * @return
     * @see java.util.List#iterator()
     */
    public Iterator iterator() {
        return list.iterator();
    }

    /**
     * @param o
     * @return
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.lastIndexOf(o);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * @return
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator() {
        return list.listIterator();
    }

    /**
     * @param index
     * @return
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    /**
     * @param index
     * @return
     * @see java.util.List#remove(int)
     */
    public Object remove(int index) {
        list.getReadWriteLock().writeLock().lock();
        try {
            return internalRemove(index);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * @param o
     * @return
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        list.getReadWriteLock().writeLock().lock();
        try {
            return internalRemove((RvConnection) o);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * @param c
     * @return
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        list.getReadWriteLock().writeLock().lock();
        boolean removed = false;
        try {
            for (Iterator i = c.iterator(); i.hasNext();)
                removed = internalRemove((RvConnection) i.next()) || removed;
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
        return removed;
    }

    /**
     * @param listChangeListener
     * @see ca.odell.glazedlists.EventList#removeListEventListener(ca.odell.glazedlists.event.ListEventListener)
     */
    public void removeListEventListener(ListEventListener listChangeListener) {
        list.removeListEventListener(listChangeListener);
    }

    /**
     * This operation is not supported.
     * 
     * @param c Ignored.
     * @return The method never returns.
     * @see java.util.List#retainAll(java.util.Collection)
     * @throws UnsupportedOperationException Always.
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * This may be used to update elements in place but must not be used to set
     * arbitrary elements.
     * 
     * @param index The index to update.
     * @param element The element already at the index.
     * @return
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        list.getReadWriteLock().writeLock().lock();
        try {
            RvConnection connection = (RvConnection) list.get(index);
            if (!(connection.equals(element)))
                throw new IllegalArgumentException("Elements don't match! " + connection + " and " + element);
            return list.set(index, element);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * @return
     * @see java.util.List#size()
     */
    public int size() {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.size();
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * This operation is not supported.
     * 
     * @param fromIndex Ignored.
     * @param toIndex Ignored.
     * @return The method never returns.
     * @see java.util.List#subList(int, int)
     * @throws UnsupportedOperationException Always.
     */
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return May be safely cast to <code>RvConnection[]</code>.
     * @see java.util.List#toArray()
     */
    public Object[] toArray() {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.toArray(new RvConnection[list.size()]);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }        
    }

    /**
     * @param a
     * @return
     * @see java.util.List#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        list.getReadWriteLock().readLock().lock();
        try {
            return list.toArray(a);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }        
    }

}
