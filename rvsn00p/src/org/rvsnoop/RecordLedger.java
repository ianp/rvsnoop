/*
 * Class:     RecordLedger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.IOException;
import java.util.Collection;

import rvsnoop.Record;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * The record ledger is the central record store in RvSnoop.
 * <p>
 * The record ledger is a wrapper around an {@link EventList}, the wrapper
 * handles all of the sychronisation issues to do with reading and writing to
 * the list, different list implementations in subclasses are used to handle the
 * different storage mechanisms that are supported.
 * <p>
 * For more detailed information on how the various record ledger
 * implementations are structured and used see the ‘Record Ledger’ chapter in
 * the developers guide.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @version $Revision$, $Date$
 */
public abstract class RecordLedger {

    /**
     * The underlying event list used by this ledger.
     */
    private final EventList list;

    /**
     * Create a new record ledger.
     *
     * @param list The list to use as the record store.
     */
    protected RecordLedger(final EventList list) {
        this.list = list;
    }

    /**
     * Add a single record to the ledger.
     * <p>
     * This method acquires a write lock on the underlying list.
     *
     * @param record The record to add.
     * @return <code>true</code> if the ledger was modified by this operation,
     *     <code>false</code> otherwise.
     * @see java.util.Collection#add(Object)
     */
    public final boolean add(Record record) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            return list.add(record);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add a collection of records to the ledger.
     * <p>
     * This method acquires a write lock on the underlying list.
     *
     * @param records The records to add.
     * @see java.util.Collection#addAll(Collection)
     */
    public final void addAll(Collection records) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            list.addAll(records);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove all of the records from the ledger.
     * <p>
     * This method acquires a write lock on the underlying list.
     *
     * @see java.util.Collection#clear()
     */
    public final void clear() {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            list.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tests whether a given record is held in this ledger.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param record The record to search for.
     * @return <code>true</code> if the record is in the ledger,
     *     <code>false</code> otherwise.
     * @see java.util.Collection#contains(Object)
     */
    public final boolean contains(Record record) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            return list.contains(record);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Create and returns a new table model that shows the contents of this ledger.
     * <p>
     * Each table model created by this method will have it's own unique format
     * instance.
     * <p>
     * This method does not lock the underlying list.
     *
     * @return A new table model.
     */
    public final EventTableModel createTableModel() {
        final RecordLedgerFormat format = new RecordLedgerFormat();
        final EventTableModel model = new EventTableModel(list, format);
        format.setModel(model);
        return model;
    }

    /**
     * Find a specific record in the ledger.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param criteria How to match records.
     * @param startIndex Where to start searching.
     * @return The found record, or <code>null</code> if none meet the selection
     *     criterion.
     */
    public final Record find(Matcher criteria, int startIndex) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            for (int i = startIndex, imax = list.size(); i < imax; ++i) {
                final Record record = (Record) list.get(i);
                if (criteria.matches(record)) return record;
            }
            for (int i = 0; i < startIndex; ++i) {
                final Record record = (Record) list.get(i);
                if (criteria.matches(record)) return record;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * Find the positions of all records matching a given criterion.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param criteria How to match records.
     * @return The indices of the matching records.
     */
    public final int[] findAllIndices(Matcher criteria) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        final int[] temp = new int[list.size()];
        try {
            int pos = 0;
            for (int i = 0, imax = list.size(); i < imax; ++i) {
                final Record record = (Record) list.get(i);
                if (criteria.matches(record)) { temp[pos++] = i; }
            }
            final int[] indices = new int[pos];
            System.arraycopy(temp, 0, indices, 0, pos);
            return indices;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Find a specific record's position in the ledger.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param criteria How to match records.
     * @param startIndex Where to start searching.
     * @return The index of the found record, or -1 if none meet the selection
     *     criterion.
     */
    public final int findIndex(Matcher criteria, int startIndex) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            for (int i = startIndex, imax = list.size(); i < imax; ++i) {
                final Record record = (Record) list.get(i);
                if (criteria.matches(record)) return i;
            }
            for (int i = 0; i < startIndex; ++i) {
                final Record record = (Record) list.get(i);
                if (criteria.matches(record)) return i;
            }
        } finally {
            lock.unlock();
        }
        return -1;
    }

    /**
     * Retrieve a record from the ledger.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param index The index of the record to retrieve.
     * @return The record.
     * @see java.util.Collection#getAll(int)
     */
    public final Record get(int index) {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            return (Record) list.get(index);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieve several records from the ledger in one operation.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @param indices The indices of the records to retrieve.
     * @return The records at the specified indices.
     */
    public final Record[] getAll(int[] indices) {
        final Record[] records = new Record[indices.length];
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            for (int i = 0, imax = indices.length; i < imax; ++i) {
                records[i] = (Record) list.get(indices[i]);
            }
        } finally {
            lock.unlock();
        }
        return records;
    }

    /**
     * Get the underlying event list used by this ledger.
     * <p>
     * This is only for use by this class and the {@link FilteredLedgerView}, it
     * should not be called in any other code.
     *
     * @return The wrapped list.
     */
    EventList getEventList() {
        return list;
    }
    
    /**
     * Remove a single record from the ledger.
     * <p>
     * This method acquires a write lock on the underlying list.
     *
     * @param record The record to remove.
     * @return <code>true</code> if the ledger was modified by this operation,
     *     <code>false</code> otherwise.
     * @see java.util.Collection#remove(Object)
     */
    public final boolean remove(Record record) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            return list.remove(record);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove several records from the ledger.
     * <p>
     * This method acquires a write lock on the underlying list.
     *
     * @param records The records to remove.
     * @see java.util.Collection#removeAll(Collection)
     */
    public final void removeAll(Collection records) {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            list.removeAll(records);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return the number of records currently in the ledger.
     * <p>
     * This method acquires a read lock on the underlying list.
     *
     * @return The size of the ledger.
     * @see java.util.Collection#add(Object)
     */
    public final int size() {
        final Lock lock = list.getReadWriteLock().readLock();
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Synchronize the ledger with it's backing store.
     * <p>
     * If the ledger implementation is persistent this method should ensure that
     * it's backing store is in a consistent state (e.g. data flushed to disk).
     *
     * @throws IOException If there was a problem writing to the persistent
     *     storage.
     */
    public void syncronize() throws IOException {
        // This is just a hook for persistend sub-classes to use.
    }
}
