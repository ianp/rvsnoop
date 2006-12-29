//:File:    ReportBug.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
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
 * Report a bug or request a new feature.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class ReportBug extends AbstractAction {

    private static String ERROR_BROWSER = "The default browser could not be opened.";

    private static final String ID = "reportBug";

    private static final Log log = LogFactory.getLog(ReportBug.class);

    private static String NAME = "Report Bug";

    private static final long serialVersionUID = -4375842586768327328L;

    private static String TOOLTIP = "Report a bug or request a new feature";

    public ReportBug() {
        super(NAME, Icons.BUG);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            BrowserLauncher.openURL("http://sourceforge.net/tracker/?group_id=63447");
        } catch (Exception e) {
            if (log.isErrorEnabled()) { log.error(ERROR_BROWSER, e); }
        }
    }

}
