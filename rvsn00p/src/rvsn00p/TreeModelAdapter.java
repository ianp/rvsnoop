//:File:    TreeModelAdapter.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * Default listener implementation which provides no-op versions of all methods.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class TreeModelAdapter implements TreeModelListener {

    protected TreeModelAdapter() {
        super();
    }
    
    public void treeNodesChanged(TreeModelEvent e) {
        // no-op
    }

    public void treeNodesInserted(TreeModelEvent e) {
        // no-op
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        // no-op
    }

    public void treeStructureChanged(TreeModelEvent e) {
        // no-op
    }

}

