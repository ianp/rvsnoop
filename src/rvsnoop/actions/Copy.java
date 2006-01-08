//:File:    Copy.java
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

import rvsnoop.Logger;
import rvsnoop.RecordSelection;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;

/**
 * Copy the currently selected record(s) to the system clipboard.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Copy extends LedgerSelectionAction {
    
    static String ERROR_IO = "There was an I/O error whilst writing data to the clipboard.";
    
    static String ERROR_RV = "There was a Rendezvous error whilst serializing the messages.";
    
    private static final String ID = "copy";
    
    private static final Logger logger = Logger.getLogger(Copy.class);

    static String NAME = "Copy";
    
    private static final long serialVersionUID = 7395491526593830048L;

    static String TOOLTIP = "Copy the selected records to the clipboard";
    
    public Copy() {
        super(ID, NAME, Icons.COPY);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
    }

    public void actionPerformed(List selected) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            final RecordSelection selection = new RecordSelection(selected);
            clipboard.setContents(selection, selection);
        } catch (TibrvException e) {
            logger.error(ERROR_RV, e);
        } catch (IOException e) {
            logger.error(ERROR_IO, e);
        }
    }

}
