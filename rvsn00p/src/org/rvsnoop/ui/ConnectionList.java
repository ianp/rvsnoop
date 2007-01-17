/*
 * Class:     ConnectionList
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.Application;
import org.rvsnoop.actions.NewRvConnection;

import rvsnoop.Connections;
import rvsnoop.RecentConnections;
import rvsnoop.RvConnection;
import ca.odell.glazedlists.swing.EventListModel;

/**
 * A custom <code>JList</code> that is used to draw the connection list.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class ConnectionList extends JList {

    private class PopupListener extends MouseAdapter implements PopupMenuListener {
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
        public void popupMenuCanceled(PopupMenuEvent e) {
            popupMenu.removeAll();
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // Do nothing.
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            // Do nothing.
        }
    }

    private static final long serialVersionUID = 8841926841362334387L;

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
        final ConnectionListCellRenderer renderer =
            new ConnectionListCellRenderer(hidingDefaultValues);
        final PopupListener popupListener = new PopupListener();
        renderer.addMouseListener(popupListener);
        popupMenu.addPopupMenuListener(popupListener);
        setCellRenderer(renderer);
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
