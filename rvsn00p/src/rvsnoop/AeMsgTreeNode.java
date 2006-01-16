//:File:    AeMsgTreeNode.java
//:Created: Jan 8, 2006
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import rvsnoop.ui.Icons;

import com.tibco.sdk.MMessageFormat;
import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

/**
 * A {@link javax.swing.tree.TreeNode TreeNode} that wraps an Active Enterprise
 * message.
 * <p>
 * At the moment this is a bit of a hack, it should really use a repository
 * connection to create {@link com.tibco.sdk.metadata.MInstance MInstance}s and
 * work with those.
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class AeMsgTreeNode extends LazyTreeNode {

    private static final Logger logger = Logger.getLogger(AeMsgTreeNode.class);

    private final List children = new ArrayList(2);
    
    private final String label;
    
    public AeMsgTreeNode(TibrvMsg message) throws TibrvException {
        super(null);
        MTree data = new MTree("");
        data.use_tibrvMsg(message);
        switch (data.getMessageFormat()) {
        case MMessageFormat.AERV :
            label = "AE Message (format: RV, version: " + message.getField("^ver^").data + ")";
            children.add(new RvFieldTreeNode(this, message.getField("^data^")));
            break;
        case MMessageFormat.XMLRV :
            label = "AE Message (format: XML, version: " + message.getField("^ver^").data + ")";
            // final TibrvMsg m = (TibrvMsg) message.getField("^data^").data;
            children.add(new RvFieldTreeNode(this, message.getField("^xmldata^")));
            break;
        default:
            throw new IllegalArgumentException("Unsupported message format: " + data.getMessageFormat());
        }
        children.add(new RvFieldTreeNode(this, message.getField("^tracking^")));
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
        return label;
    }

}
