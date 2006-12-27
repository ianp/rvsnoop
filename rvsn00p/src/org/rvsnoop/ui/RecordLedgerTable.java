/*
 * Class:     RecordLedgerTable
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.rvsnoop.RecordLedger;
import org.rvsnoop.RecordLedgerFormat;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.ui.MessageLedgerRenderer;
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
        MessageLedgerRenderer.installStripedRenderers(this);
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

}
