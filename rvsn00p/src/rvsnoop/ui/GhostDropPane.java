//:File:    GhostDropPane.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A glass pane that can draw ‘ghosted’ images.
 * <p>
 * This is based on the ideas in Romain Guy’s <a
 * href="http://www.jroller.com/page/gfx/20050216">weblog</a>.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public class GhostDropPane extends JPanel {

    /**
     * A utility class implementing {@link DropListener}.
     * <p>
     * Methods are provided to determine whether the drop event occurred inside a
     * given target component.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.6
     */
    public class DropAdapter implements DropListener {
        protected final Component component;

        public DropAdapter(final Component component) {
            this.component = component;
        }

        protected Point getTranslatedPoint(final Point point) {
            final Point p = (Point) point.clone();
            SwingUtilities.convertPointFromScreen(p, component);
            return p;
        }

        /**
         * Override this method to set the drop behaviour.
         *
         * @see rvsnoop.ui.GhostDropPane.DropListener#ghostDropped(rvsnoop.ui.GhostDropPane.DropEvent)
         */
        public void ghostDropped(DropEvent e) {
            // Do nothing by default.
        }

        protected boolean isInTarget(final Point point) {
            final Rectangle bounds = component.getBounds();
            return bounds.contains(point);
        }

    }

    /**
     * An event indicating a ghosted component has been dropped.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.6
     */
    public static class DropEvent extends EventObject {

        static final long serialVersionUID = -1760127114801081816L;

        private final Component component;

        private final Point point;

        public DropEvent(GhostDropPane source, Component component, Point point) {
            super(source);
            this.component = component;
            this.point = point;
        }

        /**
         * Get the component that was dropped.
         *
         * @return The component.
         */
        public Component getComponent() {
            return component;
        }

        /**
         * Get the point at which the drop occurred.
         *
         * @return The drop point.
         */
        public Point getDropLocation() {
            return point;
        }

    }

    /**
     * The interface for listening to ghost drop events.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.6
     */
    public interface DropListener extends EventListener {

        /**
         * Invoked when a ghost drop event occurrs.
         *
         * @param e The drop event object.
         */
        public void ghostDropped(DropEvent e);

    }

    /**
     * A handler for mouse and mouse motion events relating to a ghost glass pane.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.6
     */
    public class MouseHandler extends MouseAdapter
            implements MouseMotionListener {

        protected Component component;

        private List listeners;

        protected MouseHandler(final Component component) {
            this.component = component;
        }

        public synchronized void addGhostDropListener(final DropListener listener) {
            if (listener == null) return;
            if (listeners == null) listeners = new ArrayList(1);
            listeners.add(listener);
        }

        /**
         * Create the image that will be ghosted.
         * <p>
         * This is a hook for custom ghost images, normally the component is
         * drawn and subclasses can ignore this method.
         *
         * @param component The component to be drawn by default.
         * @param point The point drawn at.
         * @return The image to ghost.
         */
        protected Image createImage() {
            final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsConfiguration config = env.getDefaultScreenDevice().getDefaultConfiguration();
            final Image image = config.createCompatibleImage(
                    component.getWidth(), component.getHeight(), Transparency.TRANSLUCENT);
            final Graphics g = image.getGraphics();
            component.paint(g);
            g.dispose();
            return image;
        }

        /**
         * Can be used to provide an image offset if the image returned by
         * createImage is an irregular size.
         *
         * @return How to shift the ghosted image.
         */
        protected Point createImageOffset() {
            return null;
        }

        protected void fireGhostDropEvent(final Point point) {
            if (listeners == null || listeners.size() == 0) return;
            final DropEvent event = new DropEvent(GhostDropPane.this, component, point);
            for (final Iterator it = listeners.iterator(); it.hasNext(); )
                ((DropListener) it.next()).ghostDropped(event);
        }

        public void mouseDragged(final MouseEvent event) {
            if (!isVisible()) setVisible(true);
            setPoint(event);
            repaint();
        }

        public void mouseMoved(final MouseEvent event) {
            // NO-OP
        }

        public void mousePressed(final MouseEvent event) {
            final Image image = createImage();
            setPoint(event);
            setImage(image);
            setImageOffset(createImageOffset());
        }

        public void mouseReleased(final MouseEvent event) {
            setPoint(event);
            setVisible(false);
            setImage(null);
            fireGhostDropEvent(event.getPoint());
        }

        public void removeGhostDropListener(final DropListener listener) {
            if (listener == null || listeners == null) return;
            listeners.remove(listener);
        }

        /**
         * Ensures that a clone of the point is used and that it is in the
         * correct co-ordinate space.
         */
        protected void setPoint(MouseEvent event) {
            event = SwingUtilities.convertMouseEvent(event.getComponent(), event, GhostDropPane.this);
            GhostDropPane.this.setPoint(event.getPoint());
        }

    }

    static final long serialVersionUID = 1275432519912812028L;

    private final AlphaComposite composite =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    private Image dragged;

    private Point location = new Point(0, 0);

    private Point imageOffset;

    public GhostDropPane() {
        setOpaque(false);
    }

    public void paintComponent(final Graphics g) {
        if (dragged == null) return;
        final Graphics2D g2 = (Graphics2D) g;
        final Composite savedComposite = g2.getComposite();
        g2.setComposite(composite);
        int x = (int) (location.getX() - dragged.getWidth(this) / 2);
        int y = (int) (location.getY() - dragged.getHeight(this) / 2);
        if (imageOffset != null) {
            x += imageOffset.x;
            y += imageOffset.y;
        }
        g2.drawImage(dragged, x, y, null);
        g2.setComposite(savedComposite);
    }

    public void setImage(final Image dragged) {
        this.dragged = dragged;
    }

    public void setImageOffset(final Point offset) {
        this.imageOffset = offset;
    }

    public void setPoint(final Point location) {
        this.location = location;
    }

}