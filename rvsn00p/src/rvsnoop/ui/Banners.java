//:File:    Banners.java
//:Created: Dec 13, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import javax.swing.Icon;

/**
 * All of the icons used by rvSnoop.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 12/30/05 10:21 PM
public final class Banners {

    private static final int ICON_SIZE = 48;

    /**
     * Standard message banner for details panel.
     */
    public static final Icon EDIT_TYPES = Icons.createIcon("/resources/banners/edit_types.png", ICON_SIZE);

    /**
     * Standard message banner for details panel.
     */
    public static final Icon MESSAGE = Icons.createIcon("/resources/banners/message.png", ICON_SIZE);
    /**
     * Advisory message banner for details panel.
     */
    public static final Icon MESSAGE_ADVISORY = Icons.createIcon("/resources/banners/message_advisory.png", ICON_SIZE);
    /**
     * Certified messaging message banner for details panel.
     */
    public static final Icon MESSAGE_CM = Icons.createIcon("/resources/banners/message_rvcm.png", ICON_SIZE);
    /**
     * Default empty banner for details panel.
     */
    public static final Icon MESSAGE_NONE = Icons.createIcon("/resources/banners/message_none.png", ICON_SIZE);
    /**
     * Fault tolerant message banner for details panel.
     */
    public static final Icon MESSAGE_FT = Icons.createIcon("/resources/banners/message_rvft.png", ICON_SIZE);
    /**
     * Bannder for confirm quit dialog.
     */
    public static final Icon QUIT = Icons.createIcon("/resources/banners/quit.png", ICON_SIZE);
    /**
     * Banner for update check dialog.
     * This image is used when no update is available.
     */
    public static final Icon UPDATE_ALREADY = Icons.createIcon("/resources/banners/update_already.png", ICON_SIZE);
    /**
     * Banner for update check dialog.
     * This image is used when a new update is available.
     */
    public static final Icon UPDATE_AVAILABLE = Icons.createIcon("/resources/banners/update_available.png", ICON_SIZE);

    /**
     * Do not instantiate.
     */
    private Banners() {
        throw new UnsupportedOperationException();
    }

}
