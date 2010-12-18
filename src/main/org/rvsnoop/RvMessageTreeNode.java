// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A {@link javax.swing.tree.TreeNode TreeNode} that wraps a Rendezvous message.
 * <p>
 * This class is designed to act as the root node in a tree model.
 */
public final class RvMessageTreeNode extends LazyTreeNode {

    private static final Icon icon = new ImageIcon("/resources/icons/rvMessage.png");

    private static final Logger logger = Logger.getLogger();

    static String TEXT, ERROR_FIELD;

    static { NLSUtils.internationalize(RvMessageTreeNode.class); }

    private final TibrvMsg message;

    /**
     * @param message The message represented by this node.
     */
    public RvMessageTreeNode(TibrvMsg message) {
        super(null);
        this.message = message;
    }

    @Override
    protected List<TreeNode> createChildren() {
        final int numFields = message.getNumFields();
        if (numFields == 0) { return Collections.emptyList(); }
        final ArrayList<TreeNode> children = newArrayList();
        for (int i = 0; i < numFields; ++i) {
            try {
                children.add(new RvFieldTreeNode(this, message.getFieldByIndex(i)));
            } catch (TibrvException e) {
                logger.error(e, ERROR_FIELD, e.error);
                break;
            }
        }
        children.trimToSize();
        return children;
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText() {
        return TEXT;
    }

}
