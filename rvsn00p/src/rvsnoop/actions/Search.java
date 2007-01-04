/*
 * Class:     Search
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.ui.Icons;
import rvsnoop.ui.SearchDialog;
import rvsnoop.ui.UIManager;
import ca.odell.glazedlists.matchers.Matcher;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;
import com.tibco.tibrv.TibrvXml;

/**
 * Search for text in the message bodies.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Search extends AbstractAction {

    private class Searcher implements Matcher {
        private final boolean fieldData;
        private final boolean fieldNames;
        private final boolean replySubject;
        private final boolean sendSubject;
        private final boolean trackingId;
        private final String searchText;
        Searcher(boolean fieldData, boolean fieldNames, boolean replySubject,
                 boolean sendSubject, boolean trackingId, String searchText) {
            this.fieldData = fieldData;
            this.fieldNames = fieldNames;
            this.replySubject = replySubject;
            this.sendSubject = sendSubject;
            this.trackingId = trackingId;
            this.searchText = searchText;
        }

        private boolean isTextInFieldData(String text, TibrvMsgField field) throws TibrvException {
            if (fieldNames && field.name.indexOf(text) >= 0) return true;
            switch (field.type) {
            case TibrvMsg.MSG:
                return isTextInMessageData(text, (TibrvMsg) field.data);
            case TibrvMsg.STRING:
                return ((String) field.data).indexOf(text) >= 0;
            case TibrvMsg.XML:
                // XXX: Should we do something here to sniff the correct encoding from the bytes?
                return isTextInXmlData(text.getBytes(), ((TibrvXml) field.data).getBytes());
            default:
                return false;
            }
        }

        /**
         * Search all of the text in a message for a given string.
         * <p>
         * This will search all fields which contain strings, and recursively search
         * all nested messages.
         *
         * @param text The text to search for.
         * @param message The message to search.
         * @return <code>true</code> if the text was matched, <code>false</code>
         *         otherwise.
         * @throws TibrvException
         */
        private boolean isTextInMessageData(String text, TibrvMsg message) throws TibrvException {
            for (int i = 0, imax = message.getNumFields(); i < imax; ++i)
                if (isTextInFieldData(text, message.getFieldByIndex(i)))
                    return true;
            return false;
        }

        private boolean isTextInXmlData(byte[] text, byte[] xml) {
            final byte firstByte = text[0];
            SCAN_XML:
            for (int i = 0, imax = xml.length; i < imax; ++i) {
                if (xml[i] != firstByte) continue;
                if (text.length == 1) return true; // Guard for single byte text.
                if (++i == xml.length) return false; // Guard for end of XML.
                // OK, now look for the rest of the text...
                for (int j = 1, jmax = text.length; j < jmax; ) {
                    if (xml[i] != text[j]) continue SCAN_XML;
                    if (++i == xml.length) return false; // Guard for end of XML.
                    if (++j == text.length) return true;
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.matchers.Matcher#matches(java.lang.Object)
         */
        public boolean matches(Object item) {
            if (!(item instanceof Record)) { return false; }
            final Record record = (Record) item;
            if ((fieldData || fieldNames) && matchMessage(record)) return true;
            if (sendSubject && matchSendSubject(record)) return true;
            if (replySubject && matchReplySubject(record)) return true;
            return trackingId && matchTrackingId(record);
        }

        private boolean matchMessage(Record record) {
            try {
                return isTextInMessageData(searchText, record.getMessage());
            } catch (TibrvException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error reading message field.", e);
                }
                return false;
            }
        }

        private boolean matchReplySubject(Record record) {
            final String replySubject = record.getReplySubject();
            return replySubject != null && replySubject.indexOf(searchText) >= 0;
        }

        private boolean matchSendSubject(Record record) {
            final String sendSubject = record.getSendSubject();
            return sendSubject != null && sendSubject.indexOf(searchText) >= 0;
        }

        private boolean matchTrackingId(Record record) {
            final String trackingId = record.getTrackingId();
            return trackingId != null && trackingId.indexOf(searchText) >= 0;
        }

    }

    private static final Log log = LogFactory.getLog(Search.class);

    public static final String SEARCH = "search";

    public static final String SEARCH_AGAIN = "searchAgain";

    private static final long serialVersionUID = -5833838900255559506L;

    private String searchText = "Enter your search text here";

    private boolean trackingId;
    private boolean fieldData = true;
    private boolean fieldNames;
    private boolean sendSubject;
    private boolean replySubject;

    public Search(String id, String name, String tooltip, int accel) {
        super(name);
        putValue(Action.ACTION_COMMAND_KEY, id);
        if (SEARCH.equals(id))
            putValue(Action.SMALL_ICON, Icons.SEARCH);
        else if (SEARCH_AGAIN.equals(id))
            putValue(Action.SMALL_ICON, Icons.SEARCH_AGAIN);
        if (accel != 0) {
            final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel, mask));
        }
        putValue(Action.SHORT_DESCRIPTION, tooltip);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (SEARCH.equals(event.getActionCommand()) && !showDialog()) return;
        if (searchText == null || (searchText = searchText.trim()).length() == 0) return;
        final Searcher searcher = new Searcher(fieldData, fieldNames,
                replySubject, sendSubject, trackingId, searchText);
        final int[] indices = MessageLedger.FILTERED_VIEW.findAllIndices(searcher);
        if (indices.length == 0) {
            JOptionPane.showMessageDialog(UIManager.INSTANCE.getFrame(), "Nothing matched.");
        } else {
            // TODO: Display SearchResultsWindow
        }
    }

    private boolean showDialog() {
        final SearchDialog dialog = new SearchDialog(searchText, fieldData, fieldNames, sendSubject, replySubject, trackingId);
        dialog.setVisible(true);
        if (dialog.isCancelled()) return false;
        searchText = dialog.getSearchText();
        fieldData = dialog.isFieldDataSelected();
        fieldNames = dialog.isFieldNamesSelected();
        sendSubject = dialog.isSendSubjectSelected();
        replySubject = dialog.isReplySubjectSelected();
        trackingId = dialog.isTrackingIdSelected();
        return true;
    }

}
