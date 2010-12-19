// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.ui.RecordLedgerFormat.ColumnFormat;

/**
 * A manager to handle the display of visible columns for a ledger.
 */
public final class VisibleColumnsMenuManager implements MenuListener, PopupMenuListener {

    private final RecordLedgerFormat format;

    public VisibleColumnsMenuManager(RecordLedgerFormat format) {
        this.format = format;
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
        for (ColumnFormat columnFormat : format.getAllColumns()) {
            menu.add(new MenuItem(format, columnFormat));
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
        for (ColumnFormat columnFormat : format.getAllColumns()) {
            menu.add(new MenuItem(format, columnFormat));
        }
    }

    private static final class MenuItem extends JCheckBoxMenuItem implements ItemListener {
        private static final long serialVersionUID = -4655091368491662571L;
        final ColumnFormat column;
        final RecordLedgerFormat format;
        public MenuItem(RecordLedgerFormat format, ColumnFormat column) {
            super(column.getName());
            this.column = column;
            this.format = format;
            setSelected(format.contains(column));
            addItemListener(this);
        }
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                format.add(column);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                format.remove(column);
            }
        }
    }

}
