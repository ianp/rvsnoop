/*
 * Class:     ShowAllColumns
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.rvsnoop.RecordLedgerFormat;

import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Show all columns in the message ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ShowAllColumns extends AbstractAction {

    private static final String ID = "showAllColumns";

    //private static final Logger logger = Logger.getLogger(ShowAllColumns.class);

    private static String NAME = "Show All Columns";

    private static final long serialVersionUID = -8088036149267491396L;

    private static String TOOLTIP = "Show all columns in the message ledger";

    public ShowAllColumns() {
        super(NAME, Icons.FILTER_COLUMNS);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_DOWN_MASK;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        UIManager.INSTANCE.getMessageLedger().getTableFormat().setColumns(RecordLedgerFormat.ALL_COLUMNS);
    }

}
