//:File:    RecordLedgerTransferHandler.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import com.tibco.tibrv.TibrvException;

import rvsnoop.MessageLedger;
import rvsnoop.Record;
import rvsnoop.RecordSelection;

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
final class RecordLedgerTransferHandler extends TransferHandler {

    static final String ERROR_REMOVING_RECORDS = "Error (re)moving records.";

    static final long serialVersionUID = -326903741431091606L;

    RecordLedgerTransferHandler() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
     */
    public boolean canImport(JComponent comp, DataFlavor[] flavours) {
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
    protected Transferable createTransferable(JComponent c) {
        final MessageLedger ledger = MessageLedger.INSTANCE;
        final int[] indices = ((JTable) c).getSelectedRows();
        return new RecordSelection(ledger.getRecords(indices));
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
     */
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (action != MOVE) return;
        final MessageLedger ledger = MessageLedger.INSTANCE;
        try {
            ledger.removeAll(Arrays.asList(RecordSelection.read(data)));
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
    public boolean importData(JComponent comp, Transferable data) {
        final MessageLedger ledger = MessageLedger.INSTANCE;
        try {
            final Record[] records = RecordSelection.read(data);
            for (int i = 0, imax = records.length; i < imax; ++i)
                ledger.addRecord(records[i]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
