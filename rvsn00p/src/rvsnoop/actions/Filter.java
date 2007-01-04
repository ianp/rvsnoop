/*
 * Class:     Filter
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.ui.FilterDialog;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Filter the messages visible in the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class Filter extends AbstractAction {

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

    private static final long serialVersionUID = 1L;

    public static final String FILTER = "filter";

    public static final String FILTER_BY_SELECTION = "filterBySelection";

    private String sendSubject;
    private String trackingId;

    private boolean sendSubjectEnabled;
    private boolean trackingIdEnabled;

    private FilterMatcherEditor matcherEditor;

    public Filter(String id, String name, String tooltip) {
        super(name);
        putValue(Action.ACTION_COMMAND_KEY, id);
        if (FILTER.equals(id))
            putValue(Action.SMALL_ICON, Icons.FILTER);
        else if (FILTER_BY_SELECTION.equals(id))
            putValue(Action.SMALL_ICON, Icons.FILTER);
        putValue(Action.SHORT_DESCRIPTION, tooltip);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (FILTER_BY_SELECTION.equals(event.getActionCommand())) {
            final Record record = UIManager.INSTANCE.getSelectedRecord();
            if (record != null) {
                sendSubject = record.getSendSubject();
                trackingId = record.getTrackingId();
            }
        }
        final FilterDialog dialog = new FilterDialog(sendSubject, trackingId, sendSubjectEnabled, trackingIdEnabled);
        dialog.setVisible(true);
        if (dialog.isCancelled()) return;
        sendSubject = dialog.getSendSubject();
        trackingId = dialog.getTrackingId();
        sendSubjectEnabled = dialog.isSendSubjectSelected();
        trackingIdEnabled = dialog.isTrackingIdSelected();
        synchronized (this) {
            if (matcherEditor == null) {
                matcherEditor = new FilterMatcherEditor();
                MessageLedger.FILTERED_VIEW.addFilter(matcherEditor);
            }
        }
        matcherEditor.setMatcher(new FilterMatcher(sendSubject, trackingId, sendSubjectEnabled, trackingIdEnabled));
        final StringBuffer buffer = new StringBuffer();
        if (sendSubjectEnabled) buffer.append("Send Subject: ").append(sendSubject);
        if (sendSubjectEnabled && trackingIdEnabled) buffer.append("\n");
        if (trackingIdEnabled) buffer.append("Tracking ID: ").append(trackingId);
        UIManager.INSTANCE.setStatusBarFilter(buffer.toString());
    }

}
