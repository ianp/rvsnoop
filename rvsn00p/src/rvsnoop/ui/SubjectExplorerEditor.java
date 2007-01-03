//:File:    SubjectExplorerEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.EventObject;

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

import org.rvsnoop.Application;
import org.rvsnoop.RecordSubjectMatcher;

import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;
import ca.odell.glazedlists.matchers.Matcher;

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
            if (popupMenu.isPopupTrigger(e) && element != null)
                popupMenu.show(tree, e.getX(), e.getY());
        }
        public void mouseReleased(MouseEvent e) {
            // Use the same behaviour for pressed and released.
            mousePressed(e);
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

    private class SelectRecords extends AbstractAction {
        private static final long serialVersionUID = -2934261387681391415L;
        private final boolean descendants;
        SelectRecords(String name, boolean descendants) {
            super(name);
            this.descendants = descendants;
        }
        public void actionPerformed(ActionEvent e) {
            final SubjectElement subject = (SubjectElement) tree.getSelectionPath().getLastPathComponent();
            final ListSelectionModel model = UIManager.INSTANCE.getMessageLedger().getSelectionModel();
            try {
                model.setValueIsAdjusting(true);
                model.clearSelection();
                final Matcher matcher = new RecordSubjectMatcher(subject, descendants);
                final int[] indices = application.getFilteredLedger().findAllIndices(matcher);
                for (int i = 0, imax = indices.length; i < imax; ++i) {
                    model.addSelectionInterval(indices[i], indices[i]);
                }
            } finally {
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

    private final Application application;

    private SubjectElement element;

    private final EventListenerList listenerList = new EventListenerList();

    private final JPopupMenu popupMenu = new JPopupMenu();

    private final JTree tree;

    public SubjectExplorerEditor(final Application application, final JTree tree) {
        this.application = application;
        this.tree = tree;
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new PopupMenuListener());
        checkbox.addActionListener(new SubjectSelectionListener());
        popupMenu.add(new Select());
        popupMenu.add(new Deselect());
        popupMenu.addSeparator();
        popupMenu.add(new SelectRecords("Select All Records At", false));
        popupMenu.add(new SelectRecords("Select All Records Under", true));
        popupMenu.addSeparator();
        popupMenu.add(new Expand());
        popupMenu.add(new Collapse());
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
