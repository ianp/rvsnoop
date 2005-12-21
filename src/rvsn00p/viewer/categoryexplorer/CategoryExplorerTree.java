//:File:    CategoryExplorerTree.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

/**
 * CategoryExplorerTree
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryExplorerTree extends JTree {

    private static final long serialVersionUID = 1113355023776035429L;

    protected CategoryExplorerModel _model;

    protected boolean _rootAlreadyExpanded = false;

    /**
     * Construct a CategoryExplorerTree with a specific model.
     */
    public CategoryExplorerTree(CategoryExplorerModel model) {
        super(model);

        _model = model;
        init();
    }

    /**
     * Construct a CategoryExplorerTree and create a default CategoryExplorerModel.
     */
    public CategoryExplorerTree() {
        super();

        CategoryNode rootNode = new CategoryNode("Subjects");

        _model = new CategoryExplorerModel(rootNode);

        setModel(_model);

        init();
    }

    public CategoryExplorerModel getExplorerModel() {
        return (_model);
    }

    public String getToolTipText(MouseEvent e) {

        try {
            return super.getToolTipText(e);
        }
        catch (Exception ex) {
            return "";
        }

    }

    protected void init() {
        // Put visible lines on the JTree.
        putClientProperty("JTree.lineStyle", "Angled");

        // Configure the Tree with the appropriate Renderers and Editors.

        CategoryNodeRenderer renderer = new CategoryNodeRenderer();
        setEditable(true);
        setCellRenderer(renderer);

        CategoryNodeEditor editor = new CategoryNodeEditor(_model);

        setCellEditor(new CategoryImmediateEditor(this,
                                                  new CategoryNodeRenderer(),
                                                  editor));
        setShowsRootHandles(true);

        setToolTipText("");

        ensureRootExpansion();

    }

    protected void expandRootNode() {
        if (_rootAlreadyExpanded) {
            return;
        }
        _rootAlreadyExpanded = true;
        TreePath path = new TreePath(_model.getRootCategoryNode().getPath());
        expandPath(path);
    }

    protected void ensureRootExpansion() {
        _model.addTreeModelListener(new TreeModelAdapter() {
            public void treeNodesInserted(TreeModelEvent e) {
                expandRootNode();
            }
        });
    }

}
