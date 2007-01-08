/*
 * Class:     FooterPanel
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.apache.commons.lang.SystemUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * A panel component that can be used as a footer panel in a dialog or window.
 * <p>
 * The panel will configure borders and action maps correctly based on the
 * current platform.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class FooterPanel extends JPanel {
// TODO reduce visibility to package default

    private static final long serialVersionUID = -7824448104592355762L;

    private final Action ok;
    private final Action cancel;
    
    public FooterPanel(Action ok, Action cancel, Action[] extra) {
        this.ok = ok;
        this.cancel = cancel;
        int numButtons = extra != null ? extra.length : 0;
        if (ok != null) { ++numButtons; }
        if (cancel != null) { ++numButtons; }
        final JButton[] buttons = new JButton[numButtons];
        if (extra != null) {
            for (int i = 0, imax = extra.length; i < imax; ++i) {
                buttons[i] = new JButton(extra[i]);
            }
        }
        final Action leading = SystemUtils.IS_OS_WINDOWS ? ok : cancel;
        final Action trailing = SystemUtils.IS_OS_WINDOWS ? cancel : ok;
        if (trailing != null) { buttons[--numButtons] = new JButton(ok); }
        if (leading != null) { buttons[--numButtons] = new JButton(ok); }
        // The BBR handles component orientation internally.
        ButtonBarBuilder builder = new ButtonBarBuilder(this);
        builder.addGlue();
        builder.addGriddedButtons(buttons);
        final int top = LayoutStyle.getCurrent().getButtonBarPad().getPixelSize(this);
        final int left = Sizes.dluX(0).getPixelSize(this);
        final int bottom = Sizes.dluY(0).getPixelSize(this);
        int right = Sizes.dluX(0).getPixelSize(this);
        if (SystemUtils.IS_OS_MAC_OSX) { right += 16; }
        final Border outer = new MatteBorder(1, 0, 0, 0, UIManager.getColor("control"));
        final Border inner = new EmptyBorder(top, left, bottom, right);
        setBorder(new CompoundBorder(outer, inner));
    }

    /**
     * Configure the {@link ActionMap} and {@link InputMap} for the panel.
     * <p>
     * This should be called <em>after</em> the panel has been added to a
     * container.
     */
    public void configureActionMap() {
        final ActionMap actionMap = getActionMap();
        final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (cancel != null) {
            final Object command = cancel.getValue(Action.ACTION_COMMAND_KEY);
            actionMap.put(command, cancel);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), command);
        }
        if (ok != null) {
            final Object command = ok.getValue(Action.ACTION_COMMAND_KEY);
            actionMap.put(command, ok);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), command);
        }
    }

}
