/*
 * Class:     Icons
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * All of the icons used by RvSnoop.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class Icons {

    private static final Log log = LogFactory.getLog(Icons.class);

    public static final Icon ADD_CONNECTION = createIcon("/resources/icons/add_connection.png");
    public static final Icon BUG = createIcon("/resources/icons/bug.png");
    public static final Icon COLUMNS_CORNER = createIcon("/resources/icons/columns_corner_button.png");
    public static final Icon EXPORT = createIcon("/resources/icons/export.png");
    public static final Icon FILTER_COLUMNS = createIcon("/resources/icons/filter_columns.png");
    public static final Icon IMPORT = createIcon("/resources/icons/import.png");
    public static final Icon PAUSE = createIcon("/resources/icons/pause.png");
    public static final Icon RESUME = createIcon("/resources/icons/resume.png");
    public static final Icon WEB = createIcon("/resources/icons/web.png");
    public static final Icon XML_ATTRIBUTE = createIcon("/resources/icons/xml_attribute.png"); //$NON-NLS-1$
    public static final Icon XML_ELEMENT = createIcon("/resources/icons/xml_element.png"); //$NON-NLS-1$

    public static Icon createIcon(String filename) {
        return new ImageIcon(createImage(filename));
    }

    private static Image createImage(String filename) {
        if (log.isDebugEnabled()) {
            log.debug("Loading image from " + filename);
        }
        return new ImageIcon(filename).getImage();
    }

    /** Do not instantiate. */
    private Icons() {
        throw new UnsupportedOperationException();
    }

}
