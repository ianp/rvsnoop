/*
 * Class:     FilteredLedgerView
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import rvsnoop.RecordTypes;
import rvsnoop.SubjectHierarchy;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FreezableList;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * A decorator for record ledgers that supports selectively filtering the
 * contents of the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public class FilteredLedgerView extends RecordLedger {

    private final class MatcherHider implements Matcher {
        public boolean matches(Object item) {
            if (subjectFilter != null && subjectFilter.equals(item)) {
                return false;
            }
            if (typeFilter != null && typeFilter.equals(item)) {
                return false;
            }
            return true;
        }

    }

    public static FilteredLedgerView newInstance(RecordLedger ledger, boolean freezable) {
        if (freezable) {
            final FreezableList freezableList = new FreezableList(ledger.getEventList());
            final FilterList filter = new FilterList(freezableList);
            final FilteredLedgerView view = new FilteredLedgerView(filter);
            view.freezableList = freezableList;
            return view;
        } else {
            return new FilteredLedgerView(new FilterList(ledger.getEventList()));
        }
    }

    private final CompositeMatcherEditor filters = new CompositeMatcherEditor();

    private FreezableList freezableList;

    private MatcherEditor subjectFilter;

    private MatcherEditor typeFilter;

    /** Create a new <code>FilteredLedgerView</code>. */
    protected FilteredLedgerView(FilterList list) {
        super(list);
        list.setMatcherEditor(filters);
        filters.setMode(CompositeMatcherEditor.AND);
        setFilteringOnSubject(true);
        setFilteringOnType(true);
    }

    /**
     * Add an arbitrary filter to this view.
     *
     * @param filter The filter to add.
     */
    public void addFilter(MatcherEditor filter) {
        final EventList editors = filters.getMatcherEditors();
        final Lock lock = editors.getReadWriteLock().writeLock();
        lock.lock();
        try {
            editors.add(filter);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Return a list of all of the user supplied filters currently in place.
     * <p>
     * Note that the list returned by this method is live, that is, changes to
     * it will be applied to the view immediately. Also, the subject and type
     * matchers will not be visible in the list.
     *
     * @return The user supplied filters.
     */
    public EventList getMatchers() {
        return new FilterList(filters.getMatcherEditors(), new MatcherHider());
    }

    /**
     * Is this view currently filtering records based on the subject hierarchy.
     *
     * @return <code>true</code> if subject filtering is turned on,
     *     <code>false</code> otherwise.
     */
    public synchronized boolean isFilteringOnSubject() {
        return subjectFilter != null;
    }

    /**
     * Is this view currently filtering records based on their type.
     *
     * @return <code>true</code> if type filtering is turned on,
     *     <code>false</code> otherwise.
     */
    public synchronized boolean isFilteringOnType() {
        return typeFilter != null;
    }

    public boolean isFreezable() {
        return freezableList != null && !freezableList.isFrozen();
    }

    public synchronized boolean isFrozen() {
        return freezableList != null && freezableList.isFrozen();
    }

    /**
     * Remove an arbitraty filter from this view.
     *
     * @param filter The filter to remove.
     */
    public void removeFilter(MatcherEditor filter) {
        final EventList editors = filters.getMatcherEditors();
        final Lock lock = editors.getReadWriteLock().writeLock();
        lock.lock();
        try {
            editors.remove(filter);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tell this view whether to filter records based on the state of the
     * subject hierarchy.
     *
     * @param filtering Enable subject filtering or not.
     */
    public synchronized void setFilteringOnSubject(boolean filtering) {
        if (filtering && subjectFilter != null) { return; }
        if (!filtering && subjectFilter == null) { return; }
        final EventList editors = filters.getMatcherEditors();
        final Lock lock = editors.getReadWriteLock().writeLock();
        lock.lock();
        try {
            if (filtering) {
                subjectFilter = SubjectHierarchy.INSTANCE.getMatcherEditor();
                editors.add(subjectFilter);
            } else {
                editors.remove(subjectFilter);
                subjectFilter = null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tell this view whether to filter records based on their type.
     *
     * @param filtering Enable type filtering or not.
     */
    public synchronized void setFilteringOnType(boolean filtering) {
        if (filtering && typeFilter != null) return;
        if (!filtering && typeFilter == null) return;
        final EventList editors = filters.getMatcherEditors();
        final Lock lock = editors.getReadWriteLock().writeLock();
        lock.lock();
        try {
            if (filtering) {
                typeFilter = RecordTypes.getInstance().getMatcherEditor();
                editors.add(typeFilter);
            } else {
                editors.remove(typeFilter);
                typeFilter = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void setFrozen(boolean frozen) {
        if (frozen) {
            freezableList.freeze();
        } else {
            freezableList.thaw();
        }
    }

}
