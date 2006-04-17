//:File:    Paste.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Logger;
import rvsnoop.RecordSelection;
import rvsnoop.RvConnection;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * Paste the contents of the system clipboard to the message ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Paste extends AbstractAction {
    
    private static String ERROR_CLIPBOARD_LOST = "The Rendezvous message data was removed from the clipboard before it could be read.";
    
    private static String ERROR_IO = "There was an I/O error whilst reading data from the clipboard.";

    private static String ERROR_RV = "There was a Rendezvous error whilst deserializing the messages.";
    
    private static String INFO_BAD_CLIP_DATA = "The clipboard does not contain Rendezvous message data.";
    
    private static final String ID = "paste";
    
    private static final Logger logger = Logger.getLogger(Paste.class);
    
    private static String NAME = "Paste";

    private static final long serialVersionUID = -814650863486148247L;

    private static String TOOLTIP = "Paste the contents of the clipboard to the message ledger";
    
    public Paste() {
        super(NAME, Icons.PASTE);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable clipboardData = clipboard.getContents(this);
        if (!clipboardData.isDataFlavorSupported(RecordSelection.BYTES_FLAVOUR)) {
            logger.warn(INFO_BAD_CLIP_DATA);
            return;
        }
        try {
            final TibrvMsg[] messages = RecordSelection.unmarshal(clipboardData);
            for (int i = 0, imax = messages.length; i < imax; ++i)
                RvConnection.internalOnMsg(messages[i]);
        } catch (TibrvException e) {
            logger.error(ERROR_RV, e);
        } catch (IOException e) {
            logger.error(ERROR_IO, e);
        } catch (UnsupportedFlavorException e) {
            logger.error(ERROR_CLIPBOARD_LOST, e);
        }
    }

}
