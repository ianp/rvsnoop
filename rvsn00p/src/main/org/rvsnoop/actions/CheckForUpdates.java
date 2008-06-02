/*
 * Class:     CheckForUpdates
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.ui.ImageFactory;

import rvsnoop.Version;

/**
 * Check for updates.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class CheckForUpdates extends RvSnoopAction {

    static { NLSUtils.internationalize(CheckForUpdates.class); }

    private static final long serialVersionUID = -2631302391392386708L;

    public static final String COMMAND = "checkForUpdates";
    static String MNEMONIC, NAME, TOOLTIP;
    static String ERROR_IO, ERROR_PARSE, MESSAGE_NEW_VERSION, MESSAGE_UP_TO_DATE;

    private static final String ICON_ERROR = "error";
    private static final String ICON_NEW_VERSION = "updateAvailable";
    private static final String ICON_UP_TO_DATE = "updateNotAvailable";

    private static final Log log = LogFactory.getLog(CheckForUpdates.class);

    public CheckForUpdates(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final JFrame frame = application.getFrame();
        try {
            final Document doc = new Builder().build("http://rvsnoop.org/version.xml");
            if (!isVersionCurrent(doc)) {
                final Icon icon = new ImageIcon(ImageFactory.getInstance().getBannerImage(ICON_NEW_VERSION));
                JOptionPane.showMessageDialog(frame, MESSAGE_NEW_VERSION, NAME, JOptionPane.INFORMATION_MESSAGE, icon);
            } else {
                final Icon icon = new ImageIcon(ImageFactory.getInstance().getBannerImage(ICON_UP_TO_DATE));
                JOptionPane.showMessageDialog(frame, MESSAGE_UP_TO_DATE, NAME, JOptionPane.INFORMATION_MESSAGE, icon);
            }
        } catch (ParsingException e) {
            final Icon icon = new ImageIcon(ImageFactory.getInstance().getBannerImage(ICON_ERROR));
            JOptionPane.showMessageDialog(frame, ERROR_PARSE, NAME, JOptionPane.ERROR_MESSAGE, icon);
            if (log.isErrorEnabled()) { log.error(ERROR_PARSE, e); }
        } catch (IOException e) {
            final Icon icon = new ImageIcon(ImageFactory.getInstance().getBannerImage(ICON_ERROR));
            JOptionPane.showMessageDialog(frame, ERROR_IO, NAME, JOptionPane.ERROR_MESSAGE, icon);
            if (log.isErrorEnabled()) { log.error(ERROR_IO, e); }
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
