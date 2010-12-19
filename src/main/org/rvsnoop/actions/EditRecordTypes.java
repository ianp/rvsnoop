// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.ui.RecordTypesDialog;

/**
 * Display a dialog to allow editing of the record types.
 */
public final class EditRecordTypes extends RvSnoopAction {

    static { NLSUtils.internationalize(EditRecordTypes.class); }

    private static final long serialVersionUID = 2918397665675386954L;

    public static final String COMMAND = "editRecordTypes";
    static String MNEMONIC, NAME, TOOLTIP;

    public EditRecordTypes(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final RecordTypesDialog dialog =
            new RecordTypesDialog( application.getFrame(), application.getRecordTypes());
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
        application.getLedgerTable().repaint();
    }

}
