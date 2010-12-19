// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.rvsnoop.Logger;
import rvsnoop.RvConnection;

import com.tibco.tibrv.TibrvException;

/**
 * Pause all of the connections.
 * <p>
 * This works by telling the local queue to reject all new messages, it also
 * sets all of the connections to paused.
 */
public final class PauseAllConnections extends AbstractAction {

    public static final Icon PAUSE = new ImageIcon("/resources/icons/pause.png");
    public static final Icon RESUME = new ImageIcon("/resources/icons/resume.png");

    public static final String COMMAND = "pauseAllConnections";

    private static final Logger logger = Logger.getLogger();

    private static String NAME_PAUSE = "Pause All";

    private static String NAME_RESUME = "Resume All";

    private static final long serialVersionUID = -986608950126010165L;

    private static String TOOLTIP_PAUSE = "Pause all Rendezvous connections";

    private static String TOOLTIP_RESUME = "Resume all paused Rendezvous connections";

    private boolean isPaused = false;

    public PauseAllConnections() {
        super(NAME_PAUSE, PAUSE);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP_PAUSE);
    }

    public synchronized void actionPerformed(ActionEvent event) {
        if (isPaused) {
            try {
                RvConnection.resumeQueue();
                putValue(Action.NAME, NAME_PAUSE);
                putValue(Action.SHORT_DESCRIPTION, TOOLTIP_PAUSE);
                putValue(Action.SMALL_ICON, PAUSE);
                isPaused = false;
                logger.info("All connections have now been resumed.");
            } catch (TibrvException e) {
                logger.error(e, "There was a problem resuming the connections.");
            }
        } else {
            try {
                RvConnection.pauseQueue();
                putValue(Action.NAME, NAME_RESUME);
                putValue(Action.SHORT_DESCRIPTION, TOOLTIP_RESUME);
                putValue(Action.SMALL_ICON, RESUME);
                isPaused = true;
                logger.info("All connections have now been paused.");
            } catch (TibrvException e) {
                logger.error(e, "There was a problem pausing the connections.");
            }
        }
    }

}
