// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.actions.RvSnoopAction;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * An abstract class that handles the basics of importing messages to the ledger.
 */
public abstract class ImportFromFile extends RvSnoopAction {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final Logger logger = Logger.getLogger();

    private final FileFilter filter;

    protected ImportFromFile(Application application, String id, FileFilter filter) {
        super(id, application);
        putValue(Action.ACTION_COMMAND_KEY, id);
        this.filter = filter;
    }

    public void actionPerformed(ActionEvent event) {
        final JFileChooser chooser = new JFileChooser();
        if (filter != null) chooser.setFileFilter(filter);
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(application.getFrame()))
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
            logger.info("Importing records from %s.", file.getPath());
            importRecords(stream);
            logger.info("Imported records from %s.", file.getPath());
        } catch (IOException e) {
            logger.error(e, "There was a problem importing the file %s.", file.getPath());
        } finally {
            closeQuietly(stream);
        }
    }

    /**
     * Import the records contained in the stream.
     *
     * @param stream The stream containing the file contents.
     */
    protected abstract void importRecords(InputStream stream) throws IOException;

}
