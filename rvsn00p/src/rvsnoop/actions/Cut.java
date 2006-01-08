//:File:    Cut.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.Logger;
import rvsnoop.RecordSelection;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;

/**
 * Copy the currently selected record(s) to the system clipboard then remove them from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Cut extends LedgerSelectionAction {
    
    static String ERROR_IO = "There was an I/O error whilst writing data to the clipboard.";

    static String ERROR_RV = "There was a Rendezvous error whilst serializing the messages.";
    
    private static final String ID = "cut";
    
    private static final Logger logger = Logger.getLogger(Cut.class);
    
    static String NAME = "Cut";

    private static final long serialVersionUID = 795156697514723501L;

    static String TOOLTIP = "Delete the selected records but place copies on the clipboard";
    
    public Cut() {
        super(ID, NAME, Icons.CUT);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
    }

    public void actionPerformed(List selected) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            final RecordSelection selection = new RecordSelection(selected);
            clipboard.setContents(selection, selection);
            RvSnooperGUI.getInstance().removeAll(selected);
        } catch (TibrvException e) {
            logger.error(ERROR_RV, e);
        } catch (IOException e) {
            logger.error(ERROR_IO, e);
        }
    }

}
