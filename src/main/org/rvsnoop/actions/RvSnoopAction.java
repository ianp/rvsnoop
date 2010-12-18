/*
 * Class:     RvSnoopAction
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

/**
 * An action in RvSnoop.
 * <p>
 * RvSnoop actions have a reference to the application instance that they are
 * running in. This is to allow multiple application instances (i.e. multiple
 * frames) to be run in a single JVM.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class RvSnoopAction extends AbstractAction {

    static { NLSUtils.internationalize(RvSnoopAction.class); }

    private static final Log log = LogFactory.getLog(RvSnoopAction.class);

    static String DEBUG_ACCELERATOR;
    protected static final int SHORTCUT_MASK =
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    protected final Application application;

    protected RvSnoopAction(String name, Application application) {
        super(name);
        this.application = application;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public abstract void actionPerformed(ActionEvent e);

    /**
     * Simple helper method to set the accelerator key from an I18N string.
     *
     * @param accelerator The accelerator to set.
     */
    protected final void putAcceleratorValue(String accelerator) {
        try {
            boolean shift =  false;
            boolean alt = false;
            while (accelerator.startsWith("SHIFT+") || accelerator.startsWith("ALT+")) {
                if (accelerator.startsWith("SHIFT+")) {
                    shift = true;
                    accelerator = accelerator.substring("SHIFT+".length());
                } else if (accelerator.startsWith("ALT+")) {
                    shift = true;
                    accelerator = accelerator.substring("ALT+".length());
                }
            }
            final int accel = Integer.decode(accelerator).intValue();
            final int mask = SHORTCUT_MASK
                    + (shift ? InputEvent.SHIFT_MASK : 0)
                    + (alt ? InputEvent.ALT_MASK : 0);
            final KeyStroke stroke = KeyStroke.getKeyStroke(accel, mask);
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(DEBUG_ACCELERATOR,
                        new Object[] { getValue(ACTION_COMMAND_KEY), stroke }));
            }
            putValue(Action.ACCELERATOR_KEY, stroke);
        } catch (NumberFormatException e) {
            // Do not set an accelerator key then.
        }
    }

    /**
     * Simple helper method to set the mnemonic key from an I18N string.
     *
     * @param mnemonic The mnemonic to set.
     */
    protected final void putMnemonicValue(String mnemonic) {
        try {
            putValue(Action.MNEMONIC_KEY, Integer.decode(mnemonic));
        } catch (NumberFormatException e) {
            // Do not set a mnemonic then.
        }
    }

    /**
     * Simple helper method to set the small icon.
     *
     * @param icon The icon to set.
     */
    protected final void putSmallIconValue(String icon) {
        try {
            putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/resources/icons/" + icon + ".png")));
        } catch (Exception e) {
            // Do not set an icon then.
        }
    }

}
