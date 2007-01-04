/*
 * Class:     AeMsgTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import com.tibco.sdk.MMessageFormat;
import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import rvsnoop.ui.Icons;

/**
 * A {@link javax.swing.tree.TreeNode TreeNode} that wraps an Active Enterprise
 * message.
 * <p>
 * This works directly with <code>MTree</code>s at the moment. It would be nice
 * to also have a tree node that works with <code>MInstance</code>s, this would
 * require a connection to a repository however.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class AeMsgTreeNode extends LazyTreeNode {

    //private static final Logger logger = Logger.getLogger(AeMsgTreeNode.class);

    private final List children = new ArrayList(2);

    private final String text;

    public AeMsgTreeNode(TibrvMsg message) throws TibrvException {
        super(null);
        MTree data = new MTree("");
        data.use_tibrvMsg(message);
        switch (data.getMessageFormat()) {
        case MMessageFormat.AERV :
            text = "Active Enterprise Message (Format: RV, Version: " + message.getField("^ver^").data + ')';
            children.add(new RvFieldTreeNode(this, message.getField("^data^"), "Message Data", null));
            break;
        case MMessageFormat.XMLRV :
            text = "Active Enterprise Message (Format: XML, Version: " + message.getField("^ver^").data + ')';
            // final TibrvMsg m = (TibrvMsg) message.getField("^data^").data;
            children.add(new RvFieldTreeNode(this, message.getField("^xmldata^"), "Message Data", null));
            break;
        default:
            throw new IllegalArgumentException("Unsupported message format: " + data.getMessageFormat());
        }
        children.add(new RvFieldTreeNode(this, message.getField("^tracking^"), "Tracking Information", Icons.TRACKING));
    }


    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#createChildren()
     */
    protected List createChildren() {
        return children;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getAllowsChildren()
     */
    public boolean getAllowsChildren() {
        return true;
    }

    public Icon getIcon() {
        return Icons.RV_MESSAGE;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getText()
     */
    public String getText() {
        return text;
    }

}
