//:File:    SubjectExplorerRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import rvsnoop.SubjectElement;

/**
 * A custom renderer for nodes in the subject explorer tree.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class SubjectExplorerRenderer extends JPanel implements TreeCellRenderer {

    private static final Color ERROR_COLOUR = Color.RED.brighter();

    private static final long serialVersionUID = 5695225783544033925L;

    private static final Color WARNING_COLOUR = Color.ORANGE.brighter();

    private final StringBuffer buffer = new StringBuffer();
    
    final JCheckBox checkbox = new JCheckBox();

    private final JLabel label = new JLabel();

    public SubjectExplorerRenderer() {
        super(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(UIManager.getColor("Tree.textBackground"));
        checkbox.setOpaque(false);
        label.setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        add(checkbox);
        add(label);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) { 
        // Overridden for performance reasons.
        if ("text".equals(propertyName) || "selected".equals(propertyName))
            super.firePropertyChange(propertyName, oldValue, newValue);
    }

    public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean focus) {
        final SubjectElement element = (SubjectElement) value;
        label.setText((String) element.getUserObject());
        checkbox.setSelected(element.isSelected());
        buffer.append("Records at this node: ").append(element.getNumRecordsHere());
        buffer.append("\nRecords under this node: ").append(element.getNumRecordsUnder());
        if (element.isErrorHere()) {
            buffer.append("\nThere is an error at this node.");
            label.setForeground(Color.RED);
        } else if (element.isErrorUnder()) {
            buffer.append("\nThere is an error under this node.");
            label.setForeground(Color.ORANGE);
        } else {
            label.setForeground(Color.BLACK);
        }
        label.setIcon(Icons.SUBJECT);
        setToolTipText(buffer.toString());
        buffer.setLength(0);
        return this;
    }

//    /**
//     * Overridden for performance reasons.
//     * <p>
//     * As per the standard cell renderers the two repaint methods are overridden
//     * to avoid taking unnecessary code paths as they are called many times.
//     * Unlike the standard classes however, we still need to call the
//     * <code>validate</code> methods as the check box and label need to be
//     * laid out.
//     */
//    public void repaint(long tm, int x, int y, int width, int height) {
//        // Do nothing.
//    }
//
//    /**
//     * Overridden for performance reasons.
//     * <p>
//     * As per the standard cell renderers the two repaint methods are overridden
//     * to avoid taking unnecessary code paths as they are called many times.
//     * Unlike the standard classes however, we still need to call the
//     * <code>validate</code> methods as the check box and label need to be
//     * laid out.
//     */
//    public void repaint(Rectangle r) {
//        // Do nothing.
//    }

}
