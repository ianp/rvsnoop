//:File:    ProjectFileFilter.java
//:Created: Jan 17, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.io.File;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

/**
 * Filter for project files.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
// Class provides static instance instead of factory method.
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 1/17/06 2:00 PM
public class ProjectFileFilter extends FileFilter {
    public static final ProjectFileFilter INSTANCE = new ProjectFileFilter();
    
    private ProjectFileFilter() {
        super();
    }

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".rsp");
    }

    public String getDescription() {
        return "rvSnoop Project Files";
    }
}
