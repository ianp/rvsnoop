//:File:    RvMessageTreeNode.java
//:Created: Dec 12, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import rvsnoop.ui.Icons;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A {@link javax.swing.tree.TreeNode TreeNode} that wraps a Rendezvous message.
 * <p>
 * This class is designed to act as the root node in a tree model.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class RvMessageTreeNode extends LazyTreeNode {
    
    private static final Logger logger = Logger.getLogger(RvMessageTreeNode.class);
    
    private final TibrvMsg message;
    
    /**
     * @param message The message represented by this node.
     */
    public RvMessageTreeNode(TibrvMsg message) {
        super(null);
        this.message = message;
    }

    protected List createChildren() {
        final int numFields = message.getNumFields();
        final ArrayList children = new ArrayList(numFields);
        for (int i = 0; i < numFields; ++i)
            try {
                children.add(new RvFieldTreeNode(this, message.getFieldByIndex(i)));
            } catch (TibrvException e) {
                logger.error("Problem reading message.", e);
                break;
            }
        return children;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return true;
    }

    public Icon getIcon() {
        return Icons.RV_MESSAGE;
    }
    
    public String getText() {
        return "Rendezvous Message";
    }

}
