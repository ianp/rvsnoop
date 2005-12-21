//:File:    Icons.java
//:Created: Dec 13, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

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

    static final Image APPLICATION = createImage("/resources/icons/rvsn00p.png", ICON_SIZE);
    public static final Icon DELETE = createIcon("/resources/icons/delete.png", ICON_SIZE);
    public static final Icon NEW_LISTENER = createIcon("/resources/icons/new_listener.png", ICON_SIZE);
    public static final Icon PAUSE = createIcon("/resources/icons/pause.png", ICON_SIZE);
    public static final Icon RESUME = createIcon("/resources/icons/resume.png", ICON_SIZE);
    public static final Icon RV_MESSAGE = createIcon("/resources/icons/rv_message.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon RV_FIELD = createIcon("/resources/icons/rv_field.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon SUBJECT = createIcon("/resources/icons/subject.png", ICON_SIZE);
    public static final Icon XML_ATTRIBUTE = createIcon("/resources/icons/xml_attribute.png", ICON_SIZE); //$NON-NLS-1$
    public static final Icon XML_ELEMENT = createIcon("/resources/icons/xml_element.png", ICON_SIZE); //$NON-NLS-1$
    
    private static BufferedImage createCompatibleImage(int w, int h) {
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        return config.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }

    private static Icon createIcon(String filename, int size) {
        Image image = createImage(filename, size);
        return image != null ? new ImageIcon(image) : getMissingIcon(size);
    }

    private static Image createImage(String filename, int size) {
        try {
            URL url = Icons.class.getResource(filename);
            Image image = Toolkit.getDefaultToolkit().getImage(url);
            image = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            BufferedImage buffimage = createCompatibleImage(size, size);
            buffimage.createGraphics().drawImage(image, 0, 0, null);
            return image;
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Icon getMissingIcon(int size) {
        try {
            BufferedImage image = createCompatibleImage(size, size);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.RED);
            g2.fillRect(0, 0, size - 1, size - 1);
            return new ImageIcon(image);
        } catch (Exception e) {
            return null;
        }
    }

    private Icons() {
        super();
    }

}
