/*
 * Class:     HeaderPanel
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;


/**
 * A panel component that can be used as a header panel in a dialog or window.
 * <p>
 * The panel can also display warning and error notifications.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class HeaderPanel extends JPanel {
// TODO reduce visibility to package default.
    static final long serialVersionUID = 4415526231958850098L;

    private static final Color GRADIENT_END = new Color(0xDD, 0xDD, 0xFF);
    
    public static final Image IMAGE_ERROR =
        ImageFactory.getInstance().getIconImage("error");
    public static final Image IMAGE_INFORMATION =
        ImageFactory.getInstance().getIconImage("information");
    public static final Image IMAGE_WARNING =
        ImageFactory.getInstance().getIconImage("warning");
    
    /** The cached gradient. */
    private GradientPaint gradient;

    /** The main icon displayed to the right of the header panel. */
    private final ImageBorder imageBorder;

    /** The main icon displayed to the right of the header panel. */
    private final ImageBorder warningBorder;

    /** An optional message. */
    private final JTextArea description = new JTextArea();

    /** The main title text. */
    private final JLabel title = new JLabel();

    /** Used to determine whether the gradient needs regenerating. */
    private int width;

    public HeaderPanel(String title, String description, Image image) {
        super(new BorderLayout());
        imageBorder = new ImageBorder(0, 0, 0, 60, image, ImageBorder.RIGHT);
        warningBorder = new ImageBorder(2, 40, 0, 0, null, ImageBorder.HIDDEN);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("control")),
                imageBorder));
        setOpaque(true);
        this.title.setText(title);
        this.title.setOpaque(false);
        this.title.setFont(this.title.getFont().deriveFont(Font.BOLD));
        this.title.setBorder(BorderFactory.createEmptyBorder(2, 12, 0, 0));
        add(this.title, BorderLayout.NORTH);
        this.description.setText(description);
        this.description.setOpaque(false);
        this.description.setBorder(warningBorder);
        this.description.setEditable(false);
        add(this.description, BorderLayout.CENTER);
        setPreferredSize(new Dimension(480, image.getHeight(this) + 24));
    }

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
     * @param image The image to set, or <code>null</code> to remove the current
     *     image.
     */
    public void setDescriptionIcon(Image image) {
        warningBorder.setImage(image);
        warningBorder.setImagePosition(
                image != null ? ImageBorder.LEFT : ImageBorder.HIDDEN);
    }

    public void setTitle(String text) {
        title.setText(text);
    }
}
