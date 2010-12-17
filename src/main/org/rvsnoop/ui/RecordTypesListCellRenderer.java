/*
 * Class:     RecordTypesListCellRenderer
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

import org.apache.commons.lang.text.StrBuilder;
import org.jdesktop.layout.GroupLayout;

import rvsnoop.RecordType;

/**
 * A renderer for record types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RecordTypesListCellRenderer extends JPanel implements ListCellRenderer {

    private static final long serialVersionUID = -7242365820031903080L;

    private static final Icon BANNER_DESELECTED =
        new ImageIcon(ImageFactory.getInstance().getBannerImage("recordTypeDeselected"));
    private static final Icon BANNER_SELECTED =
        new ImageIcon(ImageFactory.getInstance().getBannerImage("recordTypeSelected"));

    private final StrBuilder builder = new StrBuilder();

    private final JLabel imageLabel = new DefaultListCellRenderer();
    private final JLabel titleLabel = new DefaultListCellRenderer();
    private final JLabel valueLabel = new DefaultListCellRenderer();

    public RecordTypesListCellRenderer() {
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setOpaque(true);
        Font f = titleLabel.getFont();
        imageLabel.setOpaque(true);
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
        if (value instanceof RecordType) {
            final RecordType type = (RecordType) value;
            if (type.isSelected()) {
                imageLabel.setIcon(BANNER_SELECTED);
            } else {
                imageLabel.setIcon(BANNER_DESELECTED);
            }
            imageLabel.setBackground(type.getColour());
            titleLabel.setText(type.getName());
            builder.append(type.getMatcherName());
            final String mv = type.getMatcherValue();
            if (mv != null && mv.length() > 0) {
                builder.append(' ').append('\u2018').append(mv).append('\u2019');
            }
            valueLabel.setText(builder.toString());
            builder.setLength(0);
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
