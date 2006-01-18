//:File:    MessageLedger.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.JTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TableComparatorChooser;
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
            RecordType.getMatcherEditor(), filterMatcherEditor
        }));
        final CompositeMatcherEditor matcherEditor = new CompositeMatcherEditor(matchers);
        matcherEditor.setMode(CompositeMatcherEditor.AND);
        filterList.setMatcherEditor(matcherEditor);
    }
    
    public void addRecord(Record record) {
        final Lock lock = eventList.getReadWriteLock().writeLock();
        try {
            lock.lock();
            eventList.add(record);
        } finally {
            lock.unlock();
        }
    }
    
    public void clear() {
        final Lock lock = eventList.getReadWriteLock().writeLock();
        try {
            lock.lock();
            eventList.clear();
        } finally {
            lock.unlock();
        }
    }
    
    public int getColumnCount() {
        return MessageLedgerFormat.INSTANCE.getColumnCount();
    }

    public String getColumnName(int i) {
        return MessageLedgerFormat.INSTANCE.getColumnName(i);
    }

    public EventList getEventList() {
        return filterList;
    }

    public Record getRecord(int index) {
        final Lock lock = eventList.getReadWriteLock().readLock();
        try {
            lock.lock();
            return (Record) filterList.get(index);
        } finally {
            lock.unlock();
        }
    }

    public int getRowCount() {
        return filterList.size();
    }

    public int getTotalRowCount() {
        return eventList.size();
    }

    public void provideSorter(JTable table) {
        new TableComparatorChooser(table, sortedList, true);
    }
    
    public void removeAll(Collection records) {
        final Lock lock = eventList.getReadWriteLock().writeLock();
        try {
            lock.lock();
            eventList.removeAll(records);
        } finally {
            lock.unlock();
        }
    }

    public void setMatcher(Matcher matcher) {
        filterMatcherEditor.setMatcher(matcher);
    }

}
