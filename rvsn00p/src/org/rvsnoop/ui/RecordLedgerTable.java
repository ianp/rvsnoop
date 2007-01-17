/*
 * Class:     RecordLedgerTable
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.RecordLedgerFormat;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.RecordTypes;
import rvsnoop.ui.UIUtils;

import com.tibco.tibrv.TibrvException;

import ca.odell.glazedlists.swing.EventTableModel;

/**
 * A custom <code>JTable</code> that is used to draw the record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RecordLedgerTable extends JTable {

    private static class DateCellRenderer extends DefaultTableCellRenderer {
        private static final Log log = LogFactory.getLog(DateCellRenderer.class);
        private static final long serialVersionUID = -6397207684112537883L;
        private static final DateFormat[] FORMATS = {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("MM/dd HH:mm:ss.SSS"),
            new SimpleDateFormat("HH:mm:ss.SSS"),
            new SimpleDateFormat("HH:mm:ss.SS"),
            new SimpleDateFormat("HH:mm:ss.S"),
            new SimpleDateFormat("HH:mm:ss"),
            new SimpleDateFormat("HH:mm") };
        private int currentWidth;
        private Font currentFont;
        private DateFormat currentFormat;
        DateCellRenderer() {
            super();
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            final DateFormat format = getFormat(table.getColumnModel().getColumn(col).getWidth(), table);
            final String displayed = value != null ? format.format((Date) value) : "";
            return super.getTableCellRendererComponent(table, displayed, isSelected, hasFocus, row, col);
        }
        private DateFormat getFormat(int width, JTable table) {
            final Font font = table.getFont();
            if (currentWidth != width) currentFormat = null;
            if (currentFont == null || !currentFont.equals(font)) currentFormat = null;
            if (currentFormat == null) {
                currentWidth = width;
                currentFont = font;
                final Date date = new Date();
                final FontMetrics metrics = table.getFontMetrics(font);
                for (int i = 0, imax = FORMATS.length; i < imax; ++i) {
                    final int dateWidth = metrics.stringWidth(FORMATS[i].format(date));
                    if (dateWidth < width) {
                        currentFormat = FORMATS[i];
                        if (log.isDebugEnabled()) {
                            log.debug("Setting date format to " + FORMATS[i].format(date));
                        }
                        break;
                    }
                }
                if (currentFormat == null) {
                    currentFormat = FORMATS[FORMATS.length - 1];
                    if (log.isDebugEnabled()) {
                        log.debug("Setting date format to " + FORMATS[FORMATS.length - 1].format(date));
                    }
                }
            }
            return currentFormat;
        }
    }

    private class SelectionHandler implements ListSelectionListener {
        SelectionHandler() {
            super();
        }
        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            final EventListener[] listeners =
                listenerList.getListeners(RecordLedgerSelectionListener.class);
            if (listeners == null) { return; }
            final RecordLedgerSelectionEvent event =
                new RecordLedgerSelectionEvent(RecordLedgerTable.this);
            for (int i = 0, imax = listeners.length; i < imax; ++i) {
                ((RecordLedgerSelectionListener) listeners[i]).valueChanged(event);
            }
        }
    }

    public final class StripedCellRenderer implements TableCellRenderer {

        private final TableCellRenderer baseRenderer;

        private final Color evenRowsColor;

        private final Color oddRowsColor;

        private final RecordTypes types = RecordTypes.getInstance();

        private StripedCellRenderer(TableCellRenderer baseRenderer) {
            this.baseRenderer = baseRenderer;
            this.evenRowsColor = Color.WHITE;
            this.oddRowsColor = new Color(229, 229, 255);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            TableCellRenderer renderer = baseRenderer;
            if (renderer == null) {
                final Class rendererClass = value != null ? value.getClass() : Object.class;
                renderer = table.getDefaultRenderer(rendererClass);
            }
            final Component component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                component.setForeground(types.getFirstMatchingType(ledger.get(row)).getColour());
                component.setBackground(row % 2 == 0 ? evenRowsColor : oddRowsColor);
            }
            return component;
        }

    }

    /**
     * A handler to support importing and exporting records to/from the ledger.
     * <p>
     * This class delegates most of the work to {@linkplain RecordSelection}, it
     * just handles the UI specific elements.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     * @since 1.6
     */
    final class TransferHandler extends javax.swing.TransferHandler {

        private static final long serialVersionUID = 9177742349573744479L;
        static final String ERROR_REMOVING_RECORDS = "Error (re)moving records.";

        TransferHandler() {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
         */
        public boolean canImport(JComponent target, DataFlavor[] flavours) {
            if (!RecordLedgerTable.this.equals(target)) { return false; }
            for (int i = 0, imax = flavours.length; i < imax; ++i) {
                final DataFlavor f = flavours[i];
                if (RecordSelection.BYTES_FLAVOUR.equals(f) || f.isFlavorJavaFileListType())
                    return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
         */
        protected Transferable createTransferable(JComponent source) {
            if (!RecordLedgerTable.this.equals(source)) { return null; }
            final int[] indices = ((JTable) source).getSelectedRows();
            final Record[] records = getRecordLedger().getAll(indices);
            return new RecordSelection(records);
        }

        /* (non-Javadoc)
         * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
         */
        protected void exportDone(JComponent source, Transferable data, int action) {
            if (!RecordLedgerTable.this.equals(source)) { return; }
            if (action != MOVE) return;
            try {
                getRecordLedger().removeAll(Arrays.asList(RecordSelection.read(data)));
            } catch (UnsupportedFlavorException e) {
                UIUtils.showError(ERROR_REMOVING_RECORDS, e);
            } catch (IOException e) {
                UIUtils.showError(ERROR_REMOVING_RECORDS, e);
            } catch (TibrvException e) {
                UIUtils.showError(ERROR_REMOVING_RECORDS, e);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
         */
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        /* (non-Javadoc)
         * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
         */
        public boolean importData(JComponent target, Transferable data) {
            if (!RecordLedgerTable.this.equals(target)) { return false; }
            try {
                getRecordLedger().addAll(Arrays.asList(RecordSelection.read(data)));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static final long serialVersionUID = 7601999759173563496L;

    private final RecordLedger ledger;

    public RecordLedgerTable(RecordLedger ledger) {
        super(ledger.createTableModel());
        this.ledger = ledger;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder());
        setShowGrid(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        setDragEnabled(true);
        setTransferHandler(new TransferHandler());
        getSelectionModel().addListSelectionListener(new SelectionHandler());
        configureRenderers();
    }

    /**
     * Adds a listener to the ledger that is notified each time a change to the
     * selection has occurred.
     *
     * @param listener The listener to add.
     */
    public void addRecordLedgerSelectionListener(RecordLedgerSelectionListener listener) {
        listenerList.add(RecordLedgerSelectionListener.class, listener);
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#columnAdded(javax.swing.event.TableColumnModelEvent)
     */
    public void columnAdded(TableColumnModelEvent e) {
        super.columnAdded(e);
        configureRenderers();
    }

    /* (non-Javadoc)
     * @see javax.swing.JTable#columnRemoved(javax.swing.event.TableColumnModelEvent)
     */
    public void columnRemoved(TableColumnModelEvent e) {
        super.columnRemoved(e);
        configureRenderers();
    }

    private void configureRenderers() {
        try {
            final StripedCellRenderer striper = new StripedCellRenderer(null);
            final TableColumnModel columns = getColumnModel();
            for (int i = 0, imax = columns.getColumnCount(); i < imax; ++i) {
                final Class columnClass = getColumnClass(i);
                if (Date.class.isAssignableFrom(columnClass)) {
                    columns.getColumn(i).setCellRenderer(
                            new StripedCellRenderer(new DateCellRenderer()));
                } else {
                    columns.getColumn(i).setCellRenderer(striper);
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
            // This happens when the table structure is changed, the method
            // JTable#createDefaultColumnsFromModel() gets called and this first
            // removes all columns from the model then adds them again.
            // Unfortunately, while it does this the column model itself is in
            // an inconsistent state and trying to access it causes this
            // exception. I'm not sure if this is a bug in Swing or a threading
            // issue, but ignoring it here seems to produce no errors.
        }
    }

    /**
     * Get the record ledger that is viewed in this table.
     *
     * @return The ledger.
     */
    public RecordLedger getRecordLedger() {
        return ledger;
    }

    /**
     * Convenience method to access the table format used by this table.
     *
     * @return The format.
     */
    public RecordLedgerFormat getTableFormat() {
        final EventTableModel model = ((EventTableModel) getModel());
        return (RecordLedgerFormat) model.getTableFormat();
    }

    /**
     * Removes a listener from the ledger.
     *
     * @param listener The listener to remove.
     */
    public void removeRecordLedgerSelectionListener(RecordLedgerSelectionListener listener) {
        listenerList.remove(RecordLedgerSelectionListener.class, listener);
    }

}
