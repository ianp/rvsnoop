/*
 * Class:     CheckForUpdates
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;

import rvsnoop.Version;

/**
 * Check for updates.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class CheckForUpdatesTask extends Task<Boolean, Void> {

    private static final Log log = LogFactory.getLog(CheckForUpdatesTask.class);

    public CheckForUpdatesTask(Application application) {
        super(application);
        // XOM doesn't provide us with a way to cancel this.
        setUserCanCancel(false);
    }

    @Override
    protected Boolean doInBackground() throws IOException, ParsingException {
        // Return true if version is current, false if out of date.
        final Document doc = new Builder().build("http://rvsnoop.org/version.xml");
        final int major = Integer.parseInt(doc.query("//major").get(0).getValue());
        if (major > Version.getMajor()) return false;
        final int minor = Integer.parseInt(doc.query("//patch").get(0).getValue());
        if (minor > Version.getMinor()) return false;
        final int patch = Integer.parseInt(doc.query("//patch").get(0).getValue());
        return patch <= Version.getPatch();
    }

    @Override
    protected void failed(Throwable e) {
        JFrame frame = ((SingleFrameApplication) getApplication()).getMainFrame();
        ResourceMap resourceMap = getContext().getResourceMap();
        String message = e instanceof ParsingException
                ? resourceMap.getString("checkForUpdates.error.parse")
                : resourceMap.getString("checkForUpdates.error.io");
        JOptionPane.showMessageDialog(frame,
                message,
                resourceMap.getString("checkForUpdates.Action.text"),
                JOptionPane.ERROR_MESSAGE,
                resourceMap.getIcon("banners.error"));
        if (log.isErrorEnabled()) { log.error(message, e); }
    }

    @Override
    protected void succeeded(Boolean current) {
        JFrame frame = ((SingleFrameApplication) getApplication()).getMainFrame();
        ResourceMap resourceMap = getContext().getResourceMap();
        if (current) {
            JOptionPane.showMessageDialog(frame,
                    resourceMap.getString("checkForUpdates.info.upToDate"),
                    resourceMap.getString("checkForUpdates.Action.text"),
                    JOptionPane.INFORMATION_MESSAGE,
                    resourceMap.getIcon("checkForUpdates.icon.upToDate"));
        } else {
            JOptionPane.showMessageDialog(frame,
                    resourceMap.getString("checkForUpdates.info.outOfDate"),
                    resourceMap.getString("checkForUpdates.Action.text"),
                    JOptionPane.INFORMATION_MESSAGE,
                    resourceMap.getIcon("checkForUpdates.icon.outOfDate"));
        }
    }

}
