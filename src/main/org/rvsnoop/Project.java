// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.io.File;

/**
 * Handles all of the project specific settings.
 */
public final class Project {

    private final File projectDirectory;

    public Project(File file) {
        this.projectDirectory = file;
    }

    public File getDirectory() {
        return projectDirectory;
    }

}
