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
 * Display the RvSnoop home page in a browser.
 */
final class DisplayHomePage extends AbstractAction {

    private static String ERROR_BROWSER = "The default browser could not be opened.";

    private static final String ID = "displayHomePage";

    private static final Logger logger = Logger.getLogger();

    private static String NAME = "Show Web Site";

    private static final long serialVersionUID = -6587535307193015605L;

    private static String TOOLTIP = "Show the web site in the default browser";

    public DisplayHomePage() {
        super(NAME, new ImageIcon("/resources/icons/web.png"));
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL("http://rvsn00p.sourceforge.net");
        } catch (Exception e) {
            logger.error(e, ERROR_BROWSER);
        }
    }

}
