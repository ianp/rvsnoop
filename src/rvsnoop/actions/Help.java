//:File:    Help.java
//:Created: Dec 29, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsnoop.BrowserLauncher;
import rvsnoop.Logger;
import rvsnoop.ui.Icons;

/**
 * Display the local help files in a browser.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class Help extends AbstractAction {

    static String ERROR_BROWSER = "The default browser could not be opened.";

    public static final String ID = "help";
    
    private static final Logger logger = Logger.getLogger(Help.class);
    
    static String NAME = "Help Contents";

    private static final long serialVersionUID = 7014627089362478531L;

    static String TOOLTIP = "Show the help files in the default browser";

    public Help() {
        super(NAME, Icons.HELP);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            final File home = new File(System.getProperty("rvsnoop.home"));
            final File docs = new File(home, "doc");
            final File help = new File(docs, "index.html");
            final String prefix = System.getProperty("os.name").startsWith("Win")
                ? "" : "file://";
            BrowserLauncher.openURL(prefix + help.getAbsolutePath());
        } catch (Exception e) {
            logger.error(ERROR_BROWSER, e);
        }
    }

}
