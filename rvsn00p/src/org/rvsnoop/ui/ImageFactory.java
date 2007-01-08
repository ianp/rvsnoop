/*
 * Class:     ImageFactory
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.NLSUtils;

/**
 * A single location to handle loading and caching file based images.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class ImageFactory {

    private static final class MapKey {
        private static final int PRIME = 31;
        private final String name;
        private final int size;
        MapKey(String name, int size) {
            Validate.notNull(name);
            this.name = name;
            this.size = size;
        }
        public int hashCode() {
            int result = 1;
            result = PRIME * result + name.hashCode();
            result = PRIME * result + size;
            return result;
        }
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (!(obj instanceof MapKey)) { return false; }
            final MapKey other = (MapKey) obj;
            return name.equals(other.name) && size == other.size;
        }
    }
    
    static { NLSUtils.internationalize(ImageFactory.class); }

    private static final Log log = LogFactory.getLog(ImageFactory.class);
    static String DEBUG_LOADING_IMAGE, ERROR_LOADING_IMAGE;

    private static ImageFactory instance;

    public synchronized static ImageFactory getInstance() {
        if (instance == null) { instance = new ImageFactory(); }
        return instance;
    }

    private final Map images = new HashMap();

    /** Private constructor. Use <code>getInstance()</code> instead. */
    private ImageFactory() {
        ImageIO.setUseCache(false);
    }

    private Image getImage(String filename, int size) {
        final MapKey key = new MapKey(filename, size);
        BufferedImage image = (BufferedImage) images.get(key);
        if (image != null) { return image; }
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format(DEBUG_LOADING_IMAGE,
                    new Object[] { filename }));
        }
        image = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(size, size, Transparency.TRANSLUCENT);
        final Graphics2D gg = image.createGraphics();
        InputStream stream = null;
        try {
            stream = NLSUtils.findNLSResource(filename, getClass());
            final Image i = ImageIO.read(stream);
            gg.drawImage(i, 0, 0, null);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_LOADING_IMAGE,
                        new Object[] { filename }), e);
            }
            gg.setColor(Color.RED);
            gg.fillRect(0, 0, size - 1, size - 1);
        } finally {
            IOUtils.closeQuietly(stream);
            gg.dispose();
        }
        return image;
    }

    public Image getBannerImage(String name) {
        return getImage("/resources/banners/" + name + ".png", 48);
    }

    public Image getIconImage(String name) {
        return getImage("/resources/icons/" + name + ".png", 16);
    }

}
