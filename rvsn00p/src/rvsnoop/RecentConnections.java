//:File:    RecentConnections.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import nu.xom.Document;
import nu.xom.Element;

/**
 * Provides access to a list of recently used connections.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecentConnections extends XMLConfigFile implements ListEventListener {

    private class AddRecentConnection extends AbstractAction {
        private static final long serialVersionUID = -8554698925345247337L;
        private final RvConnection connection;
        AddRecentConnection(int index, RvConnection connection) {
            super(index + " Add " + connection.getDescription());
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_0 + index));
            this.connection = connection;
        }

        public void actionPerformed(ActionEvent e) {
            Connections.getInstance().add(connection);
            connection.start();
        }

    }

    /**
     * A class that can populate a menu with recent connections.
     * <p>
     * Use it like this:
     * <code>myMenu.getPopupMenu().addPopupMenuListener(new MenuManager())</code>
     */
    public class MenuManager implements PopupMenuListener {
        public MenuManager() {
            super();
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
            // Do nothing.
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // Do nothing.
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            final JPopupMenu menu = (JPopupMenu) e.getSource();
            menu.removeAll();
            if (connections.size() == 0) {
                final JMenuItem item = menu.add("No Recent Connections");
                item.setEnabled(false);
            } else {
                int index = 0;
                for (final Iterator i = connections.iterator(); i.hasNext(); )
                    menu.add(new AddRecentConnection(index++, (RvConnection) i.next()));
            }
        }
    }

    private static final int DEFAULT_MAX_SIZE = 10;
    private static final String ROOT = "recentConnections";

    public static RecentConnections instance;

    public static synchronized RecentConnections getInstance() {
        if (instance == null) {
            final String home = System.getProperty("user.home");
            final String fs = System.getProperty("file.separator");
            final File file = new File(home + fs + ".rvsnoop" + fs + "recentConnections.xml");
            instance = new RecentConnections(file);
        }
        return instance;
    }

    // This needs to be LinkedList then we can use the removeLast method.
    private final LinkedList connections = new LinkedList();

    private int maxSize = DEFAULT_MAX_SIZE;

    private RecentConnections(File file) {
        super(file);
        load();
        Connections.getInstance().addListEventListener(this);
    }

    protected Document getDocument() {
        final Element root = new Element(ROOT);
        Project.storeConnections(root);
        return new Document(root);
    }

    /**
     * Gets the last connection used during this session.
     *
     * @return The last connection or <code>null</code> if there has been no connection this session.
     */
    public RvConnection getLastConnection() {
        return (RvConnection) (connections.size() > 0 ? connections.getFirst() : null);
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.event.ListEventListener#listChanged(ca.odell.glazedlists.event.ListEvent)
     */
    public void listChanged(ListEvent changes) {
        final EventList list = changes.getSourceList();
        list.getReadWriteLock().readLock().lock();
        try {
            while (changes.next()) {
                if (changes.getType() == ListEvent.INSERT) {
                    final Object o = list.get(changes.getIndex());
                    connections.remove(o);
                    connections.add(o);
                    if (connections.size() > maxSize)
                        connections.removeLast();
                }
            }
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    protected void load(Element root) {
        final RvConnection[] conns = Project.loadConnections(root);
        for (int i = 0, imax = conns.length; i < imax; ++i)
            connections.add(conns[i]);
        setMaxSize(maxSize);
    }

    /**
     * Set a size limit for the list.
     *
     * @param maxSize
     */
    private void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        while (connections.size() > maxSize)
            connections.removeLast();
    }

    /**
     * Gets the current number of entries in the recent listeners list.
     *
     * @return The size of the recent connections list.
     */
    public int size() {
        return connections.size();
    }

}
