/*
 * Class:     SubscribeToUpdates
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rvsnoop.BrowserLauncher;
import rvsnoop.ui.Icons;

/**
 * Subscribe to update notifications.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class SubscribeToUpdates extends AbstractAction {

    private static String ERROR_BROWSER = "The default browser could not be opened.";

    private static final String ID = "subscribeToUpdates";

    private static final Log log = LogFactory.getLog(SubscribeToUpdates.class);

    private static String NAME = "Subscribe to Updates";

    private static final long serialVersionUID = -2927101571954736097L;

    private static String TOOLTIP = "Subscribe to update notifications";

    public SubscribeToUpdates() {
        super(NAME, Icons.SUBSCRIBE_UPDATES);
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
            if (log.isErrorEnabled()) { log.error(ERROR_BROWSER, e); }
        }
    }

}
