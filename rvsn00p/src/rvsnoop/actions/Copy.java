/*
 * Class:     Copy
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.ui.Icons;

/**
 * Copy the currently selected record(s) to the system clipboard.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Copy extends LedgerSelectionAction {

    private static final String ID = "copy";

    private static String NAME = "Copy";

    private static final long serialVersionUID = 7395491526593830048L;

    private static String TOOLTIP = "Copy the selected records to the clipboard";

    public Copy() {
        super(ID, NAME, Icons.COPY);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
    }

    public void actionPerformed(Record[] records) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final RecordSelection selection = new RecordSelection(records);
        clipboard.setContents(selection, selection);
    }

}
