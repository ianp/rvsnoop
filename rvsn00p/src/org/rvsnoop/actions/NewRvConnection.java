/*
 * Class:     NewRvConnection
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.UserPreferences;

import rvsnoop.RvConnection;
import rvsnoop.ui.RvConnectionDialog;

/**
 * Create a new connection instance.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class NewRvConnection extends RvSnoopAction {

    static { NLSUtils.internationalize(NewRvConnection.class); }

    private static final long serialVersionUID = 141276655633148673L;

    public static final String COMMAND = "newRvConnection";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    public NewRvConnection(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        RvConnection conn = UserPreferences.getInstance().getMostRecentConnection();
        final RvConnectionDialog dialog = new RvConnectionDialog(conn);
        dialog.setVisible(true);
        if (dialog.isCancelled()) { return; }
        conn = dialog.getConnection();
        // TODO add a "start connection" checkbox to the dialog
        if (application.getConnections().add(conn)) { conn.start(); }
    }

}
