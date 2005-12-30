//:File:    SubjectExplorerEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.ui;

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
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import rvsn00p.SubjectElement;
import rvsn00p.SubjectHierarchy;

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
                tree.collapsePath(new TreePath(((SubjectElement) e.nextElement()).getPath()));
        }
    }
    
    private class Deselect extends AbstractAction {
        private static final long serialVersionUID = -1003331904354240811L;
        Deselect() {
            super("Deselect All");
        }
        public void actionPerformed(ActionEvent event) {
            SubjectHierarchy.getInstance().setAllSelected(element, false);
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
                tree.expandPath(new TreePath(((SubjectElement) e.nextElement()).getPath()));
        }
    }

    private class Select extends AbstractAction {
        private static final long serialVersionUID = 6270358401502502833L;
        Select() {
            super("Select All");
        }
        public void actionPerformed(ActionEvent event) {
            SubjectHierarchy.getInstance().setAllSelected(element, true);
        }
    }
    
    private static final long serialVersionUID = 7957683568277521431L;

    private SubjectElement element;
    
    final EventListenerList listenerList = new EventListenerList();
    
    private JPopupMenu popupMenu;

    private final JTree tree;

    public SubjectExplorerEditor(final JTree tree) {
        super();
        this.tree = tree;
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() && element != null)
                    popup(element, e.getX(), e.getY());
            }
        });
        checkbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (element != null)
                    SubjectHierarchy.getInstance().setSelected(element, !element.isSelected());
            }
        });
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

    void popup(SubjectElement target, int x, int y) {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.add(new Select());
            popupMenu.add(new Deselect());
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
