/*
 * Class:     ClearLedger
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

import rvsnoop.Record;
import rvsnoop.ui.UIManager;

/**
 * Clear the contents of the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class ClearLedger extends RvSnoopAction {

    static { NLSUtils.internationalize(ClearLedger.class); }

    private static final long serialVersionUID = -3552599952765687823L;

    public static final String COMMAND = "clearLedger";
    static String MNEMONIC, NAME, TOOLTIP;

    public ClearLedger(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public final void actionPerformed(ActionEvent event) {
        application.getLedger().clear();
        application.getSubjectHierarchy().reset();
        Record.resetSequence();
        // FIXME this should be replaced with a listener on the ledger.
        UIManager.INSTANCE.clearDetails();
    }

}
