//:File:    CategoryNodeEditorRenderer.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTree;

/**
 * CategoryNodeEditorRenderer
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryNodeEditorRenderer extends CategoryNodeRenderer {

    private static final long serialVersionUID = -3203812924904688773L;

    public Component getTreeCellRendererComponent(
            JTree tree, Object value,
            boolean selected, boolean expanded,
            boolean leaf, int row,
            boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree,
                                                         value, selected, expanded,
                                                         leaf, row, hasFocus);

        return c;
    }

    public JCheckBox getCheckBox() {
        return _checkBox;
    }

}
