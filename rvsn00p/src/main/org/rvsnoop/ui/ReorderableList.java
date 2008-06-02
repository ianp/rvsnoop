/*
 * Class:     ReorderableList
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * A custom list that allows it's contents to be re-ordered by dragging.
 * <p>
 * Note that this list uses a custom border implementation to provide
 * drag-and-drop feedback to the user, for this reason it is important to ensure
 * that the {@link ListCellRenderer} calls {@link JComponent#setBorder(Border)}
 * every time it is used.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class ReorderableList extends JList {

    private static final class DecoratingBorder extends CompoundBorder {
        private static final long serialVersionUID = -1837330987892062947L;
        DecoratingBorder(Border outsideBorder) {
            super(outsideBorder, null);
        }
        void setInsideBorder(Border border) {
            insideBorder = border;
        }
    }

    private final class DecoratingRenderer implements ListCellRenderer {
        private final ListCellRenderer renderer;
        DecoratingRenderer(ListCellRenderer renderer) {
            this.renderer = renderer;
        }
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JComponent) {
                final JComponent jc = (JComponent) c;
                if (index == dropTarget) {
                    final Border border = jc.getBorder();
                    dropBeforeThisItemBorder.setInsideBorder(border);
                    jc.setBorder(dropBeforeThisItemBorder);
                } else if (index == dropTarget - 1
                        && index == ReorderableList.this.list.size() - 1) {
                    final Border border = jc.getBorder();
                    dropAfterThisItemBorder.setInsideBorder(border);
                    jc.setBorder(dropAfterThisItemBorder);
                }
            }
            return c;
        }
    }

    private final class ImageGrabTimer implements ActionListener {
        private final Component component;
        private final Point point;
        public ImageGrabTimer(Component component, Point point) {
            this.component = component;
            this.point = point;
        }
        public void actionPerformed(ActionEvent e) {
            if (!mouseDown) { return; }
            dragSource = locationToIndex(point);
            if (dragSource < 0) { return; }
            final int w = getFixedCellWidth(), h = getFixedCellHeight();
            final BufferedImage image = getGraphicsConfiguration().
                    createCompatibleImage(w, h, Transparency.TRANSLUCENT);
            Graphics g = image.getGraphics();
            component.paint(g);
            g.dispose();
            SwingUtilities.convertPointToScreen(point, component);
            SwingUtilities.convertPointFromScreen(point, glasspane);
            glasspane.setLocation(point);
            glasspane.setImage(image);
            glasspane.setVisible(true);
        }
    }

    private final class ItemImageGrabber extends MouseAdapter {

        public ItemImageGrabber() {
            super();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mouseDown = true;
            final Point p = (Point) e.getPoint().clone();
            final Component c = e.getComponent();
            new Timer(DRAG_DELAY_MILLIS, new ImageGrabTimer(c, p)).start();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDown = false;
            if (dragSource < 0) { return; }
            final Point point = (Point) e.getPoint().clone();
            dropTarget = locationToIndex(point);
            // Check that the point isn't after the last index:
            Point last = indexToLocation(list.size() - 1);
            if (last == null || point.y > last.y) { dropTarget = list.size(); }
            glasspane.setVisible(false);
            glasspane.setImage(null);
            drop();
        }

    }

    private final class ItemImageMover extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragSource < 0) { return; }
            final Point point = (Point) e.getPoint().clone();
            final int oldDropTarget = dropTarget;
            dropTarget = locationToIndex(point);

            // Check that the point isn't below the last item in the list.
            final int lastIndex = list.size() - 1;
            final int lastBottom = indexToLocation(lastIndex).y
                    + getCellBounds(lastIndex, lastIndex).height;
            if (point.y > lastBottom) { dropTarget = list.size(); }

            final Component c = e.getComponent();
            SwingUtilities.convertPointToScreen(point, c);
            SwingUtilities.convertPointFromScreen(point, glasspane);
            glasspane.setLocation(point);
            glasspane.repaint();

            // Update the borders if the drop target has changed
            if (dropTarget != oldDropTarget) { repaint(); }
        }
    }

    private static final long serialVersionUID = 7047865772591888274L;

    private static final DecoratingBorder dropAfterThisItemBorder =
        new DecoratingBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));
    private static final DecoratingBorder dropBeforeThisItemBorder =
        new DecoratingBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));

    private static final int DRAG_DELAY_MILLIS = 500;

    /** The source item which will be moved. */
    private int dragSource = -1;

    /**
     * The target index that an item will be moved to.
     * <p>
     * The dropped item will displace the item currently at this index. For
     * example: if the drop target is 0 then the dropped item will be moved to
     * the head of the list; if the drop target is equal to
     * <code>list.size()</code> the dropped item will be moved to the tail of
     * the list.
     */
    private int dropTarget = -1;

    private final GhostGlassPane glasspane;

    private final EventList list;

    private boolean mouseDown;

    /**
     * Create a new <code>ReorederableList</code>.
     *
     * @param list The list to display.
     */
    public ReorderableList(EventList list, GhostGlassPane glasspane) {
        super(new EventListModel(list));
        this.glasspane = glasspane;
        this.list = list;
        addMouseListener(new ItemImageGrabber());
        addMouseMotionListener(new ItemImageMover());
    }

    private void drop() {
        final Lock lock = list.getReadWriteLock().writeLock();
        lock.lock();
        try {
            if (dropTarget < dragSource) {
                list.add(dropTarget, list.remove(dragSource));
            } else if (dropTarget == dragSource) {
                return;
            } else if (dropTarget == list.size()) {
                list.add(list.remove(dragSource));
            } else {
                list.add(dropTarget - 1, list.remove(dragSource));
            }
        } finally {
            lock.unlock();
        }
        dragSource = -1;
        dropTarget = -1;
        clearSelection();
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.JList#setCellRenderer(javax.swing.ListCellRenderer)
     */
    @Override
    public void setCellRenderer(ListCellRenderer cellRenderer) {
        super.setCellRenderer(new DecoratingRenderer(cellRenderer));
    }

}
