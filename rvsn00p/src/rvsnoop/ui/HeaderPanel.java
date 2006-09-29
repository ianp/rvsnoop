//:File:    HeaderPanel.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * A panel component that can be used as a header panel in a dialog or window.
 * <p>
 * The panel can also display warning and error notifications.
 *
 * TODO: Allow warning and error icons to be added by using a custom border on
 *       the message text area.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class HeaderPanel extends JPanel {

    static final long serialVersionUID = 4415526231958850098L;

    /** The cached gradient. */
    private GradientPaint gradient;

    /** The main icon displayed to the right of the header panel. */
    private final JLabel mainIcon = new JLabel();

    /** An optional message. */
    private final JTextArea message = new JTextArea();

    /** The main title text. */
    private final JLabel title = new JLabel();

    /** Used to determine whether the gradient needs regenerating. */
    private int width;

    public HeaderPanel(Icon icon) {
        super(new BorderLayout());
        add(mainIcon, BorderLayout.EAST);
        mainIcon.setOpaque(false);
        mainIcon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        mainIcon.setIcon(icon);
        title.setOpaque(false);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        message.setOpaque(false);
        message.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(title, BorderLayout.NORTH);
        inner.add(message, BorderLayout.CENTER);
        add(inner, BorderLayout.CENTER);
        setOpaque(true);
        setPreferredSize(new Dimension(480, icon.getIconHeight() + 24));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                javax.swing.UIManager.getColor("control")));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque()) return;
        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        Paint storedPaint = g2.getPaint();
        if (gradient == null || width != w) {
            width = w;
            gradient = new GradientPaint(0, 0, Color.WHITE, w, h,
                    javax.swing.UIManager.getColor("control"));
        }
        g2.setPaint(gradient);
        g2.fillRect(0, 0, w, h);
        g2.setPaint(storedPaint);
    }

    public void setMessage(String text) {
        message.setText(text);
    }

    public void setTitle(String text) {
        title.setText(text);
    }

}
