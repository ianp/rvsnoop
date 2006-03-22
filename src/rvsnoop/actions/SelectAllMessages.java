//:File:    SelectAllMessages.java
//:Created: Jan 18, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Select all messages in the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class SelectAllMessages extends AbstractAction {

    private static final String ID = "selectAll";

    //private static final Logger logger = Logger.getLogger(SelectAllMessages.class);

    private static String NAME = "Select All Messages";

    private static final long serialVersionUID = -1849656680349222999L;

    private static String TOOLTIP = "Select all messages in the ledger";

    public SelectAllMessages() {
        super(NAME, Icons.SELECT_ALL_MESSAGES);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_M));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JTable ledger = UIManager.INSTANCE.getMessageLedger();
        final ListSelectionModel selectionModel = ledger.getSelectionModel();
        selectionModel.setSelectionInterval(0, ledger.getRowCount() - 1);
    }

}
