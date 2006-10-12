//:File:    AddConnection.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Connections;
import rvsnoop.RecentConnections;
import rvsnoop.RvConnection;
import rvsnoop.ui.Icons;
import rvsnoop.ui.RvConnectionDialog;

/**
 * Create a new connection instance.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class AddConnection extends AbstractAction {

    private static final String ID = "addConnection";

    private static String NAME = "Add Connection...";

    private static final long serialVersionUID = 7508828375114300486L;

    private static String TOOLTIP = "Create a new Rendezvous connection";

    public AddConnection() {
        super(NAME, Icons.ADD_CONNECTION);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        RvConnection conn = RecentConnections.getInstance().getLastConnection();
        final RvConnectionDialog dialog = new RvConnectionDialog(conn);
        dialog.setVisible(true);
        if (dialog.isCancelled()) return;
        conn = dialog.getConnection();
        Connections.getInstance().add(conn);
        conn.start();
    }

}
