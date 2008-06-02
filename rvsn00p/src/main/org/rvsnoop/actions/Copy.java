/*
 * Class:     Copy
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

import rvsnoop.Record;
import rvsnoop.RecordSelection;

/**
 * Copy the currently selected record(s) to the system clipboard.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Copy extends RvSnoopAction implements RecordLedgerSelectionListener {

    static { NLSUtils.internationalize(Copy.class); }

    private static final long serialVersionUID = 7395491526593830048L;

    public static final String COMMAND = "copy";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    private transient Record[] currentSelection;

    public Copy(Application application) {
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
    @Override
    public void actionPerformed(ActionEvent e) {
        final RecordSelection sel = new RecordSelection(currentSelection);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

}
