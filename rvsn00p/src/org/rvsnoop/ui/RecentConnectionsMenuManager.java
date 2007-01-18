/*
 * Class:     RecentConnectionsMenuManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.Connections;

import rvsnoop.RvConnection;

/**
 * A manager to handle the display of all known record types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RecentConnectionsMenuManager implements MenuListener, PopupMenuListener {

    private final class MenuItem extends JMenuItem implements ActionListener {
        private static final long serialVersionUID = -6436657697729548579L;
        final RvConnection connection;
        public MenuItem(RvConnection connection, int index) {
            super(connection.getDescription());
            this.connection = connection;
            setMnemonic(KeyEvent.VK_0 + index);
            addActionListener(this);
        }
        public void actionPerformed(ActionEvent e) {
            Connections.getInstance().add(connection);
            connection.start();
        }
    }

    public RecentConnectionsMenuManager() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.MenuListener#menuCanceled(javax.swing.event.MenuEvent)
     */
    public void menuCanceled(MenuEvent e) {
        ((JMenu) e.getSource()).removeAll();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.MenuListener#menuDeselected(javax.swing.event.MenuEvent)
     */
    public void menuDeselected(MenuEvent e) {
        // Do nothing.
    }

    /* (non-Javadoc)
     * @see javax.swing.event.MenuListener#menuSelected(javax.swing.event.MenuEvent)
     */
    public void menuSelected(MenuEvent e) {
        final JMenu menu = (JMenu) e.getSource();
        menu.removeAll();
        final Iterator i = Connections.getInstance().iterator();
        int count = 0;
        while (i.hasNext()) {
            menu.add(new MenuItem((RvConnection) i.next(), ++count));
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuCanceled(PopupMenuEvent e) {
        ((JPopupMenu) e.getSource()).removeAll();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // Do nothing.
    }

    /* (non-Javadoc)
     * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
     */
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        final JPopupMenu menu = (JPopupMenu) e.getSource();
        menu.removeAll();
        final Iterator i = Connections.getInstance().iterator();
        int count = 0;
        while (i.hasNext()) {
            menu.add(new MenuItem((RvConnection) i.next(), ++count));
        }
    }

}
