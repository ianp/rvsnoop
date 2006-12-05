//:File:    ImportFromFile.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
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

import rvsnoop.Logger;
import rvsnoop.ui.UIManager;

/**
 * An abstract class that handles the basics of importing messages to the ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
abstract class ImportFromFile extends AbstractAction {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final Logger logger = Logger.getLogger(ImportFromFile.class);

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
        if (JFileChooser.APPROVE_OPTION != chooser.showOpenDialog(UIManager.INSTANCE.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
            logger.info("Importing records from " + file.getPath() + ".");
            importFromFile(stream);
            logger.info("Imported records from " + file.getPath() + ".");
        } catch (IOException e) {
            logger.error("There was a problem importing the file" + file.getPath() + ".", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Import the records contained in the stream.
     *
     * @param stream The stream containing the file contents.
     */
    protected abstract void importFromFile(InputStream stream) throws IOException;

}
