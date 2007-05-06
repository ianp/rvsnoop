/*
 * Class:     SelectAllRecords
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.ui.UIManager;

/**
 * Select all records in the visible ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class SelectAllRecords extends RvSnoopAction {

    static { NLSUtils.internationalize(SelectAllRecords.class); }

    private static final long serialVersionUID = 4661382692213813779L;

    public static final String COMMAND = "selectAllRecords";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    public SelectAllRecords(Application application) {
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
        final JTable ledger = UIManager.INSTANCE.getRecordLedger();
        final ListSelectionModel selectionModel = ledger.getSelectionModel();
        selectionModel.setSelectionInterval(0, ledger.getRowCount() - 1);
    }

}
