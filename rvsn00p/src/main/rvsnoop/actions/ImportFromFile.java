/*
 * Class:     ImportFromFile
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.ui.MainFrame;

/**
 * An abstract class that handles the basics of importing messages to the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public abstract class ImportFromFile extends AbstractAction {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final Log log = LogFactory.getLog(ImportFromFile.class);

    private final FileFilter filter;

    protected ImportFromFile(String id, String name, Icon icon, FileFilter filter) {
        super(name, icon);
        putValue(Action.ACTION_COMMAND_KEY, id);
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        if (filter != null) chooser.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(MainFrame.INSTANCE))
            return;
        final File file = chooser.getSelectedFile();
        importRecords(file);
    }

    /**
     * Import the records contained in the file.
     *
     * @param file The file containing the records.
     */
    public void importRecords(final File file) {
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
            if (log.isInfoEnabled()) {
                log.info("Importing records from " + file.getPath() + '.');
            }
            importRecords(stream);
            if (log.isInfoEnabled()) {
                log.info("Imported records from " + file.getPath() + '.');
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("There was a problem importing the file" + file.getPath() + '.', e);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Import the records contained in the stream.
     *
     * @param stream The stream containing the file contents.
     */
    protected abstract void importRecords(InputStream stream) throws IOException;

}
