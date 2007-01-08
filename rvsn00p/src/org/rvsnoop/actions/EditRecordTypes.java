/*
 * Class:     EditRecordTypes
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

import rvsnoop.ui.RecordTypesDialog;

/**
 * Display a dialog to allow editing of the record types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final RecordTypesDialog dialog = new RecordTypesDialog();
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
        application.getLedgerTable().repaint();
    }

}
