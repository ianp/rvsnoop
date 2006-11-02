//:File:    Cut.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.ui.Icons;

/**
 * Copy the currently selected record(s) to the system clipboard then remove
 * them from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Cut extends LedgerSelectionAction {

    private static final String ID = "cut";

    private static String NAME = "Cut";

    private static final long serialVersionUID = 795156697514723501L;

    private static String TOOLTIP = "Delete the selected records but place copies on the clipboard";

    public Cut() {
        super(ID, NAME, Icons.CUT);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_X));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
    }

    public void actionPerformed(Record[] records) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final RecordSelection selection = new RecordSelection(records);
        clipboard.setContents(selection, selection);
        MessageLedger.INSTANCE.removeAll(Arrays.asList(records));
    }

}
