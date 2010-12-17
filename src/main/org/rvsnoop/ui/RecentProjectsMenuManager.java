/*
 * Class:     RecentProjects
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.Project;
import org.rvsnoop.UserPreferences;


/**
 * Provides access to a list of recently used projects as well as the current project.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class RecentProjectsMenuManager implements MenuListener, PopupMenuListener {

    private final class MenuItem extends JMenuItem implements ActionListener {
        private static final long serialVersionUID = -4151087760552035722L;
        final File file;
        public MenuItem(File file, int index) {
            super(MessageFormat.format(MENU_TEXT, new Object[] {
                    new Integer(index), file.getName()
            }));
            this.file = file;
            setMnemonic(KeyEvent.VK_0 + index);
            addActionListener(this);
        }
        public void actionPerformed(ActionEvent event) {
            try {
                application.setProject(new Project(file));
            } catch (IOException e) {
                // TODO: log it and pop a dialog
            }
        }
    }
    
    static { NLSUtils.internationalize(RecentProjectsMenuManager.class); }

    static String MENU_TEXT, EMPTY_MENU_TEXT;

    private final Application application;

    public RecentProjectsMenuManager(Application application) {
        this.application = application;
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
        final Iterator<File> i = UserPreferences.getInstance().getRecentProjects().iterator();
        int count = 0;
        while (i.hasNext()) {
            menu.add(new MenuItem(i.next(), ++count));
        }
        if (count == 0) {
            menu.add(new JMenuItem(EMPTY_MENU_TEXT));
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
        final Iterator<File> i = UserPreferences.getInstance().getRecentProjects().iterator();
        int count = 0;
        while (i.hasNext()) {
            menu.add(new MenuItem(i.next(), ++count));
        }
        if (count == 0) {
            menu.add(new JMenuItem(EMPTY_MENU_TEXT));
        }
    }

}
