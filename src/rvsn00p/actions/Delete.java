//:File:    Delete.java
//:Created: Dec 28, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsn00p.ui.Icons;
import rvsn00p.ui.UIUtils;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Delete the currently selected record(s) from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Delete extends AbstractAction {
    
    private static final String ID = "delete";

    static String INFO_NOTHING_SELECTED = "You must select at least one message to delete.";
    
    static String NAME = "Delete";

    /**
     * 
     */
    private static final long serialVersionUID = -4082252661195745679L;
    
    static String TOOLTIP = "Delete the currently selected record(s) from the ledger";
    
    public Delete() {
        super(NAME, Icons.DELETE);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final RvSnooperGUI ui = RvSnooperGUI.getInstance();
        final int[] indexes = ui.getSelectedRecords();
        if (indexes == null || indexes.length == 0) {
            UIUtils.showInformation(INFO_NOTHING_SELECTED);
            return;
        }
        // First, make a local reference to the selected records.
        final List records = ui.getFilteredRecords();
        final List selected = new ArrayList(indexes.length);
        for (int i = 0, imax = indexes.length; i < imax; ++i)
            selected.add(records.get(indexes[i]));
        ui.removeAll(selected);
    }

}
