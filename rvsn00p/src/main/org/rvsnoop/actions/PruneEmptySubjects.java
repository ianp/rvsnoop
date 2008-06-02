/*
 * Class:     PruneEmptySubjects
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.Action;
import javax.swing.tree.DefaultMutableTreeNode;

import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;

/**
 * Remove any subject nodes from the tree that have no records in them.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class PruneEmptySubjects extends RvSnoopAction {

    static { NLSUtils.internationalize(PruneEmptySubjects.class); }

    private static final long serialVersionUID = 3578559190520608685L;

    public static final String COMMAND = "pruneEmptySubjects";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;

    public PruneEmptySubjects(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final SubjectHierarchy hierarchy = application.getSubjectHierarchy();
        final Enumeration e = ((DefaultMutableTreeNode) hierarchy.getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            final SubjectElement child = (SubjectElement) e.nextElement();
            if (child.isLeaf() && child.getNumRecordsHere() == 0 && child.getParent() != null) {
                hierarchy.removeNodeFromParent(child);
            }
        }
    }

}
