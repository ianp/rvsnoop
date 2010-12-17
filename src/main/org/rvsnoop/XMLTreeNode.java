/*
 * Class:     XMLTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParentNode;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;
import rvsnoop.ui.Icons;

/**
 * A {@link TreeNode} that wraps XOM node objects to allow for visual display.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
final class XMLTreeNode extends LazyTreeNode {

    private final Node node;

    public XMLTreeNode(TreeNode parent, Node node) {
        super(parent);
        this.node = node;
    }

    @Override
    protected List<TreeNode> createChildren() {
        final ParentNode pn = (ParentNode) node;
        final int numChildren = pn.getChildCount();
        if (numChildren == 0) { return Collections.emptyList(); }
        final ArrayList<TreeNode> children = newArrayList();
        for (int i = 0; i < numChildren; ++i) {
            children.add(new XMLTreeNode(this, pn.getChild(i)));
        }
        children.trimToSize();
        return children;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    @Override
    public boolean getAllowsChildren() {
        return node instanceof ParentNode;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getIcon()
     */
    @Override
    public Icon getIcon() {
        if (node instanceof Attribute)
            return Icons.XML_ATTRIBUTE;
        if (node instanceof Element)
            return Icons.XML_ELEMENT;
        // Comment, Document, DocType, ProcessingInstruction, Text
        return null;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getText()
     */
    @Override
    public String getText() {
        if (node instanceof Attribute)
            return ((Attribute) node).getLocalName();
        if (node instanceof Element)
            return ((Element) node).getLocalName();
        if (node instanceof Comment)
            return "<!-- " + node.getValue(); //$NON-NLS-1$
        if (node instanceof Document)
            return "XML Document"; //$NON-NLS-1$
        if (node instanceof DocType)
            return "<!DocType " + ((DocType) node).getRootElementName(); //$NON-NLS-1$
        if (node instanceof ProcessingInstruction)
            return "<? " + ((ProcessingInstruction) node).getTarget();
        if (node instanceof Text)
            return node.getValue(); //$NON-NLS-1$
        return "[Unsupported node type!]"; //$NON-NLS-1$
    }

}
