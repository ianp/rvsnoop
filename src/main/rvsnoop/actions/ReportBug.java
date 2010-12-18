// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rvsnoop.Logger;
import rvsnoop.BrowserLauncher;

/**
 * Report a bug or request a new feature.
 */
final class ReportBug extends AbstractAction {

    private static String ERROR_BROWSER = "The default browser could not be opened.";

    private static final String ID = "reportBug";

    private static final Logger logger = Logger.getLogger();

    private static String NAME = "Report Bug";

    private static final long serialVersionUID = -4375842586768327328L;

    private static String TOOLTIP = "Report a bug or request a new feature";

    public ReportBug() {
        super(NAME, new ImageIcon("/resources/icons/bug.png"));
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL("http://sourceforge.net/tracker/?group_id=63447");
        } catch (Exception e) {
            logger.error(e, ERROR_BROWSER);
        }
    }

}
