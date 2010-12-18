/*
 * Class:     RecordTypesMenuManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.Application;
import org.rvsnoop.SystemUtils;
import org.rvsnoop.actions.EditRecordTypes;

import rvsnoop.RecordType;

/**
 * A manager to handle the display of all known record types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecordTypesMenuManager implements MenuListener, PopupMenuListener {

    private final class MenuItem extends JCheckBoxMenuItem implements ItemListener {
        private static final long serialVersionUID = 6162170060105708067L;
        final RecordType type;
        public MenuItem(RecordType type, int index) {
            super(type.getName());
            this.type = type;
            setSelected(type.isSelected());
            setMnemonic(KeyEvent.VK_0 + index);
            addItemListener(this);
        }
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                type.setSelected(true);
                application.getRecordTypes().getMatcherEditor().relax();
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                type.setSelected(false);
                application.getRecordTypes().getMatcherEditor().constrain();
            }
        }
    }

    private final Application application;

    public RecordTypesMenuManager(Application application) {
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
        final RecordType[] types = application.getRecordTypes().getAllTypes();
        for (int i = 0, imax = types.length; i < imax; ++i) {
            menu.add(new MenuItem(types[i], i + 1));
        }
        menu.addSeparator();
        final JMenuItem item =
            menu.add(application.getAction(EditRecordTypes.COMMAND));
        if (SystemUtils.IS_OS_MAC) { item.setIcon(null); }
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
        final RecordType[] types = application.getRecordTypes().getAllTypes();
        for (int i = 0, imax = types.length; i < imax; ++i) {
            menu.add(new MenuItem(types[i], i + 1));
        }
        menu.addSeparator();
        final JMenuItem item =
            menu.add(application.getAction(EditRecordTypes.COMMAND));
        if (SystemUtils.IS_OS_MAC) { item.setIcon(null); }
    }

}
