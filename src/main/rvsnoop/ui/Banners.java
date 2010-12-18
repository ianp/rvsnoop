/*
 * Class:     Banners
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import javax.swing.*;

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
    public static final Icon MESSAGE = new ImageIcon("/resources/banners/message.png");
    /**
     * Advisory message banner for details panel.
     */
    public static final Icon MESSAGE_ADVISORY = new ImageIcon("/resources/banners/message_advisory.png");
    /**
     * Certified messaging message banner for details panel.
     */
    public static final Icon MESSAGE_CM = new ImageIcon("/resources/banners/message_rvcm.png");
    /**
     * Default empty banner for details panel.
     */
    public static final Icon MESSAGE_NONE = new ImageIcon("/resources/banners/message_none.png");
    /**
     * Fault tolerant message banner for details panel.
     */
    public static final Icon MESSAGE_FT = new ImageIcon("/resources/banners/message_rvft.png");
    /**
     * Bannder for confirm quit dialog.
     */
    public static final Icon QUIT = new ImageIcon("/resources/banners/quit.png");

    /**
     * Do not instantiate.
     */
    private Banners() {
        throw new UnsupportedOperationException();
    }

}
