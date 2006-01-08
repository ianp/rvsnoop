//:File:    ClearLedger.java
//:Created: Jan 5, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.Record;
import rvsnoop.SubjectHierarchy;
import rvsnoop.ui.Icons;

/**
 * Clear the contents of the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ClearLedger extends LedgerSelectionAction {
    
    private static final String ID = "clearLedger";

    static String NAME = "Clear Ledger";
    
    private static final long serialVersionUID = -3869401903230005506L;

    static String TOOLTIP = "Delete all records from the ledger";
    
    public ClearLedger() {
        super(ID, NAME, Icons.CLEAR_LEDGER);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
    }

    public void actionPerformed(List selected) {
        RvSnooperGUI.getInstance().getMessageLedger().clearLogRecords();
        SubjectHierarchy.INSTANCE.reset();
        RvSnooperGUI.getInstance().updateStatusLabel();
        RvSnooperGUI.getInstance().clearDetails();
        Record.resetSequence();
    }

}
