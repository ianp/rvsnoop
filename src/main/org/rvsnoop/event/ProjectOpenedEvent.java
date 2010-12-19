// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.event;

import org.rvsnoop.Project;

import java.util.EventObject;

/**
 * Event fired whenever a new project has been opened.
 */
public final class ProjectOpenedEvent extends EventObject {

    private static final long serialVersionUID = 2L;

    public ProjectOpenedEvent(Project project) {
        super(project);
    }

    @Override
    public Project getSource() {
        return (Project) super.getSource();
    }

}
