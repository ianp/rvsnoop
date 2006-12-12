//:File:    RecentConnections.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.io.IOUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

/**
 * Provides access to a list of recently used connections.
 * <p>
 * This class listens for additions to the connections list and keeps track
 * of them. It is also resposible for saving this information to a file when
 * the application shuts down and for reloading in at startup.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecentConnections implements ListEventListener {

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
     */
    public class MenuManager implements MenuListener {
        public MenuManager() {
            super();
        }
        public void menuCanceled(MenuEvent e) {
            ((JMenu) e.getSource()).removeAll();
        }
        public void menuDeselected(MenuEvent e) {
            // Do nothing.
        }
        public void menuSelected(MenuEvent e) {
            final JMenu menu = (JMenu) e.getSource();
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

    private static final String XML_ELEMENT = "connections";
    private static final String XML_NS = "http://rvsnoop.org/ns/connections/1";

    public static RecentConnections instance;

    private static final Logger logger = Logger.getLogger(RecentConnections.class);

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

    private final File file;

    private int maxSize = DEFAULT_MAX_SIZE;

    private RecentConnections(File file) {
        this.file = file;
        load();
        Connections.getInstance().addListEventListener(this);
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

    public void load() {
        if (!file.exists()) return;
        if (!file.canRead()) {
            if (Logger.isWarnEnabled()) logger.warn("Cannot read file: " + file.getName());
            return;
        }
        if (file.length() == 0) {
            if (Logger.isWarnEnabled()) logger.warn("File is empty: " + file.getName());
            return;
        }
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            final Document doc = new Builder().build(stream);
            final Elements elements = doc.getRootElement().getChildElements(RvConnection.XML_ELEMENT, RvConnection.XML_NS);
            for (int i = 0, imax = elements.size(); i < imax; ++i) {
                final RvConnection conn = RvConnection.fromXml(elements.get(i));
                connections.add(conn);
            }
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Unable to load file: " + file.getName(), e);
        } finally {
            IOUtils.closeQuietly(stream);
            setMaxSize(maxSize);
        }
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
     * Save the recent connections list.
     *
     * @throws IOException
     */
    public void store() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        OutputStream stream = null;
        try {
            final Element root = new Element(XML_ELEMENT, XML_NS);
            for (final Iterator i = Connections.getInstance().iterator(); i.hasNext(); )
                root.appendChild(((RvConnection) i.next()).toXml());
            stream = new BufferedOutputStream(new FileOutputStream(file));
            final Serializer ser = new Serializer(stream);
            ser.setIndent(2);
            ser.setLineSeparator(IOUtils.LINE_SEPARATOR);
            ser.write(new Document(root));
            ser.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
