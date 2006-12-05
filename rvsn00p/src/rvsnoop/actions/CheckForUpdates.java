//:File:    CheckForUpdates.java
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

import nu.xom.Builder;
import nu.xom.Document;

import rvsnoop.Logger;
import rvsnoop.Version;
import rvsnoop.ui.Banners;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIUtils;

/**
 * Check for updates.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class CheckForUpdates extends AbstractAction {

    private static String ERROR = "Could not complete version check.";

    private static final String ID = "checkForUpdates";

    private static final Logger logger = Logger.getLogger(CheckForUpdates.class);

    private static String MESSAGE_NEW_VERSION = "A new version is available.";

    private static String MESSAGE_UP_TO_DATE = "Your version is up to date.";

    private static String NAME = "Check for Updates";

    private static final long serialVersionUID = 947745941196389522L;

    private static String TOOLTIP = "Check for newer versions of RvSnoop (stable versions only)";

    public CheckForUpdates() {
        super(NAME, Icons.CHECK_UPDATES);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        try {
            final Document doc = new Builder().build("http://rvsnoop.org/version.xml");
            if (!isVersionCurrent(doc))
                UIUtils.showInformation(MESSAGE_NEW_VERSION, Banners.UPDATE_AVAILABLE);
            else
                UIUtils.showInformation(MESSAGE_UP_TO_DATE, Banners.UPDATE_ALREADY);
        } catch (Exception e) {
            logger.error(ERROR, e);
        }
    }

    private static boolean isVersionCurrent(Document doc) {
        final int major = Integer.parseInt(doc.query("//major").get(0).getValue());
        if (major > Version.getMajor()) return false;
        final int minor = Integer.parseInt(doc.query("//patch").get(0).getValue());
        if (minor > Version.getMinor()) return false;
        final int patch = Integer.parseInt(doc.query("//patch").get(0).getValue());
        return patch <= Version.getPatch();
    }

}
