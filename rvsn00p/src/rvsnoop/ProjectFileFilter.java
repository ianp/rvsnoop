/*
 * Class:     ProjectFileFilter
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
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
public final class ProjectFileFilter extends FileFilter {

    public ProjectFileFilter() {
        super();
    }

    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".rsp");
    }

    public String getDescription() {
        return "RvSnoop Project Files";
    }
}
