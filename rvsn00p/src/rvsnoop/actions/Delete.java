//:File:    Delete.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.ui.Icons;

/**
 * Delete the currently selected record(s) from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Delete extends LedgerSelectionAction {
    
    private static final String ID = "delete";
    
    static String NAME = "Delete";

    private static final long serialVersionUID = -4082252661195745678L;
    
    static String TOOLTIP = "Delete the currently selected record(s) from the ledger";
    
    public Delete() {
        super(ID, NAME, Icons.DELETE);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, mask));
    }

    public void actionPerformed(List selected) {
        RvSnooperGUI.getInstance().removeAll(selected);
    }

}
