/*
 * Class:     Icons
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * All of the icons used by RvSnoop.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class Icons {

    private static class ErrorReporter implements ImageObserver {
        private final String image;
        ErrorReporter(String image) {
            this.image = image;
        }
        /* (non-Javadoc)
         * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
         */
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            if ((infoflags & ImageObserver.ERROR) != 0) {
                if (log.isErrorEnabled()) {
                    log.error("Error loading image " + image + ".");
                }
            }
            return false;
        }
    }

    private static final int ICON_SIZE = 16;

    private static final Log log = LogFactory.getLog(Icons.class);

    public static final Icon ABOUT = new ImageIcon(getSmallImage("about"));
    public static final Icon ADD_CONNECTION = createIcon("/resources/icons/add_connection.png", ICON_SIZE);
    public static final Icon BUG = createIcon("/resources/icons/bug.png", ICON_SIZE);
    public static final Icon CHECK_UPDATES = createIcon("/resources/icons/check_updates.png", ICON_SIZE);
    public static final Icon COLUMNS_CORNER = createIcon("/resources/icons/columns_corner_button.png", 14);
    public static final Icon DELETE = createIcon("/resources/icons/delete.png", ICON_SIZE);
    public static final Icon DRAG_HANDLE = createIcon("/resources/icons/drag_handle.png", ICON_SIZE);
    public static final Icon EXPORT = createIcon("/resources/icons/export.png", ICON_SIZE);
    public static final Icon FILTER_COLUMNS = createIcon("/resources/icons/filter_columns.png", ICON_SIZE);
    public static final Icon FONT = createIcon("/resources/icons/font.png", ICON_SIZE);
    public static final Icon HELP = createIcon("/resources/icons/help.png", ICON_SIZE);
    public static final Icon IMPORT = createIcon("/resources/icons/import.png", ICON_SIZE);
    public static final Icon LICENSE = createIcon("/resources/icons/license.png", ICON_SIZE);
    public static final Icon PASTE = createIcon("/resources/icons/paste.png", ICON_SIZE);
    public static final Icon PAUSE = createIcon("/resources/icons/pause.png", ICON_SIZE);
    public static final Icon QUIT = createIcon("/resources/icons/quit.png", ICON_SIZE);
    public static final Icon REPUBLISH = createIcon("/resources/icons/republish.png", ICON_SIZE);
    public static final Icon RESUME = createIcon("/resources/icons/resume.png", ICON_SIZE);
    public static final Icon SEARCH = createIcon("/resources/icons/search.png", ICON_SIZE);
    public static final Icon SEARCH_AGAIN = createIcon("/resources/icons/search_again.png", ICON_SIZE);
    public static final Icon SUBSCRIBE_UPDATES = createIcon("/resources/icons/subscribe_updates.png", ICON_SIZE);
    public static final Icon WEB = createIcon("/resources/icons/web.png", ICON_SIZE);
    public static final Icon XML_ATTRIBUTE = createIcon("/resources/icons/xml_attribute.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon XML_ELEMENT = createIcon("/resources/icons/xml_element.png", ICON_SIZE); //$NON-NLS-1$

    public static Icon createIcon(String filename, int size) {
        return new ImageIcon(createImage(filename, size));
    }

    private static Image createImage(String filename, int size) {
        if (log.isDebugEnabled()) {
            log.debug("Loading image from " + filename);
        }
        return new ImageIcon(filename).getImage();
//        final GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
//        final BufferedImage image = config.createCompatibleImage(size, size, Transparency.TRANSLUCENT);
//        final Graphics2D gg = image.createGraphics();
//        try {
//            final URL url = Icons.class.getResource(filename);
//            final Image i = Toolkit.getDefaultToolkit().getImage(url);
//            gg.drawImage(i, 0, 0, new ErrorReporter(filename));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        gg.dispose();
//        return image;
    }

    public static Image getLargeImage(String name) {
        return createImage("/resources/banners/" + name + ".png", 48);
    }

    public static Image getSmallImage(String name) {
        return createImage("/resources/icons/" + name + ".png", 16);
    }

    /** Do not instantiate. */
    private Icons() {
        throw new UnsupportedOperationException();
    }

}
