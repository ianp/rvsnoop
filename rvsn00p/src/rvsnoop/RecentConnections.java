//:File:    RecentConnections.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import nu.xom.Elements;

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

    // A small value type to use instead of real connection instances.
    // We cannot use real connections because if we did then loading the
    // recent connections would cause the Connection List to be populated.
    public static class ConnectionDescriptor {
        final String description, service, network, daemon;
        int hashCode;
        final List subjects = new ArrayList();
        ConnectionDescriptor(RvConnection connection) {
            this.description = connection.getDescription();
            this.service = connection.getService();
            this.network = connection.getNetwork();
            this.daemon = connection.getDaemon();
            subjects.addAll(connection.getSubjects());
        }
        ConnectionDescriptor(String description, String service, String network, String daemon) {
            this.description = description;
            this.service = service;
            this.network = network;
            this.daemon = daemon;
        }
        public boolean equals(Object o) {
            if (o == this) return true;
            // Match ConnectionDescriptor
            if (o instanceof ConnectionDescriptor) {
                final ConnectionDescriptor that = (ConnectionDescriptor) o;
                if (service.equals(that.service)
                        && network.equals(that.network)
                        && daemon.equals(that.daemon))
                    return true;
            // but also RvConnection
            } else if (o instanceof RvConnection) {
                final RvConnection that = (RvConnection) o;
                if (hashCode() == that.hashCode()
                        && service.equals(that.getService())
                        && network.equals(that.getNetwork())
                        && daemon.equals(that.getDaemon()))
                    return true;
            }
            return false;
        }
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = new StringBuffer()
                    .append(network).append(service).append(daemon)
                    .hashCode();
            }
            return hashCode;
        }
        public String toString() {
            final StringBuffer buffer = new StringBuffer();
            buffer.append("[RvConnection: description=").append(description);
            buffer.append(", service=").append(service);
            buffer.append(", network=").append(network);
            buffer.append(", daemon=").append(daemon).append("]");
            return buffer.toString();
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
    private static final String RV_CONNECTION = "connection";
    private static final String RV_DAEMON = "daemon";
    private static final String RV_DESCRIPTION = "description";
    private static final String RV_NETWORK = "network";
    private static final String RV_SERVICE = "service";
    private static final String SUBJECT = "subject";

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
        for (final Iterator i = connections.iterator(); i.hasNext(); ) {
            final RvConnection connection = (RvConnection) i.next();
            final Element element = appendElement(root, RV_CONNECTION);
            setString(element, RV_DESCRIPTION, connection.getDescription());
            setString(element, RV_SERVICE, connection.getService());
            setString(element, RV_NETWORK, connection.getNetwork());
            setString(element, RV_DAEMON, connection.getDaemon());
            for (final Iterator j = connection.getSubjects().iterator(); j.hasNext(); )
                setString(element, SUBJECT, (String) j.next());
        }
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
        final Elements elements = root.getChildElements(RV_CONNECTION);
        for (int i = 0, imax = elements.size(); i < imax; ++i) {
            final Element element = elements.get(i);
            final String description = getString(element, RV_DESCRIPTION);
            final String service = getString(element, RV_SERVICE);
            final String network = getString(element, RV_NETWORK);
            final String daemon = getString(element, RV_DAEMON);
            final ConnectionDescriptor cd = new ConnectionDescriptor(description, service, network, daemon);
            final Elements subjects = element.getChildElements(SUBJECT);
            for (int j = 0, jmax = subjects.size(); j < jmax; ++j)
                cd.subjects.add(subjects.get(j).getValue());
            connections.add(cd);
        }
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
