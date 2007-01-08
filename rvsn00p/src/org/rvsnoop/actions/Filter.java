/*
 * Class:     Filter
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.apache.commons.lang.text.StrBuilder;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

import rvsnoop.Record;
import rvsnoop.ui.FilterDialog;

/**
 * Filter the messages visible in the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Filter extends RvSnoopAction implements RecordLedgerSelectionListener {

    private static class FilterMatcher implements Matcher {
        private String sendSubject;
        private String trackingId;

        private boolean sendSubjectEnabled;
        private boolean trackingIdEnabled;

        FilterMatcher(String sendSubject, String trackingId, boolean sendSubjectEnabled, boolean trackingIdEnabled) {
            super();
            this.sendSubject = sendSubject;
            this.trackingId = trackingId;
            this.sendSubjectEnabled = sendSubjectEnabled;
            this.trackingIdEnabled = trackingIdEnabled;
        }

        public boolean matches(Object item) {
            final Record record = (Record) item;
            if (sendSubjectEnabled && record.getSendSubject().indexOf(sendSubject) < 0) return false;
            if (trackingIdEnabled && record.getTrackingId().indexOf(trackingId) < 0) return false;
            return true;
        }
    }

    private static class FilterMatcherEditor extends AbstractMatcherEditor {
        // TODO: Make this more efficient by re-using the matcher and firing
        // constrained or relaxed instead of just changed each time.
        FilterMatcherEditor() {
            super();
        }
        void setMatcher(Matcher matcher) {
            currentMatcher = matcher;
            fireChanged(matcher);
        }
    }

    static { NLSUtils.internationalize(Filter.class); }

    private static final long serialVersionUID = 7395491526593830048L;

    public static final String FILTER_COMMAND = "filter";
    public static final String FILTER_BY_SELECTION_COMMAND = "filterBySelection";

    static String FILTER_ACCELERATOR, FILTER_MNEMONIC,
        FILTER_NAME, FILTER_TOOLTIP;
    static String FILTER_BY_SELECTION_ACCELERATOR, FILTER_BY_SELECTION_MNEMONIC,
        FILTER_BY_SELECTION_NAME, FILTER_BY_SELECTION_TOOLTIP;

    private final boolean bySelection;
    
    private String sendSubject;
    private String trackingId;

    private boolean sendSubjectEnabled;
    private boolean trackingIdEnabled;

    private FilterMatcherEditor matcherEditor;

    private transient Record[] currentSelection;

    public Filter(Application application, boolean bySelection) {
        super(bySelection ? FILTER_BY_SELECTION_NAME : FILTER_NAME, application);
        this.bySelection = bySelection;
        if (bySelection) {
            putValue(Action.ACTION_COMMAND_KEY, FILTER_BY_SELECTION_COMMAND);
            putSmallIconValue(FILTER_BY_SELECTION_COMMAND);
            putValue(Action.SHORT_DESCRIPTION, FILTER_BY_SELECTION_TOOLTIP);
            putMnemonicValue(FILTER_BY_SELECTION_MNEMONIC);
            putAcceleratorValue(FILTER_BY_SELECTION_ACCELERATOR);
        } else {
            putValue(Action.ACTION_COMMAND_KEY, FILTER_COMMAND);
            putSmallIconValue(FILTER_COMMAND);
            putValue(Action.SHORT_DESCRIPTION, FILTER_TOOLTIP);
            putMnemonicValue(FILTER_MNEMONIC);
            putAcceleratorValue(FILTER_ACCELERATOR);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (bySelection) {
            // TODO use all of the records when filtering by selection.
            final Record record = currentSelection[0];
            sendSubject = record.getSendSubject();
            trackingId = record.getTrackingId();
        }
        final FilterDialog dialog = new FilterDialog(sendSubject, trackingId, sendSubjectEnabled, trackingIdEnabled);
        dialog.setVisible(true);
        if (dialog.isCancelled()) { return; }
        sendSubject = dialog.getSendSubject();
        trackingId = dialog.getTrackingId();
        sendSubjectEnabled = dialog.isSendSubjectSelected();
        trackingIdEnabled = dialog.isTrackingIdSelected();
        synchronized (this) {
            if (matcherEditor == null) {
                matcherEditor = new FilterMatcherEditor();
                application.getFilteredLedger().addFilter(matcherEditor);
            }
        }
        matcherEditor.setMatcher(new FilterMatcher(sendSubject, trackingId, sendSubjectEnabled, trackingIdEnabled));
        final StrBuilder builder = new StrBuilder();
        if (sendSubjectEnabled) builder.append("Send Subject: ").append(sendSubject);
        if (sendSubjectEnabled && trackingIdEnabled) builder.append("\n");
        if (trackingIdEnabled) builder.append("Tracking ID: ").append(trackingId);
        application.getFrame().setStatusBarFilter(builder.toString());
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(!bySelection || currentSelection.length > 0);
    }

}
