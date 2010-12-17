/*
 * Class:     RecordLedgerSelectionEvent
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.event;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.EventObject;

import javax.swing.ListSelectionModel;

import org.rvsnoop.RecordLedger;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.Record;

/**
 * An event object encapsulating the current state of the record ledger selection.
 * <p>
 * Note that this does <em>not</em> extend {@link javax.swing.event.ListSelectionEvent}
 * as that class contains only information about the selection delta and this
 * event object contains all of the selection information.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecordLedgerSelectionEvent extends EventObject {

    private static final long serialVersionUID = 7498998301457154298L;

    private int[] selectedIndices;

    private Record[] selectedRecords;

    private final boolean selectionEmpty;

    /**
     * Create a new <code>RecordLedgerSelectionEvent</code>.
     *
     * @param source The table that the selection applies to.
     */
    public RecordLedgerSelectionEvent(RecordLedgerTable source) {
        super(source);
        selectionEmpty = source.getSelectionModel().isSelectionEmpty();
    }

    /**
     * Returns the list of indices that are currently selected.
     * <p>
     * Returns an empty array of {@link #isSelectionEmpty()} is <code>true</code>.
     *
     * @return The selected indices.
     */
    public synchronized int[] getSelectedIndices() {
        if (selectedIndices == null) { populateRecordsAndIndices(); }
        return selectedIndices;
    }

    /**
     * Returns the list of records that are currently selected.
     * <p>
     * Returns an empty array of {@link #isSelectionEmpty()} is <code>true</code>.
     *
     * @return The selected records.
     */
    public synchronized Record[] getSelectedRecords() {
        if (selectedRecords == null) { populateRecordsAndIndices(); }
        return selectedRecords;
    }

    /**
     * Returns true if no records are selected.
     *
     * @return <code>true</code> if no records are selected, <code>false</code>
     *     otherwise.
     * @see javax.swing.ListSelectionModel#isSelectionEmpty()
     */
    public boolean isSelectionEmpty() {
        return selectionEmpty;
    }

    private void populateRecordsAndIndices() {
        final RecordLedgerTable table = (RecordLedgerTable) source;
        final RecordLedger ledger = table.getRecordLedger();
        final ListSelectionModel selection = table.getSelectionModel();
        final int min = selection.getMinSelectionIndex();
        final int max = selection.getMaxSelectionIndex();
        final Record[] records = new Record[max - min];
        final int[] indices = new int[max - min];
        int count = 0;
        for (int i = min; i < max; ++i) {
            if (!selection.isSelectedIndex(i)) { continue; }
            records[count] = ledger.get(i);
            indices[count] = i;
            ++count;
        }
        selectedRecords = new Record[count];
        selectedIndices = new int[count];
        System.arraycopy(records, 0, selectedRecords, 0, count);
        System.arraycopy(indices, 0, selectedIndices, 0, count);
    }

    /* This is here for serialization. */
    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        if (selectedIndices == null) { populateRecordsAndIndices(); }
    }
}
