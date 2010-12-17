/*
 * Class:     GhostGlassPane
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JComponent;

/**
 * A glass pane that allows ghosted images to be overlaid onto it.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class GhostGlassPane extends JComponent {

    private static final long serialVersionUID = -8138313066911778423L;

    private final AlphaComposite alpha;
    private Image image;
    private Point location = new Point(0, 0);

    public GhostGlassPane() {
        setOpaque(false);
        alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public void setLocation(Point location) {
        this.location = location;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (image == null) { return; }
        Graphics2D gg = (Graphics2D) g;
        gg.setComposite(alpha);
        final int x = location.x - (image.getWidth(this)  / 2);
        final int y = location.y - (image.getHeight(this) / 2);
        gg.drawImage(image, x, y, this);
    }

}