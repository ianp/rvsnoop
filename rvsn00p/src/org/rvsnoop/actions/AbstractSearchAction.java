/*
 * Class:     AbstractSearchAction
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang.text.StrBuilder;
import org.rvsnoop.Application;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.matchers.DataAccessorFactory;
import org.rvsnoop.matchers.PredicateFactory;
import org.rvsnoop.matchers.RvSnoopMatcherEditor;
import org.rvsnoop.ui.MatcherEditorListDialog;

import rvsnoop.Record;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * Base class for actions which act on filtered ledger views.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class AbstractSearchAction extends RvSnoopAction {

    /**
     * @param name
     * @param application
     */
    protected AbstractSearchAction(String name, Application application) {
        super(name, application);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.RvSnoopAction#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent e) {
        final EventList oldMatchers = getMatcherEditors();
        final JFrame frame = application.getFrame().getFrame();
        final MatcherEditorListDialog dialog = new MatcherEditorListDialog(
                frame, getTitle(), getDescription(), getImage(), oldMatchers);

        dialog.setVisible(true);
        final EventList newMatchers = dialog.getCopyOfEditors();
        if (newMatchers == null) { return; } // User cancelled dialog.

        final List added = new ArrayList(newMatchers);
        added.removeAll(oldMatchers);
        final List removed = new ArrayList(oldMatchers);
        removed.removeAll(newMatchers);

        final FilteredLedgerView ledger = getLedger();
        for (Iterator i = added.iterator(); i.hasNext(); ) {
            ledger.addFilter((MatcherEditor) i.next());
        }
        for (Iterator i = removed.iterator(); i.hasNext(); ) {
            ledger.removeFilter((MatcherEditor) i.next());
        }

        displayResults(ledger);
    }

    /**
     * Configure the matchers based on a single record.
     *
     * @param record
     */
    protected final void configureMatchers(EventList matchers, Record record) {
        final DataAccessorFactory daf = DataAccessorFactory.getInstance();
        final PredicateFactory pf = PredicateFactory.getInstance();
        final Lock lock = matchers.getReadWriteLock().writeLock();
        lock.lock();
        try {
            final String sendSubject = record.getSendSubject();
            if (sendSubject != null && sendSubject.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createSendSubjectAccessor(),
                        pf.createStringStartsWithPredicate(sendSubject, false)));
            }
            final String replySubject = record.getReplySubject();
            if (replySubject != null && replySubject.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createReplySubjectAccessor(),
                        pf.createStringStartsWithPredicate(replySubject, false)));
            }
            final String trackingId = record.getTrackingId();
            if (trackingId != null && trackingId.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createTrackingIdAccessor(),
                        pf.createStringStartsWithPredicate(trackingId, false)));
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Configure the matchers based on multiple records.
     *
     * @param record
     */
    protected final void configureMatchers(EventList matchers, Record[] records) {
        final DataAccessorFactory daf = DataAccessorFactory.getInstance();
        final PredicateFactory pf = PredicateFactory.getInstance();
        final String[] strings = new String[records.length];
        final Lock lock = matchers.getReadWriteLock().writeLock();
        lock.lock();
        try {
            for (int i = 0, imax = records.length; i < imax; ++i) {
                strings[i] = records[i].getSendSubject();
            }
            final String sendSubject = findLongestCommonSubstring(strings);
            if (sendSubject != null && sendSubject.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createSendSubjectAccessor(),
                        pf.createStringStartsWithPredicate(sendSubject, false)));
            }

            for (int i = 0, imax = records.length; i < imax; ++i) {
                strings[i] = records[i].getReplySubject();
            }
            final String replySubject = findLongestCommonSubstring(strings);
            if (replySubject != null && replySubject.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createReplySubjectAccessor(),
                        pf.createStringStartsWithPredicate(replySubject, false)));
            }

            for (int i = 0, imax = records.length; i < imax; ++i) {
                strings[i] = records[i].getTrackingId();
            }
            final String trackingId = findLongestCommonSubstring(strings);
            if (trackingId != null && trackingId.length() > 0) {
                matchers.add(new RvSnoopMatcherEditor(
                        daf.createTrackingIdAccessor(),
                        pf.createStringStartsWithPredicate(trackingId, false)));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Hook to notify subclasses that the results need displaying.
     *
     * @param ledger The results to display.
     */
    protected void displayResults(RecordLedger ledger) {
        // Do nothing by default, assume that the results are already displayed.
    }

    private final String findLongestCommonSubstring(String[] strings) {
        final StrBuilder builder = new StrBuilder();
        int pos = 0;
        while (true) {
            if (pos == strings[0].length()) { return builder.toString(); }
            char c = strings[0].charAt(pos);
            for (int i = 0, imax = strings.length; i < imax; ++i) {
                if (pos == strings[i].length() || c != strings[i].charAt(pos)) {
                    return builder.toString();
                }
            }
            builder.append(c);
        }
    }
    protected abstract String getDescription();
    protected abstract Image getImage();

    protected abstract FilteredLedgerView getLedger();

    protected EventList getMatcherEditors() {
        return new BasicEventList();
    }

    protected abstract String getTitle();
}
