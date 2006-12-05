//:File:    Quit.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import rvsnoop.Logger;
import rvsnoop.RvConnection;
import rvsnoop.ui.Banners;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;
import rvsnoop.ui.UIUtils;

/**
 * Quit the application.
 * <p>
 * Prompt the user for confirmation before quitting.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class Quit extends AbstractAction {

    private static String CONFIRM_QUESTION = "Are you sure that you want to quit?";

    private static final String ID = "quit";

    private static final Logger logger = Logger.getLogger(Quit.class);

    private static String NAME = "Quit";

    private static final long serialVersionUID = -75328795847562924L;

    private static String TOOLTIP = "Close all listeners and exit RvSnoop";

    public Quit() {
        super(NAME, Icons.QUIT);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JFrame frame = UIManager.INSTANCE.getFrame();
        if (!UIUtils.askForConfirmation(CONFIRM_QUESTION, Banners.QUIT))
            return;
        frame.dispose();
        try {
            RvConnection.shutdown();
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Problem whilst shutting down.", e);
        } finally {
            System.exit(0);
        }
    }

}
