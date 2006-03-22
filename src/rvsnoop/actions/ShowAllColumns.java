//:File:    ShowAllColumns.java
//:Created: Jan 18, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import ca.odell.glazedlists.swing.EventTableModel;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.MessageLedgerFormat;
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
        MessageLedgerFormat.INSTANCE.showAllColumns();
        final EventTableModel model = (EventTableModel) UIManager.INSTANCE.getMessageLedger().getModel();
        model.setTableFormat(MessageLedgerFormat.INSTANCE);
    }

}
