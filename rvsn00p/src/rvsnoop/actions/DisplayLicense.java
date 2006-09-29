//:File:    DisplayLicense.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
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
 * Display the license in a browser.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class DisplayLicense extends AbstractAction {

    private static String ERROR_BROWSER = "The default browser could not be opened.";

    private static final String ID = "displayLicense";

    private static final Logger logger = Logger.getLogger(DisplayLicense.class);

    private static String NAME = "Show License";

    private static final long serialVersionUID = -6587535307193015605L;

    private static String TOOLTIP = "Show the license in the default browser";

    public DisplayLicense() {
        super(NAME, Icons.LICENSE);
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
            final File license = new File(docs, "license@suffix@");
            final String prefix = System.getProperty("os.name").startsWith("Win")
                ? "" : "file://";
            BrowserLauncher.openURL(prefix + license.getAbsolutePath());
        } catch (Exception e) {
            logger.error(ERROR_BROWSER, e);
        }
    }

}
