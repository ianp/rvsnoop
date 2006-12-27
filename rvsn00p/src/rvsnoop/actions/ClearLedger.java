//:File:    ClearLedger.java
//:Created: Jan 5, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;

import javax.swing.Action;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.SubjectHierarchy;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

/**
 * Clear the contents of the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ClearLedger extends LedgerSelectionAction {

    private static final String ID = "clearLedger";

    private static String NAME = "Clear Ledger";

    private static final long serialVersionUID = -3869401903230005506L;

    private static String TOOLTIP = "Delete all records from the ledger";

    public ClearLedger() {
        super(ID, NAME, Icons.CLEAR_LEDGER);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
    }

    public void actionPerformed(Record[] records) {
        MessageLedger.RECORD_LEDGER.clear();
        SubjectHierarchy.INSTANCE.reset();
        UIManager.INSTANCE.updateStatusLabel();
        UIManager.INSTANCE.clearDetails();
        Record.resetSequence();
    }

}
