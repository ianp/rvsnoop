// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.event.RecordLedgerSelectionEvent;
import org.rvsnoop.event.RecordLedgerSelectionListener;

import rvsnoop.Record;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * An abstract class that handles the basics of exporting messages from the ledger.
 */
public abstract class ExportToFile extends RvSnoopAction implements RecordLedgerSelectionListener {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final Logger logger = Logger.getLogger();

    private transient Record[] currentSelection;

    private final FileFilter filter;

    protected OutputStream stream;

    protected ExportToFile(Application application, String command, String name, FileFilter filter) {
        super(name, application);
        putValue(Action.ACTION_COMMAND_KEY, command);
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public final void actionPerformed(ActionEvent event) {
        actionPerformed(currentSelection);
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected final void actionPerformed(Record[] records) {
        final JFileChooser chooser = new JFileChooser();
        if (filter != null) chooser.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(application.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        exportRecords(records, file);
    }

    /**
     * Export a group of records to a file.
     *
     * @param records The records to export.
     * @param file The file to export to.
     */
    public void exportRecords(Record[] records, final File file) {
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
            logger.info("Exporting %s records to %s.", records.length, file.getPath());
            writeHeader(records.length);
            for (int i = 0, imax = records.length; i < imax; ++i) {
                writeRecord(records[i], i);
            }
            writeFooter();
            logger.info("Exported %s records to %s.", records.length, file.getPath());
        } catch (IOException e) {
            logger.error(e, "There was a problem exporting the selected records.");
        } finally {
            closeQuietly(stream);
        }
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.event.RecordLedgerSelectionListener#valueChanged(org.rvsnoop.event.RecordLedgerSelectionEvent)
     */
    public void valueChanged(RecordLedgerSelectionEvent event) {
        currentSelection = event.getSelectedRecords();
        setEnabled(currentSelection.length > 0);
    }

    /**
     * Write a record to the output stream. Called once per record.
     *
     * @param record The record to write.
     * @param index The index of the record being written.
     */
    protected abstract void writeRecord(Record record, int index) throws IOException;

    /**
     * Called once, before the first record is written.
     *
     * @param numberOfRecords The number of records that will be exported.
     */
    protected void writeHeader(int numberOfRecords) throws IOException {
        // Hook for subclasses.
    }

    /**
     * Called once, after the last record is written.
     */
    protected void writeFooter() throws IOException {
        // Hook for subclasses.
    }

}
