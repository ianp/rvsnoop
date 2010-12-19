// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.Record;

/**
 * Clear the contents of the record ledger.
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

    @Override
    public final void actionPerformed(ActionEvent event) {
        application.getLedger().clear();
        application.getSubjectHierarchy().reset();
        Record.resetSequence();
        // FIXME this should be replaced with a listener on the ledger.
        application.getFrame().clearDetails();
    }

}
