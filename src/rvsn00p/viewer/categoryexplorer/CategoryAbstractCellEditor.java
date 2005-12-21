//:File:    CategoryAbstractCellEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

/**
 * CategoryAbstractCellEditor.  Base class to handle the some common
 * details of cell editing.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryAbstractCellEditor implements TableCellEditor, TreeCellEditor {

    protected EventListenerList _listenerList = new EventListenerList();
    protected Object _value;
    protected ChangeEvent _changeEvent = null;
    protected int _clickCountToStart = 1;

    public Object getCellEditorValue() {
        return _value;
    }

    public void setCellEditorValue(Object value) {
        _value = value;
    }

    public void setClickCountToStart(int count) {
        _clickCountToStart = count;
    }

    public int getClickCountToStart() {
        return _clickCountToStart;
    }

    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            if (((MouseEvent) anEvent).getClickCount() < _clickCountToStart) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        if (this.isCellEditable(anEvent)) {
            if (anEvent == null ||
                    ((MouseEvent) anEvent).getClickCount() >= _clickCountToStart) {
                return true;
            }
        }
        return false;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public void addCellEditorListener(CellEditorListener l) {
        _listenerList.add(CellEditorListener.class, l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        _listenerList.remove(CellEditorListener.class, l);
    }

    public Component getTreeCellEditorComponent(
            JTree tree, Object value,
            boolean isSelected,
            boolean expanded,
            boolean leaf, int row) {
        return null;
    }

    public Component getTableCellEditorComponent(
            JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        return null;
    }

    protected void fireEditingStopped() {
        Object[] listeners = _listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                if (_changeEvent == null) {
                    _changeEvent = new ChangeEvent(this);
                }

                ((CellEditorListener) listeners[i + 1]).editingStopped(_changeEvent);
            }
        }
    }

    protected void fireEditingCanceled() {
        Object[] listeners = _listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                if (_changeEvent == null) {
                    _changeEvent = new ChangeEvent(this);
                }

                ((CellEditorListener) listeners[i + 1]).editingCanceled(_changeEvent);
            }
        }
    }

}
