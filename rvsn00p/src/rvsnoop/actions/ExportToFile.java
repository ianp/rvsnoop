//:File:    ExportToFile.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;

import rvsnoop.Logger;
import rvsnoop.Record;
import rvsnoop.ui.UIManager;

/**
 * An abstract class that handles the basics of exporting messages from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
abstract class ExportToFile extends LedgerSelectionAction {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final Logger logger = Logger.getLogger(ExportToFile.class);

    private final FileFilter filter;

    protected OutputStream stream;

    protected ExportToFile(String id, String name, Icon icon, FileFilter filter) {
        super(id, name, icon);
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected final void actionPerformed(Record[] records) {
        final JFileChooser chooser = new JFileChooser();
        if (filter != null) chooser.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(UIManager.INSTANCE.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
            logger.info("Exporting " + records.length + " records to " + file.getPath() + ".");
            writeHeader(records.length);
            for (int i = 0, imax = records.length; i < imax; ++i)
                writeRecord(records[i], i);
            writeFooter();
            logger.info("Exported " + records.length + " records to " + file.getPath() + ".");
        } catch (IOException e) {
            logger.error("There was a problem exporting the selected records.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
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
