/*
 * Class:     VisibleColumnsMenuManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.rvsnoop.RecordLedgerFormat;
import org.rvsnoop.RecordLedgerFormat.ColumnFormat;

/**
 * A manager to handle the display of visible columns for a ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class VisibleColumnsMenuManager implements MenuListener, PopupMenuListener {

    private static final class MenuItem extends JCheckBoxMenuItem implements ItemListener {
        private static final long serialVersionUID = -4655091368491662571L;
        final ColumnFormat column;
        final RecordLedgerFormat format;
        public MenuItem(RecordLedgerFormat format, ColumnFormat column) {
            super(column.getName());
            this.column = column;
            this.format = format;
            setSelected(format.contains(column));
        }
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                format.add(column);
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                format.remove(column);
            }
        }
    }

    private final RecordLedgerFormat format;

    public VisibleColumnsMenuManager(RecordLedgerFormat format) {
        this.format = format;
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
        final Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator();
        while (i.hasNext()) {
            menu.add(new MenuItem(format, (ColumnFormat) i.next()));
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
        final Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator();
        while (i.hasNext()) {
            menu.add(new MenuItem(format, (ColumnFormat) i.next()));
        }
    }

}
