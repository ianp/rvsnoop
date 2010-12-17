/*
 * Class:     MatcherEditorListCellRenderer
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import org.jdesktop.layout.GroupLayout;
import org.rvsnoop.matchers.Predicate;
import org.rvsnoop.matchers.RvSnoopMatcherEditor;

/**
 * A renderer for matchers.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class MatcherEditorListCellRenderer extends JPanel implements ListCellRenderer {
    // TODO add a checkbox to display the ignoringCase property

    private static final long serialVersionUID = 2729659498349769958L;

    private static final Icon BANNER_ICON =
        new ImageIcon(ImageFactory.getInstance().getBannerImage("defaultMatcherEditor"));

    private final JLabel imageLabel = new DefaultListCellRenderer();
    private final JLabel titleLabel = new DefaultListCellRenderer();
    private final JLabel valueLabel = new DefaultListCellRenderer();

    public MatcherEditorListCellRenderer() {
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setOpaque(true);
        Font f = titleLabel.getFont();
        imageLabel.setOpaque(false);
        titleLabel.setFont(f.deriveFont(Font.BOLD));
        titleLabel.setOpaque(false);
        valueLabel.setOpaque(false);
        configureLayout();
    }

    private void configureLayout() {
        // Layout
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutocreateGaps(true);
        // Horizontal group
        GroupLayout.SequentialGroup hgp = layout.createSequentialGroup();
        layout.setHorizontalGroup(hgp);
        hgp.add(imageLabel)
            .add(layout.createParallelGroup()
                .add(titleLabel)
                .add(valueLabel));
        // Vertical group
        GroupLayout.ParallelGroup vgp = layout.createParallelGroup();
        layout.setVerticalGroup(vgp);
        vgp.add(imageLabel)
            .add(layout.createSequentialGroup()
                .add(titleLabel)
                .add(valueLabel));
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof RvSnoopMatcherEditor) {
            final RvSnoopMatcherEditor m = (RvSnoopMatcherEditor) value;
            imageLabel.setIcon(BANNER_ICON);
            final Predicate predicate = m.getPredicate();
            titleLabel.setText(m.getDataAccessor().getDisplayName() + " " + predicate.getDisplayName());
            valueLabel.setText(predicate.getArgument());
        } else {
            imageLabel.setIcon(null);
            titleLabel.setText(" ");
            valueLabel.setText(" ");
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setBorder(null);
        return this;
    }

}
