//:File:    DisplayWishList.java
//:Created: Dec 24, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsn00p.ui.UIUtils;
import rvsn00p.util.BrowserLauncher;

/**
 * Display a wish list in a browser.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class DisplayWishList extends AbstractAction {

    static String ERROR_BROWSER = "The default browser could not be opened.";

    public static final String ID = "displayWishList";
    
    private static final long serialVersionUID = 9191769758987689387L;

    static String TOOLTIP = "Show the wish list in the default browser";

    private final String url;

    public DisplayWishList(String name, String url) {
        super(name);
        putValue(Action.ACTION_COMMAND_KEY, ID + "-" + url);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        this.url = url;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL(url);
        } catch (Exception e) {
            UIUtils.showError(ERROR_BROWSER, e);
        }
    }

}
