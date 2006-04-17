//:File:    StatuaBar.java
//:Created: Oct 9, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * A status bar capable of displaying notifications to the user.
 * <p>
 * The message area should be used for general status messages and the activity
 * area ahould be used to tell the user that a background thread is processing
 * something. A status bar may have additional fields which can have their
 * labels set individually.
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class StatusBar extends JPanel {

    private class FullWidthLabel extends JLabel {
        private static final long serialVersionUID = 6925029135681793153L;

        public FullWidthLabel() {
            super(StatusBar.NO_MESSAGE);
        }

        public Dimension getPreferredSize() {
            final Dimension d = super.getPreferredSize();
            d.width = StatusBar.this.getWidth();
            return d;
        }
    }

    public class StatusBarItem {
        final JLabel label;
        StatusBarItem(JLabel label) {
            this.label = label;
        }
        public Icon getIcon() {
            return label.getIcon();
        }
        public String getText() {
            return label.getText();
        }
        public String getToolTipText() {
            return label.getToolTipText();
        }
        public void set(String text, String tooltip, Icon icon) {
            label.setText(text != null ? text : NO_MESSAGE);
            label.setToolTipText(tooltip);
            label.setIcon(icon);
        }
    }

    private static final Border FIELD_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(0, 2, 0, 2));

    private static final Icon ICON_WARNING = Icons.createIcon("/resources/icons/warning.png", 12);

    private static final String NO_MESSAGE = " "; //$NON-NLS-1$
    
    private static final long serialVersionUID = 7014627089362478531L;

    private final JLabel activity = new JLabel(NO_MESSAGE, SwingConstants.TRAILING);

    private GradientPaint backgroundGradient;

    private final Color bgStart, bgEnd;

    /**
     * The font that will be used should be small enough to fit into a 16 pixel
     * high status bar.
     */
    private Font font;

    private List items;

    private final JLabel message = new FullWidthLabel();

    /**
     * Create a new <code>StatusBar</code>.
     */
    public StatusBar() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        bgStart = Color.WHITE;
        bgEnd = new Color(224, 224, 224);
        message.setBorder(BorderFactory.createEmptyBorder());
        message.setOpaque(false);
        activity.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(0, 2, 0, 18)));
        activity.setOpaque(false);
        relayout();
        setOpaque(false);
        Dimension d = getPreferredSize();
        d.height = 16;
        setPreferredSize(d);
    }

    /**
     * Add an item to this status bar.
     */
    public StatusBarItem createItem() {
        if (items == null)
            items = new ArrayList(1);
        final StatusBarItem item = new StatusBarItem(new JLabel());
        item.label.setBorder(FIELD_BORDER);
        item.label.setOpaque(false);
        items.add(item);
        // This will cause the correct font to be set on the new label.
        font = null;
        relayout();
        return item;
    }

    /**
     * Get the message currently displayed in the status bar.
     * 
     * @return The currently displayed text.
     */
    public String getMessage() {
        return message.getText();
    }

    /**
     * Make sure that the font is correctly sized and then call the super
     * implementation.
     * 
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
        if (font == null)
            resizeFont(((Graphics2D) g).getFontRenderContext());
        backgroundGradient = UIUtils.paintGradient((Graphics2D) g, getWidth(), getHeight(), bgStart, bgEnd, backgroundGradient);
        super.paintComponent(g);
    }

    /**
     * 
     */
    private void relayout() {
        removeAll();
        add(message);
        if (items != null)
            for (int i = 0, imax = items.size(); i < imax; ++i)
                add(((StatusBarItem) items.get(i)).label);
        add(activity);
        validate();
    }

    /**
     * Remove an item from the status bar.
     * 
     * @param item The item to remove.
     * @return <code>true</code> if the field was removed, <code>false</code>
     *         if it could not be found in this status bar.
     * @throws NullPointerException if <code>item</code> is null.
     */
    public boolean removeItem(StatusBarItem item) {
        if (item == null)
            throw new NullPointerException();
        if (items == null)
            return false;
        final int size = items.size();
        items.remove(item);
        if (items.size() != size) {
            relayout();
            return true;
        }
        return false;
    }

    private void resizeFont(FontRenderContext frc) {
        font = getFont();
        // Just use some sample letters to resize the font, this isn't ideal from an
        // I18N perspective, but it's better than relying on a user generated string
        // which may be empty.
        while (font.getLineMetrics("ABCDEFgHIjKLMNOPqRSTUVWXyZ", frc).getHeight() >= 14.0f) //$NON-NLS-1$
            font = font.deriveFont(font.getSize2D() - 1.0f);
        setFont(font);
        activity.setFont(font);
        message.setFont(font);
        if (items != null)
            for (int i = 0, imax = items.size(); i < imax; ++i)
                ((StatusBarItem) items.get(i)).label.setFont(font);
    }

    public void setMessage(String text) {
        message.setIcon(null);
        if (text != null && text.length() > 0) {
            message.setText(text);
            message.setToolTipText(text);
        } else {
            message.setText(NO_MESSAGE);
            message.setToolTipText(null);
        }
    }
    
    public void setWarning(String text) {
        setMessage(text);
        message.setIcon(ICON_WARNING);
    }

}
