/*
 * Class:     HeaderPanel
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.jdesktop.layout.GroupLayout;


/**
 * A panel component that can be used as a header panel in a dialog or window.
 * <p>
 * The panel can also display warning and error notifications.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class HeaderPanel extends JPanel {
// TODO reduce visibility to package default.
    static final long serialVersionUID = 4415526231958850098L;

    private static final Color GRADIENT_END = new Color(0xDD, 0xDD, 0xFF);

//    public static Icon error =
//        new ImageIcon(ImageFactory.getInstance().getIconImage("error"));
//    public static Icon information =
//        new ImageIcon(ImageFactory.getInstance().getIconImage("information"));
//    public static Icon warning =
//        new ImageIcon(ImageFactory.getInstance().getIconImage("warning"));

    /** The cached gradient. */
    private GradientPaint gradient;

    private final JLabel banner = new JLabel();

    /** An optional message. */
    private final JTextArea description = new JTextArea();

    /** The main title text. */
    private final JLabel title = new JLabel();

    private final JLabel warning = new JLabel();

    /** Used to determine whether the gradient needs regenerating. */
    private int width;

    public HeaderPanel(String titleText, String descriptionText, ImageIcon bannerIcon) {
        final Color controlColour = UIManager.getColor("control");
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, controlColour),
                new EmptyBorder(4, 4, 4, 4)));
        setOpaque(true);
        title.setText(titleText);
        title.setOpaque(false);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder());
        description.setText(descriptionText);
        description.setOpaque(false);
        description.setBorder(BorderFactory.createEmptyBorder());
        description.setEditable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        banner.setIcon(bannerIcon);
        configureLayout();
        setPreferredSize(new Dimension(480, bannerIcon.getIconHeight() + 24));
    }

    private void configureLayout() {
        // Layout
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutocreateGaps(true);
        // Horizontal group
        GroupLayout.SequentialGroup hgp = layout.createSequentialGroup();
        layout.setHorizontalGroup(hgp);
        hgp.add(layout.createParallelGroup()
                .add(this.title, 1, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
                .add(layout.createSequentialGroup()
                    .add(warning)
                    .add(this.description, 1, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)))
            .add(banner);
        // Vertical group
        GroupLayout.ParallelGroup vgp = layout.createParallelGroup();
        layout.setVerticalGroup(vgp);
        vgp.add(layout.createSequentialGroup()
            .add(this.title)
            .add(layout.createParallelGroup()
                .add(warning)
                .add(this.description)))
            .add(banner);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque()) return;
        final int w = getWidth(), h = getHeight();
        final Graphics2D gg = (Graphics2D) g;
        final Paint storedPaint = gg.getPaint();
        if (gradient == null || width != w) {
            width = w;
            gradient = new GradientPaint(0, 0, Color.WHITE, w, h, GRADIENT_END);
        }
        gg.setPaint(gradient);
        gg.fillRect(0, 0, w, h);
        gg.setPaint(storedPaint);
    }

    public void setDescription(String text) {
        description.setText(text);
    }

    /**
     * Set the small icon that should be displayed next to the description text.
     * <p>
     * For convenience there are three standard icons provided with this class
     * to represent information, warning, and error messages.
     *
     * @param icon the icon to set, or {@code null} to remove the current icon.
     */
    public void setDescriptionIcon(Icon icon) {
        warning.setIcon(icon);
    }

    public void setTitle(String text) {
        title.setText(text);
    }
}
