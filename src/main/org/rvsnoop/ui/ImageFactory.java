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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
        private final int h;
        private final String name;
        private final int w;
        MapKey(String name, int w, int h) {
            this.name = name;
            this.w = w;
            this.h = h;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (!(obj instanceof MapKey)) { return false; }
            final MapKey other = (MapKey) obj;
            if (w != other.w || h != other.h) { return false; }
            if (name == null) {
                return other.name == null;
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            int result = 1;
            result = PRIME * result + (name != null ? name.hashCode() : 0);
            result = PRIME * result + w;
            result = PRIME * result + h;
            return result;
        }
    }

    static String DEBUG_LOADING_IMAGE, ERROR_LOADING_IMAGE, WARN_SLOW_LOADING_IMAGE;

    private static ImageFactory instance;
    private static final Log log = LogFactory.getLog(ImageFactory.class);

    public static final int BANNER_SIZE = 48;
    public static final int SMALL_ICON_SIZE = 16;
    private static final long MAX_LOAD_TIME = 2000;

    static { NLSUtils.internationalize(ImageFactory.class); }

    public static synchronized ImageFactory getInstance() {
        if (instance == null) { instance = new ImageFactory(); }
        return instance;
    }

    private final Map images = new HashMap();

    /** Private constructor. Use <code>getInstance()</code> instead. */
    private ImageFactory() {
        ImageIO.setUseCache(false);
    }

    /**
     * Helper method to load a banner sized image from the default image store.
     *
     * @param name The image file to load (without extension).
     * @return The image.
     */
    public Image getBannerImage(String name) {
        return getImage("/resources/banners/" + name + ".png",
                BANNER_SIZE, BANNER_SIZE, null);
    }

    private Image getErrorImage(int w, int h, GraphicsConfiguration gc) {
        final MapKey key = new MapKey(null, w, h);
        Image image = null;
        SoftReference imageRef = (SoftReference) images.get(key);
        if (imageRef != null) { image = (Image) imageRef.get(); }
        if (image != null) { return image; }
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        }
        final BufferedImage bi = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
        final Graphics2D gg = bi.createGraphics();
        gg.setColor(Color.RED);
        gg.fillRect(0, 0, w - 1, h - 1);
        gg.dispose();
        images.put(new MapKey(null, w, h), new SoftReference(bi));
        return bi;
    }

    /**
     * Helper method to load a small icon sized image from the default image store.
     *
     * @param name The image file to load (without extension).
     * @return The image.
     */
    public Image getIconImage(String name) {
        return getImage("/resources/icons/" + name + ".png",
                SMALL_ICON_SIZE, SMALL_ICON_SIZE, null);
    }

    /**
     * Get an image from the factory.
     * <p>
     * If there is a cached copy of the image it will be returned, otherwise a
     * new image will be loaded and cached.
     *
     * @param filename The image to load.
     * @param w The width of the image, or 0 to use the image files' width.
     * @param h The height of the image, or 0 to use the image files' height.
     * @param gc The configuration to load the image in, or <code>null</code> to
     *     use the default configuration.
     * @return The image.
     */
    public Image getImage(String filename, int w, int h, GraphicsConfiguration gc) {
        final MapKey key = new MapKey(filename, w, h);
        Image image = null;
        SoftReference imageRef = (SoftReference) images.get(key);
        if (imageRef != null) { image = (Image) imageRef.get(); }
        if (image != null) { return image; }
        URL url = null;
        try {
            url = NLSUtils.findNLSResource(filename, ImageFactory.class);
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(DEBUG_LOADING_IMAGE,
                        new Object[] { url }));
            }
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            image = toolkit.createImage(url);
            // We need to set a maximum load time here or the app can hang.
            final long startTime = System.currentTimeMillis();
            while (!toolkit.prepareImage(image, -1, -1, null)) {
                if (System.currentTimeMillis() - startTime > MAX_LOAD_TIME) {
                    if (log.isWarnEnabled()) {
                        log.warn(MessageFormat.format(WARN_SLOW_LOADING_IMAGE,
                                new Object[] { url }));
                    }
                    return getErrorImage(w, h, gc);
                }
            }
            image = scaleImage(image, w, h, gc);
            images.put(new MapKey(filename, w, h), new SoftReference(image));
            return image;
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(MessageFormat.format(ERROR_LOADING_IMAGE,
                        new Object[] { url }));
            }
            return getErrorImage(w, h, gc);
        }
    }

    private Image scaleImage(Image image, int w, int h, GraphicsConfiguration gc) {
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0) { return null; }
        float aspectRatio = (float)iw / (float)ih;
        int targetWidth;
        int targetHeight;
        if (iw > ih) {
            targetWidth = w;
            targetHeight = (int)(targetWidth / aspectRatio);
            if (targetHeight > h) {
                targetHeight = h;
                targetWidth = (int)(aspectRatio * targetHeight);
            }
        } else {
            targetHeight = h;
            targetWidth = (int)(aspectRatio * targetHeight);
            if (targetWidth > w) {
                targetWidth = w;
                targetHeight = (int)(targetWidth / aspectRatio);
            }
        }
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        }
//        if (targetWidth == iw && targetHeight == iw) {
//            final BufferedImage scaled = gc.createCompatibleImage(targetWidth,
//                    targetHeight, Transparency.TRANSLUCENT);
//            Graphics2D gg = (Graphics2D) scaled.getGraphics();
//            gg.drawImage(image, 0, 0, null);
//            gg.dispose();
//            return scaled;
//        }
        final BufferedImage scaled = gc.createCompatibleImage(targetWidth,
                targetHeight, Transparency.TRANSLUCENT);
        Graphics2D gg = (Graphics2D) scaled.getGraphics();
        gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gg.drawImage(image, 0, 0, targetWidth, targetHeight, 0, 0, iw, ih, null);
        gg.dispose();
        return scaled;
    }

}
