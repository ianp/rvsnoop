/*
 * Class:     RvTestExportFormat
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

import org.rvsnoop.CausedIOException;
import org.rvsnoop.NLSUtils;

import rvsnoop.Record;

import com.reuters.msgtest.MsgTestException;
import com.reuters.msgtest.XMLConverter;

/**
 * Use the RvTest message format to export records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvTestExportFormat extends ExportFormat {

    private static class RvTestMessagesFileFilter extends FileFilter {
        RvTestMessagesFileFilter() {
            super();
        }
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".msgs");
        }

        public String getDescription() {
            return FILE_DESCRIPTION;
        }
    }
    
    static String DISPLAY_NAME, FILE_DESCRIPTION;
    static String ERROR_CONVERTING;
    
    static { NLSUtils.internationalize(RvTestExportFormat.class); }

    private final XMLConverter converter = new XMLConverter();
    
    private Writer writer;

    /* (non-Javadoc)
     * @see org.rvsnoop.io.ExportFormat#endExport()
     */
    public void endExport() throws IOException {
        writer.close();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.io.ExportFormat#exportRecord(rvsnoop.Record, int)
     */
    public void exportRecord(Record record, int index) throws IOException {
        try {
            writer.write(converter.createXML(record.getMessage()).toString());
        } catch (MsgTestException e) {
            throw new CausedIOException(ERROR_CONVERTING, e);
        }
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.io.ExportFormat#getDisplayName()
     */
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.io.ExportFormat#getFileFilter()
     */
    public FileFilter getFileFilter() {
        return new RvTestMessagesFileFilter();
    }

    /* (non-Javadoc)
     * @see org.rvsnoop.io.ExportFormat#startExport(int)
     */
    public void startExport(int numberOfRecords) throws IOException {
        FileWriter fileWriter = new FileWriter(fileOrDirectory);
        int bufferSize = numberOfRecords * 1024;
        writer = new BufferedWriter(fileWriter, bufferSize);
    }

}
