//:File:    SubjectExplorerEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.util.concurrent.Lock;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;

/**
 * A custom editor for nodes in the subject explorer tree.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class SubjectExplorerEditor extends SubjectExplorerRenderer implements TreeCellEditor, TreeSelectionListener {

    private class Collapse extends AbstractAction {
        private static final long serialVersionUID = -5793440540554792293L;
        Collapse() {
            super("Collapse");
        }
        public void actionPerformed(ActionEvent event) {
            final Enumeration e = element.depthFirstEnumeration();
            while (e.hasMoreElements())
                tree.collapsePath(new TreePath(((DefaultMutableTreeNode) e.nextElement()).getPath()));
        }
    }
    
    private class Deselect extends AbstractAction {
        private static final long serialVersionUID = -1003331904354240811L;
        Deselect() {
            super("Deselect All");
        }
        public void actionPerformed(ActionEvent event) {
            SubjectHierarchy.INSTANCE.setAllSelected(element, false);
        }
    }
    
    private class Expand extends AbstractAction {
        private static final long serialVersionUID = -3907465331493944891L;
        Expand() {
            super("Expand");
        }
        public void actionPerformed(ActionEvent event) {
            final Enumeration e = element.breadthFirstEnumeration();
            while (e.hasMoreElements())
                tree.expandPath(new TreePath(((DefaultMutableTreeNode) e.nextElement()).getPath()));
        }
    }

    private class PopupMenuListener extends MouseAdapter {
        PopupMenuListener() {
            super();
        }
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger() && element != null)
                popup(e.getX(), e.getY());
        }
    }

    private class Select extends AbstractAction {
        private static final long serialVersionUID = 6270358401502502833L;
        Select() {
            super("Select All");
        }
        public void actionPerformed(ActionEvent event) {
            SubjectHierarchy.INSTANCE.setAllSelected(element, true);
        }
    }

    private class SelectAllRecordsAt extends AbstractAction {
        private static final long serialVersionUID = 6732078846733331503L;
        SelectAllRecordsAt() {
            super("Select All Records At");
        }
        public void actionPerformed(ActionEvent e) {
            final SubjectElement subject = (SubjectElement) tree.getSelectionPath().getLastPathComponent();
            final ListSelectionModel model = UIManager.INSTANCE.getMessageLedger().getSelectionModel();
            final EventList list = MessageLedger.INSTANCE.getEventList();
            final Lock lock = list.getReadWriteLock().readLock();
            try {
                lock.lock();
                model.setValueIsAdjusting(true);
                model.clearSelection();
                int index = 0;
                for (final Iterator i = list.iterator(); i.hasNext(); ++index) {
                    final Record record = (Record) i.next();
                    if (subject.equals(record.getSubject()))
                        model.addSelectionInterval(index, index);
                }
            } finally {
                lock.unlock();
                model.setValueIsAdjusting(false);
            }
        }
    }
    
    private class SelectAllRecordsUnder extends AbstractAction {
        private static final long serialVersionUID = -7342058243983715186L;
        SelectAllRecordsUnder() {
            super("Select All Records Under");
        }
        public void actionPerformed(ActionEvent e) {
            final SubjectElement subject = (SubjectElement) tree.getSelectionPath().getLastPathComponent();
            final ListSelectionModel model = UIManager.INSTANCE.getMessageLedger().getSelectionModel();
            final EventList list = MessageLedger.INSTANCE.getEventList();
            final Lock lock = list.getReadWriteLock().readLock();
            try {
                lock.lock();
                model.setValueIsAdjusting(true);
                model.clearSelection();
                int index = 0;
                for (final Iterator i = list.iterator(); i.hasNext(); ++index) {
                    final Record record = (Record) i.next();
                    if (subject.isNodeDescendant(record.getSubject()))
                        model.addSelectionInterval(index, index);
                }
            } finally {
                lock.unlock();
                model.setValueIsAdjusting(false);
            }
        }
    }

    private class SubjectSelectionListener implements ActionListener {
        SubjectSelectionListener() {
            super();
        }
        public void actionPerformed(ActionEvent e) {
            if (element != null)
                SubjectHierarchy.INSTANCE.setSelected(element, !element.isSelected());
        }
    }

    private static final long serialVersionUID = 7957683568277521431L;

    private SubjectElement element;
    
    private final EventListenerList listenerList = new EventListenerList();
    
    private JPopupMenu popupMenu;

    private final JTree tree;

    public SubjectExplorerEditor(final JTree tree) {
        super();
        this.tree = tree;
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new PopupMenuListener());
        checkbox.addActionListener(new SubjectSelectionListener());
    }
    
    public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    
    public void cancelCellEditing() {
        final EventListener[] listeners = listenerList.getListeners(CellEditorListener.class);
        if (listeners != null && listeners.length > 0) {
            final ChangeEvent event = new ChangeEvent(this);
            for (int i = 0, imax = listeners.length; i < imax; ++i)
                ((CellEditorListener) listeners[i]).editingCanceled(event);
        }
    }

    public Object getCellEditorValue() {
        return element.isSelected() ? Boolean.TRUE : Boolean.FALSE;
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
        return getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, true);
    }

    public boolean isCellEditable(EventObject event) {
        return event instanceof MouseEvent && ((MouseEvent) event).getClickCount() == 1;
    }

    private void popup(int x, int y) {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.add(new Select());
            popupMenu.add(new Deselect());
            popupMenu.addSeparator();
            popupMenu.add(new SelectAllRecordsAt());
            popupMenu.add(new SelectAllRecordsUnder());
            popupMenu.addSeparator();
            popupMenu.add(new Expand());
            popupMenu.add(new Collapse());
        }
        popupMenu.show(tree, x, y);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }

    public boolean shouldSelectCell(EventObject event) {
        return true;
    }

    public boolean stopCellEditing() {
        final EventListener[] listeners = listenerList.getListeners(CellEditorListener.class);
        if (listeners != null && listeners.length > 0) {
            final ChangeEvent event = new ChangeEvent(this);
            for (int i = 0, imax = listeners.length; i < imax; ++i)
                ((CellEditorListener) listeners[i]).editingStopped(event);
        }
        return true;
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (tree.getSelectionCount() == 1) {
            final Object component = e.getPath().getLastPathComponent();
            if (component instanceof SubjectElement)
                element = (SubjectElement) component;
        }
    }

}
