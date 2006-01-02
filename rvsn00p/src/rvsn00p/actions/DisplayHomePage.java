//:File:    DisplayHomePage.java
//:Created: Dec 24, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsn00p.ui.Icons;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.BrowserLauncher;

/**
 * Display the RvSn00p home page in a browser.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class DisplayHomePage extends AbstractAction {

    static String ERROR_BROWSER = "The default browser could not be opened.";

    public static final String ID = "displayHomePage";
    
    static String NAME = "RvSn00p Web Site";
    
    private static final long serialVersionUID = -6587535307193015605L;

    static String TOOLTIP = "Show the web site in the default browser";

    public DisplayHomePage() {
        super(NAME, Icons.WEB);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL("http://rvsn00p.sourceforge.net");
        } catch (Exception e) {
            UIUtils.showError(ERROR_BROWSER, e);
        }
    }

}
