//:File:    Banners.java
//:Created: Dec 13, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import javax.swing.Icon;

/**
 * All of the icons used by RvSn00p.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 12/30/05 10:21 PM
public final class Banners {

    private static final int ICON_SIZE = 48;

    public static final Icon MESSAGE = Icons.createIcon("/resources/banners/message.png", ICON_SIZE);
    public static final Icon MESSAGE_ADVISORY = Icons.createIcon("/resources/banners/message_advisory.png", ICON_SIZE);
    public static final Icon MESSAGE_CM = Icons.createIcon("/resources/banners/message_rvcm.png", ICON_SIZE);
    public static final Icon MESSAGE_NONE = Icons.createIcon("/resources/banners/message_none.png", ICON_SIZE);
    public static final Icon MESSAGE_FT = Icons.createIcon("/resources/banners/message_rvft.png", ICON_SIZE);
    public static final Icon QUIT = Icons.createIcon("/resources/banners/quit.png", ICON_SIZE);
    public static final Icon UPDATE_ALREADY = Icons.createIcon("/resources/banners/update_already.png", ICON_SIZE);
    public static final Icon UPDATE_AVAILABLE = Icons.createIcon("/resources/banners/update_available.png", ICON_SIZE);
    
    /**
     * Do not instantiate.
     */
    private Banners() {
        throw new UnsupportedOperationException();
    }

}
