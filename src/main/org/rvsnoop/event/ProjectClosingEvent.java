// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.event;

import org.rvsnoop.ProjectService;

import java.util.EventObject;

/**
 * Event fired whenever a project is being closed.
 */
public class ProjectClosingEvent extends EventObject {

    public ProjectClosingEvent(ProjectService projectService) {
        super(projectService);
    }

    @Override
    public ProjectService getSource() {
        return (ProjectService) super.getSource();
    }

}
