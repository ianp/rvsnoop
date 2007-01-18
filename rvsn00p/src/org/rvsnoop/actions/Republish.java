/*
 * Class:     Republish
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.Connections;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

import rvsnoop.Record;
import rvsnoop.RvConnection;
import rvsnoop.State;
import rvsnoop.ui.UIManager;
import rvsnoop.ui.UIUtils;

/**
 * Republish the currently selected record(s) to a designated transport.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class Republish extends RvSnoopAction implements RecordLedgerSelectionListener {

    private static final long serialVersionUID = -2102299383827763915L;

    static { NLSUtils.internationalize(Republish.class); }

    private static final Log log = LogFactory.getLog(Republish.class);

    public static final String COMMAND = "republish";
    static String ACCELERATOR, ERROR_PUBLISHING, MNEMONIC, NAME, TOOLTIP, QUESTION_CONFIRM;

    private transient Record[] currentSelection;

    public Republish(Application application) {
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
    public void actionPerformed(ActionEvent event) {
        final String question = MessageFormat.format(QUESTION_CONFIRM,
                new Object[] { new Integer(currentSelection.length) });
        // FIXME get the connection list from Application
        final RvConnection[] connections = Connections.getInstance().toArray();
        final String[] connectionNames = new String[connections.length];
        for (int i = 0, imax = connections.length; i < imax; ++i) {
            connectionNames[i] = connections[i].getDescription();
        }
        final String name = (String) JOptionPane.showInputDialog(
                UIManager.INSTANCE.getFrame(),
                question, NAME, JOptionPane.QUESTION_MESSAGE, null,
                connectionNames, connectionNames[0]);
        if (name == null) { return; } // User cancelled republishing.
        final RvConnection connection = connections[ArrayUtils.indexOf(connectionNames, name)];
        if (connection.getState() != State.STARTED) { connection.start(); }
        for (int i = 0, imax = currentSelection.length; i < imax; ++i) {
            final Record record = currentSelection[i];
            try {
                connection.publish(record);
            } catch (Exception e) {
                final String message = MessageFormat.format(ERROR_PUBLISHING,
                        new Object[] { new Long(record.getSequenceNumber()), e.getLocalizedMessage() });
                if (log.isErrorEnabled()) { log.error(message, e); }
                UIUtils.showError(message, e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

}
