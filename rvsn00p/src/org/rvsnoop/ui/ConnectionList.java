/*
 * Class:     ConnectionList
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.text.StrBuilder;
import org.rvsnoop.Application;
import org.rvsnoop.actions.NewRvConnection;

import rvsnoop.Connections;
import rvsnoop.RecentConnections;
import rvsnoop.RvConnection;
import rvsnoop.State;
import ca.odell.glazedlists.swing.EventListModel;

/**
 * A custom <code>JList</code> that is used to draw the connection list.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class ConnectionList extends JList {

    private class PopupListener extends MouseAdapter {
        PopupListener() {
            super();
        }
        public void mousePressed(MouseEvent e) {
            if (!popupMenu.isPopupTrigger(e)) { return; }
            final int index = locationToIndex(e.getPoint());
            if (index == -1) { return; }
            final RvConnection connection = (RvConnection) getModel().getElementAt(index);
            if (connection == null) { return; }
            popup(connection, e.getX(), e.getY());
        }
    }

    private final class CellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1984439647326541292L;

        private final StrBuilder builder = new StrBuilder();

        private String[] details;

        private Image image;

        private String title;

        /**
         * Create a new renderer instance.
         */
        public CellRenderer() {
            setOpaque(false);
            addMouseListener(new PopupListener());
        }

        private String[] getDetails(final RvConnection connection) {
            final String service = connection.getService();
            final String network = connection.getNetwork();
            final String daemon = connection.getDaemon();
            final String[] details = new String[3];
            int numDetails = 0;
            if (!hidingDefaultValues || !RvConnection.DEFAULT_SERVICE.equals(service)) {
                details[numDetails++] = "Service: " + service;
            }
            if (!hidingDefaultValues || !RvConnection.DEFAULT_NETWORK.equals(network)) {
                details[numDetails++] = "Network: " + network;
            }
            if (!hidingDefaultValues || !RvConnection.DEFAULT_DAEMON.equals(daemon)) {
                details[numDetails++] = "Daemon: " + daemon;
            }
            if (numDetails == 3) { return details; }
            final String[] temp = new String[numDetails];
            for (int i = 0, count = 0; i < 3; ++i) {
                if (details[i] != null) { temp[count++] = details[i]; }
            }
            return temp;
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
         *      java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            final RvConnection connection = (RvConnection) value;
            title = connection.getDescription();
            details = getDetails(connection);
            setToolTipText(getToolTipText(connection));
            final State state = connection.getState();
            if (state == State.STARTED) {
                image = STARTED;
            } else if (state == State.STOPPED) {
                image = STOPPED;
            } else if (state == State.PAUSED) {
                image = PAUSED;
            }
            return this;
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            final Font font = getFont();
            final Font sfont = font.deriveFont(font.getSize2D() - 2.0f);
            final FontMetrics fm = getFontMetrics(font);
            final FontMetrics sfm = getFontMetrics(sfont);
            final Graphics g = getGraphics();
            final int h = fm.getHeight() + (sfm.getHeight() + 1) * details.length;
            double w = fm.getStringBounds(title, g).getWidth();
            for (int i = 0, imax = details.length; i < imax; ++i) {
                w = Math.max(w, sfm.getStringBounds(details[i], g).getWidth() + 8);
            }
            // border = 2 * 2, image = 16, image/text gap = 2
            return new Dimension((int) w + 22, h + 4);
        }

        private String getToolTipText(final RvConnection connection) {
            final Iterator i = connection.getSubjects().iterator();
            while (i.hasNext()) { builder.append(i.next()).append('\n'); }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            } else {
                builder.append("No subjects subscribed to");
            }
            final String tooltip = builder.toString();
            builder.setLength(0);
            return tooltip;
        }

        public void paint(Graphics g) {
            super.paint(g);
            final Graphics2D gg = (Graphics2D) g;
            final Font font = getFont();
            final Font sfont = font.deriveFont(font.getSize2D() - 2.0f);
            final FontMetrics fm = getFontMetrics(font);
            final FontMetrics sfm = getFontMetrics(sfont);
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int y = fm.getHeight() + 2;
            gg.drawImage(image, 2, y / 2 - image.getHeight(this), this);
            gg.drawString(title, 20, y);
            final Color foreground = gg.getColor();
            gg.setColor(foreground.brighter());
            gg.setFont(sfont);
            for (int i = 0, imax = details.length; i < imax; ++i) {
                y += sfm.getStringBounds(details[i], g).getHeight() + 1;
                gg.drawString(details[i], 28, y);
            }
            gg.setColor(foreground);
            gg.setFont(font);
        }

    }

    private static final long serialVersionUID = 8841926841362334387L;

    private static final Image PAUSED = ImageFactory.getInstance().getIconImage("connectionPaused");
    private static final Image STARTED = ImageFactory.getInstance().getIconImage("connectionStarted");
    private static final Image STOPPED = ImageFactory.getInstance().getIconImage("connectionStopped");

    private final Application application;
    private boolean hidingDefaultValues;

    private final JPopupMenu popupMenu = new JPopupMenu();

    /**
     * Create a new <code>ConnectionList</code>.
     *
     * @param connections The connections to display in the list.
     */
    public ConnectionList(Application application, Connections connections) {
        super(new EventListModel(Connections.getInstance()));
        this.application = application;
        setBorder(BorderFactory.createEmptyBorder());
        setCellRenderer(new CellRenderer());
        setForeground(Color.BLACK);
        setOpaque(true);
    }

    /**
     * Is this list eliding the display of default values.
     * <p>
     * Normally the list will display the settings for each connection in a
     * smaller font, if this is true then any settings which have their values
     * set to the default will not be displayed.
     *
     * @return <code>true</code> if default values are to be elided from the
     *     display, <code>false<code> otherwise.
     */
    public boolean isHidingDefaultValues() {
        return hidingDefaultValues;
    }

    private void popup(RvConnection connection, int x, int y) {
        // XXX should this be called after the menu is cancelled as well?
        popupMenu.removeAll();
        popupMenu.add(connection.getStartAction());
        popupMenu.add(connection.getPauseAction());
        popupMenu.add(connection.getStopAction());
        popupMenu.addSeparator();
        popupMenu.add(connection.getRemoveAction());
        popupMenu.addSeparator();
        popupMenu.add(application.getAction(NewRvConnection.COMMAND));
        final JMenu recent = new JMenu("Recent Connections");
        recent.setIcon(new ImageIcon(
                ImageFactory.getInstance().getIconImage(NewRvConnection.COMMAND)));
        recent.addMenuListener(RecentConnections.getInstance().new MenuManager());
        popupMenu.add(recent);
        popupMenu.show(this, x, y);
    }

    /**
     * @param hidingDefaultValues the hidingDefaultValues to set
     */
    public void setHidingDefaultValues(boolean hidingDefaultValues) {
        this.hidingDefaultValues = hidingDefaultValues;
        revalidate();
    }

}
