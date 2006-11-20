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

    static final long serialVersionUID = 8657616060311680421L;

    /**
     * Creates a colour button table cell renderer.
     */
    public ColourButtonTableCellRenderer() {
        setOpaque(true);
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
        setBackground((Color) value);
        return this;
    }

    /** Overridden for performance reasons. */
    public boolean isOpaque() {
        final Color back = getBackground();
        Component p = getParent();
        if (p != null) p = p.getParent();
        final boolean colorMatch = (back != null) && (p != null)
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
