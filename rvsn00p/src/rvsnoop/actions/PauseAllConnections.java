//:File:    PauseAllconnections.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rvsnoop.RvConnection;
import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;

/**
 * Pause all of the connections.
 * <p>
 * This works by telling the local queue to reject all new messages, it also
 * sets all of the connections to paused.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class PauseAllConnections extends AbstractAction {

    private static final String ID = "pauseAllConnections";

    private static final Log log = LogFactory.getLog(PauseAllConnections.class);

    private static String NAME_PAUSE = "Pause All";

    private static String NAME_RESUME = "Resume All";

    private static final long serialVersionUID = -986608950126010165L;

    private static String TOOLTIP_PAUSE = "Pause all Rendezvous connections";

    private static String TOOLTIP_RESUME = "Resume all paused Rendezvous connections";

    private boolean isPaused = false;

    public PauseAllConnections() {
        super(NAME_PAUSE, Icons.PAUSE);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP_PAUSE);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public synchronized void actionPerformed(ActionEvent event) {
        if (isPaused) {
            try {
                RvConnection.resumeQueue();
                putValue(Action.NAME, NAME_PAUSE);
                putValue(Action.SHORT_DESCRIPTION, TOOLTIP_PAUSE);
                putValue(Action.SMALL_ICON, Icons.PAUSE);
                isPaused = false;
                if (log.isInfoEnabled()) {
                    log.info("All connections have now been resumed.");
                }
            } catch (TibrvException e) {
                if (log.isErrorEnabled()) {
                    log.error("There was a problem resuming the connections.", e);
                }
            }
        } else {
            try {
                RvConnection.pauseQueue();
                putValue(Action.NAME, NAME_RESUME);
                putValue(Action.SHORT_DESCRIPTION, TOOLTIP_RESUME);
                putValue(Action.SMALL_ICON, Icons.RESUME);
                isPaused = true;
                if (log.isInfoEnabled()) {
                    log.info("All connections have now been paused.");
                }
            } catch (TibrvException e) {
                if (log.isErrorEnabled()) {
                    log.error("There was a problem pausing the connections.", e);
                }
            }
        }
    }

}
