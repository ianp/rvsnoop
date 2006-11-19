//:File:    MessageLedger.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * The message ledger is the main store for received messages.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class MessageLedger {

    private static final class FilterMatcherEditor extends AbstractMatcherEditor {
        private Matcher matcher;
        FilterMatcherEditor() {
            super();
        }
        /* (non-Javadoc)
         * @see ca.odell.glazedlists.matchers.AbstractMatcherEditor#getMatcher()
         */
        public Matcher getMatcher() {
            return matcher != null ? matcher : new NullMatcher();
        }
        void setMatcher(Matcher matcher) {
            this.matcher = matcher;
            fireChanged(getMatcher());
        }
    }

    private static final class NullMatcher implements Matcher {
        NullMatcher() {
            super();
        }
        public boolean matches(Object item) {
            return true;
        }
    }

    public static final MessageLedger INSTANCE = new MessageLedger();

    final EventList eventList = new BasicEventList();

    final SortedList sortedList = new SortedList(eventList, new Comparator() {
        public int compare(Object o1, Object o2) {
            final long seq1 = ((Record) o1).getSequenceNumber();
            final long seq2 = ((Record) o2).getSequenceNumber();
            if (seq1 < seq2) return -1;
            if (seq1 > seq2) return 1;
            return 0;
        }
    });

    final FilterList filterList = new FilterList(sortedList);

    final FilterMatcherEditor filterMatcherEditor = new FilterMatcherEditor();

    public MessageLedger() {
        super();
        final EventList matchers = GlazedLists.eventList(Arrays.asList(new MatcherEditor[] {
            SubjectHierarchy.INSTANCE.getMatcherEditor(),
            RecordTypes.getInstance().getMatcherEditor(), filterMatcherEditor
        }));
        final CompositeMatcherEditor matcherEditor = new CompositeMatcherEditor(matchers);
        matcherEditor.setMode(CompositeMatcherEditor.AND);
        filterList.setMatcherEditor(matcherEditor);
    }

    public void addRecord(Record record) {
        final Lock lock = filterList.getReadWriteLock().writeLock();
        lock.lock();
        try {
            eventList.add(record);
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final Lock lock = filterList.getReadWriteLock().writeLock();
        lock.lock();
        try {
            eventList.clear();
        } finally {
            lock.unlock();
        }
    }

    public EventList getEventList() {
        // TODO: Remove this method and add delegates instead.
        return filterList;
    }

    /**
     * Lock the ledger for reading.
     * <p>
     * All of the methods which modify the ledger handle locking internally so
     * there is no corresponsing <code>getWriteLock()</code> moethod.
     * <p>
     * Really this should not be exposed. The only methods which still need it
     * are related to searching, the functionality for this should be added to
     * this class and should take a Matcher or similar as an argument. Maybe
     * there should be 2 versions, one returning indices and one returning the
     * actual records: getIndices(Matcher) and getRecords(Matcher).
     *
     * @return
     */
    public Lock getReadLock() {
        // TODO: Remove this method and add delegates instead. Need to add
        // enough delegates that all locking can be internal to the ledger.
        return filterList.getReadWriteLock().readLock();
    }

    /**
     * Get a record from the ledger.
     * <p>
     * Note that this method applies to the <em>filtered</em> ledger, so the
     * index will not necessarily be the same as the <code>sequenceNumber</code>
     * of the record.
     *
     * @param index The index of the record to retrieve.
     * @return The record.
     */
    public Record getRecord(int index) {
        filterList.getReadWriteLock().readLock().lock();
        try {
            return (Record) filterList.get(index);
        } finally {
            filterList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Bulk accessor for records from the ledger.
     * <p>
     * Note that this method applies to the <em>filtered</em> ledger, so the
     * indices will not necessarily be the same as the
     * <code>sequenceNumber</code>s of the records.
     *
     * @param indices The indices of the records to retrieve.
     * @return The records.
     */
    public Record[] getRecords(int[] indices) {
        filterList.getReadWriteLock().readLock().lock();
        try {
            final Record[] records = new Record[indices.length];
            for (int i = 0, imax = records.length; i < imax; ++i)
                records[i] = (Record) filterList.get(indices[i]);
            return records;
        } finally {
            filterList.getReadWriteLock().readLock().unlock();
        }
    }

    public int getRowCount() {
        return filterList.size();
    }

    public int getTotalRowCount() {
        return eventList.size();
    }

    public void removeAll(Collection records) {
        final Lock lock = filterList.getReadWriteLock().writeLock();
        lock.lock();
        try {
            eventList.removeAll(records);
        } finally {
            lock.unlock();
        }
    }

    public void setMatcher(Matcher matcher) {
        filterMatcherEditor.setMatcher(matcher);
    }

}
