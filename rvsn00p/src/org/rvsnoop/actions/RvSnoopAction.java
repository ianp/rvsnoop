/*
 * Class:     RvSnoopAction
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.rvsnoop.Application;

/**
 * An action in RvSnoop.
 * <p>
 * RvSnoop actions have a reference to the application instance that they are
 * running in. This is to allow multiple application instances (i.e. multiple
 * frames) to be run in a single JVM.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class RvSnoopAction extends AbstractAction {

    protected final Application application;

    protected RvSnoopAction(String name, Application application) {
        super(name);
        this.application = application;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public abstract void actionPerformed(ActionEvent e);

}
