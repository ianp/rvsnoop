// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.ui;

import java.awt.ComponentOrientation;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
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

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import static com.google.common.collect.Lists.newArrayList;

/**
 * A panel component that can be used as a footer panel in a dialog or window.
 * <p>
 * The panel will configure borders and action maps correctly based on the
 * current platform.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
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
        List<JButton> buttons = newArrayList();
        if (extra != null) {
            for (int i = 0, imax = extra.length; i < imax; ++i) {
                buttons.add(i, new JButton(extra[i]));
            }
        }
        final Action leading = AppHelper.getPlatform() == PlatformType.WINDOWS ? ok : cancel;
        final Action trailing = AppHelper.getPlatform() == PlatformType.WINDOWS ? cancel : ok;
        if (trailing != null) { buttons.add(--numButtons, new JButton(trailing)); }
        if (leading != null)  { buttons.add(--numButtons, new JButton(leading));  }
        if (getComponentOrientation().equals(ComponentOrientation.RIGHT_TO_LEFT)) {
            Collections.reverse(buttons);
        }

        // Layout
        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        // Horizontal group
        final GroupLayout.SequentialGroup hgp = layout.createSequentialGroup();
        layout.setHorizontalGroup(hgp);
        hgp.addGap(1, 1, Integer.MAX_VALUE);
        for (int i = 0, imax = buttons.size(); i < imax; ++i) {
            hgp.addComponent(buttons.get(i));
        }
        // Vertical group
        final GroupLayout.ParallelGroup vgp = layout.createParallelGroup();
        layout.setVerticalGroup(vgp);
        for (int i = 0, imax = buttons.size(); i < imax; ++i) {
            vgp.addComponent(buttons.get(i)) ;
        }

        final Border outer = new MatteBorder(1, 0, 0, 0, UIManager.getColor("control"));
        final Border inner = new EmptyBorder(8, 8, 8, AppHelper.getPlatform() == PlatformType.OS_X ? 24 : 8);
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
