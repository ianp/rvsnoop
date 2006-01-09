//:File:    Icons.java
//:Created: Dec 13, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * All of the icons used by RvSn00p.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class Icons {
    
    private static final int ICON_SIZE = 16;

    public static final Image APPLICATION = createImage("/resources/icons/rvsnoop.png", ICON_SIZE);
    public static final Icon ABOUT = createIcon("/resources/icons/about.png", ICON_SIZE);
    public static final Icon ADD_CONNECTION = createIcon("/resources/icons/add_connection.png", ICON_SIZE);
    public static final Icon BUG = createIcon("/resources/icons/bug.png", ICON_SIZE);
    public static final Icon CHECK_UPDATES = createIcon("/resources/icons/check_updates.png", ICON_SIZE);
    public static final Icon CLEAR_LEDGER = createIcon("/resources/icons/clear_ledger.png", ICON_SIZE);
    public static final Icon COPY = createIcon("/resources/icons/copy.png", ICON_SIZE);
    public static final Icon CUT = createIcon("/resources/icons/cut.png", ICON_SIZE);
    public static final Icon DELETE = createIcon("/resources/icons/delete.png", ICON_SIZE);
    public static final Icon EXPORT = createIcon("/resources/icons/export.png", ICON_SIZE);
    public static final Icon SEARCH = createIcon("/resources/icons/search.png", ICON_SIZE);
    public static final Icon SEARCH_AGAIN = createIcon("/resources/icons/search_again.png", ICON_SIZE);
    public static final Icon FONT = createIcon("/resources/icons/font.png", ICON_SIZE);
    public static final Icon HELP = createIcon("/resources/icons/help.png", ICON_SIZE);
    public static final Icon LICENSE = createIcon("/resources/icons/license.png", ICON_SIZE);
    public static final Icon OPEN = createIcon("/resources/icons/open.png", ICON_SIZE);
    public static final Icon PASTE = createIcon("/resources/icons/paste.png", ICON_SIZE);
    public static final Icon PAUSE = createIcon("/resources/icons/pause.png", ICON_SIZE);
    public static final Icon QUIT = createIcon("/resources/icons/quit.png", ICON_SIZE);
    public static final Icon REPUBLISH = createIcon("/resources/icons/republish.png", ICON_SIZE);
    public static final Icon RESUME = createIcon("/resources/icons/resume.png", ICON_SIZE);
    public static final Icon RV_MESSAGE = createIcon("/resources/icons/rv_message.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon RV_FIELD = createIcon("/resources/icons/rv_field.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon RVD_PAUSED = createIcon("/resources/icons/rvd_paused.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon RVD_STARTED = createIcon("/resources/icons/rvd_started.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon RVD_STOPPED = createIcon("/resources/icons/rvd_stopped.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon SAVE = createIcon("/resources/icons/save.png", ICON_SIZE);
    public static final Icon SAVE_AS = createIcon("/resources/icons/save_as.png", ICON_SIZE);
    public static final Icon SUBJECT = createIcon("/resources/icons/subject.png", ICON_SIZE);
    public static final Icon SUBSCRIBE_UPDATES = createIcon("/resources/icons/subscribe_updates.png", ICON_SIZE);
    public static final Icon WEB = createIcon("/resources/icons/web.png", ICON_SIZE);
    public static final Icon XML_ATTRIBUTE = createIcon("/resources/icons/xml_attribute.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon XML_ELEMENT = createIcon("/resources/icons/xml_element.png", ICON_SIZE); //$NON-NLS-1$
    
    private static BufferedImage createCompatibleImage(int w, int h) {
        final GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        return config.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }

    public static Icon createIcon(String filename, int size) {
        final Image image = createImage(filename, size);
        return image != null ? new ImageIcon(image) : getMissingIcon(size);
    }

    private static Image createImage(String filename, int size) {
        try {
            final URL url = Icons.class.getResource(filename);
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            final BufferedImage buffimage = createCompatibleImage(size, size);
            buffimage.createGraphics().drawImage(image, 0, 0, null);
            return image;
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Icon getMissingIcon(int size) {
        try {
            final BufferedImage image = createCompatibleImage(size, size);
            final Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.RED);
            g2.fillRect(0, 0, size - 1, size - 1);
            return new ImageIcon(image);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Do not instantiate.
     */
    private Icons() {
        throw new UnsupportedOperationException();
    }

}
