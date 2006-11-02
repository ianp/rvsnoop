//:File:    ExportToRvTest.java
//:Created: Jan 6, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;

import rvsnoop.Logger;
import rvsnoop.Marshaller;
import rvsnoop.Record;
import rvsnoop.ui.UIManager;

/**
 * Export the current ledger selction to an RvTest format message file.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToRvTest extends LedgerSelectionAction {
    private static class RvTestMessagesFileFilter extends FileFilter {
        RvTestMessagesFileFilter() {
            super();
        }
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".msgs");
        }

        public String getDescription() {
            return "RvTest Messages Files";
        }
    }

    private static final String ID = "exportToRvTest";

    private static final Logger logger = Logger.getLogger(ExportToRvTest.class);

    private static String NAME = "RvTest Messages File";

    private static final long serialVersionUID = -483492422948058345L;

    private static String TOOLTIP = "Export the current ledger selction to an RvTest format message file";

    private final Marshaller.Implementation marshaller = Marshaller.getImplementation(Marshaller.IMPL_RVTEST);

    public ExportToRvTest() {
        super(ID, NAME, null);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        setEnabled(marshaller != null);
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected void actionPerformed(Record[] records) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new RvTestMessagesFileFilter());
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(UIManager.INSTANCE.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0, imax = records.length; i < imax; ++i)
                bw.write(marshaller.marshal("", records[i].getMessage()));
            logger.info("Written RvTest messages file to " + file.getName());
        } catch (IOException e) {
            logger.error("There was a problem writing the RvTest messages file.", e);
        } finally {
            IOUtils.closeQuietly(bw);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled && marshaller != null);
    }
}
