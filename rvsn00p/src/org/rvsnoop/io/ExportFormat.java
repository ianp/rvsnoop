/*
 * Class:     ExportFormat
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import org.rvsnoop.RecordLedgerFormat;

import rvsnoop.Record;

/**
 * Base class for export formats.
 * <p>
 * Export formats are used to describe the different means of exporting records
 * from the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class ExportFormat {

    private static final Map formats = new HashMap();
    
    public static ExportFormat getExportFormat(String displayName) {
        return (ExportFormat) formats.get(displayName);
    }
    
    /**
     * Get human readable display names for all of the known export formats.
     * 
     * @return The known export formats.
     */
    public static String[] getExportFormats() {
        if (formats.size() == 0) {
            final String[] classes = {
                    "org.rvsnoop.io.HTMLExportFormat",
                    "org.rvsnoop.io.RecordBundleExportFormat",
                    "org.rvsnoop.io.RvScriptExportFormat",
                    "org.rvsnoop.io.RvTestExportFormat"
            };
            for (int i = 0, imax = classes.length; i < imax; ++i) {
                try {
                    Class clazz = Class.forName(classes[i]);
                    final ExportFormat format = (ExportFormat) clazz.newInstance();
                    formats.put(format.getDisplayName(), format);
                } catch (Exception e) {
                    // TODO log as INFO
                    e.printStackTrace();
                }
            }
        }
        final Set keys = formats.keySet();
        final String[] names =  (String[]) keys.toArray(new String[keys.size()]);
        Arrays.sort(names);
        return names;
    }
    
    protected File fileOrDirectory;

    /**
     * Called once, after the last record is written.
     * <p>
     * The stream will be flushed and closed automatically, so there is no need
     * to do that in this method.
     */
    public void endExport() throws IOException {
        // Hook for subclasses.
    }

    /**
     * Write a record to the output stream. Called once per record.
     *
     * @param record The record to write.
     * @param index The index of the record being written.
     */
    public abstract void exportRecord(Record record, int index) throws IOException;

    public abstract String getDisplayName();

    /**
     * Get a file filter suitable for selecting files for this output format.
     * <p>
     * If the format uses a directory then no filter is required.
     * 
     * @return The file filter.
     */
    public FileFilter getFileFilter() {
        return null;
    }

    /**
     * Is record metadata included in this format.
     * <p>
     * If this returns false then the format is expected to export only the raw
     * message data.
     * 
     * @return <code>true</code> if the record metadata is exported.
     */
    public boolean isMetadataExported() {
        return false;
    }

    /**
     * Does the format export to a single file or a directory?
     *
     * @return <code>true</code> if a single file is used for the export,
     *     <code>false</code> if a directory is used.
     */
    public boolean isSingleFileExport() {
        return true;
    }
    
    /**
     * Called before the export starts to set the export location.
     * <p>
     * The export location will already have been created and ensured to be
     * writable, it will be a file or directory based on the results of calling
     * {@link #isSingleFileExport()}.
     * 
     * @param fileOrDirectory
     */
    public void setExportLocation(File fileOrDirectory) {
        this.fileOrDirectory = fileOrDirectory;
    }
    
    /**
     * If {@link #isMetadataExported()} is <code>true</code> then this will be
     * called to set the metadata that is to be exported.
     * 
     * @param format The format to use.
     */
    public void setMetadata(RecordLedgerFormat format) {
        // Hook for subclasses.
    }

    /**
     * Called once, before the first record is written.
     *
     * @param numberOfRecords The number of records that will be exported.
     */
    public void startExport(int numberOfRecords) throws IOException {
        // Hook for subclasses.
    }

}
