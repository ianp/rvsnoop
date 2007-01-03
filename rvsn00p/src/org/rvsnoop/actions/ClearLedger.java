/*
 * Class:     ClearLedger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;

import org.rvsnoop.Application;

import rvsnoop.Record;
import rvsnoop.SubjectHierarchy;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Clear the contents of the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class ClearLedger extends RvSnoopAction {

    private static final long serialVersionUID = -3552599952765687823L;

    public static final String COMMAND = "clearLedger";
    private static String NAME = "Clear Ledger";
    private static String TOOLTIP = "Delete all records from the ledger";

    public ClearLedger(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putValue(Action.SMALL_ICON, Icons.createSmallIcon(COMMAND));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent event) {
        application.getLedger().clear();
        // FIXME these should all be replaced with listeners on the ledger.
        SubjectHierarchy.INSTANCE.reset();
        UIManager.INSTANCE.updateStatusLabel();
        UIManager.INSTANCE.clearDetails();
        Record.resetSequence();
    }

}
