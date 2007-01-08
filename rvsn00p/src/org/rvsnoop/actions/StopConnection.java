/*
 * Class:     StopConnection
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.RvConnection;
import rvsnoop.State;

/**
 * Stop a connection.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class StopConnection extends RvSnoopAction implements PropertyChangeListener {

    static { NLSUtils.internationalize(StopConnection.class); }

    private static final long serialVersionUID = -3479268317500763018L;

    public static final String COMMAND = "stopConnection";
    static String MNEMONIC, NAME, TOOLTIP;

    private final RvConnection connection;

    public StopConnection(Application application, RvConnection connection) {
        super(NAME, application);
        this.connection = connection;
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        connection.addPropertyChangeListener(State.PROP_STATE, this);
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.actions.RvSnoopAction#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        connection.stop();
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e) {
        if (!State.PROP_STATE.equals(e.getPropertyName())) { return; }
        setEnabled(!State.STOPPED.equals(e.getNewValue()));
    }

}
