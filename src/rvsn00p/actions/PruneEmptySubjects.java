//:File:    PruneEmptySubjects.java
//:Created: Dec 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import rvsn00p.SubjectElement;
import rvsn00p.SubjectHierarchy;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Remove any subject nodes from the tree that have no records in them.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
final class PruneEmptySubjects extends AbstractAction {

    public static final String ID = "pruneEmptySubjects";

    static String NAME = "Prune Subjects";

    private static final long serialVersionUID = -2325639617635989562L;
    
    static String TOOLTIP = "Remove any subject nodes from the tree that have no records in them";

    public PruneEmptySubjects() {
        super(NAME);
        putValue(Action.ACTION_COMMAND_KEY, ID);
        final int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JTree tree = RvSnooperGUI.getInstance().getCategoryExplorerTree();
        final SubjectHierarchy model = (SubjectHierarchy) tree.getModel();
        final Enumeration e = ((SubjectElement) model.getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            final SubjectElement child = (SubjectElement) e.nextElement();
            if (child.isLeaf() && child.getNumRecordsHere() == 0 && child.getParent() != null)
                child.removeFromParent();
        }
    }
    
}
