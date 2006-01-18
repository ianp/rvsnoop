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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import rvsnoop.MessageLedger;
import rvsnoop.ui.UIManager;
import rvsnoop.ui.UIUtils;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * An action that operates on the currently selected records in the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
abstract class LedgerSelectionAction extends AbstractAction implements ListSelectionListener {
    
    private static String INFO_NOTHING_SELECTED = "No selection to operate on!";
    
    LedgerSelectionAction(String id, String name, Icon icon) {
        super(name, icon);
        putValue(Action.ACTION_COMMAND_KEY, id);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent event) {
        final UIManager ui = UIManager.INSTANCE;
        final int[] indexes = ui.getSelectedRecords();
        if (indexes == null || indexes.length == 0) {
            UIUtils.showInformation(INFO_NOTHING_SELECTED);
            return;
        }
        // First, make a local reference to the selected records.
        final EventList list = MessageLedger.INSTANCE.getEventList();
        final Lock lock = list.getReadWriteLock().readLock();
        try {
            lock.lock();
            final int[] rows = UIManager.INSTANCE.getSelectedRecords();
            final List selected = new ArrayList(rows.length);
            for (int i = 0, imax = rows.length; i < imax; ++i)
                selected.add(list.get(rows[i]));
            // Now we can take our time working on the selection.
            actionPerformed(selected);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Perform the action.
     * 
     * @param selected The selected records, elements can be cast to {@link rvsnoop.Record} safely.
     */
    protected abstract void actionPerformed(List selected);

    public void valueChanged(ListSelectionEvent e) {
        setEnabled(UIManager.INSTANCE.getSelectedRecord() != null);
    }

}
