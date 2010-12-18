// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.MainFrame;

import rvsnoop.Record;
import rvsnoop.RvConnection;
import rvsnoop.State;
import rvsnoop.ui.UIUtils;

/**
 * Republish the currently selected record(s) to a designated transport.
 */
public final class Republish extends RvSnoopAction implements RecordLedgerSelectionListener {

    private static final long serialVersionUID = -2102299383827763915L;

    static { NLSUtils.internationalize(Republish.class); }

    private static final Logger logger = Logger.getLogger();

    public static final String COMMAND = "republish";
    static String ACCELERATOR, ERROR_PUBLISHING, MNEMONIC, NAME, TOOLTIP, QUESTION_CONFIRM;

    public static int indexOf(Object[] array, Object objectToFind) {
        if (array == null) { return -1; }
        if (objectToFind == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) { return i; }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (objectToFind.equals(array[i])) { return i; }
            }
        }
        return -1;
    }

    private transient Record[] currentSelection;

    public Republish(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final String question = MessageFormat.format(QUESTION_CONFIRM, currentSelection.length);
        // FIXME get the connection list from Application
        final RvConnection[] connections = application.getConnections().toArray();
        final String[] connectionNames = new String[connections.length];
        for (int i = 0, imax = connections.length; i < imax; ++i) {
            connectionNames[i] = connections[i].getDescription();
        }
        final String name = (String) JOptionPane.showInputDialog(
                MainFrame.INSTANCE, question, NAME, JOptionPane.QUESTION_MESSAGE,
                null, connectionNames, connectionNames[0]);
        if (name == null) { return; } // User cancelled republishing.
        final RvConnection connection = connections[indexOf(connectionNames, name)];
        if (connection.getState() != State.STARTED) { connection.start(); }
        for (int i = 0, imax = currentSelection.length; i < imax; ++i) {
            final Record record = currentSelection[i];
            try {
                connection.publish(record);
            } catch (Exception e) {
                final String message = MessageFormat.format(ERROR_PUBLISHING, record.getSequenceNumber(), e.getLocalizedMessage());
                logger.error(e, message);
                UIUtils.showError(message, e);
            }
        }
    }

    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

}
