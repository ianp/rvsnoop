//:File:    EditRecordTypes.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsnoop.ui.Icons;
import rvsnoop.ui.RecordTypesDialog;
import rvsnoop.ui.UIManager;

/**
 * Display a dialog to allow editing of the record types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
final class EditRecordTypes extends AbstractAction {

    private static final String ID = "editRecordTypes";

    static String NAME = "Edit Record Types";

    static final long serialVersionUID = 9150832053929306731L;

    static String TOOLTIP = "Customize the record types that are used";

    public EditRecordTypes() {
        super(NAME, Icons.HELP);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        RecordTypesDialog dialog = new RecordTypesDialog();
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
//        // Now loop through all the panels and force the types to be created.
//        Iterator i = dialog.getRecordTypePanels().iterator();
//        while (i.hasNext())
//            ((RecordTypePanel) i.next()).getRecordType();
        // Now force the table to repaint.
        UIManager.INSTANCE.getMessageLedger().repaint();
    }

}
