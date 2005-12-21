//:File:    CategoryNodeRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import rvsn00p.viewer.Icons;

/**
 * CategoryNodeRenderer
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryNodeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = -4830414654486030224L;

    public static final Color FATAL_CHILDREN = new Color(189, 113, 0);

    protected JCheckBox _checkBox = new JCheckBox();
    protected JPanel _panel = new JPanel();
//    protected static ImageIcon _sat = null;
//   protected JLabel              _label  = new JLabel();

    public CategoryNodeRenderer() {
        _panel.setBackground(UIManager.getColor("Tree.textBackground"));

//        if (_sat == null) {
//            // Load the satellite image.
//            String resource =
//                    "/rvsn00p/viewer/images/channelexplorer_satellite.gif";
//            URL satURL = getClass().getResource(resource);
//
//            _sat = new ImageIcon(satURL);
//        }

        setOpaque(false);
        _checkBox.setOpaque(false);
        _panel.setOpaque(false);

        // The flowlayout set to LEFT is very important so that the editor
        // doesn't jump around.
        _panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        _panel.add(_checkBox);
        _panel.add(this);

        if (Icons.SUBJECT != null) {
            setOpenIcon(Icons.SUBJECT);
            setClosedIcon(Icons.SUBJECT);
            setLeafIcon(Icons.SUBJECT);
        }
    }

    public Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean selected, boolean expanded,
            boolean leaf, int row,
            boolean hasFocus) {

        CategoryNode node = (CategoryNode) value;
        //FileNode node = (FileNode)value;
        //String s = tree.convertValueToText(value, selected,
        //						   expanded, leaf, row, hasFocus);

        super.getTreeCellRendererComponent(
                tree, value, selected, expanded,
                leaf, row, hasFocus);

        if (row == 0) {
            // Root row -- no check box
            _checkBox.setVisible(false);
        } else {
            _checkBox.setVisible(true);
            _checkBox.setSelected(node.isSelected());
        }
        String toolTip = buildToolTip(node);
        _panel.setToolTipText(toolTip);
        if (node.hasFatalChildren()) {
            this.setForeground(FATAL_CHILDREN);
        }
        if (node.hasFatalRecords()) {
            this.setForeground(Color.red);
        }

        return _panel;
    }

    public Dimension getCheckBoxOffset() {
        return new Dimension(0, 0);
    }

    protected String buildToolTip(CategoryNode node) {
        StringBuffer result = new StringBuffer();
        result.append(node.getTotalNumberOfRecords());
        result.append(" ");
        result.append(" records has been registered by this node.");
        result.append(" Right-click for more info.");
        return result.toString();
    }

}
