//:File:    SubscribeToUpdates.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsn00p.ui.UIUtils;
import rvsn00p.util.BrowserLauncher;

/**
 * Subscribe to update notifications.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class SubscribeToUpdates extends AbstractAction {

    static String ERROR_BROWSER = "The default browser could not be opened.";

    public static final String ID = "subscribeToUpdates";
    
    static String NAME = "Subscribe to Updates";

    private static final long serialVersionUID = -2927101571954736097L;

    static String TOOLTIP = "Subscribe to update notifications";

    public SubscribeToUpdates() {
        super(NAME);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL("http://sourceforge.net/project/filemodule_monitor.php?filemodule_id=60335");
        } catch (Exception e) {
            UIUtils.showError(ERROR_BROWSER, e);
        }
    }

}
