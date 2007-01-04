/*
 * Class:     MultiLineToolTipUI
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;

import rvsnoop.StringUtils;

public final class MultiLineToolTipUI extends ToolTipUI {

    private static final int INSET = 2;

    private static final MultiLineToolTipUI INSTANCE = new MultiLineToolTipUI();

    public static void configure() {
        final String toolTipUI = MultiLineToolTipUI.class.getName();
        UIManager.put("ToolTipUI", toolTipUI);
        UIManager.put(toolTipUI, MultiLineToolTipUI.class);
    }

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    private MultiLineToolTipUI() {
        super();
    }

    public Dimension getMaximumSize(JComponent c) {
        return getPreferredSize(c);
    }

    public Dimension getMinimumSize(JComponent c) {
        return getPreferredSize(c);
    }

    public Dimension getPreferredSize(JComponent c) {
        int w = 0, h = 0;
        final String[] lines = StringUtils.split(((JToolTip) c).getTipText());
        if (lines.length > 0) {
            final Font font = c.getFont();
            final FontMetrics fontMetrics = c.getFontMetrics(font);
            h += lines.length * fontMetrics.getHeight();
            final Graphics g = c.getGraphics();
            w = 0;
            for (int i = 0, imax = lines.length; i < imax; ++i)
                w = Math.max(w, (int) fontMetrics.getStringBounds(lines[i], g).getWidth());
        }
        return new Dimension(w + 2 * INSET, h + 2 * INSET);
    }

    public void installUI(JComponent c) {
        LookAndFeel.installColorsAndFont(c, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        LookAndFeel.installBorder(c, "ToolTip.border");
    }

    public void paint(Graphics g, JComponent c) {
        final int w = c.getWidth(), h = c.getHeight();
        g.setColor(c.getBackground());
        g.fillRect(0, 0, w, h);
        g.setColor(c.getForeground());
        g.drawRect(0, 0, w, h);
        final String[] lines = StringUtils.split(((JToolTip) c).getTipText());
        if (lines.length > 0) {
            final Font font = c.getFont();
            final FontMetrics fontMetrics = c.getFontMetrics(font);
            final int fontHeight = fontMetrics.getHeight();
            int y = INSET + fontMetrics.getAscent();
            for (int i = 0, imax = lines.length; i < imax; ++i) {
                g.drawString(lines[i], INSET, y);
                y += fontHeight;
            }
        }
    }

    public void uninstallUI(JComponent c) {
        LookAndFeel.uninstallBorder(c);
    }

}