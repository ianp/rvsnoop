/*
 * Class:     ImageBorder
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A border which provides both padding (like {@link javax.swing.border.EmptyBorder} and the ability to draw images.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class ImageBorder extends AbstractBorder {

    private static final long serialVersionUID = -7838384273915851060L;

    /** Indicator that the image should not be painted. */
    public static final int HIDDEN = -1;
    /** Indicator that the image should be painted in the top border. */
    public static final int TOP = 0;
    /** Indicator that the image should be painted in the left border. */
    public static final int LEFT = 1;
    /** Indicator that the image should be painted in the bottom border. */
    public static final int BOTTOM = 2;
    /** Indicator that the image should be painted in the right border. */
    public static final int RIGHT = 3;

    private int top, left, bottom, right;

    private Image image;
    private int imagePosition = HIDDEN;
    
    /**
     * Creates a border with the specified insets and no image.
     *
     * @param top The top inset of the border.
     * @param left The left inset of the border.
     * @param bottom The bottom inset of the border.
     * @param right The right inset of the border.
     */
    public ImageBorder(int top, int left, int bottom, int right)   {
        this.top = top; 
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    /**
     * Creates a border with the specified insets and image.
     *
     * @param top The top inset of the border.
     * @param left The left inset of the border.
     * @param bottom The bottom inset of the border.
     * @param right The right inset of the border.
     * @param image The image to display.
     * @param position The border to display the image in.
     */
    public ImageBorder(int top, int left, int bottom, int right, Image image, int position)   {
        this(top, left, bottom, right);
        this.image = image;
        this.imagePosition = position;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (image == null) { return; }
        // FIXME draw the image here
        switch (imagePosition) {
        case TOP: { // We don't draw the image here anywhere in the UI.
            break;
        }
        case LEFT: { // The top-left corner.
            x += (left - image.getWidth(c)) / 2;
            y += top;
            g.drawImage(image, x, y, c);
            break;
        }
        case BOTTOM: { // We don't draw the image here anywhere in the UI.
            break;
        }
        case RIGHT: { // The top right corner, inside the border region.
            x += width - right + (right - image.getWidth(c)) / 2;
            y += top + (height - image.getHeight(c)) / 2;
            g.drawImage(image, x, y, c);
            break;
        }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
     */
    public Insets getBorderInsets(Component c)       {
        return getBorderInsets();
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = left;
        insets.top = top;
        insets.right = right;
        insets.bottom = bottom;
        return insets;
    }

    /**
     * Returns the insets of the border.
     */
    public Insets getBorderInsets() {
        return new Insets(top, left, bottom, right);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#isBorderOpaque()
     */
    public boolean isBorderOpaque() {
        return image != null && imagePosition != HIDDEN;
    }

    /**
     * Set the image that will be painted into one of the borders.
     *
     * @param image The image to paint.
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Set the border into which to paint the image.
     *
     * @param imagePosition The border to paint the image into.
     */
    public void setImagePosition(int imagePosition) {
        if (imagePosition < HIDDEN || imagePosition > RIGHT) {
            imagePosition = HIDDEN;
        }
        this.imagePosition = imagePosition;
    }

}
