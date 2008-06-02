/*
 * Class:     AeMessageTreeNode
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.rvsnoop.ui.ImageFactory;

import com.tibco.sdk.MMessageFormat;
import com.tibco.sdk.MTree;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;

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
public final class AeMessageTreeNode extends LazyTreeNode {

    private static final Icon messageIcon = new ImageIcon(ImageFactory.getInstance().getIconImage("rvMessage"));
    private static final Icon payloadIcon = new ImageIcon(ImageFactory.getInstance().getIconImage("payload"));
    private static final Icon trackingIcon = new ImageIcon(ImageFactory.getInstance().getIconImage("tracking"));

    static { NLSUtils.internationalize(AeMessageTreeNode.class); }

    static String AERV, RV, XMLJMS, XMLRV, UNKNOWN, TRACKING, DATA;

    private final List children = new ArrayList(2);

    private final String text;

    public AeMessageTreeNode(TibrvMsg message) throws TibrvException {
        super(null);
        final MTree data = new MTree("");
        data.use_tibrvMsg(message);
        final int format = data.getMessageFormat();
        if (format == MMessageFormat.AERV) {
            text = MessageFormat.format(AERV, new Object[] { message.getField("^ver^").data });
        } else if (format == MMessageFormat.RV) {
            text = MessageFormat.format(RV, new Object[] { message.getField("^ver^").data });
        } else if (format == MMessageFormat.XMLJMS) {
            text = MessageFormat.format(XMLJMS, new Object[] { message.getField("^ver^").data });
        } else if (format == MMessageFormat.XMLRV) {
            text = MessageFormat.format(XMLRV, new Object[] { message.getField("^ver^").data });
        } else {
            text = UNKNOWN;
        }
        children.add(new RvFieldTreeNode(this, message.getField("^data^"), DATA, payloadIcon));
        children.add(new RvFieldTreeNode(this, message.getField("^tracking^"), TRACKING, trackingIcon));
    }


    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#createChildren()
     */
    @Override
    protected List createChildren() {
        return children;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getAllowsChildren()
     */
    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public Icon getIcon() {
        return messageIcon;
    }

    /* (non-Javadoc)
     * @see rvsnoop.LazyTreeNode#getText()
     */
    @Override
    public String getText() {
        return text;
    }

}
