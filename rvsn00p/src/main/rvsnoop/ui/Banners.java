/*
 * Class:     Banners
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import javax.swing.Icon;

/**
 * All of the banners used by RvSnoop.
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
     * Do not instantiate.
     */
    private Banners() {
        throw new UnsupportedOperationException();
    }

}
