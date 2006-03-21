//:File:    CheckForUpdates.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;

import rvsnoop.IOUtils;
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

    private static String MESSAGE_NEW_VERSION = "A new version has been released: ";

    private static String MESSAGE_UP_TO_DATE = "Your version is up to date.";

    private static String NAME = "Check for Updates";

    private static final long serialVersionUID = 947745941196389522L;

    private static String TOOLTIP = "Check for newer versions of rvSnoop";

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
            final String[] lines = readVersionInfo();
            final Matcher matcher = Pattern.compile(".*([0-9]+)\\.([0-9]+)\\.([0-9]+).*").matcher(lines[2]);
            if (matcher.matches() && !isVersionCurrent(matcher))
                showNewVersionAvailableDialog(lines);
            else
                showVersionIsUpToDateDialog();
        } catch (Exception e) {
            logger.error(ERROR, e);
        }
    }

    private static boolean isVersionCurrent(Matcher matcher) {
        final int major = Integer.parseInt(matcher.group(1));
        if (major > Version.getMajor()) return false;
        final int minor = Integer.parseInt(matcher.group(2));
        if (minor > Version.getMinor()) return false;
        final int patch = Integer.parseInt(matcher.group(3));
        return patch <= Version.getPatch();
    }

    private static String[] readVersionInfo() throws UnknownHostException, IOException {
        InputStream istream = null;
        OutputStream ostream = null;
        Socket socket = null;
        try {
            // Open a socket.
            socket = new Socket("rvsn00p.sourceforge.net", 80);
            istream = socket.getInputStream();
            ostream = socket.getOutputStream();
            // Send the HTTP get request.
            ostream.write("GET http://rvsn00p.sourceforge.net/version.txt HTTP/1.0\n\n".getBytes());
            // Read the response.
            final StringBuffer versionText = new StringBuffer(1024);
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = istream.read(buffer)) != -1)
                versionText.append(new String(buffer, 0, bytesRead, "UTF-8"));
            final String[] lines = versionText.toString().split("\\n");
            // Strip off the server header.
            final Pattern firstLine = Pattern.compile("rvSnoop Version Information");
            int i = 0;
            for (final int length = lines.length; i < length; ++i)
                if (firstLine.matcher(lines[i]).matches())
                    break;
            final String[] tmp = new String[lines.length - i];
            System.arraycopy(lines, i, tmp, 0, tmp.length);
            return tmp;
        } finally {
            IOUtils.closeQuietly(istream);
            IOUtils.closeQuietly(ostream);
            IOUtils.closeQuietly(socket);
        }
    }

    private static void showNewVersionAvailableDialog(String[] lines) {
        lines[0] = MESSAGE_NEW_VERSION;
        UIUtils.showInformation(lines, Banners.UPDATE_AVAILABLE);
    }

    private static void showVersionIsUpToDateDialog() {
        UIUtils.showInformation(MESSAGE_UP_TO_DATE, Banners.UPDATE_ALREADY);
    }

}
