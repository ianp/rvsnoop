/*
 * Class:     Cut
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
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

/**
 * Copy the currently selected record(s) to the system clipboard then remove
 * them from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Cut extends RvSnoopAction implements RecordLedgerSelectionListener {

    static { NLSUtils.internationalize(Cut.class); }

    private static final long serialVersionUID = 795156697514723501L;

    public static final String COMMAND = "cut";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    public Cut(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.RvSnoopAction#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        application.getAction(Copy.COMMAND).actionPerformed(e);
        application.getAction(Delete.COMMAND).actionPerformed(e);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        setEnabled(event.getSelectedRecords().length > 0);
    }

}
