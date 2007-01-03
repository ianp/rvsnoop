//:File:    LedgerSelectionAction.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import rvsnoop.Record;
import rvsnoop.ui.UIManager;
import rvsnoop.ui.UIUtils;

/**
 * An action that operates on the currently selected records in the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public abstract class LedgerSelectionAction extends AbstractAction implements ListSelectionListener {

    private static String INFO_NOTHING_SELECTED = "No selection to operate on!";

    public LedgerSelectionAction(String id, String name, Icon icon) {
        super(name, icon);
        putValue(Action.ACTION_COMMAND_KEY, id);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent event) {
        final UIManager ui = UIManager.INSTANCE;
        final Record[] records = ui.getSelectedRecords();
        if (records != null && records.length > 0)
            actionPerformed(records);
        else
            UIUtils.showInformation(INFO_NOTHING_SELECTED);
    }

    /**
     * Perform the action.
     *
     * @param records The selected records, elements can be cast to {@link rvsnoop.Record} safely.
     */
    protected abstract void actionPerformed(Record[] records);

    public void valueChanged(ListSelectionEvent e) {
        setEnabled(UIManager.INSTANCE.getSelectedRecord() != null);
    }

}
