//:File:    MultiLineToolTip.java
//:Created: Dec 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.LookAndFeel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;

import rvsnoop.Logger;
import rvsnoop.StringUtils;

/**
 * A tooltip which can contain multiple lines of text.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class MultiLineToolTip extends JToolTip {

    private static final Logger logger = Logger.getLogger(MultiLineToolTip.class);
    
    private static final class MultiLineToolTipUI extends ToolTipUI {

        private static final float INSET = 2.0f;

        public static ComponentUI createUI(JComponent c) {
            return INSTANCE;
        }

        int inset = 3;

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
            
            final String lines[] = StringUtils.split(((JToolTip) c).getTipText());
            final Graphics2D g2 = (Graphics2D) c.getGraphics();
            final Font font = c.getFont();
            FontRenderContext frc;
            // In case c.getGraphics() return null.
            // This can happen on even recent versions of AIX with Java 1.4.2.
            if (g2 != null)
                frc = g2.getFontRenderContext();
            else
                frc = new FontRenderContext(new AffineTransform(), true, false);
            final LineMetrics metrics = font.getLineMetrics(lines[0], frc);
            final float lineHeight = metrics.getHeight();
            float height = 2 * metrics.getAscent();
            double width = 2 * INSET;
            for (int i = 0, imax = lines.length; i < imax; ++i) {
                height += lineHeight;
                width = Math.max(width, font.getStringBounds(lines[i], frc).getWidth());
            }
            return new Dimension((int) width, (int)height);
        }

        public void installUI(JComponent c) {
            LookAndFeel.installColorsAndFont(c, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
            LookAndFeel.installBorder(c, "ToolTip.border");
        }
        
        public void paint(Graphics g, JComponent c) {
            final String text = ((JToolTip) c).getTipText();
            if (logger.isDebugEnabled()) logger.debug("Rendering tooltip: " + text);
            final String lines[] = StringUtils.split(text);
            final Graphics2D g2 = (Graphics2D) g;
            final LineMetrics metrics = c.getFont().getLineMetrics(lines[0], g2.getFontRenderContext());
            final float lineHeight = metrics.getHeight();
            g.setColor(c.getBackground());
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.setColor(c.getForeground());
            g.drawRect(0, 0, c.getWidth(), c.getHeight());
            float y = 2 * metrics.getAscent();
            for (int i = 0, imax = lines.length; i < imax; ++i)
                g2.drawString(lines[i], INSET, y += lineHeight);
        }

        public void uninstallUI(JComponent c) {
            LookAndFeel.uninstallBorder(c);
        }

    }

    private static final MultiLineToolTipUI INSTANCE = new MultiLineToolTipUI();

    private static final long serialVersionUID = -3254979118449901073L;

    public MultiLineToolTip() {
        super();
    }

    public void updateUI() {
        setUI(MultiLineToolTipUI.createUI(this));
    }

}
