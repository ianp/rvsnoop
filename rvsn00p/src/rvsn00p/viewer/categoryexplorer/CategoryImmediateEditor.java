//:File:    CategoryImmediateEditor.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreePath;

/**
 * CategoryImmediateEditor
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryImmediateEditor extends DefaultTreeCellEditor {

    private CategoryNodeRenderer renderer;
    protected Icon editingIcon = null;

    public CategoryImmediateEditor(JTree tree,
                                   CategoryNodeRenderer renderer,
                                   CategoryNodeEditor editor) {
        super(tree, renderer, editor);
        this.renderer = renderer;
        renderer.setIcon(null);
        renderer.setLeafIcon(null);
        renderer.setOpenIcon(null);
        renderer.setClosedIcon(null);

        super.editingIcon = null;
    }

    public boolean shouldSelectCell(EventObject e) {
        boolean rv = false;  // only mouse events

        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            TreePath path = tree.getPathForLocation(me.getX(),
                                                    me.getY());
            CategoryNode node = (CategoryNode)
                    path.getLastPathComponent();

            rv = node.isLeaf() /*|| !inCheckBoxHitRegion(me)*/;
        }
        return rv;
    }

    public boolean inCheckBoxHitRegion(MouseEvent e) {
        TreePath path = tree.getPathForLocation(e.getX(),
                                                e.getY());
        if (path == null) {
            return false;
        }

        if (true) {
            // offset and lastRow DefaultTreeCellEditor
            // protected members

            Rectangle bounds = tree.getRowBounds(lastRow);
            Dimension checkBoxOffset =
                    renderer.getCheckBoxOffset();

            bounds.translate(offset + checkBoxOffset.width,
                             checkBoxOffset.height);

            // XXX: Should this value be returned?
            bounds.contains(e.getPoint());
        }
        return true;
    }

    protected boolean canEditImmediately(EventObject e) {
        boolean rv = false;

        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            rv = inCheckBoxHitRegion(me);
        }

        return rv;
    }

    protected void determineOffset(JTree tree, Object value,
                                   boolean isSelected, boolean expanded,
                                   boolean leaf, int row) {
        // Very important: means that the tree won't jump around.
        offset = 0;
    }

}
