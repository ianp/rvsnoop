// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.ProjectService;

/**
 * Provides access to a list of recently used projects as well as the current project.
 */
public final class RecentProjectsMenuManager implements MenuListener, PopupMenuListener {

    static { NLSUtils.internationalize(RecentProjectsMenuManager.class); }

    static String MENU_TEXT, EMPTY_MENU_TEXT;

    private final Application application;

    private final ProjectService projectService;

    public RecentProjectsMenuManager(Application application, ProjectService projectService) {
        this.application = application;
        this.projectService = projectService;
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
        List<File> recentProjects = projectService.getRecentProjectFiles();
        if (recentProjects.isEmpty()) {
            menu.add(new JMenuItem(EMPTY_MENU_TEXT));
        } else {
            int count = 0;
            for (File recentProject : recentProjects) {
                menu.add(new MenuItem(recentProject, ++count));
            }
        }
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
        ((JPopupMenu) e.getSource()).removeAll();
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // Do nothing.
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        final JPopupMenu menu = (JPopupMenu) e.getSource();
        menu.removeAll();
        List<File> recentProjects = projectService.getRecentProjectFiles();
        if (recentProjects.isEmpty()) {
            menu.add(new JMenuItem(EMPTY_MENU_TEXT));
        } else {
            int count = 0;
            for (File recentProject : recentProjects) {
                menu.add(new MenuItem(recentProject, ++count));
            }
        }
    }

    private final class MenuItem extends JMenuItem implements ActionListener {
        final File file;
        public MenuItem(File file, int index) {
            super(MessageFormat.format(MENU_TEXT, index, file.getName()));
            this.file = file;
            setMnemonic(KeyEvent.VK_0 + index);
            addActionListener(this);
        }
        public void actionPerformed(ActionEvent event) {
            projectService.openProject(file);
        }
    }

}
