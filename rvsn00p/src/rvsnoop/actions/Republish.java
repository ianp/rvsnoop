//:File:    Republish.java
//:Created: Jan 9, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Record;
import rvsnoop.RvConnection;
import rvsnoop.StringUtils;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIUtils;

import com.tibco.tibrv.TibrvException;

/**
 * Republish the currently selected record(s) to the transport on which they were received.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class Republish extends LedgerSelectionAction {

    private static String CONFIRM = "Really republish {0,choice,1#this|1<these} {0} {0,choice,1#message|1<messages}?";

    private static final String ID = "republish";

    private static String NAME = "Republish";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static String TOOLTIP = "Republish the selected records";

    public Republish() {
        super(ID, NAME, Icons.REPUBLISH);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_DOWN_MASK;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
    }

    public void actionPerformed(List selected) {
        final String question = StringUtils.format(CONFIRM, new Object[] { new Integer(selected.size()) });
        if (!UIUtils.askForConfirmation(question, null)) return;
        for (final Iterator i = selected.iterator(); i.hasNext(); ) {
            final Record record = (Record) i.next();
            try {
                final RvConnection connection = record.getConnection();
                if (connection != null)
                    connection.publish(record);
                else
                    UIUtils.showInformation("No connection is configured to publish the record on.");
            } catch (IllegalStateException e) {
                UIUtils.showError("Could not republish record " + record.getSequenceNumber(), e);
            } catch (TibrvException e) {
                UIUtils.showError("Could not republish record " + record.getSequenceNumber(), e);
            }
        }
    }

}
