//:File:    Search.java
//:Created: Dec 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.Logger;
import rvsnoop.Record;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * Search for text in the message bodies.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Search extends AbstractAction {

    private static final Logger logger = Logger.getLogger(Search.class);
    
    public static final String SEARCH = "search";

    public static final String SEARCH_AGAIN = "searchAgain";
    
    private static final long serialVersionUID = -5833838900255559506L;
    
    private String text = "";
    
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
        if (SEARCH.equals(event.getActionCommand()))
            text = JOptionPane.showInputDialog(RvSnooperGUI.getFrame(), "Text to search for", text);
        if (text == null || text.trim().length() == 0) return;
        final RvSnooperGUI ui = RvSnooperGUI.getInstance();
        final int start = ui.getFirstSelectedRow();
        if (text == null || text.length() == 0) return;
        ui.selectRow(search(ui.getFilteredRecords(), start < 0 ? 0 : start + 1, text));
    }

    private boolean isTextInFieldData(String text, TibrvMsgField field) throws TibrvException {
        switch (field.type) {
        case TibrvMsg.MSG:
            return isTextInMessageData(text, (TibrvMsg) field.data);
        case TibrvMsg.STRING:
        case TibrvMsg.XML:
            return ((String) field.data).equalsIgnoreCase(text);
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
    public boolean isTextInMessageData(String text, TibrvMsg message) throws TibrvException {
        for (int i = 0, imax = message.getNumFields(); i < imax; ++i)
            if (isTextInFieldData(text, message.getField(i)))
                return true;
        return false;
    }

    private boolean matches(TibrvMsg message, String text, String trackingId) {
        try {
            return isTextInMessageData(text, message)
                || trackingId.indexOf(text.toLowerCase(Locale.ENGLISH)) >= 0;
        } catch (TibrvException e) {
            logger.error("Error reading field", e);
            return false;
        }
    }

    private int search(final List records, final int start, String text) {
        final String trackingId = RvSnooperGUI.getTrackingIdTextFilter();
        for (int i = start, imax = records.size(); i < imax; ++i)
            if (matches(((Record) records.get(i)).getMessage(), text, trackingId))
                return i;
        // If we get here without matching, wrap to the start of the list.
        for (int i = 0; i < start; ++i)
            if (matches(((Record) records.get(i)).getMessage(), text, trackingId))
                return i;
        // Return -1 to signal nothing matched.
        return -1;
    }

}
