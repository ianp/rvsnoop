/*
 * Class:     RecentProjects
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Provides access to a list of recently used projects as well as the current project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class RecentProjects extends XMLConfigFile {

    /**
     * A class that can populate a menu with recent projects.
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
            if (projects.size() == 0) {
                final JMenuItem item = menu.add("No Recent Projects");
                item.setEnabled(false);
            } else {
                int index = 0;
                for (final Iterator i = projects.iterator(); i.hasNext(); )
                    menu.add(new OpenRecentAction(index++, (Project) i.next()));
            }
        }
    }

    private static class OpenRecentAction extends AbstractAction {
        private static final long serialVersionUID = 663435715412125115L;
        private final Project project;
        OpenRecentAction(int index, Project project) {
            super(index + " Open " + project.getFile().getName());
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_0 + index));
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            Project.setCurrentProject(project);
            log.info("Loaded project from " + project.getFile().getName());
        }

    }

    private static final String CONFIG_DIRECTORY = ".rvsnoop";
    private static final String CONFIG_FILE = "recentProjects.xml";
    private static final int DEFAULT_MAX_SIZE = 5;
    private static final String PROJECT = "project";
    private static final String ROOT = "recentProjects";

    private static final Log log = LogFactory.getLog(RecentProjects.class);

    public static RecentProjects INSTANCE = new RecentProjects();

    private static File getRecentConnectionsFile() {
        final String home = System.getProperty("user.home");
        final String fs = System.getProperty("file.separator");
        return new File(home + fs + CONFIG_DIRECTORY + fs + CONFIG_FILE);
    }

    private int maxSize = DEFAULT_MAX_SIZE;

    // This needs to be LinkedList then we can use the removeLast method.
    private final LinkedList projects = new LinkedList();

    private RecentProjects() {
        super(getRecentConnectionsFile());
        load();
    }

    /**
     * Adds a project to the list.
     * <p>
     * If the project is already in the list then it is promoted instead.
     *
     * @param project The project to add.
     */
    public void add(Project project) {
        projects.remove(project);
        projects.addFirst(project);
        while (projects.size() > maxSize)
            projects.removeLast();
    }

    public boolean contains(Project project) {
        return projects.contains(project);
    }

    protected Document getDocument() {
        final Element root = new Element(ROOT);
        for (final Iterator i = projects.iterator(); i.hasNext(); ) {
            try {
                setString(root, PROJECT, ((Project) i.next()).getFile().getCanonicalPath());
            } catch (IOException ignored) {
                // OK, don't add it to the recent list then.
            }
        }
        return new Document(root);
    }

    /**
     * Gets a read only view of the list of projects.
     *
     * @return The list of projects.
     */
    public List getProjects() {
        return Collections.unmodifiableList(projects);
    }

    protected void load(Element root) {
        final Elements projects = root.getChildElements(PROJECT);
        for (int i = 0, imax = projects.size(); i < imax; ++i) {
            final Element project = projects.get(i);
            final File file = new File(project.getValue());
            if (file.exists())
                try {
                    add(new Project(file.getCanonicalFile()));
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not canonicalize file name: " + file.getPath(), e);
                    }
                }
        }
    }

    /**
     * Set a size limit for the list.
     *
     * @param maxSize
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        while (projects.size() > maxSize)
            projects.removeLast();
    }

    /**
     * Gets the current number of entries in the recent listeners list.
     *
     * @return The size of the list.
     */
    public int size() {
        return projects.size();
    }

}
