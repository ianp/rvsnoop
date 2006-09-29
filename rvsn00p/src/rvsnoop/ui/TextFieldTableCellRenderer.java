//:File:    TextFieldTableCellRenderer.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer that uses a {@link JTextField} to display a cell's
 * contents.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class TextFieldTableCellRenderer extends JTextField implements
        TableCellRenderer {

    static final long serialVersionUID = 3574857347734834257L;

    /**
     * Creates a text field table cell renderer.
     */
    public TextFieldTableCellRenderer() {
        super();
        setOpaque(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setFont(table.getFont());
        setText(value == null ? "" : value.toString());
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

    /** Overridden for performance reasons. */
    public void revalidate() {
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

    /** Overridden for performance reasons. */
    public void validate() {
        // Do nothing.
    }

}
