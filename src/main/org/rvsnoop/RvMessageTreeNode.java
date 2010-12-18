/*
 * Class:     RvMessageTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import static com.google.common.collect.Lists.newArrayList;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static final Icon icon = new ImageIcon("/resources/icons/rvMessage.png");

    private static final Log log = LogFactory.getLog(RvMessageTreeNode.class);

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
                if (log.isErrorEnabled()) {
                    log.error(MessageFormat.format(ERROR_FIELD, e.error), e);
                }
                break;
            }
        }
        children.trimToSize();
        return children;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
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
