//:File:    ColourButtonTableCellRenderer.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer that uses a {@link JButton} to display a cell's
 * contents. The cell is assumed to hold a {@link Color} value.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class ColourButtonTableCellRenderer extends JLabel implements
        TableCellRenderer {

//    private static class ColourButton extends JButton {
//
//        static final long serialVersionUID = 7720164337058681575L;
//
//        ColourButton() {
//            setOpaque(true);
//        }
//        TODO: override paintComponent then we don't need to generate the
//              solid colour icon anymore, then remove the map.
//
//        /** Overridden for performance reasons. */
//        public void repaint(long tm, int x, int y, int width, int height) {
//            // Do nothing.
//        }
//
//        /** Overridden for performance reasons. */
//        public void repaint(Rectangle r) {
//            // Do nothing.
//        }
//
//        /** Overridden for performance reasons. */
//        public void revalidate() {
//            // Do nothing.
//        }
//
//        /**
//         * Notification from the <code>UIManager</code> that the look and feel
//         * [L&F] has changed. Replaces the current UI object with the latest version
//         * from the <code>UIManager</code>.
//         *
//         * @see JComponent#updateUI
//         */
//        public void updateUI() {
//            super.updateUI();
//            setForeground(null);
//            setBackground(null);
//        }
//
//        /** Overridden for performance reasons. */
//        public void validate() {
//            // Do nothing.
//        }
//    }

    static final long serialVersionUID = 8657616060311680421L;

//    private final JButton button = new ColourButton();

//    private final Map icons = new HashMap(); // <Color, Icon>

    /**
     * Creates a colour button table cell renderer.
     */
    public ColourButtonTableCellRenderer() {
//        super(new BorderLayout());
        setOpaque(true);
//        add(button, BorderLayout.CENTER);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        final Dimension size = new Dimension(16, 16);
        setMaximumSize(size);
        setMinimumSize(size);
        setPreferredSize(size);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Color colour = (Color) value;
//        Icon icon = (Icon) icons.get(colour);
//        if (icon == null) {
//            icon = UIUtils.createSolidColorIcon(16, 16, colour, Color.BLACK);
//            icons.put(colour, icon);
//        }
//        button.setFont(table.getFont());
//        button.setIcon(icon);
        setBackground(colour);
        return this;
    }

    /** Overridden for performance reasons. */
    public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) p = p.getParent();
        boolean colorMatch = (back != null) && (p != null)
                && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }

    /** Overridden for performance reasons. */
    public void repaint(long tm, int x, int y, int width, int height) {
        // Do nothing.
    }

    /** Overridden for performance reasons. */
    public void repaint(Rectangle r) {
        // Do nothing.
    }

    /**
     * Notification from the <code>UIManager</code> that the look and feel
     * [L&F] has changed. Replaces the current UI object with the latest version
     * from the <code>UIManager</code>.
     *
     * @see JComponent#updateUI
     */
    public void updateUI() {
        super.updateUI();
        setForeground(null);
        setBackground(null);
    }

}
