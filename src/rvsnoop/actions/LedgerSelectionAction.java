//:File:    LedgerSelectionAction.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.ui.UIUtils;

/**
 * An action that operates on the currently selected records in the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
abstract class LedgerSelectionAction extends AbstractAction {
    
    static String INFO_NOTHING_SELECTED = "No selection to operate on!";
    
    protected LedgerSelectionAction(String id, String name, Icon icon) {
        super(name, icon);
        putValue(Action.ACTION_COMMAND_KEY, id);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent event) {
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
        // Now we can take our time working on the selection.
        actionPerformed(selected);
    }

    /**
     * Perform the action.
     * 
     * @param selected The selected records, elements can be cast to {@link rvsnoop.Record} safely.
     */
    protected abstract void actionPerformed(List selected);

}
