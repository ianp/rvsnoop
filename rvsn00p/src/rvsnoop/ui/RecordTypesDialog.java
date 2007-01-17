/*
 * Class:     RecordTypesDialog
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.actions.EditRecordTypes;
import org.rvsnoop.ui.FooterPanel;
import org.rvsnoop.ui.HeaderPanel;
import org.rvsnoop.ui.ImageFactory;

import rvsnoop.RecordMatcher;
import rvsnoop.RecordType;
import rvsnoop.RecordTypes;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

/**
 * A dialog to allow the record types in a project to be customized.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class RecordTypesDialog extends JDialog {

    private class ColourEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private static final String EDIT = "edit";
        static final long serialVersionUID = -5137692296675824971L;
        private final JButton button = new JButton();
        private JColorChooser colorChooser;
        private Color currentColor;
        private JDialog dialog;

        public ColourEditor() {
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);
            final Dimension size = new Dimension(16, 16);
            button.setMaximumSize(size);
            button.setMinimumSize(size);
            button.setPreferredSize(size);
        }

        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                if (colorChooser == null) {
                    colorChooser = new JColorChooser();
                    dialog = JColorChooser.createDialog(button,
                            "Pick a Color", true, colorChooser, this, null);
                }
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);
                fireEditingStopped();
            } else {
                currentColor = colorChooser.getColor();
            }
        }

        public Object getCellEditorValue() {
            return currentColor;
        }

        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            currentColor = (Color)value;
            return button;
        }
    }

    private class DeleteEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private static final String EDIT = "edit";
        static final long serialVersionUID = 1812389580066670903L;
        private final JButton button = new JButton(Icons.DELETE);
        private int row = -1;

        public DeleteEditor() {
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                fireEditingStopped();
                if (row >= 0) {
                    final RecordTypes types = RecordTypes.getInstance();
                    types.removeType(types.getType(row));
                }
            }
        }

        public Object getCellEditorValue() {
            return Icons.DELETE;
        }

        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }
    }

    private class OKAction extends AbstractAction {
        static final long serialVersionUID = -652700991471360994L;

        OKAction() {
            super("OK");
        }

        public void actionPerformed(ActionEvent e) {
            RecordTypesDialog.this.setVisible(false);
        }

    }

    private class TypeMouseHandler extends GhostDropPane.MouseHandler {
        private Image image;
        private Point imageOffset;
        private long lastDragTime;
        private RecordType type;

        TypeMouseHandler(final Component component) {
            glass.super(component);
        }

        protected Image createImage() {
            return image;
        }

        protected Point createImageOffset() {
            return imageOffset;
        }

        private int getDropTarget(final Point point) {
            final int row = typesTable.rowAtPoint(point);
            if (row != -1) return row;
            SwingUtilities.convertPointToScreen(point, typesTable);
            final Point location = typesTable.getLocationOnScreen();
            if (point.y < location.y) return 0;
            return RecordTypes.getInstance().size();
        }

        /** Set the location for the drop target line to be drawn. */
        public void mouseDragged(final MouseEvent event) {
            dropIndex = getDropTarget(event.getPoint());
            lastDragTime = event.getWhen();
            super.mouseDragged(event);
        }

        /**
         * Cancel the drop target 250ms after the last drag gesture.
         *
         * @see rvsnoop.ui.GhostDropPane.MouseHandler#mouseMoved(java.awt.event.MouseEvent)
         */
        public void mouseMoved(final MouseEvent event) {
            if (event.getWhen() > lastDragTime + 250) dropIndex = -1;
        }

        public void mousePressed(final MouseEvent event) {
            final Point point = event.getPoint();
            type = RecordTypes.getInstance().getType(getDropTarget(event.getPoint()));
            // Create the image that will be used for ghosting
            final Rectangle bounds = typesTable.getCellRect(dropIndex, 0, true);
            bounds.add(typesTable.getCellRect(dropIndex, 5, true));
            final Point location = bounds.getLocation();
            SwingUtilities.convertPointToScreen(location, component);
            bounds.setLocation(location);
            try {
                image = new Robot().createScreenCapture(bounds);
            } catch (Exception e) {
                // If robot isn't supported by the graphics configuration
                // just use a plain gray rectangle of the correct size.
                if (log.isDebugEnabled()) {
                    log.debug("Robotic image capture failed, using fallback method.", e);
                }
                image = new BufferedImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
                final Graphics2D g = (Graphics2D) image.getGraphics();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, bounds.width, bounds.height);
                g.dispose();
            }
            // Set the offset so that the image doesn't jump away from the cursor
            imageOffset = new Point(point.x / 2, image.getHeight(null) / 2);
            super.mousePressed(event);
        }

        /** Clear the location for the drop target line. */
        public void mouseReleased(MouseEvent event) {
            super.mouseReleased(event);
            dropIndex = getDropTarget(event.getPoint());
            RecordTypes.getInstance().reorderType(type, dropIndex);
            type = null;
            dropIndex = -1;
        }

    }

    /**
     * A table that draws a line over the drop index.
     */
    private class TypeTable extends JTable {
        static final long serialVersionUID = 5262986877021980127L;

        TypeTable(TableModel model) {
            super(model);
        }

        public void paint(Graphics g) {
            super.paint(g);
            if (dropIndex < 0) return;
            final Rectangle bounds = this.getCellRect(dropIndex, 0, true);
            bounds.add(getCellRect(dropIndex, 5, true));
            final Color color = g.getColor();
            g.setColor(Color.BLACK);
            g.fillRect(bounds.x, bounds.y, bounds.width, 2);
            g.setColor(color);
        }
    }

    private class TypeTableFormat implements AdvancedTableFormat, WritableTableFormat {
        TypeTableFormat() {
            super();
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
         */
        public Class getColumnClass(int column) {
            switch (column) {
            case 0: return Boolean.class; // is selected
            case 1: return String.class;  // name
            case 2: return Color.class;   // colour
            case 3: return String.class;  // matcher name
            case 4: return String.class;  // matcher value
            case 5: return Icon.class;    // delete button
            default: throw new IndexOutOfBoundsException();
            }
        }

        /**
         * As the table is not sortable, this always returns <code>null</code>.
         *
         * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
         */
        public Comparator getColumnComparator(int column) {
            return null;
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
         */
        public int getColumnCount() {
            return 6;
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
         */
        public String getColumnName(int column) {
            switch (column) {
            case 0: return " ";
            case 1: return "Type Name";
            case 2: return " ";
            case 3: return "Matcher Type";
            case 4: return "Matcher Value";
            case 5: return " ";
            default: throw new IndexOutOfBoundsException();
            }
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
         */
        public Object getColumnValue(Object baseObject, int column) {
            final RecordType type = (RecordType) baseObject;
            switch (column) {
            case 0: return type.isSelected() ? Boolean.TRUE : Boolean.FALSE;
            case 1: return type.getName();
            case 2: return type.getColour();
            case 3: return type.getMatcherName();
            case 4: return type.getMatcherValue();
            case 5: return Icons.DELETE;
            default: throw new IndexOutOfBoundsException();
            }
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.WritableTableFormat#isEditable(java.lang.Object, int)
         */
        public boolean isEditable(Object baseObject, int column) {
            // For the default type only the colour can be edited.
            if (RecordTypes.DEFAULT.equals(baseObject)) return column == 2;
            // For other types, allow editing of anything.
            return true;
        }

        /* (non-Javadoc)
         * @see ca.odell.glazedlists.gui.WritableTableFormat#setColumnValue(java.lang.Object, java.lang.Object, int)
         */
        public Object setColumnValue(Object baseObject, Object editedValue, int column) {
            final RecordType type = (RecordType) baseObject;
            try {
                switch (column) {
                case 0:
                    final boolean selected = ((Boolean) editedValue).booleanValue();
                    if (type.isSelected() == selected) return null;
                    type.setSelected(selected);
                    return type;
                case 1:
                    if (type.getName().equals(editedValue)) return null;
                    type.setName((String) editedValue);
                    return type;
                case 2:
                    if (type.getColour().equals(editedValue)) return null;
                    type.setColour((Color) editedValue);
                    return type;
                case 3:
                    if (type.getMatcherName().equals(editedValue)) return null;
                    type.setMatcherName((String) editedValue);
                    return type;
                case 4:
                    if (type.getMatcherValue().equals(editedValue)) return null;
                    type.setMatcherValue((String) editedValue);
                    return type;
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(RecordTypesDialog.this,
                        e.getMessage(), "Warning: Invalid Value",
                        JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
    }

    static String HEADER_MESSAGE = "Drag the record types to re-order their priorities."
        + "\nAll changes will take effect immediately.";

    static String HEADER_TITLE = "Edit Record Types";

    private static final Log log = LogFactory.getLog(RecordTypesDialog.class);

    static final long serialVersionUID = -7458805721398275868L;

    private int dropIndex = -1;

    private final GhostDropPane glass = new GhostDropPane();

    private final JTable typesTable;

    /**
     * Creates a new <code>RecordTypesDialog</code>.
     *
     * @throws HeadlessException if the VM is running in headless mode.
     */
    public RecordTypesDialog() {
        super((Frame) null, true); // true == modal
        setGlassPane(glass);
        Container contents = getContentPane();
        contents.setLayout(new BorderLayout());
        final HeaderPanel header = new HeaderPanel(HEADER_TITLE, HEADER_MESSAGE,
                ImageFactory.getInstance().getBannerImage(EditRecordTypes.COMMAND));
        contents.add(header, BorderLayout.NORTH);
        typesTable = createContents();
        JScrollPane scroller = new JScrollPane(typesTable);
        scroller.setBackground(typesTable.getBackground());
        contents.add(scroller, BorderLayout.CENTER);
        final FooterPanel footer = new FooterPanel(new OKAction(), null, null);
        contents.add(footer, BorderLayout.SOUTH);
        footer.configureActionMap();
    }

    private JTable createContents() {
        final TableModel model = RecordTypes.getInstance().getTableModel(new TypeTableFormat());
        final JTable table = new TypeTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setRowHeight(24);
        final TableColumnModel columns = table.getColumnModel();
        columns.getColumn(1).setCellRenderer(new TextFieldTableCellRenderer());
        columns.getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
        columns.getColumn(2).setCellRenderer(new ColourButtonTableCellRenderer());
        columns.getColumn(2).setCellEditor(new ColourEditor());
        columns.getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox(RecordMatcher.getMatcherNames())));
        columns.getColumn(4).setCellRenderer(new TextFieldTableCellRenderer());
        columns.getColumn(5).setCellEditor(new DeleteEditor());
        final TypeMouseHandler handler = new TypeMouseHandler(table);
        table.addMouseListener(handler);
        table.addMouseMotionListener(handler);
        return table;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            final TableColumnModel columns = typesTable.getColumnModel();
            shrinkColumn(columns, 0);
            shrinkColumn(columns, 2);
            shrinkColumn(columns, 5);
        }
        super.setVisible(visible);
    }

    private void shrinkColumn(TableColumnModel columns, int column) {
        final Object value = typesTable.getValueAt(0, column);
        final TableCellRenderer renderer = typesTable.getCellRenderer(0, column);
        final int width = renderer.getTableCellRendererComponent(typesTable, value, false, false, 0, column).getMinimumSize().width + 4;
        final TableColumn c = columns.getColumn(column);
        c.setMaxWidth(width);
        c.setMinWidth(width);
        c.setPreferredWidth(width);
        c.setResizable(false);
    }

}
